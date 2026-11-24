package com.theneru.knocker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_5218;
import net.minecraft.server.MinecraftServer;

public final class KnockerMemoryStore {
   public static final int HISTORY_LIMIT = 120;
   public static final int DIARY_LIMIT = 200;
   public static final int STYLE_LIMIT = 80;
   /** Сколько последних записей уходит в системный промпт. */
   public static final int PROMPT_HISTORY = 50;
   public static final int PROMPT_DIARY = 80;
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
   private static final Map<UUID, Memory> CACHE = new HashMap();

   private KnockerMemoryStore() {
   }

   private static Path file(UUID player) {
      MinecraftServer server = AiKnocker.server;
      Path dir = server != null ? server.method_27050(class_5218.field_24188).resolve("aiknocker_memory") : FabricLoader.getInstance().getConfigDir().resolve("aiknocker_memory");
      return dir.resolve(String.valueOf(player) + ".json");
   }

   private static Path globalFile(UUID player) {
      return FabricLoader.getInstance().getConfigDir().resolve("aiknocker_global").resolve(String.valueOf(player) + ".txt");
   }

   public static synchronized Set<String> globalMarks(UUID player) {
      try {
         Path f = globalFile(player);
         return Files.exists(f, new LinkOption[0]) ? new HashSet(Files.readAllLines(f, StandardCharsets.UTF_8)) : new HashSet();
      } catch (IOException var2) {
         return new HashSet();
      }
   }

   public static synchronized void addGlobalMark(UUID player, String mark) {
      Set<String> marks = globalMarks(player);
      if (marks.add(mark)) {
         try {
            Files.createDirectories(globalFile(player).getParent());
            Files.write(globalFile(player), marks, StandardCharsets.UTF_8);
         } catch (IOException e) {
            AiKnocker.LOGGER.warn("Не удалось сохранить отметку", e);
         }

      }
   }

   public static synchronized UUID worldChosen() {
      MinecraftServer server = AiKnocker.server;
      if (server == null) {
         return null;
      } else {
         Path f = server.method_27050(class_5218.field_24188).resolve("aiknocker_memory").resolve("chosen.txt");

         try {
            return Files.exists(f, new LinkOption[0]) ? UUID.fromString(Files.readString(f, StandardCharsets.UTF_8).trim()) : null;
         } catch (Exception var3) {
            return null;
         }
      }
   }

   public static synchronized void setWorldChosen(UUID player) {
      MinecraftServer server = AiKnocker.server;
      if (server != null) {
         Path f = server.method_27050(class_5218.field_24188).resolve("aiknocker_memory").resolve("chosen.txt");

         try {
            Files.createDirectories(f.getParent());
            if (player == null) {
               Files.deleteIfExists(f);
            } else {
               Files.writeString(f, player.toString(), StandardCharsets.UTF_8);
            }
         } catch (IOException e) {
            AiKnocker.LOGGER.warn("Не удалось сохранить избранного", e);
         }

      }
   }

   public static synchronized void clearCache() {
      CACHE.clear();
   }

   public static synchronized Memory get(UUID player) {
      return (Memory)CACHE.computeIfAbsent(player, (id) -> {
         Path path = file(id);
         if (Files.exists(path, new LinkOption[0])) {
            try {
               Reader r = Files.newBufferedReader(path, StandardCharsets.UTF_8);

               Memory var4;
               label72: {
                  try {
                     Memory m = (Memory)GSON.fromJson(r, Memory.class);
                     if (m != null) {
                        if (m.history == null) {
                           m.history = new ArrayList();
                        }

                        if (m.diary == null) {
                           m.diary = new ArrayList();
                        }

                        if (m.herLines == null) {
                           m.herLines = new ArrayList();
                        }

                        if (m.facts == null) {
                           m.facts = new HashMap();
                        }

                        cleanDiary(m);
                        var4 = m;
                        break label72;
                     }
                  } catch (Throwable var6) {
                     if (r != null) {
                        try {
                           r.close();
                        } catch (Throwable x2) {
                           var6.addSuppressed(x2);
                        }
                     }

                     throw var6;
                  }

                  if (r != null) {
                     r.close();
                  }

                  return new Memory();
               }

               if (r != null) {
                  r.close();
               }

               return var4;
            } catch (Exception e) {
               AiKnocker.LOGGER.warn("Не удалось прочитать память {}", path, e);
            }
         }

         return new Memory();
      });
   }

