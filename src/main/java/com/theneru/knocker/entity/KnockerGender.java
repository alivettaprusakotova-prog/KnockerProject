package com.theneru.knocker.entity;

import com.theneru.knocker.KnockerMemoryStore;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class KnockerGender {
   private static final Pattern FEM_VERB = Pattern.compile("(?:^|\\s)я\\s+(?:[а-яё]+\\s+)?([а-яё]{2,}(?:ла|лась))(?:\\s|$)");
   private static final Pattern MASC_VERB = Pattern.compile("(?:^|\\s)я\\s+(?:[а-яё]+\\s+)?([а-яё]{2,}(?:л|лся|шёл|шел))(?:\\s|$)");
   private static final Map<String, String> MASC = new HashMap();
   private static final Map<String, String> ADJ = new HashMap();
   private static final Set<String> KEEP;
   private static final Set<String> PERSON_BEFORE;
   private static final Pattern WORD;

   private KnockerGender() {
   }

   public static G get(UUID player) {
      // Это сюжет для девушки: Кнокер всегда говорит с ней в женском роде.
      return KnockerGender.G.FEMALE;
   }

   public static void set(UUID player, G g) {
      if (g != KnockerGender.G.UNKNOWN) {
         KnockerMemoryStore.setGender(player, g.name());
      }

   }

   public static G detect(String norm) {
      String n = " " + norm + " ";
      if (!n.matches(".*\\s(я\\s+)?(девушка|девочка|девчонка|тян|баба|женщина)\\s.*") && !n.matches(".*\\sя\\s+(рада|готова|уверена|одна|сама|должна|счастлива|голодна|устала|влюблена)\\s.*")) {
         if (!n.matches(".*\\s(я\\s+)?(парень|пацан|мальчик|мужик|мужчина|чувак|кун)\\s.*") && !n.matches(".*\\sя\\s+(рад|готов|уверен|один|сам|должен|счастлив|голоден|влюблён|влюблен)\\s.*")) {
            Matcher f = FEM_VERB.matcher(n);
            if (f.find() && !KEEP.contains(f.group(1))) {
               return KnockerGender.G.FEMALE;
            } else {
               Matcher m = MASC_VERB.matcher(n);
               return m.find() && !m.group(1).endsWith("ол") ? KnockerGender.G.MALE : KnockerGender.G.UNKNOWN;
            }
         } else {
            return KnockerGender.G.MALE;
         }
      } else {
         return KnockerGender.G.FEMALE;
      }
   }

   public static G fromAnswer(String norm) {
      String n = " " + norm + " ";
      if (n.matches(".*\\s(девушка|девочка|девчонка|девуля|тян|ж|жен|женский|женщина|она)\\s.*")) {
         return KnockerGender.G.FEMALE;
      } else {
         return n.matches(".*\\s(парень|пацан|мальчик|мужик|м|муж|мужской|мужчина|он|кун)\\s.*") ? KnockerGender.G.MALE : detect(norm);
      }
   }

   public static String toMale(String text) {
      Matcher m = WORD.matcher(text);
      StringBuilder out = new StringBuilder();

      String low;
      for(String prev = ""; m.find(); prev = low) {
         String w = m.group();
         low = w.toLowerCase();
         String rep = (String)MASC.get(low);
         if (rep == null && ADJ.containsKey(low)) {
            String after = text.substring(m.end()).trim().toLowerCase();
            boolean endOfPhrase = after.isEmpty() || after.matches("^[.!?,…)—-].*") || after.matches("^(навсегда|и|теперь|только|ведь|вся|целиком|когда|со|с|для|мне|ему|тебе)(\\s.*|[.!?,…].*|$)");
            if (endOfPhrase || PERSON_BEFORE.contains(prev)) {
               rep = (String)ADJ.get(low);
            }
         }

         if (rep == null && !KEEP.contains(low) && !ADJ.containsKey(low) && low.length() >= 4) {
            if (low.endsWith("лась")) {
               String var10000 = low.substring(0, low.length() - 4);
               rep = var10000 + "лся";
            } else if (low.endsWith("ла") && isVerbLike(low)) {
               String var9 = low.substring(0, low.length() - 2);
               rep = var9 + "л";
            }
         }

         m.appendReplacement(out, Matcher.quoteReplacement(rep == null ? w : matchCase(w, rep)));
      }

      m.appendTail(out);
      return out.toString().replace("с ней", "с ним").replace("о ней", "о нём").replace("к ней", "к нему").replace("С НЕЙ", "С НИМ").replace("О НЕЙ", "О НЁМ");
   }

   private static boolean isVerbLike(String w) {
      char before = w.charAt(w.length() - 3);
      return "аеиоуяыюё".indexOf(before) >= 0 || w.endsWith("шла") || w.endsWith("зла") || w.endsWith("сла");
   }

   private static String matchCase(String original, String rep) {
      if (original.equals(original.toUpperCase()) && original.length() > 1) {
         return rep.toUpperCase();
      } else if (Character.isUpperCase(original.charAt(0))) {
         char var10000 = Character.toUpperCase(rep.charAt(0));
         return var10000 + rep.substring(1);
      } else {
         return rep;
      }
   }

   public static String adapt(String text, UUID player) {
      // Автоматической смены рода нет: игрок — девушка, текст остаётся женским.
      return text;
   }

   public static String aiRule(UUID player) {
      return "Кнокер — парень: говори о себе ТОЛЬКО в мужском роде («я пришёл», «я хочу», «я твой», "
            + "«я буду твоим мужем»). Игрок — девушка: обращайся к ней СТРОГО на «ты» («ты», «тебя», "
            + "«тебе», «тобой», «моя»). Никогда не говори о ней в третьем лице («она», «её», «ей») "
            + "в прямой речи. Слова «она/её» в заметках памяти — не повод менять местоимения в диалоге.";
   }

   static {
      String[][] always = new String[][]{{"она", "он"}, {"её", "его"}, {"ее", "его"}, {"ей", "ему"}, {"неё", "него"}, {"нее", "него"}, {"вышла", "вышел"}, {"нашла", "нашёл"}, {"пришла", "пришёл"}, {"прошла", "прошёл"}, {"ушла", "ушёл"}, {"зашла", "зашёл"}, {"шла", "шёл"}, {"замёрзла", "замёрз"}, {"смогла", "смог"}, {"могла", "мог"}, {"легла", "лёг"}, {"выбралась", "выбрался"}, {"убила", "убил"}, {"должница", "должник"}};

      for(String[] p : always) {
         MASC.put(p[0], p[1]);
      }

      String[][] adj = new String[][]{{"сама", "сам"}, {"одна", "один"}, {"рада", "рад"}, {"готова", "готов"}, {"уверена", "уверен"}, {"должна", "должен"}, {"нужна", "нужен"}, {"одинока", "одинок"}, {"влюблена", "влюблён"}, {"зла", "зол"}, {"голодная", "голодный"}, {"добрая", "добрый"}, {"заботливая", "заботливый"}, {"интересная", "интересный"}, {"красивая", "красивый"}, {"любимая", "любимый"}, {"милая", "милый"}, {"нежная", "нежный"}, {"особенная", "особенный"}, {"странная", "странный"}, {"хорошая", "хороший"}, {"глупая", "глупый"}, {"смешная", "смешной"}, {"живая", "живой"}, {"тёплая", "тёплый"}, {"теплая", "теплый"}, {"такая", "такой"}, {"моя", "мой"}, {"твоя", "твой"}, {"сильной", "сильным"}, {"красивой", "красивым"}};

      for(String[] p : adj) {
         ADJ.put(p[0], p[1]);
      }

      KEEP = Set.of("дела", "сначала", "подвала", "стола", "игла", "мгла", "пола", "села", "скала", "смела", "умница", "скачалась", "прервалась", "удалась", "началась", "мила", "сала", "бала", "выла", "пробела", "сила", "могила", "пчела", "стрела", "смола", "зола", "мыла", "пила", "вилла");
      PERSON_BEFORE = Set.of("ты", "такая", "такой", "очень", "же", "ведь", "вся", "была", "был", "так", "слишком", "совсем", "всегда", "теперь", "только");
      WORD = Pattern.compile("[А-Яа-яЁё]+");
   }

   public static enum G {
      UNKNOWN,
      FEMALE,
      MALE;

      // $FF: synthetic method
      private static G[] $values() {
         return new G[]{UNKNOWN, FEMALE, MALE};
      }
   }
}
