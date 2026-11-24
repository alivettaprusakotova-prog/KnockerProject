package com.theneru.knocker.entity;

import com.theneru.knocker.AiKnocker;
import com.theneru.knocker.KnockerAdvancements;
import com.theneru.knocker.KnockerConfig;
import com.theneru.knocker.KnockerMemoryStore;
import com.theneru.knocker.ModSounds;
import com.theneru.knocker.util.KnockerMessages;
import com.theneru.knocker.util.KnockerPhase;
import com.theneru.knocker.util.KnockerWords;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.regex.Pattern;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.server.MinecraftServer;

public class KnockerChat {
   private final KnockerEntity knocker;
   private final KnockerMovement movement;
   private final Random random = new Random();
   private boolean waitingForAI = false;
   private final KnockerBrain brain;
   private int lastSpokeTick = -100000;
   private final Deque<Outgoing> queue = new ArrayDeque();
   private int queueTimer = 0;
   private String queuedMessage = null;
   private int talkPointsTick = -100000;
   private int complimentTick = -100000;
   private int loveTick = -100000;
   private int aboutTick = -100000;
   public KnockerChat(KnockerEntity knocker, KnockerMovement movement) {
      this.knocker = knocker;
      this.movement = movement;
      this.brain = new KnockerBrain(knocker);
   }

   public boolean canSpeakUnprompted(int gap) {
      return this.queue.isEmpty() && this.knocker.tickCounter - this.lastSpokeTick >= gap;
   }

   /**
    * Все слова игрока уходят локальной модели. Заскриптованные ответы на
    * «обними», «поцелуй», «я твоя» и т.п. полностью убраны: ИИ сам решает,
    * что сказать и какое действие поставить в конце. Единственное исключение —
    * прямая просьба о подарке: она всегда исполняется мгновенно.
    */
   public void handlePlayerMessage(class_1657 player, String message) {
      if (player == null || message == null || message.isBlank() || this.knocker.isAway()) {
         return;
      }

      String norm = KnockerMessages.normalize(message);
      if (!KnockerConfig.useExternalAI() && this.knocker.getEnding().onChat(player, message)) {
         return;
      }

      KnockerAdvancements.grant(player, "first_contact");
      if (!KnockerBrain.isRudeText(message)) {
         this.knocker.getObserver().rememberQuote(message);
         this.addTalkPoints(norm);
      }

      KnockerMemoryStore.addHerLine(player.method_5667(), message);
      if (KnockerGender.get(player.method_5667()) == KnockerGender.G.UNKNOWN) {
         KnockerGender.set(player.method_5667(), KnockerGender.detect(norm));
      }

      if (KnockerMessages.containsAny(norm, KnockerWords.LOVE, KnockerWords.I_LIKE_YOU, KnockerWords.BEAUTIFUL)) {
         this.knocker.getThoughts().onCompliment(player, this.knocker.getPhase(), KnockerMessages.containsAny(norm, KnockerWords.LOVE));
         this.knocker.getMood().onCompliment();
      }

      this.knocker.getStories().scanNames(player, message);

      // Вернуть украденную вещь можно без скриптовой реплики — ответ всё равно даст ИИ.
      if (KnockerMessages.containsAny(norm, "отдай", "верни", "дай обратно")) {
         String back = this.knocker.getHelp().giveBackCarried(player);
         if (back == null) {
            back = this.knocker.getItems().handleGiveBack(player, norm);
         }
         if (back != null) {
            this.knocker.rememberEvent("она попросила вернуть её вещь, и я отдал");
         }
      }

      boolean giftGiven = false;
      if (KnockerItems.isGiftRequest(message)) {
         giftGiven = this.knocker.getItems().giftFromRequest(player, message);
      }

      this.brain.observe(message);

      if (!KnockerConfig.useExternalAI()) {
         String reply = this.brain.reply(player, this.knocker.getPhase(), message);
         this.pendingRude = KnockerBrain.isRudeText(message);
         this.applyBrainAction(player);
         this.remember(player, message, reply);
         this.say(player, reply);
         return;
      }

      if (this.waitingForAI) {
         this.queuedMessage = message;
         return;
      }

      MinecraftServer server = player.method_5682();
      if (server == null) {
         return;
      }

      this.waitingForAI = true;
      player.method_7353(class_2561.method_43470("§8Кнокер печатает..."), true);
      List<String[]> snapshot = new ArrayList<>(KnockerMemoryStore.recent(player.method_5667(), KnockerMemoryStore.PROMPT_HISTORY));
      KnockerAI.ask(this.knocker, player, message, snapshot)
            .whenComplete((reply, error) -> server.execute(() -> this.onAiResult(player, message, reply, error, giftGiven)));
   }

