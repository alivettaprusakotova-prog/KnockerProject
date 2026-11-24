package com.theneru.knocker.entity;

import com.theneru.knocker.KnockerGiftUtils;
import com.theneru.knocker.ModSounds;
import com.theneru.knocker.util.KnockerPhase;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_2595;
import net.minecraft.class_2818;
import net.minecraft.class_3419;

public class KnockerMovement {
   public static final int CHEST_SEARCH_RADIUS = 32;
   public static final int GIFT_SEARCH_RADIUS = 8;
   private static final int CHEST_TIMEOUT = 400;
   private final KnockerEntity knocker;
   private class_2338 targetPos = null;
   private TargetType targetType;
   private int targetTimer;
   private int chestSearchCooldown;
   private final Map<class_2338, Integer> ignoredChests;
   private int followPause;
   private class_243 flashReturnPos;
   private int flashTicks;

   public KnockerMovement(KnockerEntity knocker) {
      this.targetType = KnockerMovement.TargetType.NONE;
      this.targetTimer = 0;
      this.chestSearchCooldown = 0;
      this.ignoredChests = new HashMap();
      this.followPause = 0;
      this.flashReturnPos = null;
      this.flashTicks = 0;
      this.knocker = knocker;
   }

   public void tick(class_1657 player, boolean canSteal) {
      if (player != null && this.flashTicks <= 0) {
         KnockerPhase phase = this.knocker.getPhase();
         if (phase.ordinal() < KnockerPhase.ATTACHED.ordinal()) {
            this.clearTarget();
         } else {
            if (this.chestSearchCooldown > 0) {
               --this.chestSearchCooldown;
            }

            if (this.followPause > 0) {
               --this.followPause;
            }

            if (this.targetTimer > 0 && --this.targetTimer == 0) {
               if (this.targetType == KnockerMovement.TargetType.CHEST && this.targetPos != null) {
                  this.ignoreChest(this.targetPos, 1200);
               }

               this.clearTarget();
            }

            if (this.knocker.tickCounter % 10 == 0 || this.targetType == KnockerMovement.TargetType.NONE) {
               this.chooseTarget(player, phase, canSteal);
            }

            if (this.targetType == KnockerMovement.TargetType.PLAYER) {
               this.targetPos = player.method_24515();
            }

            if (this.targetPos != null) {
               this.moveToTarget(phase);
            }

         }
      }
   }

   private void chooseTarget(class_1657 player, KnockerPhase phase, boolean canSteal) {
      class_1542 gift = this.findNearestGift(8);
      if (gift != null) {
         this.setTarget(gift.method_24515(), KnockerMovement.TargetType.GIFT, 100);
      } else {
         if (canSteal) {
            if (this.targetType == KnockerMovement.TargetType.CHEST && this.isChestWorthStealing(this.targetPos)) {
               return;
            }

            if (this.chestSearchCooldown <= 0) {
               this.chestSearchCooldown = 40;
               class_2338 chest = this.findNearestChest(32);
               if (chest != null) {
                  this.setTarget(chest, KnockerMovement.TargetType.CHEST, 400);
                  return;
               }
            }
         }

         if (phase.ordinal() >= KnockerPhase.LOVE.ordinal() && this.followPause <= 0) {
            this.setTarget(player.method_24515(), KnockerMovement.TargetType.PLAYER, 0);
         } else {
            this.clearTarget();
         }
      }
   }

   private void setTarget(class_2338 pos, TargetType type, int timeout) {
      if (type != this.targetType || !pos.equals(this.targetPos)) {
         this.targetPos = pos.method_10062();
         this.targetType = type;
         this.targetTimer = timeout;
      }
   }

   public void clearTarget() {
      this.targetPos = null;
      this.targetType = KnockerMovement.TargetType.NONE;
      this.targetTimer = 0;
   }

