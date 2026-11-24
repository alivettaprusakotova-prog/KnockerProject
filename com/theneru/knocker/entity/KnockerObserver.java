package com.theneru.knocker.entity;

import com.theneru.knocker.ModSounds;
import com.theneru.knocker.util.KnockerPhase;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import net.minecraft.class_1657;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2519;
import net.minecraft.class_3222;
import net.minecraft.class_3419;
import net.minecraft.class_3442;
import net.minecraft.class_3468;
import net.minecraft.class_2902.class_2903;

public class KnockerObserver {
   private static final Map<Event, String[][]> LINES = new EnumMap(Event.class);
   private static final DateTimeFormatter CLOCK_FORMAT;
   private final KnockerEntity knocker;
   private final KnockerChat chat;
   private final Random random = new Random();
   private int nightsSlept = 0;
   private long lastLogoutMillis = 0L;
   private final List<Quote> quotes = new ArrayList();
   private UUID trackedPlayer = null;
   private int baseWalk;
   private int lastDiamonds;
   private int lastTorches;
   private int lastKills;
   private int lastDeaths;
   private boolean wasSleeping;
   private boolean wasInCave;
   private boolean afkSaid;
   private float fallPeak = 0.0F;
   private int sprintTicks = 0;
   private int stillTicks = 0;
   private int ghostSteps = 0;
   private class_243 lastPos = null;
   private final Map<Event, Integer> nextAllowed = new EnumMap(Event.class);
   private int nextAnyTick = 0;
   private int nextAmbientTick = 6000;

   private static int cooldown(Event e) {
      short var10000;
      switch (e.ordinal()) {
         case 0:
         case 1:
         case 6:
            var10000 = 6000;
            break;
         case 2:
            var10000 = 2400;
            break;
         case 3:
         default:
            var10000 = 0;
            break;
         case 4:
            var10000 = 1200;
            break;
         case 5:
            var10000 = 3600;
      }

      return var10000;
   }

   public KnockerObserver(KnockerEntity knocker, KnockerChat chat) {
      this.knocker = knocker;
      this.chat = chat;
   }

   public void tick(class_3222 player) {
      int now = this.knocker.tickCounter;
      if (!player.method_5667().equals(this.trackedPlayer)) {
         this.startSession(player);
      } else {
         boolean sleeping = player.method_6113();
         if (sleeping && !this.wasSleeping) {
            ++this.nightsSlept;
            this.fire(player, KnockerObserver.Event.SLEEP);
         }

         this.wasSleeping = sleeping;
         if (!player.method_24828()) {
            this.fallPeak = Math.max(this.fallPeak, player.field_6017);
         } else {
            if (this.fallPeak > 5.0F && !player.method_5799() && !player.method_7337()) {
               this.fire(player, KnockerObserver.Event.FALL);
            }

            this.fallPeak = 0.0F;
         }

         this.sprintTicks = player.method_5624() ? this.sprintTicks + 1 : 0;
         if (this.sprintTicks == 120) {
            this.fire(player, KnockerObserver.Event.RUN);
         }

         class_243 pos = player.method_19538();
         if (this.lastPos != null && pos.method_1025(this.lastPos) < 0.0025) {
            ++this.stillTicks;
         } else {
            this.stillTicks = 0;
            this.afkSaid = false;
         }

         this.lastPos = pos;
         if (this.stillTicks > 1800 && !this.afkSaid && !sleeping) {
            this.afkSaid = true;
            if (this.knocker.getPhase() == KnockerPhase.WATCHER) {
               this.ghostSteps = 3;
            } else {
               this.fire(player, KnockerObserver.Event.AFK);
            }
         }

         if (this.ghostSteps > 0 && now % 12 == 0) {
            --this.ghostSteps;
            double rad = Math.toRadians((double)player.method_36454());
            this.knocker.method_37908().method_43128((class_1657)null, player.method_23317() + Math.sin(rad) * (double)3.0F, player.method_23318(), player.method_23321() - Math.cos(rad) * (double)3.0F, ModSounds.STEP, class_3419.field_15251, 0.6F, 1.0F);
         }

         if (now % 40 == 0) {
            class_3442 stats = player.method_14248();
            int diamonds = stats.method_15024(class_3468.field_15427, class_2246.field_10442) + stats.method_15024(class_3468.field_15427, class_2246.field_29029);
            if (diamonds > this.lastDiamonds) {
               this.fire(player, KnockerObserver.Event.DIAMOND);
            }

            this.lastDiamonds = diamonds;
            int torches = stats.method_15024(class_3468.field_15372, class_1802.field_8810);
            if (torches > this.lastTorches) {
               this.fire(player, KnockerObserver.Event.TORCH);
            }

            this.lastTorches = torches;
            int kills = stats.method_15024(class_3468.field_15419, class_3468.field_15414);
            if (kills > this.lastKills) {
               this.fire(player, KnockerObserver.Event.KILL);
            }

            this.lastKills = kills;
            int deaths = stats.method_15024(class_3468.field_15419, class_3468.field_15421);
            if (deaths > this.lastDeaths) {
               this.fire(player, KnockerObserver.Event.DEATH);
            }

            this.lastDeaths = deaths;
            int top = player.method_37908().method_8624(class_2903.field_13203, player.method_31477(), player.method_31479());
            boolean inCave = top - player.method_31478() > 10 && !player.method_37908().method_8311(player.method_24515());
            if (inCave && !this.wasInCave) {
               this.fire(player, KnockerObserver.Event.CAVE);
            }

            this.wasInCave = inCave;
            if (now >= this.nextAmbientTick) {
               this.nextAmbientTick = now + 6000 + this.random.nextInt(6000);
               if (this.chat.canSpeakUnprompted(900)) {
                  this.ambient(player, this.walked(stats));
               }
            }

         }
      }
   }

