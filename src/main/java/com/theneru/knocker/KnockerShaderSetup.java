package com.theneru.knocker;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class KnockerShaderSetup implements PreLaunchEntrypoint {
   private static final String INSANITY_OPTIONS = "FOG_DENSITY=1.50\nLIGHT_SHAFT=true\nLIGHT_SHAFT_STRENGTH=1.25\nSATURATION=0.85\nWEATHER_RR=148\nWEATHER_RG=150\nWEATHER_RB=158\nshadowMapResolution=1024\nshadowDistance=128.0\nREFLECTION=1\n";

   public void onPreLaunch() {
      try {
         run();
      } catch (Throwable t) {
         System.out.println("[AI Knocker] Не удалось настроить шейдер: " + String.valueOf(t));
      }

   }

   private static void run() throws IOException {
      FabricLoader fl = FabricLoader.getInstance();
      if (fl.isModLoaded("iris")) {
         Path cfg = fl.getConfigDir().resolve("aiknocker.properties");
         Properties p = new Properties();
         if (Files.exists(cfg, new LinkOption[0])) {
            Reader r = Files.newBufferedReader(cfg, StandardCharsets.UTF_8);

            try {
               p.load(r);
            } catch (Throwable var13) {
               if (r != null) {
                  try {
                     r.close();
                  } catch (Throwable var12) {
                     var13.addSuppressed(var12);
                  }
               }

               throw var13;
            }

            if (r != null) {
               r.close();
            }
         }

         if (Boolean.parseBoolean(p.getProperty("auto_shader", "true").trim())) {
            if (!"done".equalsIgnoreCase(p.getProperty("shader_setup", "ask").trim())) {
               Path packs = fl.getGameDir().resolve("shaderpacks");
               if (Files.isDirectory(packs, new LinkOption[0])) {
                  String pack = null;
                  boolean insanity = false;
                  Stream<Path> s = Files.list(packs);

                  try {
                     Objects.requireNonNull(s);

                     for(Path f : s.toList()) {
                        String n = f.getFileName().toString();
                        String low = n.toLowerCase(Locale.ROOT);
                        if (!low.endsWith(".txt")) {
                           if (low.contains("insanity")) {
                              pack = n;
                              insanity = true;
                              break;
                           }

                           if (low.contains("hysteria") && pack == null) {
                              pack = n;
                           }
                        }
                     }
                  } catch (Throwable var14) {
                     if (s != null) {
                        try {
                           s.close();
                        } catch (Throwable var11) {
                           var14.addSuppressed(var11);
                        }
                     }

                     throw var14;
                  }

                  if (s != null) {
                     s.close();
                  }

                  if (pack != null) {
                     Path opts = packs.resolve(pack + ".txt");
                     if (insanity && Files.notExists(opts, new LinkOption[0])) {
                        Files.writeString(opts, "FOG_DENSITY=1.50\nLIGHT_SHAFT=true\nLIGHT_SHAFT_STRENGTH=1.25\nSATURATION=0.85\nWEATHER_RR=148\nWEATHER_RG=150\nWEATHER_RB=158\nshadowMapResolution=1024\nshadowDistance=128.0\nREFLECTION=1\n", StandardCharsets.UTF_8);
                     }

                     Path iris = fl.getConfigDir().resolve("iris.properties");
                     setKeys(iris, new String[][]{{"shaderPack", pack}, {"enableShaders", "true"}});
                     setKeys(cfg, new String[][]{{"shader_setup", "done"}});
                     System.out.println("[AI Knocker] Шейдер " + pack + " включён и настроен.");
                  }
               }
            }
         }
      }
   }

   private static void setKeys(Path file, String[][] kv) throws IOException {
      List<String> lines = Files.exists(file, new LinkOption[0]) ? new ArrayList(Files.readAllLines(file, StandardCharsets.UTF_8)) : new ArrayList();

      for(String[] e : kv) {
         String key = e[0];
         String val = e[1].replace("\\", "\\\\");
         boolean found = false;

         for(int i = 0; i < lines.size(); ++i) {
            String l = ((String)lines.get(i)).trim();
            if (l.startsWith(key + "=") || l.startsWith(key + " =")) {
               lines.set(i, key + "=" + val);
               found = true;
            }
         }

         if (!found) {
            lines.add(key + "=" + val);
         }
      }

      Files.createDirectories(file.getParent());
      Files.write(file, lines, StandardCharsets.UTF_8);
   }
}