   private class_1542 findNearestGift(int radius) {
      List<class_1542> items = this.knocker.method_37908().method_8390(class_1542.class, this.knocker.method_5829().method_1014((double)radius), (itemx) -> itemx.method_5805() && !this.knocker.isOwnDrop(itemx) && itemx.method_24921() instanceof class_1657 && KnockerGiftUtils.isGift(itemx.method_6983()));
      class_1542 best = null;
      double bestDist = Double.MAX_VALUE;

      for(class_1542 item : items) {
         double d = this.knocker.method_5858(item);
         if (d < bestDist) {
            bestDist = d;
            best = item;
         }
      }

      return best;
   }

   private class_2338 findNearestChest(int radius) {
      class_1937 world = this.knocker.method_37908();
      class_2338 center = this.knocker.method_24515();
      int chunkRadius = (radius >> 4) + 1;
      int centerCx = center.method_10263() >> 4;
      int centerCz = center.method_10260() >> 4;
      class_2338 best = null;
      double bestDist = (double)radius * (double)radius;

      for(int cx = centerCx - chunkRadius; cx <= centerCx + chunkRadius; ++cx) {
         for(int cz = centerCz - chunkRadius; cz <= centerCz + chunkRadius; ++cz) {
            class_2818 chunk = world.method_8398().method_21730(cx, cz);
            if (chunk != null) {
               for(class_2586 be : chunk.method_12214().values()) {
                  if (be instanceof class_2595) {
                     class_2595 chest = (class_2595)be;
                     class_2338 pos = be.method_11016();
                     if (Math.abs(pos.method_10264() - center.method_10264()) <= 8 && !this.isIgnored(pos) && !chest.method_5442() && !this.knocker.getItems().isStash(pos)) {
                        double d = pos.method_10262(center);
                        if (d < bestDist) {
                           bestDist = d;
                           best = pos.method_10062();
                        }
                     }
                  }
               }
            }
         }
      }

      return best;
   }

