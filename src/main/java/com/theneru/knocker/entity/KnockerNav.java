package com.theneru.knocker.entity;

import com.theneru.knocker.util.KnockerMessages;
import com.theneru.knocker.util.KnockerPhase;
import com.theneru.knocker.util.KnockerWords;
import java.util.Random;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2398;
import net.minecraft.class_243;
import net.minecraft.class_3195;
import net.minecraft.class_3218;
import net.minecraft.class_3532;
import net.minecraft.class_6862;
import net.minecraft.class_7045;
import net.minecraft.class_2902.class_2903;

public class KnockerNav {
   private static final String[] WHERE = new String[]{"где", "в какой стороне", "куда идти", "как пройти", "как дойти", "далеко ли", "как найти", "знаешь где", "подскажи дорогу"};
   private static final String[] ESCORT = new String[]{"проводи", "сопроводи", "отведи", "доведи", "проведи", "покажи дорогу", "покажи где", "веди меня", "отвези", "пошли в", "пойдем в", "пойдём в", "пошли к", "идем в", "идём в", "го в", "отправимся в", "веди нас"};
   private static final String[] COME = new String[]{"подойди", "иди сюда", "иди ко мне", "подь сюда", "го ко мне", "приди", "приходи", "подойди ко мне", "иди ближе", "подойди ближе", "давай сюда", "ко мне"};
   private static final String[] ENTER = new String[]{"зайди в дом", "заходи в дом", "зайди домой", "иди в дом", "иди домой", "зайди внутрь", "заходи внутрь", "заходи", "зайди", "входи", "войди"};
   private static final String[] FOLLOW = new String[]{"ходи со мной", "ходи за мной", "будь рядом", "не отходи", "иди за мной", "пошли со мной", "пойдем со мной", "пойдём со мной", "следуй за мной", "за мной", "пошли", "пойдем", "пойдём"};
   private static final String[] STAY = new String[]{"стой", "стоять", "жди здесь", "жди тут", "останься тут", "останься здесь", "подожди здесь", "подожди тут", "замри", "стой тут", "стой здесь"};
   private final KnockerEntity knocker;
   private final KnockerMovement movement;
   private final Random random = new Random();
   private Mode mode;
   private int until;
   private class_243 goal;
   private String goalName;
   private class_1657 who;
   private class_243 lastPos;
   private int stuckTicks;
   private int waitTalkTick;

   public KnockerNav(KnockerEntity knocker, KnockerMovement movement) {
      this.mode = KnockerNav.Mode.NONE;
      this.until = 0;
      this.goal = null;
      this.goalName = "";
      this.who = null;
      this.lastPos = null;
      this.stuckTicks = 0;
      this.waitTalkTick = 0;
      this.knocker = knocker;
      this.movement = movement;
   }

   public boolean isActive() {
      return this.mode != KnockerNav.Mode.NONE;
   }

   public void stop() {
      this.mode = KnockerNav.Mode.NONE;
      this.knocker.method_5942().method_6340();
   }

