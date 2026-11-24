package com.theneru.knocker;

import net.minecraft.class_2378;
import net.minecraft.class_2960;
import net.minecraft.class_3414;
import net.minecraft.class_7923;

public final class ModSounds {
   public static final class_3414 WHISPER = register("whisper");
   public static final class_3414 BREATH = register("breath");
   public static final class_3414 KNOCK = register("knock");
   public static final class_3414 HEARTBEAT = register("heartbeat");
   public static final class_3414 LAUGH = register("laugh");
   public static final class_3414 LAUGH_DISTORTED = register("laugh_distorted");
   public static final class_3414 SCREAM = register("scream");
   public static final class_3414 STINGER = register("stinger");
   public static final class_3414 GLITCH = register("glitch");
   public static final class_3414 STEP = register("step");
   public static final class_3414 UNDERWATER = register("underwater");

   private ModSounds() {
   }

   private static class_3414 register(String name) {
      class_2960 id = class_2960.method_60655("aiknocker", name);
      return (class_3414)class_2378.method_10230(class_7923.field_41172, id, class_3414.method_47908(id));
   }

   public static void init() {
   }
}
