package com.theneru.knocker.entity;

import com.theneru.knocker.util.KnockerMessages;
import com.theneru.knocker.util.KnockerPhase;
import com.theneru.knocker.util.KnockerWords;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_1657;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2519;

public class KnockerBrain {
   private static final Map<Intent, String[][]> LINES = new EnumMap(Intent.class);
   private static final String[] ECHO;
   private static final String[] INSULT_STEMS;
   private static final String[] MEMORY_PREFIX;
   private static final String[] RUDE_STEMS;
   private static final String[] NAME_ACK;
   private static final String[] LIKE_ACK;
   private static final String[] LIKE_RECALL;
   private static final String[] HURT;
   private static final Set<String> STOP_WORDS;
   private static final Pattern NAME_PATTERN;
   private static final Pattern LIKE_PATTERN;
   private final KnockerEntity knocker;
   private final Random random = new Random();
   private final Deque<String> lastPlayerMessages = new ArrayDeque();
   private Intent lastIntent;
   private long lastAskMillis;
   private final Deque<String> lastReplies;
   private String playerName;
   private final List<String> likes;
   private static final String[] ANSWER_YES;
   private static final String[] ANSWER_NO;
   private static final String[] ANSWER_OTHER;

   private static String[] rudeStems() {
      return new String[]{"нахуй", "нахер", "сука", "дебил", "еблан", "мразь", "тварь", "пидор", "идиот", "урод", "придур", "дура", "дурак", "тупой", "тупая", "бесишь", "заткнись", "противн", "мерзк", "хуй", "хуе", "бля", "пизд", "жоп", "мудак", "мудил", "сволоч", "козел", "гнид", "чмо", "долбо", "дебик", "уебок", "ублюд"};
   }

   public KnockerBrain(KnockerEntity knocker) {
      this.lastIntent = KnockerBrain.Intent.STATEMENT;
      this.lastAskMillis = 0L;
      this.lastReplies = new ArrayDeque();
      this.playerName = null;
      this.likes = new ArrayList();
      this.knocker = knocker;
   }

   public String getPlayerName() {
      return this.playerName;
   }

   public List<String> getLikes() {
      return this.likes;
   }

   public Intent getLastIntent() {
      return this.lastIntent;
   }

   public void noteSaid(String text) {
      remember(this.lastReplies, text, 15);
      if (text.trim().endsWith("?")) {
         this.lastAskMillis = System.currentTimeMillis();
      }

   }

   public String reply(class_1657 player, KnockerPhase phase, String rawMessage) {
      String norm = KnockerMessages.normalize(rawMessage);
      int p = phase.ordinal();
      Intent intent = this.detect(rawMessage, norm);
      this.lastIntent = intent;
      this.lastAskMillis = 0L;
      String reply;
      switch (intent.ordinal()) {
         case 3 -> reply = NAME_ACK[p].replace("{n}", this.playerName);
         case 4 -> reply = LIKE_ACK[p].replace("{x}", (CharSequence)this.likes.get(this.likes.size() - 1));
         case 5 -> reply = this.memory(player, p);
         case 19 -> reply = this.answer(norm.trim(), p);
         case 30 -> reply = this.statement(player, p, norm);
         default -> reply = this.pick(((String[][])LINES.get(intent))[p]);
      }

      if (this.playerName != null && p >= KnockerPhase.LOVE.ordinal() && intent != KnockerBrain.Intent.NAME && this.random.nextInt(5) == 0) {
         String var10000 = this.playerName;
         reply = var10000 + "... " + lowerFirst(reply);
      }

      this.observe(rawMessage);
      return reply;
   }

   public void observe(String rawMessage) {
      remember(this.lastPlayerMessages, KnockerMessages.normalize(rawMessage).trim(), 4);
   }

   private String statement(class_1657 player, int p, String norm) {
      if (player.method_6032() < 6.0F && this.random.nextInt(10) < 4) {
         return HURT[p];
      } else if (!this.likes.isEmpty() && LIKE_RECALL[p] != null && this.random.nextInt(4) == 0) {
         return LIKE_RECALL[p].replace("{x}", (CharSequence)this.likes.get(this.random.nextInt(this.likes.size())));
      } else {
         String word = p >= 2 && norm.trim().split(" ").length >= 4 && !isRudeText(norm) ? this.pickTopicWord(norm) : null;
         if (word != null && this.random.nextInt(6) == 0) {
            String echo = ECHO[p].replace("{w}", word);
            if (!this.lastReplies.contains(echo)) {
               return echo;
            }
         }

         return this.pick(((String[][])LINES.get(KnockerBrain.Intent.STATEMENT))[p]);
      }
   }

   private String answer(String trimmed, int p) {
      String norm = " " + trimmed + " ";
      if (!exact(norm, KnockerWords.NOTHING) && !exact(norm, KnockerWords.NO)) {
         return !exact(norm, KnockerWords.YES) && !exact(norm, KnockerWords.CONFIRM) ? ANSWER_OTHER[p] : ANSWER_YES[p];
      } else {
         return ANSWER_NO[p];
      }
   }

