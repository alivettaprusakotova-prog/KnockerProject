package com.theneru.knocker.entity;

import com.theneru.knocker.KnockerConfig;
import com.theneru.knocker.ModSounds;
import com.theneru.knocker.util.KnockerPhase;
import java.util.List;
import java.util.Random;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_3419;
import net.minecraft.class_9334;

public class KnockerTrips {
   private static final String[] RETURN_LINES = new String[]{"Я вернулся. Скучала?", "Я принёс тебе кое-что...", "Не спрашивай, где я был.", "Я ходил туда, где темно. Это тебе.", "Ты ведь не уходила, пока меня не было?"};
   private static final String[] NOTES = new String[]{"я видел, как ты спишь", "не открывай дверь никому кроме меня", "я всегда рядом", "ты моя"};
   private static final class_1792[] STRANGE;
   private static final class_1792[] VALUABLE;
   private final KnockerEntity knocker;
   private final KnockerMovement movement;
   private final Random random = new Random();
   private boolean away = false;
   private int awayUntil = 0;
   private int nextTripTick;
   private int returnKnocks;
   private class_1657 returnTo;

   public KnockerTrips(KnockerEntity knocker, KnockerMovement movement) {
      this.nextTripTick = KnockerConfig.ticksBetween(KnockerConfig.tripMin, KnockerConfig.tripMax);
      this.returnKnocks = 0;
      this.returnTo = null;
      this.knocker = knocker;
      this.movement = movement;
   }

   public boolean isAway() {
      return this.away;
   }

   public boolean tick(class_1657 player) {
      int now = this.knocker.tickCounter;
      if (this.away) {
         if (player != null) {
            this.knocker.method_5814(player.method_23317(), player.method_23318() + (double)40.0F, player.method_23321());
         }

         this.knocker.method_18799(class_243.field_1353);
         if (this.returnKnocks > 0) {
            if (now % 12 == 0 && this.returnTo != null) {
               --this.returnKnocks;
               this.knockNear(this.returnTo);
               if (this.returnKnocks == 0) {
                  this.comeBack(this.returnTo, false);
               }
            }

            return true;
         } else {
            if (now >= this.awayUntil && player != null) {
               this.chooseReturn(player);
            }

            return true;
         }
      } else if (player != null && this.knocker.getPhase() == KnockerPhase.PARANOIA) {
         if (now >= this.nextTripTick && !this.knocker.getGuard().isFighting() && !(this.knocker.method_5739(player) > 48.0F)) {
            this.startTrip();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void startTrip() {
      this.away = true;
      this.awayUntil = this.knocker.tickCounter + 1200 + this.random.nextInt(2400);
      this.knocker.method_5942().method_6340();
      this.knocker.method_5648(true);
      this.knocker.method_5875(true);
      this.knocker.method_5684(true);
   }

   private void chooseReturn(class_1657 player) {
      class_1937 world = this.knocker.method_37908();
      class_2338 home = this.knocker.getHomePos();
      if (home != null && world.method_23886() && player.method_24515().method_10262(home) < (double)1600.0F && this.random.nextInt(10) < 4) {
         class_2338 window = KnockerDoors.findWindow(world, home, 10);
         if (window != null) {
            KnockerDoors.breakWindowOpening(world, window);
         }

         this.knocker.playAction("break");
         this.restore();
         this.movement.teleportTo(new class_243((double)home.method_10263() + (double)0.5F, (double)home.method_10264() + 0.6, (double)home.method_10260() + (double)0.5F));
         this.knocker.getItems().deliver(player, List.of(this.randomFind()), (String)null);
         this.knocker.getChat().say(player, window != null ? "я дома. жду тебя у кровати." : "я дома. жду тебя.");
      } else if (this.random.nextBoolean()) {
         this.returnKnocks = 3;
         this.returnTo = player;
      } else {
         this.comeBack(player, true);
      }
   }

   private void comeBack(class_1657 player, boolean inFront) {
      this.restore();
      if (inFront) {
         this.movement.appearInFront(player);
      } else {
         this.movement.teleportBehind(player);
      }

      this.knocker.getItems().deliver(player, List.of(this.randomFind()), RETURN_LINES[this.random.nextInt(RETURN_LINES.length)]);
   }

   private void restore() {
      this.away = false;
      this.returnKnocks = 0;
      this.knocker.method_5648(false);
      this.knocker.method_5875(false);
      this.knocker.method_5684(false);
      this.nextTripTick = this.knocker.tickCounter + KnockerConfig.ticksBetween(KnockerConfig.tripMin, KnockerConfig.tripMax);
   }

   public void resetAfterLoad() {
      this.knocker.method_5648(false);
      this.knocker.method_5875(false);
      this.knocker.method_5684(false);
   }

   private void knockNear(class_1657 player) {
      double rad = Math.toRadians((double)player.method_36454());
      this.knocker.method_37908().method_43128((class_1657)null, player.method_23317() + Math.sin(rad) * (double)4.0F, player.method_23318() + (double)1.0F, player.method_23321() - Math.cos(rad) * (double)4.0F, ModSounds.KNOCK, class_3419.field_15251, 1.0F, 1.0F);
   }

   private class_1799 randomFind() {
      int roll = this.random.nextInt(100);
      if (roll < 35) {
         class_1799 note = new class_1799(class_1802.field_8407);
         String[] var10002 = NOTES;
         note.method_57379(class_9334.field_49631, class_2561.method_43470("§7" + var10002[this.random.nextInt(NOTES.length)]));
         return note;
      } else if (roll < 75) {
         class_1799 strange = new class_1799(STRANGE[this.random.nextInt(STRANGE.length)]);
         strange.method_57379(class_9334.field_49631, class_2561.method_43470("§7я нашёл это для тебя"));
         return strange;
      } else {
         return new class_1799(VALUABLE[this.random.nextInt(VALUABLE.length)]);
      }
   }

   static {
      STRANGE = new class_1792[]{class_1802.field_8398, class_1802.field_8606, class_1802.field_17515, class_1802.field_8511, class_1802.field_8680};
      VALUABLE = new class_1792[]{class_1802.field_8477, class_1802.field_8687, class_1802.field_8695, class_1802.field_27063};
   }
}
