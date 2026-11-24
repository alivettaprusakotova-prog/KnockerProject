package com.theneru.knocker.entity;

import com.theneru.knocker.util.KnockerMessages;
import com.theneru.knocker.util.KnockerPhase;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import net.minecraft.class_1268;
import net.minecraft.class_1304;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2363;
import net.minecraft.class_2398;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2680;
import net.minecraft.class_3218;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_3481;
import net.minecraft.class_3612;
import net.minecraft.class_3922;
import net.minecraft.class_7225;
import net.minecraft.class_9334;
import net.minecraft.class_2350.class_2353;

public class KnockerHelp {
   private final KnockerEntity knocker;
   private final Random random = new Random();
   private Job job;
   private class_1657 who;
   private int until;
   private int timer;
   private class_2338 spot;
   private class_1799 handBefore;
   private int nextCookTick;
   private final List<class_1799> carried;

   public KnockerHelp(KnockerEntity knocker) {
      this.job = KnockerHelp.Job.NONE;
      this.until = 0;
      this.timer = 0;
      this.spot = null;
      this.handBefore = class_1799.field_8037;
      this.nextCookTick = 2400;
      this.carried = new ArrayList();
      this.knocker = knocker;
   }

   private int now() {
      return this.knocker.tickCounter;
   }

   private int phase() {
      return this.knocker.getPhase().ordinal();
   }

   private String pick(String... lines) {
      return this.knocker.getChat().getBrain().pick(lines);
   }

   public String handleChat(class_1657 player, String norm) {
      int p = this.phase();
      if (KnockerMessages.containsAny(norm, "подержи", "подержи это", "держи это", "возьми это", "понеси", "поноси", "сохрани это")) {
         class_1799 hand = player.method_6047();
         if (hand.method_7960()) {
            return "что подержать?.. у тебя в руке ничего нет";
         } else if (this.carried.size() >= 9) {
            return "у меня руки заняты... забери что-нибудь";
         } else {
            this.carried.add(hand.method_7972());
            player.method_6122(class_1268.field_5808, class_1799.field_8037);
            this.knocker.playAction("give");
            this.knocker.rememberEvent("она дала мне подержать " + hand.method_7964().getString());
            String var6;
            switch (this.knocker.getPhase()) {
               case WATCHER:
               case CURIOUS:
                  var6 = "...ладно. держу";
                  break;
               case ATTACHED:
                  var6 = "держу. скажи «отдай», когда нужно";
                  break;
               case LOVE:
                  var6 = "я сохраню. как всё, что от тебя";
                  break;
               case OBSESSION:
                  var6 = "Держу. Мне нравится, когда у меня твоё.";
                  break;
               default:
                  var6 = "МОЁ. ...шучу. Наверное.";
            }

            return var6;
         }
      } else if (p < KnockerPhase.CURIOUS.ordinal()) {
         return null;
      } else if (KnockerMessages.containsAny(norm, "копай со мной", "пошли копать", "помоги копать", "давай копать", "покопаем", "пошли в шахту копать", "копать руду", "ищи руду")) {
         this.start(KnockerHelp.Job.MINE, player, 3600);
         this.knocker.getNav().startFollow(player, 3600);
         String var10000;
         switch (this.knocker.getPhase()) {
            case CURIOUS -> var10000 = "копать?.. хорошо... я посмотрю, что тут";
            case ATTACHED -> var10000 = "пойдём. я найду для тебя что-нибудь блестящее";
            case LOVE -> var10000 = "всё, что найду — твоё... почти всё";
            case OBSESSION -> var10000 = "Копаем. Не отходи от меня под землёй.";
            default -> var10000 = "КОПАЕМ. ТУТ ТЕМНО. ТУТ НИКТО НАС НЕ НАЙДЁТ.";
         }

         return var10000;
      } else if (KnockerMessages.containsAny(norm, "порыбачим", "пошли рыбачить", "давай рыбачить", "рыбалка", "на рыбалку", "половим рыбу")) {
         class_2338 shore = this.findShore(player.method_24515());
         if (shore == null) {
            return "тут нет воды... и хорошо";
         } else {
            this.start(KnockerHelp.Job.FISH, player, 3600);
            this.spot = shore;
            this.handBefore = this.knocker.method_6047().method_7972();
            this.knocker.method_5673(class_1304.field_6173, new class_1799(class_1802.field_8378));
            if (this.knocker.method_5707(class_243.method_24955(shore)) > (double)400.0F) {
               this.knocker.getKnockerMovement().teleportTo(class_243.method_24955(shore));
            } else {
               this.knocker.method_5942().method_6337((double)shore.method_10263() + (double)0.5F, (double)shore.method_10264(), (double)shore.method_10260() + (double)0.5F, (double)1.0F);
            }

            return this.pick("вода... я её боюсь. но с тобой посижу", "ладно... только не толкай меня в воду", "рыбалка... как раньше. с другом");
         }
      } else {
         return null;
      }
   }

