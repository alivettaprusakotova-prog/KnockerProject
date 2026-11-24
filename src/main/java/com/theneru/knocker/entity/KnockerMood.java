package com.theneru.knocker.entity;

import com.theneru.knocker.ModSounds;
import com.theneru.knocker.util.KnockerMessages;
import com.theneru.knocker.util.KnockerPhase;
import com.theneru.knocker.util.KnockerWords;
import net.minecraft.class_1657;
import net.minecraft.class_2398;
import net.minecraft.class_243;

public class KnockerMood {
   private final KnockerEntity knocker;
   private Mood mood;
   private int until;
   private int sulkTalkTick;

   public KnockerMood(KnockerEntity knocker) {
      this.mood = KnockerMood.Mood.CALM;
      this.until = 0;
      this.sulkTalkTick = -1000;
      this.knocker = knocker;
   }

   public Mood get() {
      return this.mood;
   }

   public boolean isSulking() {
      return this.mood == KnockerMood.Mood.SULK;
   }

   public void set(Mood m, int seconds) {
      if (this.mood != KnockerMood.Mood.SULK || m == KnockerMood.Mood.CALM || m == KnockerMood.Mood.HAPPY) {
         boolean unstable = this.knocker.getPhase().ordinal() >= KnockerPhase.OBSESSION.ordinal();
         this.mood = m;
         this.until = this.knocker.tickCounter + seconds * 20 / (unstable && m != KnockerMood.Mood.SULK ? 2 : 1);
         this.knocker.syncMood(m.ordinal());
      }
   }

   public void onGift(class_1657 player) {
      if (this.mood == KnockerMood.Mood.SULK) {
         this.makeUp(player, "...ладно. подарок принимаю. прощаю.");
      } else {
         this.set(KnockerMood.Mood.HAPPY, 180);
      }

   }

   public void onTouch(class_1657 player) {
      if (this.mood == KnockerMood.Mood.SULK) {
         this.makeUp(player, "...ладно. иди сюда. прощаю.");
      } else {
         this.set(KnockerMood.Mood.HAPPY, 90);
      }

   }

   public void onCompliment() {
      if (this.mood != KnockerMood.Mood.SULK) {
         this.set(KnockerMood.Mood.HAPPY, 180);
      }

   }

   public void onHurt(class_1657 player) {
      if (this.knocker.getPhase().ordinal() >= KnockerPhase.LOVE.ordinal()) {
         this.startSulk(player);
      } else {
         this.set(KnockerMood.Mood.ANGRY, 60);
      }

   }

   public void onInsult(class_1657 player) {
      if (this.knocker.getPhase().ordinal() >= KnockerPhase.LOVE.ordinal() && this.knocker.method_59922().method_43056()) {
         this.startSulk(player);
      } else {
         this.set(KnockerMood.Mood.ANGRY, 90);
      }

   }

   public void onJealous() {
      if (this.mood != KnockerMood.Mood.SULK) {
         this.set(KnockerMood.Mood.JEALOUS, 180);
      }

   }

   public void onLonely() {
      if (this.mood == KnockerMood.Mood.CALM) {
         this.set(KnockerMood.Mood.SAD, 300);
      }

   }

   private void startSulk(class_1657 player) {
      this.mood = KnockerMood.Mood.SULK;
      this.until = this.knocker.tickCounter + 6000;
      this.knocker.syncMood(this.mood.ordinal());
      this.knocker.rememberEvent("я обиделся на неё");
      this.knocker.getKnockerMovement().clearTarget();
   }

   private void makeUp(class_1657 player, String line) {
      this.set(KnockerMood.Mood.HAPPY, 120);
      this.knocker.getChat().say(player, line);
      this.knocker.playAction("shy");
      this.knocker.spawnParticles(class_2398.field_11201, 5);
      this.knocker.rememberEvent("мы помирились");
   }

