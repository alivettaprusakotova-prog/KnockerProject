package com.theneru.knocker.entity;

import com.theneru.knocker.AiKnocker;
import com.theneru.knocker.KnockerAdvancements;
import com.theneru.knocker.KnockerConfig;
import com.theneru.knocker.KnockerMemoryStore;
import com.theneru.knocker.ModSounds;
import com.theneru.knocker.util.KnockerMessages;
import com.theneru.knocker.util.KnockerWords;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1937;
import net.minecraft.class_2244;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2398;
import net.minecraft.class_2399;
import net.minecraft.class_2401;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2533;
import net.minecraft.class_2561;
import net.minecraft.class_2586;
import net.minecraft.class_2595;
import net.minecraft.class_2680;
import net.minecraft.class_2738;
import net.minecraft.class_2742;
import net.minecraft.class_2760;
import net.minecraft.class_3222;
import net.minecraft.class_3749;
import net.minecraft.class_5903;
import net.minecraft.class_5904;
import net.minecraft.class_5905;
import net.minecraft.class_9334;
import net.minecraft.class_2902.class_2903;

public class KnockerEnding {
   public static final String GONE_TAG = "aiknocker_gone";
   private final KnockerEntity knocker;
   private final KnockerMovement movement;
   private boolean done = false;
   private Stage stage;
   private int stageTick;
   private int nextTryTick;
   private class_1657 target;
   private class_2338 basement;
   private boolean caged;
   private int basementSince;
   private int lastBreakfastDay;
   private boolean glassBroken;
   private int leaveTimer;

   public KnockerEnding(KnockerEntity knocker, KnockerMovement movement) {
      this.stage = KnockerEnding.Stage.NONE;
      this.stageTick = 0;
      this.nextTryTick = 0;
      this.target = null;
      this.basement = null;
      this.caged = false;
      this.basementSince = -1;
      this.lastBreakfastDay = -1;
      this.glassBroken = false;
      this.leaveTimer = 0;
      this.knocker = knocker;
      this.movement = movement;
   }

   public boolean isActive() {
      return this.stage != KnockerEnding.Stage.NONE;
   }

   public boolean tick(class_1657 player) {
      int now = this.knocker.tickCounter;
      switch (this.stage.ordinal()) {
         case 0:
            this.tickEscape(player);
            if (this.done && this.knocker.getObsessionPoints() < 950) {
               this.done = false;
               this.nextTryTick = now + 1200;
            }

            if (!this.done && player != null && now >= this.nextTryTick) {
               if (this.knocker.getObsessionPoints() >= 1000 && this.knocker.method_37908().method_23886()) {
                  if (!(this.knocker.method_5739(player) > 24.0F) && !this.knocker.getGuard().isFighting() && !this.knocker.isAway()) {
                     this.startAsking(player);
                     return true;
                  }

                  return false;
               }

               return false;
            }

            return false;
         case 1:
            if (this.target != null && !this.target.method_31481() && this.target.method_5805()) {
               this.knocker.method_5942().method_6340();
               this.knocker.method_5988().method_6226(this.target, 90.0F, 90.0F);
               if (now - this.stageTick == 60) {
                  this.knocker.getChat().say(this.target, "Ответь мне.");
               }

               if (now - this.stageTick > 1200) {
                  this.refused();
               }

               return true;
            }

            this.stage = KnockerEnding.Stage.NONE;
            this.nextTryTick = now + 2400;
            return false;
         case 2:
            this.tickGood(now - this.stageTick);
            return true;
         case 3:
            this.tickBad(now - this.stageTick);
            return true;
         case 4:
            this.tickBasement(now - this.stageTick);
            return true;
         default:
            return false;
      }
   }

   private void startAsking(class_1657 player) {
      this.target = player;
      this.stage = KnockerEnding.Stage.ASKING;
      this.stageTick = this.knocker.tickCounter;
      this.movement.appearInFront(player);
      this.knocker.playSoundFor(player, ModSounds.STINGER, 0.8F, 0.8F);
      this.knocker.getChat().say(player, "Ты останешься со мной навсегда?");
      this.knocker.getThoughts().force(player, "сейчас... или никогда", false, 6);
   }