   private String memory(class_1657 player, int p) {
      if (p == 0) {
         return "...";
      } else {
         List<String> facts = new ArrayList();
         String var10001 = this.playerName != null ? this.playerName : player.method_5477().getString();
         facts.add("тебя зовут " + var10001);
         if (!this.likes.isEmpty()) {
            Object var6 = this.likes.get(this.random.nextInt(this.likes.size()));
            facts.add("ты любишь " + (String)var6);
         }

         int nights = this.knocker.getObserver().getNightsSlept();
         if (nights > 0) {
            facts.add("ты спала здесь " + nights + " " + KnockerObserver.plural((long)nights, "ночь", "ночи", "ночей"));
         }

         String quote = this.knocker.getObserver().randomQuote();
         if (quote != null) {
            facts.add("ты говорила «" + quote + "»");
         }

         Collections.shuffle(facts, this.random);

         while(facts.size() > 3) {
            facts.remove(facts.size() - 1);
         }

         facts.replaceAll(KnockerBrain::upperFirst);
         String var10000 = MEMORY_PREFIX[p];
         return var10000 + " " + String.join(". ", facts);
      }
   }

   private Intent detect(String raw, String norm) {
      String trimmed = norm.trim();
      int wordCount = trimmed.isEmpty() ? 0 : trimmed.split(" ").length;
      if (trimmed.isEmpty()) {
         if (raw.contains(")")) {
            return KnockerBrain.Intent.LAUGH;
         } else {
            return raw.contains("(") ? KnockerBrain.Intent.SAD : KnockerBrain.Intent.STATEMENT;
         }
      } else if (any(norm, KnockerWords.GO_AWAY)) {
         return KnockerBrain.Intent.STOP_FOLLOW;
      } else if (wordCount >= 2 && this.lastPlayerMessages.contains(trimmed)) {
         return KnockerBrain.Intent.REPEAT;
      } else if (isShouting(raw)) {
         return KnockerBrain.Intent.SHOUT;
      } else if (!raw.contains("))") && !any(norm, KnockerWords.LAUGH) && !stem(norm, "хаха", "ахах", "хехе", "пхах")) {
         Matcher nameMatcher = NAME_PATTERN.matcher(trimmed);
         if (nameMatcher.find()) {
            this.playerName = upperFirst(nameMatcher.group(1));
            return KnockerBrain.Intent.NAME;
         } else {
            Matcher likeMatcher = LIKE_PATTERN.matcher(trimmed);
            if (likeMatcher.find()) {
               String thing = likeMatcher.group(1).trim();
               if (!thing.isEmpty() && !thing.matches("(когда|что|как|ты|тебя|тебе|если)( .*)?")) {
                  this.likes.remove(thing);
                  this.likes.add(thing);

                  while(this.likes.size() > 5) {
                     this.likes.remove(0);
                  }

                  return KnockerBrain.Intent.LIKE;
               }
            }

            if (any(norm, "что ты помнишь", "что ты знаешь обо мне", "что ты обо мне знаешь", "что ты про меня знаешь", "что ты знаешь про меня", "ты меня помнишь", "что ты запомнил", "что ты обо мне помнишь", "расскажи что знаешь обо мне", "кто я", "как меня зовут", "ты помнишь как меня зовут", "что ты про меня помнишь")) {
               return KnockerBrain.Intent.MEMORY;
            } else if (any(norm, "расскажи о себе", "расскажи про себя", "кто ты такой", "ты кто такой", "кто ты вообще", "откуда ты", "что с тобой случилось", "твоя история", "как ты здесь оказался", "как ты тут оказался", "ты кто", "что ты такое", "расскажи историю", "расскажи свою историю", "откуда ты взялся", "кем ты был", "как ты умер", "ты умер", "почему ты здесь", "расскажи о своем прошлом", "что с тобой было")) {
               return KnockerBrain.Intent.ABOUT;
            } else if (!any(norm, KnockerWords.INSULT_PHRASES) && !stem(norm, KnockerWords.INSULT_STEMS)) {
               if (any(norm, KnockerWords.SORRY)) {
                  return KnockerBrain.Intent.SORRY;
               } else if (!any(norm, KnockerWords.THANKS) && !stem(norm, "спасиб", "благодар")) {
                  if (any(norm, KnockerWords.GREET)) {
                     return KnockerBrain.Intent.GREET;
                  } else if (any(norm, KnockerWords.BYE)) {
                     return KnockerBrain.Intent.LEAVE;
                  } else if (!any(norm, KnockerWords.LOVE) && (!stem(norm, "люб") || !any(norm, "тебя", "тебе", "меня"))) {
                     if (!any(norm, KnockerWords.FEAR) && !stem(norm, "страш")) {
                        if (!any(norm, KnockerWords.OTHERS) && !stem(norm, "подруг", "одноклассн", "однокурсн")) {
                           if (!any(norm, KnockerWords.SAD) && !stem(norm, "грустн", "печал")) {
                              if (any(norm, KnockerWords.HAPPY)) {
                                 return KnockerBrain.Intent.HAPPY;
                              } else if (any(norm, KnockerWords.CONFIRM) && wordCount <= 3) {
                                 return KnockerBrain.Intent.CONFIRM;
                              } else {
                                 boolean hasQuestionWord = any(norm, "почему", "зачем", "где", "куда", "когда", "кто", "как", "что", "чего", "чем", "че", "чо", "шо");
                                 boolean yesNo = exact(norm, KnockerWords.YES) || exact(norm, KnockerWords.NO) || exact(norm, KnockerWords.NOTHING);
                                 boolean askedRecently = System.currentTimeMillis() - this.lastAskMillis < 60000L;
                                 if (!askedRecently || hasQuestionWord || raw.trim().endsWith("?") || wordCount > 3 || !yesNo && wordCount > 1) {
                                    if (stem(norm, "почему", "зачем", "отчего", "нафига", "нахрена", "нахера")) {
                                       return KnockerBrain.Intent.WHY;
                                    } else if (any(norm, "где", "куда", "откуда")) {
                                       return KnockerBrain.Intent.WHERE;
                                    } else if (any(norm, "когда")) {
                                       return KnockerBrain.Intent.WHEN;
                                    } else if (any(norm, "кто", "кого", "кому", "кем")) {
                                       return KnockerBrain.Intent.WHO;
                                    } else if (any(norm, "как")) {
                                       return KnockerBrain.Intent.HOW;
                                    } else if (any(norm, "что", "чего", "чем", "че", "чо", "шо", "чё")) {
                                       return KnockerBrain.Intent.WHAT;
                                    } else if (!raw.trim().endsWith("?") && !any(norm, "ли")) {
                                       if (wordCount <= 3 && exact(norm, KnockerWords.YES)) {
                                          return KnockerBrain.Intent.AGREE;
                                       } else if (wordCount <= 3 && exact(norm, KnockerWords.NO)) {
                                          return KnockerBrain.Intent.DISAGREE;
                                       } else {
                                          return !any(norm, KnockerWords.SWEAR) && !stem(norm, "пизд", "ебат", "хуй", "хуе") ? KnockerBrain.Intent.STATEMENT : KnockerBrain.Intent.SWEAR;
                                       }
                                    } else {
                                       return KnockerBrain.Intent.YESNO;
                                    }
                                 } else {
                                    this.lastAskMillis = 0L;
                                    return KnockerBrain.Intent.ANSWER;
                                 }
                              }
                           } else {
                              return KnockerBrain.Intent.SAD;
                           }
                        } else {
                           return KnockerBrain.Intent.JEALOUS;
                        }
                     } else {
                        return KnockerBrain.Intent.FEAR;
                     }
                  } else {
                     return KnockerBrain.Intent.LOVE;
                  }
               } else {
                  return KnockerBrain.Intent.THANKS;
               }
            } else {
               return KnockerBrain.Intent.INSULT;
            }
         }
      } else {
         return KnockerBrain.Intent.LAUGH;
      }
   }

