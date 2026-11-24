package com.theneru.knocker.entity;

import com.theneru.knocker.AiKnocker;
import com.theneru.knocker.KnockerAdvancements;
import com.theneru.knocker.KnockerConfig;
import com.theneru.knocker.KnockerMemoryStore;
import com.theneru.knocker.ModSounds;
import com.theneru.knocker.util.KnockerPhase;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.class_124;
import net.minecraft.class_1296;
import net.minecraft.class_1309;
import net.minecraft.class_1321;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2398;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2586;
import net.minecraft.class_2625;
import net.minecraft.class_2680;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_3726;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_3988;
import net.minecraft.class_5544;
import net.minecraft.class_8242;
import net.minecraft.class_9262;
import net.minecraft.class_9302;
import net.minecraft.class_9331;
import net.minecraft.class_9334;
import net.minecraft.class_2350.class_2353;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;

public class KnockerHaunt {
   private final KnockerEntity knocker;
   private final KnockerMovement movement;
   private final Random random = new Random();
   private int nextPeekTick = -1;
   private int nextTorchTick = 0;
   private int torchesThisNight = 0;
   private long lastPorchDay = -1L;
   private int nextFakeTick = -1;
   private int nextWindowTick = 0;
   private long lastWindowNight = -1L;
   private int jealousTalkTick = -100000;
   private int watchedTalkTick = -100000;
   private int creepTick = 0;
   private class_2338 knockGlass = null;
   private int knocksLeft = 0;
   private int knockTimer = 0;
   private String fakeFollowUp = null;
   private int fakeFollowTimer = 0;
   private int lastDiaryTick = -100000;
   private int nextNestTick = 2400;
   private int nestPieces = 0;

   public KnockerHaunt(KnockerEntity knocker, KnockerMovement movement) {
      this.knocker = knocker;
      this.movement = movement;
   }

   private int now() {
      return this.knocker.tickCounter;
   }

