package com.theneru.knocker.entity;

import com.theneru.knocker.KnockerAdvancements;
import com.theneru.knocker.KnockerConfig;
import com.theneru.knocker.KnockerGiftUtils;
import com.theneru.knocker.util.KnockerMessages;
import com.theneru.knocker.util.KnockerPhase;
import com.theneru.knocker.util.KnockerWords;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.class_1304;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2392;
import net.minecraft.class_2398;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_2586;
import net.minecraft.class_2595;
import net.minecraft.class_2680;
import net.minecraft.class_3218;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_9334;
import net.minecraft.class_2902.class_2903;

public class KnockerItems {
   private static final String[] FAVORITE_FOOD_LINES = new String[]{"...|м...", "м... вкусно...|это... моё любимое?..|откуда ты знаешь?..", "Это моё любимое... спасибо|Хлеб... как раньше, когда я был живым|Ты угадала...", "Ты помнишь, что я люблю... я так счастлив|Моё любимое... от тебя. Это лучше всего|Ты заботишься обо мне...", "Ты знаешь меня. Только ты.|Ты запомнила. Значит, думаешь обо мне.|Никто больше не знает, что я люблю. Только ты.", "Моё любимое... ты меня изучала? Хе-хе...|Ты знаешь обо мне всё... так нечестно. Я тоже хочу знать о тебе ВСЁ.|Вкусно. Я съем всё, что ты дашь."};
   private static final String[] FOOD_LINES = new String[]{"...|...", "...спасибо|ням...|это мне?..|вкусно...", "Спасибо... я давно не ел|Ты делишься со мной?..|Спасибо... правда", "Ты кормишь меня... как мило|Вкусно... спасибо тебе|Ты такая заботливая...|Я съем всё до крошки", "Ешь со мной. Всегда.|Только из твоих рук.|Спасибо. Не корми так никого больше.", "Ты не отравила это?.. Хе-хе. Шучу.|Вкусно... но ты вкуснее пахнешь.|Я съел. Теперь часть тебя во мне."};
   private static final String[] HATED_LINES = new String[]{"...|...", "фу...|не надо...|бе...", "Это... невкусно. Не надо так|Фу. Забери|Я такое не ем...", "Фу... ты правда думала, что я это ем?..|Это гадость... но я не обижаюсь|Забери, пожалуйста...", "Ты издеваешься?|Ещё раз — и я обижусь.|Это мусор. Я не мусор.", "ТЫ ХОЧЕШЬ МЕНЯ ОТРАВИТЬ?|Ты кормишь меня ЭТИМ? После всего?|Я запомню это."};
   private static final String[] WEAPON_LINES = new String[]{"...|...", "это... мне?|острое...|зачем?..", "Я буду защищать тебя им|Теперь я смогу тебя охранять|Спасибо... я не подведу", "Теперь никто к тебе не подойдёт|Я буду рядом. С этим.|Ты доверяешь мне оружие?..", "Хорошее оружие. Для тех, кто к тебе подойдёт.|Острое. Как то, что я чувствую.|Теперь все узнают, что ты под защитой.", "Спасибо. Теперь я смогу... защищать. Хе-хе.|Острое... интересно, каково это — на тебе?.. шучу.|Теперь никто. Никто. НИКТО не подойдёт."};
   private static final String[] FLOWER_LINES = new String[]{"...|...", "цветок?..|красиво...|мне?..", "Никто не дарил мне цветов...|Я сохраню его|Он пахнет тобой", "Я сохраню это... навсегда|Ты даришь мне цветы... я сейчас расплачусь|Красиво... как ты", "Я спрячу его. Там, где никто не найдёт.|Цветы от тебя. Мои.|Я засушу его. Чтобы он остался навсегда.", "Цветы... на могилы тоже кладут цветы.|Я засушу его... как всё, что ты мне даёшь.|Он завянет. А я — нет."};
   private static final String[] VALUABLE_LINES = new String[]{"...|...", "блестит...|это мне?..|зачем...", "Это... слишком. Спасибо|Я сохраню|Блестит, как твои глаза", "Я сохраню это... в особом месте|Ты слишком добрая ко мне...|Это сокровище. Как ты.", "Моё. Как и ты.|Я положу это к остальному. К твоим вещам.|Спасибо. Я это заслужил.", "Ты платишь мне?.. За что? ЧТО ТЫ СДЕЛАЛА?|Подкупаешь меня? Хе-хе...|Моё. МОЁ. Как ты."};
   private static final String[] STASH_FOUND = new String[]{"...", "э-это... не моё...", "Не смотри туда... пожалуйста", "Ты... ты не должна была это видеть... это всё твоё, я просто хранил", "Это моё. Закрой. Сейчас же.", "ТЫ РЫЛАСЬ В МОИХ ВЕЩАХ?"};
   private static final String[] NEAR_STASH = new String[]{null, null, "там ничего нет... правда", "не копай там... пожалуйста", "Отойди оттуда.", "НЕ ТРОГАЙ. ЭТО. МЕСТО."};
   private static final String[] FOOD_PRESENT = new String[]{null, null, "ты голодная... я принёс", "Поешь... я не хочу, чтобы тебе было плохо", "Ешь. Ты должна быть сильной. Для меня.", "Ешь. ЕШЬ. Я смотрю."};
   private static final String[] DIAMOND_PRESENT = new String[]{null, null, null, "Я нашёл это... оно блестит как твои глаза", "Подарок. Не теряй его.", "Я достал его глубоко-глубоко... там, где темно. Для тебя."};
   private static final String[] REQUEST_OK = new String[]{"...", "...подожди", "Сейчас... подожди немного", "Для тебя? Сейчас принесу...", "", ""};
   private static final String[] REQUEST_PRICE = new String[]{"", "", "", "", "Принесу. Но сначала скажи, что ты моя.", "Хорошо... но скажи: «я твоя». Скажи это."};
   private static final String[] REQUEST_NO = new String[]{"...", "нет... не сейчас", "Не сейчас... позже", "Прости... я ничего не нашёл", "Заслужи.", "НЕТ. Ты слишком много просишь."};
   private static final String[] REQUEST_BUSY = new String[]{"...", "я уже приносил...", "Подожди... я недавно приносил", "Чуть позже... хорошо?", "Ты уже получила своё.", "ЕЩЁ? Тебе всегда мало."};
   private static final String[] DELIVER = new String[]{"...", "вот...", "Держи...", "Это тебе...", "Держи. Не потеряй.", "Держи. Ты должна мне."};
   private static final String[] PRICE_PAID = new String[]{"", "", "", "", "Хорошая... держи.", "Вот так... МОЯ. Держи."};
   private static final String[] PRICE_REFUSED = new String[]{"", "", "", "", "Не хочешь?.. Ладно. Ничего не получишь.", "НЕТ? Тогда НИЧЕГО."};
   private static final int[] REQUEST_CHANCE = new int[]{0, 15, 35, 55, 75, 90};
   private static final class_1792[] PRESENT_FOOD;
   private static final class_1792[] CREEPY_FINDS;
   private final KnockerEntity knocker;
   private final KnockerMovement movement;
   private final Random random = new Random();
   private class_1799 eating;
   private class_1799 offhandBeforeEating;
   private class_1799 weaponBeforeEating;
   private int eatTicks;
   private class_2338 stashPos;
   private int stashFoundCooldown;
   private int nearStashCooldown;
   private int nextFoodPresentTick;
   private int nextDiamondTick;
   private int requestCooldownUntil;
   private RequestKind pendingPriceKind;
   private int pendingPriceUntil;
   private RequestKind pendingDelivery;
   private int pendingDeliveryTick;
   private class_1657 pendingPlayer;
   private int thanksCooldown;
   private static final String[] GIVE_BACK;

