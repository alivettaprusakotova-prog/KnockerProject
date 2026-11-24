package com.theneru.knocker;

import net.minecraft.class_1657;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_8779;
import net.minecraft.server.MinecraftServer;

public final class KnockerAdvancements {
   private KnockerAdvancements() {
   }

   public static void grant(class_1657 player, String id) {
      if (player instanceof class_3222 sp) {
         MinecraftServer server = sp.method_5682();
         if (server != null) {
            for(String parent : new String[]{"root", "first_contact"}) {
               class_8779 e = server.method_3851().method_12896(class_2960.method_60655("aiknocker", parent));
               if (e != null) {
                  sp.method_14236().method_12878(e, "trigger");
               }
            }

            class_8779 adv = server.method_3851().method_12896(class_2960.method_60655("aiknocker", id));
            if (adv != null) {
               sp.method_14236().method_12878(adv, "trigger");
            }

         }
      }
   }

   public static void phase(class_1657 player, int phaseOrdinal) {
      String[] ids = new String[]{null, "chosen", "attached", "love", "obsession", "paranoia"};

      for(int i = 1; i <= phaseOrdinal && i < ids.length; ++i) {
         grant(player, ids[i]);
      }

   }
}
