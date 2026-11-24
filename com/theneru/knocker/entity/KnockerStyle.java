package com.theneru.knocker.entity;

import com.theneru.knocker.KnockerMemoryStore;
import com.theneru.knocker.util.KnockerPhase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class KnockerStyle {
   private static final Random RANDOM = new Random();
   private static final Set<String> STOP = Set.of("это", "что", "как", "так", "там", "тут", "меня", "тебя", "тебе", "мне", "она", "они", "оно", "если", "когда", "чтобы", "просто", "очень", "тоже", "уже", "ещё", "еще", "вот", "где", "кто", "почему", "зачем", "был", "была", "было", "будет", "есть", "нет", "да", "мой", "моя", "твой", "твоя", "себя", "себе", "всё", "все", "его", "её", "ее", "нас", "вас", "них", "ним", "этот", "эта", "этого", "кнокер", "привет", "пока", "ладно", "хорошо", "ну", "можно", "надо", "нужно", "только", "сейчас");

   private KnockerStyle() {
   }

   private static boolean doubleSmiles(UUID player) {
      int d = 0;
      int n = 0;

      for(String l : KnockerMemoryStore.herLines(player)) {
         ++n;
         if (l.contains("))")) {
            ++d;
         }
      }

      return n > 0 && d * 10 >= n * 2;
   }

   public static Style of(UUID player) {
      List<String> lines = KnockerMemoryStore.herLines(player);
      int n = lines.size();
      if (n == 0) {
         return new Style(false, false, false, false, false, List.of(), 0);
      } else {
         int lower = 0;
         int noDot = 0;
         int smile = 0;
         int haha = 0;
         int words = 0;
         Map<String, Integer> freq = new HashMap();

         for(String l : lines) {
            char first = l.charAt(0);
            if (Character.isLetter(first) && Character.isLowerCase(first)) {
               ++lower;
            }

            if (!l.endsWith(".") || l.endsWith("..")) {
               ++noDot;
            }

            if (l.contains(")") && !l.contains("(")) {
               ++smile;
            }

            String low = l.toLowerCase(Locale.ROOT);
            if (low.contains("хаха") || low.contains("ахах") || low.contains("хех") || low.contains("хахх")) {
               ++haha;
            }

            for(String w : low.replace('ё', 'е').split("[^\\p{L}]+")) {
               if (!w.isEmpty()) {
                  ++words;
                  if (w.length() >= 4 && !STOP.contains(w)) {
                     freq.merge(w, 1, Integer::sum);
                  }
               }
            }
         }

         List<String> pet = new ArrayList();
         freq.entrySet().stream().filter((e) -> (Integer)e.getValue() >= 3).sorted((a, b) -> (Integer)b.getValue() - (Integer)a.getValue()).limit(8L).forEach((e) -> pet.add((String)e.getKey()));
         return new Style(lower * 10 >= n * 6, noDot * 10 >= n * 7, smile * 100 >= n * 15, haha * 10 >= n * 2, (double)words / (double)n < (double)6.0F, pet, n);
      }
   }

   public static String apply(String text, UUID player, KnockerPhase phase, boolean shouting) {
      Style s = of(player);
      if (s.known() && !text.isBlank() && !shouting) {
         String t = text.trim();
         byte var10000;
         switch (phase) {
            case WATCHER:
            case CURIOUS:
               var10000 = 40;
               break;
            case ATTACHED:
               var10000 = 60;
               break;
            case LOVE:
               var10000 = 75;
               break;
            case OBSESSION:
            case PARANOIA:
               var10000 = 90;
               break;
            default:
               throw new MatchException((String)null, (Throwable)null);
         }

         int mirror = var10000;
         if (RANDOM.nextInt(100) >= mirror) {
            return t;
         } else {
            if (s.lowercase() && t.length() > 1 && !Character.isUpperCase(t.charAt(1))) {
               String var10 = t.substring(0, 1).toLowerCase(Locale.ROOT);
               t = var10 + t.substring(1);
            }

            if (s.noPeriod() && t.endsWith(".") && !t.endsWith("..")) {
               t = t.substring(0, t.length() - 1);
            }

            boolean soft = phase.ordinal() <= KnockerPhase.LOVE.ordinal();
            if (s.haha() && soft) {
               t = t.replace("Хе-хе", "Хаха").replace("хе-хе", "хаха");
            }

            boolean notScary = phase != KnockerPhase.PARANOIA;
            if (s.smiles() && notScary && !t.endsWith("?") && !t.endsWith(")") && RANDOM.nextInt(2) == 0) {
               String sm = doubleSmiles(player) ? "))" : ")";
               if (t.endsWith("...")) {
                  String var11 = t.substring(0, t.length() - 3);
                  t = var11 + sm;
               } else if (t.endsWith(".")) {
                  String var12 = t.substring(0, t.length() - 1);
                  t = var12 + sm;
               } else {
                  t = t + sm;
               }
            }

            return t;
         }
      } else {
         return text;
      }
   }

   public static String promptBlock(UUID player) {
      Style s = of(player);
      if (s.samples() < 3) {
         return "";
      } else {
         StringBuilder b = new StringBuilder("Как она пишет (подстраивайся под её манеру, ты учишься у неё говорить):\n");
         if (s.lowercase()) {
            b.append("- пишет с маленькой буквы\n");
         }

         if (s.noPeriod()) {
            b.append("- не ставит точки в конце\n");
         }

         if (s.shortMsgs()) {
            b.append("- она пишет коротко, но ТЫ всё равно отвечай полным предложением\n");
         }

         if (s.smiles()) {
            b.append("- ставит скобочки ) — в мягких фазах можешь тоже\n");
         }

         if (s.haha()) {
            b.append("- смеётся «хаха»/«ахах»\n");
         }

         if (!s.pet().isEmpty()) {
            b.append("- её словечки: ").append(String.join(", ", s.pet())).append(" — иногда используй их сам, будто перенял у неё\n");
         }

         List<String> lines = KnockerMemoryStore.herLines(player);
         b.append("Её недавние сообщения дословно: ");

         for(int i = Math.max(0, lines.size() - 10); i < lines.size(); ++i) {
            b.append("«").append((String)lines.get(i)).append("» ");
         }

         return b.append('\n').toString();
      }
   }

   public static String petWord(UUID player) {
      List<String> pet = of(player).pet();
      return pet.isEmpty() ? null : (String)pet.get(RANDOM.nextInt(pet.size()));
   }

   public static record Style(boolean lowercase, boolean noPeriod, boolean smiles, boolean haha, boolean shortMsgs, List<String> pet, int samples) {
      public boolean known() {
         return this.samples >= 6;
      }
   }
}