   public boolean onChat(class_1657 player, String message) {
      if (this.stage != KnockerEnding.Stage.ASKING) {
         return this.stage == KnockerEnding.Stage.GOOD || this.stage == KnockerEnding.Stage.BAD || this.stage == KnockerEnding.Stage.BASEMENT;
      } else {
         String norm = KnockerMessages.normalize(message);
         boolean yes = KnockerMessages.containsAny(norm, "да", "останусь", "я останусь", "навсегда", "останусь с тобой", "конечно", "хорошо", "ладно", "согласна", "я твоя") || KnockerMessages.containsAny(norm, KnockerWords.I_AM_YOURS);
         boolean no = KnockerMessages.containsAny(norm, KnockerWords.NO) || KnockerMessages.containsAny(norm, KnockerWords.GO_AWAY) || KnockerMessages.containsAny(norm, KnockerWords.INSULT_PHRASES) || KnockerMessages.containsAny(norm, KnockerWords.BYE) || KnockerMessages.containsAny(norm, "не останусь", "никогда", "ни за что", "уйду", "я уйду");
         if (no && !norm.contains(" не нет ")) {
            this.refused();
         } else if (yes) {
            this.startGood();
         } else {
            this.knocker.getChat().say(player, "Да или нет?");
         }

         return true;
      }
   }

   public void onHit() {
      if (this.stage == KnockerEnding.Stage.ASKING) {
         this.startBad();
      }

   }

   private void refused() {
      if (this.knocker.method_59922().method_43056()) {
         this.startBasement();
      } else {
         this.startBad();
      }

   }

   private void startGood() {
      this.stage = KnockerEnding.Stage.GOOD;
      this.stageTick = this.knocker.tickCounter;
      this.knocker.getChat().say(this.target, "...Я знал.");
      this.target.method_6092(new class_1293(class_1294.field_38092, 140, 0, false, false));
      this.target.method_6092(new class_1293(class_1294.field_5919, 80, 0, false, false));
   }

   private void tickGood(int t) {
      if (t == 40) {
         this.knocker.getThoughts().force(this.target, "ОНА СКАЗАЛА ДА", true, 6);
      }

      if (t == 70) {
         class_2338 center = this.buildCage(this.target);
         this.basement = center;
         this.caged = true;
         this.target.method_5859((double)center.method_10263() + (double)0.5F, (double)center.method_10264(), (double)center.method_10260() + (double)0.5F);
         this.movement.teleportTo(new class_243((double)center.method_10263() + (double)0.5F, (double)center.method_10264(), (double)center.method_10260() + (double)3.5F));
      }

      if (t == 110) {
         title(this.target, "§dНАВСЕГДА ВМЕСТЕ", "§7Концовка: ты осталась... здесь");
         KnockerAdvancements.grant(this.target, "forever");
         this.knocker.playAction("shy");
      }

      if (t == 180) {
         this.knocker.getChat().say(this.target, "Не бойся. Это не клетка. Это чтобы тебя никто не забрал.");
      }

      if (t == 300) {
         this.knocker.getChat().say(this.target, "Я буду приносить тебе еду. Каждое утро. И книги... ты же любишь читать?");
      }

      if (t == 420) {
         this.knocker.getChat().say(this.target, "Теперь ты никуда не уйдёшь. И я тоже.");
         this.knocker.setObsessionPoints(650);
         this.knocker.rememberEvent("она сказала «да», и я поселил её в стеклянной комнате под землёй, чтобы никто не забрал");
         this.finish();
      }

   }