   public KnockerItems(KnockerEntity knocker, KnockerMovement movement) {
      this.eating = class_1799.field_8037;
      this.offhandBeforeEating = class_1799.field_8037;
      this.weaponBeforeEating = class_1799.field_8037;
      this.eatTicks = 0;
      this.stashPos = null;
      this.stashFoundCooldown = 0;
      this.nearStashCooldown = 0;
      this.nextFoodPresentTick = 6000;
      this.nextDiamondTick = 36000;
      this.requestCooldownUntil = 0;
      this.pendingPriceKind = null;
      this.pendingPriceUntil = 0;
      this.pendingDelivery = null;
      this.pendingDeliveryTick = 0;
      this.pendingPlayer = null;
      this.thanksCooldown = 0;
      this.knocker = knocker;
      this.movement = movement;
   }

   private int now() {
      return this.knocker.tickCounter;
   }

   private int phase() {
      return this.knocker.getPhase().ordinal();
   }

   public void tick(class_1657 player) {
      if (this.thanksCooldown > 0) {
         --this.thanksCooldown;
      }

      if (this.stashFoundCooldown > 0) {
         --this.stashFoundCooldown;
      }

      if (this.nearStashCooldown > 0) {
         --this.nearStashCooldown;
      }

      this.tickEating();
      if (this.pendingDelivery != null && this.now() >= this.pendingDeliveryTick && this.pendingPlayer != null && !this.pendingPlayer.method_31481()) {
         this.deliver(this.pendingPlayer, this.itemsFor(this.pendingDelivery), DELIVER[this.phase()]);
         this.pendingDelivery = null;
      }

      if (this.pendingPriceKind != null && this.now() > this.pendingPriceUntil) {
         this.pendingPriceKind = null;
      }

      if (this.now() % 100 == 0 && player != null) {
         double dist = (double)this.knocker.method_5739(player);
         if (this.stashPos != null && this.nearStashCooldown <= 0 && NEAR_STASH[this.phase()] != null && player.method_24515().method_10262(this.stashPos) < (double)36.0F) {
            this.nearStashCooldown = 6000;
            this.knocker.getChat().say(player, NEAR_STASH[this.phase()]);
         }

         if (!(dist > (double)24.0F) && this.knocker.getChat().canSpeakUnprompted(400)) {
            int points = this.knocker.getObsessionPoints();
            if (points < 300 || this.now() < this.nextFoodPresentTick || player.method_7344().method_7586() > 10 && !(player.method_6032() < 8.0F) && hasFood(player)) {
               if (points >= 400 && this.now() >= this.nextDiamondTick) {
                  this.nextDiamondTick = this.now() + KnockerConfig.ticksBetween(KnockerConfig.diamondMin, KnockerConfig.diamondMax);
                  String line = DIAMOND_PRESENT[this.phase()];
                  if (line != null) {
                     this.deliver(player, List.of(new class_1799(class_1802.field_8477)), line);
                  }
               }

            } else {
               this.nextFoodPresentTick = this.now() + KnockerConfig.ticks(KnockerConfig.foodPresent);
               String line = FOOD_PRESENT[this.phase()];
               if (line != null) {
                  this.deliver(player, this.itemsFor(KnockerItems.RequestKind.FOOD), line);
               }

            }
         }
      }
   }

