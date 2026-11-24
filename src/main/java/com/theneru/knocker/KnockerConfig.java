package com.theneru.knocker;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import net.fabricmc.loader.api.FabricLoader;

public final class KnockerConfig {
   public static String apiKey = "";
   public static String apiUrl = "http://127.0.0.1:11434/v1/chat/completions";
   public static String model = "gemma3:4b";
   public static String aiMode = "auto";
   public static String playerGender = "auto";
   public static String chatStyle = "human";
   public static String aiSetup = "ask";
   public static boolean debug = false;
   public static boolean atmosphere = true;
   public static String welcome = "";
   public static String aiDevice = "gpu";
   public static double randomPhraseMin = (double)3.0F;
   public static double randomPhraseMax = (double)6.0F;
   public static double thoughtsCooldown = (double)3.0F;
   public static double intrusiveMin = (double)8.0F;
   public static double intrusiveMax = (double)15.0F;
   public static double bedDelay = (double)2.0F;
   public static double foodPresent = (double)10.0F;
   public static double diamondMin = (double)30.0F;
   public static double diamondMax = (double)60.0F;
   public static double tripMin = (double)10.0F;
   public static double tripMax = (double)30.0F;
   public static double steal = (double)5.0F;
   public static double doorKnock = (double)5.0F;
   public static double paranoiaSilence = (double)2.0F;
   public static double endingRetry = (double)60.0F;
   public static double peekMin = (double)5.0F;
   public static double peekMax = (double)10.0F;
   public static double torch = (double)1.5F;
   public static double fakeMin = (double)10.0F;
   public static double fakeMax = (double)20.0F;
   public static double windowKnock = (double)8.0F;
   public static double questionMin = (double)4.0F;
   public static double questionMax = (double)8.0F;
   public static boolean voiceEnabled = true;
   public static int voiceRadius = 32;
   public static boolean voiceShowText = true;
   private static final String VOICE_TEMPLATE = "#\n# ===== ГОЛОС (нужен мод Simple Voice Chat) =====\n# распознавание речи: модель Vosk распакуй в папку config/aiknocker_vosk\nvoice_enabled=true\n# с какого расстояния он слышит тебя (блоков)\nvoice_radius=32\n# показывать в чате серым, что он расслышал\nvoice_show_text=true\n";
   private static final String FILE_NAME = "aiknocker.properties";
   private static final String TEMPLATE = "# AI Knocker — настройки\n# Мод от команды VesperLorn · новости и баги: https://t.me/vesperlorngame\n#\n# ===== ИИ =====\n# ИИ ставится САМ: при первом входе в мир мод спросит «Установить ИИ?» — жми кнопку.\n# Всё скачается в .minecraft/aiknocker (в систему ничего не ставится). Удалить — удалить эту папку.\n# Если Ollama у тебя уже стоит — мод найдёт её сам.\n# ai_setup: ask = спросить, done = готово, declined = без ИИ (вернуть: /knockerai install)\nai_setup=ask\n# ai_mode: offline = Кнокер отвечает только своими фразами, auto = ИИ, если он есть\nai_mode=auto\n# (для продвинутых: любой OpenAI-совместимый ИИ, например DeepSeek или OpenRouter)\napi_key=\napi_url=http://127.0.0.1:11434/v1/chat/completions\nmodel=gemma3:4b\n# ai_device: gpu = ИИ на видеокарте (отвечает быстро, но пока думает — игра может лагать)\n#            cpu = ИИ на процессоре (думает дольше, зато игра не лагает)\nai_device=gpu\n# chat_style: human = пишет как живой игрок, classic = [Кнокер] красным\nchat_style=human\n# player_gender: auto = Кнокер поймёт сам (спросит «ты парень или девушка?»), female, male\nplayer_gender=auto\n# true = показывать ошибки ИИ в чате\ndebug=false\n#\n# ===== АТМОСФЕРА =====\n# atmosphere: в дождь экран сереет, когда он злится или на паранойе — красная виньетка\natmosphere=true\n# auto_shader: если стоит Iris и в shaderpacks лежит Insanity — мод сам включит и настроит его\nauto_shader=true\n# shader_setup: done = уже настроено (поставь ask, чтобы настроить заново)\nshader_setup=ask\n#\n# ===== КУЛДАУНЫ (минуты реального времени, можно дробные: 0.5 = 30 сек) =====\n# как часто сам что-то говорит\nrandom_phrase_min=3\nrandom_phrase_max=6\n# «мысли» на экране: не чаще чем раз в N минут\nthoughts_cooldown=3\n# навязчивые мысли на одержимости/паранойе\nintrusive_min=8\nintrusive_max=15\n# через сколько минут на «Одержимости» он поставит кровать рядом с твоей\nbed_delay=2\n# подгон еды, когда ты голодная\nfood_present=10\n# алмаз в подарок\ndiamond_min=30\ndiamond_max=60\n# паранойя: «уходит» и возвращается с находкой\ntrip_min=10\ntrip_max=30\n# кражи из сундуков\nsteal=5\n# стук в одну и ту же дверь\ndoor_knock=5\n# паранойя: сколько можно молчать, пока он не появится перед лицом\nparanoia_silence=2\n# концовка: если ты сбежала из подвала, через сколько спросит снова\nending_retry=60\n# ранние фазы: выглядывает из-за деревьев во время игры\npeek_min=5\npeek_max=10\n# одержимость/паранойя ночью: гасит факел у дома\ntorch=1.5\n# паранойя: фейковые системные сообщения\nfake_min=10\nfake_max=20\n# ночью стучит в окно, когда ты дома\nwindow_knock=8\n# он расспрашивает тебя (имя, любимый цвет, страхи...)\nquestion_min=4\nquestion_max=8\n";
   private static final String COOLDOWNS_TEMPLATE = "#\n# ===== КУЛДАУНЫ (минуты реального времени, можно дробные: 0.5 = 30 сек) =====\n# как часто сам что-то говорит\nrandom_phrase_min=3\nrandom_phrase_max=6\n# «мысли» на экране: не чаще чем раз в N минут\nthoughts_cooldown=3\n# навязчивые мысли на одержимости/паранойе\nintrusive_min=8\nintrusive_max=15\n# через сколько минут на «Одержимости» он поставит кровать рядом с твоей\nbed_delay=2\n# подгон еды, когда ты голодная\nfood_present=10\n# алмаз в подарок\ndiamond_min=30\ndiamond_max=60\n# паранойя: «уходит» и возвращается с находкой\ntrip_min=10\ntrip_max=30\n# кражи из сундуков\nsteal=5\n# стук в одну и ту же дверь\ndoor_knock=5\n# паранойя: сколько можно молчать, пока он не появится перед лицом\nparanoia_silence=2\n# концовка: если ты сбежала из подвала, через сколько спросит снова\nending_retry=60\n# ранние фазы: выглядывает из-за деревьев во время игры\npeek_min=5\npeek_max=10\n# одержимость/паранойя ночью: гасит факел у дома\ntorch=1.5\n# паранойя: фейковые системные сообщения\nfake_min=10\nfake_max=20\n# ночью стучит в окно, когда ты дома\nwindow_knock=8\n# он расспрашивает тебя (имя, любимый цвет, страхи...)\nquestion_min=4\nquestion_max=8\n";