   private class_2338 buildCage(class_1657 player) {
      class_1937 world = this.knocker.method_37908();
      class_2338 home = this.knocker.getHomePos() != null ? this.knocker.getHomePos() : player.method_24515();
      int floorY = Math.max(world.method_31607() + 2, home.method_10264() - 14);
      class_2338 c = new class_2338(home.method_10263(), floorY, home.method_10260());

      for(int x = -5; x <= 5; ++x) {
         for(int z = -5; z <= 5; ++z) {
            for(int y = -1; y <= 4; ++y) {
               class_2338 p = c.method_10069(x, y, z);
               boolean wall = Math.abs(x) == 5 || Math.abs(z) == 5 || y == -1 || y == 4;
               boolean glass = !wall && (Math.abs(x) == 2 || Math.abs(z) == 2 || y == 3) && Math.abs(x) <= 2 && Math.abs(z) <= 2;
               class_2680 st = wall ? class_2246.field_28900.method_9564() : (glass ? class_2246.field_10033.method_9564() : class_2246.field_10124.method_9564());
               world.method_8501(p, st);
            }
         }
      }

      for(int i = -4; i <= 4; ++i) {
         for(int y = 0; y <= 2; ++y) {
            world.method_8501(c.method_10069(i, y, -4), class_2246.field_10504.method_9564());
            world.method_8501(c.method_10069(-4, y, i), class_2246.field_10504.method_9564());
         }
      }

      world.method_8501(c.method_10069(3, 3, 3), (class_2680)class_2246.field_16541.method_9564().method_11657(class_3749.field_16545, true));
      world.method_8501(c.method_10069(-3, 3, 3), (class_2680)class_2246.field_16541.method_9564().method_11657(class_3749.field_16545, true));
      world.method_8501(c.method_10069(0, 0, 4), class_2246.field_10616.method_9564());
      class_2680 bed = (class_2680)class_2246.field_10610.method_9564().method_11657(class_2244.field_11177, class_2350.field_11043);
      world.method_8501(c.method_10069(-1, 0, 0), (class_2680)bed.method_11657(class_2244.field_9967, class_2742.field_12557));
      world.method_8501(c.method_10069(-1, 0, -1), (class_2680)bed.method_11657(class_2244.field_9967, class_2742.field_12560));

      for(int x = 0; x <= 1; ++x) {
         for(int z = -1; z <= 1; ++z) {
            world.method_8501(c.method_10069(x, 0, z), class_2246.field_10536.method_9564());
         }
      }

      world.method_8501(c.method_10069(-1, 0, 1), class_2246.field_10536.method_9564());
      world.method_8501(c.method_10069(1, 2, 1), (class_2680)class_2246.field_16541.method_9564().method_11657(class_3749.field_16545, true));
      buildHatch(world, c.method_10069(4, 0, 4));
      this.glassBroken = false;
      return c;
   }

   private static void buildHatch(class_1937 world, class_2338 bottom) {
      int surface = world.method_8624(class_2903.field_13203, bottom.method_10263(), bottom.method_10260());
      int hatchY = Math.max(bottom.method_10264() + 6, surface - 1);
      class_2680 wall = class_2246.field_28900.method_9564();
      class_2680 ladder = (class_2680)class_2246.field_9983.method_9564().method_11657(class_2399.field_11253, class_2350.field_11043);

      for(int y = bottom.method_10264(); y < hatchY; ++y) {
         class_2338 p = new class_2338(bottom.method_10263(), y, bottom.method_10260());
         world.method_8501(p.method_10072(), wall);
         if (y > bottom.method_10264() + 4) {
            for(class_2350 d : new class_2350[]{class_2350.field_11043, class_2350.field_11034, class_2350.field_11039}) {
               if (!world.method_8320(p.method_10093(d)).method_26212(world, p.method_10093(d))) {
                  world.method_8501(p.method_10093(d), wall);
               }
            }
         }

         world.method_8501(p, ladder);
      }

      class_2338 hatch = new class_2338(bottom.method_10263(), hatchY, bottom.method_10260());
      world.method_8501(hatch, (class_2680)((class_2680)class_2246.field_10453.method_9564().method_11657(class_2533.field_11177, class_2350.field_11043)).method_11657(class_2533.field_11625, class_2760.field_12619));
      class_2338 nook = hatch.method_10074().method_10067();
      world.method_8501(nook.method_10074(), wall);
      world.method_8501(nook.method_10084(), wall);
      world.method_8501(nook, (class_2680)((class_2680)class_2246.field_10363.method_9564().method_11657(class_2401.field_11007, class_2738.field_12473)).method_11657(class_2401.field_11177, class_2350.field_11034));
      world.method_8501(hatch.method_10084(), class_2246.field_10124.method_9564());
   }

   private void startBad() {
      this.stage = KnockerEnding.Stage.BAD;
      this.stageTick = this.knocker.tickCounter;
      this.knocker.onInsulted();
      this.knocker.showScareLook();
      this.knocker.playAction("scream");
      this.knocker.playSoundFor(this.target, ModSounds.SCREAM, 0.6F, 0.8F);
      this.knocker.getChat().say(this.target, "НЕТ?");
      this.knocker.getThoughts().force(this.target, "НЕТ НЕТ НЕТ НЕТ НЕТ", true, 0);
      this.target.method_6092(new class_1293(class_1294.field_38092, 200, 0, false, false));
   }