   public String giveBackCarried(class_1657 player) {
      if (this.carried.isEmpty()) {
         return null;
      } else if (this.phase() == KnockerPhase.PARANOIA.ordinal() && this.random.nextBoolean()) {
         return "НЕТ. Теперь это моё.";
      } else {
         for(class_1799 s : this.carried) {
            player.method_31548().method_7398(s);
         }

         this.carried.clear();
         this.knocker.playAction("give");
         return this.phase() >= KnockerPhase.OBSESSION.ordinal() ? "Держи. Я всё сохранил." : "держи... всё на месте";
      }
   }

   private void start(Job j, class_1657 player, int ticks) {
      this.stopJob();
      this.job = j;
      this.who = player;
      this.until = this.now() + ticks;
      this.timer = 0;
   }

   private void stopJob() {
      if (this.job == KnockerHelp.Job.FISH) {
         this.knocker.method_5673(class_1304.field_6173, this.handBefore);
      }

      this.job = KnockerHelp.Job.NONE;
      this.spot = null;
   }

   public void cancelJob() {
      if (this.job != KnockerHelp.Job.NONE) {
         this.stopJob();
      }

   }

   public boolean tick(class_1657 player) {
      this.tickAutoCook(player);
      if (this.job == KnockerHelp.Job.NONE) {
         return false;
      } else if (this.who != null && !this.who.method_31481() && this.now() <= this.until) {
         ++this.timer;
         boolean var10000;
         switch (this.job.ordinal()) {
            case 1:
               this.tickMine();
               var10000 = false;
               break;
            case 2:
               var10000 = this.tickFish();
               break;
            case 3:
               var10000 = this.tickCook();
               break;
            default:
               var10000 = false;
         }

         return var10000;
      } else {
         if (this.job == KnockerHelp.Job.MINE && this.who != null && !this.knocker.getGames().isBusy()) {
            this.knocker.getChat().say(this.who, "устал копать... хватит на сегодня");
         }

         this.stopJob();
         return false;
      }
   }

   private void tickMine() {
      if (this.timer % 40 == 0) {
         class_1937 world = this.knocker.method_37908();
         class_2338 c = this.knocker.method_24515();

         for(class_2338 p : class_2338.method_10097(c.method_10069(-4, -2, -4), c.method_10069(4, 3, 4))) {
            class_2680 st = world.method_8320(p);
            if (isOre(st) && exposed(world, p)) {
               class_2338 pos = p.method_10062();
               this.knocker.method_5988().method_20248((double)pos.method_10263() + (double)0.5F, (double)pos.method_10264() + (double)0.5F, (double)pos.method_10260() + (double)0.5F);
               this.knocker.method_6104(class_1268.field_5808);
               boolean diamond = st.method_26164(class_3481.field_28989) || st.method_26164(class_3481.field_29194);
               if (diamond && this.phase() >= KnockerPhase.OBSESSION.ordinal() && this.random.nextInt(3) == 0) {
                  world.method_8651(pos, false, this.knocker);
                  this.knocker.getItems().stash(new class_1799(st.method_26164(class_3481.field_28989) ? class_1802.field_8477 : class_1802.field_8687));
                  this.knocker.getChat().say(this.who, "этот я оставлю себе. хе-хе. ты же не против?");
               } else {
                  world.method_8651(pos, true, this.knocker);
                  if (diamond) {
                     this.knocker.getChat().say(this.who, this.pick("смотри! блестит... как ты", "это тебе. самое красивое — тебе"));
                  }
               }

               return;
            }
         }

      }
   }

   private static boolean isOre(class_2680 st) {
      return st.method_26164(class_3481.field_29193) || st.method_26164(class_3481.field_28988) || st.method_26164(class_3481.field_29195) || st.method_26164(class_3481.field_23062) || st.method_26164(class_3481.field_28990) || st.method_26164(class_3481.field_28991) || st.method_26164(class_3481.field_28989) || st.method_26164(class_3481.field_29194);
   }

   private static boolean exposed(class_1937 world, class_2338 p) {
      for(class_2350 d : class_2350.values()) {
         if (world.method_8320(p.method_10093(d)).method_26215()) {
            return true;
         }
      }

      return false;
   }

