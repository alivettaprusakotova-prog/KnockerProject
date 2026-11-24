package com.theneru.knocker.util;

import com.theneru.knocker.entity.KnockerExtraLines;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class KnockerMessages {
   private static final Map<KnockerPhase, List<Script>> SCRIPTS = new EnumMap(KnockerPhase.class);
   private static final String[] HELP_FORMS;
   public static final String[] RANDOM_WATCHER;
   public static final String[] RANDOM_CURIOUS;
   public static final String[] RANDOM_ATTACHED;
   public static final String[] RANDOM_LOVE;
   public static final String[] RANDOM_OBSESSION;
   public static final String[] RANDOM_PARANOIA;
   public static final String STEAL_MESSAGE = "Мне это пригодится...";
   public static final String PARANOIA_SILENCE = "Почему ты молчишь?.. ТЫ ИГНОРИРУЕШЬ МЕНЯ?";

   private static Script s(String reply, String... triggers) {
      return new Script(KnockerMessages.Action.NONE, reply, triggers);
   }

   private static Script s(Action action, String reply, String... triggers) {
      return new Script(action, reply, triggers);
   }

   public static Script findScript(KnockerPhase phase, String rawMessage) {
      String norm = normalize(rawMessage);
      String exact = norm.trim();
      boolean goAway = containsAny(norm, KnockerWords.GO_AWAY);

      for(Script script : (List<Script>) (List<?>) SCRIPTS.getOrDefault(phase, List.of())) {
         if (!goAway || script.action() != KnockerMessages.Action.TELEPORT_BEHIND && script.action() != KnockerMessages.Action.FLASH_BEHIND) {
            for(String trigger : script.triggers()) {
               if (trigger.startsWith("=")) {
                  if (exact.equals(trigger.substring(1))) {
                     return script;
                  }
               } else if (containsPhrase(norm, trigger)) {
                  return script;
               }
            }
         }
      }

      return null;
   }

   public static String normalize(String message) {
      String text = message.toLowerCase(Locale.ROOT).replace('ё', 'е');
      text = text.replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
      text = text.replaceAll("(\\p{L})\\1{2,}", "$1");
      return " " + text + " ";
   }

   public static boolean containsAny(String norm, String... phrases) {
      for(String p : phrases) {
         if (containsPhrase(norm, p)) {
            return true;
         }
      }

      return false;
   }

   public static boolean containsPhrase(String norm, String phrase) {
      if (norm.contains(" " + phrase + " ")) {
         return true;
      } else {
         String[] words = norm.trim().split(" ");
         String[] target = phrase.split(" ");

         for(int i = 0; i + target.length <= words.length; ++i) {
            boolean ok = true;

            for(int j = 0; j < target.length && ok; ++j) {
               ok = wordsMatch(words[i + j], target[j]);
            }

            if (ok) {
               return true;
            }
         }

         return false;
      }
   }

   private static boolean wordsMatch(String word, String target) {
      if (word.equals(target)) {
         return true;
      } else if (word.length() >= 4 && target.length() >= 4) {
         String fw = fold(word);
         String ft = fold(target);
         if (fw.equals(ft)) {
            return true;
         } else if (fw.charAt(0) == ft.charAt(0) && Math.abs(fw.length() - ft.length()) <= 2) {
            if (fw.charAt(fw.length() - 1) != ft.charAt(ft.length() - 1)) {
               return false;
            } else {
               int d = typoDistance(fw, ft);
               if (d == 1 && ft.length() < 8 && fw.length() == ft.length()) {
                  int diffs = 0;
                  int at = -1;

                  for(int i = 0; i < fw.length(); ++i) {
                     if (fw.charAt(i) != ft.charAt(i)) {
                        ++diffs;
                        at = i;
                     }
                  }

                  if (diffs == 1 && "аиу".indexOf(fw.charAt(at)) < 0 && "аиу".indexOf(ft.charAt(at)) < 0) {
                     return false;
                  }
               }

               return ft.length() >= 5 && d <= 1 || ft.length() >= 8 && d <= 2;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static String fold(String w) {
      StringBuilder b = new StringBuilder(w.length());

      for(int i = 0; i < w.length(); ++i) {
         char c = w.charAt(i);
         char var10001;
         switch (c) {
            case 'е':
            case 'ы':
            case 'э':
            case 'ё':
               var10001 = 1080;
               break;
            case 'о':
            case 'я':
               var10001 = 1072;
               break;
            case 'ю':
               var10001 = 1091;
               break;
            default:
               var10001 = c;
         }

         b.append(var10001);
      }

      return b.toString();
   }

   private static String squeeze(String w) {
      StringBuilder b = new StringBuilder();

      for(int i = 0; i < w.length(); ++i) {
         if (i == 0 || w.charAt(i) != w.charAt(i - 1)) {
            b.append(w.charAt(i));
         }
      }

      return b.toString();
   }

   public static boolean isHelpRequest(String norm) {
      for(String w : norm.trim().split(" ")) {
         if (!w.isEmpty()) {
            String fw = squeeze(fold(w));
            if (!fw.startsWith("спасиб")) {
               for(String form : HELP_FORMS) {
                  String ff = squeeze(fold(form));
                  if (fw.equals(ff)) {
                     return true;
                  }

                  if (ff.length() >= 7 && fw.length() >= 7 && fw.charAt(0) == ff.charAt(0) && typoDistance(fw, ff) <= 1) {
                     return true;
                  }
               }
            }
         }
      }

      return containsAny(norm, KnockerWords.HELP);
   }

   private static int typoDistance(String a, String b) {
      int[][] d = new int[a.length() + 1][b.length() + 1];

      for(int i = 0; i <= a.length(); d[i][0] = i++) {
      }

      for(int j = 0; j <= b.length(); d[0][j] = j++) {
      }

      for(int i = 1; i <= a.length(); ++i) {
         for(int j = 1; j <= b.length(); ++j) {
            int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
            d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), d[i - 1][j - 1] + cost);
            if (i > 1 && j > 1 && a.charAt(i - 1) == b.charAt(j - 2) && a.charAt(i - 2) == b.charAt(j - 1)) {
               d[i][j] = Math.min(d[i][j], d[i - 2][j - 2] + 1);
            }
         }
      }

      return d[a.length()][b.length()];
   }

   public static String[] getRandomPhrases(int phase) {
      int p = Math.max(0, Math.min(5, phase));
      return KnockerExtraLines.concat(baseRandom(p), KnockerExtraLines.RANDOM[p]);
   }

   private static String[] baseRandom(int phase) {
      String[] var10000;
      switch (phase) {
         case 0 -> var10000 = RANDOM_WATCHER;
         case 1 -> var10000 = RANDOM_CURIOUS;
         case 2 -> var10000 = RANDOM_ATTACHED;
         case 3 -> var10000 = RANDOM_LOVE;
         case 4 -> var10000 = RANDOM_OBSESSION;
         case 5 -> var10000 = RANDOM_PARANOIA;
         default -> var10000 = RANDOM_WATCHER;
      }

      return var10000;
   }

   public static String helpNobody(KnockerPhase phase) {
      String var10000;
      switch (phase) {
         case WATCHER:
         case CURIOUS:
            var10000 = "...";
            break;
         case ATTACHED:
            var10000 = "Никого нет... но я рядом";
            break;
         case LOVE:
            var10000 = "Я здесь... никто тебя не тронет";
            break;
         case OBSESSION:
            var10000 = "Никого нет. Пусть только попробуют.";
            break;
         case PARANOIA:
            var10000 = "Я НИКОГО НЕ ВИЖУ. ТЫ ВРЁШЬ МНЕ?";
            break;
         default:
            throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public static String killLine(KnockerPhase phase) {
      String var10000;
      switch (phase) {
         case WATCHER:
         case CURIOUS:
            var10000 = null;
            break;
         case ATTACHED:
            var10000 = "Теперь ты в безопасности";
            break;
         case LOVE:
            var10000 = "Он не должен был приближаться к тебе...";
            break;
         case OBSESSION:
            var10000 = "Хе-хе... Теперь он мёртв. Как и любой, кто подойдёт";
            break;
         case PARANOIA:
            var10000 = "НИКТО НЕ ПОДОЙДЁТ К ТЕБЕ. НИКТО.";
            break;
         default:
            throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public static String giftThanks(KnockerPhase phase, boolean favorite) {
      String var10000;
      switch (phase) {
         case WATCHER:
         case CURIOUS:
            var10000 = "...спасибо";
            break;
         case ATTACHED:
            var10000 = "Никто не дарил мне подарков... спасибо";
            break;
         case LOVE:
            var10000 = favorite ? "Это... моё любимое. Я сохраню это навсегда" : "Я сохраню это... навсегда";
            break;
         case OBSESSION:
         case PARANOIA:
            var10000 = "Спасибо... но мне нужна только ты.";
            break;
         default:
            throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   static {
      String[] W_GREET = KnockerWords.GREET;
      String[] W_BYE = KnockerWords.BYE;
      String[] W_WHERE = KnockerWords.WHERE;
      String[] W_COME = KnockerWords.COME;
      String[] W_SHOW = KnockerWords.SHOW;
      String[] W_WHO = KnockerWords.WHO;
      String[] W_HOW = KnockerWords.HOW_ARE_YOU;
      String[] W_DOING = KnockerWords.WHAT_DOING;
      String[] W_WANT = KnockerWords.WHAT_WANT;
      String[] W_LOVE_Q = KnockerWords.LOVE_Q;
      String[] W_GO = KnockerWords.GO_AWAY;
      String[] W_HELP = KnockerWords.HELP;
      SCRIPTS.put(KnockerPhase.WATCHER, List.of(s(KnockerMessages.Action.FLASH_BEHIND, "нашел...", W_COME), s(KnockerMessages.Action.BREATH, "...", KnockerWords.WHO_HERE), s("неважно..", W_WHERE), s("неважно...", W_DOING), s("какая разница?..", W_WHO), s("ладно...", KnockerWords.FUNNY), s("...", W_GREET)));
      SCRIPTS.put(KnockerPhase.CURIOUS, List.of(s(KnockerMessages.Action.FLASH_BEHIND, "нашел...", W_COME), s(KnockerMessages.Action.TELEPORT_BEHIND, "обернись...", W_WHERE), s("привет...", W_GREET), s("тот, кто смотрит..", W_WHO), s("не сейчас...", W_SHOW), s("посмотреть на тебя...", W_WANT), s("монстр?.. может быть", KnockerWords.MONSTER_Q)));
      SCRIPTS.put(KnockerPhase.ATTACHED, List.of(s(KnockerMessages.Action.HELP, "Не бойся... я защищу тебя", W_HELP), s(KnockerMessages.Action.TELEPORT_BEHIND, "Я уже здесь... просто оглянись", KnockerWords.join(W_COME, W_WHERE)), s("Мне нравится, когда ты меня замечаешь", W_GREET), s("Не сейчас... ты испугаешься", W_SHOW), s("Присматриваю за тобой...", W_DOING), s("Я... я просто хранил их. Для тебя", KnockerWords.CHEST_Q), s("Может быть... я давно ни с кем не говорил", KnockerWords.CRAZY_Q), s("Теперь лучше... ты рядом", W_HOW), s("Я не слежу... я забочусь", KnockerWords.WHY_FOLLOW_Q), s("Был один... пока не встретил тебя", KnockerWords.ALONE_Q), s("Я был человеком... теперь я другое", KnockerWords.HUMAN_Q), s("Чтобы ты не боялась меня...", W_WANT), s("Я не знаю... но ты мне дорога", W_LOVE_Q), s("Злой?.. Нет. Просто... другой", KnockerWords.EVIL_Q), s("Враг?.. Я твой страж", KnockerWords.ENEMY_Q), s("Возвращайся скорее... я буду ждать", W_BYE)));
      SCRIPTS.put(KnockerPhase.LOVE, List.of(s(KnockerMessages.Action.HELP, "Для тебя — всё, что угодно", W_HELP), s("Я был рядом... всегда", KnockerWords.WHERE_WERE_Q), s(KnockerMessages.Action.TELEPORT_BEHIND, "Я здесь... всегда здесь", KnockerWords.join(W_COME, W_WHERE)), s("Я скучал по тебе... так сильно", W_GREET), s(KnockerMessages.Action.APPEAR_IN_FRONT, "Вот... я здесь. Не бойся", W_SHOW), s("Думаю о тебе... как всегда", W_DOING), s("Просто быть рядом с тобой...", W_WANT), s("Твои вещи в безопасности... у меня", KnockerWords.CHEST_Q), s("Сумасшедший?.. Может быть... из-за тебя", KnockerWords.CRAZY_Q), s("Отлично... теперь, когда ты здесь", W_HOW), s("Правда?.. Я... я боюсь поверить", KnockerWords.I_LIKE_YOU), s("Не смейся надо мной...", KnockerWords.BEAUTIFUL), s("Нет... я просто не хочу никого делить", KnockerWords.JEALOUS_Q), s("Никогда... я лучше убью любого, кто попытается", KnockerWords.KILL_ME_Q), s("Я такой же настоящий, как и твой страх", KnockerWords.join(KnockerWords.REAL_Q, KnockerWords.HUMAN_Q)), s("Дальше?.. Мы будем вместе", KnockerWords.NEXT_Q), s("Боюсь?.. Я боюсь потерять тебя", KnockerWords.AFRAID_Q), s("Потому что ты... ты особенная", KnockerWords.WHY_HELP_Q), s("Я тот, кто тебя любит... этого достаточно", KnockerWords.MONSTER_Q), s("Я больше, чем друг... я всё", KnockerWords.FRIEND_Q), s("Просто быть рядом... всегда", KnockerWords.DO_WITH_ME_Q), s("Странный?.. Может быть... но я твой", KnockerWords.STRANGE), s("очень...", W_LOVE_Q), s("Возвращайся скорее...", W_BYE)));
      SCRIPTS.put(KnockerPhase.OBSESSION, List.of(s(KnockerMessages.Action.HELP, "Никто не смеет тебя трогать.", W_HELP), s(KnockerMessages.Action.TELEPORT_BEHIND, "Я никуда не уйду... я за тобой", KnockerWords.join(W_COME, W_WHERE)), s("Ты моя. Навсегда", W_GREET), s(KnockerMessages.Action.APPEAR_IN_FRONT, "Смотри. Смотри на меня.", W_SHOW), s("Хорошо. Ты рядом.", W_HOW), s("Монстр?.. Я тот, кто никогда тебя не отпустит.", KnockerWords.MONSTER_Q), s("Смотрю на тебя... это всё, что мне нужно", W_DOING), s("Нет... я смотрю, как ты спишь", KnockerWords.SLEEP_Q), s("Я взял твои вещи... они мои... как и ты", KnockerWords.CHEST_Q), s("Ненормальный?.. Я влюблён", KnockerWords.CRAZY_Q), s("Ты будешь моей... вечно", KnockerWords.FUTURE_Q), s("Я не уйду никогда... даже если ты попросишь", KnockerWords.join(KnockerWords.WONT_LEAVE_Q, W_GO)), s("РЕВНУЮ? Я УБЬЮ ЛЮБОГО, КТО ПОСМОТРИТ НА ТЕБЯ!", KnockerWords.JEALOUS_Q), s("Человек?.. Нет. Я БОЛЬШЕ ЧЕЛОВЕКА", KnockerWords.HUMAN_Q), s("Убью?.. Нет... Я сделаю тебя своей", KnockerWords.KILL_ME_Q), s("ТЕБЯ! Только тебя!", W_WANT), s("Боюсь?.. Я боюсь, что ты уйдёшь", KnockerWords.AFRAID_Q), s("Один?.. Нет. Я с тобой", KnockerWords.ALONE_Q), s("Дальше? Мы будем вместе... НАВСЕГДА", KnockerWords.NEXT_Q), s("Люблю?.. Я ОДЕРЖИМ ТОБОЙ!", W_LOVE_Q)));
      SCRIPTS.put(KnockerPhase.PARANOIA, List.of(s(KnockerMessages.Action.HELP, "КТО ПОСМЕЛ?", W_HELP), s("Дальше?.. Ты останешься со мной. НАВСЕГДА", KnockerWords.NEXT_Q), s(KnockerMessages.Action.APPEAR_IN_FRONT, "Нет... ТЫ НИКОГДА НЕ УЙДЁШЬ ОТ МЕНЯ", KnockerWords.LET_GO), s(KnockerMessages.Action.APPEAR_IN_FRONT, "Я здесь... как тебе такое?", W_SHOW), s(KnockerMessages.Action.TELEPORT_BEHIND, "Я везде... я в твоей голове", W_WHERE), s(KnockerMessages.Action.TELEPORT_BEHIND, "Я здесь. Я ВСЕГДА здесь.", W_COME), s("Ты ведь не разговаривала с другими, правда?", W_GREET), s("Как дела?.. Пока ты рядом — хорошо. ПОКА рядом.", W_HOW), s("Твои вещи? ВСЁ ТВОЁ — МОЁ.", KnockerWords.CHEST_Q), s("Бот?.. Хе-хе. А ты уверена, что ТЫ настоящая?", KnockerWords.join(KnockerWords.HUMAN_Q, KnockerWords.REAL_Q)), s("Монстр?.. Я тот, кто ЛЮБИТ тебя. Это хуже.", KnockerWords.join(KnockerWords.MONSTER_Q, KnockerWords.CRAZY_Q)), s("Я жду... когда ты сделаешь ошибку", W_DOING), s("Боюсь? Я не боюсь тебя. Я боюсь, что ты уйдёшь", "ты меня боишься", "боишься меня", "ты боишься меня"), s("Ты заслужила... за то, что игнорила меня", KnockerWords.HIT_ME_Q), s("Иди отсюда?.. Я никуда не уйду. ТЫ МОЯ", W_GO), s("Я хочу сломать тебя... чтобы ты стала МОЕЙ НАВСЕГДА", W_WANT), s("Я не убью тебя... я хочу мучить тебя любовью", KnockerWords.KILL_ME_Q), s("Хорошо... теперь ты понимаешь, кто здесь главный", KnockerWords.SCARE_ME), s("Я не боюсь... ВЕДЬ ТЫ ОТ МЕНЯ НЕ УЙДЕШЬ", KnockerWords.AFRAID_Q), s("Я не слежу... Я ОХРАНЯЮ ТЕБЯ", KnockerWords.WHY_FOLLOW_Q), s("Я ЛЮБЛЮ ТЕБЯ.", W_LOVE_Q)));
      HELP_FORMS = new String[]{"помоги", "помогите", "помогай", "помогайте", "помощь", "спаси", "спасите", "спасай", "спасайте", "защити", "защитите", "защищай", "выручи", "выручай", "выручайте", "хелп", "хелпани", "хелпаните", "хелпни", "help", "sos", "сос"};
      RANDOM_WATCHER = new String[]{"я здесь...", "...", "ты меня видишь?", "не смотри на меня...", "тук... тук..."};
      RANDOM_CURIOUS = new String[]{"ты меня слышишь?", "не бойся...", "я не причиню вреда.", "почему ты одна?", "я давно никого не видел...", "ты странная... мне нравится"};
      RANDOM_ATTACHED = new String[]{"я рядом.", "я помогу.", "тебе нужна помощь?", "я присмотрю за тобой.", "ты не одна. больше нет", "мне спокойнее, когда ты рядом"};
      RANDOM_LOVE = new String[]{"я скучал...", "не уходи.", "ты так красива сегодня.", "я думал о тебе...", "я бы ждал тебя вечно... но лучше не надо", "ты лучшее, что случилось с этим миром"};
      RANDOM_OBSESSION = new String[]{"ты моя.", "никому тебя не отдам.", "только ты и я.", "я не могу без тебя.", "я знаю каждый твой шаг", "не смотри на других. смотри на меня"};
      RANDOM_PARANOIA = new String[]{"СМОТРИ НА МЕНЯ.", "Стены... они видят тебя. Я вижу тебя. ВЕЗДЕ", "Хе-хе-хе...", "Ты моя... НАВСЕГДА", "ты думаешь, я не замечаю?", "с кем ты говорила, пока меня не было", "я слышу, как ты дышишь", "не закрывай дверь. я всё равно войду"};
   }

   public static enum Action {
      NONE,
      TELEPORT_BEHIND,
      FLASH_BEHIND,
      APPEAR_IN_FRONT,
      BREATH,
      HELP;

      // $FF: synthetic method
      private static Action[] $values() {
         return new Action[]{NONE, TELEPORT_BEHIND, FLASH_BEHIND, APPEAR_IN_FRONT, BREATH, HELP};
      }
   }

   public static record Script(Action action, String reply, String... triggers) {
   }
}