   public static int ticks(double minutes) {
      return (int)Math.max(1L, Math.round(minutes * (double)1200.0F));
   }

   public static int ticksBetween(double min, double max) {
      int a = ticks(min);
      int b = ticks(Math.max(min, max));
      return a + (b > a ? (new Random()).nextInt(b - a) : 0);
   }

   private KnockerConfig() {
   }

   public static synchronized void save(String key, String value) {
      Path path = FabricLoader.getInstance().getConfigDir().resolve("aiknocker.properties");

      try {
         List<String> lines = Files.exists(path, new LinkOption[0]) ? new ArrayList(Files.readAllLines(path, StandardCharsets.UTF_8)) : new ArrayList();
         boolean found = false;

         for(int i = 0; i < lines.size(); ++i) {
            String l = ((String)lines.get(i)).trim();
            if (l.startsWith(key + "=") || l.startsWith("# " + key + "=") || l.startsWith("#" + key + "=")) {
               if (!found) {
                  lines.set(i, key + "=" + value);
               } else {
                  lines.set(i, "# " + l);
               }

               found = true;
            }
         }

         if (!found) {
            lines.add(key + "=" + value);
         }

         Files.createDirectories(path.getParent());
         Files.write(path, lines, StandardCharsets.UTF_8);
      } catch (IOException e) {
         AiKnocker.LOGGER.error("Не удалось сохранить {} в {}", new Object[]{key, path, e});
      }

      switch (key) {
         case "api_key" -> apiKey = value;
         case "api_url" -> apiUrl = value;
         case "model" -> model = value;
         case "ai_setup" -> aiSetup = value;
      }

   }