   public String handleChat(class_1657 player, String norm) {
      int p = this.knocker.getPhase().ordinal();
      if (!KnockerMessages.containsAny(norm, KnockerWords.GO_AWAY) && !norm.contains("не ходи")) {
         if (KnockerBrain.isRudeText(norm)) {
            return null;
         } else {
            Place place = findPlace(norm);
            boolean escort = KnockerMessages.containsAny(norm, ESCORT);
            boolean where = KnockerMessages.containsAny(norm, WHERE);
            if (KnockerMessages.containsAny(norm, ENTER) && (place == KnockerNav.Place.HOME || norm.contains("заход") || norm.contains("зайд") || norm.contains("вход") || norm.contains("войд")) && !where) {
               return this.goHome(player, p);
            } else if (place == null || !escort && !where) {
               if (KnockerMessages.containsAny(norm, COME) && !norm.contains("не подход") && !norm.contains("не иди")) {
                  if (p == 0) {
                     return null;
                  } else {
                     this.startCome(player);
                     return this.pick((new String[][]{null, {"иду...", "сейчас...", "ладно... иду"}, {"Иду", "Сейчас подойду", "Уже иду"}, {"Иду к тебе", "Бегу...", "Уже рядом"}, {"Иду.", "Я и так всегда рядом.", "Сейчас."}, {"ИДУ.", "Я УЖЕ ЗДЕСЬ.", "Ты позвала — я иду."}})[p]);
                  }
               } else if (KnockerMessages.containsAny(norm, FOLLOW) && !norm.contains("не иди") && !norm.contains("не ходи") && p >= KnockerPhase.CURIOUS.ordinal()) {
                  this.start(KnockerNav.Mode.FOLLOW, player, 3600);
                  return this.pick("иду за тобой", "куда угодно", "веди...");
               } else if (KnockerMessages.containsAny(norm, STAY) && this.isActiveOrNear(player)) {
                  this.start(KnockerNav.Mode.STAY, player, 2400);
                  return this.pick((new String[][]{{"..."}, {"...ладно"}, {"Стою", "Жду тебя здесь"}, {"Подожду... только недолго", "Жду"}, {"Стою. Но я тебя вижу.", "Жду. Не заставляй ждать долго."}, {"СТОЮ. И СМОТРЮ.", "Я подожду. Я всегда жду."}})[p]);
               } else {
                  return null;
               }
            } else {
               class_2338 target = this.locate(player, place);
               if (target == null) {
                  return place == KnockerNav.Place.HOME ? this.pick("а где твой дом?.. ты ещё не спала здесь", "покажи мне его... поспи там хоть раз") : this.pick("не знаю... я не видел здесь " + accusative(place), "я не нашёл " + accusative(place) + " поблизости", "здесь нет " + genitive(place) + "...");
               } else {
                  String dir = direction(player, target);
                  if (escort && p >= KnockerPhase.CURIOUS.ordinal()) {
                     this.startEscort(player, class_243.method_24953(target), place.name);
                     String var9;
                     switch (p) {
                        case 1 -> var9 = "...иди за мной. " + dir;
                        case 2 -> var9 = "Пойдём, я покажу. Не отставай";
                        case 3 -> var9 = "Пойдём... я отведу тебя куда угодно";
                        case 4 -> var9 = "Иди за мной. И не отходи.";
                        default -> var9 = "ПОШЛИ. Держись рядом. ВСЕГДА рядом.";
                     }

                     return var9;
                  } else {
                     String var10000;
                     switch (p) {
                        case 0:
                           var10000 = "...туда. " + dir;
                           break;
                        case 1:
                        case 2:
                           var10000 = place.name.substring(0, 1).toUpperCase() + place.name.substring(1) + "... " + dir + ". Хочешь, провожу?";
                           break;
                        case 3:
                           var10000 = dir + ". Могу проводить... мне не трудно";
                           break;
                        case 4:
                           var10000 = dir + ". Но зачем тебе туда? Тебе мало меня?";
                           break;
                        default:
                           var10000 = dir + ". ЗАЧЕМ ТЕБЕ ТУДА? КТО ТАМ?";
                     }

                     return var10000;
                  }
               }
            }
         }
      } else {
         if (this.mode != KnockerNav.Mode.NONE) {
            this.stop();
         }

         return null;
      }
   }

   private boolean isActiveOrNear(class_1657 player) {
      return this.mode != KnockerNav.Mode.NONE || this.knocker.method_5739(player) < 24.0F;
   }

   private String goHome(class_1657 player, int p) {
      class_2338 home = this.knocker.getHomePos();
      if (home == null) {
         return this.pick("в какой дом?.. ты ещё не спала здесь", "а где твой дом?..");
      } else {
         this.start(KnockerNav.Mode.HOME, player, 2400);
         this.goal = class_243.method_24953(home);
         this.knocker.getDoors().invite(1200);
         return this.pick((new String[][]{{"..."}, {"...хорошо", "внутрь?.."}, {"Захожу...", "Хорошо, захожу"}, {"Ты зовёшь меня к себе?.. Иду", "Захожу. Можно?"}, {"Захожу. Это теперь и мой дом.", "Иду домой. К нам."}, {"ЗАХОЖУ. Я и так знаю, где что лежит.", "Наш дом. НАШ."}})[p]);
      }
   }

   private void start(Mode m, class_1657 player, int ticks) {
      this.mode = m;
      this.who = player;
      this.until = this.knocker.tickCounter + ticks;
      this.stuckTicks = 0;
      this.lastPos = this.knocker.method_19538();
      this.movement.clearTarget();
   }

