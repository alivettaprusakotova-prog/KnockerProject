package com.theneru.knocker.entity;

import com.theneru.knocker.KnockerAdvancements;
import com.theneru.knocker.KnockerConfig;
import com.theneru.knocker.ModSounds;
import com.theneru.knocker.util.KnockerPhase;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2323;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2389;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2756;
import net.minecraft.class_3419;
import net.minecraft.class_3481;

public class KnockerDoors {
   private static final String[][] ASK = new String[][]{{"..."}, {"..."}, {"тук-тук... можно?", "я зайду?)"}, {"можно войти?)", "тук-тук... я зайду?)", "это я... открой?)"}, {"я захожу.", "открой. или я сам.", "я знаю, что ты дома."}, {"Я ЗНАЮ, ЧТО ТЫ ТАМ.", "тук. тук. тук.", "открой. ОТКРОЙ."}};
   private static final String[] LOCKED = new String[]{"...", "...", "закрыто?..", "ты закрылась?..", "Закрылась от меня?", "ЗАПЕРЛАСЬ?"};
   private static final String[] WINDOW = new String[]{"...", "...", "я... через окно, ладно?", "прости за окно... хе-хе", "Окно. Тоже вход.", "ТЫ ДУМАЛА, ДВЕРЬ МЕНЯ ОСТАНОВИТ?"};
   private static final String[] BREAK_DOOR = new String[]{"", "", "", "", "Я же просил открыть.", "Я ЖЕ ПРОСИЛ ОТКРЫТЬ."};
   private static final String[] OTHER_WAY = new String[]{"...", "...", "ладно... я найду другой вход", "ничего... я найду, как войти", "", ""};
   private final KnockerEntity knocker;
   private final KnockerMovement movement;
   private final Random random = new Random();
   private final Map<class_2338, Integer> doorCooldown = new HashMap();
   private class_2338 door = null;
   private int timer = 0;
   private int closeTimer = 0;
   private class_2338 doorToClose = null;
   private final Map<class_2338, Integer> recentlyOpened = new HashMap();
   private int otherWayTimer = 0;
   private class_1657 target = null;
   private int invitedUntil = 0;

   public KnockerDoors(KnockerEntity knocker, KnockerMovement movement) {
      this.knocker = knocker;
      this.movement = movement;
   }

   public void invite(int ticks) {
      this.invitedUntil = this.knocker.tickCounter + ticks;
   }

   public boolean isBusy() {
      return this.door != null;
   }

   public void tick(class_1657 player) {
      class_1937 world = this.knocker.method_37908();
      if (this.doorToClose != null) {
         ++this.closeTimer;
         boolean passed = this.knocker.method_5707(class_243.method_24953(this.doorToClose)) > (double)9.0F && this.closeTimer > 40;
         if (passed || this.closeTimer > 400) {
            this.setDoor(this.doorToClose, false);
            this.doorToClose = null;
         }
      }

      if (this.otherWayTimer > 0 && --this.otherWayTimer == 0 && this.target != null && !this.target.method_31481() && this.knocker.getPhase() == KnockerPhase.PARANOIA && this.random.nextBoolean()) {
         this.movement.teleportBehind(this.target);
      }

      if (this.door != null) {
         this.tickWaiting(world);
      } else if (this.knocker.tickCounter % 10 == 0 && player != null) {
         if (this.knocker.tickCounter < this.invitedUntil) {
            class_2338 d = this.findClosedDoor(world, 2);
            if (d != null) {
               class_2248 var5 = world.method_8320(d).method_26204();
               if (var5 instanceof class_2323) {
                  class_2323 db = (class_2323)var5;
                  if (db.method_51169().comp_1471()) {
                     this.openAndCloseBehind(d);
                     return;
                  }
               }
            }
         }

         KnockerPhase phase = this.knocker.getPhase();
         if (phase.ordinal() >= KnockerPhase.ATTACHED.ordinal() && !this.movement.isFollowPaused()) {
            double dist = (double)this.knocker.method_5739(player);
            if (!(dist < (double)3.0F) && !(dist > (double)32.0F)) {
               class_2338 found = this.findClosedDoor(world, 2);
               if (found != null) {
                  if (this.doorBetween(world, found, player)) {
                     Integer openedAt = (Integer)this.recentlyOpened.get(found);
                     if (openedAt != null && this.knocker.tickCounter - openedAt < 2400) {
                        class_2248 var9 = world.method_8320(found).method_26204();
                        if (var9 instanceof class_2323) {
                           class_2323 db = (class_2323)var9;
                           if (db.method_51169().comp_1471()) {
                              this.openAndCloseBehind(found);
                              return;
                           }
                        }
                     }

                     if (!this.onCooldown(found)) {
                        this.door = found;
                        this.target = player;
                        this.timer = 60;
                        this.knocker.method_5942().method_6340();
                        this.knock(world);
                        String[] pool = ASK[phase.ordinal()];
                        this.knocker.getChat().say(player, pool[this.random.nextInt(pool.length)]);
                     }
                  }
               }
            }
         }
      }
   }