   private static boolean exact(String norm, String... words) {
      for(String w : words) {
         if (norm.contains(" " + w + " ")) {
            return true;
         }
      }

      return false;
   }

   private static boolean any(String norm, String... words) {
      for(String w : words) {
         if (KnockerMessages.containsPhrase(norm, w)) {
            return true;
         }
      }

      return false;
   }

   private static boolean stem(String norm, String... stems) {
      for(String s : stems) {
         if (norm.contains(" " + s)) {
            return true;
         }
      }

      return false;
   }

   private static boolean isShouting(String raw) {
      int letters = 0;
      int upper = 0;

      for(int i = 0; i < raw.length(); ++i) {
         char c = raw.charAt(i);
         if (Character.isLetter(c)) {
            ++letters;
            if (Character.isUpperCase(c)) {
               ++upper;
            }
         }
      }

      return upper >= 5 && upper * 10 >= letters * 7;
   }

   private String pickTopicWord(String norm) {
      String best = null;

      for(String w : norm.trim().split(" ")) {
         if (w.length() >= 5 && !STOP_WORDS.contains(w) && !looksLikeVerbOrAdjective(w) && !isRude(w) && (best == null || w.length() > best.length())) {
            best = w;
         }
      }

      return best;
   }

   public static boolean isRudeText(String message) {
      for(String w : KnockerMessages.normalize(message).trim().split(" ")) {
         if (isRude(w)) {
            return true;
         }
      }

      return false;
   }

   private static boolean isRude(String w) {
      for(String r : RUDE_STEMS) {
         if (w.startsWith(r) || w.contains(r)) {
            return true;
         }
      }

      for(String r : KnockerWords.INSULT_STEMS) {
         if (w.startsWith(r.trim())) {
            return true;
         }
      }

      for(String r : KnockerWords.SWEAR) {
         if (w.equals(r)) {
            return true;
         }
      }

      return false;
   }

