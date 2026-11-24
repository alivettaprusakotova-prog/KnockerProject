package com.theneru.knocker.client;

import com.theneru.knocker.entity.KnockerEntity;
import net.minecraft.class_1799;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_5617;
import net.minecraft.class_7833;
import net.minecraft.class_811;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class KnockerRenderer extends GeoEntityRenderer<KnockerEntity> {
   public KnockerRenderer(class_5617.class_5618 context) {
      super(context, new KnockerModel());
      this.field_4673 = 0.5F;
      this.addRenderLayer(new AutoGlowingGeoLayer(this));
      this.addRenderLayer(new BlockAndItemGeoLayer<KnockerEntity>(this) {
         protected @Nullable class_1799 getStackForBone(GeoBone bone, KnockerEntity animatable) {
            class_1799 var10000;
            switch (bone.getName()) {
               case "rightItem" -> var10000 = animatable.method_6047();
               case "leftItem" -> var10000 = animatable.method_6079();
               default -> var10000 = null;
            }

            return var10000;
         }

         protected class_811 getTransformTypeForStack(GeoBone bone, class_1799 stack, KnockerEntity animatable) {
            return bone.getName().equals("leftItem") ? class_811.field_4323 : class_811.field_4320;
         }

         protected void renderStackForBone(class_4587 poseStack, GeoBone bone, class_1799 stack, KnockerEntity animatable, class_4597 bufferSource, float partialTick, int packedLight, int packedOverlay) {
            poseStack.method_22907(class_7833.field_40714.rotationDegrees(-90.0F));
            super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
         }
      });
   }
}
