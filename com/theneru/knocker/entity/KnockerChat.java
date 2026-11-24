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
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.server.MinecraftServer;

public class KnockerChat {
   private static final int HISTORY_SIZE = 10;
   private final KnockerEntity knocker;
   private final KnockerMovement movement;
   private final Random random = new Random();
   private final Deque<String[]> history = new ArrayDeque();
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
   private static final String[][] HELP_LINES = new String[][]{{"...", "я смотрю...", "справишься..."}, {"...держись", "я... попробую", "не бойся..."}, {"Не бойся... я защищу тебя", "Я здесь. Держись за мной", "Сейчас..."}, {"Для тебя — всё, что угодно", "Никто тебя не тронет", "Я рядом. Всегда"}, {"Никто не смеет тебя трогать.", "Отойди. Я сам.", "Они пожалеют."}, {"КТО ПОСМЕЛ?", "Я ИХ ВСЕХ УБЬЮ.", "ОНИ — НИКТО. ТЫ — МОЯ."}};
   private boolean pendingRude = false;
   private static final int[] FLIRT_CHANCE = new int[]{0, 0, 20, 30, 35, 20};
   private static final String[][] FLIRT = new String[][]{null, null, {"у тебя красивые руки... я просто заметил. всё", "ты так близко стоишь... это нормально?", "а ты всегда так пахнешь... или только сегодня?)", "если я подойду ближе... ты не убежишь?", "я думал о тебе... ну. в смысле. просто думал", "ты сегодня какая-то... не знаю. глаз не оторвать", "можно я буду стоять рядом? просто рядом", "у тебя тёплые ладони? мне интересно"}, {"иди сюда. ближе. ещё ближе)", "ты специально так на меня смотришь?", "если ты ещё раз так улыбнёшься... я за себя не ручаюсь", "мне нравится, когда ты рядом. очень рядом", "я бы поцеловал тебя... но вдруг ты укусишь)", "ты знаешь, что сводишь меня с ума? специально же", "ночью без тебя холодно. просто говорю", "можно я обниму тебя и не буду отпускать? минут сто", "ты краснеешь или мне кажется?)", "давай сегодня ты не будешь от меня уходить"}, {"ты моя. вся. от макушки до пяток", "я хочу быть так близко, чтобы слышать, как ты дышишь", "не смотри так. а то я подумаю, что ты меня хочешь", "я помню, как ты пахнешь. каждую ночь помню", "иди ко мне. я не спрашиваю", "твоя кровать такая большая... для одной", "когда ты спишь, ты такая... беззащитная. мне нравится"}, {"ты МОЯ. никто больше не будет тебя касаться", "я хочу оставить на тебе след. чтобы все видели, чья ты", "ночью я лягу рядом. ты даже не проснёшься"}};

   public KnockerChat(KnockerEntity knocker, KnockerMovement movement) {
      this.knocker = knocker;
      this.movement = movement;
      this.brain = new KnockerBrain(knocker);
   }

   public boolean canSpeakUnprompted(int gap) {
      return this.queue.isEmpty() && this.knocker.tickCounter - this.lastSpokeTick >= gap;
   }

