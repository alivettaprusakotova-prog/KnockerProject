package com.theneru.knocker.entity;

import com.theneru.knocker.KnockerMemoryStore;
import com.theneru.knocker.util.KnockerMessages;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.class_1792;
import net.minecraft.class_1802;

public final class KnockerFavorites {
   private static final Map<String, class_1792> FLOWERS = new LinkedHashMap();
   private static final Map<String, class_1792> FOOD = new LinkedHashMap();

   private KnockerFavorites() {
   }

   private static void f(Map<String, class_1792> map, String stem, class_1792 item) {
      map.put(KnockerMessages.fold(stem), item);
   }

   private static class_1792 match(Map<String, class_1792> map, String answer) {
      if (answer == null) {
         return null;
      } else {
         for(String w : KnockerMessages.normalize(answer).split(" ")) {
            String f = KnockerMessages.fold(w);

            for(Map.Entry<String, class_1792> e : map.entrySet()) {
               if (f.startsWith((String)e.getKey())) {
                  return (class_1792)e.getValue();
               }
            }
         }

         return null;
      }
   }

   public static class_1792 flower(UUID player) {
      return match(FLOWERS, (String)KnockerMemoryStore.facts(player).get("flower"));
   }

   public static class_1792 food(UUID player) {
      return match(FOOD, (String)KnockerMemoryStore.facts(player).get("food"));
   }

   static {
      f(FLOWERS, "орхиде", class_1802.field_17499);
      f(FLOWERS, "тюльпан", class_1802.field_17502);
      f(FLOWERS, "ромашк", class_1802.field_17512);
      f(FLOWERS, "одуванч", class_1802.field_8491);
      f(FLOWERS, "васил", class_1802.field_17513);
      f(FLOWERS, "ландыш", class_1802.field_17514);
      f(FLOWERS, "подсолнух", class_1802.field_17525);
      f(FLOWERS, "сирен", class_1802.field_17526);
      f(FLOWERS, "пион", class_1802.field_17529);
      f(FLOWERS, "роз", class_1802.field_17527);
      f(FLOWERS, "мак", class_1802.field_8880);
      f(FLOWERS, "гвозд", class_1802.field_17511);
      f(FLOWERS, "хризантем", class_1802.field_17501);
      f(FLOWERS, "лили", class_1802.field_17514);
      f(FLOWERS, "сакур", class_1802.field_42696);
      f(FLOWERS, "лаванд", class_1802.field_17500);
      f(FOOD, "хлеб", class_1802.field_8229);
      f(FOOD, "бутер", class_1802.field_8229);
      f(FOOD, "пицц", class_1802.field_8229);
      f(FOOD, "торт", class_1802.field_17534);
      f(FOOD, "печеньк", class_1802.field_8423);
      f(FOOD, "печень", class_1802.field_8423);
      f(FOOD, "пирог", class_1802.field_8741);
      f(FOOD, "яблок", class_1802.field_8279);
      f(FOOD, "яблоч", class_1802.field_8279);
      f(FOOD, "стейк", class_1802.field_8176);
      f(FOOD, "мяс", class_1802.field_8176);
      f(FOOD, "шашлык", class_1802.field_8176);
      f(FOOD, "курин", class_1802.field_8544);
      f(FOOD, "куриц", class_1802.field_8544);
      f(FOOD, "наггетс", class_1802.field_8544);
      f(FOOD, "рыб", class_1802.field_8509);
      f(FOOD, "суши", class_1802.field_8509);
      f(FOOD, "ролл", class_1802.field_8509);
      f(FOOD, "картош", class_1802.field_8512);
      f(FOOD, "картофел", class_1802.field_8512);
      f(FOOD, "морков", class_1802.field_8071);
      f(FOOD, "арбуз", class_1802.field_8497);
      f(FOOD, "дын", class_1802.field_8497);
      f(FOOD, "мёд", class_1802.field_20417);
      f(FOOD, "суп", class_1802.field_8208);
      f(FOOD, "борщ", class_1802.field_8515);
      f(FOOD, "свёкл", class_1802.field_8515);
      f(FOOD, "ягод", class_1802.field_16998);
      f(FOOD, "клубник", class_1802.field_16998);
      f(FOOD, "малин", class_1802.field_16998);
      f(FOOD, "шоколад", class_1802.field_8423);
      f(FOOD, "сладк", class_1802.field_8423);
      f(FOOD, "конфет", class_1802.field_8423);
      f(FOOD, "молок", class_1802.field_8103);
   }
}