   public boolean tickWatched(class_1657 player) {
      KnockerPhase phase = this.knocker.getPhase();
      boolean active = phase == KnockerPhase.PARANOIA || phase == KnockerPhase.OBSESSION && this.knocker.method_37908().method_23886();
      if (active && !this.knocker.getGuard().isFighting()) {
         double dist = (double)this.knocker.method_5739(player);
         boolean watched = dist < (double)48.0F && this.knocker.method_6057(player) && KnockerGestures.looksAt(player, this.knocker.method_33571(), 0.55);
         if (watched) {
            this.knocker.method_5942().method_6340();
            this.knocker.method_5988().method_6226(player, 30.0F, 30.0F);
            if (dist < (double)6.0F && this.now() - this.watchedTalkTick > 6000 && this.random.nextInt(200) == 0) {
               this.watchedTalkTick = this.now();
               this.knocker.getChat().say(player, phase == KnockerPhase.PARANOIA ? "не моргай." : "не смотри на меня так...");
            }

            return true;
         } else {
            if (dist > (double)4.0F && ++this.creepTick >= 40 + this.random.nextInt(20)) {
               this.creepTick = 0;
               class_243 dir = player.method_19538().method_1020(this.knocker.method_19538()).method_18805((double)1.0F, (double)0.0F, (double)1.0F).method_1029();
               class_243 next = this.knocker.method_19538().method_1019(dir.method_1021(Math.min((double)3.0F, dist - (double)3.0F)));
               if (!KnockerGestures.looksAt(player, next.method_1031((double)0.0F, (double)1.5F, (double)0.0F), 0.4)) {
                  this.movement.teleportTo(next);
                  if (this.random.nextInt(3) == 0) {
                     this.knocker.method_37908().method_8396((class_1657)null, this.knocker.method_24515(), ModSounds.STEP, class_3419.field_15251, 0.4F, 0.8F);
                  }
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   public void tick(class_1657 player) {
      if (player instanceof class_3222 sp) {
         KnockerPhase phase = this.knocker.getPhase();
         int p = phase.ordinal();
         class_1937 world = this.knocker.method_37908();
         this.tickPeek(sp, phase);
         if (p >= KnockerPhase.OBSESSION.ordinal()) {
            this.tickNest(sp, phase);
         }

         if (p >= KnockerPhase.OBSESSION.ordinal()) {
            this.tickTorches(sp);
         }

         if (p >= KnockerPhase.ATTACHED.ordinal()) {
            this.tickPorch(sp, phase);
         }

         if (phase == KnockerPhase.PARANOIA) {
            this.tickFake(sp);
         }

         if (p >= KnockerPhase.LOVE.ordinal()) {
            this.tickWindow(sp, phase);
         }

         if (p >= KnockerPhase.LOVE.ordinal() && this.now() % 20 == 0) {
            this.tickJealousy(sp, phase);
         }

         if (this.knockGlass != null && --this.knockTimer <= 0) {
            world.method_8396((class_1657)null, this.knockGlass, ModSounds.KNOCK, class_3419.field_15245, 0.8F, 1.3F);
            this.knocker.playAction("knock");
            this.knockTimer = 14;
            if (--this.knocksLeft <= 0) {
               this.knockGlass = null;
            }
         }

         if (this.fakeFollowUp != null && --this.fakeFollowTimer <= 0) {
            this.knocker.getChat().say(sp, this.fakeFollowUp);
            this.fakeFollowUp = null;
         }

         if (!world.method_23886()) {
            this.torchesThisNight = 0;
         }

      }
   }

   private void tickPeek(class_3222 player, KnockerPhase phase) {
      if (phase.ordinal() <= KnockerPhase.ATTACHED.ordinal()) {
         if (this.nextPeekTick < 0) {
            this.nextPeekTick = this.now() + KnockerConfig.ticksBetween(KnockerConfig.peekMin, KnockerConfig.peekMax);
         }

         if (this.now() >= this.nextPeekTick && !this.knocker.isPeeking() && !this.knocker.getGuard().isFighting()) {
            this.nextPeekTick = this.now() + KnockerConfig.ticksBetween(KnockerConfig.peekMin, KnockerConfig.peekMax);
            class_243 spot = AiKnocker.findHideSpot(player, (double)12.0F, (double)24.0F, true);
            if (spot != null && AiKnocker.lastSpotIsTree) {
               this.movement.teleportTo(spot);
               this.knocker.startPeeking(player, 300 + this.random.nextInt(300));
            }
         }
      }
   }

   private void tickTorches(class_3222 player) {
      class_1937 world = this.knocker.method_37908();
      class_2338 home = this.knocker.getHomePos();
      if (home != null && world.method_23886() && this.now() >= this.nextTorchTick && this.torchesThisNight < 4) {
         if (!(player.method_5707(class_243.method_24953(home)) > (double)1024.0F)) {
            this.nextTorchTick = this.now() + KnockerConfig.ticks(KnockerConfig.torch);
            List<class_2338> torches = new ArrayList();

            for(class_2338 pos : class_2338.method_10097(home.method_10069(-10, -3, -10), home.method_10069(10, 4, 10))) {
               class_2680 st = world.method_8320(pos);
               if (st.method_27852(class_2246.field_10336) || st.method_27852(class_2246.field_10099) || st.method_27852(class_2246.field_22092) || st.method_27852(class_2246.field_22093)) {
                  torches.add(pos.method_10062());
               }
            }

            if (!torches.isEmpty()) {
               class_2338 t = (class_2338)torches.get(this.random.nextInt(torches.size()));
               world.method_8501(t, class_2246.field_10124.method_9564());
               world.method_8396((class_1657)null, t, class_3417.field_15102, class_3419.field_15245, 0.6F, 1.0F);
               if (world instanceof class_3218) {
                  class_3218 sw = (class_3218)world;
                  sw.method_14199(class_2398.field_11251, (double)t.method_10263() + (double)0.5F, (double)t.method_10264() + 0.6, (double)t.method_10260() + (double)0.5F, 8, 0.1, 0.1, 0.1, 0.01);
               }

               ++this.torchesThisNight;
               if (this.torchesThisNight == 1 && this.knocker.getChat().canSpeakUnprompted(600)) {
                  this.knocker.getChat().say(player, this.knocker.getPhase() == KnockerPhase.PARANOIA ? "В ТЕМНОТЕ ТЫ ВИДИШЬ ТОЛЬКО МЕНЯ." : "так лучше... в темноте ты ближе");
               }

            }
         }
      }
   }

   private void tickPorch(class_3222 player, KnockerPhase phase) {
      class_1937 world = this.knocker.method_37908();
      class_2338 home = this.knocker.getHomePos();
      if (home != null && this.now() % 100 == 0) {
         long day = world.method_8532() / 24000L;
         long time = world.method_8532() % 24000L;
         if (day != this.lastPorchDay && time <= 6000L) {
            if (!(player.method_5707(class_243.method_24953(home)) < (double)256.0F)) {
               this.lastPorchDay = day;
               class_243 at = class_243.method_24953(home).method_1031((double)0.0F, 0.6, (double)0.0F);
               class_1792[] flowers = new class_1792[]{class_1802.field_8880, class_1802.field_8491, class_1802.field_17513, class_1802.field_17501, class_1802.field_17512, class_1802.field_17514};
               class_1792 favFlower = KnockerFavorites.flower(player.method_5667());
               if (favFlower != null) {
                  flowers = new class_1792[]{favFlower};
               }

               class_1799 gift;
               String note;
               switch (phase) {
                  case ATTACHED:
                     gift = new class_1799(flowers[this.random.nextInt(flowers.length)]);
                     note = "это тебе... не говори никому";
                     break;
                  case LOVE:
                     gift = new class_1799(flowers[this.random.nextInt(flowers.length)], 3);
                     note = favFlower != null ? this.pick("твои любимые... я помню", "ты говорила, что любишь их", "я искал их всю ночь") : this.pick("я думал о тебе всю ночь", "ты красивая, когда спишь... ой", "доброе утро. это от меня");
                     break;
                  case OBSESSION:
                     gift = favFlower != null ? new class_1799(favFlower, 8) : (this.random.nextBoolean() ? new class_1799(class_1802.field_17512, 5) : new class_1799(class_1802.field_17534));
                     note = this.pick("я знаю, куда ты ходила вчера", "не забывай, чья ты", "я был здесь, пока тебя не было");
                     break;
                  default:
                     class_1792[] creepy = new class_1792[]{class_1802.field_17515, class_1802.field_8606, class_1802.field_8398, class_1802.field_8680};
                     gift = new class_1799(creepy[this.random.nextInt(creepy.length)]);
                     note = this.pick("Я ВИДЕЛ ТЕБЯ", "почему ты не дома", "это осталось от того, кто подошёл к тебе");
               }

               this.drop(at, gift);
               class_1799 paper = new class_1799(class_1802.field_8407);
               class_9331 var10001 = class_9334.field_49631;
               String var10002 = KnockerGender.adapt(note, player.method_5667());
               paper.method_57379(var10001, class_2561.method_43470("§7" + var10002));
               this.drop(at, paper);
               if (phase.ordinal() >= KnockerPhase.LOVE.ordinal() && this.random.nextInt(3) == 0) {
                  this.drop(at, this.diaryBook(player));
               }

               if (phase.ordinal() >= KnockerPhase.OBSESSION.ordinal()) {
                  this.placeSign(home, phase == KnockerPhase.PARANOIA ? new String[]{"Я", "ЗНАЮ", "ГДЕ ТЫ", "СПИШЬ"} : new String[]{"здесь", "живёт", KnockerGender.get(player.method_5667()) == KnockerGender.G.MALE ? "мой" : "моя", "♥"});
               }

               this.knocker.rememberEvent("оставил ей подарок у кровати");
            }
         }
      }
   }

   public boolean handleChat(class_1657 player, String norm) {
      if (!norm.contains("дневник") && !norm.contains("записи") && !norm.contains("что ты пишешь")) {
         return false;
      } else {
         KnockerPhase phase = this.knocker.getPhase();
         if (phase.ordinal() < KnockerPhase.ATTACHED.ordinal()) {
            this.knocker.getChat().say(player, "какой дневник?.. у меня нет никакого дневника");
            return true;
         } else if (this.now() - this.lastDiaryTick < 6000) {
            this.knocker.getChat().say(player, "я уже давал... перечитай");
            return true;
         } else {
            this.lastDiaryTick = this.now();
            this.knocker.playAction("give");
            player.method_31548().method_7398(this.diaryBook(player));
            KnockerAdvancements.grant(player, "diary");
            KnockerChat var10000 = this.knocker.getChat();
            String var10002;
            switch (phase) {
               case ATTACHED -> var10002 = "только не смейся... ладно?";
               case LOVE -> var10002 = "там... про тебя. почти всё. не читай вслух";
               case OBSESSION -> var10002 = "Читай. Там всё о тебе. Всё.";
               default -> var10002 = "ЧИТАЙ. ТЕПЕРЬ ТЫ ЗНАЕШЬ, ЧТО Я ЗНАЮ.";
            }

            var10000.say(player, var10002);
            this.knocker.getThoughts().show(player, "она читает мои записи...", false, 3);
            return true;
         }
      }
   }

   private class_1799 diaryBook(class_1657 player) {
      List<String> lines = new ArrayList();
      lines.add("Я опять пишу о ней.");

      for(String e : this.knocker.getEvents()) {
         lines.add(e);
      }

      for(String f : KnockerMemoryStore.diary(player.method_5667())) {
         lines.add(f);
      }

      if (this.knocker.getPhase() == KnockerPhase.PARANOIA) {
         lines.add("ОНА МОЯ ОНА МОЯ ОНА МОЯ ОНА МОЯ ОНА МОЯ");
      } else {
         lines.add("Завтра напишу ещё.");
      }

      List<class_9262<class_2561>> pages = new ArrayList();
      StringBuilder page = new StringBuilder();

      for(String l : lines) {
         if (page.length() + l.length() > 200) {
            pages.add(class_9262.method_57137(class_2561.method_43470(page.toString())));
            page.setLength(0);
            if (pages.size() >= 30) {
               break;
            }
         }

         page.append("- ").append(l).append("\n\n");
      }

      if (page.length() > 0 && pages.size() < 30) {
         pages.add(class_9262.method_57137(class_2561.method_43470(page.toString())));
      }

      class_1799 book = new class_1799(class_1802.field_8360);
      book.method_57379(class_9334.field_49606, new class_9302(class_9262.method_57137("Дневник"), "Кнокер", 0, pages, true));
      return book;
   }

   private void placeSign(class_2338 home, String[] text) {
      class_1937 world = this.knocker.method_37908();

      for(class_2350 d : class_2353.field_11062) {
         for(int dist = 1; dist <= 2; ++dist) {
            class_2338 pos = home.method_10079(d, dist);
            if (world.method_8320(pos).method_26215() && world.method_8320(pos.method_10074()).method_26212(world, pos.method_10074())) {
               world.method_8501(pos, class_2246.field_10330.method_9564());
               class_2586 var9 = world.method_8321(pos);
               if (var9 instanceof class_2625) {
                  class_2625 sign = (class_2625)var9;
                  class_8242 st = new class_8242();

                  for(int i = 0; i < 4 && i < text.length; ++i) {
                     st = st.method_49857(i, class_2561.method_43470(text[i]));
                  }

                  sign.method_49840(st, true);
                  sign.method_5431();
               }

               return;
            }
         }
      }

   }

   private boolean sameRoom(class_1937 world, class_2338 bed, class_2338 p) {
      boolean indoorBed = !world.method_8311(bed.method_10084());
      if (indoorBed && world.method_8311(p)) {
         return false;
      } else {
         class_243 from = class_243.method_24953(bed).method_1031((double)0.0F, 0.6, (double)0.0F);
         class_243 to = class_243.method_24953(p);
         class_3965 hit = world.method_17742(new class_3959(from, to, class_3960.field_17558, class_242.field_1348, class_3726.method_16194()));
         return hit.method_17783() == class_240.field_1333 || hit.method_17777().equals(p) || hit.method_17777().equals(bed);
      }
   }

   private void tickNest(class_3222 player, KnockerPhase phase) {
      class_1937 world = this.knocker.method_37908();
      class_2338 home = this.knocker.getHomePos();
      if (home != null && this.now() >= this.nextNestTick && this.nestPieces < 14) {
         if (!(player.method_5707(class_243.method_24953(home)) < (double)144.0F)) {
            this.nextNestTick = this.now() + 2400 + this.random.nextInt(2400);
            class_2680[] calm = new class_2680[]{(class_2680)class_2246.field_27099.method_9564().method_11657(class_5544.field_27175, true), (class_2680)class_2246.field_27100.method_9564().method_11657(class_5544.field_27175, true), class_2246.field_10151.method_9564(), class_2246.field_10074.method_9564(), class_2246.field_9981.method_9564(), class_2246.field_10536.method_9564()};
            class_2680[] dark = new class_2680[]{(class_2680)class_2246.field_27141.method_9564().method_11657(class_5544.field_27175, true), (class_2680)class_2246.field_27140.method_9564().method_11657(class_5544.field_27175, true), class_2246.field_9998.method_9564(), class_2246.field_10481.method_9564(), class_2246.field_10343.method_9564()};
            class_2680[] pool = phase == KnockerPhase.PARANOIA && this.random.nextBoolean() ? dark : calm;

            for(int attempt = 0; attempt < 20; ++attempt) {
               class_2338 pos = home.method_10069(this.random.nextInt(7) - 3, 0, this.random.nextInt(7) - 3);

               for(int dy = -1; dy <= 1; ++dy) {
                  class_2338 p = pos.method_10086(dy);
                  if (world.method_8320(p).method_26215() && world.method_8320(p.method_10074()).method_26212(world, p.method_10074()) && this.sameRoom(world, home, p)) {
                     world.method_8501(p, pool[this.random.nextInt(pool.length)]);
                     ++this.nestPieces;
                     if (this.nestPieces == 1) {
                        this.knocker.rememberEvent("я начал украшать место вокруг её кровати. наше гнездо");
                     }

                     if (this.nestPieces == 6) {
                        this.knocker.getChat().say(player, phase == KnockerPhase.PARANOIA ? "Я украсил твою кровать. Тебе нравится? СКАЖИ, ЧТО НРАВИТСЯ." : "Я кое-что сделал у твоей кровати... для нас.");
                     }

                     return;
                  }
               }
            }

         }
      }
   }

   private void tickFake(class_3222 player) {
      if (this.nextFakeTick < 0) {
         this.nextFakeTick = this.now() + KnockerConfig.ticksBetween(KnockerConfig.fakeMin, KnockerConfig.fakeMax);
      }

      if (this.now() >= this.nextFakeTick && this.fakeFollowUp == null) {
         this.nextFakeTick = this.now() + KnockerConfig.ticksBetween(KnockerConfig.fakeMin, KnockerConfig.fakeMax);
         class_2561 name = player.method_5476();
         switch (this.random.nextInt(6)) {
            case 0:
               player.method_7353(class_2561.method_43469("multiplayer.player.left", new Object[]{name}).method_27692(class_124.field_1054), false);
               this.fakeFollowUp = "шучу. ты никуда не уйдёшь.";
               break;
            case 1:
               player.method_7353(class_2561.method_43469("multiplayer.player.joined", new Object[]{class_2561.method_43470("Друг")}).method_27692(class_124.field_1054), false);
               this.fakeFollowUp = "он пришёл. наконец-то. ...хе-хе. нет. никто не придёт.";
               break;
            case 2:
               player.method_7353(class_2561.method_43470(player.method_5477().getString() + " был убит Кнокером").method_27692(class_124.field_1080), false);
               this.fakeFollowUp = "пока нет.";
               break;
            case 3:
            default:
               player.method_7353(class_2561.method_43470("[Сервер] ты здесь не одна").method_27692(class_124.field_1076), false);
               this.fakeFollowUp = null;
               break;
            case 4:
            case 5:
               String fake = KnockerGender.adapt(this.pick("я люблю кнокера", "я никуда не уйду", "он рядом. он всегда рядом", "не ищите меня", "мне хорошо здесь", "я его", "кто-нибудь видит его?", "я сама его позвала"), player.method_5667());
               class_2561 line = class_2561.method_43469("chat.type.text", new Object[]{name, class_2561.method_43470(fake)});
               if (this.knocker.method_37908().method_8503() != null) {
                  this.knocker.method_37908().method_8503().method_3760().method_14571().forEach((p) -> p.method_7353(line, false));
               }

               this.knocker.rememberEvent("я написал в чат от её имени: «" + fake + "»");
               this.fakeFollowUp = this.pick("видишь? ты сама это сказала.", "это же правда. правда?", "теперь все знают.");
               this.fakeFollowTimer = 80;
               return;
         }

         this.fakeFollowTimer = 60;
      }
   }

   private void tickWindow(class_3222 player, KnockerPhase phase) {
      class_1937 world = this.knocker.method_37908();
      class_2338 home = this.knocker.getHomePos();
      if (home != null && world.method_23886() && this.now() >= this.nextWindowTick && this.knockGlass == null) {
         long night = world.method_8532() / 24000L;
         if (night != this.lastWindowNight) {
            if (!(player.method_5707(class_243.method_24953(home)) > (double)100.0F) && !world.method_8311(player.method_24515())) {
               if (!player.method_6113()) {
                  if (this.knocker.getTicksSinceTalk() >= 1200 && !this.knocker.getGames().isBusy()) {
                     if (!(this.knocker.method_5739(player) < 12.0F) || world.method_8311(this.knocker.method_24515().method_10084())) {
                        this.nextWindowTick = this.now() + KnockerConfig.ticks(KnockerConfig.windowKnock);
                        class_2338 pp = player.method_24515();

                        for(class_2338 g : class_2338.method_10097(pp.method_10069(-7, -1, -7), pp.method_10069(7, 2, 7))) {
                           if (KnockerDoors.isGlass(world.method_8320(g))) {
                              for(class_2350 d : class_2353.field_11062) {
                                 class_2338 out = g.method_10093(d);
                                 if (!(out.method_10262(pp) <= g.method_10262(pp))) {
                                    class_2338 feet = out;

                                    for(int i = 0; i < 3 && !world.method_8320(feet.method_10074()).method_26212(world, feet.method_10074()); ++i) {
                                       feet = feet.method_10074();
                                    }

                                    if (world.method_8320(feet).method_26215() && world.method_8320(feet.method_10084()).method_26215() && world.method_8311(feet)) {
                                       this.lastWindowNight = night;
                                       this.movement.teleportTo(class_243.method_24955(feet));
                                       this.knocker.method_5988().method_6226(player, 90.0F, 90.0F);
                                       this.knockGlass = g.method_10062();
                                       this.knocksLeft = 3;
                                       this.knockTimer = 20;
                                       KnockerChat var10000 = this.knocker.getChat();
                                       String var10002;
                                       switch (phase) {
                                          case LOVE -> var10002 = "тук-тук... я в окне. не пугайся";
                                          case OBSESSION -> var10002 = "Впусти меня.";
                                          default -> var10002 = "Я ВИЖУ ТЕБЯ ЧЕРЕЗ СТЕКЛО.";
                                       }

                                       var10000.say(player, var10002);
                                       return;
                                    }
                                 }
                              }
                           }
                        }

                     }
                  }
               }
            }
         }
      }
   }

   private void tickJealousy(class_3222 player, KnockerPhase phase) {
      List<class_1309> near = this.knocker.method_37908().method_8390(class_1309.class, player.method_5829().method_1014((double)3.0F), (ex) -> {
         boolean var10000;
         label42: {
            if (ex != this.knocker && !(ex instanceof class_1657) && !(ex instanceof KnockerEntity) && (ex instanceof class_3988 || ex instanceof class_1296)) {
               if (!(ex instanceof class_1321)) {
                  break label42;
               }

               class_1321 t = (class_1321)ex;
               if (!t.method_6181()) {
                  break label42;
               }
            }

            var10000 = false;
            return var10000;
         }

         var10000 = true;
         return var10000;
      });
      if (!near.isEmpty()) {
         double strength = phase == KnockerPhase.PARANOIA ? 1.4 : (phase == KnockerPhase.OBSESSION ? (double)1.0F : 0.6);

         for(class_1309 e : near) {
            class_243 away = e.method_19538().method_1020(player.method_19538()).method_18805((double)1.0F, (double)0.0F, (double)1.0F);
            if (away.method_1027() < 0.01) {
               away = new class_243((double)1.0F, (double)0.0F, (double)0.0F);
            }

            away = away.method_1029().method_1021(strength);
            e.method_18800(away.field_1352, 0.35, away.field_1350);
            e.field_6037 = true;
            class_1937 var10 = this.knocker.method_37908();
            if (var10 instanceof class_3218) {
               class_3218 sw = (class_3218)var10;
               sw.method_14199(class_2398.field_11231, e.method_23317(), e.method_23320(), e.method_23321(), 2, 0.2, 0.2, 0.2, (double)0.0F);
            }
         }

         if (this.now() - this.jealousTalkTick > 4800 && this.knocker.getChat().canSpeakUnprompted(600)) {
            this.jealousTalkTick = this.now();
            boolean villager = near.get(0) instanceof class_3988;
            KnockerChat var10000 = this.knocker.getChat();
            String var10002;
            switch (phase) {
               case LOVE -> var10002 = villager ? "он стоит слишком близко к тебе..." : "даже животные... не надо, чтобы они тебя трогали";
               case OBSESSION -> var10002 = villager ? "Отойди от неё." : "Никто не будет тебя касаться. Даже они.";
               default -> var10002 = villager ? "ЕЩЁ ШАГ — И ОН НЕ ПРОСНЁТСЯ." : "ОНИ НЕ ДОЛЖНЫ ДЫШАТЬ РЯДОМ С ТОБОЙ.";
            }

            var10000.say(player, var10002);
         }

      }
   }

   private void drop(class_243 at, class_1799 stack) {
      class_1542 item = new class_1542(this.knocker.method_37908(), at.field_1352, at.field_1351, at.field_1350, stack);
      item.method_18800((double)0.0F, 0.1, (double)0.0F);
      this.knocker.markOwnDrop(item);
      this.knocker.method_37908().method_8649(item);
   }

   private String pick(String... lines) {
      return lines[this.random.nextInt(lines.length)];
   }
}