   public static synchronized void save(UUID player) {
      Memory m = (Memory)CACHE.get(player);
      if (m != null) {
         Path path = file(player);

         try {
            Files.createDirectories(path.getParent());
            Writer w = Files.newBufferedWriter(path, StandardCharsets.UTF_8);

            try {
               GSON.toJson(m, w);
            } catch (Throwable var7) {
               if (w != null) {
                  try {
                     w.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (w != null) {
               w.close();
            }
         } catch (IOException e) {
            AiKnocker.LOGGER.warn("Не удалось сохранить память {}", path, e);
         }

      }
   }

   public static synchronized void addMessage(UUID player, String role, String text) {
      Memory m = get(player);
      m.history.add(new String[]{role, text});
      save(player);
   }

   public static synchronized List<String[]> recent(UUID player, int n) {
      List<String[]> h = get(player).history;
      return new ArrayList(h.subList(Math.max(0, h.size() - n), h.size()));
   }

   public static synchronized List<String[]> takeOldestIfLong(UUID player) {
      Memory m = get(player);
      if (m.history.size() <= HISTORY_LIMIT) {
         return null;
      } else {
         int count = m.history.size() - HISTORY_LIMIT + PROMPT_HISTORY;
         List<String[]> old = new ArrayList(m.history.subList(0, count));
         m.history.subList(0, count).clear();
         save(player);
         return old;
      }
   }

   public static synchronized void addDiary(UUID player, List<String> facts) {
      Memory m = get(player);

      for(String f : facts) {
         String fact = f.trim();
         if (goodFact(fact) && !isDuplicate(m.diary, fact)) {
            m.diary.add(fact);
         }
      }

      while(m.diary.size() > DIARY_LIMIT) {
         m.diary.remove(0);
      }

      save(player);
   }

   private static boolean goodFact(String fact) {
      // Фильтр JUNK отключён: обещания, свадьба, дети и чувства — это как раз сюжет.
      return fact.length() >= 3 && fact.length() <= 220;
   }

   private static boolean isDuplicate(List<String> diary, String fact) {
      Set<String> a = words(fact);

      for(String d : diary) {
         Set<String> b = words(d);
         if (!a.isEmpty() && !b.isEmpty()) {
            Set<String> common = new HashSet(a);
            common.retainAll(b);
            if ((double)common.size() >= 0.7 * (double)Math.min(a.size(), b.size())) {
               return true;
            }
         }
      }

      return false;
   }

   private static Set<String> words(String s) {
      Set<String> w = new HashSet();

      for(String x : s.toLowerCase().replace('ё', 'е').split("[^\\p{L}]+")) {
         if (x.length() > 2) {
            w.add(x);
         }
      }

      return w;
   }

   private static void cleanDiary(Memory m) {
      List<String> clean = new ArrayList();

      for(String f : m.diary) {
         if (goodFact(f) && !isDuplicate(clean, f)) {
            clean.add(f);
         }
      }

      m.diary = clean;
   }

   public static synchronized List<String> diary(UUID player) {
      return new ArrayList(get(player).diary);
   }

   public static synchronized void addHerLine(UUID player, String text) {
      if (text != null) {
         String t = text.trim();
         if (!t.isEmpty() && !t.startsWith("/") && t.length() <= 150) {
            Memory m = get(player);
            m.herLines.add(t);

            while(m.herLines.size() > 160) {
               m.herLines.remove(0);
            }

            save(player);
         }
      }
   }

   public static synchronized List<String> herLines(UUID player) {
      return new ArrayList(get(player).herLines);
   }

   public static synchronized String gender(UUID player) {
      return get(player).gender;
   }

   public static synchronized void setGender(UUID player, String gender) {
      get(player).gender = gender;
      save(player);
   }

   public static synchronized Map<String, String> facts(UUID player) {
      return new HashMap(get(player).facts);
   }

   public static synchronized void setFact(UUID player, String key, String value) {
      get(player).facts.put(key, value);
      save(player);
   }

   public static synchronized void forgetExcept(UUID player, String keepFact) {
      Memory m = new Memory();
      m.herLines = get(player).herLines;
      m.diary.add(keepFact);
      CACHE.put(player, m);
      save(player);
   }

   public static synchronized void forget(UUID player) {
      CACHE.put(player, new Memory());
      save(player);
   }

   public static final class Memory {
      public List<String[]> history = new ArrayList();
      public List<String> diary = new ArrayList();
      public List<String> herLines = new ArrayList();
      public String gender = null;
      public Map<String, String> facts = new HashMap();
   }
}
