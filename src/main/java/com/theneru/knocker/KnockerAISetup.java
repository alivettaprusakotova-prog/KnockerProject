package com.theneru.knocker;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2568;
import net.minecraft.class_3222;
import net.minecraft.class_5250;
import net.minecraft.class_2558.class_2559;
import net.minecraft.class_2568.class_5247;
import net.minecraft.server.MinecraftServer;

public final class KnockerAISetup {
   public static final String MODEL = "gemma3:4b";
   private static final String OLLAMA_URL = "http://127.0.0.1:11434";
   private static final String ZIP_URL = "https://github.com/ollama/ollama/releases/latest/download/ollama-windows-amd64.zip";
   private static final HttpClient HTTP;
   private static volatile boolean busy;
   private static volatile String status;
   private static volatile Process ollamaProcess;
   private static volatile MinecraftServer server;

   private KnockerAISetup() {
   }

   private static Path root() {
      return FabricLoader.getInstance().getGameDir().resolve("aiknocker");
   }

   private static Path portableExe() {
      return root().resolve("ollama").resolve("ollama.exe");
   }

   private static boolean windows() {
      return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
   }

   public static void onServerStarted(MinecraftServer srv) {
      server = srv;
      if ("done".equals(KnockerConfig.aiSetup) || "installing".equals(KnockerConfig.aiSetup)) {
         runAsync(() -> {
            ensureOllamaRunning();
            if (hasModel()) {
               useModel();
            } else {
               install();
            }

         });
      }
   }

   public static void onServerStopped() {
      Process p = ollamaProcess;
      if (p != null) {
         p.destroy();
      }

      ollamaProcess = null;
      server = null;
   }

   public static void onJoin(class_3222 player) {
      if (!busy && !"done".equals(KnockerConfig.aiSetup) && !"declined".equals(KnockerConfig.aiSetup)) {
         if (!KnockerConfig.useExternalAI() || KnockerConfig.apiUrl.contains("11434")) {
            runAsync(() -> {
               if (ping()) {
                  status = "Ollama найдена";
                  install();
               } else {
                  MinecraftServer srv = server;
                  if (srv != null) {
                     srv.execute(() -> ask(player));
                  }

               }
            });
         }
      }
   }

   private static void ask(class_3222 player) {
      if (!windows()) {
         player.method_7353(class_2561.method_43470("§7[AI Knocker] Чтобы Кнокер думал сам, установи Ollama с ollama.com и перезайди в мир — дальше всё настроится автоматически."), false);
      } else {
         class_5250 yes = class_2561.method_43470("§a§l[УСТАНОВИТЬ ИИ]").method_27694((s) -> s.method_10958(new class_2558(class_2559.field_11750, "/knockerai install")).method_10949(new class_2568(class_5247.field_24342, class_2561.method_43470("Скачается ~6 ГБ в папку .minecraft/aiknocker\nВ систему ничего не ставится"))));
         class_5250 no = class_2561.method_43470("§7[без ИИ]").method_27694((s) -> s.method_10958(new class_2558(class_2559.field_11750, "/knockerai no")));
         player.method_7353(class_2561.method_43470("§5[AI Knocker] §fКнокеру нужен ИИ, чтобы думать как живой. Скачать? (~6 ГБ, один раз, бесплатно, работает без интернета)"), false);
         player.method_7353(class_2561.method_43470("   ").method_10852(yes).method_10852(class_2561.method_43470("   ")).method_10852(no), false);
      }
   }

   public static void startInstall() {
      if (!busy) {
         KnockerConfig.save("ai_setup", "installing");
         runAsync(KnockerAISetup::install);
      }
   }

   public static void decline() {
      KnockerConfig.save("ai_setup", "declined");
   }

   public static String status() {
      if (busy) {
         return status;
      } else {
         String var10000;
         switch (KnockerConfig.aiSetup) {
            case "done" -> var10000 = "ИИ работает (" + KnockerConfig.model + ")";
            case "declined" -> var10000 = "ИИ выключен. Включить: /knockerai install";
            default -> var10000 = "ИИ не установлен. Установить: /knockerai install";
         }

         return var10000;
      }
   }

