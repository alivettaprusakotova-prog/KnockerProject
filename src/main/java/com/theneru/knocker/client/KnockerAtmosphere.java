package com.theneru.knocker.client;

import com.theneru.knocker.KnockerConfig;
import com.theneru.knocker.entity.KnockerEntity;
import com.theneru.knocker.entity.KnockerMood;
import com.theneru.knocker.util.KnockerPhase;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_638;
import net.minecraft.class_9779;

public final class KnockerAtmosphere {
   private static float gray = 0.0F;
   private static float red = 0.0F;
   private static float sky = 0.0F;

   private KnockerAtmosphere() {
   }

   public static void render(class_332 ctx, class_9779 counter) {
      if (KnockerConfig.atmosphere) {
         class_310 mc = class_310.method_1551();
         class_638 world = mc.field_1687;
         if (world != null && mc.field_1724 != null) {
            float delta = counter.method_60637(false);
            boolean open = world.method_8311(mc.field_1724.method_24515().method_10084());
            sky = approach(sky, open ? 1.0F : 0.0F, 0.02F);
            float rain = world.method_8430(delta);
            float thunder = world.method_8478(delta);
            float grayTarget = (rain * 0.7F + thunder * 0.3F) * sky;
            gray = approach(gray, grayTarget, 0.01F);
            float redTarget = 0.0F;
            KnockerEntity k = nearest(mc, (double)64.0F);
            if (k != null) {
               KnockerPhase ph = k.getPhase();
               KnockerMood.Mood mood = k.getMoodClient();
               if (ph == KnockerPhase.PARANOIA) {
                  redTarget = KnockerEntity.isNightTime(world) ? 0.55F : 0.35F;
               } else if (ph == KnockerPhase.OBSESSION && KnockerEntity.isNightTime(world)) {
                  redTarget = 0.25F;
               }

               if (mood == KnockerMood.Mood.ANGRY) {
                  redTarget = Math.max(redTarget, 0.7F);
               } else if (mood == KnockerMood.Mood.SULK || mood == KnockerMood.Mood.JEALOUS) {
                  redTarget = Math.max(redTarget, 0.45F);
               }
            }

            red = approach(red, redTarget, 0.008F);
            int w = ctx.method_51421();
            int h = ctx.method_51443();
            if (gray > 0.01F) {
               int a = (int)(gray * 60.0F);
               ctx.method_25294(0, 0, w, h, argb(a, 96, 102, 114));
               if (thunder > 0.01F) {
                  ctx.method_25294(0, 0, w, h, argb((int)(thunder * sky * 35.0F), 10, 14, 26));
               }
            }

            if (red > 0.01F) {
               int rings = 22;
               int step = Math.max(1, Math.min(w, h) / 70);

               for(int i = 0; i < rings; ++i) {
                  float f = 1.0F - (float)i / (float)rings;
                  int a = (int)(red * 70.0F * f * f);
                  if (a > 0) {
                     int c = argb(a, 120, 0, 8);
                     int o = i * step;
                     ctx.method_25294(o, o, w - o, o + step, c);
                     ctx.method_25294(o, h - o - step, w - o, h - o, c);
                     ctx.method_25294(o, o + step, o + step, h - o - step, c);
                     ctx.method_25294(w - o - step, o + step, w - o, h - o - step, c);
                  }
               }
            }

         }
      }
   }

   private static KnockerEntity nearest(class_310 mc, double radius) {
      KnockerEntity best = null;
      double bd = radius * radius;

      for(class_1297 e : mc.field_1687.method_18112()) {
         if (e instanceof KnockerEntity k) {
            if (k.method_5805()) {
               double d = k.method_5858(mc.field_1724);
               if (d < bd) {
                  bd = d;
                  best = k;
               }
            }
         }
      }

      return best;
   }

   private static float approach(float cur, float target, float speed) {
      return cur < target ? Math.min(target, cur + speed) : Math.max(target, cur - speed);
   }

   private static int argb(int a, int r, int g, int b) {
      a = Math.max(0, Math.min(255, a));
      return a << 24 | r << 16 | g << 8 | b;
   }
}