   public void startFollow(class_1657 player, int ticks) {
      this.start(KnockerNav.Mode.FOLLOW, player, ticks);
   }

   public void startHand(class_1657 player) {
      this.start(KnockerNav.Mode.HAND, player, 3600);
   }

   private void startCome(class_1657 player) {
      this.start(KnockerNav.Mode.COME, player, 800);
      this.knocker.getDoors().invite(800);
   }

   private void startEscort(class_1657 player, class_243 target, String name) {
      this.start(KnockerNav.Mode.ESCORT, player, 12000);
      this.goal = target;
      this.goalName = name;
   }

   public boolean tick(class_1657 player) {
      if (this.mode == KnockerNav.Mode.NONE) {
         return false;
      } else {
         int now = this.knocker.tickCounter;
         if (now <= this.until && this.who != null && !this.who.method_31481()) {
            double speed = (double)1.0F + (double)this.knocker.getPhase().ordinal() * 0.08;
            switch (this.mode.ordinal()) {
               case 1:
                  if ((double)this.knocker.method_5739(this.who) < (double)2.5F) {
                     this.knocker.method_5988().method_6226(this.who, 30.0F, 30.0F);
                     this.stop();
                     return true;
                  }

                  if (this.knocker.method_5739(this.who) > 64.0F) {
                     this.movement.teleportBehind(this.who);
                     this.stop();
                     return true;
                  }

                  if (now % 10 == 0) {
                     this.knocker.method_5942().method_6335(this.who, speed);
                  }
                  break;
               case 2:
                  this.tickEscort(now, speed);
                  break;
               case 3:
                  if (this.goal == null || this.knocker.method_5707(this.goal) < (double)6.25F) {
                     this.stop();
                     return true;
                  }

                  if (now % 10 == 0) {
                     this.walkTowards(this.goal, speed);
                  }
                  break;
               case 4:
                  if ((double)this.knocker.method_5739(this.who) > (double)3.5F && now % 10 == 0) {
                     this.knocker.method_5942().method_6335(this.who, speed);
                  }

                  if (this.knocker.method_5739(this.who) > 64.0F) {
                     this.movement.teleportBehind(this.who);
                  }
                  break;
               case 5:
                  this.knocker.method_5942().method_6340();
                  this.knocker.method_5988().method_6226(this.who, 30.0F, 30.0F);
                  break;
               case 6:
                  double yaw = Math.toRadians((double)this.who.method_36454());
                  class_243 right = new class_243(-Math.cos(yaw), (double)0.0F, -Math.sin(yaw));
                  class_243 spot = this.who.method_19538().method_1019(right.method_1021(1.1));
                  double d = this.knocker.method_19538().method_1022(spot);
                  if (d > (double)8.0F) {
                     this.movement.teleportTo(spot);
                  } else if (d > 0.6) {
                     this.knocker.method_5942().method_6337(spot.field_1352, spot.field_1351, spot.field_1350, this.who.method_5624() ? 1.8 : 1.2);
                  } else {
                     this.knocker.method_5942().method_6340();
                     this.knocker.method_36456(this.who.method_36454());
                     this.knocker.method_5636(this.who.method_36454());
                  }

                  if (now % 40 == 0) {
                     this.knocker.spawnParticles(class_2398.field_11201, 1);
                  }
            }

            this.tickStuck();
            return true;
         } else {
            if (this.mode == KnockerNav.Mode.COME && this.who != null && !this.who.method_31481() && this.knocker.method_5739(this.who) > 6.0F) {
               this.movement.teleportBehind(this.who);
            }

            this.stop();
            return false;
         }
      }
   }

