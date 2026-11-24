package com.theneru.knocker.entity;

import com.theneru.knocker.KnockerAdvancements;
import com.theneru.knocker.KnockerMemoryStore;
import com.theneru.knocker.ModSounds;
import com.theneru.knocker.util.KnockerMessages;
import com.theneru.knocker.util.KnockerPhase;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_243;
import net.minecraft.class_3489;
import net.minecraft.class_3532;

public class KnockerGames {
   private final KnockerEntity knocker;
   private final Random random = new Random();
   private Game game;
   private class_1657 who;
   private int timer;
   private int startTick;
   private int secret;
   private int tries;
   private int riddle;
   private Dare dare;
   private String dareText;
   private double dareStartY;
   private int wins;
   private int losses;
   private static final String[] RPS = new String[]{"камень", "ножницы", "бумага"};
   private Game lastGame;
   private int lastGameEnd;
   private static final String[][] RIDDLES = new String[][]{{"зелёный, тихий, подходит сзади и шипит. кто?", "крипер"}, {"ночью стучит, днём молчит, в дверь не входит, а всегда рядом. кто?", "ты", "кнокер", "ты сам"}, {"чем больше из неё берёшь, тем больше она становится. что это?", "яма", "дыра"}, {"у неё есть ключ, но нет замка. есть место, но нет комнаты. что это?", "клавиатура"}, {"всегда идёт, но никогда не приходит. что это?", "завтра"}, {"я хожу за тобой днём и исчезаю ночью. кто я?", "тень"}, {"его можно сломать, даже не прикоснувшись. что это?", "обещание", "слово"}, {"что можно увидеть с закрытыми глазами?", "сон", "сны"}, {"глаза светятся, ходит с палкой, телепортируется, если посмотришь. кто?", "эндермен", "эндер"}, {"чем дольше я есть, тем меньше от меня остаётся. кто я?", "свеча", "факел"}};
   private static final String[][] TRUTHS = new String[][]{{"ты боишься темноты?", "что ты делала вчера?", "у тебя есть секрет?"}, {"ты когда-нибудь думала обо мне перед сном?", "кого ты любишь больше всех?", "ты врёшь мне?"}, {"ты бы осталась со мной навсегда?", "кто последний, с кем ты говорила, кроме меня?", "ты меня боишься?"}};

   public KnockerGames(KnockerEntity knocker) {
      this.game = KnockerGames.Game.NONE;
      this.timer = 0;
      this.startTick = 0;
      this.secret = 0;
      this.tries = 0;
      this.riddle = -1;
      this.dare = KnockerGames.Dare.NONE;
      this.dareText = "";
      this.dareStartY = (double)0.0F;
      this.wins = 0;
      this.losses = 0;
      this.lastGame = KnockerGames.Game.NONE;
      this.lastGameEnd = -100000;
      this.knocker = knocker;
   }

   public boolean isBusy() {
      return this.game == KnockerGames.Game.HIDE_COUNT || this.game == KnockerGames.Game.HIDE_SEEK;
   }

   private int now() {
      return this.knocker.tickCounter;
   }

   private int phase() {
      return this.knocker.getPhase().ordinal();
   }

   private void say(String text) {
      if (this.who != null) {
         this.knocker.getChat().say(this.who, text);
      }

   }

   private String pick(String... lines) {
      return this.knocker.getChat().getBrain().pick(lines);
   }