   public void onGiftPickup(class_1799 stack, class_1657 player) {
      this.knocker.getGames().onGift(stack);
      if (player != null) {
         KnockerAdvancements.grant(player, "first_gift");
      }

      if (player != null && KnockerGiftUtils.classify(stack) != KnockerGiftUtils.Kind.HATED_FOOD) {
         this.knocker.getMood().onGift(player);
      }

      KnockerGiftUtils.Kind kind = KnockerGiftUtils.classify(stack);
      this.knocker.addObsession(KnockerGiftUtils.getGiftPoints(stack));
      String itemName = stack.method_7964().getString();
      KnockerEntity var10000 = this.knocker;
      String var10001;
      switch (kind) {
         case HATED_FOOD -> var10001 = "она дала мне гадость: " + itemName;
         case WEAPON -> var10001 = "она подарила мне оружие: " + itemName;
         case FAVORITE_FOOD -> var10001 = "она угостила меня моим любимым: " + itemName;
         default -> var10001 = "она подарила мне: " + itemName;
      }

      var10000.rememberEvent(var10001);
      int p = this.phase();
      String var7;
      switch (kind) {
         case HATED_FOOD:
            this.throwBack(stack, player);
            if (p == KnockerPhase.PARANOIA.ordinal()) {
               this.knocker.onInsulted();
            }

            var7 = this.variant(HATED_LINES[p]);
            break;
         case WEAPON:
            this.equipWeapon(stack);
            var7 = this.variant(WEAPON_LINES[p]);
            break;
         case FAVORITE_FOOD:
            this.startEating(stack);
            var7 = null;
            break;
         case FOOD:
            this.startEating(stack);
            var7 = null;
            break;
         case FLOWER:
            this.stash(stack);
            this.knocker.spawnParticles(class_2398.field_11201, 5);
            var7 = this.variant(FLOWER_LINES[p]);
            break;
         case VALUABLE:
            this.stash(stack);
            this.knocker.spawnParticles(class_2398.field_11201, 5);
            var7 = this.variant(VALUABLE_LINES[p]);
            break;
         case JUNK:
            var7 = null;
            break;
         default:
            throw new MatchException((String)null, (Throwable)null);
      }

      String line = var7;
      if (line != null && player != null) {
         this.thank(player, line);
      }

   }

