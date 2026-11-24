package com.theneru.knocker;

import java.util.Set;
import net.minecraft.class_1743;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1829;
import net.minecraft.class_1835;
import net.minecraft.class_3489;
import net.minecraft.class_9334;

public class KnockerGiftUtils {
   private static final Set<class_1792> FAVORITE_FOOD;
   private static final Set<class_1792> HATED_FOOD;
   private static final Set<class_1792> FAVORITE_VALUABLE;
   private static final Set<class_1792> VALUABLE;

   public static Kind classify(class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         class_1792 item = stack.method_7909();
         if (!(item instanceof class_1829) && !(item instanceof class_1743) && !(item instanceof class_1835)) {
            if (HATED_FOOD.contains(item)) {
               return KnockerGiftUtils.Kind.HATED_FOOD;
            } else if (FAVORITE_FOOD.contains(item)) {
               return KnockerGiftUtils.Kind.FAVORITE_FOOD;
            } else if (VALUABLE.contains(item)) {
               return KnockerGiftUtils.Kind.VALUABLE;
            } else if (stack.method_31573(class_3489.field_20344)) {
               return KnockerGiftUtils.Kind.FLOWER;
            } else {
               return stack.method_57826(class_9334.field_50075) ? KnockerGiftUtils.Kind.FOOD : KnockerGiftUtils.Kind.JUNK;
            }
         } else {
            return KnockerGiftUtils.Kind.WEAPON;
         }
      } else {
         return KnockerGiftUtils.Kind.JUNK;
      }
   }

   public static boolean isGift(class_1799 stack) {
      return classify(stack) != KnockerGiftUtils.Kind.JUNK;
   }

   public static boolean isFavoriteGift(class_1799 stack) {
      return FAVORITE_VALUABLE.contains(stack.method_7909()) || FAVORITE_FOOD.contains(stack.method_7909());
   }

   public static int getGiftPoints(class_1799 stack) {
      int var10000;
      switch (classify(stack).ordinal()) {
         case 2:
            var10000 = -3;
            break;
         case 3:
            var10000 = isFavoriteGift(stack) ? 6 : 4;
            break;
         case 4:
         case 5:
         default:
            var10000 = isFavoriteGift(stack) ? 5 : 2;
            break;
         case 6:
            var10000 = 0;
      }

      return var10000;
   }

   static {
      FAVORITE_FOOD = Set.of(class_1802.field_8229, class_1802.field_17534, class_1802.field_8423, class_1802.field_8741, class_1802.field_16998, class_1802.field_20417);
      HATED_FOOD = Set.of(class_1802.field_8511, class_1802.field_8680, class_1802.field_8635, class_1802.field_8323, class_1802.field_8429, class_1802.field_8209, class_1802.field_8846, class_1802.field_8373, class_1802.field_8509, class_1802.field_8766, class_1802.field_8233);
      FAVORITE_VALUABLE = Set.of(class_1802.field_8477, class_1802.field_22020, class_1802.field_17512, class_1802.field_8802, class_1802.field_22022);
      VALUABLE = Set.of(class_1802.field_8477, class_1802.field_8687, class_1802.field_8695, class_1802.field_22020, class_1802.field_8620, class_1802.field_27063, class_1802.field_8759, class_1802.field_8463, class_1802.field_8367, class_1802.field_8448, class_1802.field_8288, class_1802.field_38746, class_1802.field_8207);
   }

   public static enum Kind {
      FAVORITE_FOOD,
      FOOD,
      HATED_FOOD,
      WEAPON,
      FLOWER,
      VALUABLE,
      JUNK;

      // $FF: synthetic method
      private static Kind[] $values() {
         return new Kind[]{FAVORITE_FOOD, FOOD, HATED_FOOD, WEAPON, FLOWER, VALUABLE, JUNK};
      }
   }
}