   public String handleChat(class_1657 player, String norm) {
      int p = this.phase();
      switch (this.game.ordinal()) {
         case 3:
            return this.rpsMove(norm);
         case 4:
            String r = this.numberGuess(norm);
            if (r != null) {
               return r;
            }
            break;
         case 5:
            return this.riddleAnswer(norm);
         case 6:
            String r = this.truthOrDareChoice(norm);
            if (r != null) {
               return r;
            }
            break;
         case 7:
            this.game = KnockerGames.Game.NONE;
            KnockerMemoryStore.addDiary(player.method_5667(), List.of("на «правду» она ответила: " + norm));
            this.knocker.addObsession(2);
            return this.pick("я запомню это", "правда?.. хорошо", "теперь я знаю о тебе ещё немного", "хм... я тебе верю. пока");
         case 8:
            if (this.dare == KnockerGames.Dare.SAY && KnockerMessages.normalize(this.dareText).trim().length() > 0 && norm.contains(KnockerMessages.normalize(this.dareText).trim())) {
               return this.dareDone();
            }
      }

      if (this.game == KnockerGames.Game.NONE && this.lastGame == KnockerGames.Game.RPS && this.now() - this.lastGameEnd < 600 && norm.trim().matches("^(да|ага|угу|давай|ещё|еще|еще раз|ещё раз|давай еще|давай ещё|давай еще раз|давай ещё раз|еще давай|ещё давай|го|погнали|конечно|хочу)( давай)?$")) {
         this.lastGame = KnockerGames.Game.NONE;
         return this.startRps(player);
      } else if (this.game == KnockerGames.Game.NONE && p >= KnockerPhase.CURIOUS.ordinal() && norm.trim().matches("^(п д|пд|п или д|п/д)$")) {
         return this.startTruthOrDare(player);
      } else if (KnockerMessages.containsAny(norm, "стоп игра", "хватит играть", "не хочу играть", "закончим игру") && this.game != KnockerGames.Game.NONE) {
         this.game = KnockerGames.Game.NONE;
         return this.pick("ладно... поиграем потом", "уже? ну ладно");
      } else if (p < KnockerPhase.CURIOUS.ordinal()) {
         return KnockerMessages.containsAny(norm, "прятки", "поиграем", "сыграем", "давай играть") ? "...игры?.. потом. может быть" : null;
      } else if (KnockerMessages.containsAny(norm, "прятки", "в прятки", "прятаться")) {
         return this.startHide(player);
      } else if (KnockerMessages.containsAny(norm, "камень ножницы", "камень-ножницы", "кнб", "цу е фа", "цуефа")) {
         return this.startRps(player);
      } else if (KnockerMessages.containsAny(norm, "угадай число", "загадай число", "угадывать числа")) {
         return this.startNumber(player);
      } else if (KnockerMessages.containsAny(norm, "загадку", "загадка", "загадки", "загадай что")) {
         return this.startRiddle(player);
      } else if (KnockerMessages.containsAny(norm, "правда или действие", "правду или действие")) {
         return this.startTruthOrDare(player);
      } else {
         return KnockerMessages.containsAny(norm, "поиграем", "сыграем", "давай играть", "во что поиграть", "мне скучно давай") ? this.pick("во что?.. прятки, загадки, камень-ножницы-бумага... или правда или действие?", "давай. в прятки? или загадку загадать?") : null;
      }
   }

   private String startHide(class_1657 player) {
      this.knocker.getHelp().cancelJob();
      this.who = player;
      this.game = KnockerGames.Game.HIDE_COUNT;
      this.startTick = this.now();
      this.timer = 0;
      this.knocker.method_5942().method_6340();
      String var10000;
      switch (this.phase()) {
         case 1:
         case 2:
            var10000 = "прятки?.. хорошо. закрываю глаза. считаю до 30";
            break;
         case 3:
            var10000 = "Давай! Я закрываю глаза... считаю до 30. Прячься";
            break;
         case 4:
            var10000 = "Прячься. Я всё равно найду. Раз...";
            break;
         default:
            var10000 = "ПРЯЧЬСЯ. Я СЧИТАЮ. Ты никогда не спрячешься от меня.";
      }

      return var10000;
   }