   public void handleStolen(class_1799 stack) {
      switch (KnockerGiftUtils.classify(stack)) {
         case WEAPON -> this.equipWeapon(stack);
         case JUNK -> this.throwAway(stack);
         default -> this.stash(stack);
      }

   }

   public String handleGiveBack(class_1657 player, String norm) {
      if (!KnockerMessages.containsAny(norm, "отдай", "верни", "дай обратно", "это моё", "это мое", "положи", "брось")) {
         return null;
      } else {
         class_1799 held = !this.weaponBeforeEating.method_7960() ? this.weaponBeforeEating : this.knocker.method_6047();
         if (!held.method_7960() && (this.eating.method_7960() || !this.weaponBeforeEating.method_7960())) {
            int p = this.phase();
            if (p == KnockerPhase.PARANOIA.ordinal()) {
               return GIVE_BACK[p];
            } else {
               class_1799 give = held.method_7972();
               if (!this.weaponBeforeEating.method_7960()) {
                  this.weaponBeforeEating = class_1799.field_8037;
               } else {
                  this.knocker.method_5673(class_1304.field_6173, class_1799.field_8037);
               }

               this.knocker.playAction("give");
               player.method_31548().method_7398(give);
               this.knocker.rememberEvent("она попросила вернуть " + give.method_7964().getString() + ", и я отдал");
               return GIVE_BACK[p];
            }
         } else {
            return null;
         }
      }
   }

   private String variant(String entry) {
      return this.knocker.getChat().getBrain().pick(entry.split("\\|"));
   }

   private void thank(class_1657 player, String line) {
      if (this.thanksCooldown > 0) {
         this.knocker.spawnParticles(class_2398.field_11201, 2);
      } else {
         this.thanksCooldown = 500;
         this.knocker.getChat().say(player, line);
      }
   }

   private void startEating(class_1799 food) {
      if (!this.eating.method_7960()) {
         this.stash(food);
      } else {
         this.eating = food.method_46651(1);
         if (food.method_7947() > 1) {
            this.stash(food.method_46651(food.method_7947() - 1));
         }

         this.offhandBeforeEating = this.knocker.method_6079().method_7972();
         this.weaponBeforeEating = this.knocker.method_6047().method_7972();
         this.knocker.method_5673(class_1304.field_6173, this.eating.method_7972());
         this.knocker.method_5673(class_1304.field_6171, class_1799.field_8037);
         this.eatTicks = 40;
      }
   }

   private void tickEating() {
      if (!this.eating.method_7960()) {
         class_1937 world = this.knocker.method_37908();
         if (this.eatTicks % 4 == 0 && world instanceof class_3218) {
            class_3218 server = (class_3218)world;
            class_243 mouth = this.knocker.method_33571().method_1019(this.knocker.method_5828(1.0F).method_1021(0.3));
            server.method_14199(new class_2392(class_2398.field_11218, this.eating), mouth.field_1352, mouth.field_1351 - 0.1, mouth.field_1350, 4, 0.08, 0.08, 0.08, 0.05);
            world.method_43128((class_1657)null, this.knocker.method_23317(), this.knocker.method_23318(), this.knocker.method_23321(), class_3417.field_20614, class_3419.field_15254, 0.6F, 0.9F + this.random.nextFloat() * 0.2F);
         }

         if (--this.eatTicks <= 0) {
            boolean favorite = KnockerGiftUtils.classify(this.eating) == KnockerGiftUtils.Kind.FAVORITE_FOOD;
            world.method_43128((class_1657)null, this.knocker.method_23317(), this.knocker.method_23318(), this.knocker.method_23321(), class_3417.field_19149, class_3419.field_15254, 0.5F, 1.0F);
            this.knocker.method_6025(4.0F);
            this.knocker.spawnParticles(class_2398.field_11201, favorite ? 6 : 3);
            this.knocker.method_5673(class_1304.field_6173, this.weaponBeforeEating);
            this.knocker.method_5673(class_1304.field_6171, this.offhandBeforeEating);
            this.eating = class_1799.field_8037;
            this.offhandBeforeEating = class_1799.field_8037;
            this.weaponBeforeEating = class_1799.field_8037;
            class_1657 player = this.knocker.getChosen((double)16.0F);
            if (player != null) {
               this.thank(player, this.variant((favorite ? FAVORITE_FOOD_LINES : FOOD_LINES)[this.phase()]));
            }

            if (player != null && favorite) {
               this.knocker.getThoughts().onFavoriteFood(player, this.knocker.getPhase());
            }

         }
      }
   }