   public void handlePlayerMessage(class_1657 player, String message) {
      if (player != null && message != null && !message.isBlank()) {
         if (!this.knocker.isAway()) {
            if (!this.knocker.getEnding().onChat(player, message)) {
               KnockerAdvancements.grant(player, "first_contact");
               String sulk = this.knocker.getMood().handleChat(player, KnockerMessages.normalize(message));
               if (sulk != null) {
                  if (!sulk.isEmpty()) {
                     this.say(player, sulk);
                  }

                  this.remember(player, message, sulk.isEmpty() ? "(молчит, обиделся)" : sulk);
               } else {
                  if (!KnockerBrain.isRudeText(message)) {
                     this.knocker.getObserver().rememberQuote(message);
                     this.addTalkPoints(KnockerMessages.normalize(message));
                  }

                  KnockerMemoryStore.addHerLine(player.method_5667(), message);
                  if (KnockerGender.get(player.method_5667()) == KnockerGender.G.UNKNOWN) {
                     KnockerGender.set(player.method_5667(), KnockerGender.detect(KnockerMessages.normalize(message)));
                  }

                  String giveBack = KnockerMessages.containsAny(KnockerMessages.normalize(message), "отдай", "верни", "дай обратно") ? this.knocker.getHelp().giveBackCarried(player) : null;
                  if (giveBack == null) {
                     giveBack = this.knocker.getItems().handleGiveBack(player, KnockerMessages.normalize(message));
                  }

                  if (giveBack != null) {
                     this.say(player, giveBack);
                     this.remember(player, message, giveBack);
                  } else if (this.knocker.getHaunt().handleChat(player, KnockerMessages.normalize(message))) {
                     this.remember(player, message, "держи мой дневник... там про тебя");
                  } else {
                     String itemReply = this.knocker.getItems().handleChat(player, KnockerMessages.normalize(message));
                     if (itemReply != null) {
                        this.say(player, itemReply);
                        this.remember(player, message, itemReply);
                        this.brain.observe(message);
                     } else {
                        KnockerPhase phaseNow = this.knocker.getPhase();
                        String normMsg = KnockerMessages.normalize(message);
                        if (phaseNow.ordinal() >= KnockerPhase.ATTACHED.ordinal() && KnockerMessages.containsAny(normMsg, KnockerWords.join(KnockerWords.LOVE, KnockerWords.I_LIKE_YOU, KnockerWords.BEAUTIFUL))) {
                           if (phaseNow == KnockerPhase.ATTACHED || phaseNow == KnockerPhase.LOVE) {
                              this.knocker.playAction("shy");
                           }

                           this.knocker.getThoughts().onCompliment(player, phaseNow, KnockerMessages.containsAny(normMsg, KnockerWords.LOVE));
                           this.knocker.getMood().onCompliment();
                        }

                        if (KnockerMessages.isHelpRequest(KnockerMessages.normalize(message)) && !KnockerMessages.containsAny(KnockerMessages.normalize(message), KnockerWords.WHY_HELP_Q)) {
                           String reply = this.handleHelp(player);
                           this.say(player, reply);
                           this.remember(player, message, reply);
                           this.brain.observe(message);
                        } else {
                           String normNow = KnockerMessages.normalize(message);
                           this.knocker.getStories().scanNames(player, message);
                           String gameReply = this.knocker.getGames().handleChat(player, normNow);
                           if (gameReply != null) {
                              this.say(player, gameReply);
                              this.remember(player, message, gameReply);
                           } else {
                              String storyReply = this.knocker.getStories().handleChat(player, normNow);
                              if (storyReply != null) {
                                 this.say(player, storyReply);
                                 this.remember(player, message, storyReply);
                              } else if (this.knocker.getStories().wantsStory(normNow)) {
                                 this.remember(player, message, "(рассказывает ей свою историю)");
                              } else {
                                 String helpReply = this.knocker.getHelp().handleChat(player, normNow);
                                 if (helpReply != null) {
                                    this.say(player, helpReply);
                                    this.remember(player, message, helpReply);
                                 } else {
                                    String navReply = this.knocker.getNav().handleChat(player, KnockerMessages.normalize(message));
                                    if (navReply != null) {
                                       this.say(player, navReply);
                                       this.remember(player, message, navReply);
                                       this.brain.observe(message);
                                    } else {
                                       String answerReply = this.knocker.getQuestions().onMessage(player, message);
                                       if (answerReply != null) {
                                          this.say(player, answerReply);
                                          this.remember(player, message, answerReply);
                                       } else {
                                          KnockerMessages.Script script = KnockerMessages.findScript(this.knocker.getPhase(), message);
                                          if (script != null && KnockerConfig.useExternalAI() && script.action() == KnockerMessages.Action.NONE && script.reply().replaceAll("[^\\p{L}]", "").length() < 4) {
                                             script = null;
                                          }

                                          if (script != null) {
                                             String reply = script.reply();
                                             if (script.action() == KnockerMessages.Action.HELP) {
                                                if (!this.knocker.getGuard().help(player)) {
                                                   reply = KnockerMessages.helpNobody(this.knocker.getPhase());
                                                }
                                             } else {
                                                this.doAction(script.action(), player);
                                             }

                                             this.say(player, reply);
                                             this.remember(player, message, reply);
                                             this.brain.observe(message);
                                          } else {
                                             int act = KnockerMessages.containsAny(normNow, "обними меня", "обними", "обнимашки", "давай обнимемся") ? 0 : (KnockerMessages.containsAny(normNow, "возьми меня за руку", "возьми за руку", "дай руку", "держи меня за руку") ? 1 : (KnockerMessages.containsAny(normNow, "давай посидим", "посиди со мной", "сядь рядом", "присядь рядом") ? 2 : (KnockerMessages.containsAny(normNow, "потанцуем", "давай танцевать", "станцуй", "потанцуй со мной") ? 3 : (KnockerMessages.containsAny(normNow, "поцелуй меня", "поцелуй в щеку", "поцелуй в щёку", "чмок") ? 4 : -1))));
                                             if (act >= 0) {
                                                this.remember(player, message, "(отвечает действием)");
                                                this.knocker.getActions().perform(player, act);
                                             } else {
                                                String topicReply = KnockerTopics.reply(this.knocker, player, normNow, KnockerConfig.useExternalAI());
                                                if (topicReply != null) {
                                                   this.sayLater(player, topicReply, 20 + Math.min(40, topicReply.length()));
                                                   this.remember(player, message, topicReply);
                                                   this.brain.observe(message);
                                                } else if (!KnockerConfig.useExternalAI()) {
                                                   String reply = this.brain.reply(player, this.knocker.getPhase(), message);
                                                   this.pendingRude = KnockerBrain.isRudeText(message);
                                                   this.applyBrainAction(player);
                                                   this.remember(player, message, reply);
                                                   this.sayLater(player, reply, 20 + Math.min(40, reply.length()));
                                                } else if (this.waitingForAI) {
                                                   this.queuedMessage = message;
                                                } else {
                                                   MinecraftServer server = player.method_5682();
                                                   if (server != null) {
                                                      this.waitingForAI = true;
                                                      player.method_7353(class_2561.method_43470("§8Кнокер печатает..."), true);
                                                      List<String[]> snapshot = new ArrayList();

                                                      for(String[] h : KnockerMemoryStore.recent(player.method_5667(), 12)) {
                                                         if (!h[0].equals("assistant") || KnockerAI.tidy(h[1]) != null && !h[1].contains("—")) {
                                                            snapshot.add(h);
                                                         }
                                                      }

                                                      KnockerAI.ask(this.knocker, player, message, snapshot).whenComplete((replyx, error) -> server.execute(() -> this.onAiResult(player, message, replyx, error)));
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
               }
            }
         }
      }
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

   private void onAiResult(class_1657 player, String message, String reply, Throwable error) {
      this.waitingForAI = false;
      if (!this.knocker.method_31481() && !player.method_31481()) {
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
            if (text != null && isParrot(text, message)) {
               text = null;
            }

            if (text != null) {
               text = dropEchoStart(text, message);
            }

            if (text != null && this.brain.saidRecently(text)) {
               text = null;
            }

            if (text != null) {
               text = this.fixNameMixup(player, text);
            }

            if (text == null) {
               AiKnocker.LOGGER.info("Ответ ИИ не подошёл (обрывок/повтор/не по-русски), отвечает свой мозг: {}", parsed[0]);
               String fallback = KnockerTopics.reply(this.knocker, player, KnockerMessages.normalize(message), false);
               if (fallback == null) {
                  fallback = this.brain.reply(player, this.knocker.getPhase(), message);
                  this.applyBrainAction(player);
               }

               this.remember(player, message, fallback);
               this.say(player, fallback);
            } else {
               if (!text.isBlank()) {
                  this.say(player, text);
                  this.remember(player, message, text);
               }

               this.doAiAction(parsed[1], player);
            }
         } else {
            Throwable cause = error instanceof CompletionException && error.getCause() != null ? error.getCause() : error;
            String why = cause == null ? "пустой ответ" : cause.getMessage();
            AiKnocker.LOGGER.warn("Внешний ИИ не ответил: {}", why, cause);
            if (KnockerConfig.debug) {
               player.method_7353(class_2561.method_43470("§8[AI Knocker] Внешний ИИ: " + why), false);
            }

            String fallback = this.brain.reply(player, this.knocker.getPhase(), message);
            this.applyBrainAction(player);
            this.remember(player, message, fallback);
            this.say(player, fallback);
         }
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

   private String handleHelp(class_1657 player) {
      KnockerPhase phase = this.knocker.getPhase();
      byte var10000;
      switch (phase) {
         case WATCHER -> var10000 = 30;
         case CURIOUS -> var10000 = 60;
         default -> var10000 = 100;
      }

      int chance = var10000;
      if (this.random.nextInt(100) < chance) {
         if (!this.knocker.getGuard().help(player)) {
            return KnockerMessages.helpNobody(phase);
         }

         if (phase == KnockerPhase.WATCHER) {
            return "...";
         }
      } else if (phase.ordinal() <= KnockerPhase.CURIOUS.ordinal()) {
         return phase == KnockerPhase.WATCHER ? "..." : "я... не могу...";
      }

      return this.brain.pick(HELP_LINES[phase.ordinal()]);
   }

   private static String dropEchoStart(String reply, String message) {
      Matcher m = Pattern.compile("^([\\p{L}]+)(\\.{2,}|…|\\?|,)\\s+(.+)$").matcher(reply.trim());
      if (!m.matches()) {
         return reply;
      } else {
         String first = KnockerMessages.normalize(m.group(1)).trim();
         if (!(" " + KnockerMessages.normalize(message) + " ").contains(" " + first + " ")) {
            return reply;
         } else {
            String rest = m.group(3);
            return rest.split(" ").length >= 2 ? Character.toUpperCase(rest.charAt(0)) + rest.substring(1) : reply;
         }
      }
   }

   private static boolean isParrot(String reply, String message) {
      Set<String> hers = new HashSet(Arrays.asList(KnockerMessages.normalize(message).trim().split(" ")));
      String[] mine = KnockerMessages.normalize(reply).trim().split(" ");
      if (mine.length > 5) {
         return false;
      } else {
         for(String w : mine) {
            if (!w.isEmpty() && !hers.contains(w)) {
               return false;
            }
         }

         return true;
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

   private void doAiAction(String action, class_1657 player) {
      if (action != null) {
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
            this.knocker.getItems().aiGift(player);
         } else if (action.startsWith("защит")) {
            this.knocker.getGuard().help(player);
         } else if (action.startsWith("смех")) {
            this.knocker.playAction("laugh");
         } else if (action.startsWith("смущ")) {
            this.knocker.playAction("shy");
         }

      }
   }

   private void doAction(KnockerMessages.Action action, class_1657 player) {
      switch (action) {
         case TELEPORT_BEHIND:
            this.movement.teleportBehind(player);
            break;
         case FLASH_BEHIND:
            this.movement.flashBehind(player, 30);
            break;
         case APPEAR_IN_FRONT:
            this.movement.appearInFront(player);
            break;
         case BREATH:
            this.knocker.playSoundFor(player, ModSounds.BREATH, 0.8F, 1.0F);
         case NONE:
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
      if (player != null) {
         String recall = this.random.nextInt(3) == 0 ? this.knocker.getQuestions().recall(player) : null;
         if (recall != null && !this.brain.saidRecently(recall)) {
            this.say(player, recall);
         } else {
            int ph = this.knocker.getPhase().ordinal();
            if (ph >= KnockerPhase.ATTACHED.ordinal() && ph < FLIRT.length && FLIRT[ph] != null && this.random.nextInt(100) < FLIRT_CHANCE[ph]) {
               String f = this.brain.pick(FLIRT[ph]);
               if (!this.brain.saidRecently(f)) {
                  this.say(player, f);
                  return;
               }
            }

            String[] phrases = KnockerMessages.getRandomPhrases(this.knocker.getPhase().ordinal());
            this.say(player, this.brain.pick(phrases));
         }
      }
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