   public boolean tick(class_1657 player) {
      if (this.game == KnockerGames.Game.DARE) {
         this.tickDare(player);
      }

      if (this.game != KnockerGames.Game.NONE && this.isBusy()) {
         if (this.who != null && !this.who.method_31481()) {
            int t = this.now() - this.startTick;
            if (this.game == KnockerGames.Game.HIDE_COUNT) {
               this.knocker.method_5942().method_6340();
               class_243 away = this.knocker.method_19538().method_1020(this.who.method_19538());
               this.knocker.method_5988().method_20248(this.knocker.method_23317() + away.field_1352, this.knocker.method_23320(), this.knocker.method_23321() + away.field_1350);
               if (t > 0 && t % 100 == 0 && t < 600) {
                  this.say(t / 20 + "...");
               }

               if (t >= 600) {
                  this.game = KnockerGames.Game.HIDE_SEEK;
                  this.startTick = this.now();
                  this.say(this.phase() >= 5 ? "ИДУ ИСКАТЬ." : "кто не спрятался... я иду искать");
                  if (this.phase() >= 5) {
                     this.knocker.getKnockerMovement().teleportBehind(this.who);
                     this.knocker.playSoundFor(this.who, ModSounds.STINGER, 0.6F, 1.0F);
                  }
               }

               return true;
            } else {
               double dist = (double)this.knocker.method_5739(this.who);
               if (dist < (double)3.0F && this.knocker.method_6057(this.who)) {
                  this.game = KnockerGames.Game.NONE;
                  this.knocker.playAction("laugh");
                  this.knocker.playSoundFor(this.who, ModSounds.LAUGH, 0.6F, 1.0F);
                  this.knocker.addObsession(3);
                  KnockerAdvancements.grant(this.who, "hide_seek");
                  this.say(this.pick(this.phase() >= 4 ? new String[]{"НАШЁЛ.", "Нашёл. Я всегда тебя найду.", "Попалась. Хе-хе."} : new String[]{"нашёл!", "вот ты где... хе-хе", "попалась!"}));
                  return true;
               } else if (t > 1800) {
                  this.game = KnockerGames.Game.NONE;
                  this.say(this.pick("сдаюсь... выходи. ты хорошо прячешься", "ладно... ты победила. где ты была?"));
                  this.knocker.addObsession(2);
                  return true;
               } else {
                  if (t % 40 == 0) {
                     double spread = Math.max((double)2.0F, (double)12.0F - (double)t / (double)120.0F);
                     class_243 guess = this.who.method_19538().method_1031((this.random.nextDouble() - (double)0.5F) * spread * (double)2.0F, (double)0.0F, (this.random.nextDouble() - (double)0.5F) * spread * (double)2.0F);
                     this.knocker.method_5942().method_6337(guess.field_1352, guess.field_1351, guess.field_1350, (double)1.0F);
                  }

                  if (t % 300 == 150) {
                     this.say(dist < (double)6.0F ? this.pick("горячо...", "я тебя почти чувствую...", "ты где-то совсем рядом)") : (dist < (double)15.0F ? this.pick("тепло...", "кажется, я слышу тебя", "ты близко. я знаю") : (dist < (double)30.0F ? this.pick("холодно...", "хм. не здесь...", "куда ты забралась?..") : this.pick("где же ты...", "ты хорошо спряталась...", "ау?..", "я найду тебя. всегда нахожу", "ты же не ушла насовсем?.."))));
                  }

                  return true;
               }
            }
         } else {
            this.game = KnockerGames.Game.NONE;
            return false;
         }
      } else {
         return false;
      }
   }

   private String startRps(class_1657 player) {
      this.who = player;
      this.game = KnockerGames.Game.RPS;
      return "давай. раз... два... три! что у тебя?";
   }

   private String rpsMove(String norm) {
      int her = norm.contains("камен") ? 0 : (norm.contains("нож") ? 1 : (norm.contains("бумаг") ? 2 : -1));
      if (her < 0) {
         this.game = KnockerGames.Game.NONE;
         return null;
      } else {
         this.game = KnockerGames.Game.NONE;
         this.lastGame = KnockerGames.Game.RPS;
         this.lastGameEnd = this.now();
         int p = this.phase();
         int his = p >= KnockerPhase.OBSESSION.ordinal() ? (her + 2) % 3 : this.random.nextInt(3);
         String h = RPS[his];
         if (his == her) {
            return h + "... ничья. ещё раз?";
         } else {
            boolean heWins = (his + 1) % 3 == her;
            if (heWins) {
               ++this.wins;
               String var10000;
               switch (p) {
                  case 4 -> var10000 = h + ". Я выиграл. Я всегда выигрываю у тебя.";
                  case 5 -> var10000 = h + ". Я ЗНАЛ, ЧТО ТЫ ВЫБЕРЕШЬ. Я ВСЕГДА ЗНАЮ.";
                  default -> var10000 = h + "... я выиграл! хе-хе. ещё?";
               }

               return var10000;
            } else {
               ++this.losses;
               this.knocker.addObsession(1);
               return this.pick(h + "... ты выиграла. ещё раз!", h + "... нечестно. ты умная", h + "... ладно, ты победила");
            }
         }
      }
   }

