package com.theneru.knocker.client;

import com.theneru.knocker.net.KnockerActionPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;

public class KnockerActionScreen extends class_437 {
   private static final String[] NAMES = new String[]{"Обнять", "Взять за руку", "Посидеть рядом", "Потанцевать", "Поцеловать в щёку"};

   public KnockerActionScreen() {
      super(class_2561.method_43470("Действия с Кнокером"));
   }

   protected void method_25426() {
      int cx = this.field_22789 / 2;
      int cy = this.field_22790 / 2;
      int bh = 20;
      int bw = 0;

      for(String n : NAMES) {
         bw = Math.max(bw, this.field_22793.method_1727(n) + 14);
      }

      int radius = (int)Math.ceil((double)(bw + 10) / ((double)2.0F * Math.sin(Math.PI / (double)NAMES.length)));
      radius = Math.min(radius, Math.max(40, this.field_22790 / 2 - bh - 6));
      radius = Math.max(radius, 60);

      for(int i = 0; i < NAMES.length; ++i) {
         double a = Math.toRadians((double)-90.0F + (double)i * (double)360.0F / (double)NAMES.length);
         int x = cx + (int)(Math.cos(a) * (double)radius) - bw / 2;
         int y = cy + (int)(Math.sin(a) * (double)radius) - bh / 2;
         final int index = i;
         this.method_37063(class_4185.method_46430(class_2561.method_43470(NAMES[i]), (b) -> {
            ClientPlayNetworking.send(new KnockerActionPayload(index));
            this.method_25419();
         }).method_46434(x, y, bw, bh).method_46431());
      }

   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      super.method_25394(context, mouseX, mouseY, delta);
      String t = "§c♥";
      context.method_51433(this.field_22793, t, (this.field_22789 - this.field_22793.method_1727(t)) / 2, this.field_22790 / 2 - 4, 16777215, true);
   }

   public boolean method_25421() {
      return false;
   }
}