   private void tickWaiting(class_1937 world) {
      KnockerPhase phase = this.knocker.getPhase();
      this.knocker.method_5942().method_6340();
      if (phase == KnockerPhase.PARANOIA && this.timer % 15 == 0 && this.timer > 0) {
         this.knock(world);
      }

      if (--this.timer <= 0) {
         class_2338 d = this.door;
         this.door = null;
         class_2680 state = world.method_8320(d);
         class_2248 var6 = state.method_26204();
         if (var6 instanceof class_2323) {
            class_2323 doorBlock = (class_2323)var6;
            if (!(Boolean)state.method_11654(class_2323.field_10945)) {
               int p = phase.ordinal();
               if (doorBlock.method_51169().comp_1471()) {
                  this.openAndCloseBehind(d);
                  this.cooldown(d, 2400);
                  return;
               }

               this.cooldown(d, KnockerConfig.ticks(KnockerConfig.doorKnock));
               if (this.target != null) {
                  this.knocker.getChat().say(this.target, LOCKED[p]);
               }

               class_2338 window = findWindow(world, d, 6);
               if (window != null) {
                  this.knocker.playAction("break");
                  boolean tall = breakWindowOpening(world, window);
                  if (this.target != null) {
                     this.knocker.getChat().say(this.target, WINDOW[p]);
                     if (tall) {
                        this.knocker.method_5942().method_6337((double)window.method_10263() + (double)0.5F, (double)window.method_10264(), (double)window.method_10260() + (double)0.5F, 1.2);
                     } else if (p >= KnockerPhase.OBSESSION.ordinal()) {
                        this.movement.teleportBehind(this.target);
                     }
                  }

                  return;
               }

               if (p >= KnockerPhase.OBSESSION.ordinal()) {
                  this.knocker.playAction("break");
                  world.method_22352(d, false);
                  if (this.target != null) {
                     this.knocker.getChat().say(this.target, BREAK_DOOR[p]);
                  }
               } else {
                  if (this.target != null && !OTHER_WAY[p].isEmpty()) {
                     this.knocker.getChat().say(this.target, OTHER_WAY[p]);
                  }

                  this.otherWayTimer = 200;
               }

               return;
            }
         }

         this.cooldown(d, 2400);
      }
   }

   private boolean doorBetween(class_1937 world, class_2338 doorPos, class_1657 player) {
      class_2680 st = world.method_8320(doorPos);
      if (!(st.method_26204() instanceof class_2323)) {
         return false;
      } else {
         class_2350 f = (class_2350)st.method_11654(class_2323.field_10938);
         class_243 c = class_243.method_24953(doorPos);
         double me = (this.knocker.method_23317() - c.field_1352) * (double)f.method_10148() + (this.knocker.method_23321() - c.field_1350) * (double)f.method_10165();
         double her = (player.method_23317() - c.field_1352) * (double)f.method_10148() + (player.method_23321() - c.field_1350) * (double)f.method_10165();
         if (me * her >= (double)0.0F) {
            return false;
         } else if (player.method_5707(c) > (double)256.0F) {
            return false;
         } else {
            return !world.method_8311(player.method_24515().method_10084());
         }
      }
   }

