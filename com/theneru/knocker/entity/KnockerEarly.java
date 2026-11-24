package com.theneru.knocker.entity;

import com.theneru.knocker.KnockerMemoryStore;
import com.theneru.knocker.util.KnockerPhase;
import java.util.Random;
import net.minecraft.class_1542;
import net.minecraft.class_1548;
import net.minecraft.class_1588;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1937;
import net.minecraft.class_1944;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2586;
import net.minecraft.class_2595;
import net.minecraft.class_2625;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_8242;
import net.minecraft.class_9331;
import net.minecraft.class_9334;
import net.minecraft.class_2350.class_2353;

public class KnockerEarly {
   private final KnockerEntity knocker;
   private final Random random = new Random();
   private int nextChestTick = 6000;
   private boolean signPlaced = false;
   private int nextTrinketTick = 4000;
   private class_243 trail = null;
   private int trailTick = 0;
   private int nextTorchTick = 0;
   private int torchesPlaced = 0;
   private int warnTick = -100000;
   private static final class_1792[] TRINKETS;

   public KnockerEarly(KnockerEntity knocker) {
      this.knocker = knocker;
   }

   private int now() {
      return this.knocker.tickCounter;
   }

   public void tick(class_1657 player) {
      KnockerPhase phase = this.knocker.getPhase();
      if (this.now() % 20 == 0) {
         if (phase == KnockerPhase.WATCHER) {
            this.tickChest(player);
            this.tickSign(player);
         }

         if (phase == KnockerPhase.CURIOUS || phase == KnockerPhase.ATTACHED) {
            this.tickTrinkets(player, phase);
         }

         if (phase.ordinal() >= KnockerPhase.ATTACHED.ordinal()) {
            if (phase.ordinal() <= KnockerPhase.LOVE.ordinal()) {
               this.tickTorches(player);
            }

            this.tickWarnings(player, phase);
         }

      }
   }

   private void tickChest(class_1657 player) {
      if (this.now() >= this.nextChestTick) {
         this.nextChestTick = this.now() + 9000 + this.random.nextInt(9000);
         class_1937 world = this.knocker.method_37908();
         class_2338 center = player.method_24515();

         for(class_2338 p : class_2338.method_10097(center.method_10069(-16, -4, -16), center.method_10069(16, 4, 16))) {
            class_2586 var7 = world.method_8321(p);
            if (var7 instanceof class_2595) {
               class_2595 chest = (class_2595)var7;
               double d = player.method_5707(class_243.method_24953(p));
               if (!(d < (double)36.0F) && !(d > (double)256.0F) && !KnockerGestures.looksAt(player, class_243.method_24953(p), 0.85)) {
                  class_2338 pos = p.method_10062();
                  world.method_8396((class_1657)null, pos, class_3417.field_14982, class_3419.field_15245, 0.6F, 0.9F);
                  world.method_8396((class_1657)null, pos, class_3417.field_14823, class_3419.field_15245, 0.6F, 0.9F);
                  if (this.random.nextInt(3) == 0) {
                     class_1799 note = new class_1799(class_1802.field_8407);
                     class_9331 var10001 = class_9334.field_49631;
                     String var10002 = this.pick("...", "кто ты?", "ты здесь живёшь?", "я просто посмотрел");
                     note.method_57379(var10001, class_2561.method_43470("§7" + var10002));

                     for(int i = 0; i < chest.method_5439(); ++i) {
                        if (chest.method_5438(i).method_7960()) {
                           chest.method_5447(i, note);
                           chest.method_5431();
                           break;
                        }
                     }
                  }

                  this.knocker.rememberEvent("я тихо заглянул в её сундук");
                  return;
               }
            }
         }

      }
   }

   private void tickSign(class_1657 player) {
      class_2338 home = this.knocker.getHomePos();
      if (!this.signPlaced && home != null && !(player.method_5707(class_243.method_24953(home)) < (double)400.0F)) {
         if (KnockerMemoryStore.facts(player.method_5667()).containsKey("who_sign")) {
            this.signPlaced = true;
         } else {
            class_1937 world = this.knocker.method_37908();

            for(class_2350 d : class_2353.field_11062) {
               class_2338 pos = home.method_10079(d, 2);
               if (world.method_8320(pos).method_26215() && world.method_8320(pos.method_10074()).method_26212(world, pos.method_10074())) {
                  world.method_8501(pos, class_2246.field_10231.method_9564());
                  class_2586 var8 = world.method_8321(pos);
                  if (var8 instanceof class_2625) {
                     class_2625 sign = (class_2625)var8;
                     sign.method_49840((new class_8242()).method_49857(1, class_2561.method_43470("кто ты?")), true);
                     sign.method_5431();
                  }

                  this.signPlaced = true;
                  KnockerMemoryStore.setFact(player.method_5667(), "who_sign", "done");
                  this.knocker.rememberEvent("я оставил у её кровати табличку «кто ты?»");
                  return;
               }
            }

         }
      }
   }