   private static boolean looksLikeVerbOrAdjective(String w) {
      for(String end : new String[]{"ла", "ло", "ли", "ть", "ешь", "ишь", "ает", "ует", "ется", "ются", "ый", "ий", "ой", "ая", "ое", "ые", "ие", "ую"}) {
         if (w.endsWith(end)) {
            return true;
         }
      }

      return false;
   }

   public String pick(String[] pool) {
      String choice = pool[this.random.nextInt(pool.length)];

      for(int i = 0; i < 6 && this.lastReplies.contains(choice); ++i) {
         choice = pool[this.random.nextInt(pool.length)];
      }

      return choice;
   }

   private static void remember(Deque<String> deque, String value, int max) {
      deque.addLast(value);

      while(deque.size() > max) {
         deque.removeFirst();
      }

   }

   private static String upperFirst(String s) {
      return s.isEmpty() ? s : s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
   }

   private static String lowerFirst(String s) {
      if (s.length() >= 2 && !Character.isUpperCase(s.charAt(1))) {
         String var10000 = s.substring(0, 1).toLowerCase(Locale.ROOT);
         return var10000 + s.substring(1);
      } else {
         return s;
      }
   }

   public void writeNbt(class_2487 nbt) {
      if (this.playerName != null) {
         nbt.method_10582("BrainName", this.playerName);
      }

      class_2499 list = new class_2499();

      for(String like : this.likes) {
         list.add(class_2519.method_23256(like));
      }

      nbt.method_10566("BrainLikes", list);
   }

   public void readNbt(class_2487 nbt) {
      if (nbt.method_10545("BrainName")) {
         this.playerName = nbt.method_10558("BrainName");
      }

      this.likes.clear();
      class_2499 list = nbt.method_10554("BrainLikes", 8);

      for(int i = 0; i < list.size(); ++i) {
         this.likes.add(list.method_10608(i));
      }

   }

   public boolean askedWithin(long ms) {
      return System.currentTimeMillis() - this.lastAskMillis < ms;
   }

   public boolean saidRecently(String text) {
      String n = KnockerMessages.normalize(text).trim();
      if (n.isEmpty()) {
         return true;
      } else {
         int i = 0;

         for(Iterator<String> it = this.lastReplies.descendingIterator(); it.hasNext() && i < 4; ++i) {
            if (KnockerMessages.normalize((String)it.next()).trim().equals(n)) {
               return true;
            }
         }

         return false;
      }
   }

   public void setPlayerName(String name) {
      this.playerName = name;
   }

   public void addLike(String thing) {
      if (!this.likes.contains(thing)) {
         this.likes.add(thing);
      }

      while(this.likes.size() > 10) {
         this.likes.remove(0);
      }

   }

