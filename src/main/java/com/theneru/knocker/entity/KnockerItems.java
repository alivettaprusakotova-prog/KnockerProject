package com.theneru.knocker.entity;

import com.theneru.knocker.KnockerAdvancements;
import com.theneru.knocker.KnockerGiftUtils;
import com.theneru.knocker.util.KnockerMessages;
import com.theneru.knocker.util.KnockerPhase;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
import net.minecraft.class_2595;
import net.minecraft.class_2680;
import net.minecraft.class_3218;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
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
   private static final String[] GIVE_BACK = new String[]{"...", "...на", "Прости... держи", "Держи. Я просто хотел подержать", "Ладно. Только для тебя.", "НЕТ. Это моё теперь."};

   // Расширенный пул подарков: всё, что попросит невеста, должно у неё появиться.
   private static final class_1792[] FOOD_PRESENTS = new class_1792[]{
         class_1802.field_8229, class_1802.field_8176, class_1802.field_8279, class_1802.field_8423,
         class_1802.field_17534, class_1802.field_8741, class_1802.field_16998, class_1802.field_8261,
         class_1802.field_8208, class_1802.field_8179, class_1802.field_8544
   };
   private static final class_1792[] FLOWER_PRESENTS = new class_1792[]{
         class_1802.field_8880, class_1802.field_17527, class_1802.field_17529, class_1802.field_17515,
         class_1802.field_17512, class_1802.field_17514, class_1802.field_17502, class_1802.field_17525,
         class_1802.field_17526, class_1802.field_17499
   };
   private static final class_1792[] RARE_PRESENTS = new class_1792[]{
         class_1802.field_8477, class_1802.field_8687, class_1802.field_22020, class_1802.field_8288,
         class_1802.field_8367, class_1802.field_8463, class_1802.field_8603, class_1802.field_8733,
         class_1802.field_27063, class_1802.field_38746, class_1802.field_8207, class_1802.field_8864,
         class_1802.field_8620, class_1802.field_8695
   };

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
   private int thanksCooldown;

   public KnockerItems(KnockerEntity knocker, KnockerMovement movement) {
      this.eating = class_1799.field_8037;
      this.offhandBeforeEating = class_1799.field_8037;
      this.weaponBeforeEating = class_1799.field_8037;
      this.eatTicks = 0;
      this.stashPos = null;
      this.stashFoundCooldown = 0;
      this.nearStashCooldown = 0;
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
      // Никаких таймеров подарков и ограничений по фазе: подарок выдаётся только по просьбе/тегу ИИ.
      if (this.now() % 100 == 0 && player != null && this.stashPos != null && this.nearStashCooldown <= 0
            && NEAR_STASH[this.phase()] != null && player.method_24515().method_10262(this.stashPos) < 36.0F) {
         this.nearStashCooldown = 6000;
         this.knocker.getChat().say(player, NEAR_STASH[this.phase()]);
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
      String event = switch (kind) {
         case HATED_FOOD -> "она дала мне гадость: " + itemName;
         case WEAPON -> "она подарила мне оружие: " + itemName;
         case FAVORITE_FOOD -> "она угостила меня моим любимым: " + itemName;
         default -> "она подарила мне: " + itemName;
      };
      this.knocker.rememberEvent(event);

      String line = null;
      int p = this.phase();
      switch (kind) {
         case HATED_FOOD -> {
            this.throwBack(stack, player);
            if (p == KnockerPhase.PARANOIA.ordinal()) {
               this.knocker.onInsulted();
            }
            line = this.variant(HATED_LINES[p]);
         }
         case WEAPON -> {
            this.equipWeapon(stack);
            line = this.variant(WEAPON_LINES[p]);
         }
         case FAVORITE_FOOD, FOOD -> this.startEating(stack);
         case FLOWER -> {
            this.stash(stack);
            this.knocker.spawnParticles(class_2398.field_11201, 5);
            line = this.variant(FLOWER_LINES[p]);
         }
         case VALUABLE -> {
            this.stash(stack);
            this.knocker.spawnParticles(class_2398.field_11201, 5);
            line = this.variant(VALUABLE_LINES[p]);
         }
         case JUNK -> {
         }
      }

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
      }

      class_1799 held = !this.weaponBeforeEating.method_7960() ? this.weaponBeforeEating : this.knocker.method_6047();
      if (!held.method_7960() && (this.eating.method_7960() || !this.weaponBeforeEating.method_7960())) {
         int p = this.phase();
         if (p != KnockerPhase.PARANOIA.ordinal()) {
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
      }

      return null;
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
            world.method_43128(null, this.knocker.method_23317(), this.knocker.method_23318(), this.knocker.method_23321(), class_3417.field_20614, class_3419.field_15254, 0.6F, 0.9F + this.random.nextFloat() * 0.2F);
         }

         if (--this.eatTicks <= 0) {
            boolean favorite = KnockerGiftUtils.classify(this.eating) == KnockerGiftUtils.Kind.FAVORITE_FOOD;
            world.method_43128(null, this.knocker.method_23317(), this.knocker.method_23318(), this.knocker.method_23321(), class_3417.field_19149, class_3419.field_15254, 0.5F, 1.0F);
            this.knocker.method_6025(4.0F);
            this.knocker.spawnParticles(class_2398.field_11201, favorite ? 6 : 3);
            this.knocker.method_5673(class_1304.field_6173, this.weaponBeforeEating);
            this.knocker.method_5673(class_1304.field_6171, this.offhandBeforeEating);
            this.eating = class_1799.field_8037;
            this.offhandBeforeEating = class_1799.field_8037;
            this.weaponBeforeEating = class_1799.field_8037;
            class_1657 player = this.knocker.getChosen(16.0);
            if (player != null) {
               this.thank(player, this.variant((favorite ? FAVORITE_FOOD_LINES : FOOD_LINES)[this.phase()]));
               if (favorite) {
                  this.knocker.getThoughts().onFavoriteFood(player, this.knocker.getPhase());
               }
            }
         }
      }
   }

   private void throwBack(class_1799 stack, class_1657 player) {
      class_1937 world = this.knocker.method_37908();
      class_1542 item = new class_1542(world, this.knocker.method_23317(), this.knocker.method_23320() - 0.3, this.knocker.method_23321(), stack.method_7972());
      class_243 dir = player != null ? player.method_19538().method_1020(this.knocker.method_19538()).method_1029() : this.knocker.method_5828(1.0F);
      item.method_18800(dir.field_1352 * 0.35, 0.25, dir.field_1350 * 0.35);
      item.method_6982(40);
      world.method_8649(item);
      this.knocker.markOwnDrop(item);
      world.method_43128(null, this.knocker.method_23317(), this.knocker.method_23318(), this.knocker.method_23321(), class_3417.field_14789, class_3419.field_15254, 0.7F, 1.0F);
      this.knocker.playAction("spit");
   }

   private void throwAway(class_1799 stack) {
      class_1937 world = this.knocker.method_37908();
      double angle = this.random.nextDouble() * Math.PI * 2.0;
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
      for (int i = 0; i < chest.method_5439() && !rest.method_7960(); ++i) {
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
            net.minecraft.class_2586 block = world.method_8321(this.stashPos);
            if (block instanceof class_2595 chest) {
               return chest;
            }
            return null;
         }
         this.stashPos = null;
      }

      class_2338 base = this.knocker.getHomePos() != null ? this.knocker.getHomePos() : this.knocker.method_24515();
      class_1657 player = this.knocker.getChosen(64.0);
      for (int attempt = 0; attempt < 30; ++attempt) {
         double angle = this.random.nextDouble() * Math.PI * 2.0;
         int distance = 12 + this.random.nextInt(13);
         int x = base.method_10263() + (int)Math.round(Math.cos(angle) * distance);
         int z = base.method_10260() + (int)Math.round(Math.sin(angle) * distance);
         class_2338 column = new class_2338(x, base.method_10264(), z);
         if (world.method_22340(column)) {
            int top = world.method_8624(class_2903.field_13202, x, z);
            class_2338 pos = new class_2338(x, top - 3, z);
            if (player == null || !(player.method_24515().method_10262(pos) < 100.0F)) {
               class_2680 here = world.method_8320(pos);
               class_2680 above = world.method_8320(pos.method_10084());
               if (!here.method_26215() && here.method_26227().method_15769() && !above.method_26215()
                     && above.method_26227().method_15769() && world.method_8321(pos) == null) {
                  world.method_8501(pos, class_2246.field_10034.method_9564());
                  net.minecraft.class_2586 block = world.method_8321(pos);
                  if (block instanceof class_2595 chest) {
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

   /** Прямой триггер из чата: «дай», «принеси», «подари», «хочу», «алмаз» и т.п. */
   public static boolean isGiftRequest(String raw) {
      if (raw == null) {
         return false;
      }
      String norm = KnockerMessages.normalize(raw);
      // «верни/отдай обратно» — это про его собственные вещи, не подарок.
      if (contains(norm, "обратно", "верни мне", "отдай мне обратно")) {
         return false;
      }
      return contains(norm, "дай", "дашь", "принеси", "подари", "подар", "хочу", "хотела", "плюшк",
            "алмаз", "брюлик", "изумруд", "незер", "тотем", "цветок", "цветы", "роз", "пион",
            "куша", "еду", "еды", "поест", "голод", "жрать", "хавк", "торт", "пирог", "яблок",
            "хлеб", "еда", "перекус", "подгон", "закинь", "найди", "сделай мне");
   }

   private static boolean contains(String norm, String... words) {
      for (String w : words) {
         if (norm.contains(w)) {
            return true;
         }
      }
      return false;
   }

   /** Выдаёт подарок по просьбе игрока, независимо от фазы и таймеров. */
   public boolean giftFromRequest(class_1657 player, String raw) {
      if (player == null || raw == null) {
         return false;
      }
      String text = KnockerMessages.normalize(raw);
      class_1792 named = namedItem(text);
      List<class_1799> stacks = new ArrayList<>();

      if (named != null) {
         stacks.add(new class_1799(named, amountFor(named)));
      } else {
         if (contains(text, "цвет", "роз", "пион", "букет")) {
            stacks.add(new class_1799(randomFrom(FLOWER_PRESENTS), 2));
         } else if (contains(text, "поест", "еду", "еды", "куша", "голод", "торт", "пирог", "хлеб", "еда", "плюшк")) {
            stacks.add(new class_1799(randomFrom(FOOD_PRESENTS), 5));
         } else {
            stacks.add(new class_1799(randomFrom(RARE_PRESENTS), 2));
         }
      }

      // Если она просит и еду, и редкость — даём и то, и другое.
      if (named != null && contains(text, "еду", "еды", "поест", "куша", "голод")) {
         stacks.add(new class_1799(randomFrom(FOOD_PRESENTS), 5));
      }

      deliver(player, stacks, null);
      this.knocker.rememberEvent("я принёс ей подарок по её просьбе");
      return true;
   }

   /** Подарок по тегу ИИ [подарок: ...] или [действие: подарок]. */
   public void aiGift(class_1657 player, String requested) {
      if (player == null) {
         return;
      }
      if (requested != null && !requested.isBlank()) {
         class_1792 named = namedItem(KnockerMessages.normalize(requested.toLowerCase(Locale.ROOT)));
         List<class_1799> stacks = new ArrayList<>();
         if (named != null) {
            stacks.add(new class_1799(named, amountFor(named)));
         } else {
            stacks.add(new class_1799(randomFrom(RARE_PRESENTS), 2));
         }
         deliver(player, stacks, null);
      } else {
         giftFromRequest(player, "дай подарок");
      }
      this.knocker.rememberEvent("я принёс ей подарок");
   }

   public void aiGift(class_1657 player) {
      this.aiGift(player, null);
   }

   private static class_1792 randomFrom(class_1792[] pool) {
      return pool[new Random().nextInt(pool.length)];
   }

   private static int amountFor(class_1792 item) {
      if (item == class_1802.field_8288 || item == class_1802.field_8367 || item == class_1802.field_38746
            || item == class_1802.field_8207 || item == class_1802.field_8864 || item == class_1802.field_22020) {
         return 1;
      }
      if (item == class_1802.field_17534 || item == class_1802.field_8741) {
         return 1;
      }
      if (isFood(item)) {
         return 5;
      }
      return item == class_1802.field_8477 || item == class_1802.field_8687 ? 3 : 2;
   }

   private static boolean isFood(class_1792 item) {
      for (class_1792 food : FOOD_PRESENTS) {
         if (food == item) {
            return true;
         }
      }
      return false;
   }

   /** Ищет конкретный предмет в тексте игрока или в теге ИИ. */
   private static class_1792 namedItem(String norm) {
      if (contains(norm, "алмаз", "брюлик", "diamond")) {
         return class_1802.field_8477;
      }
      if (contains(norm, "изумруд", "смарагд", "emerald")) {
         return class_1802.field_8687;
      }
      if (contains(norm, "незер", "незерит")) {
         return class_1802.field_22020;
      }
      if (contains(norm, "тотем", "бессмерт")) {
         return class_1802.field_8288;
      }
      if (contains(norm, "зачарованн", "enchanted")) {
         return class_1802.field_8367;
      }
      if (contains(norm, "золотое яблок", "золотой яблок")) {
         return class_1802.field_8463;
      }
      if (contains(norm, "торт", "тортик", "cake")) {
         return class_1802.field_17534;
      }
      if (contains(norm, "тыквен", "пирог", "pie")) {
         return class_1802.field_8741;
      }
      if (contains(norm, "роза визера", "визер", "wither rose")) {
         return class_1802.field_17515;
      }
      if (contains(norm, "пион", "peony")) {
         return class_1802.field_17529;
      }
      if (contains(norm, "роза", "rose")) {
         return class_1802.field_17527;
      }
      if (contains(norm, "цвет", "букет", "лаванд", "орхид", "ирис", "тюльпан", "подснежник")) {
         return randomFrom(FLOWER_PRESENTS);
      }
      if (contains(norm, "пион", "плюш", "еда", "еду", "еды", "хлеб", "куша", "поест", "голод", "мяс", "картош", "печен", "суп")) {
         return randomFrom(FOOD_PRESENTS);
      }
      if (contains(norm, "золото", "золот")) {
         return class_1802.field_8695;
      }
      if (contains(norm, "желез")) {
         return class_1802.field_8620;
      }
      if (contains(norm, "аметист")) {
         return class_1802.field_27063;
      }
      if (contains(norm, "сердце моря")) {
         return class_1802.field_8207;
      }
      if (contains(norm, "жемчуг")) {
         return class_1802.field_8864;
      }
      if (contains(norm, "эхо", "echo")) {
         return class_1802.field_38746;
      }
      return null;
   }

   public void deliver(class_1657 player, List<class_1799> stacks, String line) {
      if (player == null || stacks == null || stacks.isEmpty()) {
         return;
      }

      double dist = this.knocker.method_5739(player);
      boolean needToComeCloser = dist > 3.0 || this.knocker.isAway() || this.knocker.method_5767()
            || this.phase() <= KnockerPhase.CURIOUS.ordinal();
      if (needToComeCloser && !this.movement.comeToGift(player)) {
         this.movement.teleportBehind(player);
      }

      this.knocker.method_5988().method_6226(player, 30.0F, 30.0F);
      this.knocker.playAction("give");
      class_1937 world = this.knocker.method_37908();

      for (class_1799 stack : stacks) {
         if (stack.method_7960()) {
            continue;
         }
         // Падаем прямо к её ногам, без разлёта по сторонам.
         class_1542 item = new class_1542(world, player.method_23317(), player.method_23318() + 0.3, player.method_23321(), stack.method_7972());
         item.method_18800(0.0, 0.05, 0.0);
         item.method_6982(10);
         world.method_8649(item);
         this.knocker.markOwnDrop(item);
      }

      if (line != null && !line.isEmpty()) {
         this.knocker.getChat().say(player, line);
      }
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
}