   private static boolean hasFood(class_1657 player) {
      for(int i = 0; i < player.method_31548().method_5439(); ++i) {
         if (player.method_31548().method_5438(i).method_57826(class_9334.field_50075)) {
            return true;
         }
      }

      return false;
   }

   private void throwBack(class_1799 stack, class_1657 player) {
      class_1937 world = this.knocker.method_37908();
      class_1542 item = new class_1542(world, this.knocker.method_23317(), this.knocker.method_23320() - 0.3, this.knocker.method_23321(), stack.method_7972());
      class_243 dir = player != null ? player.method_19538().method_1020(this.knocker.method_19538()).method_1029() : this.knocker.method_5828(1.0F);
      item.method_18800(dir.field_1352 * 0.35, (double)0.25F, dir.field_1350 * 0.35);
      item.method_6982(40);
      world.method_8649(item);
      this.knocker.markOwnDrop(item);
      world.method_43128((class_1657)null, this.knocker.method_23317(), this.knocker.method_23318(), this.knocker.method_23321(), class_3417.field_14789, class_3419.field_15254, 0.7F, 1.0F);
      this.knocker.playAction("spit");
   }

   private void throwAway(class_1799 stack) {
      class_1937 world = this.knocker.method_37908();
      double angle = this.random.nextDouble() * Math.PI * (double)2.0F;
      class_1542 item = new class_1542(world, this.knocker.method_23317(), this.knocker.method_23320(), this.knocker.method_23321(), stack.method_7972());
      item.method_18800(Math.cos(angle) * 0.6, 0.4, Math.sin(angle) * 0.6);
      world.method_8649(item);
      this.knocker.markOwnDrop(item);
   }

   private void equipWeapon(class_1799 weapon) {
      if (!this.eating.method_7960()) {
         if (!this.weaponBeforeEating.method_7960()) {
            this.stash(this.weaponBeforeEating.method_7972());
         }

         this.weaponBeforeEating = weapon.method_46651(1);
      } else {
         class_1799 old = this.knocker.method_6047();
         if (!old.method_7960()) {
            this.stash(old.method_7972());
         }

         this.knocker.method_5673(class_1304.field_6173, weapon.method_46651(1));
      }
   }

   public boolean isEating() {
      return !this.eating.method_7960();
   }

   public boolean isStash(class_2338 pos) {
      return this.stashPos != null && this.stashPos.equals(pos);
   }

   public void stash(class_1799 stack) {
      if (!stack.method_7960()) {
         class_2595 chest = this.ensureStashChest();
         if (chest == null || !insert(chest, stack)) {
            if (this.knocker.method_6079().method_7960() && this.eating.method_7960()) {
               this.knocker.method_5673(class_1304.field_6171, stack.method_7972());
            } else {
               this.throwAway(stack);
            }

         }
      }
   }

   private static boolean insert(class_2595 chest, class_1799 stack) {
      class_1799 rest = stack.method_7972();

      for(int i = 0; i < chest.method_5439() && !rest.method_7960(); ++i) {
         class_1799 slot = chest.method_5438(i);
         if (slot.method_7960()) {
            chest.method_5447(i, rest.method_7972());
            rest = class_1799.field_8037;
         } else if (class_1799.method_31577(slot, rest) && slot.method_7947() < slot.method_7914()) {
            int move = Math.min(rest.method_7947(), slot.method_7914() - slot.method_7947());
            slot.method_7933(move);
            rest.method_7934(move);
         }
      }

      chest.method_5431();
      return rest.method_7960();
   }