   private String startNumber(class_1657 player) {
      this.who = player;
      this.game = KnockerGames.Game.NUMBER;
      this.secret = 1 + this.random.nextInt(10);
      this.tries = 0;
      return "я загадал число от 1 до 10. угадаешь? у тебя три попытки";
   }

   private String numberGuess(String norm) {
      Matcher m = Pattern.compile("\\b(\\d{1,2})\\b").matcher(norm);
      int guess;
      if (m.find()) {
         guess = Integer.parseInt(m.group(1));
      } else {
         guess = wordNumber(norm);
      }

      if (guess < 1) {
         return null;
      } else {
         ++this.tries;
         int p = this.phase();
         if (guess == this.secret) {
            this.game = KnockerGames.Game.NONE;
            if (p >= KnockerPhase.PARANOIA.ordinal() && this.random.nextBoolean()) {
               return "Нет. Не " + guess + ". Я поменял число. Ты не угадаешь. Никогда.";
            } else {
               this.knocker.addObsession(2);
               return this.pick("угадала! " + this.secret + "... ты читаешь мои мысли?", this.secret + "! как ты узнала?..");
            }
         } else if (this.tries >= 3) {
            this.game = KnockerGames.Game.NONE;
            return "нет... это было " + this.secret + ". хе-хе";
         } else {
            return guess < this.secret ? "больше..." : "меньше...";
         }
      }
   }

   private static int wordNumber(String norm) {
      String[] words = new String[]{"один", "два", "три", "четыре", "пять", "шесть", "семь", "восемь", "девять", "десять"};

      for(int i = 0; i < words.length; ++i) {
         if ((" " + norm + " ").contains(" " + words[i] + " ")) {
            return i + 1;
         }
      }

      return -1;
   }

   private String startRiddle(class_1657 player) {
      this.who = player;
      this.game = KnockerGames.Game.RIDDLE;
      this.riddle = this.random.nextInt(RIDDLES.length);
      this.tries = 0;
      return RIDDLES[this.riddle][0];
   }

   private String riddleAnswer(String norm) {
      String[] r = RIDDLES[this.riddle];

      for(int i = 1; i < r.length; ++i) {
         if (KnockerMessages.containsAny(norm, r[i])) {
            this.game = KnockerGames.Game.NONE;
            this.knocker.addObsession(2);
            return this.riddle == 1 ? "...да. это я. всегда рядом." : this.pick("правильно... ты умная", "да! хе-хе", "верно. ещё загадку?");
         }
      }

      ++this.tries;
      if (this.tries < 3 && !KnockerMessages.containsAny(norm, "сдаюсь", "не знаю", "скажи ответ")) {
         return this.pick("нет... подумай ещё", "не то...", "холодно...");
      } else {
         this.game = KnockerGames.Game.NONE;
         return "ответ — " + r[1] + ". хе-хе";
      }
   }

   private String startTruthOrDare(class_1657 player) {
      this.who = player;
      this.game = KnockerGames.Game.TRUTH_OR_DARE;
      return "правда или действие?..";
   }