   private void tickEscort(int now, double speed) {
      double toGoal = Math.sqrt(horizontalDistSq(this.knocker.method_19538(), this.goal));
      double toHer = (double)this.knocker.method_5739(this.who);
      if (toGoal < (double)8.0F) {
         this.knocker.getChat().say(this.who, this.pick((new String[][]{{"...здесь"}, {"пришли... " + this.goalName}, {"Пришли. Вот " + this.goalName}, {"Вот... " + this.goalName + ". Я же говорил, доведу", "Пришли. Тебе понравилось идти со мной?"}, {"Пришли. Не уходи далеко отсюда.", "Вот " + this.goalName + ". Я буду рядом."}, {"ПРИШЛИ. Я ВСЁ ВРЕМЯ БЫЛ РЯДОМ. ЗАМЕТИЛА?", "Здесь. Не говори ни с кем."}})[this.knocker.getPhase().ordinal()]));
         this.stop();
      } else if (toHer > (double)80.0F) {
         this.knocker.getChat().say(this.who, this.pick("ты отстала... ладно", "я тебя потерял... ты же специально?"));
         this.stop();
      } else if (toHer > (double)14.0F) {
         this.knocker.method_5942().method_6340();
         this.knocker.method_5988().method_6226(this.who, 30.0F, 30.0F);
         this.stuckTicks = 0;
         if (now - this.waitTalkTick > 900 && toHer > (double)24.0F) {
            this.waitTalkTick = now;
            this.knocker.getChat().say(this.who, this.pick("не отставай...", "я жду", "сюда...", "ты где?.."));
         }

      } else {
         if (now % 10 == 0) {
            this.walkTowards(this.goal, speed);
         }

         if (now % 1200 == 0) {
            this.knocker.getChat().say(this.who, this.pick("ещё " + (int)toGoal + " блоков...", "почти пришли", "держись за мной", "осталось немного"));
         }

      }
   }

   private void walkTowards(class_243 target, double speed) {
      class_243 pos = this.knocker.method_19538();
      class_243 dir = target.method_1020(pos).method_18805((double)1.0F, (double)0.0F, (double)1.0F);
      double len = dir.method_1033();
      if (len < (double)20.0F) {
         this.knocker.method_5942().method_6337(target.field_1352, target.field_1351, target.field_1350, speed);
      } else {
         class_243 step = pos.method_1019(dir.method_1029().method_1021((double)20.0F));
         int y = this.knocker.method_37908().method_8624(class_2903.field_13203, class_3532.method_15357(step.field_1352), class_3532.method_15357(step.field_1350));
         this.knocker.method_5942().method_6337(step.field_1352, (double)y, step.field_1350, speed);
      }
   }

   private void tickStuck() {
      if (this.mode != KnockerNav.Mode.STAY && this.mode != KnockerNav.Mode.FOLLOW && this.mode != KnockerNav.Mode.HAND) {
         if (this.knocker.tickCounter % 20 == 0) {
            class_243 pos = this.knocker.method_19538();
            if (this.lastPos == null || !(pos.method_1025(this.lastPos) < (double)0.5F) || this.mode == KnockerNav.Mode.ESCORT && !(this.knocker.method_5739(this.who) <= 14.0F)) {
               this.stuckTicks = 0;
            } else {
               this.stuckTicks += 20;
            }

            this.lastPos = pos;
            if (this.stuckTicks >= 120) {
               this.stuckTicks = 0;
               class_243 target = this.mode == KnockerNav.Mode.COME ? this.who.method_19538() : this.goal;
               if (target != null) {
                  class_243 dir = target.method_1020(pos).method_18805((double)1.0F, (double)0.0F, (double)1.0F);
                  if (!(dir.method_1027() < (double)1.0F)) {
                     class_243 jump = pos.method_1019(dir.method_1029().method_1021(Math.min((double)8.0F, dir.method_1033())));
                     int y = this.knocker.method_37908().method_8624(class_2903.field_13203, class_3532.method_15357(jump.field_1352), class_3532.method_15357(jump.field_1350));
                     this.movement.teleportTo(new class_243(jump.field_1352, (double)y, jump.field_1350));
                  }
               }
            }
         }
      }
   }

   private static Place findPlace(String norm) {
      for(String w : norm.split(" ")) {
         if (!w.isEmpty()) {
            String f = KnockerMessages.fold(w);

            for(Place pl : KnockerNav.Place.values()) {
               for(String stem : pl.stems) {
                  String fs = KnockerMessages.fold(stem);
                  if (pl == KnockerNav.Place.HOME) {
                     if (f.equals(fs) || f.startsWith(fs) && fs.length() >= 4) {
                        return pl;
                     }
                  } else if (f.startsWith(fs)) {
                     return pl;
                  }
               }
            }
         }
      }

      return null;
   }

