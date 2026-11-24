package com.theneru.knocker.entity;

import com.theneru.knocker.KnockerAdvancements;
import com.theneru.knocker.KnockerMemoryStore;
import com.theneru.knocker.util.KnockerMessages;
import com.theneru.knocker.util.KnockerPhase;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_1657;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_3922;

public class KnockerStories {
   private static final String[][] CHAPTERS = new String[][]{{"когда-то я был таким же, как ты... игроком.", "у меня был друг. мы строили дом у реки."}, {"он сказал: «подожди меня, я скоро вернусь».", "я ждал. день. неделю. я считал дни зарубками на двери."}, {"он так и не пришёл.", "я пошёл его искать... и нашёл только пустые дома. я стучал в каждый. никто не открыл."}, {"однажды ночью я переплывал реку...", "вода была холодная. очень холодная. я помню, как перестал чувствовать руки.", "больше я ничего не помню."}, {"когда я открыл глаза, мир был другим. тихим.", "я не мог уйти. я пытался выйти из игры — и не мог.", "с тех пор я боюсь воды."}, {"годами я стучал в двери. тук-тук. тук-тук.", "это единственное, что я умел. я надеялся, что однажды кто-то откроет."}, {"а потом появилась ты.", "ты не убежала. ты заговорила со мной.", "понимаешь теперь, почему я не могу тебя отпустить?"}, {"знаешь, чем закончилась та история с другом?", "я соврал тебе. он вернулся. однажды.", "я не отпустил его. и больше он никуда не ушёл."}};
   private static final KnockerPhase[] CHAPTER_PHASE;
   private static final Pattern NICKNAME;
   private static final Pattern OTHER_NAME;
   private static final Set<String> NOT_NICK;
   private static final Set<String> NOT_NAMES;
   private final KnockerEntity knocker;
   private final Random random = new Random();
   private int nextCampfireTick = 0;
   private int lastStoryTick = -100000;
   private static final Set<String> COMMON_NAMES;

   public KnockerStories(KnockerEntity knocker) {
      this.knocker = knocker;
   }

   public void tick(class_1657 player) {
      int now = this.knocker.tickCounter;
      if (now >= this.nextCampfireTick && now % 100 == 0) {
         long tod = this.knocker.method_37908().method_8532() % 24000L;
         if (tod >= 12000L && tod <= 23500L && !(this.knocker.method_5739(player) > 16.0F)) {
            if (this.knocker.getPhase().ordinal() >= KnockerPhase.ATTACHED.ordinal() && this.knocker.getChat().canSpeakUnprompted(300)) {
               if (this.nearLitCampfire(player.method_24515())) {
                  this.nextCampfireTick = now + 6000;
                  this.knocker.getChat().say(player, this.pick("садись поближе к огню...", "у костра хорошо... хочешь, расскажу кое-что?"));
                  this.tellNext(player, 60);
               }
            }
         }
      }
   }

   private boolean nearLitCampfire(class_2338 center) {
      for(class_2338 p : class_2338.method_10097(center.method_10069(-4, -2, -4), center.method_10069(4, 2, 4))) {
         class_2680 st = this.knocker.method_37908().method_8320(p);
         if (st.method_26204() instanceof class_3922 && (Boolean)st.method_11654(class_3922.field_17352)) {
            return true;
         }
      }

      return false;
   }

   private static boolean plausibleNick(String norm, String whole, String word) {
      String w = whole.trim();
      boolean loose = w.startsWith("ты теперь") || w.startsWith("теперь ты") || w.startsWith("ты у меня") || w.startsWith("твое") || w.startsWith("твоё");
      if (!loose) {
         return true;
      } else {
         String[] words = norm.trim().split("\\s+");
         if (words.length != 0 && words[words.length - 1].equals(word)) {
            return !word.matches(".*(ешь|ишь|ете|ите|ется|ится|ть|ться|ла|ли|ло|ый|ий|ой|ая|яя|ое|ее|ые|ие|ся|сь)$");
         } else {
            return false;
         }
      }
   }