   private boolean isChestWorthStealing(class_2338 pos) {
      if (pos != null && !this.isIgnored(pos) && !this.knocker.getItems().isStash(pos)) {
         class_2586 var3 = this.knocker.method_37908().method_8321(pos);
         boolean var10000;
         if (var3 instanceof class_2595) {
            class_2595 chest = (class_2595)var3;
            if (!chest.method_5442()) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      } else {
         return false;
      }
   }

   public void ignoreChest(class_2338 pos, int ticks) {
      this.ignoredChests.put(pos.method_10062(), this.knocker.tickCounter + ticks);
   }

   private boolean isIgnored(class_2338 pos) {
      Integer until = (Integer)this.ignoredChests.get(pos);
      if (until == null) {
         return false;
      } else if (until <= this.knocker.tickCounter) {
         this.ignoredChests.remove(pos);
         return false;
      } else {
         return true;
      }
   }

   private void moveToTarget(KnockerPhase phase) {
      class_243 target = class_243.method_24955(this.targetPos);
      double distSq = this.knocker.method_5707(target);
      double stopDist = this.targetType == KnockerMovement.TargetType.PLAYER ? (double)2.5F : (double)1.5F;
      if (distSq <= stopDist * stopDist) {
         if (this.targetType == KnockerMovement.TargetType.PLAYER) {
            this.knocker.method_5942().method_6340();
         }

      } else {
         if (this.knocker.tickCounter % 10 == 0 || this.knocker.method_5942().method_6357()) {
            this.knocker.method_5942().method_6337(target.field_1352, target.field_1351, target.field_1350, this.getSpeed(phase));
         }

         if (this.knocker.tickCounter % 60 == 0 && !this.knocker.method_5942().method_6357() && this.knocker.method_59922().method_43048(4) == 0) {
            this.knocker.method_37908().method_8396((class_1657)null, this.knocker.method_24515(), ModSounds.STEP, class_3419.field_15251, 0.35F, 0.9F + this.knocker.method_59922().method_43057() * 0.2F);
         }

      }
   }

   private double getSpeed(KnockerPhase phase) {
      double var10000;
      switch (phase) {
         case CURIOUS -> var10000 = (double)1.0F;
         case ATTACHED -> var10000 = 1.15;
         case LOVE -> var10000 = 1.3;
         case OBSESSION -> var10000 = (double)1.5F;
         case PARANOIA -> var10000 = 1.8;
         default -> var10000 = (double)1.0F;
      }

      return var10000;
   }

   public void pauseFollow(class_1657 player, int ticks) {
      this.followPause = ticks;
      this.clearTarget();
      class_243 away = this.knocker.method_19538().method_1020(player.method_19538());
      if (away.method_37268() < 0.01) {
         away = new class_243((double)1.0F, (double)0.0F, (double)0.0F);
      }

      class_243 to = this.knocker.method_19538().method_1019(away.method_1029().method_1021((double)10.0F));
      this.knocker.method_5942().method_6337(to.field_1352, to.field_1351, to.field_1350, (double)1.0F);
   }

   public boolean isFollowPaused() {
      return this.followPause > 0;
   }

   public boolean isGoingToChest() {
      return this.targetType == KnockerMovement.TargetType.CHEST;
   }

   public boolean isAtChest() {
      return this.targetType == KnockerMovement.TargetType.CHEST && this.targetPos != null && this.knocker.method_5707(class_243.method_24953(this.targetPos)) <= (double)6.25F;
   }

   public class_2338 getTargetPos() {
      return this.targetPos;
   }

   public boolean teleportBehind(class_1657 player) {
      return this.placeRelative(player, (double)-2.0F, false);
   }

   public boolean appearInFront(class_1657 player) {
      boolean ok = this.placeRelative(player, 1.3, true);
      if (ok) {
         this.knocker.showScareLook();
         this.knocker.playAction("appear");
      }

      return ok;
   }

   public boolean flashBehind(class_1657 player, int ticks) {
      class_243 previous = this.flashTicks > 0 && this.flashReturnPos != null ? this.flashReturnPos : this.knocker.method_19538();
      if (!this.teleportBehind(player)) {
         return false;
      } else {
         this.flashReturnPos = previous;
         this.flashTicks = ticks;
         return true;
      }
   }

   public void tickFlash() {
      if (this.flashTicks > 0) {
         if (--this.flashTicks == 0 && this.flashReturnPos != null) {
            this.teleportTo(this.flashReturnPos);
            this.flashReturnPos = null;
         }

      }
   }

   public void teleportTo(class_243 pos) {
      this.knocker.method_5942().method_6340();
      this.knocker.method_5808(pos.field_1352, pos.field_1351, pos.field_1350, this.knocker.method_36454(), this.knocker.method_36455());
   }

   private boolean placeRelative(class_1657 player, double forward, boolean facePlayer) {
      float yaw = player.method_36454();
      double rad = Math.toRadians((double)yaw);
      double dirX = -Math.sin(rad);
      double dirZ = Math.cos(rad);
      double[] scales = new double[]{(double)1.0F, 0.7, (double)1.5F};
      double[] yOffsets = new double[]{(double)0.0F, (double)1.0F, (double)-1.0F};

      for(double scale : scales) {
         for(double dy : yOffsets) {
            class_243 pos = new class_243(player.method_23317() + dirX * forward * scale, player.method_23318() + dy, player.method_23321() + dirZ * forward * scale);
            if (this.isFree(pos)) {
               float knockerYaw = facePlayer ? yaw + 180.0F : yaw;
               this.clearTarget();
               this.knocker.method_5942().method_6340();
               this.knocker.method_5808(pos.field_1352, pos.field_1351, pos.field_1350, knockerYaw, 0.0F);
               this.knocker.method_5847(knockerYaw);
               this.knocker.method_5636(knockerYaw);
               this.knocker.method_37908().method_43128((class_1657)null, pos.field_1352, pos.field_1351, pos.field_1350, facePlayer ? ModSounds.STINGER : ModSounds.GLITCH, class_3419.field_15251, facePlayer ? 0.8F : 0.5F, 1.0F);
               return true;
            }
         }
      }

      return false;
   }

   private boolean isFree(class_243 pos) {
      return this.knocker.method_37908().method_8587(this.knocker, this.knocker.method_18377(this.knocker.method_18376()).method_30757(pos));
   }

   public static enum TargetType {
      NONE,
      GIFT,
      CHEST,
      PLAYER;

      // $FF: synthetic method
      private static TargetType[] $values() {
         return new TargetType[]{NONE, GIFT, CHEST, PLAYER};
      }
   }
}
