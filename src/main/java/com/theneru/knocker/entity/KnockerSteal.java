package com.theneru.knocker.entity;

import com.theneru.knocker.KnockerConfig;
import com.theneru.knocker.KnockerGiftUtils;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_2595;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;

public class KnockerSteal {
   public static final int STEAL_COOLDOWN = 6000;
   public static final int MIN_POINTS = 250;
   private final KnockerEntity knocker;
   private final KnockerMovement movement;
   private final Random random = new Random();
   private int stealCooldown = 0;

   public KnockerSteal(KnockerEntity knocker, KnockerMovement movement) {
      this.knocker = knocker;
      this.movement = movement;
   }

   public boolean canSteal() {
      return this.stealCooldown <= 0 && this.knocker.getObsessionPoints() >= 250;
   }

   public void tick() {
      if (this.stealCooldown > 0) {
         --this.stealCooldown;
      }

      if (this.knocker.getObsessionPoints() >= 250) {
         if (this.knocker.tickCounter % 10 == 0) {
            this.pickUpNearbyGifts();
         }

         if (this.canSteal() && this.knocker.tickCounter % 20 == 0 && this.movement.isAtChest()) {
            this.stealFromChest(this.movement.getTargetPos());
         }

      }
   }

   private void pickUpNearbyGifts() {
      for(class_1542 item : this.knocker.method_37908().method_8390(class_1542.class, this.knocker.method_5829().method_1014((double)1.5F), (itemx) -> itemx.method_5805() && !this.knocker.isOwnDrop(itemx) && itemx.method_24921() instanceof class_1657)) {
         class_1799 stack = item.method_6983();
         if (!stack.method_7960() && KnockerGiftUtils.isGift(stack)) {
            class_1799 gift = stack.method_7972();
            item.method_31472();
            this.knocker.method_37908().method_8396((class_1657)null, this.knocker.method_24515(), class_3417.field_15197, class_3419.field_15254, 0.5F, 1.0F);
            class_1657 player = this.knocker.getChosen((double)16.0F);
            this.knocker.getItems().onGiftPickup(gift, player);
            break;
         }
      }

   }

   private void stealFromChest(class_2338 pos) {
      if (pos != null) {
         class_2586 var3 = this.knocker.method_37908().method_8321(pos);
         if (var3 instanceof class_2595) {
            class_2595 chest = (class_2595)var3;
            ArrayList var8 = new ArrayList();

            for(int i = 0; i < chest.method_5439(); ++i) {
               if (!chest.method_5438(i).method_7960()) {
                  var8.add(i);
               }
            }

            if (var8.isEmpty()) {
               this.movement.ignoreChest(pos, 2400);
               this.movement.clearTarget();
            } else {
               class_3965 hit = this.knocker.method_37908().method_17742(new class_3959(this.knocker.method_33571(), class_243.method_24953(pos), class_3960.field_17558, class_242.field_1348, this.knocker));
               if (hit.method_17783() != class_240.field_1333 && !hit.method_17777().equals(pos)) {
                  this.movement.ignoreChest(pos, 1200);
                  this.movement.clearTarget();
               } else {
                  this.knocker.playAction("chest");
                  int slot = (Integer)var8.get(this.random.nextInt(var8.size()));
                  class_1799 stolen = chest.method_5441(slot);
                  chest.method_5431();
                  this.stealCooldown = KnockerConfig.ticks(KnockerConfig.steal);
                  this.movement.ignoreChest(pos, this.stealCooldown);
                  this.movement.clearTarget();
                  this.knocker.method_37908().method_8396((class_1657)null, pos, class_3417.field_14982, class_3419.field_15245, 0.5F, 0.8F);
                  this.knocker.method_37908().method_8396((class_1657)null, pos, class_3417.field_14823, class_3419.field_15245, 0.5F, 0.8F);
                  this.keepStolen(stolen);
                  class_1657 player = this.knocker.getChosen((double)16.0F);
                  if (player != null) {
                     this.knocker.getChat().say(player, "Мне это пригодится...");
                  }

               }
            }
         } else {
            this.movement.clearTarget();
         }
      }
   }

   private void keepStolen(class_1799 stack) {
      if (!stack.method_7960()) {
         this.knocker.getItems().handleStolen(stack);
      }

   }

   public int getStealCooldown() {
      return this.stealCooldown;
   }

   public void setStealCooldown(int cooldown) {
      this.stealCooldown = cooldown;
   }
}
