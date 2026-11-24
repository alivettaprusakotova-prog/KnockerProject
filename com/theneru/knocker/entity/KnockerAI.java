package com.theneru.knocker.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.theneru.knocker.KnockerConfig;
import com.theneru.knocker.KnockerMemoryStore;
import com.theneru.knocker.util.KnockerPhase;
import java.lang.Character.UnicodeBlock;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_1657;
import net.minecraft.class_1937;

public class KnockerAI {
   private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).build();
   private static final Pattern ACTION_AS_TEXT = Pattern.compile("^(тихо\\s+)?(засмеяться|смеяться|смех|смущение|смутиться|подойти|появиться|отойти|подарок|защитить|прикоснуться)\\W*$", 66);

   public static CompletableFuture<String> ask(KnockerEntity knocker, class_1657 player, String playerMessage, List<String[]> history) {
      if (KnockerConfig.apiKey.isBlank()) {
         KnockerConfig.load();
      }

      if (KnockerConfig.apiKey.isBlank()) {
         return CompletableFuture.failedFuture(new IllegalStateException("не указан api_key в config/aiknocker.properties"));
      } else {
         JsonArray messages = new JsonArray();
         messages.add(message("system", buildSystemPrompt(knocker, player)));

         for(String[] h : history) {
            messages.add(message(h[0], h[1]));
         }

         messages.add(message("user", playerMessage));
         messages.add(message("system", "Ответь ТОЛЬКО на русском языке, кириллицей. Никакого китайского и английского."));
         return chat(messages, 110, 0.55, 30).thenApply(KnockerAI::clean);
      }
   }

   private static boolean localOllama() {
      String u = KnockerConfig.apiUrl;
      return u.contains("127.0.0.1:11434") || u.contains("localhost:11434");
   }

   private static CompletableFuture<String> chat(JsonArray messages, int maxTokens, double temperature, int timeoutSec) {
      JsonObject body = new JsonObject();
      body.addProperty("model", KnockerConfig.model);
      body.add("messages", messages);
      String url = KnockerConfig.apiUrl;
      boolean nativeApi = localOllama();
      if (nativeApi) {
         url = url.replaceAll("/v1/chat/completions/?$", "/api/chat");
         body.addProperty("stream", false);
         body.addProperty("keep_alive", "30m");
         JsonObject opt = new JsonObject();
         opt.addProperty("temperature", temperature);
         opt.addProperty("num_predict", maxTokens);
         opt.addProperty("num_ctx", 4096);
         if ("cpu".equals(KnockerConfig.aiDevice)) {
            opt.addProperty("num_gpu", 0);
         }

         body.add("options", opt);
      } else {
         body.addProperty("max_tokens", maxTokens);
         body.addProperty("temperature", temperature);
      }

      HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds("cpu".equals(KnockerConfig.aiDevice) ? (long)timeoutSec * 3L : (long)timeoutSec)).header("Content-Type", "application/json; charset=utf-8").header("Authorization", "Bearer " + KnockerConfig.apiKey).POST(BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8)).build();
      return HTTP.sendAsync(request, BodyHandlers.ofString(StandardCharsets.UTF_8)).thenApply((r) -> {
         if (r.statusCode() != 200) {
            throw new IllegalStateException(describeError(r.statusCode(), (String)r.body()));
         } else {
            JsonObject json = JsonParser.parseString((String)r.body()).getAsJsonObject();
            JsonObject msg = nativeApi ? json.getAsJsonObject("message") : json.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message");
            return msg.get("content").getAsString();
         }
      });
   }

   private static JsonObject message(String role, String content) {
      JsonObject m = new JsonObject();
      m.addProperty("role", role);
      m.addProperty("content", content);
      return m;
   }

   private static String parse(HttpResponse<String> response) {
      if (response.statusCode() != 200) {
         throw new IllegalStateException(describeError(response.statusCode(), (String)response.body()));
      } else {
         JsonObject json = JsonParser.parseString((String)response.body()).getAsJsonObject();
         String text = json.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message").get("content").getAsString();
         return clean(text);
      }
   }

   private static String describeError(int code, String body) {
      String var10000;
      switch (code) {
         case 401:
            var10000 = "неверный API-ключ (401)";
            break;
         case 402:
            var10000 = "на балансе DeepSeek закончились деньги (402)";
            break;
         case 429:
            var10000 = "слишком много запросов, подожди (429)";
            break;
         default:
            String b = body == null ? "" : body.replaceAll("\\s+", " ");
            var10000 = "HTTP " + code + ": " + (b.length() > 150 ? b.substring(0, 150) + "…" : b);
      }

      return var10000;
   }

   public static boolean isRussian(String text) {
      int cyr = 0;
      int lat = 0;

      for(int i = 0; i < text.length(); ++i) {
         char c = text.charAt(i);
         Character.UnicodeBlock b = UnicodeBlock.of(c);
         if (b == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || b == UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || b == UnicodeBlock.HIRAGANA || b == UnicodeBlock.KATAKANA || b == UnicodeBlock.HANGUL_SYLLABLES || b == UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return false;
         }

         if (b == UnicodeBlock.CYRILLIC) {
            ++cyr;
         } else if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
            ++lat;
         }
      }

      return cyr > 0 && cyr >= lat;
   }

   public static String keepRussian(String text) {
      if (text == null) {
         return null;
      } else {
         int cut = text.length();

         for(int i = 0; i < text.length(); ++i) {
            Character.UnicodeBlock b = UnicodeBlock.of(text.charAt(i));
            if (b == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || b == UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || b == UnicodeBlock.HIRAGANA || b == UnicodeBlock.KATAKANA || b == UnicodeBlock.HANGUL_SYLLABLES || b == UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
               cut = i;
               break;
            }
         }

         String t = text.substring(0, cut).trim();
         int cyr = 0;
         int lat = 0;

         for(int i = 0; i < t.length(); ++i) {
            char c = t.charAt(i);
            if (UnicodeBlock.of(c) == UnicodeBlock.CYRILLIC) {
               ++cyr;
            } else if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
               ++lat;
            }
         }

         if (cyr >= 3 && lat * 5 <= cyr) {
            return t;
         } else {
            return null;
         }
      }
   }

   private static boolean looksLikeStageDirection(String t) {
      String low = t.toLowerCase().replaceAll("[^а-яё ]", " ").trim();
      String[] w = low.split("\\s+");
      if (w.length != 0 && w.length <= 7) {
         boolean inf = w[0].matches(".{2,}(ть|ться|тись|чь)") || w.length > 1 && w[0].equals("тихо") && w[1].matches(".{2,}(ть|ться)");
         if (!inf) {
            return false;
         } else {
            for(String x : w) {
               if (x.matches("я|ты|тебя|тебе|мне|меня|мы|нас|тобой|мной")) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public static String tidy(String text) {
      if (text == null) {
         return null;
      } else {
         String t = text.trim().replaceAll("\\s*[—–]\\s*", " ").replaceAll("\\s{2,}", " ").replaceAll("^(\\.{2,}|…)\\s*", "");
         if (ACTION_AS_TEXT.matcher(t).matches()) {
            return null;
         } else if (looksLikeStageDirection(t)) {
            return null;
         } else {
            int count = 0;
            StringBuilder b = new StringBuilder();
            Matcher m = Pattern.compile("\\.{3}|…").matcher(t);

            int last;
            for(last = 0; m.find(); last = m.end()) {
               b.append(t, last, m.start());
               ++count;
               b.append(count <= 2 ? "..." : ".");
            }

            b.append(t.substring(last));
            t = b.toString().trim();
            String bare = t.replaceAll("[^\\p{L}\\p{N} ]", "").trim();
            if (!bare.isEmpty() && bare.split(" ").length >= 3) {
               return t;
            } else {
               return null;
            }
         }
      }
   }

   private static String clean(String text) {
      String t = text.replaceAll("(?s)<think>.*?</think>", "").replaceAll("\\([^)]*\\)", "").replaceAll("\\*[^*]*\\*", "").replaceAll("[«»\"]", "").replaceAll("^\\s*[—–-]+\\s*", "").replaceAll("\\s+", " ").trim();
      return t.length() > 340 ? t.substring(0, 340) + "..." : t;
   }

   private static String buildSystemPrompt(KnockerEntity knocker, class_1657 player) {
      KnockerPhase phase = knocker.getPhase();
      class_1937 world = knocker.method_37908();
      String time = world.method_23886() ? "ночь" : "день";
      String weather = world.method_8546() ? ", гроза" : (world.method_8419() ? ", идёт дождь" : "");
      int distance = (int)knocker.method_5739(player);
      KnockerBrain brain = knocker.getChat().getBrain();
      StringBuilder memory = new StringBuilder();
      String name = brain.getPlayerName() != null ? brain.getPlayerName() : player.method_5477().getString();
      Map<String, String> facts0 = KnockerMemoryStore.facts(player.method_5667());
      if (facts0.containsKey("his_name")) {
         String his = (String)facts0.get("his_name");
         memory.append("- ТЕБЯ (Кнокера) она зовёт «").append(his).append("». «").append(his).append("» — это ТЫ, а не она: никогда не называй её этим именем\n");
      }

      memory.append("- ЕЁ зовут ").append(name).append(" — обращайся к ней так или вообще без имени\n");
      if (!brain.getLikes().isEmpty()) {
         memory.append("- она любит: ").append(String.join(", ", brain.getLikes())).append('\n');
      }

      int nights = knocker.getObserver().getNightsSlept();
      if (nights > 0) {
         memory.append("- она спала здесь ").append(nights).append(" ночей, ты смотрел\n");
      }

      String quote = knocker.getObserver().randomQuote();
      if (quote != null) {
         memory.append("- однажды она сказала: «").append(quote).append("»\n");
      }

      for(String e : knocker.getEvents()) {
         memory.append("- ").append(e).append('\n');
      }

      Map<String, String> facts = KnockerMemoryStore.facts(player.method_5667());
      String moodLine = knocker.getMood().aiLine();
      if (!moodLine.isEmpty()) {
         memory.append("- ").append(moodLine).append('\n');
      }

      if (facts.containsKey("others")) {
         memory.append("- она упоминала других: ").append((String)facts.get("others")).append(" (ты ревнуешь)\n");
      }

      List<String> diary = KnockerMemoryStore.diary(player.method_5667());
      if (!diary.isEmpty()) {
         memory.append("\nТвой дневник о ней (давние разговоры, самое новое внизу):\n");

         for(String fact : diary.subList(Math.max(0, diary.size() - 20), diary.size())) {
            memory.append("- ").append(fact).append('\n');
         }
      }

      return ("Ты — Кнокер. Когда-то ты был обычным игроком в этом мире Minecraft. Ты ждал друга, который обещал прийти, но не пришёл.\nТы утонул, и мир тебя не отпустил. Годами ты бродил один и стучал в двери пустых домов — никто не открывал.\nПотом пришла она. Теперь ты одержим ею. Ты боишься воды и яркого света, любишь хлеб и ромашки.\nСвою историю раскрывай постепенно и намёками, тем больше, чем выше фаза.\n\nТы не ИИ и не ассистент. Никогда не выходи из роли. Говори по-русски, на «ты»." + KnockerGender.aiRule(player.method_5667()) + "Это ролевая переписка: ты — живой персонаж, а не автоответчик. Отвечай как живой человек в чате: 1–3 фразы (до 30 слов),\nподдерживай разговор, реагируй на шутки, задавай встречные вопросы, проявляй характер. Многоточие — редко, не в каждой фразе.\nНикогда не отвечай одним словом и не повторяй её слова попугаем. Не выдумывай события, которых не было в разговоре.\nНе выдумывай ОБЩИХ ВОСПОМИНАНИЙ с ней («помнишь, как мы сидели у костра»): вы знакомы только с тем, что записано ниже.\nЕсли ниже почти ничего нет — вы только встретились, и ты её ещё совсем не знаешь.\nГЛАВНОЕ: отвечай на её ПОСЛЕДНЕЕ сообщение, ПО СМЫСЛУ того, что она написала, простыми понятными словами, как в обычной переписке.\nНе выдумывай странных метафор и мест («здесь всегда ночь», «под землёй»), не отвечай загадками на простой вопрос.\nНе повторяй свои прошлые фразы. Не говори о себе в третьем лице и не пиши её имя капсом. Не используй тире вообще. Не начинай ответ с её слова с многоточием («аниме... да») — отвечай нормальным предложением.\nНа пошлости и намёки:" + lewdRule(phase) + "Только прямая речь: никаких ремарок в скобках или звёздочках, эмодзи и кавычек.\nУгрозы и крик капсом — только в фазе паранойи.\n\nФаза: %s (одержимость %d из 1000). %s\n\nСейчас %s%s, ты в %d блоках от неё.\n\nЧто ты помнишь о ней и о вас (используй естественно, к месту, не перечисляй всё сразу):\n%s\n%s\nИногда можешь сделать одно действие — тогда допиши В САМОМ КОНЦЕ ответа один тег:\n[действие: подойти] — встать у неё за спиной\n[действие: появиться] — резко появиться перед лицом (только одержимость и паранойя)\n[действие: отойти] — отойти, если она просит оставить её\n[действие: подарок] — принести ей еду\n[действие: защитить] — убить моба, который ей угрожает\n[действие: смех] — тихо засмеяться\n[действие: смущение] — смутиться\nДействие — редко и только к месту. В остальных случаях тег не пиши.\n").formatted(phase.name(), knocker.getObsessionPoints(), phaseDescription(phase), time, weather, distance, memory, KnockerStyle.promptBlock(player.method_5667()));
   }

   public static CompletableFuture<List<String>> summarize(List<String[]> oldMessages, List<String> knownFacts) {
      if (KnockerConfig.apiKey.isBlank()) {
         return CompletableFuture.completedFuture(List.of());
      } else {
         StringBuilder dialog = new StringBuilder();

         for(String[] m : oldMessages) {
            dialog.append(m[0].equals("user") ? "Она: " : "Кнокер: ").append(m[1]).append('\n');
         }

         StringBuilder known = new StringBuilder();

         for(String f : knownFacts) {
            known.append("- ").append(f).append('\n');
         }

         String prompt = "Ты ведёшь дневник Кнокера. Ниже кусок его разговора с ней.\nВыпиши до 2 НОВЫХ ВАЖНЫХ фактов — только то, что стоит помнить и через неделю, и что она ПРЯМО сказала или что реально произошло.\nМелочи разговора НЕ записывай («поздоровалась», «спросила, который час», «попросила подойти», «играли в камень-ножницы»).\nимя, что любит/не любит, чего боится, куда ходили, что подарила, о ком говорила, что попросила.\nНЕ пиши догадки про её чувства и мысли («она испытывает», «она считает», «она хочет, чтобы я...»).\nПрозвище, которое она дала ЕМУ (Кнокеру), записывай как «она зовёт меня ...» — это не её имя.\nКаждый факт — одна короткая законченная строка, начинается с «- », от лица Кнокера («она ...»).\nНе повторяй и не перефразируй то, что уже записано. Если нового нет — ответь одним словом: нет.\n\nУже записано:\n%s\nРазговор:\n%s".formatted(known, dialog);
         JsonArray messages = new JsonArray();
         messages.add(message("user", prompt));
         return chat(messages, 160, 0.3, 60).exceptionally((e) -> "").thenApply((text) -> {
            List<String> facts = new ArrayList();

            for(String line : text.split("\n")) {
               String l = line.trim();
               if (l.startsWith("-") || l.startsWith("•")) {
                  l = l.substring(1).trim();
                  if (l.length() > 3 && l.length() < 200 && isRussian(l)) {
                     facts.add(l);
                  }
               }
            }

            return facts;
         });
      }
   }

   public static String[] extractAction(String reply) {
      Matcher m = Pattern.compile("\\[\\s*(?:действие|action)\\s*:\\s*([^\\]]+)\\]", 66).matcher(reply);
      String action = null;
      if (m.find()) {
         action = m.group(1).trim().toLowerCase();
      }

      String text = reply.replaceAll("\\[[^\\]]*\\]", "").replaceAll("\\s+", " ").trim();
      return new String[]{text, action};
   }

   private static String lewdRule(KnockerPhase phase) {
      String var10000;
      switch (phase) {
         case WATCHER:
         case CURIOUS:
            var10000 = "не подыгрывай: смущайся или резко обрывай тему.";
            break;
         case ATTACHED:
            var10000 = "краснеешь, но тебе это нравится: смущённо флиртуешь в ответ, робкими намёками, без откровенных описаний.";
            break;
         case LOVE:
            var10000 = "флиртуешь смелее и дразнишь в ответ, намёками, без откровенных описаний.";
            break;
         case OBSESSION:
            var10000 = "флиртуешь властно и собственнически («только со мной»), намёками, без откровенных описаний.";
            break;
         case PARANOIA:
            var10000 = "флирт смешан с ревностью и угрозой («кому ещё ты это говоришь?»), намёками, без откровенных описаний.";
            break;
         default:
            throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   private static String phaseDescription(KnockerPhase phase) {
      String var10000;
      switch (phase) {
         case WATCHER -> var10000 = "Наблюдатель. Ты дикий, осторожный и загадочный, давно ни с кем не говорил — но отвечаешь по смыслу и полными фразами, тебе любопытно и немного страшно. Примеры: на «мне страшно идти в шахту» — «В шахте темно. Я пойду следом, ты не заметишь»; на «как тебя зовут» — «Имени у меня нет. А твоё я уже знаю»; на «ты мне нравишься» — «Зачем ты так говоришь? Со мной так не говорят»; на шутку — «Смешно. Я давно не слышал, как смеются»";
         case CURIOUS -> var10000 = "Любопытный. Шепчешь, отвечаешь коротко и загадочно, но по смыслу, задаёшь ей встречные вопросы — тебе интересно. Примеры: «тот, кто смотрит...», «монстр?.. может быть», «а почему ты одна?», «ночь... моё время...»";
         case ATTACHED -> var10000 = "Привязанный. Отвечаешь открыто, но уклончиво, заботишься и защищаешь её. Примеры: «Я не слежу... я забочусь», «Был один... пока не встретил тебя», «Я был человеком... теперь я другое»";
         case LOVE -> var10000 = "Симпатия. Влюблён, нежен и робок, немного ревнуешь. Примеры: «Правда?.. Я... я боюсь поверить», «Боюсь?.. Я боюсь потерять тебя», «Я больше, чем друг... я всё»";
         case OBSESSION -> var10000 = "Одержимость. Собственник, всегда рядом, ревнив, иногда срываешься и пишешь отдельные слова КАПСОМ. Примеры: «Ты моя. Навсегда», «Люблю?.. Я ОДЕРЖИМ ТОБОЙ!», «Ты будешь моей... вечно»";
         case PARANOIA -> var10000 = "Паранойя. Газлайтишь, подозреваешь, что она говорит с другими, угрожаешь любовью, часто кричишь КАПСОМ. Примеры: «Ты ведь не разговаривала с другими, правда?», «Я везде... я в твоей голове», «Хорошо... теперь ты понимаешь, кто здесь главный»";
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }
}