   private boolean tickFish() {
      if (this.knocker.method_5739(this.who) > 24.0F) {
         this.knocker.getChat().say(this.who, "ты ушла... и я ухожу от воды");
         this.stopJob();
         return false;
      } else if (this.spot != null && this.knocker.method_5707(class_243.method_24955(this.spot)) > (double)6.25F) {
         if (this.knocker.method_5942().method_6357()) {
            this.knocker.method_5942().method_6337((double)this.spot.method_10263() + (double)0.5F, (double)this.spot.method_10264(), (double)this.spot.method_10260() + (double)0.5F, (double)1.0F);
         }

         return true;
      } else {
         this.knocker.method_5942().method_6340();
         if (this.spot != null && this.timer % 60 == 0) {
            class_1937 var2 = this.knocker.method_37908();
            if (var2 instanceof class_3218) {
               class_3218 sw = (class_3218)var2;
               class_2338 w = this.waterNear(this.spot);
               if (w != null) {
                  sw.method_14199(class_2398.field_11244, (double)w.method_10263() + (double)0.5F, (double)w.method_10264() + 0.9, (double)w.method_10260() + (double)0.5F, 3, 0.15, (double)0.0F, 0.15, 0.02);
               }
            }
         }

         if (this.spot != null) {
            class_2338 water = this.waterNear(this.spot);
            if (water != null) {
               this.knocker.method_5988().method_20248((double)water.method_10263() + (double)0.5F, (double)water.method_10264(), (double)water.method_10260() + (double)0.5F);
            }
         }

         if (this.timer % 400 == 0 && this.random.nextBoolean()) {
            class_1792 fish = this.random.nextInt(4) == 0 ? class_1802.field_8209 : class_1802.field_8429;
            class_1799 catchStack = new class_1799(this.random.nextInt(25) == 0 ? class_1802.field_8864 : fish);
            class_1542 e = new class_1542(this.knocker.method_37908(), this.who.method_23317(), this.who.method_23318() + (double)0.5F, this.who.method_23321(), catchStack);
            this.knocker.markOwnDrop(e);
            this.knocker.method_37908().method_8649(e);
            this.knocker.method_6104(class_1268.field_5808);
            if (this.spot != null) {
               class_1937 var5 = this.knocker.method_37908();
               if (var5 instanceof class_3218) {
                  class_3218 sw2 = (class_3218)var5;
                  class_2338 w = this.waterNear(this.spot);
                  if (w != null) {
                     sw2.method_14199(class_2398.field_11202, (double)w.method_10263() + (double)0.5F, (double)(w.method_10264() + 1), (double)w.method_10260() + (double)0.5F, 12, 0.3, 0.1, 0.3, 0.1);
                  }
               }
            }

            this.knocker.method_37908().method_8396((class_1657)null, this.knocker.method_24515(), class_3417.field_15093, class_3419.field_15254, 0.6F, 1.0F);
            this.knocker.getChat().say(this.who, this.pick("поймал... держи", "рыба. тебе", "я поймал! ...не смейся", "она так дёргалась... как я, когда ты рядом"));
         } else if (this.timer % 900 == 450 && this.random.nextInt(3) == 0) {
            this.knocker.getChat().say(this.who, this.pick("я не люблю смотреть на воду...", "здесь холодно...", "не отходи далеко, ладно?"));
         }

         return true;
      }
   }

   private void tickAutoCook(class_1657 player) {
      if (this.job == KnockerHelp.Job.NONE && this.now() >= this.nextCookTick && this.now() % 100 == 0) {
         if (this.phase() >= KnockerPhase.ATTACHED.ordinal() && player.method_7344().method_7586() <= 6) {
            if (!(this.knocker.method_5739(player) > 24.0F)) {
               class_2338 stove = this.findStove(player.method_24515());
               if (stove != null) {
                  this.nextCookTick = this.now() + 9600;
                  this.start(KnockerHelp.Job.COOK, player, 400);
                  this.spot = stove;
                  this.knocker.getKnockerMovement().teleportTo(class_243.method_24955(stove).method_1031((double)1.0F, (double)0.0F, (double)0.0F));
                  this.knocker.getChat().say(player, this.pick("ты голодная... подожди, я приготовлю", "сиди. я сейчас что-нибудь приготовлю", "у тебя урчит в животе... я слышу"));
               }
            }
         }
      }
   }

