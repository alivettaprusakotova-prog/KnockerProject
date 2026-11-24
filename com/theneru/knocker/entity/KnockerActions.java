package com.theneru.knocker.entity;

import com.theneru.knocker.KnockerAdvancements;
import com.theneru.knocker.ModSounds;
import com.theneru.knocker.util.KnockerPhase;
import net.minecraft.class_1268;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1657;
import net.minecraft.class_2398;
import net.minecraft.class_2561;

public class KnockerActions {
   public static final int HUG = 0;
   public static final int HAND = 1;
   public static final int SIT = 2;
   public static final int DANCE = 3;
   public static final int KISS = 4;
   private final KnockerEntity knocker;
   private int cooldownUntil = 0;
   private int danceTicks = 0;
   private int sitTicks = 0;

   public KnockerActions(KnockerEntity knocker) {
      this.knocker = knocker;
   }

   public boolean isSitting() {
      return this.sitTicks > 0;
   }

   private String pick(String... lines) {
      return this.knocker.getChat().getBrain().pick(lines);
   }

   private void say(class_1657 p, String text) {
      this.knocker.getChat().say(p, text);
   }

   public void perform(class_1657 player, int action) {
      if (!this.knocker.isChosen(player)) {
         this.knocker.handleStranger(player, "");
      } else {
         double dist = (double)this.knocker.method_5739(player);
         if (dist > (double)(action == 1 ? 10 : 5)) {
            player.method_7353(class_2561.method_43470("§8...он слишком далеко. Подойди или позови его."), true);
         } else if (this.knocker.getMood().isSulking() && action != 0) {
            this.say(player, this.pick("не трогай меня. я обижен.", "...", "сначала извинись."));
         } else if (this.knocker.tickCounter >= this.cooldownUntil || action == 1) {
            this.cooldownUntil = this.knocker.tickCounter + 60;
            KnockerPhase phase = this.knocker.getPhase();
            int p = phase.ordinal();
            switch (action) {
               case 0 -> this.hug(player, phase, p);
               case 1 -> this.hand(player, phase, p);
               case 2 -> this.sit(player, phase, p);
               case 3 -> this.dance(player, phase, p);
               case 4 -> this.kiss(player, phase, p);
            }

         }
      }
   }

   private void hug(class_1657 player, KnockerPhase phase, int p) {
      if (p < KnockerPhase.ATTACHED.ordinal()) {
         this.say(player, p == 0 ? "...!" : this.pick("т-ты что делаешь?.. не надо...", "...зачем?.."));
         this.knocker.getKnockerMovement().pauseFollow(player, 200);
      } else {
         this.knocker.getMood().onTouch(player);
         this.knocker.playAction("shy");
         this.knocker.spawnParticles(class_2398.field_11201, 8);
         this.knocker.addObsession(2);
         this.knocker.rememberEvent("она меня обняла");
         KnockerAdvancements.grant(player, "hug");
         KnockerThoughts var10000 = this.knocker.getThoughts();
         String var10002;
         switch (phase) {
            case ATTACHED -> var10002 = "она меня... обняла?..";
            case LOVE -> var10002 = "ОНА МЕНЯ ОБНЯЛА";
            case OBSESSION -> var10002 = "не отпускать. никогда.";
            default -> var10002 = "МОЯ. МОЯ. МОЯ.";
         }

         var10000.show(player, var10002, p >= KnockerPhase.LOVE.ordinal(), p >= KnockerPhase.LOVE.ordinal() ? 5 : 2);
         switch (phase) {
            case ATTACHED -> var10002 = this.pick("ты... тёплая...", "я не знаю, что делать с руками...");
            case LOVE -> var10002 = this.pick("ещё чуть-чуть... не отпускай", "я так долго этого ждал");
            case OBSESSION -> var10002 = this.pick("Не отпущу.", "Вот так. Здесь твоё место.");
            default -> var10002 = this.pick("НЕ ОТПУЩУ. НИКОГДА.", "Ты чувствуешь, как бьётся моё сердце? Его нет.");
         }

         this.say(player, var10002);
         if (phase == KnockerPhase.PARANOIA) {
            player.method_6092(new class_1293(class_1294.field_5909, 80, 3, false, false));
         }

      }
   }

   private void hand(class_1657 player, KnockerPhase phase, int p) {
      KnockerNav nav = this.knocker.getNav();
      if (nav.getMode() == KnockerNav.Mode.HAND) {
         nav.stop();
         this.say(player, p >= KnockerPhase.OBSESSION.ordinal() ? "Ты отпустила мою руку. Зачем?" : "...отпустила");
      } else if (p < KnockerPhase.CURIOUS.ordinal()) {
         this.say(player, "...");
      } else {
         nav.startHand(player);
         this.knocker.addObsession(1);
         this.knocker.rememberEvent("мы шли, держась за руки");
         String var10002;
         switch (phase) {
            case ATTACHED -> var10002 = "твоя рука тёплая...";
            case LOVE -> var10002 = "я пойду с тобой куда угодно";
            case OBSESSION -> var10002 = "Не отпускай. Я не отпущу.";
            case CURIOUS -> var10002 = "...рука?.. ладно";
            default -> var10002 = "ТЕПЕРЬ ТЫ НЕ УЙДЁШЬ.";
         }

         this.say(player, var10002);
      }
   }