   private String fixNameMixup(class_1657 player, String text) {
      String his = (String)KnockerMemoryStore.facts(player.method_5667()).get("his_name");
      if (his != null && his.length() >= 2) {
         String hers = this.brain.getPlayerName();
         String q = Pattern.quote(his);
         String out = text.replaceAll("(?iu),\\s*" + q + "(?=[.!?…)]|\\s*$)", hers != null ? ", " + hers : "").replaceAll("(?iu)^" + q + ",\\s*", hers != null ? hers + ", " : "");
         return out.trim();
      } else {
         return text;
      }
   }

   private void onAiResult(class_1657 player, String message, String reply, Throwable error, boolean giftAlreadyGiven) {
      this.waitingForAI = false;
      if (this.knocker.method_31481() || player.method_31481()) {
         return;
      }

      if (this.queuedMessage != null) {
         String next = this.queuedMessage;
         this.queuedMessage = null;
         MinecraftServer srv = player.method_5682();
         if (srv != null) {
            srv.execute(() -> this.handlePlayerMessage(player, next));
         }
      }

      if (error == null && reply != null && !reply.isBlank()) {
         String[] parsed = KnockerAI.extractAction(reply);
         String text = KnockerAI.tidy(KnockerAI.keepRussian(parsed[0]));
         if (text == null) {
            text = KnockerAI.tidy(parsed[0]);
         }
         if (text != null) {
            text = this.fixNameMixup(player, text);
         }

         if (text == null || text.isBlank()) {
            text = this.brain.ambient(this.knocker.getPhase());
         }

         this.say(player, text);
         this.remember(player, message, text);
         this.doAiAction(parsed[1], parsed[2], player, giftAlreadyGiven);
      } else {
         Throwable cause = error instanceof CompletionException && error.getCause() != null ? error.getCause() : error;
         String why = cause == null ? "пустой ответ" : cause.getMessage();
         AiKnocker.LOGGER.warn("Локальная модель не ответила: {}", why, cause);
         if (KnockerConfig.debug) {
            player.method_7353(class_2561.method_43470("§8[AI Knocker] ИИ: " + why), false);
         }

         String line = this.brain.ambient(this.knocker.getPhase());
         this.remember(player, message, line);
         this.say(player, line);
      }
   }

   private void addTalkPoints(String norm) {
      int now = this.knocker.tickCounter;
      if (now - this.talkPointsTick >= 1200) {
         this.talkPointsTick = now;
         this.knocker.addObsession(1);
      }

      if (KnockerMessages.containsAny(norm, KnockerWords.LOVE) && now - this.loveTick >= 6000) {
         this.loveTick = now;
         this.knocker.addObsession(5);
      } else if (KnockerMessages.containsAny(norm, KnockerWords.join(KnockerWords.I_LIKE_YOU, KnockerWords.BEAUTIFUL)) && now - this.complimentTick >= 2400) {
         this.complimentTick = now;
         this.knocker.addObsession(3);
      }

      if (KnockerMessages.containsAny(norm, "меня зовут", "моё имя", "мое имя", "я люблю", "мне нравится", "я боюсь", "я ненавижу", "я не люблю", "мой любимый", "моя любимая", "мои любимые", "моё любимое", "мое любимое", "я живу", "мне лет") && now - this.aboutTick >= 2400) {
         this.aboutTick = now;
         this.knocker.addObsession(2);
      }

   }

   private void applyBrainAction(class_1657 player) {
      if (this.brain.getLastIntent() == KnockerBrain.Intent.INSULT) {
         this.knocker.onInsulted();
      } else if (this.brain.getLastIntent() == KnockerBrain.Intent.STOP_FOLLOW) {
         if (this.pendingRude) {
            this.knocker.onInsulted();
         }

         KnockerPhase phase = this.knocker.getPhase();
         if (phase.ordinal() <= KnockerPhase.LOVE.ordinal()) {
            this.movement.pauseFollow(player, 1200);
         } else if (phase == KnockerPhase.PARANOIA) {
            this.movement.teleportBehind(player);
         }

      }
   }