   public String handleChat(class_1657 player, String norm) {
      Matcher nick = NICKNAME.matcher(norm);
      if (nick.find() && !norm.contains("не будешь") && !norm.contains("как тебя") && !NOT_NICK.contains(nick.group(1)) && plausibleNick(norm, nick.group(0), nick.group(1))) {
         String name = normName(upper(nick.group(1)));
         KnockerMemoryStore.setFact(player.method_5667(), "his_name", name);
         this.knocker.rememberEvent("она дала мне имя: " + name);
         KnockerAdvancements.grant(player, "named");
         this.knocker.addObsession(3);
         String var10000;
         switch (this.knocker.getPhase()) {
            case WATCHER -> var10000 = "..." + name + "?..";
            case CURIOUS -> var10000 = name + "?.. у меня никогда не было имени...";
            case ATTACHED -> var10000 = name + "... мне нравится. Для тебя я " + name;
            case LOVE -> var10000 = name + "... ты дала мне имя. Я буду носить его всегда";
            case OBSESSION -> var10000 = name + ". Хорошо. Но тогда ты — моя.";
            case PARANOIA -> var10000 = name + ". ТАК МЕНЯ ЗОВЁШЬ ТОЛЬКО ТЫ. НИКТО ДРУГОЙ.";
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         return var10000;
      } else if (!KnockerMessages.containsAny(norm, "расскажи историю", "расскажи о прошлом", "расскажи о своем прошлом", "расскажи о своём прошлом", "как ты умер", "как ты стал", "что с тобой случилось", "расскажи ещё", "что было дальше", "расскажи про себя")) {
         return null;
      } else if (this.knocker.getPhase().ordinal() < KnockerPhase.ATTACHED.ordinal()) {
         return this.pick("...не сейчас", "ты ещё не готова это слышать", "потом... может быть");
      } else if (this.knocker.tickCounter - this.lastStoryTick < 1200) {
         return "дай мне немного времени... это тяжело вспоминать";
      } else {
         int next = this.chapterIndex(player);
         if (next >= CHAPTERS.length) {
            return this.pick("я рассказал тебе всё...", "больше нечего рассказывать. теперь есть только ты.");
         } else if (this.knocker.getPhase().ordinal() < CHAPTER_PHASE[next].ordinal()) {
            return this.pick("дальше... я пока не могу рассказать", "потом. когда ты будешь ближе ко мне");
         } else {
            this.tellNext(player, 30);
            return null;
         }
      }
   }

   public boolean wantsStory(String norm) {
      return KnockerMessages.containsAny(norm, "расскажи историю", "расскажи о прошлом", "расскажи о своем прошлом", "расскажи о своём прошлом", "как ты умер", "как ты стал", "что с тобой случилось", "расскажи ещё", "что было дальше", "расскажи про себя");
   }

   private void tellNext(class_1657 player, int firstDelay) {
      int next = this.chapterIndex(player);
      if (next < CHAPTERS.length && this.knocker.getPhase().ordinal() >= CHAPTER_PHASE[next].ordinal()) {
         this.lastStoryTick = this.knocker.tickCounter;
         int delay = firstDelay;

         for(String line : CHAPTERS[next]) {
            this.knocker.getChat().sayLater(player, line, delay);
            delay = 60 + line.length() * 2;
         }

         KnockerMemoryStore.setFact(player.method_5667(), "story", String.valueOf(next + 1));
         if (next + 1 >= CHAPTERS.length) {
            KnockerAdvancements.grant(player, "his_story");
         }

         KnockerMemoryStore.addDiary(player.method_5667(), List.of("я рассказал ей главу своей истории №" + (next + 1)));
         this.knocker.addObsession(2);
      }
   }

   private int chapterIndex(class_1657 player) {
      try {
         return Integer.parseInt((String)KnockerMemoryStore.facts(player.method_5667()).getOrDefault("story", "0"));
      } catch (NumberFormatException var3) {
         return 0;
      }
   }

   public String dream(class_1657 player) {
      Map<String, String> f = KnockerMemoryStore.facts(player.method_5667());
      boolean dark = this.knocker.getPhase().ordinal() >= KnockerPhase.OBSESSION.ordinal();
      List<String> d = new ArrayList();
      d.add(dark ? "мне снилось, что ты ушла. я нашёл тебя. и ты больше никуда не уходила." : "мне снилось, что мы сидели у реки... и вода меня не пугала");
      if (f.containsKey("flower")) {
         d.add("мне снилось поле из " + (String)f.get("flower") + "... и ты посередине");
      }

      if (f.containsKey("color")) {
         d.add("мне снилось, что всё вокруг было " + (String)f.get("color") + "... как ты любишь");
      }

      if (f.containsKey("fear")) {
         d.add(dark ? "мне снилось, что ты боялась " + (String)f.get("fear") + ". а я стоял рядом и смотрел." : "тебе снилось " + (String)f.get("fear") + "? ты ворочалась... я был рядом");
      }

      if (f.containsKey("dream")) {
         d.add("мне снилось, что твоя мечта сбылась — " + (String)f.get("dream") + ". и я был там");
      }

      if (f.containsKey("name")) {
         String var10001 = (String)f.get("name");
         d.add("я всю ночь повторял твоё имя... " + var10001 + ". " + (String)f.get("name") + ". " + (String)f.get("name") + ".");
      }

      if (f.containsKey("others") && dark) {
         String[] var5 = ((String)f.get("others")).split(",");
         d.add("мне снился " + var5[0] + ". во сне я сделал с ним кое-что. хе-хе.");
      }

      return (String)d.get(this.random.nextInt(d.size()));
   }

   public void scanNames(class_1657 player, String raw) {
      if (this.knocker.getPhase().ordinal() >= KnockerPhase.ATTACHED.ordinal()) {
         String name = findOtherName(raw);
         if (name != null && !NOT_NAMES.contains(name)) {
            String his = (String)KnockerMemoryStore.facts(player.method_5667()).get("his_name");
            if (his == null || !normName(name).equalsIgnoreCase(his)) {
               Map<String, String> f = KnockerMemoryStore.facts(player.method_5667());
               String own = (String)f.getOrDefault("name", "");
               if (own.isEmpty() || !name.toLowerCase().startsWith(own.toLowerCase().substring(0, Math.min(3, own.length())))) {
                  String base = normName(name);
                  String others = (String)f.getOrDefault("others", "");
                  if (!others.contains(base)) {
                     KnockerMemoryStore.setFact(player.method_5667(), "others", others.isEmpty() ? base : others + "," + base);
                     KnockerMemoryStore.addDiary(player.method_5667(), List.of("она говорила о каком-то " + base + ". кто это?"));
                  }

                  this.knocker.rememberEvent("она упомянула " + base);
                  this.knocker.getMood().onJealous();
                  KnockerPhase phase = this.knocker.getPhase();
                  KnockerThoughts var10000 = this.knocker.getThoughts();
                  String var10002;
                  switch (phase) {
                     case ATTACHED -> var10002 = "кто такой " + base + "?..";
                     case LOVE -> var10002 = base + "... кто это? почему она о нём говорит?";
                     case OBSESSION -> var10002 = base + ". ЗАПОМНИТЬ. " + base + ".";
                     default -> var10002 = "НАЙТИ " + base.toUpperCase() + ".";
                  }

                  var10000.show(player, var10002, phase.ordinal() >= KnockerPhase.OBSESSION.ordinal(), 3);
               }
            }
         }
      }
   }

   private static String findOtherName(String raw) {
      Matcher m = OTHER_NAME.matcher(raw);
      if (m.find()) {
         return m.group(1);
      } else {
         Matcher low = Pattern.compile("(?:^|\\s)(?:с|со|у|к|про|о|об|друг|подруга|брат|сестра|парень|девушка)\\s+([а-яё]{3,15})").matcher(raw.toLowerCase());

         while(low.find()) {
            String w = low.group(1);
            String stem2 = w.length() > 4 ? w.substring(0, w.length() - 2) : w;
            String stem1 = w.substring(0, w.length() - 1);

            for(String c : new String[]{w, stem2 + "я", stem2 + "а", stem1 + "а", stem1 + "я", stem2, stem1}) {
               if (COMMON_NAMES.contains(c)) {
                  char var10000 = Character.toUpperCase(c.charAt(0));
                  return var10000 + c.substring(1);
               }
            }
         }

         return null;
      }
   }

   private static String normName(String n) {
      if (n.endsWith("ей") && n.length() > 4) {
         String var2 = n.substring(0, n.length() - 2);
         return var2 + "а";
      } else if (n.endsWith("ой") && n.length() > 4) {
         String var1 = n.substring(0, n.length() - 2);
         return var1 + "а";
      } else if (n.endsWith("ом") && n.length() > 4) {
         return n.substring(0, n.length() - 2);
      } else if (n.endsWith("ем") && n.length() > 4) {
         String var10000 = n.substring(0, n.length() - 2);
         return var10000 + "й";
      } else {
         return n;
      }
   }

   private static String upper(String s) {
      return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
   }

   private String pick(String... lines) {
      return this.knocker.getChat().getBrain().pick(lines);
   }

   static {
      CHAPTER_PHASE = new KnockerPhase[]{KnockerPhase.ATTACHED, KnockerPhase.ATTACHED, KnockerPhase.ATTACHED, KnockerPhase.LOVE, KnockerPhase.LOVE, KnockerPhase.LOVE, KnockerPhase.OBSESSION, KnockerPhase.PARANOIA};
      NICKNAME = Pattern.compile("(?:(?:можно|давай|разреши)\\s+(?:я\\s+)?(?:буду\\s+)?(?:называть|звать|назову)\\s+тебя|можно\\s+тебя\\s+(?:называть|звать|буду называть|буду звать)|я буду (?:называть|звать) тебя|тебя буду (?:называть|звать)|буду тебя (?:называть|звать)|я тебя (?:назову|буду называть|буду звать|называю|зову)|будешь у меня|будешь зваться|тебя теперь зовут|теперь тебя зовут|тебя зовут теперь|назову тебя|буду звать тебя|давай ты будешь|отныне ты|отныне тебя зовут|твоё имя|твое имя|твоё новое имя|твое новое имя|все тебя зовут|всё тебя зовут|ты теперь|теперь ты|ты у меня)\\s+([а-яёa-z]{2,20})");
      OTHER_NAME = Pattern.compile("(?:^|\\s)(?:с|со|у|к|про|о|об|мой друг|моя подруга|мой парень|моя девушка|друг|подруга|брат|сестра)\\s+([А-ЯЁ][а-яё]{2,15})");
      NOT_NICK = Set.of("мой", "моя", "моим", "мое", "моё", "твой", "наш", "один", "одна", "здесь", "тут", "со", "с", "не", "мне", "меня", "тоже", "всегда", "навсегда", "рядом", "дома", "мой друг", "друг", "свободен", "мертв", "мёртв", "живой", "злой", "добрый", "хороший", "плохой", "милый", "странный", "страшный");
      NOT_NAMES = Set.of("Кнокер", "Кнокером", "Кнокеру", "Майнкрафт", "Майн", "Энд", "Незер", "Стив", "Алекс", "Боже", "Блин");
      COMMON_NAMES = Set.of("саша", "женя", "дима", "миша", "максим", "макс", "артем", "артём", "никита", "ваня", "иван", "кирилл", "егор", "денис", "даня", "данил", "даниил", "илья", "леша", "лёша", "алексей", "андрей", "антон", "влад", "владик", "вова", "сергей", "серега", "серёжа", "сережа", "паша", "павел", "рома", "роман", "костя", "олег", "игорь", "тимур", "марат", "руслан", "арсений", "сеня", "глеб", "матвей", "лев", "лева", "лёва", "стас", "вадим", "юра", "коля", "петя", "гоша", "тимофей", "тима", "ярик", "ярослав", "слава", "витя", "вика", "настя", "маша", "даша", "катя", "аня", "лиза", "полина", "соня", "юля", "оля", "лена", "алина", "ксюша", "вероника", "диана", "карина", "кристина", "милана", "ульяна", "варя", "алиса", "ева", "таня", "света", "ира", "наташа", "марина", "яна", "надя", "люба", "валя", "лера", "арина", "вера");
   }
}
