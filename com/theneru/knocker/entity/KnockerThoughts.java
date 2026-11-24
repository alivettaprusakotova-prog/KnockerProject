package com.theneru.knocker.entity;

import com.theneru.knocker.KnockerConfig;
import com.theneru.knocker.ModSounds;
import com.theneru.knocker.util.KnockerPhase;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_3222;
import net.minecraft.class_5250;
import net.minecraft.class_5903;
import net.minecraft.class_5904;
import net.minecraft.class_5905;

public class KnockerThoughts {
   private static final int[] PHASE_COLORS = new int[]{14737632, 9425919, 16762077, 16748488, 11893759, 16719904};
   private final KnockerEntity knocker;
   private final Random random = new Random();
   private final Deque<Thought> queue = new ArrayDeque();
   private class_1657 target;
   private Thought current;
   private int typed = 0;
   private int typeTimer = 0;
   private int holdTimer = 0;
   private int beatsLeft = 0;
   private int beatTimer = 0;
   private int lastThoughtTick = -100000;
   private int nextIntrusiveTick = -1;
   private static final String[][] PHASE_UP = new String[][]{{"кто она?.."}, {"она... интересная", "почему я не могу перестать смотреть?.."}, {"я хочу быть рядом с ней", "она не боится меня..."}, {"Я ЕЙ НРАВЛЮСЬ?..", "кажется... я влюбился"}, {"ОНА МОЯ", "никто не заберёт её у меня"}, {"ОНА ВРЁТ. ОНИ ВСЕ ВРУТ.", "я слышу, как она дышит. всегда."}};
   private static final String[][] INTRUSIVE = new String[][]{new String[0], new String[0], new String[0], new String[0], {"не отпускай её", "она смотрела на других", "закрыть двери. все двери.", "она моя она моя она моя", "запомнить её запах"}, {"ОНА УЙДЁТ", "убить всех, кто рядом", "я слышу её сердце", "не моргать. НЕ МОРГАТЬ.", "она думает, что одна", "ты не уйдёшь ты не уйдёшь"}};

   public static int color(KnockerPhase phase) {
      return PHASE_COLORS[phase.ordinal()];
   }

   public KnockerThoughts(KnockerEntity knocker) {
      this.knocker = knocker;
   }