   private void openAndCloseBehind(class_2338 pos) {
      this.setDoor(pos, true);
      this.recentlyOpened.put(pos.method_10062(), this.knocker.tickCounter);
      this.doorToClose = pos.method_10062();
      this.closeTimer = 0;
   }

   public static boolean breakWindowOpening(class_1937 world, class_2338 window) {
      world.method_22352(window, false);
      class_2338 up = window.method_10084();
      class_2338 down = window.method_10074();
      if (isGlass(world.method_8320(up))) {
         world.method_22352(up, false);
         return true;
      } else if (isGlass(world.method_8320(down))) {
         world.method_22352(down, false);
         return true;
      } else {
         return world.method_8320(up).method_26215() || world.method_8320(down).method_26215();
      }
   }

   public static boolean isGlass(class_2680 state) {
      return state.method_26164(class_3481.field_15490) || state.method_26204() instanceof class_2389 && !state.method_27852(class_2246.field_10576);
   }

   private void knock(class_1937 world) {
      if (this.door != null) {
         world.method_8396((class_1657)null, this.door, ModSounds.KNOCK, class_3419.field_15245, 0.9F, 1.0F);
         this.knocker.method_5988().method_20248((double)this.door.method_10263() + (double)0.5F, (double)this.door.method_10264() + (double)1.5F, (double)this.door.method_10260() + (double)0.5F);
         this.knocker.playAction("knock");
         if (this.target != null) {
            KnockerAdvancements.grant(this.target, "knock_knock");
         }

      }
   }

   private void setDoor(class_2338 pos, boolean open) {
      class_1937 world = this.knocker.method_37908();
      class_2680 state = world.method_8320(pos);
      class_2248 var6 = state.method_26204();
      if (var6 instanceof class_2323 doorBlock) {
         doorBlock.method_10033(this.knocker, world, state, pos, open);
      }

   }

   private class_2338 findClosedDoor(class_1937 world, int radius) {
      class_2338 center = this.knocker.method_24515();

      for(int x = -radius; x <= radius; ++x) {
         for(int y = -1; y <= 1; ++y) {
            for(int z = -radius; z <= radius; ++z) {
               class_2338 pos = center.method_10069(x, y, z);
               class_2680 state = world.method_8320(pos);
               if (state.method_26204() instanceof class_2323 && !(Boolean)state.method_11654(class_2323.field_10945)) {
                  if (state.method_11654(class_2323.field_10946) == class_2756.field_12609) {
                     pos = pos.method_10074();
                  }

                  return pos.method_10062();
               }
            }
         }
      }

      return null;
   }

   public static class_2338 findWindow(class_1937 world, class_2338 around, int radius) {
      class_2338 best = null;
      double bestDist = Double.MAX_VALUE;

      for(int x = -radius; x <= radius; ++x) {
         for(int y = -1; y <= 3; ++y) {
            for(int z = -radius; z <= radius; ++z) {
               class_2338 pos = around.method_10069(x, y, z);
               class_2680 state = world.method_8320(pos);
               if (isGlass(state)) {
                  double d = pos.method_10262(around);
                  if (d < bestDist) {
                     bestDist = d;
                     best = pos.method_10062();
                  }
               }
            }
         }
      }

      return best;
   }

   private boolean onCooldown(class_2338 pos) {
      Integer until = (Integer)this.doorCooldown.get(pos);
      if (until == null) {
         return false;
      } else if (until <= this.knocker.tickCounter) {
         this.doorCooldown.remove(pos);
         return false;
      } else {
         return true;
      }
   }

   private void cooldown(class_2338 pos, int ticks) {
      this.doorCooldown.put(pos.method_10062(), this.knocker.tickCounter + ticks);
   }
}