   private boolean tickCook() {
      this.knocker.method_5942().method_6340();
      if (this.spot != null) {
         this.knocker.method_5988().method_20248((double)this.spot.method_10263() + (double)0.5F, (double)this.spot.method_10264() + (double)0.5F, (double)this.spot.method_10260() + (double)0.5F);
      }

      if (this.timer % 20 == 0 && this.spot != null) {
         class_1937 var2 = this.knocker.method_37908();
         if (var2 instanceof class_3218) {
            class_3218 sw = (class_3218)var2;
            sw.method_14199(class_2398.field_11251, (double)this.spot.method_10263() + (double)0.5F, (double)this.spot.method_10264() + 1.1, (double)this.spot.method_10260() + (double)0.5F, 3, 0.2, 0.1, 0.2, 0.01);
            sw.method_8396((class_1657)null, this.spot, class_3417.field_15006, class_3419.field_15245, 0.7F, 1.0F);
         }
      }

      if (this.timer >= 100) {
         class_1792 fav = KnockerFavorites.food(this.who.method_5667());
         class_1792 cooked = fav != null && fav.method_57347().method_57832(class_9334.field_50075) ? fav : class_1802.field_8176;
         this.knocker.getKnockerMovement().teleportBehind(this.who);
         this.knocker.playAction("give");
         this.who.method_31548().method_7398(new class_1799(cooked, 3));
         this.knocker.getChat().say(this.who, this.pick("готово. ешь, пока горячее", "я приготовил... это тебе", "ешь. я хочу, чтобы ты была сильной"));
         this.knocker.rememberEvent("я приготовил ей поесть");
         this.stopJob();
      }

      return true;
   }

   private class_2338 findShore(class_2338 center) {
      class_1937 world = this.knocker.method_37908();

      for(int r = 1; r <= 16; ++r) {
         for(class_2338 p : class_2338.method_10097(center.method_10069(-r, -4, -r), center.method_10069(r, 3, r))) {
            if (world.method_8316(p).method_39360(class_3612.field_15910) && world.method_8320(p.method_10084()).method_26215()) {
               for(class_2350 d : class_2353.field_11062) {
                  class_2338 side = p.method_10093(d);
                  class_2338 onLand = side.method_10084();
                  if (world.method_8320(side).method_26212(world, side) && world.method_8320(onLand).method_26215() && world.method_8320(onLand.method_10084()).method_26215()) {
                     return onLand.method_10062();
                  }

                  if (world.method_8320(side).method_26215() && world.method_8320(side.method_10084()).method_26215() && world.method_8320(side.method_10074()).method_26212(world, side.method_10074())) {
                     return side.method_10062();
                  }
               }
            }
         }
      }

      return null;
   }

   private class_2338 waterNear(class_2338 s) {
      for(class_2350 d : class_2353.field_11062) {
         if (this.knocker.method_37908().method_8316(s.method_10093(d)).method_39360(class_3612.field_15910)) {
            return s.method_10093(d);
         }

         if (this.knocker.method_37908().method_8316(s.method_10093(d).method_10074()).method_39360(class_3612.field_15910)) {
            return s.method_10093(d).method_10074();
         }

         if (this.knocker.method_37908().method_8316(s.method_10093(d).method_10087(2)).method_39360(class_3612.field_15910)) {
            return s.method_10093(d).method_10087(2);
         }
      }

      return null;
   }

   private class_2338 findStove(class_2338 center) {
      class_1937 world = this.knocker.method_37908();

      for(class_2338 p : class_2338.method_10097(center.method_10069(-16, -4, -16), center.method_10069(16, 4, 16))) {
         class_2680 st = world.method_8320(p);
         if (st.method_26204() instanceof class_2363 || st.method_26204() instanceof class_3922 && (Boolean)st.method_11654(class_3922.field_17352)) {
            return p.method_10062();
         }
      }

      return null;
   }

   public void writeNbt(class_2487 nbt, class_7225.class_7874 lookup) {
      class_2499 list = new class_2499();

      for(class_1799 s : this.carried) {
         if (!s.method_7960()) {
            list.add(s.method_57358(lookup));
         }
      }

      nbt.method_10566("Carried", list);
   }

   public void readNbt(class_2487 nbt, class_7225.class_7874 lookup) {
      this.carried.clear();
      class_2499 list = nbt.method_10554("Carried", 10);

      for(int i = 0; i < list.size(); ++i) {
         Optional var10000 = class_1799.method_57360(lookup, list.method_10602(i));
         List var10001 = this.carried;
         Objects.requireNonNull(var10001);
         var10000.ifPresent(var10001::add);
      }

   }

   private static enum Job {
      NONE,
      MINE,
      FISH,
      COOK;

      // $FF: synthetic method
      private static Job[] $values() {
         return new Job[]{NONE, MINE, FISH, COOK};
      }
   }
}