   private void tickBad(int t) {
      if (t == 40) {
         this.movement.appearInFront(this.target);
         this.knocker.playSoundFor(this.target, ModSounds.LAUGH_DISTORTED, 0.8F, 1.0F);
         this.knocker.getChat().say(this.target, "ЕСЛИ НЕ МНОЙ — ТО НИКЕМ.");
      }

      if (t == 90) {
         this.knocker.playAction("break");
         title(this.target, "§4ЕСЛИ НЕ МНОЙ — ТО НИКЕМ", "§7Концовка: ты ушла");
         KnockerAdvancements.grant(this.target, "nobody");
         this.target.method_5643(this.knocker.method_48923().method_48812(this.knocker), 1000.0F);
      }

      if (t == 110) {
         class_1799 note = new class_1799(class_1802.field_8407);
         note.method_57379(class_9334.field_49631, class_2561.method_43470("§7ты сама выбрала"));
         this.knocker.method_5775(note);
         class_1657 var4 = this.target;
         if (var4 instanceof class_3222) {
            class_3222 sp = (class_3222)var4;
            KnockerMemoryStore.addGlobalMark(sp.method_5667(), "killed");
            KnockerMemoryStore.forgetExcept(sp.method_5667(), "мне снится, что я уже убил её однажды. она сказала «нет». но это просто сон... правда?");
            AiKnocker.scheduleReturn(sp, 0, 6000);
         }

         this.finish();
         this.knocker.method_31472();
      }

   }

   private void startBasement() {
      this.stage = KnockerEnding.Stage.BASEMENT;
      this.stageTick = this.knocker.tickCounter;
      this.knocker.onInsulted();
      this.knocker.getChat().say(this.target, "...Нет?");
      this.knocker.getThoughts().force(this.target, "она не понимает. я ей помогу.", false, 3);
      this.knocker.playSoundFor(this.target, ModSounds.STINGER, 0.8F, 0.7F);
      this.target.method_6092(new class_1293(class_1294.field_38092, 220, 0, false, false));
      this.target.method_6092(new class_1293(class_1294.field_5919, 120, 0, false, false));
   }

   private void tickBasement(int t) {
      if (t == 40) {
         this.knocker.getChat().say(this.target, "Тогда я не дам тебе уйти.");
      }

      if (t == 90) {
         class_2338 center = this.buildBasement(this.target);
         this.basement = center;
         this.caged = false;
         this.target.method_5859((double)center.method_10263() + (double)0.5F, (double)center.method_10264(), (double)center.method_10260() + (double)0.5F);
         this.movement.teleportTo(new class_243((double)center.method_10263() + (double)1.5F, (double)center.method_10264(), (double)center.method_10260() + (double)1.5F));
         this.knocker.playSoundFor(this.target, ModSounds.KNOCK, 1.0F, 0.8F);
      }

      if (t == 130) {
         title(this.target, "§4ТЫ НИКУДА НЕ УЙДЁШЬ", "§7Концовка: подвал");
         KnockerAdvancements.grant(this.target, "basement");
         this.knocker.playAction("head_tilt");
      }

      if (t == 200) {
         this.knocker.getChat().say(this.target, "Здесь тихо. Никого нет. Только мы.");
      }

      if (t == 300) {
         this.knocker.getChat().say(this.target, "Можешь попробовать выбраться... хе-хе. Я подожду.");
         this.knocker.setObsessionPoints(850);
         this.knocker.rememberEvent("она сказала «нет», и я запер её в подвале под землёй");
         this.stage = KnockerEnding.Stage.NONE;
         this.nextTryTick = this.knocker.tickCounter + KnockerConfig.ticks(KnockerConfig.endingRetry);
      }

   }

