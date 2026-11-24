package com.theneru.knocker.entity;

import com.theneru.knocker.AiKnocker;
import com.theneru.knocker.KnockerAdvancements;
import com.theneru.knocker.KnockerConfig;
import com.theneru.knocker.KnockerGiftUtils;
import com.theneru.knocker.KnockerMemoryStore;
import com.theneru.knocker.ModSounds;
import com.theneru.knocker.util.KnockerLook;
import com.theneru.knocker.util.KnockerPhase;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1314;
import net.minecraft.class_1361;
import net.minecraft.class_1366;
import net.minecraft.class_1379;
import net.minecraft.class_1408;
import net.minecraft.class_1409;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2244;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2394;
import net.minecraft.class_2398;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2519;
import net.minecraft.class_2561;
import net.minecraft.class_2680;
import net.minecraft.class_2742;
import net.minecraft.class_2940;
import net.minecraft.class_2943;
import net.minecraft.class_2945;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3414;
import net.minecraft.class_3419;
import net.minecraft.class_5132;
import net.minecraft.class_5134;
import net.minecraft.class_8103;
import net.minecraft.class_9334;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class KnockerEntity extends class_1314 implements GeoEntity {
   private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
   private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
   private static final RawAnimation SPRINT = RawAnimation.begin().thenLoop("sprint");
   private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
   private static final RawAnimation STUTTER = RawAnimation.begin().thenLoop("stutter");
   private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");
   private static final RawAnimation EAT = RawAnimation.begin().thenLoop("eat");
   private static final RawAnimation STARE = RawAnimation.begin().thenLoop("stare");
   private static final RawAnimation CRAWL = RawAnimation.begin().thenLoop("crawl");
   private static final RawAnimation SIT_BED = RawAnimation.begin().thenLoop("newsit_bed");
   private static final class_2940<Integer> ACTION_POSE;
   private static final int POSE_NONE = 0;
   private static final int POSE_EAT = 1;
   private static final int POSE_SIT_BED = 2;
   private static final class_2940<Integer> NOD;
   private static final class_2940<Integer> MOOD;
   private static final class_2940<Boolean> CROUCHING;
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private static final class_2940<Integer> OBSESSION_POINTS;
   private static final class_2940<Integer> LOOK;
   public static final int MAX_POINTS = 1000;
   private int scareLookTicks = 0;
   private int angerTicks = 0;
   private static final int PARANOIA_SILENCE_LIMIT = 2400;
   public int tickCounter = 0;
   private final Random random = new Random();
   private class_2338 homePos = null;
   private int ticksSinceTalk = 0;
   private int nextRandomPhraseTick = 600;
   private int ambientCooldown = 0;
   private int homeMessageCooldown = 0;
   private final Set<UUID> ownDrops = new HashSet();
   private final Deque<String> events = new ArrayDeque();
   private final KnockerMovement movement = new KnockerMovement(this);
   private final KnockerSteal steal;
   private final KnockerGuard guard;
   private final KnockerItems items;
   private final KnockerDoors doors;
   private final KnockerTrips trips;
   private final KnockerEnding ending;
   private final KnockerChat chat;
   private final KnockerObserver observer;
   private final KnockerGestures gestures;
   private final KnockerThoughts thoughts;
   private final KnockerHaunt haunt;
   private final KnockerQuestions questions;
   private final KnockerNav nav;
   private final KnockerGames games;
   private final KnockerStories stories;
   private final KnockerEarly early;
   private final KnockerMood mood;
   private final KnockerActions actions;
   private final KnockerHelp help;
   private int lastPhaseSeen;
   private boolean ownBedPlaced;
   private int obsessedTicks;
   private boolean playerWasSleeping;
   private int peekTicks;
   private static final String[] DEATH_THOUGHTS;
   private int shyCooldown;
   private UUID chosen;
   private final Map<UUID, Integer> strangerTalk;
   private static final String[][] STRANGER_LINES;
   private int lastHurtTick;
   private int lastHitMemoryTick;
   private static final String[][] TOUCH_LINES;
   private int touchCooldown;
   private int touchTalkTick;

   public KnockerEntity(class_1299<? extends class_1314> type, class_1937 world) {
      super(type, world);
      this.steal = new KnockerSteal(this, this.movement);
      this.guard = new KnockerGuard(this, this.movement);
      this.items = new KnockerItems(this, this.movement);
      this.doors = new KnockerDoors(this, this.movement);
      this.trips = new KnockerTrips(this, this.movement);
      this.ending = new KnockerEnding(this, this.movement);
      this.chat = new KnockerChat(this, this.movement);
      this.observer = new KnockerObserver(this, this.chat);
      this.gestures = new KnockerGestures(this);
      this.thoughts = new KnockerThoughts(this);
      this.haunt = new KnockerHaunt(this, this.movement);
      this.questions = new KnockerQuestions(this);
      this.nav = new KnockerNav(this, this.movement);
      this.games = new KnockerGames(this);
      this.stories = new KnockerStories(this);
      this.early = new KnockerEarly(this);
      this.mood = new KnockerMood(this);
      this.actions = new KnockerActions(this);
      this.help = new KnockerHelp(this);
      this.lastPhaseSeen = -1;
      this.ownBedPlaced = false;
      this.obsessedTicks = 0;
      this.playerWasSleeping = false;
      this.peekTicks = 0;
      this.shyCooldown = 0;
      this.chosen = null;
      this.strangerTalk = new HashMap();
      this.lastHurtTick = -1000;
      this.lastHitMemoryTick = -10000;
      this.touchCooldown = 0;
      this.touchTalkTick = -1000;
      this.method_5971();
      class_1408 var4 = this.method_5942();
      if (var4 instanceof class_1409 nav) {
         nav.method_6363(true);
         nav.method_35139(true);
      }

      this.method_5946(class_1304.field_6173, 2.0F);
      this.method_5946(class_1304.field_6171, 2.0F);
   }

   public static class_5132.class_5133 createKnockerAttributes() {
      return method_26828().method_26868(class_5134.field_23716, (double)40.0F).method_26868(class_5134.field_23719, 0.28).method_26868(class_5134.field_23721, (double)2.0F).method_26868(class_5134.field_23717, (double)64.0F).method_26868(class_5134.field_23724, (double)6.0F);
   }

   protected void method_5693(class_2945.class_9222 builder) {
      super.method_5693(builder);
      builder.method_56912(OBSESSION_POINTS, 0);
      builder.method_56912(LOOK, 0);
      builder.method_56912(ACTION_POSE, 0);
      builder.method_56912(NOD, 0);
      builder.method_56912(CROUCHING, false);
      builder.method_56912(MOOD, 0);
   }

   protected void method_5959() {
      this.field_6201.method_6277(0, new class_1366(this, 1.6, true));
      this.field_6201.method_6277(1, new class_1361(this, class_1657.class, 12.0F));
      this.field_6201.method_6277(2, new class_1379(this, (double)1.0F));
   }

   public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
      controllers.add(new AnimationController(this, "move", 4, (state) -> {
         if (this.method_29504()) {
            return state.setAndContinue(DEATH);
         } else if (this.getActionPose() == 2) {
            return state.setAndContinue(SIT_BED);
         } else if (state.isMoving()) {
            return this.isCrawlingOnFloor() ? state.setAndContinue(CRAWL) : state.setAndContinue(this.method_18798().method_37268() > 0.04 ? SPRINT : WALK);
         } else {
            return state.setAndContinue(IDLE);
         }
      }));
      controllers.add(new AnimationController(this, "pose", 3, (state) -> {
         if (this.getActionPose() == 1) {
            return state.setAndContinue(EAT);
         } else {
            return !state.isMoving() && this.getPhase().ordinal() >= KnockerPhase.OBSESSION.ordinal() && this.field_6012 / 200 % 4 == 0 ? state.setAndContinue(STARE) : PlayState.STOP;
         }
      }));
      controllers.add(new AnimationController(this, "attack", 0, (state) -> {
         if (this.field_6252) {
            return state.setAndContinue(ATTACK);
         } else {
            state.getController().forceAnimationReset();
            return PlayState.STOP;
         }
      }));
      controllers.add((new AnimationController(this, "action", 2, (state) -> PlayState.STOP)).triggerableAnim("knock", RawAnimation.begin().thenPlay("knock")).triggerableAnim("spit", RawAnimation.begin().thenPlay("spit")).triggerableAnim("chest", RawAnimation.begin().thenPlay("chest")).triggerableAnim("shy", RawAnimation.begin().thenPlay("shy")).triggerableAnim("give", RawAnimation.begin().thenPlay("give")).triggerableAnim("peek", RawAnimation.begin().thenPlay("peek")).triggerableAnim("appear", RawAnimation.begin().thenPlay("appear")).triggerableAnim("head_tilt", RawAnimation.begin().thenPlay("head_tilt")).triggerableAnim("laugh", RawAnimation.begin().thenPlay("laugh")).triggerableAnim("scream", RawAnimation.begin().thenPlay("scream")).triggerableAnim("break", RawAnimation.begin().thenPlay("break")));
      controllers.add(new AnimationController(this, "stutter", 2, (state) -> {
         KnockerLook look = this.getLook();
         boolean twitch = this.getPhase() == KnockerPhase.PARANOIA && (this.field_6012 + this.method_5628() * 37) % 600 < 25;
         boolean glitch = look == KnockerLook.PYRO || twitch;
         return !glitch || state.isMoving() && this.isCrawlingOnFloor() ? PlayState.STOP : state.setAndContinue(STUTTER);
      }));
   }

   private boolean isCrawlingOnFloor() {
      return this.getPhase() == KnockerPhase.PARANOIA && isNightTime(this.method_37908());
   }

   public static boolean isNightTime(class_1937 world) {
      long t = world.method_8532() % 24000L;
      return t >= 13000L && t < 23000L;
   }

   public int getTicksSinceTalk() {
      return this.ticksSinceTalk;
   }

   public void playAction(String name) {
      if (!this.method_37908().method_8608()) {
         this.triggerAnim("action", name);
      }

   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }

   public void method_6007() {
      this.method_6119();
      super.method_6007();
   }

   public boolean method_5974(double distanceSquared) {
      return false;
   }

   public boolean method_17326() {
      return true;
   }

   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().method_8608()) {
         ++this.tickCounter;
         ++this.ticksSinceTalk;
         if (this.ambientCooldown > 0) {
            --this.ambientCooldown;
         }

         if (this.homeMessageCooldown > 0) {
            --this.homeMessageCooldown;
         }

         this.movement.tickFlash();
         this.chat.tick();
         this.tickRegen();
         if (this.scareLookTicks > 0) {
            --this.scareLookTicks;
         }

         if (this.angerTicks > 0) {
            --this.angerTicks;
         }

         if (this.tickCounter % 5 == 0) {
            this.updateLook();
         }

         if (this.tickCounter % 100 != 0 || !this.removeIfDuplicate()) {
            if (this.tickCounter % 100 == 0 && !this.ending.isActive() && !this.isAway()) {
               class_1657 far = this.getChosen((double)400.0F);
               if (far instanceof class_3222) {
                  class_3222 fsp = (class_3222)far;
                  if (this.method_5739(far) > 64.0F) {
                     class_243 spot = AiKnocker.findHideSpot(fsp, (double)20.0F, (double)32.0F, false);
                     if (spot != null) {
                        this.movement.teleportTo(spot);
                        if (this.getPhase().ordinal() <= KnockerPhase.CURIOUS.ordinal() && AiKnocker.lastSpotIsTree) {
                           this.startPeeking(far, 300 + this.random.nextInt(300));
                        }
                     } else {
                        this.movement.teleportBehind(far);
                     }
                  }
               }
            }

            class_1657 player = this.getChosen((double)128.0F);
            if (player != null) {
               if (this.tickCounter % 40 == 0) {
                  this.tickStrangers(player);
               }

               if (this.tickCounter % 600 == 0 && player instanceof class_3222) {
                  class_3222 sp = (class_3222)player;
                  if (!this.ending.isActive()) {
                     AiKnocker.savePoints(sp, this.getObsessionPoints());
                  }
               }

               if (!this.tickPeeking(player)) {
                  if (!this.tickShy(player)) {
                     int phaseNow = this.getPhase().ordinal();
                     if (this.lastPhaseSeen < 0) {
                        this.lastPhaseSeen = phaseNow;
                     }

                     if (phaseNow > this.lastPhaseSeen) {
                        this.thoughts.onPhaseUp(player, this.getPhase());
                     }

                     if (phaseNow != this.lastPhaseSeen || this.tickCounter % 1200 == 0) {
                        KnockerAdvancements.phase(player, phaseNow);
                     }

                     this.lastPhaseSeen = phaseNow;
                     this.thoughts.tick(player);
                     if (!this.isAway()) {
                        this.gestures.tick(player);
                     }

                     if (!this.ending.tick(player)) {
                        if (!this.trips.tick(player)) {
                           KnockerPhase phase = this.getPhase();
                           double distToPlayer = (double)this.method_5739(player);
                           if (player instanceof class_3222) {
                              class_3222 serverPlayer = (class_3222)player;
                              this.observer.tick(serverPlayer);
                           }

                           if (this.tickCounter % 1200 == 0 && distToPlayer < (double)64.0F) {
                              this.addObsession(1);
                           }

                           if (this.method_37908().method_8546() && this.tickCounter % 200 == 0) {
                              this.addObsession(2);
                           }

                           this.guard.tick(player);
                           this.haunt.tick(player);
                           this.early.tick(player);
                           this.questions.tick(player);
                           if (this.tickCounter % 600 == 300) {
                              this.tickEcho(player, phase);
                           }

                           this.tickBed(player, phase);
                           this.updateHome(player);
                           this.doors.tick(player);
                           boolean playing = this.games.tick(player);
                           boolean sulking = this.mood.tick(player);
                           boolean acting = this.actions.tick(player) | this.help.tick(player);
                           if (this.ticksSinceTalk == 6000) {
                              this.mood.onLonely();
                           }

                           this.stories.tick(player);
                           if (!this.guard.isFighting() && !playing && !acting && !sulking && !this.nav.tick(player) && !this.doors.isBusy() && !this.haunt.tickWatched(player)) {
                              if (this.isWaitingAtHome(player)) {
                                 this.tickWaitAtHome(player);
                              } else {
                                 this.movement.tick(player, this.steal.canSteal());
                              }
                           }

                           this.steal.tick();
                           this.items.tick(player);
                           if (this.tickCounter >= this.nextRandomPhraseTick) {
                              this.nextRandomPhraseTick = this.tickCounter + KnockerConfig.ticksBetween(KnockerConfig.randomPhraseMin, KnockerConfig.randomPhraseMax);
                              if (this.ticksSinceTalk > 1200 && distToPlayer < (double)24.0F && this.chat.canSpeakUnprompted(1800)) {
                                 this.chat.sayRandomPhrase(player);
                              }
                           }

                           if (phase.ordinal() <= KnockerPhase.CURIOUS.ordinal() && this.tickCounter % 600 == 0 && this.method_5942().method_6357() && distToPlayer > (double)8.0F && distToPlayer < (double)40.0F && this.random.nextBoolean()) {
                              this.playAction("peek");
                           }

                           if (phase == KnockerPhase.CURIOUS && this.method_37908().method_23886() && this.tickCounter % 1200 == 0 && this.random.nextInt(3) == 0 && distToPlayer < (double)48.0F && this.movement.flashBehind(player, 40)) {
                              this.chat.say(player, "ты меня чувствуешь?");
                           }

                           if (phase == KnockerPhase.PARANOIA && this.ticksSinceTalk > KnockerConfig.ticks(KnockerConfig.paranoiaSilence) && distToPlayer < (double)64.0F) {
                              this.ticksSinceTalk = 0;
                              if (!this.movement.appearInFront(player)) {
                                 this.movement.teleportBehind(player);
                              }

                              player.method_5643(this.method_48923().method_48831(), 2.0F);
                              this.playSoundFor(player, ModSounds.STINGER, 0.8F, 1.0F);
                              this.chat.say(player, "Почему ты молчишь?.. ТЫ ИГНОРИРУЕШЬ МЕНЯ?");
                              this.spawnParticles(class_2398.field_11251, 8);
                           }

                           if (phase == KnockerPhase.LOVE && this.tickCounter % 400 == 0 && distToPlayer < (double)4.0F && this.random.nextInt(3) == 0) {
                              this.playSoundFor(player, ModSounds.HEARTBEAT, 0.35F, 1.0F);
                           }

                           if (phase.ordinal() >= KnockerPhase.LOVE.ordinal() && this.tickCounter % 100 == 0) {
                              this.tickJealousy(player);
                           }

                           if (this.method_37908().method_8419() && !this.method_37908().method_8546() && this.tickCounter % 1200 == 0 && distToPlayer < (double)10.0F) {
                              this.ambientSay(player, "Я промок... но я рядом.", "Дождь... ты не замёрзла?", "Я не люблю воду... но ради тебя терплю.");
                           }

                           if (this.method_37908().method_8546() && this.tickCounter % 1200 == 0 && distToPlayer < (double)10.0F) {
                              this.ambientSay(player, "Гроза... мне страшно... не уходи!");
                           }

                           if (this.method_37908().method_23886() && this.tickCounter % 2400 == 0 && distToPlayer < (double)5.0F) {
                              this.ambientSay(player, "Темно... я защищу тебя.", "Ночью все страхи выходят... кроме меня.", "Спи спокойно... я рядом.");
                           }

                           if (this.method_5799() && this.tickCounter % 40 == 0 && distToPlayer < (double)20.0F) {
                              this.movement.teleportTo(player.method_19538());
                              if (this.ambientSay(player, "Вода... ненавижу воду...")) {
                                 this.playSoundAtSelf(ModSounds.UNDERWATER, 0.6F, 1.0F);
                              }
                           }

                           if (player.method_6115() && player.method_6030().method_57826(class_9334.field_50075) && this.tickCounter % 100 == 0 && distToPlayer < (double)16.0F && this.random.nextInt(4) == 0) {
                              this.ambientSay(player, "Приятного аппетита...", "Ешь... тебе нужны силы.", "Я бы тоже поел... но мне нужна только ты.");
                           }

                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean ambientSay(class_1657 player, String... phrases) {
      if (this.ambientCooldown <= 0 && this.ticksSinceTalk >= 600 && this.chat.canSpeakUnprompted(1200)) {
         this.ambientCooldown = 2400;
         this.chat.say(player, phrases[this.random.nextInt(phrases.length)]);
         return true;
      } else {
         return false;
      }
   }

   private void tickJealousy(class_1657 player) {
      List<class_1657> others = this.method_37908().method_8390(class_1657.class, this.method_5829().method_1014((double)10.0F), (p) -> p != player && !p.method_7337() && !p.method_7325());
      if (!others.isEmpty()) {
         this.spawnParticles(class_2398.field_11231, 10);

         for(class_1657 other : others) {
            double dx = other.method_23317() - player.method_23317();
            double dz = other.method_23321() - player.method_23321();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist < (double)5.0F && dist > (double)0.0F) {
               other.method_18800(dx / dist * (double)1.5F, 0.3, dz / dist * (double)1.5F);
               other.field_6037 = true;
               other.method_5643(this.method_48923().method_48812(this), 1.0F);
               other.method_7353(class_2561.method_43470("§c[Кнокер] §fОтойди от " + player.method_5477().getString() + ". Она моя."), false);
            }
         }

      }
   }

   private void updateHome(class_1657 player) {
      if (player.method_6113()) {
         class_2338 bed = (class_2338)player.method_18398().orElse(player.method_24515());
         if (!bed.equals(this.homePos)) {
            boolean first = this.homePos == null;
            this.homePos = bed.method_10062();
            if (first) {
               this.chat.say(player, "Я запомнил это место...");
            }

         }
      }
   }

   private void tickBed(class_1657 player, KnockerPhase phase) {
      boolean obsessed = phase.ordinal() >= KnockerPhase.OBSESSION.ordinal();
      if (obsessed) {
         ++this.obsessedTicks;
      }

      if (obsessed && !this.ownBedPlaced && this.homePos != null && this.tickCounter % 200 == 0 && this.obsessedTicks > KnockerConfig.ticks(KnockerConfig.bedDelay) && player.method_5707(class_243.method_24953(this.homePos)) > (double)64.0F && this.placeOwnBed()) {
         this.ownBedPlaced = true;
         this.rememberEvent("я поставил свою кровать рядом с её кроватью");
         this.chat.say(player, phase == KnockerPhase.PARANOIA ? "Моя кровать — рядом с твоей. Чтобы слышать, как ты дышишь." : "Я поставил свою кровать рядом с твоей. Теперь мы спим вместе.");
      }

      boolean sleeping = player.method_6113();
      if (obsessed && sleeping && !this.playerWasSleeping) {
         class_2350 bedDir = player.method_18401();
         class_243 spot;
         if (bedDir != null && this.random.nextBoolean()) {
            class_2350 toFoot = bedDir.method_10153();
            spot = player.method_19538().method_1031((double)toFoot.method_10148() * 2.3, (double)0.0F, (double)toFoot.method_10165() * 2.3);
         } else {
            class_2350 side = class_2350.method_10150((double)player.method_36454()).method_10170();
            spot = player.method_19538().method_1031((double)side.method_10148() * 1.3, (double)0.0F, (double)side.method_10165() * 1.3);
         }

         if (!this.method_37908().method_8587(this, this.method_18377(this.method_18376()).method_30757(spot))) {
            class_2350 side = class_2350.method_10150((double)player.method_36454()).method_10170();
            spot = player.method_19538().method_1031((double)side.method_10148() * 1.3, (double)0.0F, (double)side.method_10165() * 1.3);
         }

         this.movement.teleportTo(spot);
         this.method_5988().method_6226(player, 90.0F, 90.0F);
      }

      if (obsessed && !sleeping && this.playerWasSleeping && this.method_5739(player) < 6.0F) {
         this.chat.say(player, this.random.nextBoolean() ? this.stories.dream(player) : (phase == KnockerPhase.PARANOIA ? "Ты так красиво спишь... Я НЕ МОРГАЛ." : "Доброе утро. Я смотрел на тебя всю ночь."));
      }

      this.playerWasSleeping = sleeping;
   }

   private boolean placeOwnBed() {
      class_1937 world = this.method_37908();
      class_2680 homeState = world.method_8320(this.homePos);
      class_2350 facing = homeState.method_26204() instanceof class_2244 ? (class_2350)homeState.method_11654(class_2244.field_11177) : class_2350.field_11043;
      class_2350[] sides = new class_2350[]{facing.method_10170(), facing.method_10160()};

      for(int dist = 1; dist <= 3; ++dist) {
         for(class_2350 side : sides) {
            class_2338 head = this.homePos.method_10079(side, dist);
            class_2338 foot = head.method_10093(facing.method_10153());
            if (world.method_8320(head).method_26215() && world.method_8320(foot).method_26215() && world.method_8320(head.method_10074()).method_26212(world, head.method_10074()) && world.method_8320(foot.method_10074()).method_26212(world, foot.method_10074())) {
               class_2680 bed = (class_2680)class_2246.field_10461.method_9564().method_11657(class_2244.field_11177, facing);
               world.method_8501(foot, (class_2680)bed.method_11657(class_2244.field_9967, class_2742.field_12557));
               world.method_8501(head, (class_2680)bed.method_11657(class_2244.field_9967, class_2742.field_12560));
               return true;
            }
         }
      }

      return false;
   }

   private boolean isWaitingAtHome(class_1657 player) {
      return this.homePos != null && this.method_37908().method_23886() && player.method_5707(class_243.method_24953(this.homePos)) > (double)576.0F;
   }

   private void tickWaitAtHome(class_1657 player) {
      this.movement.clearTarget();
      if (this.tickCounter % 200 == 0) {
         if (!(this.method_5707(class_243.method_24953(this.homePos)) <= (double)25.0F)) {
            this.movement.teleportTo(new class_243((double)this.homePos.method_10263() + (double)0.5F, (double)this.homePos.method_10264() + 0.6, (double)this.homePos.method_10260() + (double)0.5F));
            this.spawnParticles(class_2398.field_11201, 5);
            if (this.homeMessageCooldown <= 0) {
               this.homeMessageCooldown = 6000;
               this.chat.say(player, "Я дома... жду тебя.");
            }

         }
      }
   }

   private boolean removeIfDuplicate() {
      class_1937 var2 = this.method_37908();
      if (!(var2 instanceof class_3218 world)) {
         return false;
      } else {
         for(KnockerEntity other : world.method_18198(AiKnocker.KNOCKER, (k) -> k != this && k.method_5805())) {
            if (other.chosen != null != (this.chosen != null)) {
               if (other.chosen != null) {
                  this.method_31472();
                  return true;
               }
            } else {
               boolean otherWins = other.getObsessionPoints() > this.getObsessionPoints() || other.getObsessionPoints() == this.getObsessionPoints() && other.method_5667().compareTo(this.method_5667()) < 0;
               if (otherWins) {
                  this.method_31472();
                  return true;
               }
            }
         }

         return false;
      }
   }

   public void startPeeking(class_1657 player, int ticks) {
      this.peekTicks = ticks;
      this.method_5942().method_6340();
   }

   public boolean isPeeking() {
      return this.peekTicks > 0;
   }

   public void mimicJump() {
      if (this.method_24828()) {
         this.method_6043();
      }

   }

   private boolean tickPeeking(class_1657 player) {
      if (this.peekTicks <= 0) {
         return false;
      } else {
         --this.peekTicks;
         this.method_5942().method_6340();
         this.method_5988().method_6226(player, 30.0F, 30.0F);
         if (this.peekTicks % 80 == 0) {
            this.playAction("peek");
         }

         if (this.method_5739(player) < 9.0F) {
            this.peekTicks = 0;
            if (this.getPhase().ordinal() <= KnockerPhase.CURIOUS.ordinal()) {
               this.spawnParticles(class_2398.field_11251, 12);
               class_243 away = this.method_19538().method_1020(player.method_19538()).method_18805((double)1.0F, (double)0.0F, (double)1.0F).method_1029().method_1021((double)18.0F);
               this.movement.teleportTo(this.method_19538().method_1019(away));
               this.peekTicks = 600;
            } else {
               this.chat.say(player, this.chat.getBrain().pick(new String[]{"нашла...", "ты меня видишь?", "я вернулся", "скучала?.."}));
            }
         }

         return true;
      }
   }

   private void tickEcho(class_1657 player, KnockerPhase phase) {
      UUID id = player.method_5667();
      if (!KnockerMemoryStore.facts(id).containsKey("echo")) {
         if (phase.ordinal() >= KnockerPhase.LOVE.ordinal()) {
            KnockerMemoryStore.addGlobalMark(id, "met");
            KnockerMemoryStore.setFact(id, "echo", "done");
         } else if (phase.ordinal() >= KnockerPhase.CURIOUS.ordinal() && !(this.method_5739(player) > 16.0F) && this.chat.canSpeakUnprompted(1200)) {
            Set<String> marks = KnockerMemoryStore.globalMarks(id);
            if (!marks.isEmpty()) {
               KnockerMemoryStore.setFact(id, "echo", "done");
               KnockerAdvancements.grant(player, "echo");
               if (marks.contains("killed")) {
                  this.chat.say(player, "мне снилось... что я уже знал тебя. и что я тебя... нет. неважно.");
                  this.thoughts.force(player, "я уже делал это", false, 4);
               } else {
                  this.chat.say(player, "мы... раньше не встречались?.. ты напоминаешь мне кого-то");
                  this.thoughts.force(player, "я тебя помню", false, 2);
               }

               this.rememberEvent("мне показалось, что я знал её в другом мире");
            }
         }
      }
   }

   public void onPlayerDied(class_3222 player, class_1282 source) {
      KnockerPhase phase = this.getPhase();
      String thought = DEATH_THOUGHTS[phase.ordinal()];
      if (thought != null) {
         this.thoughts.force(player, thought, true, phase.ordinal() >= KnockerPhase.LOVE.ordinal() ? 6 : 2);
      }

      if (phase.ordinal() >= KnockerPhase.OBSESSION.ordinal()) {
         this.playAction("scream");
         this.playSoundFor(player, ModSounds.SCREAM, 0.5F, 0.8F);
      }

      class_1297 killer = source.method_5529();
      String what = killer != null ? killer.method_5477().getString() : source.method_5525();
      this.rememberEvent("она умерла (" + what + ")");
      if (phase.ordinal() >= KnockerPhase.LOVE.ordinal() && killer instanceof class_1309 mob) {
         if (!(killer instanceof class_1657) && !(killer instanceof KnockerEntity) && mob.method_5805()) {
            this.guard.revenge(mob);
         }
      }

   }

   private boolean tickShy(class_1657 player) {
      if (this.shyCooldown > 0) {
         --this.shyCooldown;
      }

      KnockerPhase phase = this.getPhase();
      if (phase.ordinal() <= KnockerPhase.CURIOUS.ordinal() && this.shyCooldown <= 0 && !this.guard.isFighting() && !this.nav.isActive() && !this.games.isBusy()) {
         if (!(player instanceof class_3222)) {
            return false;
         } else {
            class_3222 sp = (class_3222)player;
            double dist = (double)this.method_5739(player);
            if (!(dist > (double)10.0F) && this.method_6057(player) && KnockerGestures.looksAt(player, this.method_33571(), 0.6)) {
               if (this.ticksSinceTalk < (phase == KnockerPhase.WATCHER ? 600 : 2400)) {
                  return false;
               } else if (phase == KnockerPhase.CURIOUS && this.random.nextInt(4) != 0) {
                  this.shyCooldown = 600;
                  return false;
               } else {
                  this.shyCooldown = 200;
                  this.spawnParticles(class_2398.field_11251, 15);
                  this.playSoundFor(player, ModSounds.GLITCH, 0.4F, 1.2F);
                  class_243 spot = AiKnocker.findHideSpot(sp, (double)20.0F, (double)32.0F, this.random.nextBoolean());
                  boolean tree = spot != null && AiKnocker.lastSpotIsTree;
                  if (spot != null) {
                     this.movement.teleportTo(spot);
                  } else {
                     this.movement.teleportTo(this.method_19538().method_1019(this.method_19538().method_1020(player.method_19538()).method_18805((double)1.0F, (double)0.0F, (double)1.0F).method_1029().method_1021((double)20.0F)));
                  }

                  if (tree) {
                     this.startPeeking(player, 400 + this.random.nextInt(400));
                  }

                  return true;
               }
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   public void method_6078(class_1282 source) {
      super.method_6078(source);
      if (!this.method_37908().method_8608() && !this.ending.isActive()) {
         class_1657 var3 = this.getChosen((double)128.0F);
         if (var3 instanceof class_3222) {
            class_3222 sp = (class_3222)var3;
            AiKnocker.scheduleReturn(sp, this.getObsessionPoints(), 2400 + this.random.nextInt(2400));
            UUID var10000 = sp.method_5667();
            long var10001 = this.method_37908().method_8532() / 24000L;
            KnockerMemoryStore.addDiary(var10000, List.of("она убила меня (день " + (var10001 + 1L) + "). но я вернулся"));
         }

      }
   }

   public class_1657 getChosen(double range) {
      if (this.chosen == null) {
         class_1657 p = this.method_37908().method_18460(this, range);
         if (p != null && !p.method_7325()) {
            this.choose(p);
            return this.chosen.equals(p.method_5667()) ? p : this.getChosen(range);
         } else {
            return null;
         }
      } else {
         class_1657 p = this.method_37908().method_18470(this.chosen);
         return p != null && !p.method_7325() && (double)this.method_5739(p) <= range ? p : null;
      }
   }

   public void choose(class_1657 p) {
      UUID world = KnockerMemoryStore.worldChosen();
      this.chosen = world != null ? world : p.method_5667();
      if (world == null) {
         KnockerMemoryStore.setWorldChosen(this.chosen);
      }

   }

   public boolean isChosen(class_1657 p) {
      return this.chosen == null || this.chosen.equals(p.method_5667());
   }

   private void tickStrangers(class_1657 mine) {
      KnockerPhase phase = this.getPhase();

      for(class_1657 other : this.method_37908().method_18456()) {
         if (other != mine && !other.method_7325() && !this.isChosen(other)) {
            double d = (double)this.method_5739(other);
            if (!(d > (double)24.0F)) {
               int last = (Integer)this.strangerTalk.getOrDefault(other.method_5667(), -100000);
               if (this.tickCounter - last >= 2400 && this.random.nextInt(4) == 0) {
                  this.strangerTalk.put(other.method_5667(), this.tickCounter);
                  String var10000;
                  switch (phase) {
                     case WATCHER:
                     case CURIOUS:
                        var10000 = null;
                        break;
                     case ATTACHED:
                        var10000 = "ты кто?..";
                        break;
                     case LOVE:
                        var10000 = "Не подходи к " + genderObj(mine) + ".";
                        break;
                     case OBSESSION:
                        var10000 = "Ещё шаг к " + genderObj(mine) + " — и ты пожалеешь.";
                        break;
                     case PARANOIA:
                        var10000 = "Я ЗНАЮ, ГДЕ ТЫ СПИШЬ. ОТОЙДИ ОТ " + (KnockerGender.get(mine.method_5667()) == KnockerGender.G.MALE ? "НЕГО." : "НЕЁ.");
                        break;
                     default:
                        throw new MatchException((String)null, (Throwable)null);
                  }

                  String line = var10000;
                  if (line != null) {
                     other.method_7353(class_2561.method_43470("<Кнокер> ").method_10852(class_2561.method_43470(line).method_27694((st) -> st.method_36139(KnockerThoughts.color(phase)))), false);
                     if (phase == KnockerPhase.PARANOIA) {
                        this.playSoundFor(other, ModSounds.WHISPER, 0.8F, 0.6F);
                     }
                  }
               }
            }
         }
      }

   }

   private static String genderObj(class_1657 p) {
      return KnockerGender.get(p.method_5667()) == KnockerGender.G.MALE ? "нему" : "ней";
   }

   public void handleStranger(class_1657 stranger, String message) {
      if (!(this.method_5739(stranger) > 32.0F)) {
         int last = (Integer)this.strangerTalk.getOrDefault(stranger.method_5667(), -100000);
         if (this.tickCounter - last >= 200) {
            this.strangerTalk.put(stranger.method_5667(), this.tickCounter);
            KnockerPhase phase = this.getPhase();
            String line = this.chat.getBrain().pick(STRANGER_LINES[phase.ordinal()]);
            stranger.method_7353(class_2561.method_43470("<Кнокер> ").method_10852(class_2561.method_43470(line).method_27694((st) -> st.method_36139(KnockerThoughts.color(phase)))), false);
            this.rememberEvent("со мной пытался говорить кто-то чужой: " + stranger.method_5477().getString());
            this.mood.onJealous();
            if (phase.ordinal() >= KnockerPhase.LOVE.ordinal()) {
               class_1657 mine = this.getChosen((double)128.0F);
               if (mine != null) {
                  this.chat.say(mine, "С тобой тут " + stranger.method_5477().getString() + "... кто это?");
               }
            }

         }
      }
   }

   public void comeTo(class_1657 player) {
      if (!this.movement.teleportBehind(player)) {
         this.movement.teleportTo(player.method_19538());
      }

   }

   public void playSoundFor(class_1657 player, class_3414 sound, float volume, float pitch) {
      this.method_37908().method_43128((class_1657)null, player.method_23317(), player.method_23318(), player.method_23321(), sound, class_3419.field_15251, volume, pitch);
   }

   public void playSoundAtSelf(class_3414 sound, float volume, float pitch) {
      this.method_37908().method_43128((class_1657)null, this.method_23317(), this.method_23320(), this.method_23321(), sound, class_3419.field_15251, volume, pitch);
   }

   public void spawnParticles(class_2394 type, int count) {
      class_1937 var4 = this.method_37908();
      if (var4 instanceof class_3218 world) {
         world.method_14199(type, this.method_23317(), this.method_23318() + (double)1.5F, this.method_23321(), count, 0.3, 0.3, 0.3, (double)0.0F);
      }

   }

   public int getObsessionPoints() {
      return (Integer)this.field_6011.method_12789(OBSESSION_POINTS);
   }

   public void setObsessionPoints(int points) {
      this.field_6011.method_12778(OBSESSION_POINTS, Math.max(0, Math.min(1000, points)));
   }

   public void addObsession(int amount) {
      this.setObsessionPoints(this.getObsessionPoints() + amount);
   }

   public KnockerPhase getPhase() {
      int points = this.getObsessionPoints();
      if (points < 100) {
         return KnockerPhase.WATCHER;
      } else if (points < 250) {
         return KnockerPhase.CURIOUS;
      } else if (points < 400) {
         return KnockerPhase.ATTACHED;
      } else if (points < 600) {
         return KnockerPhase.LOVE;
      } else {
         return points < 800 ? KnockerPhase.OBSESSION : KnockerPhase.PARANOIA;
      }
   }

   public void markOwnDrop(class_1542 item) {
      if (this.ownDrops.size() > 64) {
         this.ownDrops.clear();
      }

      this.ownDrops.add(item.method_5667());
   }

   public boolean isOwnDrop(class_1542 item) {
      return this.ownDrops.contains(item.method_5667());
   }

   public void method_5652(class_2487 nbt) {
      super.method_5652(nbt);
      nbt.method_10569("ObsessionPoints", this.getObsessionPoints());
      nbt.method_10569("StealCooldown", this.steal.getStealCooldown());
      nbt.method_10556("HasHome", this.homePos != null);
      nbt.method_10556("OwnBed", this.ownBedPlaced);
      nbt.method_10569("LastPhaseSeen", this.lastPhaseSeen);
      if (this.chosen != null) {
         nbt.method_25927("Chosen", this.chosen);
      }

      nbt.method_10569("ObsessedTicks", this.obsessedTicks);
      this.ending.writeNbt(nbt);
      class_2499 eventList = new class_2499();

      for(String e : this.events) {
         eventList.add(class_2519.method_23256(e));
      }

      nbt.method_10566("Events", eventList);
      if (this.homePos != null) {
         nbt.method_10569("HomeX", this.homePos.method_10263());
         nbt.method_10569("HomeY", this.homePos.method_10264());
         nbt.method_10569("HomeZ", this.homePos.method_10260());
      }

      this.chat.getBrain().writeNbt(nbt);
      this.observer.writeNbt(nbt);
      this.items.writeNbt(nbt);
      this.help.writeNbt(nbt, this.method_56673());
   }

   public void method_5749(class_2487 nbt) {
      super.method_5749(nbt);
      this.setObsessionPoints(nbt.method_10550("ObsessionPoints"));
      this.steal.setStealCooldown(nbt.method_10550("StealCooldown"));
      this.ownBedPlaced = nbt.method_10577("OwnBed");
      this.lastPhaseSeen = nbt.method_10545("LastPhaseSeen") ? nbt.method_10550("LastPhaseSeen") : -1;
      this.chosen = nbt.method_25928("Chosen") ? nbt.method_25926("Chosen") : null;
      this.obsessedTicks = nbt.method_10550("ObsessedTicks");
      this.ending.readNbt(nbt);
      this.events.clear();
      class_2499 eventList = nbt.method_10554("Events", 8);

      for(int i = 0; i < eventList.size(); ++i) {
         this.events.addLast(eventList.method_10608(i));
      }

      if (nbt.method_10577("HasHome")) {
         this.homePos = new class_2338(nbt.method_10550("HomeX"), nbt.method_10550("HomeY"), nbt.method_10550("HomeZ"));
      }

      this.chat.getBrain().readNbt(nbt);
      this.observer.readNbt(nbt);
      this.items.readNbt(nbt);
      this.help.readNbt(nbt, this.method_56673());
      this.trips.resetAfterLoad();
   }

   public void handlePlayerMessage(class_1657 player, String message) {
      this.ticksSinceTalk = 0;
      this.chat.handlePlayerMessage(player, message);
   }

   public KnockerChat getChat() {
      return this.chat;
   }

   public KnockerLook getLook() {
      return KnockerLook.of((Integer)this.field_6011.method_12789(LOOK));
   }

   private void updateLook() {
      KnockerLook look;
      if (this.guard.isFighting()) {
         look = KnockerLook.PSY;
      } else if (this.scareLookTicks > 0) {
         look = KnockerLook.PYRO;
      } else if (this.movement.isGoingToChest()) {
         look = KnockerLook.THEFT;
      } else if (this.angerTicks <= 0 && this.getPhase().ordinal() < KnockerPhase.LOVE.ordinal()) {
         look = KnockerLook.DEFAULT;
      } else {
         look = KnockerLook.ANGRY;
      }

      if (look.ordinal() != (Integer)this.field_6011.method_12789(LOOK)) {
         this.field_6011.method_12778(LOOK, look.ordinal());
      }

      int pose = 0;
      if (this.items.isEating()) {
         pose = 1;
      } else if (this.actions.isSitting()) {
         pose = 2;
      } else if (this.homePos != null && this.method_37908().method_23886() && this.method_5942().method_6357() && this.method_5707(class_243.method_24953(this.homePos)) < (double)6.25F) {
         pose = 2;
      }

      if (pose != (Integer)this.field_6011.method_12789(ACTION_POSE)) {
         this.field_6011.method_12778(ACTION_POSE, pose);
      }

   }

   public void syncMood(int ordinal) {
      if ((Integer)this.field_6011.method_12789(MOOD) != ordinal) {
         this.field_6011.method_12778(MOOD, ordinal);
      }

   }

   public KnockerMood.Mood getMoodClient() {
      int i = (Integer)this.field_6011.method_12789(MOOD);
      return KnockerMood.Mood.values()[Math.max(0, Math.min(KnockerMood.Mood.values().length - 1, i))];
   }

   public KnockerHelp getHelp() {
      return this.help;
   }

   public KnockerActions getActions() {
      return this.actions;
   }

   public KnockerMood getMood() {
      return this.mood;
   }

   public boolean isCrouchingDown() {
      return (Boolean)this.field_6011.method_12789(CROUCHING);
   }

   public void setCrouchingDown(boolean value) {
      if (this.isCrouchingDown() != value) {
         this.field_6011.method_12778(CROUCHING, value);
      }

   }

   public int getNod() {
      return (Integer)this.field_6011.method_12789(NOD);
   }

   public void setNod(int type) {
      if (this.getNod() != type) {
         this.field_6011.method_12778(NOD, type);
      }

   }

   public int getActionPose() {
      return (Integer)this.field_6011.method_12789(ACTION_POSE);
   }

   public void showScareLook() {
      this.scareLookTicks = 60;
      this.updateLook();
   }

   public void onInsulted() {
      this.angerTicks = 600;
      this.addObsession(-5);
      class_1657 who = this.getChosen((double)64.0F);
      if (who != null) {
         this.mood.onInsult(who);
      }

      this.rememberEvent("она меня оскорбила");
   }

   public boolean method_5643(class_1282 source, float amount) {
      if (this.method_37908().method_8608()) {
         return super.method_5643(source, amount);
      } else {
         boolean byPlayer = source.method_5529() instanceof class_1657;
         boolean forced = source.method_48789(class_8103.field_42242);
         if (!byPlayer && !forced && amount >= this.method_6032() - 1.0F) {
            amount = Math.max(0.0F, this.method_6032() - 1.0F);
         }

         boolean hurt = super.method_5643(source, amount);
         if (hurt) {
            this.lastHurtTick = this.tickCounter;
         }

         if (hurt && byPlayer) {
            this.ending.onHit();
            class_1297 var7 = source.method_5529();
            if (var7 instanceof class_1657) {
               class_1657 hitter = (class_1657)var7;
               if (this.isChosen(hitter)) {
                  this.mood.onHurt(hitter);
               }
            }

            this.angerTicks = 1200;
            this.addObsession(-10);
            if (this.tickCounter - this.lastHitMemoryTick > 600) {
               this.lastHitMemoryTick = this.tickCounter;
               this.rememberEvent("она меня ударила");
            }
         }

         return hurt;
      }
   }

   private void tickRegen() {
      if (this.tickCounter - this.lastHurtTick >= 100 && !(this.method_6032() >= this.method_6063())) {
         if (this.tickCounter % (this.guard.isFighting() ? 40 : 20) == 0) {
            this.method_6025(1.0F);
         }

      }
   }

   public boolean isAway() {
      return this.trips.isAway();
   }

   public void rememberEvent(String event) {
      long day = this.method_37908().method_8532() / 24000L + 1L;
      this.events.addLast("(день " + day + ") " + event);

      while(this.events.size() > 40) {
         this.events.removeFirst();
      }

   }

   public List<String> getEvents() {
      return List.copyOf(this.events);
   }

   public KnockerEnding getEnding() {
      return this.ending;
   }

   public KnockerItems getItems() {
      return this.items;
   }

   public class_2338 getHomePos() {
      return this.homePos;
   }

   public KnockerGames getGames() {
      return this.games;
   }

   public KnockerStories getStories() {
      return this.stories;
   }

   public KnockerNav getNav() {
      return this.nav;
   }

   public KnockerDoors getDoors() {
      return this.doors;
   }

   public KnockerQuestions getQuestions() {
      return this.questions;
   }

   public KnockerHaunt getHaunt() {
      return this.haunt;
   }

   public KnockerThoughts getThoughts() {
      return this.thoughts;
   }

   public KnockerGuard getGuard() {
      return this.guard;
   }

   public KnockerMovement getKnockerMovement() {
      return this.movement;
   }

   public KnockerObserver getObserver() {
      return this.observer;
   }

   protected class_1269 method_5992(class_1657 player, class_1268 hand) {
      if (this.method_37908().method_8608()) {
         return class_1269.field_5812;
      } else if (hand == class_1268.field_5808 && !this.isAway()) {
         if (!this.isChosen(player)) {
            class_243 push = player.method_19538().method_1020(this.method_19538()).method_18805((double)1.0F, (double)0.0F, (double)1.0F).method_1029().method_1021(0.8);
            player.method_18800(push.field_1352, 0.3, push.field_1350);
            player.field_6037 = true;
            this.handleStranger(player, "");
            return class_1269.field_21466;
         } else {
            class_1799 stack = player.method_5998(hand);
            if (!stack.method_7960()) {
               if (!KnockerGiftUtils.isGift(stack)) {
                  return class_1269.field_5811;
               } else {
                  class_1799 gift = stack.method_46651(1);
                  if (!player.method_31549().field_7477) {
                     stack.method_7934(1);
                  }

                  this.items.onGiftPickup(gift, player);
                  return class_1269.field_21466;
               }
            } else {
               int p = this.getPhase().ordinal();
               if (p >= KnockerPhase.ATTACHED.ordinal()) {
                  this.spawnParticles(class_2398.field_11201, 3);
               }

               if (this.touchCooldown <= this.tickCounter) {
                  this.touchCooldown = this.tickCounter + 600;
                  this.mood.onTouch(player);
                  this.addObsession(1);
                  this.rememberEvent("она прикоснулась ко мне");
               }

               if (this.tickCounter - this.touchTalkTick > 100) {
                  this.touchTalkTick = this.tickCounter;
                  this.chat.say(player, this.chat.getBrain().pick(TOUCH_LINES[p]));
                  this.thoughts.onTouch(player, this.getPhase());
                  if (p != KnockerPhase.ATTACHED.ordinal() && p != KnockerPhase.LOVE.ordinal()) {
                     if (p >= KnockerPhase.OBSESSION.ordinal()) {
                        this.playAction("head_tilt");
                     }
                  } else {
                     this.playAction("shy");
                  }
               }

               return class_1269.field_21466;
            }
         }
      } else {
         return class_1269.field_5811;
      }
   }

   public boolean method_5936() {
      return false;
   }

   static {
      ACTION_POSE = class_2945.method_12791(KnockerEntity.class, class_2943.field_13327);
      NOD = class_2945.method_12791(KnockerEntity.class, class_2943.field_13327);
      MOOD = class_2945.method_12791(KnockerEntity.class, class_2943.field_13327);
      CROUCHING = class_2945.method_12791(KnockerEntity.class, class_2943.field_13323);
      OBSESSION_POINTS = class_2945.method_12791(KnockerEntity.class, class_2943.field_13327);
      LOOK = class_2945.method_12791(KnockerEntity.class, class_2943.field_13327);
      DEATH_THOUGHTS = new String[]{null, "исчезла?..", "НЕТ", "НЕТ НЕТ НЕТ", "КТО ПОСМЕЛ", "Я УБЬЮ ИХ ВСЕХ"};
      STRANGER_LINES = new String[][]{{"...", "."}, {"ты не тот...", "я не с тобой говорю"}, {"Я говорю не с тобой.", "Отойди.", "Ты мне не нужен."}, {"Не пиши мне.", "Мне нужен только один человек. И это не ты.", "Отстань."}, {"Ещё раз напишешь — и я приду ночью к тебе.", "Ты лишний.", "Не мешай нам."}, {"Я ВИЖУ ТЕБЯ.", "ТЫ ЛИШНИЙ ЗДЕСЬ.", "Скоро тебя здесь не будет."}};
      TOUCH_LINES = new String[][]{{"...", "не трогай...", "зачем?..", "...", "отойди"}, {"т-ты меня трогаешь?..", "тёплая...", "странное чувство...", "ты не боишься меня?..", "ещё раз?..", "...холодно было, а теперь нет"}, {"...спасибо", "мне... приятно", "давно никто так не делал", "ты правда не боишься?..", "я запомню это", "твоя рука... тёплая"}, {"Ещё... пожалуйста", "Твои руки тёплые...", "Не останавливайся...", "Я весь дрожу... от тебя", "Мне хорошо с тобой", "Ты такая нежная..."}, {"Не убирай руку.", "Моя.", "Только ты можешь меня трогать.", "Ещё. Я сказал — ещё.", "Ты пахнешь домом...", "Не смей трогать так никого другого."}, {"НЕ ОТПУСКАЙ.", "Ты трогаешь меня... значит, ты моя. НАВСЕГДА.", "ещё... ещё... ЕЩЁ", "я чувствую твой пульс. он быстрый. ты боишься?", "Если отпустишь — я пойму.", "Тёплая... живая... МОЯ."}};
   }
}