   private void startSession(class_3222 player) {
      this.trackedPlayer = player.method_5667();
      class_3442 stats = player.method_14248();
      this.baseWalk = walkStat(stats);
      this.lastDiamonds = stats.method_15024(class_3468.field_15427, class_2246.field_10442) + stats.method_15024(class_3468.field_15427, class_2246.field_29029);
      this.lastTorches = stats.method_15024(class_3468.field_15372, class_1802.field_8810);
      this.lastKills = stats.method_15024(class_3468.field_15419, class_3468.field_15414);
      this.lastDeaths = stats.method_15024(class_3468.field_15419, class_3468.field_15421);
      this.wasSleeping = player.method_6113();
      this.wasInCave = false;
      this.lastPos = null;
   }

   private static int walkStat(class_3442 stats) {
      return stats.method_15024(class_3468.field_15419, class_3468.field_15377) + stats.method_15024(class_3468.field_15419, class_3468.field_15364);
   }

   private int walked(class_3442 stats) {
      return (walkStat(stats) - this.baseWalk) / 100;
   }

   private void ambient(class_3222 player, int walkedBlocks) {
      List<Event> kinds = new ArrayList();
      if (walkedBlocks >= 300) {
         kinds.add(KnockerObserver.Event.STEPS);
      }

      if (this.nightsSlept >= 2) {
         kinds.add(KnockerObserver.Event.NIGHTS);
      }

      kinds.add(KnockerObserver.Event.CLOCK);
      if (!this.oldQuotes().isEmpty()) {
         kinds.add(KnockerObserver.Event.QUOTE);
      }

      Collections.shuffle(kinds, this.random);

      for(Event kind : kinds) {
         boolean var10000;
         switch (kind.ordinal()) {
            case 9:
               var10000 = this.fire(player, KnockerObserver.Event.STEPS, "{n}", String.valueOf(walkedBlocks));
               break;
            case 10:
               var10000 = this.fire(player, KnockerObserver.Event.NIGHTS, "{n}", String.valueOf(this.nightsSlept), "{nights}", plural((long)this.nightsSlept, "ночь", "ночи", "ночей"));
               break;
            case 11:
               var10000 = this.fire(player, KnockerObserver.Event.CLOCK, "{time}", clock(System.currentTimeMillis()));
               break;
            case 12:
               List<Quote> old = this.oldQuotes();
               Quote q = (Quote)old.get(this.random.nextInt(old.size()));
               var10000 = this.fire(player, KnockerObserver.Event.QUOTE, "{q}", q.text(), "{ago}", ago(q.time()));
               break;
            default:
               var10000 = false;
         }

         boolean said = var10000;
         if (said) {
            return;
         }
      }

   }

   public void onPlayerLeft() {
      this.lastLogoutMillis = System.currentTimeMillis();
   }

   public void onPlayerReturned(class_3222 player) {
      if (this.lastLogoutMillis != 0L) {
         long away = System.currentTimeMillis() - this.lastLogoutMillis;
         long left = this.lastLogoutMillis;
         this.lastLogoutMillis = 0L;
         if (away >= 120000L) {
            this.nextAnyTick = 0;
            this.fire(player, KnockerObserver.Event.RETURN, "{time}", clock(left), "{away}", duration(away));
         }
      }
   }

   public void rememberQuote(String raw) {
      String text = raw.trim();
      if (text.length() >= 8 && text.length() <= 60 && !text.startsWith("/")) {
         this.quotes.add(new Quote(System.currentTimeMillis(), text));

         while(this.quotes.size() > 20) {
            this.quotes.remove(0);
         }

      }
   }