   private void tickEscape(class_1657 player) {
      if (this.basement != null && player != null && this.knocker.tickCounter % 40 == 0) {
         double dy = player.method_23318() - (double)this.basement.method_10264();
         boolean inside = player.method_5707(class_243.method_24953(this.basement)) <= (double)225.0F && dy <= (double)8.0F;
         if (inside && !this.caged) {
            if (this.basementSince < 0) {
               this.basementSince = this.knocker.tickCounter;
            }

            if (this.knocker.tickCounter - this.basementSince > 6000) {
               this.basementSince = -1;
               this.basement = null;
               class_2338 up = this.knocker.method_37908().method_8598(class_2903.field_13203, player.method_24515());
               class_2338 home = this.knocker.getHomePos();
               class_2338 out = home != null ? home.method_10084() : up;
               player.method_6092(new class_1293(class_1294.field_5919, 60, 0, false, false));
               player.method_5859((double)out.method_10263() + (double)0.5F, (double)out.method_10264(), (double)out.method_10260() + (double)0.5F);
               this.knocker.getChat().say(player, "...иди. в этот раз я тебя отпускаю. в этот раз.");
               this.knocker.rememberEvent("я выпустил её из подвала");
               return;
            }
         }

         if (!inside) {
            this.basementSince = -1;
         }

         if (inside && this.caged) {
            class_243 rel = player.method_19538().method_1020(class_243.method_24955(this.basement));
            boolean inGlass = Math.abs(rel.field_1352) < (double)2.0F && Math.abs(rel.field_1350) < (double)2.0F;
            if (inGlass && !this.glassBroken) {
               player.method_6092(new class_1293(class_1294.field_5901, 80, 1, false, false));
            } else if (!inGlass && !this.glassBroken) {
               this.glassBroken = true;
               this.knocker.method_5988().method_6226(player, 90.0F, 90.0F);
               this.knocker.playSoundFor(player, ModSounds.STINGER, 0.6F, 0.6F);
               this.knocker.getChat().say(player, this.knocker.getChat().getBrain().pick(new String[]{"...ты разбила стекло.", "Зачем ты это сделала?..", "Тебе было плохо в нашей комнате?"}));
               this.knocker.getThoughts().force(player, "она хочет уйти.", false, 4);
               this.leaveTimer = 3;
            }

            if (this.leaveTimer > 0 && --this.leaveTimer <= 2) {
               this.leaveTimer = 0;
               this.knocker.getChat().say(player, this.knocker.getChat().getBrain().pick(new String[]{"Люк наверху открывается рычагом. Иди... если хочешь.", "Я не буду держать. Наверху люк. Но я буду ждать.", "Иди. Я подожду наверху. Я всегда жду."}));
               this.knocker.spawnParticles(class_2398.field_11237, 15);
               class_2338 home = this.knocker.getHomePos();
               class_2338 top = this.knocker.method_37908().method_8598(class_2903.field_13203, home != null ? home : this.basement);
               this.movement.teleportTo(class_243.method_24955(top));
            }

            if (!this.glassBroken) {
               int day = (int)(this.knocker.method_37908().method_8532() / 24000L);
               long time = this.knocker.method_37908().method_8532() % 24000L;
               if (day != this.lastBreakfastDay && time > 0L && time < 3000L) {
                  this.lastBreakfastDay = day;
                  class_1799 food = new class_1799(this.knocker.method_59922().method_43056() ? class_1802.field_8229 : class_1802.field_8544, 3);
                  class_1799 book = new class_1799(class_1802.field_8529);
                  this.knocker.method_37908().method_8649(new class_1542(this.knocker.method_37908(), player.method_23317(), player.method_23318() + (double)0.5F, player.method_23321(), food));
                  if (this.knocker.method_59922().method_43048(3) == 0) {
                     this.knocker.method_37908().method_8649(new class_1542(this.knocker.method_37908(), player.method_23317(), player.method_23318() + (double)0.5F, player.method_23321(), book));
                  }

                  this.knocker.getChat().say(player, this.knocker.getChat().getBrain().pick(new String[]{"Доброе утро. Завтрак.", "Я принёс тебе поесть... ешь, пожалуйста", "Ты хорошо спала? Я смотрел."}));
               }

            }
         } else if (!inside) {
            this.basement = null;
            if (this.caged) {
               this.caged = false;
               this.glassBroken = false;
               this.knocker.rememberEvent("она разбила стекло и ушла из нашей комнаты");
               this.knocker.getChat().say(player, this.knocker.method_59922().method_43056() ? "Ты разбила стекло... Тебе было плохо со мной?" : "Зачем?.. Я же всё для тебя сделал. Ладно. Я буду рядом. Всегда.");
               this.knocker.addObsession(100);
               this.knocker.getThoughts().force(player, "она ушла. ОНА УШЛА.", true, 4);
            } else {
               this.knocker.rememberEvent("она выбралась из подвала");
               this.knocker.getChat().say(player, this.knocker.method_59922().method_43056() ? "Выбралась?.. Хе-хе. Умница. Но я всё равно рядом." : "ТЫ ВЫБРАЛАСЬ. Ничего... В следующий раз я построю глубже.");
               this.knocker.playSoundFor(player, ModSounds.LAUGH_DISTORTED, 0.6F, 1.0F);
            }

         }
      }
   }