   private static double num(Properties p, String key, double def) {
      try {
         return Double.parseDouble(p.getProperty(key, String.valueOf(def)).trim().replace(',', '.'));
      } catch (NumberFormatException var5) {
         return def;
      }
   }

   public static boolean useExternalAI() {
      boolean var10000;
      switch (aiMode) {
         case "offline" -> var10000 = false;
         case "api" -> var10000 = true;
         default -> var10000 = !apiKey.isBlank();
      }

      return var10000;
   }

   public static void load() {
      Path path = FabricLoader.getInstance().getConfigDir().resolve("aiknocker.properties");

      try {
         if (Files.notExists(path, new LinkOption[0])) {
            Files.createDirectories(path.getParent());
            Files.writeString(path, "# AI Knocker — настройки\n# Мод от команды VesperLorn · новости и баги: https://t.me/vesperlorngame\n#\n# ===== ИИ =====\n# ИИ ставится САМ: при первом входе в мир мод спросит «Установить ИИ?» — жми кнопку.\n# Всё скачается в .minecraft/aiknocker (в систему ничего не ставится). Удалить — удалить эту папку.\n# Если Ollama у тебя уже стоит — мод найдёт её сам.\n# ai_setup: ask = спросить, done = готово, declined = без ИИ (вернуть: /knockerai install)\nai_setup=ask\n# ai_mode: offline = Кнокер отвечает только своими фразами, auto = ИИ, если он есть\nai_mode=auto\n# (для продвинутых: любой OpenAI-совместимый ИИ, например DeepSeek или OpenRouter)\napi_key=\napi_url=http://127.0.0.1:11434/v1/chat/completions\nmodel=gemma3:4b\n# ai_device: gpu = ИИ на видеокарте (отвечает быстро, но пока думает — игра может лагать)\n#            cpu = ИИ на процессоре (думает дольше, зато игра не лагает)\nai_device=gpu\n# chat_style: human = пишет как живой игрок, classic = [Кнокер] красным\nchat_style=human\n# player_gender: auto = Кнокер поймёт сам (спросит «ты парень или девушка?»), female, male\nplayer_gender=auto\n# true = показывать ошибки ИИ в чате\ndebug=false\n#\n# ===== АТМОСФЕРА =====\n# atmosphere: в дождь экран сереет, когда он злится или на паранойе — красная виньетка\natmosphere=true\n# auto_shader: если стоит Iris и в shaderpacks лежит Insanity — мод сам включит и настроит его\nauto_shader=true\n# shader_setup: done = уже настроено (поставь ask, чтобы настроить заново)\nshader_setup=ask\n#\n# ===== КУЛДАУНЫ (минуты реального времени, можно дробные: 0.5 = 30 сек) =====\n# как часто сам что-то говорит\nrandom_phrase_min=3\nrandom_phrase_max=6\n# «мысли» на экране: не чаще чем раз в N минут\nthoughts_cooldown=3\n# навязчивые мысли на одержимости/паранойе\nintrusive_min=8\nintrusive_max=15\n# через сколько минут на «Одержимости» он поставит кровать рядом с твоей\nbed_delay=2\n# подгон еды, когда ты голодная\nfood_present=10\n# алмаз в подарок\ndiamond_min=30\ndiamond_max=60\n# паранойя: «уходит» и возвращается с находкой\ntrip_min=10\ntrip_max=30\n# кражи из сундуков\nsteal=5\n# стук в одну и ту же дверь\ndoor_knock=5\n# паранойя: сколько можно молчать, пока он не появится перед лицом\nparanoia_silence=2\n# концовка: если ты сбежала из подвала, через сколько спросит снова\nending_retry=60\n# ранние фазы: выглядывает из-за деревьев во время игры\npeek_min=5\npeek_max=10\n# одержимость/паранойя ночью: гасит факел у дома\ntorch=1.5\n# паранойя: фейковые системные сообщения\nfake_min=10\nfake_max=20\n# ночью стучит в окно, когда ты дома\nwindow_knock=8\n# он расспрашивает тебя (имя, любимый цвет, страхи...)\nquestion_min=4\nquestion_max=8\n", StandardCharsets.UTF_8);
            AiKnocker.LOGGER.info("Создан {} — впиши туда api_key", path);
         }

         Properties p = new Properties();
         Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);

         try {
            p.load(reader);
         } catch (Throwable var6) {
            if (reader != null) {
               try {
                  reader.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (reader != null) {
            reader.close();
         }

         apiKey = p.getProperty("api_key", "").trim();
         apiUrl = p.getProperty("api_url", apiUrl).trim();
         model = p.getProperty("model", model).trim();
         debug = Boolean.parseBoolean(p.getProperty("debug", "false").trim());
         welcome = p.getProperty("welcome", "").trim();
         aiDevice = p.getProperty("ai_device", "gpu").trim().toLowerCase();
         atmosphere = !"false".equalsIgnoreCase(p.getProperty("atmosphere", "true").trim());
         aiMode = p.getProperty("ai_mode", "auto").trim().toLowerCase();
         chatStyle = p.getProperty("chat_style", "human").trim().toLowerCase();
         aiSetup = p.getProperty("ai_setup", "ask").trim().toLowerCase();
         playerGender = p.getProperty("player_gender", "auto").trim().toLowerCase();
         randomPhraseMin = num(p, "random_phrase_min", (double)3.0F);
         randomPhraseMax = num(p, "random_phrase_max", (double)6.0F);
         thoughtsCooldown = num(p, "thoughts_cooldown", (double)3.0F);
         intrusiveMin = num(p, "intrusive_min", (double)8.0F);
         intrusiveMax = num(p, "intrusive_max", (double)15.0F);
         bedDelay = num(p, "bed_delay", (double)2.0F);
         foodPresent = num(p, "food_present", (double)10.0F);
         diamondMin = num(p, "diamond_min", (double)30.0F);
         diamondMax = num(p, "diamond_max", (double)60.0F);
         tripMin = num(p, "trip_min", (double)10.0F);
         tripMax = num(p, "trip_max", (double)30.0F);
         steal = num(p, "steal", (double)5.0F);
         doorKnock = num(p, "door_knock", (double)5.0F);
         paranoiaSilence = num(p, "paranoia_silence", (double)2.0F);
         endingRetry = num(p, "ending_retry", (double)60.0F);
         peekMin = num(p, "peek_min", (double)5.0F);
         peekMax = num(p, "peek_max", (double)10.0F);
         torch = num(p, "torch", (double)1.5F);
         fakeMin = num(p, "fake_min", (double)10.0F);
         fakeMax = num(p, "fake_max", (double)20.0F);
         windowKnock = num(p, "window_knock", (double)8.0F);
         questionMin = num(p, "question_min", (double)4.0F);
         questionMax = num(p, "question_max", (double)8.0F);
         voiceEnabled = Boolean.parseBoolean(p.getProperty("voice_enabled", "true").trim());
         voiceRadius = (int)num(p, "voice_radius", (double)32.0F);
         voiceShowText = Boolean.parseBoolean(p.getProperty("voice_show_text", "true").trim());
         if (!p.containsKey("random_phrase_min")) {
            Files.writeString(path, "#\n# ===== КУЛДАУНЫ (минуты реального времени, можно дробные: 0.5 = 30 сек) =====\n# как часто сам что-то говорит\nrandom_phrase_min=3\nrandom_phrase_max=6\n# «мысли» на экране: не чаще чем раз в N минут\nthoughts_cooldown=3\n# навязчивые мысли на одержимости/паранойе\nintrusive_min=8\nintrusive_max=15\n# через сколько минут на «Одержимости» он поставит кровать рядом с твоей\nbed_delay=2\n# подгон еды, когда ты голодная\nfood_present=10\n# алмаз в подарок\ndiamond_min=30\ndiamond_max=60\n# паранойя: «уходит» и возвращается с находкой\ntrip_min=10\ntrip_max=30\n# кражи из сундуков\nsteal=5\n# стук в одну и ту же дверь\ndoor_knock=5\n# паранойя: сколько можно молчать, пока он не появится перед лицом\nparanoia_silence=2\n# концовка: если ты сбежала из подвала, через сколько спросит снова\nending_retry=60\n# ранние фазы: выглядывает из-за деревьев во время игры\npeek_min=5\npeek_max=10\n# одержимость/паранойя ночью: гасит факел у дома\ntorch=1.5\n# паранойя: фейковые системные сообщения\nfake_min=10\nfake_max=20\n# ночью стучит в окно, когда ты дома\nwindow_knock=8\n# он расспрашивает тебя (имя, любимый цвет, страхи...)\nquestion_min=4\nquestion_max=8\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
         } else if (!p.containsKey("question_min")) {
            Files.writeString(path, "\nquestion_min=4\nquestion_max=8\nplayer_gender=auto\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
         }

         if (!p.containsKey("peek_min") && p.containsKey("random_phrase_min")) {
            String extra = "#\n# ===== КУЛДАУНЫ (минуты реального времени, можно дробные: 0.5 = 30 сек) =====\n# как часто сам что-то говорит\nrandom_phrase_min=3\nrandom_phrase_max=6\n# «мысли» на экране: не чаще чем раз в N минут\nthoughts_cooldown=3\n# навязчивые мысли на одержимости/паранойе\nintrusive_min=8\nintrusive_max=15\n# через сколько минут на «Одержимости» он поставит кровать рядом с твоей\nbed_delay=2\n# подгон еды, когда ты голодная\nfood_present=10\n# алмаз в подарок\ndiamond_min=30\ndiamond_max=60\n# паранойя: «уходит» и возвращается с находкой\ntrip_min=10\ntrip_max=30\n# кражи из сундуков\nsteal=5\n# стук в одну и ту же дверь\ndoor_knock=5\n# паранойя: сколько можно молчать, пока он не появится перед лицом\nparanoia_silence=2\n# концовка: если ты сбежала из подвала, через сколько спросит снова\nending_retry=60\n# ранние фазы: выглядывает из-за деревьев во время игры\npeek_min=5\npeek_max=10\n# одержимость/паранойя ночью: гасит факел у дома\ntorch=1.5\n# паранойя: фейковые системные сообщения\nfake_min=10\nfake_max=20\n# ночью стучит в окно, когда ты дома\nwindow_knock=8\n# он расспрашивает тебя (имя, любимый цвет, страхи...)\nquestion_min=4\nquestion_max=8\n".substring("#\n# ===== КУЛДАУНЫ (минуты реального времени, можно дробные: 0.5 = 30 сек) =====\n# как часто сам что-то говорит\nrandom_phrase_min=3\nrandom_phrase_max=6\n# «мысли» на экране: не чаще чем раз в N минут\nthoughts_cooldown=3\n# навязчивые мысли на одержимости/паранойе\nintrusive_min=8\nintrusive_max=15\n# через сколько минут на «Одержимости» он поставит кровать рядом с твоей\nbed_delay=2\n# подгон еды, когда ты голодная\nfood_present=10\n# алмаз в подарок\ndiamond_min=30\ndiamond_max=60\n# паранойя: «уходит» и возвращается с находкой\ntrip_min=10\ntrip_max=30\n# кражи из сундуков\nsteal=5\n# стук в одну и ту же дверь\ndoor_knock=5\n# паранойя: сколько можно молчать, пока он не появится перед лицом\nparanoia_silence=2\n# концовка: если ты сбежала из подвала, через сколько спросит снова\nending_retry=60\n# ранние фазы: выглядывает из-за деревьев во время игры\npeek_min=5\npeek_max=10\n# одержимость/паранойя ночью: гасит факел у дома\ntorch=1.5\n# паранойя: фейковые системные сообщения\nfake_min=10\nfake_max=20\n# ночью стучит в окно, когда ты дома\nwindow_knock=8\n# он расспрашивает тебя (имя, любимый цвет, страхи...)\nquestion_min=4\nquestion_max=8\n".indexOf("# ранние фазы: выглядывает"));
            Files.writeString(path, extra, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
         }

         if (!p.containsKey("ai_device")) {
            Files.writeString(path, "\n# ai_device: gpu = ИИ на видеокарте (быстро, но пока думает — игра может лагать), cpu = на процессоре (дольше, без лагов)\nai_device=gpu\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
         }

         if (!p.containsKey("atmosphere")) {
            String add = "\n# ===== АТМОСФЕРА =====\n# atmosphere: в дождь экран сереет, когда он злится или на паранойе — красная виньетка\natmosphere=true\n# auto_shader: если стоит Iris и в shaderpacks лежит Insanity — мод сам включит и настроит его\nauto_shader=true\n" + (p.containsKey("shader_setup") ? "" : "# shader_setup: done = уже настроено (ask — настроить заново)\nshader_setup=ask\n");
            Files.writeString(path, add, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
         }
      } catch (IOException e) {
         AiKnocker.LOGGER.error("Не удалось прочитать {}", path, e);
      }

   }
}