   public int getNightsSlept() {
      return this.nightsSlept;
   }

   public String randomQuote() {
      List<Quote> old = this.oldQuotes();
      return old.isEmpty() ? null : ((Quote)old.get(this.random.nextInt(old.size()))).text();
   }

   private List<Quote> oldQuotes() {
      long border = System.currentTimeMillis() - 600000L;
      List<Quote> old = new ArrayList();

      for(Quote q : this.quotes) {
         if (q.time() < border) {
            old.add(q);
         }
      }

      return old;
   }

   private boolean fire(class_3222 player, Event event, String... replacements) {
      String[][] table = (String[][])LINES.get(event);
      String[] pool = table[this.knocker.getPhase().ordinal()];
      if (pool == null) {
         return false;
      } else {
         int now = this.knocker.tickCounter;
         if (now < this.nextAnyTick) {
            return false;
         } else {
            Integer next = (Integer)this.nextAllowed.get(event);
            if (next != null && now < next) {
               return false;
            } else if (this.knocker.method_5739(player) > 48.0F && event != KnockerObserver.Event.RETURN && event != KnockerObserver.Event.DEATH) {
               return false;
            } else if (event != KnockerObserver.Event.RETURN && event != KnockerObserver.Event.DEATH && !this.chat.canSpeakUnprompted(1200)) {
               return false;
            } else {
               this.nextAnyTick = now + 2400;
               this.nextAllowed.put(event, now + cooldown(event));
               String line = pool[this.random.nextInt(pool.length)];

               for(int i = 0; i + 1 < replacements.length; i += 2) {
                  line = line.replace(replacements[i], replacements[i + 1]);
               }

               this.chat.say(player, line);
               return true;
            }
         }
      }
   }

   private static String clock(long millis) {
      return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(CLOCK_FORMAT);
   }

   private static String duration(long millis) {
      long minutes = millis / 60000L;
      if (minutes < 60L) {
         return minutes + " " + plural(minutes, "минуту", "минуты", "минут");
      } else {
         long hours = minutes / 60L;
         if (hours < 48L) {
            return hours + " " + plural(hours, "час", "часа", "часов");
         } else {
            long days = hours / 24L;
            return days + " " + plural(days, "день", "дня", "дней");
         }
      }
   }

   private static String ago(long millis) {
      long days = (System.currentTimeMillis() - millis) / 86400000L;
      if (days <= 0L) {
         return "сегодня";
      } else {
         return days == 1L ? "вчера" : days + " " + plural(days, "день", "дня", "дней") + " назад";
      }
   }

   public static String plural(long n, String one, String few, String many) {
      long mod100 = n % 100L;
      long mod10 = n % 10L;
      if (mod100 >= 11L && mod100 <= 14L) {
         return many;
      } else if (mod10 == 1L) {
         return one;
      } else {
         return mod10 >= 2L && mod10 <= 4L ? few : many;
      }
   }

   public void writeNbt(class_2487 nbt) {
      nbt.method_10569("NightsSlept", this.nightsSlept);
      nbt.method_10544("LastLogout", this.lastLogoutMillis);
      class_2499 list = new class_2499();

      for(Quote q : this.quotes) {
         long var10001 = q.time();
         list.add(class_2519.method_23256(var10001 + "\t" + q.text()));
      }

      nbt.method_10566("Quotes", list);
   }

   public void readNbt(class_2487 nbt) {
      this.nightsSlept = nbt.method_10550("NightsSlept");
      this.lastLogoutMillis = nbt.method_10537("LastLogout");
      this.quotes.clear();
      class_2499 list = nbt.method_10554("Quotes", 8);

      for(int i = 0; i < list.size(); ++i) {
         String s = list.method_10608(i);
         int tab = s.indexOf(9);
         if (tab > 0) {
            try {
               this.quotes.add(new Quote(Long.parseLong(s.substring(0, tab)), s.substring(tab + 1)));
            } catch (NumberFormatException var7) {
            }
         }
      }

   }

