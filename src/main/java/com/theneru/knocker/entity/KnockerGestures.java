package com.theneru.knocker.entity;

import com.theneru.knocker.util.KnockerPhase;
import java.util.Random;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_3222;
import net.minecraft.class_3468;
import net.minecraft.class_3532;

public class KnockerGestures {
   public static final int NOD_NONE = 0;
   public static final int NOD_YES = 1;
   public static final int NOD_NO = 2;
   private final KnockerEntity knocker;
   private final Random random = new Random();
   private boolean wasSneaking = false;
   private int sneakCount = 0;
   private int lastSneakTick = -1000;
   private int sneakHeldTicks = 0;
   private int replyCrouches = 0;
   private int replyTimer = 0;
   private int crouchCooldown = 0;
   private final float[] pitches = new float[24];
   private final float[] yaws = new float[24];
   private int sample = 0;
   private int nodCooldown = 0;
   private int nodTimer = 0;
   private int lastJumps = -1;
   private boolean wasSwinging = false;
   private int mimicDelay = -1;
   private boolean mimicJump;
   private boolean mimicSwing;

   public KnockerGestures(KnockerEntity knocker) {
      this.knocker = knocker;
   }

   public void tick(class_1657 player) {
      int now = this.knocker.tickCounter;
      if (this.crouchCooldown > 0) {
         --this.crouchCooldown;
      }

      if (this.nodCooldown > 0) {
         --this.nodCooldown;
      }

      KnockerPhase phase = this.knocker.getPhase();
      double dist = (double)this.knocker.method_5739(player);
      boolean sees = dist < (double)20.0F && this.knocker.method_6057(player);
      this.tickNodAnimation();
      this.tickMimic(player, phase, sees);
      boolean sneaking = player.method_5715();
      if (sneaking && !this.wasSneaking && sees) {
         this.sneakCount = now - this.lastSneakTick < 15 ? this.sneakCount + 1 : 1;
         this.lastSneakTick = now;
      }

      this.wasSneaking = sneaking;
      this.sneakHeldTicks = sneaking ? this.sneakHeldTicks + 1 : 0;
      if (this.sneakCount >= 2 && now - this.lastSneakTick > 12 && this.crouchCooldown <= 0 && this.replyCrouches == 0) {
         int count = Math.min(this.sneakCount, 6);
         this.sneakCount = 0;
         this.crouchCooldown = 60;
         this.onCrouchSeries(player, phase, count);
         this.knocker.getGames().onCrouches(count);
      } else if (now - this.lastSneakTick > 40) {
         this.sneakCount = 0;
      }

      if (this.replyCrouches > 0 && --this.replyTimer <= 0) {
         boolean down = !this.knocker.isCrouchingDown();
         this.knocker.setCrouchingDown(down);
         this.replyTimer = 4;
         if (!down) {
            --this.replyCrouches;
         }
      } else if (this.replyCrouches == 0) {
         boolean mirror = phase.ordinal() >= KnockerPhase.ATTACHED.ordinal() && this.sneakHeldTicks > 30 && dist < (double)10.0F;
         if (this.knocker.isCrouchingDown() != mirror) {
            this.knocker.setCrouchingDown(mirror);
         }
      }

      this.pitches[this.sample % this.pitches.length] = player.method_36455();
      this.yaws[this.sample % this.yaws.length] = player.method_5791();
      ++this.sample;
      if (this.sample >= this.pitches.length && this.nodCooldown <= 0 && sees && !(dist > (double)12.0F) && this.looksAtMe(player)) {
         int yesSwings = this.swings(this.pitches, 8.0F);
         int noSwings = this.swings(this.yaws, 12.0F);
         float pitchRange = this.range(this.pitches);
         float yawRange = this.range(this.yaws);
         if (yesSwings >= 3 && yawRange < pitchRange) {
            this.onNod(player, phase, true);
         } else if (noSwings >= 3 && pitchRange < yawRange) {
            this.onNod(player, phase, false);
         }

      }
   }

   private void onCrouchSeries(class_1657 player, KnockerPhase phase, int count) {
      switch (phase) {
         case WATCHER:
            if (this.random.nextInt(3) == 0) {
               this.startCrouches(1);
            }
            break;
         case CURIOUS:
            this.startCrouches(count);
            if (this.random.nextInt(3) == 0) {
               this.say(player, "?..", "ты... здороваешься?", "так говорят у вас?..");
            }
            break;
         case ATTACHED:
         case LOVE:
            this.startCrouches(count);
            if (this.random.nextInt(3) == 0) {
               this.say(player, "хе-хе", ")", "привет-привет", "я тоже так умею");
            }

            if (phase == KnockerPhase.LOVE && this.random.nextBoolean()) {
               this.knocker.playAction("shy");
            }
            break;
         case OBSESSION:
            this.startCrouches(count + 1);
            if (this.random.nextInt(3) == 0) {
               this.say(player, "Ещё.", "Я повторю всё, что ты делаешь.", "Мы одинаковые.");
            }
            break;
         case PARANOIA:
            this.startCrouches(count * 2);
            if (this.random.nextInt(2) == 0) {
               this.say(player, "ЕЩЁ. ЕЩЁ. ЕЩЁ.", "я повторяю. я всегда повторяю.", "Ты думаешь, это игра?");
            }
      }

      this.knocker.method_5988().method_6226(player, 30.0F, 30.0F);
   }

