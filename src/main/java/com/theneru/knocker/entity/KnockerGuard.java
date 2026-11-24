package com.theneru.knocker.entity;

import com.theneru.knocker.ModSounds;
import com.theneru.knocker.util.KnockerMessages;
import com.theneru.knocker.util.KnockerPhase;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1321;
import net.minecraft.class_1324;
import net.minecraft.class_1569;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_5134;

public class KnockerGuard {
   private final KnockerEntity knocker;
   private final KnockerMovement movement;
   private class_1309 enemy = null;
   private int enemyTimer = 0;
   private int killLineCooldown = 0;
   private KnockerPhase lastPhase = null;
   private int stuckTicks = 0;
   private final Map<UUID, Integer> unreachable = new HashMap();
   private boolean revenge = false;
   private boolean askedForHelp = false;

   public KnockerGuard(KnockerEntity knocker, KnockerMovement movement) {
      this.knocker = knocker;
      this.movement = movement;
   }

   public void revenge(class_1309 killer) {
      this.enemy = killer;
      this.enemyTimer = 1200;
      this.revenge = true;
      this.askedForHelp = false;
      this.knocker.method_5980(killer);
      this.movement.teleportTo(killer.method_19538().method_1031((double)1.0F, (double)0.0F, (double)0.0F));
   }

   public boolean isFighting() {
      return this.enemy != null && this.enemy.method_5805();
   }

   public void tick(class_1657 player) {
      KnockerPhase phase = this.knocker.getPhase();
      this.updateStats(phase);
      if (this.killLineCooldown > 0) {
         --this.killLineCooldown;
      }

      if (this.enemy != null) {
         if (!this.enemy.method_5805()) {
            this.onKilled(player, phase);
            this.stop();
         } else if (--this.enemyTimer <= 0 || !this.revenge && this.enemy.method_5858(player) > (double)1600.0F) {
            this.giveUp();
         }
      }

      if (this.enemy == null && this.knocker.tickCounter % 10 == 0) {
         class_1309 found = null;
         if (phase.ordinal() >= KnockerPhase.OBSESSION.ordinal()) {
            found = this.attackerOf(player);
         }

         if (found == null && phase == KnockerPhase.PARANOIA) {
            found = this.nearest(player, (double)10.0F, (e) -> isThreat(e, player) && !isPet(e));
         }

         if (found != null) {
            this.engage(player, found);
         }
      }

      if (this.enemy != null) {
         this.knocker.method_5980(this.enemy);
         double d = this.knocker.method_5858(this.enemy);
         if (d > (double)144.0F) {
            if (this.revenge) {
               this.movement.teleportTo(this.enemy.method_19538().method_1031((double)1.0F, (double)0.0F, (double)0.0F));
            } else {
               this.jumpTo(this.enemy, player);
            }
         }

         if (d > (double)12.25F) {
            ++this.stuckTicks;
         } else {
            this.stuckTicks = 0;
         }

         if (this.stuckTicks == 60) {
            this.jumpTo(this.enemy, player);
         }

         if (this.stuckTicks > 140) {
            this.giveUp();
         }
      }

      if (this.knocker.tickCounter % 200 == 0) {
         this.unreachable.values().removeIf((t) -> t < this.knocker.tickCounter);
      }

   }

   public boolean help(class_1657 player) {
      class_1309 target = this.attackerOf(player);
      if (target == null) {
         target = this.nearest(player, (double)16.0F, (e) -> e instanceof class_1569);
      }

      if (target == null) {
         this.movement.teleportBehind(player);
         return false;
      } else {
         this.engage(player, target);
         this.askedForHelp = true;
         this.jumpTo(target, player);
         return true;
      }
   }

   private void engage(class_1657 player, class_1309 target) {
      this.askedForHelp = false;
      this.enemy = target;
      this.enemyTimer = 600;
      this.knocker.method_5980(target);
   }

   private void stop() {
      this.revenge = false;
      this.enemy = null;
      this.stuckTicks = 0;
      this.knocker.method_5980((class_1309)null);
      this.knocker.method_5942().method_6340();
   }

   private void giveUp() {
      if (this.enemy != null) {
         this.unreachable.put(this.enemy.method_5667(), this.knocker.tickCounter + 1200);
      }

      this.stop();
   }