   static {
      LINES.put(KnockerObserver.Event.DIAMOND, new String[][]{null, {"блестит..."}, {"ты нашла алмазы. я видел. молодец"}, {"алмазы... они почти такие же красивые, как ты"}, {"я знаю, где ты их хранишь"}, {"я переложу их. только я буду знать, где они"}});
      LINES.put(KnockerObserver.Event.TORCH, new String[][]{null, {"ярко... не люблю свет"}, {"зачем ты поставила факел. мне и так было видно"}, {"свет... ради тебя я потерплю"}, {"убери свет. я хочу видеть тебя в темноте"}, {"СВЕТ НЕ ПОМОЖЕТ."}});
      LINES.put(KnockerObserver.Event.KILL, new String[][]{{"ха..."}, {"он тебе мешал?.."}, {"осторожнее... я бы сам его убил"}, {"он не должен был приближаться к тебе..."}, {"Теперь он мёртв... как и любой, кто подойдёт"}, {"Ты ударила его... как смеешь трогать кого-то, кроме меня?"}});
      LINES.put(KnockerObserver.Event.DEATH, new String[][]{{"..."}, {"ты... исчезла на секунду"}, {"не делай так больше. пожалуйста"}, {"я думал, что потерял тебя..."}, {"ты не имеешь права умирать без меня"}, {"Я ВИДЕЛ. НИКОГДА БОЛЬШЕ ТАК НЕ ДЕЛАЙ."}});
      LINES.put(KnockerObserver.Event.FALL, new String[][]{null, {"ой..."}, {"Осторожнее... я не вынесу, если ты пострадаешь"}, {"Я всегда поймаю тебя"}, {"Я говорил... я всегда поймаю тебя"}, {"Я помню этот момент... я запомню каждый твой шаг"}});
      LINES.put(KnockerObserver.Event.RUN, new String[][]{null, {"куда так быстро?"}, {"не убегай... я всё равно рядом"}, {"подожди меня..."}, {"от меня не убегают."}, {"БЕГИ. БЕГИ. Я ВСЁ РАВНО БЫСТРЕЕ."}});
      LINES.put(KnockerObserver.Event.CAVE, new String[][]{null, {"тут темно... тебе страшно?"}, {"Я чувствую мобов... я рядом"}, {"держись ближе... тут опасно"}, {"под землёй никто не услышит. только я"}, {"здесь только ты и я. наконец-то"}});
      LINES.put(KnockerObserver.Event.AFK, new String[][]{null, {"ау", "ты тут?"}, {"ты тут?", "ты уснула?.."}, {"ты уснула?.. я подожду", "ау... я скучаю"}, {"Стой так... я хочу запомнить этот момент"}, null});
      LINES.put(KnockerObserver.Event.SLEEP, new String[][]{{"спокойной ночи..."}, {"спишь?.."}, {"спи... я посторожу"}, {"спокойной ночи... я буду рядом"}, {"Нет... я смотрю, как ты спишь"}, {"Спи. Я буду смотреть. Всю ночь."}});
      LINES.put(KnockerObserver.Event.STEPS, new String[][]{null, null, {"ты сегодня прошла {n} блоков... я шёл рядом"}, {"я считал твои шаги сегодня. {n}"}, {"{n} блоков. я знаю каждый"}, {"{n} БЛОКОВ. КУДА ТЫ ТАК ХОДИШЬ?"}});
      LINES.put(KnockerObserver.Event.NIGHTS, new String[][]{null, null, null, {"ты спала здесь {n} {nights}... я помню каждую"}, {"ты спала {n} {nights}. я был рядом все {n}"}, {"{n} {nights} я смотрел, как ты спишь. ты даже не заметила"}});
      LINES.put(KnockerObserver.Event.CLOCK, new String[][]{null, null, null, {"уже {time}... тебе не пора спать?"}, {"{time}. ты должна быть со мной"}, {"сейчас {time}. я знаю, где ты сидишь."}});
      LINES.put(KnockerObserver.Event.QUOTE, new String[][]{null, null, {"ты {ago} сказала «{q}». я помню"}, {"помнишь, ты {ago} сказала «{q}»?.. я всё помню"}, {"«{q}». ты сказала это {ago}. я помню каждое слово"}, {"«{q}». {ago}. ты думала, я забуду?"}});
      LINES.put(KnockerObserver.Event.RETURN, new String[][]{{"..."}, {"ты вернулась..."}, {"тебя не было {away}... я ждал"}, {"ты ушла в {time}... и тебя не было {away}. я скучал"}, {"{away}. тебя не было {away}. где ты была?"}, {"почему ты вышла в {time}. с кем ты была {away}?"}});
      CLOCK_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
   }

   public static enum Event {
      DIAMOND,
      TORCH,
      KILL,
      DEATH,
      FALL,
      RUN,
      CAVE,
      AFK,
      SLEEP,
      STEPS,
      NIGHTS,
      CLOCK,
      QUOTE,
      RETURN;

      // $FF: synthetic method
      private static Event[] $values() {
         return new Event[]{DIAMOND, TORCH, KILL, DEATH, FALL, RUN, CAVE, AFK, SLEEP, STEPS, NIGHTS, CLOCK, QUOTE, RETURN};
      }
   }

   private static record Quote(long time, String text) {
   }
}