   private static void install() {
      busy = true;

      try {
         try {
            if (!ping()) {
               if (!windows()) {
                  broadcast("§c[AI Knocker] Автоустановка только для Windows. Поставь Ollama с ollama.com.");
                  return;
               }

               if (!Files.exists(portableExe(), new LinkOption[0])) {
                  downloadOllama();
               }

               ensureOllamaRunning();
               if (!ping()) {
                  broadcast("§c[AI Knocker] Не удалось запустить ИИ. Подробности в логе игры.");
                  return;
               }
            }

            if (!hasModel()) {
               pullModel();
            }

            useModel();
            broadcast("§5[AI Knocker] §fГотово. Кнокер теперь думает сам...");
         } catch (Exception e) {
            AiKnocker.LOGGER.error("Установка ИИ не удалась", e);
            broadcast("§c[AI Knocker] Установка ИИ прервалась: " + e.getMessage() + ". Попробуй ещё раз: /knockerai install");
         }

      } finally {
         busy = false;
         actionbar("");
      }
   }

   private static void downloadOllama() throws Exception {
      Path dir = root().resolve("ollama");
      Path zip = root().resolve("ollama.zip");
      Files.createDirectories(root());
      status = "Скачиваю Ollama...";
      broadcast("§5[AI Knocker] §7Скачиваю ИИ... можно играть, это идёт в фоне.");
      HttpResponse<InputStream> resp = HTTP.send(HttpRequest.newBuilder(URI.create("https://github.com/ollama/ollama/releases/latest/download/ollama-windows-amd64.zip")).GET().build(), BodyHandlers.ofInputStream());
      if (resp.statusCode() != 200) {
         throw new IllegalStateException("GitHub ответил " + resp.statusCode());
      } else {
         long total = resp.headers().firstValueAsLong("Content-Length").orElse(-1L);
         InputStream in = (InputStream)resp.body();

         try {
            OutputStream out = Files.newOutputStream(zip);

            try {
               byte[] buf = new byte[65536];
               long done = 0L;
               int lastPct = -1;

               int n;
               while((n = in.read(buf)) > 0) {
                  out.write(buf, 0, n);
                  done += (long)n;
                  if (total > 0L) {
                     int pct = (int)(done * 100L / total);
                     if (pct != lastPct) {
                        lastPct = pct;
                        status = "Скачиваю Ollama: " + pct + "%";
                        actionbar("§5Кнокер просыпается... §7ИИ " + pct + "% (1/2)");
                     }
                  }
               }
            } catch (Throwable var17) {
               if (out != null) {
                  try {
                     out.close();
                  } catch (Throwable var15) {
                     var17.addSuppressed(var15);
                  }
               }

               throw var17;
            }

            if (out != null) {
               out.close();
            }
         } catch (Throwable var18) {
            if (in != null) {
               try {
                  in.close();
               } catch (Throwable var14) {
                  var18.addSuppressed(var14);
               }
            }

            throw var18;
         }

         if (in != null) {
            in.close();
         }

         status = "Распаковываю...";
         actionbar("§5Кнокер просыпается... §7распаковка");
         ZipInputStream zin = new ZipInputStream(Files.newInputStream(zip));

         ZipEntry e;
         try {
            while((e = zin.getNextEntry()) != null) {
               Path target = dir.resolve(e.getName()).normalize();
               if (target.startsWith(dir)) {
                  if (e.isDirectory()) {
                     Files.createDirectories(target);
                  } else {
                     Files.createDirectories(target.getParent());
                     Files.copy(zin, target, new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
                  }
               }
            }
         } catch (Throwable var16) {
            try {
               zin.close();
            } catch (Throwable var13) {
               var16.addSuppressed(var13);
            }

            throw var16;
         }

         zin.close();
         Files.deleteIfExists(zip);
      }
   }

   private static void ensureOllamaRunning() {
      if (!ping() && Files.exists(portableExe(), new LinkOption[0])) {
         try {
            Path models = root().resolve("models");
            Files.createDirectories(models);
            ProcessBuilder pb = new ProcessBuilder(new String[]{portableExe().toString(), "serve"});
            pb.environment().put("OLLAMA_MODELS", models.toString());
            pb.environment().put("OLLAMA_HOST", "127.0.0.1:11434");
            pb.environment().put("OLLAMA_KEEP_ALIVE", "30m");
            pb.environment().put("OLLAMA_NUM_PARALLEL", "1");
            pb.environment().put("OLLAMA_MAX_LOADED_MODELS", "1");
            pb.environment().put("OLLAMA_VULKAN", "1");
            pb.redirectErrorStream(true);
            pb.redirectOutput(root().resolve("ollama.log").toFile());
            ollamaProcess = pb.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
               Process p = ollamaProcess;
               if (p != null) {
                  p.destroy();
               }

            }));

            for(int i = 0; i < 60 && !ping(); ++i) {
               Thread.sleep(500L);
            }
         } catch (Exception e) {
            AiKnocker.LOGGER.error("Не удалось запустить Ollama", e);
         }

      }
   }

   private static void pullModel() throws Exception {
      status = "Скачиваю модель...";
      JsonObject body = new JsonObject();
      body.addProperty("model", "gemma3:4b");
      body.addProperty("stream", true);
      HttpResponse<InputStream> resp = HTTP.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:11434/api/pull")).POST(BodyPublishers.ofString(body.toString())).build(), BodyHandlers.ofInputStream());
      int lastPct = -1;
      BufferedReader r = new BufferedReader(new InputStreamReader((InputStream)resp.body(), StandardCharsets.UTF_8));

      String line;
      try {
         while((line = r.readLine()) != null) {
            if (!line.isBlank()) {
               JsonObject o = JsonParser.parseString(line).getAsJsonObject();
               if (o.has("error")) {
                  throw new IllegalStateException(o.get("error").getAsString());
               }

               if (o.has("total") && o.has("completed")) {
                  long total = o.get("total").getAsLong();
                  long done = o.get("completed").getAsLong();
                  int pct = total > 0L ? (int)(done * 100L / total) : 0;
                  if (pct != lastPct) {
                     lastPct = pct;
                     status = "Скачиваю модель: " + pct + "%";
                     actionbar("§5Кнокер просыпается... §7мозг " + pct + "% (2/2)");
                  }
               }
            }
         }
      } catch (Throwable var12) {
         try {
            r.close();
         } catch (Throwable var11) {
            var12.addSuppressed(var11);
         }

         throw var12;
      }

      r.close();
      if (!hasModel()) {
         throw new IllegalStateException("модель не скачалась");
      }
   }

   private static void useModel() {
      KnockerConfig.save("api_key", "ollama");
      KnockerConfig.save("api_url", "http://127.0.0.1:11434/v1/chat/completions");
      if (!KnockerConfig.model.equals("knocker") || !hasModel("knocker")) {
         KnockerConfig.save("model", "gemma3:4b");
      }

      KnockerConfig.save("ai_setup", "done");
      KnockerConfig.load();
   }

   private static boolean ping() {
      try {
         HttpResponse<String> r = HTTP.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:11434/api/tags")).timeout(Duration.ofSeconds(3L)).GET().build(), BodyHandlers.ofString());
         return r.statusCode() == 200;
      } catch (Exception var1) {
         return false;
      }
   }

   private static boolean hasModel() {
      return hasModel("gemma3:4b");
   }

   private static boolean hasModel(String name) {
      try {
         HttpResponse<String> r = HTTP.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:11434/api/tags")).timeout(Duration.ofSeconds(5L)).GET().build(), BodyHandlers.ofString());

         for(JsonElement m : JsonParser.parseString((String)r.body()).getAsJsonObject().getAsJsonArray("models")) {
            String n = m.getAsJsonObject().get("name").getAsString();
            if (n.equals(name) || n.equals(name + ":latest")) {
               return true;
            }
         }
      } catch (Exception var6) {
      }

      return false;
   }

   private static void runAsync(Runnable r) {
      Thread t = new Thread(r, "AiKnocker-AISetup");
      t.setDaemon(true);
      t.start();
   }

   private static void broadcast(String msg) {
      MinecraftServer srv = server;
      if (srv != null) {
         srv.execute(() -> srv.method_3760().method_14571().forEach((p) -> p.method_7353(class_2561.method_43470(msg), false)));
      }
   }

   private static void actionbar(String msg) {
      MinecraftServer srv = server;
      if (srv != null) {
         srv.execute(() -> srv.method_3760().method_14571().forEach((p) -> p.method_7353(class_2561.method_43470(msg), true)));
      }
   }

   static {
      HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5L)).followRedirects(Redirect.ALWAYS).build();
      busy = false;
      status = "";
   }
}