   private class_2338 locate(class_1657 player, Place place) {
      if (place == KnockerNav.Place.HOME) {
         return this.knocker.getHomePos();
      } else {
         class_1937 var4 = this.knocker.method_37908();
         if (var4 instanceof class_3218) {
            class_3218 world = (class_3218)var4;

            try {
               class_2338 found = world.method_8487(place.tag, player.method_24515(), 64, false);
               if (found == null) {
                  return null;
               } else {
                  int y = world.method_8624(class_2903.field_13203, found.method_10263(), found.method_10260());
                  return new class_2338(found.method_10263(), place == KnockerNav.Place.MINESHAFT ? found.method_10264() : y, found.method_10260());
               }
            } catch (Exception var6) {
               return null;
            }
         } else {
            return null;
         }
      }
   }

   private static String direction(class_1657 player, class_2338 target) {
      double dx = (double)target.method_10263() + (double)0.5F - player.method_23317();
      double dz = (double)target.method_10260() + (double)0.5F - player.method_23321();
      int dist = (int)Math.sqrt(dx * dx + dz * dz);
      String[] compass = new String[]{"юг", "юго-запад", "запад", "северо-запад", "север", "северо-восток", "восток", "юго-восток"};
      double angle = Math.toDegrees(Math.atan2(-dx, dz));
      int idx = (int)Math.round(class_3532.method_15338(angle) / (double)45.0F) & 7;
      String side = compass[(idx + 8) % 8];
      double rel = class_3532.method_15338(angle - (double)player.method_36454());
      String relative = Math.abs(rel) < (double)30.0F ? "прямо перед тобой" : (Math.abs(rel) > (double)150.0F ? "у тебя за спиной" : (rel > (double)0.0F ? "справа от тебя" : "слева от тебя"));
      return dist < 16 ? "совсем рядом, " + relative : "на " + side + ", блоков " + roundDist(dist) + " — " + relative;
   }

   private static int roundDist(int d) {
      return d < 100 ? d / 10 * 10 : d / 50 * 50;
   }

   private static double horizontalDistSq(class_243 a, class_243 b) {
      double dx = a.field_1352 - b.field_1352;
      double dz = a.field_1350 - b.field_1350;
      return dx * dx + dz * dz;
   }

   private static String accusative(Place p) {
      String var10000;
      switch (p.ordinal()) {
         case 0 -> var10000 = "деревню";
         case 1 -> var10000 = "шахту";
         default -> var10000 = p.name;
      }

      return var10000;
   }

   private static String genitive(Place p) {
      String var10000;
      switch (p.ordinal()) {
         case 0 -> var10000 = "деревень";
         case 1 -> var10000 = "шахт";
         case 2 -> var10000 = "порталов";
         case 3 -> var10000 = "кораблей";
         default -> var10000 = "дома";
      }

      return var10000;
   }

   private String pick(String... lines) {
      return this.knocker.getChat().getBrain().pick(lines);
   }

   public Mode getMode() {
      return this.mode;
   }

   public static enum Mode {
      NONE,
      COME,
      ESCORT,
      HOME,
      FOLLOW,
      STAY,
      HAND;

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{NONE, COME, ESCORT, HOME, FOLLOW, STAY, HAND};
      }
   }

   private static enum Place {
      VILLAGE(class_7045.field_37045, "деревня", new String[]{"деревн", "село", "сел", "жител", "посёл", "поселен", "город"}),
      MINESHAFT(class_7045.field_37046, "шахта", new String[]{"шахт", "рудник", "заброшенн"}),
      PORTAL(class_7045.field_37048, "портал", new String[]{"портал"}),
      SHIP(class_7045.field_37047, "корабль", new String[]{"корабл", "кораб", "судно"}),
      HOME((class_6862)null, "дом", new String[]{"дом", "домой", "хат", "кроват", "база", "базу"});

      final class_6862<class_3195> tag;
      final String name;
      final String[] stems;

      private Place(class_6862<class_3195> tag, String name, String[] stems) {
         this.tag = tag;
         this.name = name;
         this.stems = stems;
      }

      // $FF: synthetic method
      private static Place[] $values() {
         return new Place[]{VILLAGE, MINESHAFT, PORTAL, SHIP, HOME};
      }
   }
}