   private class_2595 ensureStashChest() {
      class_1937 world = this.knocker.method_37908();
      if (this.stashPos != null) {
         if (world.method_22340(this.stashPos)) {
            class_2586 var3 = world.method_8321(this.stashPos);
            if (var3 instanceof class_2595) {
               class_2595 chest = (class_2595)var3;
               return chest;
            }
         }

         if (!world.method_22340(this.stashPos)) {
            return null;
         }

         this.stashPos = null;
      }

      class_2338 base = this.knocker.getHomePos() != null ? this.knocker.getHomePos() : this.knocker.method_24515();
      class_1657 player = this.knocker.getChosen((double)64.0F);

      for(int attempt = 0; attempt < 30; ++attempt) {
         double angle = this.random.nextDouble() * Math.PI * (double)2.0F;
         int distance = 12 + this.random.nextInt(13);
         int x = base.method_10263() + (int)Math.round(Math.cos(angle) * (double)distance);
         int z = base.method_10260() + (int)Math.round(Math.sin(angle) * (double)distance);
         class_2338 column = new class_2338(x, base.method_10264(), z);
         if (world.method_22340(column)) {
            int top = world.method_8624(class_2903.field_13202, x, z);
            class_2338 pos = new class_2338(x, top - 3, z);
            if (player == null || !(player.method_24515().method_10262(pos) < (double)100.0F)) {
               class_2680 here = world.method_8320(pos);
               class_2680 above = world.method_8320(pos.method_10084());
               if (!here.method_26215() && here.method_26227().method_15769() && !above.method_26215() && above.method_26227().method_15769() && world.method_8321(pos) == null) {
                  world.method_8501(pos, class_2246.field_10034.method_9564());
                  class_2586 var16 = world.method_8321(pos);
                  if (var16 instanceof class_2595) {
                     class_2595 chest = (class_2595)var16;
                     this.stashPos = pos.method_10062();
                     return chest;
                  }
               }
            }
         }
      }

      return null;
   }

   public void onStashOpened(class_1657 player) {
      if (this.stashFoundCooldown <= 0) {
         this.stashFoundCooldown = 1200;
         int p = this.phase();
         this.knocker.rememberEvent("она нашла мой тайный сундук");
         this.knocker.getChat().say(player, STASH_FOUND[p]);
         if (p == KnockerPhase.PARANOIA.ordinal()) {
            this.knocker.onInsulted();
            this.movement.appearInFront(player);
         } else if (p == KnockerPhase.OBSESSION.ordinal()) {
            this.movement.teleportBehind(player);
         } else if (p >= KnockerPhase.CURIOUS.ordinal()) {
            this.knocker.playAction("shy");
            this.movement.pauseFollow(player, 600);
         }

      }
   }

   public String handleChat(class_1657 player, String norm) {
      int p = this.phase();
      if (this.pendingPriceKind != null) {
         if (KnockerMessages.containsAny(norm, KnockerWords.NOT_YOURS) && !KnockerMessages.containsPhrase(norm, "я твоя")) {
            this.pendingPriceKind = null;
            return PRICE_REFUSED[p];
         }

         if (KnockerMessages.containsAny(norm, KnockerWords.I_AM_YOURS)) {
            RequestKind kind = this.pendingPriceKind;
            this.pendingPriceKind = null;
            this.knocker.addObsession(3);
            this.deliver(player, this.itemsFor(kind), (String)null);
            return PRICE_PAID[p];
         }
      }

      RequestKind kind = detectRequest(norm);
      if (kind == null) {
         return null;
      } else if (this.now() >= this.requestCooldownUntil && this.pendingDelivery == null) {
         int chance = REQUEST_CHANCE[p];
         if (kind == KnockerItems.RequestKind.DIAMOND) {
            chance = p >= KnockerPhase.LOVE.ordinal() ? chance / 2 : 0;
         }

         if (this.random.nextInt(100) >= chance) {
            this.requestCooldownUntil = this.now() + 1200;
            return REQUEST_NO[p];
         } else {
            this.requestCooldownUntil = this.now() + 3600;
            if (p >= KnockerPhase.OBSESSION.ordinal()) {
               this.pendingPriceKind = kind;
               this.pendingPriceUntil = this.now() + 1200;
               return REQUEST_PRICE[p];
            } else {
               this.pendingDelivery = kind;
               this.pendingPlayer = player;
               this.pendingDeliveryTick = this.now() + 100 + this.random.nextInt(100);
               return REQUEST_OK[p];
            }
         }
      } else {
         return REQUEST_BUSY[p];
      }
   }

   private static RequestKind detectRequest(String norm) {
      if (KnockerMessages.containsAny(norm, KnockerWords.ASK_DIAMOND)) {
         return KnockerItems.RequestKind.DIAMOND;
      } else if (KnockerMessages.containsAny(norm, KnockerWords.ASK_FOOD)) {
         return KnockerItems.RequestKind.FOOD;
      } else {
         return KnockerMessages.containsAny(norm, KnockerWords.ASK_ANY) ? KnockerItems.RequestKind.ANY : null;
      }
   }