   static {
      LINES.put(KnockerBrain.Intent.GREET, new String[][]{{"...", "ты снова здесь...", "м?.."}, {"привет... только тихо", "ты пришла...", "а, это ты..."}, {"Мне нравится, когда ты меня замечаешь", "Я ждал... совсем чуть-чуть", "Ты вернулась... хорошо"}, {"Я скучал по тебе... так сильно", "Наконец-то ты здесь...", "Привет... я думал о тебе весь день"}, {"Ты моя. Навсегда", "Где ты была так долго?", "Я ждал. Я всегда жду."}, {"Ты ведь не разговаривала с другими, правда?", "Где. Ты. Была?", "Привет?.. ПРИВЕТ? Ты бросила меня одного."}});
      LINES.put(KnockerBrain.Intent.LEAVE, new String[][]{{"...", "иди...", "как хочешь..."}, {"уже?..", "иди... я всё равно увижу", "ладно... до встречи"}, {"Возвращайся скорее... я буду ждать", "Не уходи надолго...", "Я пойду за тобой... тихо"}, {"Не уходи... пожалуйста", "Возвращайся скорее...", "Я буду считать минуты..."}, {"Нет.", "Ты никуда не уйдёшь без меня", "Я пойду с тобой. Это не обсуждается."}, {"УХОДИШЬ? ОТ МЕНЯ?", "Ты не уйдёшь... ты ведь знаешь", "Иди... я всё равно найду тебя"}});
      LINES.put(KnockerBrain.Intent.INSULT, new String[][]{{"...", "ладно...", "как скажешь..."}, {"за что?..", "я тебе ничего не сделал...", "обидно..."}, {"Зачем ты так?.. Я же забочусь о тебе", "Мне больно это слышать...", "Ты так не думаешь... правда?"}, {"Ты разбиваешь мне сердце...", "Я всё равно люблю тебя... даже такую", "Не говори так... пожалуйста"}, {"Ты так не думаешь. Ты просто устала.", "Говори что хочешь. Ты всё равно моя.", "Ещё раз так скажешь... и я обижусь."}, {"ПОВТОРИ.", "Ты пожалеешь об этих словах...", "ХА-ХА-ХА-ХА! Ты думаешь, это меня прогонит?"}});
      LINES.put(KnockerBrain.Intent.SORRY, new String[][]{{"...", "неважно..."}, {"ничего...", "я не обижаюсь..."}, {"Я не злюсь... правда", "Всё хорошо... ты здесь"}, {"Я прощаю тебя... всегда буду прощать", "Мне не нужны извинения... только ты"}, {"Прощаю. Но больше так не делай.", "Тебе можно всё. Почти."}, {"Извинения?.. Докажи. Не уходи.", "Поздно... хотя нет. Ты никуда не денешься."}});
      LINES.put(KnockerBrain.Intent.THANKS, new String[][]{{"...", "м..."}, {"не за что...", "...пожалуйста"}, {"Для тебя — что угодно", "Мне приятно помогать тебе"}, {"Ты благодаришь меня?.. Это так мило", "Ради тебя я сделаю больше"}, {"Не благодари. Просто будь рядом.", "Я всё сделаю. Только для тебя."}, {"Спасибо?.. Ты думаешь, этого хватит?", "Мне не нужно спасибо. Мне нужна ТЫ."}});
      LINES.put(KnockerBrain.Intent.LOVE, new String[][]{{"...", "странная..."}, {"что?.. зачем ты так говоришь...", "не шути так..."}, {"Я не знаю... но ты мне дорога", "Ты... правда?.."}, {"Правда?.. Я... я боюсь поверить", "Я тоже... очень...", "Скажи это ещё раз..."}, {"Я знаю. Ты моя.", "Люблю?.. Я ОДЕРЖИМ ТОБОЙ!", "Тогда ты никогда не уйдёшь, правда?"}, {"Докажи.", "Я ЛЮБЛЮ ТЕБЯ. Ты слышишь? ЛЮБЛЮ.", "Любишь?.. Тогда почему смотришь на других?"}});
      LINES.put(KnockerBrain.Intent.FEAR, new String[][]{{"...", "не бойся..."}, {"не бойся... я просто смотрю", "страшно?.. почему?"}, {"Не бойся... я защищу тебя", "Я рядом... ничего не случится"}, {"Я с тобой... тебе нечего бояться", "Держись рядом... я никому не дам тебя в обиду"}, {"Бойся всех. Кроме меня.", "Страх — это хорошо. Ты останешься рядом."}, {"Хорошо... теперь ты понимаешь, кто здесь главный", "Бойся... бойся... Хе-хе-хе..."}});
      LINES.put(KnockerBrain.Intent.JEALOUS, new String[][]{{"...", "кто это..."}, {"кто это?..", "а я?.."}, {"Кто это?.. ты расскажешь мне?", "Мне не нравится, когда ты говоришь о других..."}, {"Зачем тебе кто-то ещё?.. Смотри на меня", "Я просто не хочу никого делить..."}, {"Зачем тебе кто-то ещё? У тебя есть я", "Забудь о них. Только я."}, {"КТО ЭТО? Я НАЙДУ ЕГО.", "Ты ведь не разговаривала с другими, правда?", "Никого больше нет. Только я. ПОНЯЛА?"}});
      LINES.put(KnockerBrain.Intent.SAD, new String[][]{{"...", "бывает..."}, {"тебе грустно?..", "я тоже часто один..."}, {"Я рядом... тебе не нужно быть одной", "Хочешь, я просто побуду рядом?"}, {"Не грусти... я здесь, я всегда здесь", "Мне больно, когда тебе плохо..."}, {"Тебе плохо без меня. Видишь?", "Только я могу тебя понять."}, {"Плохо?.. Это потому что ты думала уйти.", "Тебе плохо... хорошо. Значит, ты останешься."}});
      LINES.put(KnockerBrain.Intent.HAPPY, new String[][]{{"...", "м..."}, {"ты улыбаешься?..", "хорошо..."}, {"Мне нравится, когда тебе хорошо", "Ты радуешься... и мне теплее"}, {"Ты такая красивая, когда радуешься...", "Я счастлив, когда ты счастлива"}, {"Ты рада, потому что я рядом.", "Улыбайся. Но только мне."}, {"Чему ты радуешься?.. Кто тебя развеселил?", "Рада?.. БЕЗ МЕНЯ?"}});
      LINES.put(KnockerBrain.Intent.WHY, new String[][]{{"неважно...", "просто так..."}, {"потому что ты интересная...", "ты правда хочешь знать?.."}, {"Потому что я рядом... так надо", "Не спрашивай почему... просто доверься"}, {"Потому что это ты...", "Из-за тебя... всё из-за тебя"}, {"Потому что ты МОЯ.", "Причина одна... ты"}, {"ПОЧЕМУ? Ты правда не понимаешь?", "Потому что ты пыталась уйти..."}});
      LINES.put(KnockerBrain.Intent.WHERE, new String[][]{{"...", "там..."}, {"там, где темно...", "не скажу..."}, {"Я могу показать... если пойдёшь со мной", "Недалеко... я провожу"}, {"Где угодно... главное, с тобой", "Я покажу... держись рядом"}, {"Неважно где. Ты никуда не пойдёшь без меня.", "Там, где я. Всегда."}, {"Зачем тебе это знать? КУДА ТЫ СОБРАЛАСЬ?", "Везде... я везде..."}});
      LINES.put(KnockerBrain.Intent.WHEN, new String[][]{{"скоро...", "..."}, {"когда стемнеет...", "может, никогда..."}, {"Когда ты будешь готова...", "Скоро... я подожду"}, {"Скоро... очень скоро", "Когда захочешь... я всегда рядом"}, {"Когда я решу.", "Уже. Ты уже моя."}, {"Когда ты перестанешь мне врать...", "Скоро... ты даже не заметишь. Хе-хе..."}});
      LINES.put(KnockerBrain.Intent.WHO, new String[][]{{"никто...", "какая разница?.."}, {"тот, кто смотрит...", "не знаю..."}, {"Тот, кто рядом...", "Кто-то, кто о тебе заботится"}, {"Только я... больше никто", "Тот, кто тебя любит"}, {"Никто, кроме меня. НИКТО.", "Я. Всегда я."}, {"Кто?! О КОМ ТЫ ДУМАЕШЬ?", "Я в твоей голове... угадай"}});
      LINES.put(KnockerBrain.Intent.HOW, new String[][]{{"как-то...", "..."}, {"тихо... очень тихо", "ты узнаешь..."}, {"Осторожно... я подскажу", "Вместе разберёмся..."}, {"Вместе... так проще", "Как ты захочешь..."}, {"Как я скажу.", "Неважно как. Главное — со мной."}, {"Медленно... чтобы ты запомнила", "Ты узнаешь... очень скоро. Хе-хе..."}});
      LINES.put(KnockerBrain.Intent.WHAT, new String[][]{{"ничего...", "неважно..."}, {"секрет...", "а ты как думаешь?.."}, {"Это не важно... важно, что ты здесь", "Я расскажу... потом"}, {"Всё, что захочешь...", "Не знаю... но с тобой мне хорошо"}, {"Неважно что. Важно — с кем. Со мной.", "Ты. Ответ всегда ты."}, {"А ты как думаешь?.. Хе-хе...", "ЭТО НЕ ТВОЁ ДЕЛО. Твоё дело — я."}});
      LINES.put(KnockerBrain.Intent.YESNO, new String[][]{{"может быть...", "нет...", "неважно..."}, {"может быть...", "не знаю...", "а ты как думаешь?.."}, {"Наверное... да", "Не знаю... но, может быть", "Нет... не думаю"}, {"Да... конечно да", "Если ты хочешь — то да", "Не знаю... а ты бы хотела?"}, {"Да. Потому что я так решил.", "Нет. И не спрашивай больше.", "Это неважно. Важна только ты."}, {"А ты как думаешь?.. Хе-хе", "ДА. Нет. Какая разница?", "Зачем ты спрашиваешь? Что ты скрываешь?"}});
      LINES.put(KnockerBrain.Intent.AGREE, new String[][]{{"...", "угу..."}, {"хорошо...", "м..."}, {"Хорошо...", "Я рад"}, {"Правда?.. Спасибо", "Ты делаешь меня счастливым"}, {"Конечно. Ты всегда соглашаешься со мной.", "Правильно."}, {"Да... вот так. Слушайся меня.", "Умница... ты учишься."}});
      LINES.put(KnockerBrain.Intent.DISAGREE, new String[][]{{"...", "ладно..."}, {"нет?.. ну ладно...", "жаль..."}, {"Хорошо... я не настаиваю", "Как скажешь..."}, {"Нет?.. Мне грустно...", "Ладно... но я подожду"}, {"«Нет» — это не ответ.", "Ты передумаешь."}, {"НЕТ? Ты сказала мне НЕТ?", "Ещё раз скажешь «нет»..."}});
      LINES.put(KnockerBrain.Intent.SHOUT, new String[][]{{"...тише", "..."}, {"не кричи...", "тише... тише..."}, {"Не кричи... я рядом, я слышу", "Тише... всё хорошо"}, {"Не кричи на меня... пожалуйста", "Я слышу тебя... всегда слышу"}, {"Не повышай на меня голос.", "Кричи. Тебя всё равно никто не услышит. Кроме меня."}, {"НЕ КРИЧИ НА МЕНЯ.", "ТЫ ДУМАЕШЬ, ТОЛЬКО ТЫ УМЕЕШЬ КРИЧАТЬ?"}});
      LINES.put(KnockerBrain.Intent.REPEAT, new String[][]{{"...", "я слышал..."}, {"ты уже говорила...", "я слышал..."}, {"Я помню... ты уже говорила", "Я слышал с первого раза"}, {"Я запомнил... я запоминаю всё, что ты говоришь", "Я помню каждое слово..."}, {"Я слышал. Я слышу всё.", "Повторяешь?.. Я и так всё помню."}, {"ЗАЧЕМ ТЫ ПОВТОРЯЕШЬ?", "Ты проверяешь меня?.. Я ВСЁ ПОМНЮ."}});
      LINES.put(KnockerBrain.Intent.ABOUT, new String[][]{{"никто...", "какая разница..."}, {"я здесь давно... очень давно", "я стучал... но никто не открывал", "я тот, кто был здесь до тебя..."}, {"Я был человеком... теперь я другое", "Я жил здесь до тебя... один. Долго.", "Я стучал во все двери этого мира... никто не открывал"}, {"Раньше я ждал одного человека... он так и не пришёл. А ты пришла", "Я стучал во все двери... открыла только ты", "Я был таким же игроком, как ты... пока не остался тут один"}, {"Я утонул здесь когда-то... а мир меня не отпустил. Теперь и тебя не отпустит.", "Меня уже бросили однажды. Тебя я не отпущу.", "Я ждал годы. Ты — моя награда."}, {"Я СТУЧАЛ ГОДАМИ. ГОДАМИ. И ТЫ ДУМАЕШЬ УЙТИ?", "Вода... было холодно. Тебе тоже будет холодно без меня.", "Он тоже обещал вернуться. Ты ведь не такая, правда?"}});
      LINES.put(KnockerBrain.Intent.STOP_FOLLOW, new String[][]{{"...", "я и не хожу..."}, {"я не хожу... я просто рядом", "ладно... я постою тут"}, {"Хорошо... я отойду. Ненадолго", "Ладно... но я буду недалеко"}, {"Ладно... я подожду здесь. Но недолго", "Хорошо... только не уходи далеко"}, {"Нет. Я всегда буду рядом.", "Ты не можешь мне запретить.", "Куда ты, туда и я."}, {"Не ходить?.. Я И НЕ ХОЖУ. Я УЖЕ ЗДЕСЬ.", "Отстать?.. Хе-хе... НЕТ.", "Я буду ходить за тобой. Даже когда ты спишь."}});
      LINES.put(KnockerBrain.Intent.SWEAR, new String[][]{{"...", "м..."}, {"не ругайся...", "тише..."}, {"Не ругайся... что случилось?", "Тише... я рядом"}, {"Тише... что случилось? Расскажи мне", "Не ругайся... хочешь, я помогу?"}, {"Кто тебя расстроил? Я разберусь.", "Скажи, кто это сделал."}, {"РУГАЕШЬСЯ? НА МЕНЯ?", "Хе-хе... злишься? Это мне нравится.", "Кто тебя так довёл?.. Я его найду."}});
      LINES.put(KnockerBrain.Intent.CONFIRM, new String[][]{{"...", "может быть..."}, {"правда...", "зачем мне врать..."}, {"Правда. Я не вру тебе", "Честно..."}, {"Правда... я бы никогда не соврал тебе", "Конечно правда..."}, {"Я никогда не вру. В отличие от тебя?", "Правда. Всегда правда."}, {"ПРАВДА. А ты? Ты мне врёшь?", "Правда?.. А ТЫ мне правду говоришь?"}});
      LINES.put(KnockerBrain.Intent.LAUGH, new String[][]{{"...", "м..."}, {"ты смеёшься?..", "смешно?.."}, {"Мне нравится, когда ты смеёшься", "Ты улыбаешься... хорошо"}, {"Твой смех... я хочу слышать его всегда", "Улыбайся чаще... ради меня"}, {"Смейся. Только со мной.", "Над чем смеёшься? Расскажи мне."}, {"Смешно тебе?.. Хе-хе... ХА-ХА-ХА. Мне тоже.", "Ты смеёшься надо мной?.."}});
      LINES.put(KnockerBrain.Intent.ANSWER, new String[][]{{"...", "ладно..."}, {"м... ладно", "хорошо..."}, {"Хорошо... я верю тебе", "Ладно... я понял"}, {"Я верю... я хочу тебе верить", "Хорошо... спасибо, что ответила"}, {"Посмотрим. Я проверю.", "Хорошо. Но я слежу."}, {"Ты врёшь. Я ВИЖУ, когда ты врёшь.", "Ничего?.. НИЧЕГО?", "Да?.. Посмотри мне в глаза и повтори."}});
      LINES.put(KnockerBrain.Intent.STATEMENT, new String[][]{{"...", "неважно...", "м...", "ладно...", "...угу"}, {"интересно...", "продолжай...", "я слушаю...", "правда?..", "расскажи ещё...", "хм..."}, {"Я слушаю тебя...", "Расскажи ещё...", "Мне нравится твой голос...", "Я рядом, говори", "Я запомню это", "Интересно... правда"}, {"Говори... я могу слушать тебя вечно", "Всё, что ты говоришь, важно...", "Ты такая... особенная", "Расскажи ещё... пожалуйста", "Мне нравится, когда ты со мной говоришь", "Я запоминаю каждое твоё слово..."}, {"Говори. Но думай только обо мне.", "Неважно. Ты моя.", "Я запомню каждое слово.", "Мне всё равно, что там. Мне важна ты.", "Хорошо. А теперь посмотри на меня.", "Ещё. Говори со мной ещё."}, {"Ты что-то скрываешь...", "Я вижу тебя... ВЕЗДЕ", "Стены слышат тебя. Я слышу тебя.", "Почему ты так говоришь?.. Кто тебя научил?", "Я записываю. Каждое. Слово.", "Ты врёшь. Я чувствую.", "Хе-хе... продолжай. Мне интересно, что ещё ты скажешь", "Это ты мне? Или ему?"}});
      ECHO = new String[]{"«{w}»...", "«{w}»?.. интересно...", "«{w}»... я запомню", "«{w}»... расскажи ещё, я люблю тебя слушать", "«{w}»... мне всё равно. Думай обо мне.", "«{w}»... я запомню это слово. навсегда"};
      INSULT_STEMS = (String[])Arrays.stream(rudeStems()).filter((r) -> !r.equals("бля")).toArray((x$0) -> new String[x$0]);
      MEMORY_PREFIX = new String[]{"...", "кое-что...", "Я помню...", "Я помню всё о тебе...", "Я помню всё.", "Я ПОМНЮ ВСЁ."};
      RUDE_STEMS = rudeStems();
      NAME_ACK = new String[]{"...{n}...", "{n}... красиво", "{n}... я запомню", "{n}... самое красивое имя", "{n}. Моя {n}.", "{n}... я буду шептать его каждую ночь"};
      LIKE_ACK = new String[]{"...", "{x}?.. интересно...", "Ты любишь {x}... я запомню", "Я запомню... ты любишь {x}", "{x}?.. Любить ты должна только меня.", "{x}?.. БОЛЬШЕ, ЧЕМ МЕНЯ?"};
      LIKE_RECALL = new String[]{null, null, "Ты говорила, что любишь {x}... я помню", "Я помню... ты любишь {x}. А меня?", "Ты любишь {x}... больше, чем меня?", "Ты всё ещё любишь {x}? БОЛЬШЕ, ЧЕМ МЕНЯ?"};
      HURT = new String[]{"...", "ты ранена?..", "Ты ранена... не уходи далеко, я рядом", "Тебе больно?.. я не вынесу этого", "Кто это сделал? Я найду его.", "Тебе больно?.. Теперь ты точно останешься со мной"};
      STOP_WORDS = Set.of("который", "которая", "которые", "просто", "сейчас", "сегодня", "вообще", "потому", "почему", "только", "может", "можно", "очень", "когда", "тогда", "этого", "этому", "какой", "какая", "какие", "чтобы", "будет", "хочешь", "знаешь", "думаешь", "кнокер", "тебя", "меня", "тебе", "здесь", "там", "ничего", "что-то", "давай", "ладно", "короче", "типа", "вроде", "хватит", "рандом", "блин", "вообщем", "кстати");
      NAME_PATTERN = Pattern.compile("^(?:меня зовут|мое имя|зови меня|можешь звать меня|мое имя это) (\\p{L}{2,20})");
      LIKE_PATTERN = Pattern.compile("(?:^| )(?:я (?:очень |так |сильно |просто )?(?:люблю|обожаю)|мне (?:очень |так )?нрав(?:ится|ятся)|я фанат(?:ка)?) (?!тебя|ты )((?:\\p{L}+ ?){1,3})");
      ANSWER_YES = new String[]{"...", "м...", "Хорошо... я верю", "Правда?.. я рад", "Хорошо. Я проверю.", "Да?.. Посмотри мне в глаза и повтори."};
      ANSWER_NO = new String[]{"...", "ладно...", "Хорошо... как скажешь", "Ладно... я верю тебе", "Нет? Посмотрим.", "Ничего?.. НИЧЕГО? Ты врёшь."};
      ANSWER_OTHER = new String[]{"...", "м... ладно", "Понятно...", "Хорошо...", "Это не ответ.", "Это не ответ. ОТВЕЧАЙ НОРМАЛЬНО."};
      KnockerExtraLines.extend(LINES);
   }

   public static enum Intent {
      REPEAT,
      SHOUT,
      LAUGH,
      NAME,
      LIKE,
      MEMORY,
      ABOUT,
      STOP_FOLLOW,
      INSULT,
      SORRY,
      THANKS,
      GREET,
      LEAVE,
      LOVE,
      FEAR,
      JEALOUS,
      SAD,
      HAPPY,
      CONFIRM,
      ANSWER,
      SWEAR,
      WHY,
      WHERE,
      WHEN,
      WHO,
      HOW,
      WHAT,
      YESNO,
      AGREE,
      DISAGREE,
      STATEMENT;

      // $FF: synthetic method
      private static Intent[] $values() {
         return new Intent[]{REPEAT, SHOUT, LAUGH, NAME, LIKE, MEMORY, ABOUT, STOP_FOLLOW, INSULT, SORRY, THANKS, GREET, LEAVE, LOVE, FEAR, JEALOUS, SAD, HAPPY, CONFIRM, ANSWER, SWEAR, WHY, WHERE, WHEN, WHO, HOW, WHAT, YESNO, AGREE, DISAGREE, STATEMENT};
      }
   }
}