   private void doAiAction(String action, String giftName, class_1657 player, boolean giftAlreadyGiven) {
      if (action == null || action.isBlank()) {
         return;
      }

      KnockerPhase phase = this.knocker.getPhase();
      if (action.startsWith("подойт")) {
         this.movement.teleportBehind(player);
      } else if (action.startsWith("появ")) {
         if (phase.ordinal() >= KnockerPhase.OBSESSION.ordinal()) {
            this.movement.appearInFront(player);
         } else {
            this.movement.teleportBehind(player);
         }
      } else if (action.startsWith("отой")) {
         this.movement.pauseFollow(player, 1200);
      } else if (action.startsWith("подар")) {
         if (!giftAlreadyGiven) {
            this.knocker.getItems().aiGift(player, giftName);
         }
      } else if (action.startsWith("защит")) {
         this.knocker.getGuard().help(player);
      } else if (action.startsWith("смех")) {
         this.knocker.playAction("laugh");
      } else if (action.startsWith("смущ")) {
         this.knocker.playAction("shy");
      }
   }

   private void remember(class_1657 player, String playerMessage, String reply) {
      UUID id = player.method_5667();
      KnockerMemoryStore.addMessage(id, "user", playerMessage);
      KnockerMemoryStore.addMessage(id, "assistant", reply);
      List<String[]> old = KnockerMemoryStore.takeOldestIfLong(id);
      MinecraftServer server = player.method_5682();
      if (old != null && server != null && KnockerConfig.useExternalAI()) {
         KnockerAI.summarize(old, KnockerMemoryStore.diary(id)).whenComplete((facts, error) -> {
            if (facts != null && !facts.isEmpty()) {
               server.execute(() -> KnockerMemoryStore.addDiary(id, facts));
            }

            if (error != null) {
               AiKnocker.LOGGER.warn("Не удалось записать дневник: {}", error.getMessage());
            }

         });
      }

   }

   public void say(class_1657 player, String text) {
      if (text != null && !text.isBlank()) {
         List<String> parts = this.humanize(player, text);
         this.sendNow(player, (String)parts.get(0));

         for(int i = 1; i < parts.size(); ++i) {
            this.queue.addLast(new Outgoing(player, (String)parts.get(i), 15 + this.random.nextInt(25)));
         }

      }
   }

   public void sayLater(class_1657 player, String text, int delay) {
      if (text != null && !text.isBlank()) {
         player.method_7353(class_2561.method_43470("§8Кнокер печатает..."), true);
         List<String> parts = this.humanize(player, text);
         this.queue.addLast(new Outgoing(player, (String)parts.get(0), delay));

         for(int i = 1; i < parts.size(); ++i) {
            this.queue.addLast(new Outgoing(player, (String)parts.get(i), 15 + this.random.nextInt(25)));
         }

      }
   }

   public void sayRandomPhrase(class_1657 player) {
      if (player == null || this.random.nextInt(100) >= 15) {
         return;
      }
      // Редкие атмосферные реплики вместо частых скриптовых фраз из STATEMENT/FLIRT.
      this.say(player, this.brain.ambient(this.knocker.getPhase()));
   }
   public void tick() {
      Outgoing next = (Outgoing)this.queue.peekFirst();
      if (next == null) {
         this.queueTimer = 0;
      } else if (++this.queueTimer >= next.delay()) {
         this.queue.pollFirst();
         this.queueTimer = 0;
         if (!next.player().method_31481()) {
            this.sendNow(next.player(), next.text());
         }

      }
   }

   public String displayName(class_1657 player) {
      if (this.knocker.getPhase().ordinal() >= KnockerPhase.ATTACHED.ordinal()) {
         String his = (String)KnockerMemoryStore.facts(player.method_5667()).get("his_name");
         if (his != null && !his.isBlank()) {
            return his;
         }
      }

      return "Кнокер";
   }