   private class_2338 buildBasement(class_1657 player) {
      class_1937 world = this.knocker.method_37908();
      int floorY = Math.max(world.method_31607() + 2, player.method_31478() - 14);
      class_2338 c = new class_2338(player.method_31477(), floorY, player.method_31479());

      for(int x = -3; x <= 3; ++x) {
         for(int z = -3; z <= 3; ++z) {
            for(int y = -1; y <= 3; ++y) {
               class_2338 p = c.method_10069(x, y, z);
               boolean wall = Math.abs(x) == 3 || Math.abs(z) == 3 || y == -1 || y == 3;
               world.method_8501(p, wall ? class_2246.field_29031.method_9564() : class_2246.field_10124.method_9564());
            }
         }
      }

      world.method_8501(c.method_10069(0, 2, 0), (class_2680)class_2246.field_22110.method_9564().method_11657(class_3749.field_16545, true));
      class_2680 bed = (class_2680)class_2246.field_10461.method_9564().method_11657(class_2244.field_11177, class_2350.field_11043);
      world.method_8501(c.method_10069(-2, 0, -1), (class_2680)bed.method_11657(class_2244.field_9967, class_2742.field_12557));
      world.method_8501(c.method_10069(-2, 0, -2), (class_2680)bed.method_11657(class_2244.field_9967, class_2742.field_12560));
      class_2338 chestPos = c.method_10069(2, 0, -2);
      world.method_8501(chestPos, class_2246.field_10034.method_9564());
      class_2586 notes = world.method_8321(chestPos);
      if (notes instanceof class_2595 chest) {
         String[] notes = new String[]{"день 1. я смотрел, как ты спишь", "ты сказала «нет». ты не это имела в виду", "здесь тебя никто не найдёт. кроме меня", "я считаю твои вдохи", "ты моя"};

         for(int i = 0; i < notes.length; ++i) {
            class_1799 note = new class_1799(class_1802.field_8407);
            note.method_57379(class_9334.field_49631, class_2561.method_43470("§7" + notes[i]));
            chest.method_5447(i * 2, note);
         }

         class_1799 pick = new class_1799(class_1802.field_8387);
         pick.method_57379(class_9334.field_49631, class_2561.method_43470("§8если сможешь"));
         chest.method_5447(13, pick);
         chest.method_5447(15, new class_1799(class_1802.field_8229, 8));
         chest.method_5447(16, new class_1799(class_1802.field_8279, 4));
         chest.method_5447(17, new class_1799(class_1802.field_8810, 8));
      }

      return c;
   }

   private void finish() {
      this.done = true;
      this.stage = KnockerEnding.Stage.NONE;
      this.nextTryTick = Integer.MAX_VALUE;
   }

   private static void title(class_1657 player, String title, String subtitle) {
      if (player instanceof class_3222 sp) {
         sp.field_13987.method_14364(new class_5905(20, 100, 40));
         sp.field_13987.method_14364(new class_5903(class_2561.method_43470(subtitle)));
         sp.field_13987.method_14364(new class_5904(class_2561.method_43470(title)));
      }

   }

   public void writeNbt(class_2487 nbt) {
      nbt.method_10556("EndingDone", this.done);
      if (this.basement != null) {
         nbt.method_10544("Basement", this.basement.method_10063());
      }

      nbt.method_10556("Caged", this.caged);
   }

   public void readNbt(class_2487 nbt) {
      this.done = nbt.method_10577("EndingDone");
      this.basement = nbt.method_10545("Basement") ? class_2338.method_10092(nbt.method_10537("Basement")) : null;
      this.caged = nbt.method_10577("Caged");
   }

   private static enum Stage {
      NONE,
      ASKING,
      GOOD,
      BAD,
      BASEMENT;

      // $FF: synthetic method
      private static Stage[] $values() {
         return new Stage[]{NONE, ASKING, GOOD, BAD, BASEMENT};
      }
   }
}