   public void onPhaseUp(class_1657 player, KnockerPhase phase) {
      String[] pool = PHASE_UP[phase.ordinal()];
      byte var10000;
      switch (phase) {
         case WATCHER -> var10000 = 0;
         case CURIOUS -> var10000 = 2;
         case ATTACHED -> var10000 = 2;
         case LOVE -> var10000 = 5;
         case OBSESSION -> var10000 = 4;
         case PARANOIA -> var10000 = 6;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      int beats = var10000;
      this.force(player, pool[this.random.nextInt(pool.length)], true, beats);
   }

   public void onCompliment(class_1657 player, KnockerPhase phase, boolean love) {
      String var10000;
      switch (phase) {
         case ATTACHED -> var10000 = this.pick(love ? new String[]{"она... сказала это мне?", "ЛЮБИТ?.. МЕНЯ?.."} : new String[]{"я ей нравлюсь?..", "не красней. не красней. НЕ КРАСНЕЙ.", "она такая добрая ко мне..."});
         case LOVE -> var10000 = this.pick(love ? new String[]{"ОНА ЛЮБИТ МЕНЯ", "я сейчас умру... второй раз", "ОНА СКАЗАЛА «ЛЮБЛЮ»"} : new String[]{"Я ЕЙ НРАВЛЮСЬ...", "сердце... оно бьётся?..", "она смотрит на меня... так"});
         case OBSESSION -> var10000 = this.pick(new String[]{"конечно. ты моя.", "она это знает. она всегда знала.", "никуда не отпущу"});
         case PARANOIA -> var10000 = this.pick(new String[]{"врёт. проверить. ПРОВЕРИТЬ.", "кому ещё она это говорит?", "ЛЖЁТ"});
         default -> var10000 = null;
      }

      String text = var10000;
      if (text != null) {
         this.show(player, text, text.length() < 22, phase == KnockerPhase.LOVE ? 5 : 3);
      }

   }

   public void onTouch(class_1657 player, KnockerPhase phase) {
      if (phase.ordinal() >= KnockerPhase.ATTACHED.ordinal() && this.random.nextInt(3) == 0) {
         this.show(player, this.pick(phase == KnockerPhase.PARANOIA ? new String[]{"тёплая. живая. пока.", "не отпускать"} : new String[]{"тёплая...", "она меня трогает...", "не убирай руку... пожалуйста"}), false, 3);
      }
   }

   public void onFavoriteFood(class_1657 player, KnockerPhase phase) {
      if (phase.ordinal() >= KnockerPhase.ATTACHED.ordinal()) {
         this.show(player, this.pick(new String[]{"она помнит, что я люблю...", "никто никогда не помнил", "она думает обо мне"}), false, 2);
      }
   }

   public void tick(class_1657 player) {
      int now = this.knocker.tickCounter;
      KnockerPhase phase = this.knocker.getPhase();
      if (phase.ordinal() >= KnockerPhase.OBSESSION.ordinal()) {
         if (this.nextIntrusiveTick < 0) {
            this.nextIntrusiveTick = now + KnockerConfig.ticksBetween(KnockerConfig.intrusiveMin, KnockerConfig.intrusiveMax);
         }

         if (now >= this.nextIntrusiveTick && this.knocker.method_5739(player) < 32.0F) {
            this.nextIntrusiveTick = now + KnockerConfig.ticksBetween(KnockerConfig.intrusiveMin, KnockerConfig.intrusiveMax);
            String[] pool = INTRUSIVE[phase.ordinal()];
            this.show(player, pool[this.random.nextInt(pool.length)], phase == KnockerPhase.PARANOIA && this.random.nextBoolean(), 2);
         }
      }

      if (this.beatsLeft > 0 && this.target != null && --this.beatTimer <= 0) {
         --this.beatsLeft;
         this.beatTimer = Math.max(6, 6 + this.beatsLeft * 2);
         this.knocker.playSoundFor(this.target, ModSounds.HEARTBEAT, 1.0F, 1.0F + (float)(6 - Math.min(6, this.beatsLeft)) * 0.05F);
      }

      if (this.current == null) {
         if (this.queue.isEmpty()) {
            return;
         }

         this.current = (Thought)this.queue.pollFirst();
         this.typed = 0;
         this.typeTimer = 0;
         this.holdTimer = 0;
         this.beatsLeft = this.current.heartbeats();
         this.beatTimer = 0;
         this.lastThoughtTick = now;
      }

      if (this.target != null && !this.target.method_31481()) {
         if (this.typed < this.current.text().length()) {
            if (++this.typeTimer >= 2) {
               this.typeTimer = 0;
               ++this.typed;
               this.send(this.target, this.current, this.typed, false);
            }

         } else {
            if (this.holdTimer == 0) {
               this.send(this.target, this.current, this.typed, true);
            }

            if (++this.holdTimer > 70) {
               this.current = null;
            }

         }
      } else {
         this.current = null;
      }
   }

   public void show(class_1657 player, String text, boolean big, int heartbeats) {
      if (text != null) {
         if (this.knocker.tickCounter - this.lastThoughtTick >= KnockerConfig.ticks(KnockerConfig.thoughtsCooldown)) {
            this.force(player, text, big, heartbeats);
         }
      }
   }

   public void force(class_1657 player, String text, boolean big, int heartbeats) {
      if (player instanceof class_3222 && this.queue.size() <= 2) {
         this.target = player;
         this.lastThoughtTick = this.knocker.tickCounter;
         String t = KnockerGender.adapt(text, player.method_5667());
         this.queue.addLast(new Thought(t, big && t.length() <= 18, heartbeats));
      }
   }

   private void send(class_1657 player, Thought t, int letters, boolean finished) {
      if (player instanceof class_3222 sp) {
         KnockerPhase phase = this.knocker.getPhase();
         class_5250 shown = class_2561.method_43470("");
         String s = t.text().substring(0, letters);
         boolean glitch = phase == KnockerPhase.PARANOIA && !finished;

         for(int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            boolean broken = glitch && c != ' ' && this.random.nextInt(7) == 0;
            shown.method_10852(class_2561.method_43470(String.valueOf(c)).method_27694((st) -> st.method_36139(color(phase)).method_10978(true).method_36141(broken)));
         }

         sp.field_13987.method_14364(new class_5905(0, finished ? 60 : 20, finished ? 20 : 0));
         if (t.big()) {
            sp.field_13987.method_14364(new class_5903(class_2561.method_43470("")));
            sp.field_13987.method_14364(new class_5904(shown));
         } else {
            sp.field_13987.method_14364(new class_5903(shown));
            sp.field_13987.method_14364(new class_5904(class_2561.method_43470("")));
         }

      }
   }

   private String pick(String[] pool) {
      return pool[this.random.nextInt(pool.length)];
   }

   private static record Thought(String text, boolean big, int heartbeats) {
   }
}