   private String truthOrDareChoice(String norm) {
      if (norm.trim().equals("п")) {
         norm = "правда";
      } else if (norm.trim().equals("д")) {
         norm = "действие";
      }

      int tier = this.phase() >= KnockerPhase.OBSESSION.ordinal() ? 2 : (this.phase() >= KnockerPhase.LOVE.ordinal() ? 1 : 0);
      if (norm.contains("правд")) {
         this.game = KnockerGames.Game.TRUTH;
         String[] q = TRUTHS[tier];
         return q[this.random.nextInt(q.length)];
      } else if (norm.contains("действ")) {
         this.game = KnockerGames.Game.DARE;
         this.startTick = this.now();
         this.dareStartY = this.who.method_23318();
         int roll = this.random.nextInt(4);
         switch (roll) {
            case 0:
               this.dare = KnockerGames.Dare.SAY;
               this.dareText = tier == 2 ? "я твоя навсегда" : (tier == 1 ? "ты мне нравишься" : "кнокер хороший");
               String var10000 = this.dareText;
               return "скажи: «" + KnockerGender.adapt(var10000, this.who.method_5667()) + "»";
            case 1:
               this.dare = KnockerGames.Dare.CROUCH;
               return "присядь передо мной три раза";
            case 2:
               this.dare = KnockerGames.Dare.FLOWER;
               return "принеси мне цветок. любой. у тебя две минуты";
            default:
               this.dare = KnockerGames.Dare.HIGH;
               return "заберись на что-нибудь высокое. блоков на пять выше. у тебя минута";
         }
      } else {
         return null;
      }
   }

   private void tickDare(class_1657 player) {
      if (this.who != null) {
         int t = this.now() - this.startTick;
         if (this.dare == KnockerGames.Dare.HIGH && this.who.method_23318() >= this.dareStartY + (double)5.0F) {
            this.say(this.dareDone());
         } else {
            int limit = this.dare == KnockerGames.Dare.FLOWER ? 2400 : 1200;
            if (t > limit) {
               this.game = KnockerGames.Game.NONE;
               this.dare = KnockerGames.Dare.NONE;
               this.say(this.phase() >= KnockerPhase.OBSESSION.ordinal() ? "Не сделала. Я запомню." : "не сделала... ладно");
            }

         }
      }
   }

   public void onCrouches(int count) {
      if (this.game == KnockerGames.Game.DARE && this.dare == KnockerGames.Dare.CROUCH && count >= 3) {
         this.say(this.dareDone());
      }

   }

   public void onGift(class_1799 stack) {
      if (this.game == KnockerGames.Game.DARE && this.dare == KnockerGames.Dare.FLOWER && stack.method_31573(class_3489.field_20344)) {
         this.say(this.dareDone());
      }

   }

   private String dareDone() {
      this.game = KnockerGames.Game.NONE;
      this.dare = KnockerGames.Dare.NONE;
      this.knocker.addObsession(5);
      this.knocker.playAction(this.phase() <= KnockerPhase.LOVE.ordinal() ? "shy" : "head_tilt");
      return this.pick((new String[][]{{"..."}, {"ты сделала... хе-хе"}, {"Сделала! Хе-хе... ты смелая"}, {"Ты правда это сделала... мне приятно", "Хорошая..."}, {"Хорошая. Послушная.", "Вот так. Умница."}, {"ХОРОШО. Теперь ты делаешь, что я скажу.", "Умница. Мне нравится, когда ты слушаешься."}})[this.phase()]);
   }

   private static int clamp(int v) {
      return class_3532.method_15340(v, 1, 10);
   }

   private static enum Game {
      NONE,
      HIDE_COUNT,
      HIDE_SEEK,
      RPS,
      NUMBER,
      RIDDLE,
      TRUTH_OR_DARE,
      TRUTH,
      DARE;

      // $FF: synthetic method
      private static Game[] $values() {
         return new Game[]{NONE, HIDE_COUNT, HIDE_SEEK, RPS, NUMBER, RIDDLE, TRUTH_OR_DARE, TRUTH, DARE};
      }
   }

   private static enum Dare {
      NONE,
      SAY,
      CROUCH,
      FLOWER,
      HIGH;

      // $FF: synthetic method
      private static Dare[] $values() {
         return new Dare[]{NONE, SAY, CROUCH, FLOWER, HIGH};
      }
   }
}