   public String handleChat(class_1657 player, String norm) {
      if (this.mood != KnockerMood.Mood.SULK) {
         return null;
      } else if (!KnockerMessages.containsAny(norm, KnockerWords.SORRY) && !KnockerMessages.containsAny(norm, "прости", "извини", "прошу прощения", "мне жаль", "не обижайся", "мир")) {
         if (this.knocker.tickCounter - this.sulkTalkTick < 200) {
            return "";
         } else {
            this.sulkTalkTick = this.knocker.tickCounter;
            return this.knocker.getChat().getBrain().pick(new String[]{"я с тобой не разговариваю.", "...", "отстань.", "ты знаешь, что сделала.", "не хочу тебя слышать."});
         }
      } else {
         this.set(KnockerMood.Mood.HAPPY, 120);
         this.knocker.rememberEvent("она извинилась, и я простил");
         this.knocker.playAction("shy");
         String var10000;
         switch (this.knocker.getPhase()) {
            case OBSESSION -> var10000 = "...Ладно. Прощаю. Но я запомнил.";
            case PARANOIA -> var10000 = "Прощаю. В последний раз. ПОСЛЕДНИЙ.";
            default -> var10000 = "...правда?.. ладно. прощаю";
         }

         return var10000;
      }
   }

   public boolean tick(class_1657 player) {
      int now = this.knocker.tickCounter;
      if (this.mood != KnockerMood.Mood.CALM && now > this.until) {
         if (this.mood == KnockerMood.Mood.SULK) {
            this.knocker.getChat().say(player, "...ладно. я больше не обижаюсь.");
         }

         this.mood = KnockerMood.Mood.CALM;
         this.knocker.syncMood(this.mood.ordinal());
      }

      switch (this.mood.ordinal()) {
         case 1:
            if (now % 200 == 0 && this.knocker.method_5739(player) < 10.0F) {
               this.knocker.spawnParticles(class_2398.field_11201, 1);
            }
            break;
         case 2:
         case 4:
            if (now % 300 == 0 && this.knocker.method_5739(player) < 10.0F) {
               this.knocker.spawnParticles(class_2398.field_11231, 1);
            }
            break;
         case 3:
            if (now % 600 == 0 && this.knocker.method_5739(player) < 6.0F && this.knocker.method_59922().method_43048(3) == 0) {
               this.knocker.playSoundAtSelf(ModSounds.BREATH, 0.25F, 0.7F);
            }
            break;
         case 5:
            this.knocker.method_5942().method_6340();
            class_243 away = this.knocker.method_19538().method_1020(player.method_19538());
            this.knocker.method_5988().method_20248(this.knocker.method_23317() + away.field_1352, this.knocker.method_23320(), this.knocker.method_23321() + away.field_1350);
            this.knocker.method_5847(this.knocker.method_43078());
            return true;
      }

      return false;
   }

   public String aiLine() {
      return this.mood == KnockerMood.Mood.CALM ? "" : "Настроение сейчас: " + this.mood.ai + ".";
   }

   public static enum Mood {
      CALM("спокоен", "ты спокоен"),
      HAPPY("рад", "ты сейчас счастлив и тёплый, тебе хорошо с ней"),
      JEALOUS("ревнует", "ты сейчас ревнуешь: она говорила о других — ты колкий и подозрительный"),
      SAD("грустит", "тебе сейчас грустно и одиноко, ты тихий"),
      ANGRY("злится", "ты сейчас злишься на неё, отвечаешь резко"),
      SULK("обижен", "ты обижен и демонстративно не хочешь разговаривать: отвечай коротко и холодно, жди извинений");

      public final String title;
      public final String ai;

      private Mood(String title, String ai) {
         this.title = title;
         this.ai = ai;
      }

      // $FF: synthetic method
      private static Mood[] $values() {
         return new Mood[]{CALM, HAPPY, JEALOUS, SAD, ANGRY, SULK};
      }
   }
}