   private static boolean isThreat(class_1309 e, class_1657 player) {
      if (e instanceof class_1569) {
         return true;
      } else {
         boolean var10000;
         if (e instanceof class_1308) {
            class_1308 mob = (class_1308)e;
            if (mob.method_5968() == player) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      }
   }

   private void onKilled(class_1657 player, KnockerPhase phase) {
      if (this.revenge) {
         this.knocker.rememberEvent("я убил того, кто убил её");
         this.knocker.getChat().say(player, phase == KnockerPhase.PARANOIA ? "ОН МЁРТВ. ОН БОЛЬШЕ НИКОГДА." : "Он больше никого не тронет.");
      } else {
         if (this.askedForHelp) {
            this.askedForHelp = false;
            this.knocker.addObsession(5);
            this.knocker.rememberEvent("она попросила о помощи, и я убил того, кто ей угрожал");
         }

         String line = KnockerMessages.killLine(phase);
         if (line != null && this.killLineCooldown <= 0 && this.knocker.method_59922().method_43048(10) < 4) {
            this.killLineCooldown = 3600;
            this.knocker.getChat().say(player, line);
            if (phase == KnockerPhase.PARANOIA && this.knocker.method_59922().method_43048(2) == 0) {
               this.knocker.playSoundFor(player, ModSounds.LAUGH_DISTORTED, 0.6F, 1.0F);
            }
         }

      }
   }

   private void jumpTo(class_1309 target, class_1657 player) {
      class_243 toPlayer = player.method_19538().method_1020(target.method_19538());
      class_243 dir = toPlayer.method_1027() > 0.01 ? toPlayer.method_1029() : new class_243((double)1.0F, (double)0.0F, (double)0.0F);
      this.movement.teleportTo(target.method_19538().method_1019(dir.method_1021((double)1.5F)));
   }

   private class_1309 attackerOf(class_1657 player) {
      class_1309 attacker = player.method_6065();
      if (attacker != null && attacker != this.knocker && !(attacker instanceof KnockerEntity) && attacker.method_5805()) {
         if (this.unreachable.containsKey(attacker.method_5667())) {
            return null;
         } else {
            return attacker.method_5858(player) > (double)576.0F ? null : attacker;
         }
      } else {
         return null;
      }
   }

   private class_1309 nearest(class_1657 player, double radius, Predicate<class_1309> filter) {
      List<class_1309> list = this.knocker.method_37908().method_8390(class_1309.class, player.method_5829().method_1014(radius), (ex) -> ex != this.knocker && ex != player && ex.method_5805() && !(ex instanceof KnockerEntity) && !(ex instanceof class_1657) && filter.test(ex) && !this.unreachable.containsKey(ex.method_5667()) && !ex.method_5767());
      class_1309 best = null;
      double bestDist = Double.MAX_VALUE;

      for(class_1309 e : list) {
         double d = e.method_5858(player);
         if (d < bestDist) {
            bestDist = d;
            best = e;
         }
      }

      return best;
   }

   private static boolean isPet(class_1309 e) {
      boolean var10000;
      if (e instanceof class_1321 pet) {
         if (pet.method_6181()) {
            var10000 = true;
            return var10000;
         }
      }

      var10000 = false;
      return var10000;
   }

   private void updateStats(KnockerPhase phase) {
      if (phase != this.lastPhase) {
         this.lastPhase = phase;
         double var10000;
         switch (phase) {
            case WATCHER:
            case CURIOUS:
               var10000 = (double)2.0F;
               break;
            case ATTACHED:
               var10000 = (double)6.0F;
               break;
            case LOVE:
               var10000 = (double)8.0F;
               break;
            case OBSESSION:
               var10000 = (double)14.0F;
               break;
            case PARANOIA:
               var10000 = (double)20.0F;
               break;
            default:
               throw new MatchException((String)null, (Throwable)null);
         }

         double damage = var10000;
         class_1324 attack = this.knocker.method_5996(class_5134.field_23721);
         if (attack != null) {
            attack.method_6192(damage);
         }

         switch (phase) {
            case WATCHER:
            case CURIOUS:
               var10000 = (double)40.0F;
               break;
            case ATTACHED:
               var10000 = (double)60.0F;
               break;
            case LOVE:
               var10000 = (double)80.0F;
               break;
            case OBSESSION:
               var10000 = (double)120.0F;
               break;
            case PARANOIA:
               var10000 = (double)200.0F;
               break;
            default:
               throw new MatchException((String)null, (Throwable)null);
         }

         double maxHealth = var10000;
         class_1324 health = this.knocker.method_5996(class_5134.field_23716);
         if (health != null && health.method_6201() != maxHealth) {
            boolean grew = maxHealth > health.method_6201();
            health.method_6192(maxHealth);
            if (grew) {
               this.knocker.method_6033((float)maxHealth);
            } else if ((double)this.knocker.method_6032() > maxHealth) {
               this.knocker.method_6033((float)maxHealth);
            }
         }

         class_1324 armor = this.knocker.method_5996(class_5134.field_23724);
         if (armor != null) {
            armor.method_6192(phase.ordinal() >= KnockerPhase.OBSESSION.ordinal() ? (double)12.0F : (double)6.0F);
         }

      }
   }
}