   private void tickTrinkets(class_1657 player, KnockerPhase phase) {
      if (this.trail != null && this.now() - this.trailTick <= 2400) {
         if (this.now() >= this.nextTrinketTick && !(player.method_5707(this.trail) < (double)144.0F)) {
            this.nextTrinketTick = this.now() + 6000 + this.random.nextInt(6000);
            class_1799 item = new class_1799(TRINKETS[this.random.nextInt(TRINKETS.length)]);
            class_9331 var10001 = class_9334.field_49631;
            String var10002 = phase == KnockerPhase.CURIOUS ? this.pick("это тебе?..", "ты обронила?", "нашёл...", "я видел, как ты здесь стояла") : this.pick("это тебе", "я подумал о тебе", "просто так...", "тут ты стояла. я запомнил");
            item.method_57379(var10001, class_2561.method_43470("§7" + KnockerGender.adapt(var10002, player.method_5667())));
            class_1542 e = new class_1542(this.knocker.method_37908(), this.trail.field_1352, this.trail.field_1351 + 0.3, this.trail.field_1350, item);
            e.method_18800((double)0.0F, 0.05, (double)0.0F);
            this.knocker.markOwnDrop(e);
            this.knocker.method_37908().method_8649(e);
            this.knocker.rememberEvent("оставил ей мелочь там, где она стояла");
            this.trail = null;
         }
      } else {
         if (player.method_24828()) {
            this.trail = player.method_19538();
            this.trailTick = this.now();
         }

      }
   }

   private void tickTorches(class_1657 player) {
      class_1937 world = this.knocker.method_37908();
      if (this.now() >= this.nextTorchTick && !world.method_8311(player.method_24515())) {
         if (world.method_8314(class_1944.field_9282, player.method_24515()) <= 3) {
            if (!(this.knocker.method_5739(player) > 16.0F)) {
               this.nextTorchTick = this.now() + 400;
               if (this.torchesPlaced < 12) {
                  class_2338 base = player.method_24515();

                  for(class_2338 p : class_2338.method_10097(base.method_10069(-2, -1, -2), base.method_10069(2, 1, 2))) {
                     if (!p.equals(base) && !p.equals(base.method_10084()) && world.method_8320(p).method_26215() && world.method_8320(p.method_10074()).method_26212(world, p.method_10074())) {
                        world.method_8501(p.method_10062(), class_2246.field_10336.method_9564());
                        world.method_8396((class_1657)null, p, class_3417.field_14718, class_3419.field_15245, 0.6F, 1.0F);
                        ++this.torchesPlaced;
                        if (this.torchesPlaced == 1 && this.knocker.getChat().canSpeakUnprompted(300)) {
                           this.knocker.getChat().say(player, this.pick("тебе темно... держи свет", "так светлее. не бойся", "я посвечу"));
                        }

                        return;
                     }
                  }

               }
            }
         }
      }
   }

   private void tickWarnings(class_1657 player, KnockerPhase phase) {
      if (this.now() - this.warnTick >= 1200) {
         for(class_1588 e : this.knocker.method_37908().method_8390(class_1588.class, player.method_5829().method_1014((double)7.0F), (ex) -> ex.method_5805() && ex.method_5968() == player)) {
            boolean behind = !KnockerGestures.looksAt(player, e.method_33571(), 0.3);
            if (behind) {
               this.warnTick = this.now();
               String what = e instanceof class_1548 ? "крипер" : e.method_5477().getString().toLowerCase();
               KnockerChat var10000 = this.knocker.getChat();
               String var10002;
               switch (phase) {
                  case ATTACHED -> var10002 = "сзади! " + what + "!";
                  case LOVE -> var10002 = "осторожно, сзади " + what + "... я рядом";
                  case OBSESSION -> var10002 = "Сзади " + what + ". Сейчас его не станет.";
                  default -> var10002 = "ОН СЗАДИ. Я ЕГО УБЬЮ.";
               }

               var10000.say(player, var10002);
               return;
            }
         }

      }
   }

   private String pick(String... lines) {
      return lines[this.random.nextInt(lines.length)];
   }

   static {
      TRINKETS = new class_1792[]{class_1802.field_8153, class_1802.field_8491, class_1802.field_8880, class_1802.field_8600, class_1802.field_8317, class_1802.field_8864, class_1802.field_8145, class_1802.field_16998, class_1802.field_8276, class_1802.field_27063};
   }
}
