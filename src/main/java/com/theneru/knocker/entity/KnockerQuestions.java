package com.theneru.knocker.entity;

import com.theneru.knocker.KnockerConfig;
import com.theneru.knocker.KnockerMemoryStore;
import com.theneru.knocker.util.KnockerMessages;
import com.theneru.knocker.util.KnockerPhase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.class_1657;

public class KnockerQuestions {
   private static final Q[] QUESTIONS;
   private static final Set<String> FILLER;
   private static final String[][] ACK;
   private static final String[] REFUSED;
   private final KnockerEntity knocker;
   private final Random random = new Random();
   private Q pending = null;
   private int pendingUntil = 0;
   private int nextAskTick = -1;

   public KnockerQuestions(KnockerEntity knocker) {
      this.knocker = knocker;
   }

   public void tick(class_1657 player) {
      int now = this.knocker.tickCounter;
      KnockerPhase phase = this.knocker.getPhase();
      boolean askGender = KnockerGender.get(player.method_5667()) == KnockerGender.G.UNKNOWN && this.knocker.getTicksSinceTalk() < 2400;
      if (phase.ordinal() >= KnockerPhase.CURIOUS.ordinal() || askGender) {
         if (this.pending != null && now > this.pendingUntil) {
            this.pending = null;
         }

         if (this.nextAskTick < 0) {
            this.nextAskTick = now + (askGender ? 400 : KnockerConfig.ticksBetween((double)1.0F, (double)3.0F));
         }

         if (now >= this.nextAskTick && this.pending == null) {
            if (!(this.knocker.method_5739(player) > 16.0F) && this.knocker.getChat().canSpeakUnprompted(600)) {
               this.nextAskTick = now + KnockerConfig.ticksBetween(KnockerConfig.questionMin, KnockerConfig.questionMax);
               Q q = this.nextQuestion(player, phase);
               if (q != null) {
                  this.pending = q;
                  this.pendingUntil = now + 1800;
                  this.knocker.getChat().say(player, this.knocker.getChat().getBrain().pick(q.ask()));
               }
            }
         }
      }
   }

   private Q nextQuestion(class_1657 player, KnockerPhase phase) {
      Map<String, String> known = KnockerMemoryStore.facts(player.method_5667());
      List<Q> open = new ArrayList();

      for(Q q : QUESTIONS) {
         if (q.key().equals("gender") || phase.ordinal() >= q.minPhase().ordinal()) {
            if (q.key().equals("gender")) {
               if (KnockerGender.get(player.method_5667()) == KnockerGender.G.UNKNOWN) {
                  return q;
               }
            } else if ((!q.key().equals("name") || this.knocker.getChat().getBrain().getPlayerName() == null) && !known.containsKey(q.key())) {
               open.add(q);
            }
         }
      }

      return open.isEmpty() ? null : (Q)open.get(this.random.nextInt(open.size()));
   }