   private void startCrouches(int n) {
      this.replyCrouches = n;
      this.replyTimer = 6 + this.random.nextInt(6);
   }

   private void onNod(class_1657 player, KnockerPhase phase, boolean yes) {
      this.nodCooldown = 60;
      this.sample = 0;
      if (this.knocker.getChat().getBrain().askedWithin(30000L)) {
         this.knocker.handlePlayerMessage(player, yes ? "да" : "нет");
         this.nod(yes ? 1 : 2);
      } else if (phase != KnockerPhase.WATCHER || !this.random.nextBoolean()) {
         this.nod(yes ? 1 : 2);
         if (this.random.nextInt(4) == 0) {
            if (yes) {
               this.say(player, this.phaseLine(phase, "...", "да?..", "да", "Да.", "ДА."));
            } else {
               this.say(player, this.phaseLine(phase, "...", "нет?..", "почему нет?..", "Не мотай головой.", "НЕ ГОВОРИ МНЕ НЕТ."));
            }
         }

      }
   }

   public void nod(int type) {
      this.knocker.setNod(type);
      this.nodTimer = 30;
   }

   private void tickNodAnimation() {
      if (this.nodTimer > 0 && --this.nodTimer == 0) {
         this.knocker.setNod(0);
      }

   }

   private boolean looksAtMe(class_1657 player) {
      return looksAt(player, this.knocker.method_33571(), 0.8);
   }

   public static boolean looksAt(class_1657 player, class_243 point, double minDot) {
      class_243 look = player.method_5828(1.0F);
      class_243 to = point.method_1020(player.method_33571()).method_1029();
      return look.method_1026(to) > minDot;
   }

   private void tickMimic(class_1657 player, KnockerPhase phase, boolean sees) {
      if (phase.ordinal() >= KnockerPhase.CURIOUS.ordinal()) {
         boolean paranoia = phase == KnockerPhase.PARANOIA;
         boolean watching = paranoia ? this.knocker.method_5739(player) < 24.0F : sees && looksAt(player, this.knocker.method_33571(), 0.6);
         if (player instanceof class_3222) {
            class_3222 sp = (class_3222)player;
            int jumps = sp.method_14248().method_15024(class_3468.field_15419, class_3468.field_15428);
            if (this.lastJumps >= 0 && jumps > this.lastJumps && watching && this.random.nextInt(paranoia ? 1 : 2) == 0) {
               this.mimicJump = true;
            }

            this.lastJumps = jumps;
         }

         boolean swinging = player.field_6252;
         if (swinging && !this.wasSwinging && watching && this.random.nextInt(paranoia ? 1 : 3) == 0) {
            this.mimicSwing = true;
         }

         this.wasSwinging = swinging;
         if ((this.mimicJump || this.mimicSwing) && this.mimicDelay < 0) {
            this.mimicDelay = paranoia ? 2 : 6 + this.random.nextInt(6);
         }

         if (this.mimicDelay >= 0 && this.mimicDelay-- == 0) {
            if (this.mimicJump) {
               this.knocker.mimicJump();
            }

            if (this.mimicSwing) {
               this.knocker.method_6104(class_1268.field_5808);
            }

            this.mimicJump = this.mimicSwing = false;
            this.mimicDelay = -1;
         }

      }
   }

   private int swings(float[] values, float minSwing) {
      int start = this.sample % values.length;
      int swings = 0;
      int dir = 0;
      float extreme = values[start];

      for(int i = 1; i < values.length; ++i) {
         float v = values[(start + i) % values.length];
         float d = class_3532.method_15393(v - extreme);
         if (dir >= 0 && d < -minSwing) {
            if (dir > 0) {
               ++swings;
            }

            dir = -1;
            extreme = v;
         } else if (dir <= 0 && d > minSwing) {
            if (dir < 0) {
               ++swings;
            }

            dir = 1;
            extreme = v;
         } else if (dir > 0 && d > 0.0F || dir < 0 && d < 0.0F) {
            extreme = v;
         }
      }

      return swings;
   }

   private float range(float[] values) {
      float min = Float.MAX_VALUE;
      float max = -Float.MAX_VALUE;
      float base = values[0];

      for(float v : values) {
         float d = class_3532.method_15393(v - base);
         min = Math.min(min, d);
         max = Math.max(max, d);
      }

      return max - min;
   }

   private String phaseLine(KnockerPhase phase, String... byGroup) {
      String var10000;
      switch (phase) {
         case WATCHER:
            var10000 = byGroup[0];
            break;
         case CURIOUS:
            var10000 = byGroup[1];
            break;
         case ATTACHED:
         case LOVE:
            var10000 = byGroup[2];
            break;
         case OBSESSION:
            var10000 = byGroup[3];
            break;
         case PARANOIA:
            var10000 = byGroup[4];
            break;
         default:
            throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   private void say(class_1657 player, String... lines) {
      if (this.knocker.getChat().canSpeakUnprompted(100)) {
         this.knocker.getChat().say(player, this.knocker.getChat().getBrain().pick(lines));
      }
   }
}