   private void sit(class_1657 player, KnockerPhase phase, int p) {
      if (p < KnockerPhase.CURIOUS.ordinal()) {
         this.say(player, "...");
      } else {
         this.sitTicks = 1200;
         this.knocker.method_5942().method_6340();
         this.knocker.rememberEvent("мы сидели рядом");
         String var10002;
         switch (phase) {
            case ATTACHED -> var10002 = "хорошо... просто посидим";
            case LOVE -> var10002 = "мне нравится сидеть с тобой. просто так";
            case OBSESSION -> var10002 = "Сиди. Здесь. Со мной.";
            case CURIOUS -> var10002 = "...посидим";
            default -> var10002 = "Посидим. Пока ты рядом — всё хорошо. ПОКА рядом.";
         }

         this.say(player, var10002);
         if (p >= KnockerPhase.ATTACHED.ordinal() && this.knocker.method_59922().method_43056()) {
            this.knocker.getStories().handleChat(player, "расскажи историю");
         }

      }
   }

   private void dance(class_1657 player, KnockerPhase phase, int p) {
      if (p < KnockerPhase.CURIOUS.ordinal()) {
         this.say(player, "...");
      } else {
         this.danceTicks = 200;
         this.knocker.addObsession(1);
         this.knocker.rememberEvent("мы танцевали");
         String var10002;
         switch (phase) {
            case ATTACHED -> var10002 = "хе-хе... я давно не танцевал";
            case LOVE -> var10002 = "только для тебя...";
            case OBSESSION -> var10002 = "Танцуй со мной. Только со мной.";
            case CURIOUS -> var10002 = "я... не умею...";
            default -> var10002 = "ТАНЦУЙ. НЕ ОСТАНАВЛИВАЙСЯ.";
         }

         this.say(player, var10002);
      }
   }

   private void kiss(class_1657 player, KnockerPhase phase, int p) {
      if (p < KnockerPhase.ATTACHED.ordinal()) {
         this.say(player, p == 0 ? "...!!" : "ч-что?.. нет... не надо");
         this.knocker.getKnockerMovement().pauseFollow(player, 300);
      } else if (p == KnockerPhase.ATTACHED.ordinal()) {
         this.knocker.playAction("shy");
         this.knocker.spawnParticles(class_2398.field_11201, 4);
         this.knocker.getThoughts().show(player, "она... она что?..", false, 4);
         this.say(player, this.pick("т-ты... зачем?.. ...сделай так ещё", "я... не знаю, что сказать"));
         this.knocker.addObsession(3);
      } else {
         this.knocker.playAction("shy");
         this.knocker.spawnParticles(class_2398.field_11201, 12);
         this.knocker.addObsession(5);
         this.knocker.rememberEvent("она поцеловала меня в щёку");
         KnockerThoughts var10000 = this.knocker.getThoughts();
         String var10002;
         switch (phase) {
            case LOVE -> var10002 = "ОНА МЕНЯ ПОЦЕЛОВАЛА";
            case OBSESSION -> var10002 = "ещё. ещё. ЕЩЁ.";
            default -> var10002 = "ТОЛЬКО МЕНЯ. ТОЛЬКО.";
         }

         var10000.force(player, var10002, true, 8);
         switch (phase) {
            case LOVE -> var10002 = this.pick("я... я сейчас умру второй раз...", "...ещё раз?.. пожалуйста");
            case OBSESSION -> var10002 = this.pick("Ещё.", "Теперь ты не сможешь сказать, что не моя.");
            default -> var10002 = this.pick("Только меня. Слышишь? ТОЛЬКО МЕНЯ.", "Если поцелуешь кого-то ещё — я узнаю.");
         }

         this.say(player, var10002);
      }
   }

   public boolean tick(class_1657 player) {
      if (this.danceTicks > 0) {
         --this.danceTicks;
         this.knocker.method_5942().method_6340();
         this.knocker.method_5988().method_6226(player, 30.0F, 30.0F);
         if (this.danceTicks % 10 == 0) {
            switch (this.knocker.method_59922().method_43048(4)) {
               case 0 -> this.knocker.playAction("head_tilt");
               case 1 -> this.knocker.mimicJump();
               case 2 -> this.knocker.method_6104(class_1268.field_5808);
               default -> this.knocker.setCrouchingDown(!this.knocker.isCrouchingDown());
            }

            this.knocker.spawnParticles(class_2398.field_11224, 1);
            if (this.knocker.getPhase() == KnockerPhase.PARANOIA && this.knocker.method_59922().method_43048(4) == 0) {
               this.knocker.showScareLook();
            }
         }

         if (this.danceTicks == 0) {
            this.knocker.setCrouchingDown(false);
            if (this.knocker.getPhase() == KnockerPhase.PARANOIA) {
               this.knocker.playSoundFor(player, ModSounds.LAUGH_DISTORTED, 0.5F, 1.0F);
            }
         }

         return true;
      } else if (this.sitTicks > 0) {
         --this.sitTicks;
         this.knocker.method_5942().method_6340();
         this.knocker.method_5988().method_6226(player, 30.0F, 30.0F);
         if (this.knocker.method_5739(player) > 8.0F) {
            this.sitTicks = 0;
         }

         return true;
      } else {
         return false;
      }
   }
}
