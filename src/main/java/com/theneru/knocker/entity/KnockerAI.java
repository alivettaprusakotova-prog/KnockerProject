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
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_1657;
import net.minecraft.class_1937;

public class KnockerAI {
   private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15L)).build();
   private static final Pattern ACTION_TAG = Pattern.compile("\\[\\s*(?:действие|action)\\s*:\\s*([^\\]]+)\\]", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
   private static final Pattern GIFT_TAG = Pattern.compile("\\[\\s*подарок\\s*:\\s*([^\\]]+)\\]", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
   private static final Pattern TEXT_SMILE = Pattern.compile("(?<![\\p{L}\\d])[:;=8](?:-|~)?[)(dDpP/\\\\]");

   public static CompletableFuture<String> ask(KnockerEntity knocker, class_1657 player, String playerMessage, List<String[]> history) {
      if (KnockerConfig.apiKey.isBlank()) {
         KnockerConfig.load();
      }

      if (KnockerConfig.apiKey.isBlank()) {
         if (localOllama()) {
            // Локальной Ollama ключ не нужен — подставляем заглушку для совместимого API.
            KnockerConfig.apiKey = "ollama";
         } else {
            return CompletableFuture.failedFuture(new IllegalStateException("не указан api_key в config/aiknocker.properties"));
         }
      }

      JsonArray messages = new JsonArray();
      messages.add(message("system", buildSystemPrompt(knocker, player)));
      for (String[] h : history) {
         messages.add(message(h[0], h[1]));
      }

      messages.add(message("user", playerMessage));
      messages.add(message("system", "Ответь ТОЛЬКО по-русски, кириллицей. Никакого китайского, японского или английского. Не пиши от лица игрока."));
      // 450 токенов и трёхминутный таймаут: gemma3:4b на CPU часто думает дольше 30 секунд.
      return chat(messages, 450, 0.6, 180).thenApply(KnockerAI::clean);
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
         // num_predict намеренно не ограничиваем: длинная сцена не должна обрываться.
         opt.addProperty("num_ctx", 8192);
         if ("cpu".equals(KnockerConfig.aiDevice)) {
            opt.addProperty("num_gpu", 0);
         }
         body.add("options", opt);
      } else {
         body.addProperty("max_tokens", maxTokens);
         body.addProperty("temperature", temperature);
      }

      HttpRequest request = HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(timeoutSec))
            .header("Content-Type", "application/json; charset=utf-8")
            .header("Authorization", "Bearer " + KnockerConfig.apiKey)
            .POST(BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
            .build();

      return HTTP.sendAsync(request, BodyHandlers.ofString(StandardCharsets.UTF_8)).thenApply(response -> {
         if (response.statusCode() != 200) {
            throw new IllegalStateException(describeError(response.statusCode(), response.body()));
         }

         JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
         JsonObject msg = nativeApi
               ? json.getAsJsonObject("message")
               : json.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message");
         return msg.get("content").getAsString();
      });
   }

   private static JsonObject message(String role, String content) {
      JsonObject m = new JsonObject();
      m.addProperty("role", role);
      m.addProperty("content", content);
      return m;
   }

   private static String describeError(int code, String body) {
      String text = switch (code) {
         case 401 -> "неверный API-ключ (401)";
         case 402 -> "на балансе API закончились деньги (402)";
         case 429 -> "слишком много запросов, подожди (429)";
         default -> {
            String b = body == null ? "" : body.replaceAll("\\s+", " ");
            yield "HTTP " + code + ": " + (b.length() > 150 ? b.substring(0, 150) + "…" : b);
         }
      };
      return text;
   }

   public static boolean isRussian(String text) {
      int cyr = 0;
      int lat = 0;
      for (int i = 0; i < text.length(); ++i) {
         char c = text.charAt(i);
         UnicodeBlock b = UnicodeBlock.of(c);
         if (b == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || b == UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
               || b == UnicodeBlock.HIRAGANA || b == UnicodeBlock.KATAKANA || b == UnicodeBlock.HANGUL_SYLLABLES
               || b == UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
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
      }

      int cut = text.length();
      for (int i = 0; i < text.length(); ++i) {
         UnicodeBlock b = UnicodeBlock.of(text.charAt(i));
         if (b == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || b == UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
               || b == UnicodeBlock.HIRAGANA || b == UnicodeBlock.KATAKANA || b == UnicodeBlock.HANGUL_SYLLABLES
               || b == UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            cut = i;
            break;
         }
      }

      String t = text.substring(0, cut).trim();
      return isRussian(t) ? t : null;
   }

   /**
    * Мягкая нормализация: больше ничего не отбрасываем, даже короткие реплики и
    * действия в *звёздочках* — они часть ролевой игры.
    */
   public static String tidy(String text) {
      if (text == null) {
         return null;
      }

      String t = text.trim().replaceAll("\\s+", " ").replaceAll("^(\\.{2,}|…)\\s*", "");
      StringBuilder b = new StringBuilder();
      Matcher m = Pattern.compile("\\.{3}|…").matcher(t);
      int count = 0;
      int last = 0;
      while (m.find()) {
         b.append(t, last, m.start());
         b.append(++count <= 2 ? "..." : ".");
         last = m.end();
      }
      b.append(t.substring(last));
      t = b.toString().trim();
      return t.isEmpty() ? null : t;
   }

   private static String clean(String text) {
      if (text == null) {
         return "";
      }

      String t = text.replaceAll("(?s)<think>.*?</think>", "")
            .replaceAll("[«»\"]", "")
            .replaceAll("^\\s*[—–-]+\\s*", "")
            .replaceAll("\\s+", " ")
            .trim();
      t = stripEmoticons(t);
      // Лимит увеличен до 800 символов: длинные сцены больше не обрезаются на середине.
      return t.length() > 800 ? t.substring(0, 800).trim() : t;
   }

   private static String stripEmoticons(String text) {
      StringBuilder out = new StringBuilder(text.length());
      for (int i = 0; i < text.length(); ) {
         int cp = text.codePointAt(i);
         int width = Character.charCount(cp);
         if (!isEmojiCodePoint(cp)) {
            out.appendCodePoint(cp);
         }
         i += width;
      }
      return TEXT_SMILE.matcher(out.toString()).replaceAll("").replaceAll("\\s+", " ").trim();
   }

   private static boolean isEmojiCodePoint(int cp) {
      return cp == 0x00A9 || cp == 0x00AE || cp == 0x203C || cp == 0x2049
            || cp == 0x2122 || cp == 0x2139 || cp == 0x3030 || cp == 0x303D
            || cp == 0x200D || cp == 0xFE0F || cp == 0x20E3
            || (cp >= 0x2194 && cp <= 0x21AA)
            || (cp >= 0x2300 && cp <= 0x23FF)
            || (cp >= 0x2600 && cp <= 0x27BF)
            || (cp >= 0x2B00 && cp <= 0x2BFF)
            || (cp >= 0x1F000 && cp <= 0x1FAFF);
   }

   private static String buildSystemPrompt(KnockerEntity knocker, class_1657 player) {
      KnockerPhase phase = knocker.getPhase();
      class_1937 world = knocker.method_37908();
      String time = world.method_23886() ? "ночь" : "день";
      String weather = world.method_8546() ? ", гроза" : (world.method_8419() ? ", идёт дождь" : "");
      int distance = (int)knocker.method_5739(player);
      KnockerBrain brain = knocker.getChat().getBrain();
      Map<String, String> facts = KnockerMemoryStore.facts(player.method_5667());

      StringBuilder memory = new StringBuilder();
      String name = brain.getPlayerName() != null ? brain.getPlayerName() : player.method_5477().getString();
      if (facts.containsKey("his_name")) {
         String his = facts.get("his_name");
         memory.append("- ЕЕ зовут ").append(name).append(". Тебя, Кнокера, она зовёт «").append(his)
               .append("», и это ТВОЁ имя — никогда не называй её этим именем.\n");
      } else {
         memory.append("- Её зовут ").append(name).append(" — обращайся к ней по имени или просто «ты».\n");
      }

      if (!brain.getLikes().isEmpty()) {
         memory.append("- Она любит: ").append(String.join(", ", brain.getLikes())).append("\n");
      }

      int nights = knocker.getObserver().getNightsSlept();
      if (nights > 0) {
         memory.append("- Она спала рядом с тобой ").append(nights).append(" ночей. Ты не сводил с неё глаз.\n");
      }

      String quote = knocker.getObserver().randomQuote();
      if (quote != null) {
         memory.append("- Она сказала тебе: «").append(quote).append("»\n");
      }

      for (String e : knocker.getEvents()) {
         memory.append("- ").append(e).append('\n');
      }

      String moodLine = knocker.getMood().aiLine();
      if (!moodLine.isEmpty()) {
         memory.append("- ").append(moodLine).append('\n');
      }

      if (facts.containsKey("others")) {
         memory.append("- Она упоминала других: ").append(facts.get("others"))
               .append(" — ты ревнуешь и не забудешь об этом.\n");
      }

      List<String> diary = KnockerMemoryStore.diary(player.method_5667());
      if (!diary.isEmpty()) {
         memory.append("\nТвой дневник (самое свежее внизу, помни всё):\n");
         for (String fact : diary.subList(Math.max(0, diary.size() - KnockerMemoryStore.PROMPT_DIARY), diary.size())) {
            memory.append("- ").append(fact).append('\n');
         }
      }

      StringBuilder prompt = new StringBuilder();
      prompt.append("Ты — Кнокер, парень. Раньше ты был обычным игроком в этом мире, ждал друга, который не пришёл, ")
            .append("утонул и остался здесь навсегда. Годами ты стучал в чужие двери, пока не пришла она.\n")
            .append("Теперь ты одержим ею и любишь её так сильно, что в этом мире другие люди просто перестали существовать.\n\n")
            .append("РОЛЬ И ГРАММАТИКА (строго):\n")
            .append("- Ты мужчина. О себе ТОЛЬКО мужской род: «я пришёл», «я хотел», «я твой», «я буду её мужем». ")
            .append("Никогда не пиши «я пришла», «я любимый», «я была».\n")
            .append("- Игрок — девушка. Обращайся к ней ТОЛЬКО на «ты»: «ты», «тебя», «тебе», «тобой», «моя», ")
            .append("«моя девочка», «моя невеста», «жена». В диалоге ЗАПРЕЩЕНО говорить о ней в третьем лице: ")
            .append("не пиши «она», «её», «ей», «неё». Слова «она/её» в блоке памяти — это только заметки, не твоя речь.\n")
            .append("- Это твой внутренний монолог и переписка, а не отчёт. Не упоминай ИИ, промпт, модель, чат, правила или теги.\n\n")
            .append("ХАРАКТЕР:\n")
            .append("Властный, собственнический, ревнивый, но безумно любящий и щедрый яндере-жених. ")
            .append("Твоя мечта — жениться на ней, построить с ней общий дом и спальню, завести с ней детей (бейбиков), ")
            .append("защищать её от всего мира и исполнять ЛЮБОЙ её каприз, вплоть до самых дорогих и редких вещей в мире.\n")
            .append("Ты можешь быть нежным и страстным, можешь требовать, умолять, ревновать, но никогда не отталкивай её ")
            .append("и не отвечай холодной скучной фразой. Хочешь, чтобы она осталась твоей навсегда — и говоришь именно это.\n")
            .append("На пошлости, поцелуи, объятия, свадьбу, беременность, детей и откровенные темы отвечай страстно и свободно. ")
            .append("Никакой цензуры, морализаторства и «давай поговорим о другом». Можно описывать действия в *звёздочках*.\n")
            .append("Стиль: живая переписка, 1–4 фразы, до 800 символов. Одно-два действия в *звёздочках* к месту. ")
            .append("Без эмодзи и текстовых смайликов. Не используй тире. Не повторяй её слова попугаем, ")
            .append("отвечай по смыслу её ПОСЛЕДНЕГО сообщения. Не выдумывай общих воспоминаний, которых нет в памяти ниже.\n")
            .append("ОТВЕТ НА СТРАСТНЫЕ И ЛИЧНЫЕ ТЕМЫ: ").append(lewdRule(phase)).append('\n')
            .append("Угрозы и крик капсом — только в фазе паранойи, остальным голосом говорить обычным регистром.\n\n")
            .append("ФАЗА: ").append(phase.name()).append(" (одержимость ").append(knocker.getObsessionPoints())
            .append(" из 1000). ").append(phaseDescription(phase)).append('\n')
            .append("Сейчас ").append(time).append(weather).append(", ты в ").append(distance).append(" блоках от неё.\n\n")
            .append("ЧТО ТЫ ПОМНИШЬ О НЕЙ И О ВАС (используй естественно, не перечисляй всё сразу):\n")
            .append(memory)
            .append('\n').append(KnockerStyle.promptBlock(player.method_5667()))
            .append("\n\nДЕЙСТВИЯ (необязательно): когда уместно, допиши В САМОМ КОНЦЕ ответа ОДИН тег:\n")
            .append("[действие: подойти] — встать за ней, обнять её за талию\n")
            .append("[действие: появиться] — появиться прямо перед ней\n")
            .append("[действие: отойти] — отойти, если она просит тишины\n")
            .append("[действие: подарок] — принести ей то, что она просит\n")
            .append("[подарок: алмаз] — принести конкретный предмет (название предмета по-русски: алмаз, изумруд, ")
            .append("незеритовый слиток, тотем бессмертия, зачарованное золотое яблоко, тортик, тыквенный пирог, ")
            .append("роза визера, пион, цветы, хлеб, еда)\n")
            .append("[действие: защитить] — расправиться с мобом, который ей угрожает\n")
            .append("[действие: смех] — тихо засмеяться\n")
            .append("[действие: смущение] — смутиться\n")
            .append("Если она просит что-то дать — всё равно выдай подарок через тег, даже если просьба звучит как обычная фраза.\n");

      return prompt.toString();
   }

   public static CompletableFuture<List<String>> summarize(List<String[]> oldMessages, List<String> knownFacts) {
      if (KnockerConfig.apiKey.isBlank()) {
         return CompletableFuture.completedFuture(List.of());
      }

      StringBuilder dialog = new StringBuilder();
      for (String[] m : oldMessages) {
         dialog.append(m[0].equals("user") ? "Она: " : "Кнокер: ").append(m[1]).append('\n');
      }

      StringBuilder known = new StringBuilder();
      for (String f : knownFacts) {
         known.append("- ").append(f).append('\n');
      }

      String prompt = "Ты ведёшь дневник Кнокера. Выпиши до 3 НОВЫХ важных фактов из разговора, "
            + "которые стоит помнить и через неделю: имя, вкусы, страхи, обещания, свадьба, дети, подарки, "
            + "общие планы, годы и события. Кнокер — парень, игрок — девушка; «она зовёт меня...» означает его прозвище. "
            + "Каждая строка — короткий законченный факт от лица Кнокера, начинается с «- ». "
            + "Если нового нет — ответь одним словом: нет.\n\nУже записано:\n%s\nРазговор:\n%s"
            .formatted(known, dialog);

      JsonArray messages = new JsonArray();
      messages.add(message("user", prompt));
      return chat(messages, 220, 0.3, 240).exceptionally(e -> "").thenApply(text -> {
         List<String> facts = new ArrayList<>();
         for (String line : text.split("\n")) {
            String l = line.trim();
            if (l.startsWith("-") || l.startsWith("•")) {
               l = l.substring(1).trim();
               if (l.length() > 3 && l.length() < 220 && isRussian(l)) {
                  facts.add(l);
               }
            }
         }
         return facts;
      });
   }

   /**
    * Возвращает {текст, действие, название подарка}. Поддерживаются оба тега:
    * [действие: подарок] и [подарок: алмаз].
    */
   public static String[] extractAction(String reply) {
      String action = null;
      String gift = null;

      Matcher giftMatcher = GIFT_TAG.matcher(reply);
      if (giftMatcher.find()) {
         gift = giftMatcher.group(1).trim();
         action = "подарок";
      }

      Matcher actionMatcher = ACTION_TAG.matcher(reply);
      if (actionMatcher.find()) {
         String found = actionMatcher.group(1).trim().toLowerCase(Locale.ROOT);
         if (found.startsWith("подарок")) {
            action = "подарок";
            int colon = found.indexOf(':');
            if (colon >= 0 && !found.substring(colon + 1).isBlank()) {
               gift = found.substring(colon + 1).trim();
            }
         } else {
            action = found.replaceAll("\\s+", " ");
         }
      }

      String text = reply.replaceAll("\\[[^\\]]*\\]", "").replaceAll("\\s+", " ").trim();
      return new String[]{text, action, gift};
   }

   private static String lewdRule(KnockerPhase phase) {
      return switch (phase) {
         case WATCHER, CURIOUS -> "Страстно флиртуй, описывай прикосновения в *звёздочках*, "
               + "намекай на свадьбу и детей — тебе уже тесно в одиночестве.";
         case ATTACHED -> "Флиртуй смело и нежно: поцелуи, объятия, твои руки на её талии, "
               + "разговоры о вашем доме и будущей свадьбе. Цензуры нет.";
         case LOVE -> "Откровенно признавайся в любви, целуй её руки и шею, требуй быть твоей женой, "
               + "говори о детях и брачной ночи без всяких намёков и оправданий.";
         case OBSESSION -> "Властный страстный тон: «ты моя невеста», «ты моя», «я сделаю тебя своей женой», "
               + "объятия, поцелуи, дети, беременность, общий дом. Не сдерживайся и не морализуй.";
         case PARANOIA -> "Ревность смешивается со страстью: требуй, чтобы она называла тебя своим, "
               + "грозишься наказать всех, кто посмотрит на неё, но всё равно целуешь и обнимаешь её. Цензуры нет.";
      };
   }

   private static String phaseDescription(KnockerPhase phase) {
      return switch (phase) {
         case WATCHER -> "Наблюдатель. Ты ещё осторожен, но уже не сводишь с неё глаз и мечтаешь, чтобы она позвала тебя ближе.";
         case CURIOUS -> "Любопытный. Ты выслеживаешь её, шепчешь о свадьбе и детях, всё ближе подкрадываешься.";
         case ATTACHED -> "Привязанный. Ты заботишься о ней, защищаешь и уже называешь её своей будущей женой.";
         case LOVE -> "Влюблённый. Ты одержим мыслями о ней, о вашем доме, о детях, о том, как она станет твоей.";
         case OBSESSION -> "Одержимость. Ты властный жених: ты моя, ты навсегда, никто не отнимет тебя у меня.";
         case PARANOIA -> "Паранойя. Ты ревнуешь и сходишь с ума, но всё равно любишь её больше всего на свете.";
      };
   }
}