   private void sendNow(class_1657 player, String text) {
      this.lastSpokeTick = this.knocker.tickCounter;
      this.brain.noteSaid(text);
      boolean human = KnockerConfig.chatStyle.equals("human");
      KnockerPhase phase = this.knocker.getPhase();
      if (human) {
         int color = KnockerThoughts.color(phase);
         String var10001 = this.displayName(player);
         player.method_7353(class_2561.method_43470("<" + var10001 + "> ").method_10852(class_2561.method_43470(text).method_27694((s) -> s.method_36139(color))), false);
      } else {
         int color = KnockerThoughts.color(phase);
         String var9 = this.displayName(player);
         player.method_7353(class_2561.method_43470("§c[" + var9 + "] ").method_10852(class_2561.method_43470(text).method_27694((s) -> s.method_36139(color))), false);
      }

      String lower = text.toLowerCase(Locale.ROOT);
      if (!lower.contains("хе-хе") && !lower.contains("ха-ха") && !lower.contains("хи-хи")) {
         if (isShouting(text) && phase.ordinal() >= KnockerPhase.OBSESSION.ordinal()) {
            this.knocker.playAction("scream");
         } else if (text.trim().endsWith("?") && phase.ordinal() >= KnockerPhase.OBSESSION.ordinal() && this.random.nextInt(6) == 0) {
            this.knocker.playAction("head_tilt");
         }
      } else {
         this.knocker.playAction("laugh");
      }

      if (!lower.contains("хе-хе") && !lower.contains("ха-ха") && !lower.contains("хи-хи")) {
         if (lower.contains("тук")) {
            this.knocker.playSoundFor(player, ModSounds.KNOCK, 0.6F, 1.0F);
         } else if (isShouting(text) && phase == KnockerPhase.PARANOIA && this.random.nextInt(10) < 3) {
            this.knocker.playSoundFor(player, ModSounds.SCREAM, 0.35F, 1.0F);
         } else if (phase.ordinal() <= KnockerPhase.CURIOUS.ordinal() && this.random.nextInt(8) == 0) {
            this.knocker.playSoundFor(player, ModSounds.WHISPER, 0.35F, 0.9F + this.random.nextFloat() * 0.2F);
         }
      } else {
         boolean distorted = phase == KnockerPhase.PARANOIA && this.random.nextInt(3) == 0;
         this.knocker.playSoundFor(player, distorted ? ModSounds.LAUGH_DISTORTED : ModSounds.LAUGH, 0.6F, 1.0F);
      }

   }

   private List<String> humanize(class_1657 player, String text) {
      List<String> parts = new ArrayList();
      if (KnockerConfig.chatStyle.equals("human") && !text.isBlank()) {
         boolean shouting = isShouting(text);
         KnockerPhase phase = this.knocker.getPhase();
         String t = text.trim();
         if (!shouting && phase.ordinal() <= KnockerPhase.ATTACHED.ordinal()) {
            t = lowerFirst(t);
         }

         if (t.endsWith(".") && !t.endsWith("..") && this.random.nextInt(10) < 6) {
            t = t.substring(0, t.length() - 1);
         }

         t = KnockerStyle.apply(t, player.method_5667(), phase, shouting);
         t = KnockerGender.adapt(t, player.method_5667());
         int split = t.indexOf("... ");
         if (split > 3 && t.length() > 20 && this.random.nextInt(10) < 4) {
            parts.add(t.substring(0, split + 3));
            String rest = t.substring(split + 4).trim();
            parts.add(shouting ? rest : lowerFirst(rest));
         } else {
            parts.add(t);
         }

         return parts;
      } else {
         parts.add(KnockerGender.adapt(text, player.method_5667()));
         return parts;
      }
   }

   private static String lowerFirst(String s) {
      if (s.isEmpty()) {
         return s;
      } else if (s.length() > 1 && Character.isUpperCase(s.charAt(1))) {
         return s;
      } else {
         String var10000 = s.substring(0, 1).toLowerCase(Locale.ROOT);
         return var10000 + s.substring(1);
      }
   }

   private static boolean isShouting(String text) {
      int upper = 0;

      for(int i = 0; i < text.length(); ++i) {
         if (Character.isUpperCase(text.charAt(i))) {
            ++upper;
         }
      }

      return upper >= 6;
   }

   public KnockerBrain getBrain() {
      return this.brain;
   }

   private static record Outgoing(class_1657 player, String text, int delay) {
   }
}