   private class_1792 favoriteFoodOr(class_1792 fallback) {
      class_1657 p = this.knocker.getChosen((double)64.0F);
      class_1792 fav = p == null ? null : KnockerFavorites.food(p.method_5667());
      return fav != null && this.random.nextInt(10) < 7 ? fav : fallback;
   }

   private List<class_1799> itemsFor(RequestKind kind) {
      List<class_1799> list = new ArrayList();
      switch (kind.ordinal()) {
         case 0:
            list.add(new class_1799(this.favoriteFoodOr(PRESENT_FOOD[this.random.nextInt(PRESENT_FOOD.length)]), 5));
            break;
         case 1:
            list.add(new class_1799(class_1802.field_8477));
            break;
         case 2:
            int roll = this.random.nextInt(100);
            if (this.phase() == KnockerPhase.PARANOIA.ordinal() && roll < 30) {
               class_1799 creepy = new class_1799(CREEPY_FINDS[this.random.nextInt(CREEPY_FINDS.length)]);
               creepy.method_57379(class_9334.field_49631, class_2561.method_43470("§7я нашёл это для тебя"));
               list.add(creepy);
            } else if (roll < 50) {
               list.add(new class_1799(PRESENT_FOOD[this.random.nextInt(PRESENT_FOOD.length)], 5));
            } else if (roll < 70) {
               list.add(new class_1799(this.random.nextBoolean() ? class_1802.field_17512 : class_1802.field_8880));
            } else if (roll < 85) {
               list.add(new class_1799(class_1802.field_8620, 3));
            } else if (roll < 95) {
               list.add(new class_1799(class_1802.field_8687));
            } else {
               list.add(new class_1799(class_1802.field_8477));
            }
      }

      return list;
   }

   public void aiGift(class_1657 player) {
      if (this.now() >= this.requestCooldownUntil && this.phase() >= KnockerPhase.ATTACHED.ordinal()) {
         this.requestCooldownUntil = this.now() + 6000;
         this.deliver(player, this.itemsFor(KnockerItems.RequestKind.FOOD), (String)null);
      }
   }

   public void deliver(class_1657 player, List<class_1799> stacks, String line) {
      if (this.knocker.method_5739(player) > 6.0F) {
         this.movement.teleportBehind(player);
      }

      this.knocker.method_5988().method_6226(player, 30.0F, 30.0F);
      this.knocker.playAction("give");
      class_1937 world = this.knocker.method_37908();

      for(class_1799 stack : stacks) {
         class_1542 item = new class_1542(world, player.method_23317(), player.method_23318() + 0.3, player.method_23321(), stack.method_7972());
         item.method_18800((double)0.0F, 0.1, (double)0.0F);
         item.method_6982(10);
         world.method_8649(item);
         this.knocker.markOwnDrop(item);
      }

      if (line != null && !line.isEmpty()) {
         this.knocker.getChat().say(player, line);
      }

      this.knocker.rememberEvent("я принёс ей подарок");
   }

   public void writeNbt(class_2487 nbt) {
      if (this.stashPos != null) {
         nbt.method_10569("StashX", this.stashPos.method_10263());
         nbt.method_10569("StashY", this.stashPos.method_10264());
         nbt.method_10569("StashZ", this.stashPos.method_10260());
      }

   }

   public void readNbt(class_2487 nbt) {
      if (nbt.method_10545("StashX")) {
         this.stashPos = new class_2338(nbt.method_10550("StashX"), nbt.method_10550("StashY"), nbt.method_10550("StashZ"));
      }

   }

   static {
      PRESENT_FOOD = new class_1792[]{class_1802.field_8229, class_1802.field_8176, class_1802.field_8512, class_1802.field_8544, class_1802.field_8279};
      CREEPY_FINDS = new class_1792[]{class_1802.field_8606, class_1802.field_8398, class_1802.field_8680, class_1802.field_17515, class_1802.field_8511};
      GIVE_BACK = new String[]{"...", "...на", "Прости... держи", "Держи. Я просто хотел подержать", "Ладно. Только для тебя.", "НЕТ. Это моё теперь."};
   }

   public static enum RequestKind {
      FOOD,
      DIAMOND,
      ANY;

      // $FF: synthetic method
      private static RequestKind[] $values() {
         return new RequestKind[]{FOOD, DIAMOND, ANY};
      }
   }
}
