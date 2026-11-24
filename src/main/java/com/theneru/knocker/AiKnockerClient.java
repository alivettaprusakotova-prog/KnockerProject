package com.theneru.knocker;

import com.theneru.knocker.client.KnockerActionScreen;
import com.theneru.knocker.client.KnockerAtmosphere;
import com.theneru.knocker.client.KnockerRenderer;
import com.theneru.knocker.client.ObsessionScreen;
import com.theneru.knocker.entity.KnockerEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.class_1297;
import net.minecraft.class_2561;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675.class_307;

public class AiKnockerClient implements ClientModInitializer {
   private static class_304 obsessionKey;
   private static class_304 actionsKey;

   public void onInitializeClient() {
      EntityRendererRegistry.register(AiKnocker.KNOCKER, KnockerRenderer::new);
      System.out.println("\ud83c\udfa8 AI Knocker зарегистрирован!");
      HudRenderCallback.EVENT.register(KnockerAtmosphere::render);
      obsessionKey = KeyBindingHelper.registerKeyBinding(new class_304("key.aiknocker.obsession", class_307.field_1668, 79, "category.aiknocker"));
      actionsKey = KeyBindingHelper.registerKeyBinding(new class_304("key.aiknocker.actions", class_307.field_1668, 82, "category.aiknocker"));
      ClientTickEvents.END_CLIENT_TICK.register((ClientTickEvents.EndTick)(client) -> {
         if (obsessionKey.method_1436()) {
            this.openObsessionScreen(client);
         }

         if (actionsKey.method_1436() && client.field_1724 != null && client.field_1755 == null) {
            client.method_1507(new KnockerActionScreen());
         }

      });
   }

   private void openObsessionScreen(class_310 client) {
      if (client.field_1724 != null) {
         if (client.field_1687 != null) {
            KnockerEntity knocker = null;
            double best = Double.MAX_VALUE;

            for(class_1297 e : client.field_1687.method_18112()) {
               if (e instanceof KnockerEntity) {
                  KnockerEntity k = (KnockerEntity)e;
                  if (k.method_5805()) {
                     double d = k.method_5858(client.field_1724);
                     if (d < best) {
                        best = d;
                        knocker = k;
                     }
                  }
               }
            }

            if (knocker != null) {
               client.method_1507(new ObsessionScreen(knocker));
            } else {
               client.field_1724.method_7353(class_2561.method_43470("§8...его здесь нет. Но он тебя видит."), true);
            }

         }
      }
   }
}
