package com.theneru.knocker.client;

import com.theneru.knocker.entity.KnockerEntity;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public class KnockerModel extends GeoModel<KnockerEntity> {
   private static final class_2960 MODEL = class_2960.method_60655("aiknocker", "geo/entity/knocker.geo.json");
   private static final class_2960 ANIMATIONS = class_2960.method_60655("aiknocker", "animations/entity/knocker.animation.json");
   private static final class_2960 DEFAULT = tex("knocker");
   private static final class_2960 ANGRY = tex("knocker_angry");
   private static final class_2960 THEFT = tex("knocker_theft");
   private static final class_2960 PYRO = tex("knocker_pyro");
   private static final class_2960 PSY = tex("knocker_psy");
   private static final float CROUCH_TILT = -0.45F;

   private static class_2960 tex(String name) {
      return class_2960.method_60655("aiknocker", "textures/entity/" + name + ".png");
   }

   public class_2960 getModelResource(KnockerEntity animatable) {
      return MODEL;
   }

   public class_2960 getTextureResource(KnockerEntity animatable) {
      class_2960 var10000;
      switch (animatable.getLook()) {
         case DEFAULT -> var10000 = DEFAULT;
         case ANGRY -> var10000 = ANGRY;
         case THEFT -> var10000 = THEFT;
         case PYRO -> var10000 = PYRO;
         case PSY -> var10000 = PSY;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public class_2960 getAnimationResource(KnockerEntity animatable) {
      return ANIMATIONS;
   }

   public void setCustomAnimations(KnockerEntity animatable, long instanceId, AnimationState<KnockerEntity> animationState) {
      super.setCustomAnimations(animatable, instanceId, animationState);
      GeoBone head = this.getAnimationProcessor().getBone("head");
      if (head != null && !animatable.method_29504()) {
         float yaw = class_3532.method_15393(animatable.method_5791() - animatable.method_43078());
         float pitch = animatable.method_36455();
         head.setRotY(head.getRotY() + yaw * ((float)Math.PI / 180F));
         head.setRotX(head.getRotX() + pitch * ((float)Math.PI / 180F));
         float t = (float)animatable.field_6012 + animationState.getPartialTick();
         int nod = animatable.getNod();
         if (nod == 1) {
            head.setRotX(head.getRotX() + class_3532.method_15374(t * 0.7F) * 0.35F);
         } else if (nod == 2) {
            head.setRotY(head.getRotY() + class_3532.method_15374(t * 0.7F) * 0.45F);
         }

         if (animatable.isCrouchingDown()) {
            GeoBone waist = this.getAnimationProcessor().getBone("waist");
            if (waist != null) {
               waist.setRotX(waist.getRotX() + -0.45F);
            }

            head.setRotX(head.getRotX() - -0.45F);
         }

      }
   }
}
