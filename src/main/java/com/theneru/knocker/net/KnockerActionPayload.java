package com.theneru.knocker.net;

import net.minecraft.class_2960;
import net.minecraft.class_8710;
import net.minecraft.class_9129;
import net.minecraft.class_9135;
import net.minecraft.class_9139;

public record KnockerActionPayload(int action) implements class_8710 {
   public static final class_8710.class_9154<KnockerActionPayload> ID = new class_8710.class_9154(class_2960.method_60655("aiknocker", "action"));
   public static final class_9139<class_9129, KnockerActionPayload> CODEC;

   public class_8710.class_9154<? extends class_8710> method_56479() {
      return ID;
   }

   static {
      CODEC = class_9139.method_56434(class_9135.field_49675, KnockerActionPayload::action, KnockerActionPayload::new);
   }
}