   public String onMessage(class_1657 player, String raw) {
      if (this.pending != null && this.knocker.tickCounter <= this.pendingUntil) {
         String norm = KnockerMessages.normalize(raw).trim();
         if (!raw.trim().endsWith("?") && norm.split(" ").length <= 12) {
            if (!this.pending.key().equals("gender") && norm.matches("^(не\\s+)?(смотри|покажи|подойди|иди|стой|отойди|уйди|обними|давай|скажи|расскажи|возьми|дай|отдай|принеси|помоги|прячься|сядь|подержи|зайди|проводи|пошли|пойдем|пойдём|отстань|хватит)( .*)?$")) {
               return null;
            } else {
               Q q = this.pending;
               this.pending = null;
               int p = this.knocker.getPhase().ordinal();
               if (norm.matches("^(не скажу|не хочу|не твое дело|не твоё дело|секрет|отстань|потом|нет|не знаю|хз|незнаю)( .*)?$")) {
                  return REFUSED[p];
               } else if (q.key().equals("gender")) {
                  KnockerGender.G g = KnockerGender.fromAnswer(norm);
                  if (g == KnockerGender.G.UNKNOWN) {
                     return "...ладно. потом скажешь";
                  } else {
                     KnockerGender.set(player.method_5667(), g);
                     return g == KnockerGender.G.FEMALE ? "девушка... хорошо. я запомню" : "парень... хорошо. я запомню";
                  }
               } else {
                  String answer = extract(norm);
                  if (answer.isEmpty()) {
                     return REFUSED[p];
                  } else {
                     String nickReply = null;
                     if (q.key().equals("name")) {
                        char var10000 = Character.toUpperCase(answer.charAt(0));
                        String name = var10000 + answer.substring(1);
                        this.knocker.getChat().getBrain().setPlayerName(name);
                        answer = name;
                        nickReply = this.nickCheck(player, name, p);
                     } else if (Set.of("color", "food", "animal", "flower").contains(q.key())) {
                        this.knocker.getChat().getBrain().addLike(answer);
                     }

                     KnockerMemoryStore.setFact(player.method_5667(), q.key(), answer);
                     if (q.fact() != null) {
                        KnockerMemoryStore.addDiary(player.method_5667(), List.of(q.fact().formatted(answer)));
                     }

                     KnockerEntity var10 = this.knocker;
                     String var10001 = q.fact();
                     Object[] var10002 = new Object[]{answer};
                     var10.rememberEvent("я спросил, и она ответила: " + var10001.formatted(var10002));
                     return nickReply != null ? nickReply : this.knocker.getChat().getBrain().pick(ACK[p]).formatted(answer);
                  }
               }
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private String nickCheck(class_1657 player, String name, int p) {
      String nick = player.method_5477().getString().toLowerCase();
      String lat = translit(name.toLowerCase().split(" ")[0]);
      String ru = name.toLowerCase().split(" ")[0];
      boolean same = lat.length() >= 3 && (nick.contains(lat) || lat.contains(nick)) || ru.length() >= 3 && nick.contains(ru);
      if (same) {
         return p >= KnockerPhase.LOVE.ordinal() ? "ты не врёшь мне. хорошо. " + name + "..." : "ты не врёшь мне... хорошо";
      } else if (p >= KnockerPhase.OBSESSION.ordinal()) {
         String var10000 = player.method_5477().getString();
         return "Над тобой написано «" + var10000 + "». Зачем ты мне врёшь?.. Ладно. " + name + ". Пока.";
      } else {
         return p >= KnockerPhase.ATTACHED.ordinal() && this.random.nextBoolean() ? name + "?.. а над тобой написано " + player.method_5477().getString() + "... ладно. " + name + " красивее" : null;
      }
   }

   private static String translit(String s) {
      String[] from = new String[]{"а", "б", "в", "г", "д", "е", "ё", "ж", "з", "и", "й", "к", "л", "м", "н", "о", "п", "р", "с", "т", "у", "ф", "х", "ц", "ч", "ш", "щ", "ъ", "ы", "ь", "э", "ю", "я"};
      String[] to = new String[]{"a", "b", "v", "g", "d", "e", "e", "zh", "z", "i", "y", "k", "l", "m", "n", "o", "p", "r", "s", "t", "u", "f", "h", "ts", "ch", "sh", "sch", "", "y", "", "e", "yu", "ya"};
      StringBuilder b = new StringBuilder();

      for(char c : s.toCharArray()) {
         String ch = String.valueOf(c);
         int i = Arrays.asList(from).indexOf(ch);
         b.append(i >= 0 ? to[i] : ch);
      }

      return b.toString();
   }

   static String extract(String norm) {
      List<String> keep = new ArrayList();

      for(String w : norm.split(" ")) {
         if ((w.equals("и") || w.equals("или")) && !keep.isEmpty() && !((String)keep.get(keep.size() - 1)).equals(w)) {
            keep.add(w);
         } else if (!w.isEmpty() && !FILLER.contains(w)) {
            keep.add(w);
            if (keep.size() >= 5) {
               break;
            }
         }
      }

      while(!keep.isEmpty() && (((String)keep.get(keep.size() - 1)).equals("и") || ((String)keep.get(keep.size() - 1)).equals("или"))) {
         keep.remove(keep.size() - 1);
      }

      return String.join(" ", keep);
   }

   public String recall(class_1657 player) {
      Map<String, String> f = KnockerMemoryStore.facts(player.method_5667());
      if (f.isEmpty()) {
         return null;
      } else {
         List<String> lines = new ArrayList();
         int p = this.knocker.getPhase().ordinal();
         boolean dark = p >= KnockerPhase.OBSESSION.ordinal();
         if (f.containsKey("color")) {
            lines.add(dark ? "я покрашу всё вокруг тебя в " + (String)f.get("color") + "..." : "я видел сегодня что-то " + (String)f.get("color") + "... подумал о тебе");
         }

         if (f.containsKey("flower")) {
            lines.add(dark ? "я нарвал " + (String)f.get("flower") + ". все. чтобы никто больше не нарвал." : (String)f.get("flower") + "... я найду их для тебя");
         }

         if (f.containsKey("fear")) {
            lines.add(dark ? "ты боишься " + (String)f.get("fear") + "... а меня?" : "ты говорила, что боишься " + (String)f.get("fear") + "... я рядом");
         }

         if (f.containsKey("dream")) {
            lines.add(dark ? "твоя мечта — " + (String)f.get("dream") + ". а моя — ты." : "ты мечтаешь о " + (String)f.get("dream") + "... я тоже хочу мечтать");
         }

         if (f.containsKey("animal")) {
            String var10001 = (String)f.get("animal");
            lines.add(var10001 + "... я бы хотел быть " + (String)f.get("animal") + ", чтобы ты меня гладила");
         }

         if (f.containsKey("music")) {
            lines.add("я пытался вспомнить " + (String)f.get("music") + "... я давно не слышал музыки");
         }

         if (f.containsKey("someone") && dark) {
            lines.add("ты говорила про «" + (String)f.get("someone") + "»... ОН ЕЩЁ ЖИВ?");
         }

         if (f.containsKey("food")) {
            lines.add("я бы приготовил тебе " + (String)f.get("food") + "... если бы умел");
         }

         return lines.isEmpty() ? null : (String)lines.get(this.random.nextInt(lines.size()));
      }
   }

   static {
      QUESTIONS = new Q[]{new Q("gender", KnockerPhase.CURIOUS, new String[]{"а ты... парень или девушка?", "кто ты... парень? девушка?"}, (String)null), new Q("name", KnockerPhase.CURIOUS, new String[]{"как тебя зовут?..", "у тебя есть имя?", "как мне тебя называть?.."}, "имя — %s"), new Q("color", KnockerPhase.CURIOUS, new String[]{"какой твой любимый цвет?", "какой цвет тебе нравится?"}, "любимый цвет — %s"), new Q("food", KnockerPhase.CURIOUS, new String[]{"что ты любишь есть?", "какая твоя любимая еда?"}, "любимая еда — %s"), new Q("animal", KnockerPhase.CURIOUS, new String[]{"какое животное тебе нравится?", "у тебя есть любимое животное?"}, "любимое животное — %s"), new Q("fear", KnockerPhase.ATTACHED, new String[]{"чего ты боишься?..", "что тебя пугает больше всего?"}, "боится — %s"), new Q("flower", KnockerPhase.ATTACHED, new String[]{"какие цветы тебе нравятся?", "если бы я принёс цветы... какие?"}, "любимые цветы — %s"), new Q("music", KnockerPhase.ATTACHED, new String[]{"какую музыку ты слушаешь?", "что ты слушаешь, когда грустно?"}, "слушает — %s"), new Q("friends", KnockerPhase.ATTACHED, new String[]{"у тебя есть друзья?..", "с кем ты дружишь?"}, "про друзей — %s"), new Q("hobby", KnockerPhase.ATTACHED, new String[]{"чем ты занимаешься, когда не здесь?", "что ты любишь делать?"}, "любит делать — %s"), new Q("dream", KnockerPhase.LOVE, new String[]{"о чём ты мечтаешь?", "если бы ты могла всё... чего бы ты хотела?"}, "мечтает — %s"), new Q("season", KnockerPhase.LOVE, new String[]{"какое время года ты любишь?", "ты любишь зиму или лето?"}, "любимое время года — %s"), new Q("someone", KnockerPhase.LOVE, new String[]{"тебе кто-нибудь нравится?..", "у тебя есть кто-то... особенный?"}, "про того, кто нравится — %s"), new Q("sad", KnockerPhase.LOVE, new String[]{"что ты делаешь, когда тебе плохо?", "кто тебя утешает, когда грустно?"}, "когда плохо — %s"), new Q("leave", KnockerPhase.OBSESSION, new String[]{"ты бы ушла от меня?", "что будет, если я исчезну?"}, "про то, уйдёт ли — %s"), new Q("others", KnockerPhase.PARANOIA, new String[]{"С кем ты говорила сегодня?", "Кто ещё знает, где ты живёшь?"}, "про других — %s")};
      FILLER = Set.of("ну", "мой", "моя", "мое", "моё", "мои", "любимый", "любимая", "любимое", "любимые", "наверное", "наверно", "это", "я", "люблю", "нравится", "нравятся", "мне", "обожаю", "хм", "э", "эм", "типа", "короче", "вроде", "какой", "какая", "цвет", "есть", "еда", "цветы", "цветок", "животное", "музыка", "музыку", "боюсь", "мечтаю", "о", "об", "том", "чтобы", "а", "и", "же", "да", "у", "меня", "зовут", "звать", "имя", "наверн", "слушаю", "ем", "хочу", "думаю", "кажется", "пожалуй", "всё", "все", "очень", "сильно", "больше", "всего", "самый", "самая");
      ACK = new String[][]{{"...%s", "запомню..."}, {"%s?.. запомню", "%s... интересно"}, {"%s... я запомню", "Запомнил. %s", "%s... хорошо, что ты сказала"}, {"%s... теперь я знаю о тебе ещё чуть-чуть", "Я запишу это. %s", "%s... как мило"}, {"%s. Запомнил. Навсегда.", "Теперь я знаю и это. %s.", "%s... Моё."}, {"%s. Я ЗАПОМНИЛ.", "%s... а кому ещё ты это говорила?", "Я ЗНАЮ. %s."}};
      REFUSED = new String[]{"...ладно", "не хочешь говорить?..", "ладно... потом скажешь", "Ты что-то скрываешь?..", "Я всё равно узнаю.", "ТЫ ЧТО-ТО СКРЫВАЕШЬ."};
   }

   private static record Q(String key, KnockerPhase minPhase, String[] ask, String fact) {
   }
}
