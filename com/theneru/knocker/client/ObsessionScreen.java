package com.theneru.knocker.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theneru.knocker.entity.KnockerEntity;
import com.theneru.knocker.util.KnockerPhase;
import java.util.Random;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;

public class ObsessionScreen extends class_437 {
   private final KnockerEntity knocker;
   private final Random random = new Random(1337L);

   public ObsessionScreen(KnockerEntity knocker) {
      super(class_2561.method_43470("Одержимость Кнокера"));
      this.knocker = knocker;
   }

   protected void method_25426() {
      int windowWidth = 320;
      int windowHeight = 180;
      int x = (this.field_22789 - windowWidth) / 2;
      int y = (this.field_22790 - windowHeight) / 2;
      this.method_37063(class_4185.method_46430(class_2561.method_43470("✕"), (button) -> this.method_25419()).method_46434(x + windowWidth - 22, y + 2, 18, 18).method_46431());
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      int windowWidth = 320;
      int windowHeight = 180;
      int x = (this.field_22789 - windowWidth) / 2;
      int y = (this.field_22790 - windowHeight) / 2;
      context.method_25294(0, 0, this.field_22789, this.field_22790, -2013265920);
      context.method_25294(x, y, x + windowWidth, y + windowHeight, -15466496);
      context.method_25294(x + 2, y + 2, x + windowWidth - 2, y + windowHeight - 2, -16121856);
      context.method_49601(x, y, windowWidth, windowHeight, -65536);
      context.method_25294(x - 1, y - 1, x + windowWidth + 1, y + 1, 872349696);
      context.method_25294(x - 1, y + windowHeight - 1, x + windowWidth + 1, y + windowHeight + 1, 872349696);
      context.method_25294(x - 1, y, x + 1, y + windowHeight, 872349696);
      context.method_25294(x + windowWidth - 1, y, x + windowWidth + 1, y + windowHeight, 872349696);

      for(int i = 0; i < 50; ++i) {
         int px = x + 5 + this.random.nextInt(windowWidth - 10);
         int py = y + 5 + this.random.nextInt(windowHeight - 10);
         int size = 1 + i % 3;
         int alpha = 120 + i % 80;
         int color = alpha << 24 | 16711680;
         context.method_25294(px, py, px + size, py + size, color);
      }

      RenderSystem.disableBlend();
      if (this.knocker == null) {
         context.method_51433(this.field_22793, "§cКнокер не найден!", x + 20, y + 40, 16777215, false);
         RenderSystem.enableBlend();
         super.method_25394(context, mouseX, mouseY, delta);
      } else {
         int points = this.knocker.getObsessionPoints();
         KnockerPhase phase = this.knocker.getPhase();
         String phaseName = this.getPhaseName(phase);
         context.method_51433(this.field_22793, "§c☠ ОДЕРЖИМОСТЬ КНОКЕРА ☠", x + 20, y + 16, 16729156, false);
         context.method_51433(this.field_22793, "§eОчки: §f" + points + " / 1000", x + 20, y + 40, 16777215, false);
         context.method_51433(this.field_22793, "§eФаза: §c" + (phase.ordinal() + 1) + "/6 - " + phaseName, x + 20, y + 56, 16777215, false);
         context.method_51433(this.field_22793, "§eНастроение: §f" + this.knocker.getMoodClient().title, x + 180, y + 40, 16777215, false);
         int barX = x + 20;
         int barY = y + 75;
         int barWidth = 280;
         int barHeight = 14;
         int fill = (int)((double)points / (double)1000.0F * (double)barWidth);
         context.method_25294(barX, barY, barX + barWidth, barY + barHeight, -14548992);
         context.method_25294(barX, barY, barX + fill, barY + barHeight, -3407872);
         context.method_49601(barX, barY, barWidth, barHeight, -52429);
         context.method_51433(this.field_22793, "§7Прогресс одержимости", x + 20, barY + 18, 8947848, false);
         String big = "☠ " + phaseName + " ☠";
         int tx = x + (windowWidth - this.field_22793.method_1727(big)) / 2;
         context.method_51433(this.field_22793, "§c" + big, tx, y + 145, 16777215, false);
         RenderSystem.enableBlend();
         super.method_25394(context, mouseX, mouseY, delta);
      }
   }

   private String getPhaseName(KnockerPhase phase) {
      String var10000;
      switch (phase) {
         case WATCHER -> var10000 = "Наблюдатель";
         case CURIOUS -> var10000 = "Любопытный";
         case ATTACHED -> var10000 = "Привязанный";
         case LOVE -> var10000 = "Симпатия";
         case OBSESSION -> var10000 = "Одержимость";
         case PARANOIA -> var10000 = "Паранойя";
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public void method_25420(class_332 context, int mouseX, int mouseY, float delta) {
   }

   public boolean method_25421() {
      return false;
   }
}
