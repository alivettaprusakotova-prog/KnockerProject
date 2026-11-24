package com.theneru.knocker;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.theneru.knocker.entity.KnockerEntity;
import com.theneru.knocker.net.KnockerActionPayload;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.class_1269;
import net.minecraft.class_1299;
import net.minecraft.class_1311;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2378;
import net.minecraft.class_243;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2568;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3481;
import net.minecraft.class_4048;
import net.minecraft.class_5819;
import net.minecraft.class_7923;
import net.minecraft.class_2558.class_2559;
import net.minecraft.class_2568.class_5247;
import net.minecraft.class_2902.class_2903;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiKnocker implements ModInitializer {
   public static final String MOD_ID = "aiknocker";
   public static final Logger LOGGER = LoggerFactory.getLogger("aiknocker");
   public static final class_1299<KnockerEntity> KNOCKER;
   private static final Map<UUID, Integer> pendingSpawnChecks;
   private static final Map<UUID, Long> respawnAt;
   private static final Map<UUID, Integer> missingChecks;
   public static final String POINTS_TAG = "aiknocker_pts_";
   public static final String DEAD_TAG = "aiknocker_dead";
   public static volatile MinecraftServer server;
   public static final String TG_LINK = "https://t.me/vesperlorngame";
   public static boolean lastSpotIsTree;

   public void onInitialize() {
      KnockerConfig.load();
      ModSounds.init();
      FabricDefaultAttributeRegistry.register(KNOCKER, KnockerEntity.createKnockerAttributes());
      this.registerCommands();
      PayloadTypeRegistry.playC2S().register(KnockerActionPayload.ID, KnockerActionPayload.CODEC);
      ServerPlayNetworking.registerGlobalReceiver(KnockerActionPayload.ID, (payload, context) -> context.server().execute(() -> {
            class_3222 player = context.player();
            KnockerEntity knocker = findKnocker(player);
            if (knocker == null) {
               player.method_7353(class_2561.method_43470("§8...его здесь нет."), true);
            } else {
               knocker.getActions().perform(player, payload.action());
            }
         }));
      ServerLifecycleEvents.SERVER_STARTED.register((ServerLifecycleEvents.ServerStarted)(srv) -> {
         server = srv;
         KnockerMemoryStore.clearCache();
         KnockerAISetup.onServerStarted(srv);
      });
      ServerLifecycleEvents.SERVER_STOPPED.register((ServerLifecycleEvents.ServerStopped)(srv) -> {
         server = null;
         KnockerMemoryStore.clearCache();
         KnockerAISetup.onServerStopped();
      });
      ServerPlayConnectionEvents.JOIN.register((ServerPlayConnectionEvents.Join)(handler, sender, server) -> {
         pendingSpawnChecks.put(handler.method_32311().method_5667(), 100);
         welcome(handler.method_32311());
      });
      ServerTickEvents.END_SERVER_TICK.register(AiKnocker::tickSpawnChecks);
      ServerPlayConnectionEvents.DISCONNECT.register((ServerPlayConnectionEvents.Disconnect)(handler, server) -> {
         KnockerEntity knocker = findKnocker(handler.method_32311());
         if (knocker != null) {
            knocker.getObserver().onPlayerLeft();
         }

      });
      ServerLivingEntityEvents.AFTER_DEATH.register((ServerLivingEntityEvents.AfterDeath)(entity, source) -> {
         if (entity instanceof class_3222 player) {
            KnockerEntity knocker = findKnocker(player);
            if (knocker != null && knocker.method_5739(player) < 128.0F) {
               knocker.onPlayerDied(player, source);
            }
         }

      });
      UseBlockCallback.EVENT.register((UseBlockCallback)(player, world, hand, hitResult) -> {
         if (!world.method_8608() && world instanceof class_3218 serverWorld) {
            class_2338 pos = hitResult.method_17777();

            for(KnockerEntity knocker : serverWorld.method_18198(KNOCKER, (k) -> k.method_5805())) {
               if (knocker.getItems().isStash(pos)) {
                  knocker.getItems().onStashOpened(player);
               }
            }
         }

         return class_1269.field_5811;
      });
      ServerMessageEvents.CHAT_MESSAGE.register((ServerMessageEvents.ChatMessage)(message, sender, params) -> {
         String text = message.method_46291().getString();
         sender.method_5682().execute(() -> {
            KnockerEntity knocker = findKnocker(sender);
            if (knocker != null) {
               if (knocker.isChosen(sender)) {
                  knocker.handlePlayerMessage(sender, text);
               } else {
                  knocker.handleStranger(sender, text);
               }

            }
         });
      });
      LOGGER.info("AI Knocker загружен");
   }

   private void registerCommands() {
      CommandRegistrationCallback.EVENT.register((CommandRegistrationCallback)(dispatcher, registryAccess, environment) -> {
         dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)class_2170.method_9247("spawnknocker").requires((src) -> src.method_9259(2))).executes((context) -> {
            class_3222 player = ((class_2168)context.getSource()).method_44023();
            if (player == null) {
               return 0;
            } else {
               KnockerEntity existing = findKnocker(player);
               if (existing != null) {
                  existing.comeTo(player);
                  ((class_2168)context.getSource()).method_9226(() -> class_2561.method_43470("§aКнокер уже есть — теперь он рядом."), false);
               } else {
                  spawnNear(player, (double)2.0F);
                  ((class_2168)context.getSource()).method_9226(() -> class_2561.method_43470("§aКнокер появился!"), false);
               }

               return 1;
            }
         }));
         dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)class_2170.method_9247("obsession").requires((src) -> src.method_9259(2))).then(class_2170.method_9244("value", IntegerArgumentType.integer(0, 1000)).executes((context) -> {
            int value = IntegerArgumentType.getInteger(context, "value");
            class_3222 player = ((class_2168)context.getSource()).method_44023();
            if (player == null) {
               return 0;
            } else {
               KnockerEntity knocker = findKnocker(player);
               if (knocker == null) {
                  knocker = spawnNear(player, (double)2.0F);
               }

               knocker.setObsessionPoints(value);
               if (knocker.method_5739(player) > 50.0F) {
                  knocker.comeTo(player);
               }

               ((class_2168)context.getSource()).method_9226(() -> class_2561.method_43470("§6Очки одержимости: " + value), false);
               return 1;
            }
         })));
         dispatcher.register((LiteralArgumentBuilder)class_2170.method_9247("knockermemory").executes((context) -> {
            class_3222 player = ((class_2168)context.getSource()).method_44023();
            if (player == null) {
               return 0;
            } else {
               List<String> diary = KnockerMemoryStore.diary(player.method_5667());
               player.method_7353(class_2561.method_43470("§7--- Дневник Кнокера (" + diary.size() + ") ---"), false);
               if (diary.isEmpty()) {
                  player.method_7353(class_2561.method_43470("§8пока пусто: дневник пишется, когда разговор становится длинным"), false);
               }

               for(String fact : diary) {
                  player.method_7353(class_2561.method_43470("§7- " + fact), false);
               }

               return 1;
            }
         }));
         dispatcher.register((LiteralArgumentBuilder)class_2170.method_9247("knockerforget").executes((context) -> {
            class_3222 player = ((class_2168)context.getSource()).method_44023();
            if (player == null) {
               return 0;
            } else {
               KnockerMemoryStore.forget(player.method_5667());
               ((class_2168)context.getSource()).method_9226(() -> class_2561.method_43470("§7Кнокер всё забыл... или делает вид."), false);
               return 1;
            }
         }));
         dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)class_2170.method_9247("knockerreset").requires((src) -> src.method_9259(2))).executes((context) -> {
            class_3222 player = ((class_2168)context.getSource()).method_44023();
            if (player == null) {
               return 0;
            } else {
               player.method_5738("aiknocker_gone");
               player.method_5738("aiknocker_dead");
               KnockerMemoryStore.setWorldChosen(player.method_5667());
               KnockerMemoryStore.forget(player.method_5667());
               player.method_5752().removeIf((t) -> t.startsWith("aiknocker_pts_"));
               respawnAt.remove(player.method_5667());
               KnockerEntity old = findKnocker(player);
               if (old != null) {
                  old.method_31472();
               }

               spawnNear(player, (double)2.0F);
               ((class_2168)context.getSource()).method_9226(() -> class_2561.method_43470("§aКнокер вернулся. Всё сначала."), false);
               return 1;
            }
         }));
         dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)class_2170.method_9247("knockerai").requires((src) -> src.method_9259(2) || src.method_9211().method_3724() && src.method_44023() != null)).then(class_2170.method_9247("install").executes((context) -> {
            KnockerAISetup.startInstall();
            ((class_2168)context.getSource()).method_9226(() -> class_2561.method_43470("§5Кнокер просыпается... §7ИИ скачивается в фоне, можно играть."), false);
            return 1;
         }))).then(class_2170.method_9247("no").executes((context) -> {
            KnockerAISetup.decline();
            ((class_2168)context.getSource()).method_9226(() -> class_2561.method_43470("§7Хорошо. Кнокер будет отвечать своими словами. Передумаешь — /knockerai install"), false);
            return 1;
         }))).then(class_2170.method_9247("status").executes((context) -> {
            ((class_2168)context.getSource()).method_9226(() -> class_2561.method_43470("§7" + KnockerAISetup.status()), false);
            return 1;
         })));
         dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)class_2170.method_9247("knockerbug").executes((context) -> bugReport((class_2168)context.getSource(), ""))).then(class_2170.method_9244("text", StringArgumentType.greedyString()).executes((context) -> bugReport((class_2168)context.getSource(), StringArgumentType.getString(context, "text")))));
         dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)class_2170.method_9247("knockerreload").requires((src) -> src.method_9259(2))).executes((context) -> {
            KnockerConfig.load();
            ((class_2168)context.getSource()).method_9226(() -> class_2561.method_43470(KnockerConfig.useExternalAI() ? "§aКонфиг перечитан: отвечает внешний ИИ" : "§aКонфиг перечитан: Кнокер отвечает сам"), false);
            return 1;
         }));
      });
   }

   private static int bugReport(class_2168 src, String text) {
      class_3222 player = src.method_44023();
      if (player == null) {
         return 0;
      } else {
         try {
            Path file = KnockerBugReport.create(player, text);
            String path = file.toAbsolutePath().toString();
            src.method_9226(() -> class_2561.method_43470("§aБаг-репорт сохранён. Скинь этот файл нам в телеграм (t.me/vesperlorngame):").method_10852(class_2561.method_43470("\n§7" + path).method_27694((s) -> s.method_10958(new class_2558(class_2559.field_21462, path)).method_10949(new class_2568(class_5247.field_24342, class_2561.method_43470("Нажми — путь скопируется"))))), false);
            return 1;
         } catch (Exception e) {
            LOGGER.error("Не удалось создать баг-репорт", e);
            src.method_9213(class_2561.method_43470("Не удалось создать баг-репорт: " + e.getMessage()));
            return 0;
         }
      }
   }

   private static void tickSpawnChecks(MinecraftServer server) {
      long now = (long)server.method_3780();
      if (now % 600L == 0L) {
         for(class_3222 player : server.method_3760().method_14571()) {
            if (!player.method_7325() && !pendingSpawnChecks.containsKey(player.method_5667()) && !player.method_5752().contains("aiknocker_gone")) {
               if (findKnocker(player) != null) {
                  missingChecks.remove(player.method_5667());
               } else if (isHisPlayer(player) && (Integer)missingChecks.merge(player.method_5667(), 1, Integer::sum) >= 2) {
                  missingChecks.remove(player.method_5667());
                  Long at = (Long)respawnAt.get(player.method_5667());
                  if (at == null && player.method_5752().contains("aiknocker_dead")) {
                     respawnAt.put(player.method_5667(), now + 1200L);
                  } else if (at == null || now >= at) {
                     respawnAt.remove(player.method_5667());
                     boolean wasDead = player.method_5752().remove("aiknocker_dead");
                     KnockerEntity k = spawnFar(player, loadPoints(player));
                     if (wasDead) {
                        k.rememberEvent("я вернулся. она думала, что убила меня");
                     }

                     LOGGER.info("Кнокер появился вдалеке от {}", player.method_5477().getString());
                  }
               }
            }
         }
      }

      if (!pendingSpawnChecks.isEmpty()) {
         Iterator<Map.Entry<UUID, Integer>> it = pendingSpawnChecks.entrySet().iterator();

         while(it.hasNext()) {
            Map.Entry<UUID, Integer> entry = (Map.Entry)it.next();
            int left = (Integer)entry.getValue() - 1;
            if (left > 0) {
               entry.setValue(left);
            } else {
               it.remove();
               class_3222 player = server.method_3760().method_14602((UUID)entry.getKey());
               if (player != null) {
                  KnockerAISetup.onJoin(player);
                  if (!player.method_5752().contains("aiknocker_gone")) {
                     KnockerEntity knocker = findKnocker(player);
                     if (knocker == null) {
                        if (isHisPlayer(player)) {
                           if (player.method_5752().contains("aiknocker_dead")) {
                              respawnAt.putIfAbsent(player.method_5667(), (long)server.method_3780() + 1200L);
                           } else {
                              spawnFar(player, loadPoints(player));
                              LOGGER.info("Кнокер появился вдалеке от {}", player.method_5477().getString());
                           }
                        }
                     } else {
                        knocker.getObserver().onPlayerReturned(player);
                     }
                  }
               }
            }
         }

      }
   }

   private static boolean isHisPlayer(class_3222 player) {
      UUID chosen = KnockerMemoryStore.worldChosen();
      return chosen == null || chosen.equals(player.method_5667());
   }

   public static KnockerEntity findKnocker(class_3222 player) {
      class_3218 world = player.method_51469();
      KnockerEntity best = null;
      double bestDist = Double.MAX_VALUE;

      for(KnockerEntity k : world.method_18198(KNOCKER, (kx) -> kx.method_5805())) {
         double d = k.method_5858(player);
         if (d < bestDist) {
            bestDist = d;
            best = k;
         }
      }

      return best;
   }

   public static void savePoints(class_3222 player, int points) {
      String tag = "aiknocker_pts_" + points;
      if (!player.method_5752().contains(tag)) {
         player.method_5752().removeIf((t) -> t.startsWith("aiknocker_pts_"));
         player.method_5780(tag);
      }
   }

   public static int loadPoints(class_3222 player) {
      for(String t : player.method_5752()) {
         if (t.startsWith("aiknocker_pts_")) {
            try {
               return Integer.parseInt(t.substring("aiknocker_pts_".length()));
            } catch (NumberFormatException var4) {
            }
         }
      }

      return 0;
   }

   public static void scheduleReturn(class_3222 player, int points, int delay) {
      savePoints(player, points);
      player.method_5780("aiknocker_dead");
      MinecraftServer server = player.method_5682();
      if (server != null) {
         respawnAt.put(player.method_5667(), (long)server.method_3780() + (long)delay);
      }

   }

   private static void welcome(class_3222 player) {
      if (!"done".equals(KnockerConfig.welcome)) {
         KnockerConfig.save("welcome", "done");
         KnockerConfig.welcome = "done";
         player.method_7353(class_2561.method_43470("§5§lAI Knocker §r§7от команды §dVesperLorn"), false);
         player.method_7353(class_2561.method_43470("§7Новости, обновления и баги — в нашем телеграме: ").method_10852(class_2561.method_43470("§d§nt.me/vesperlorngame").method_27694((st) -> st.method_10958(new class_2558(class_2559.field_11749, "https://t.me/vesperlorngame")).method_10949(new class_2568(class_5247.field_24342, class_2561.method_43470("открыть канал"))))), false);
      }
   }

   public static KnockerEntity spawnFar(class_3222 player, int points) {
      class_3218 world = player.method_51469();
      class_243 best = findHideSpot(player, (double)18.0F, (double)32.0F, false);
      KnockerEntity knocker;
      if (best == null) {
         knocker = spawnNear(player, (double)6.0F);
      } else {
         knocker = new KnockerEntity(KNOCKER, world);
         knocker.method_5808(best.field_1352, best.field_1351, best.field_1350, 0.0F, 0.0F);
         world.method_8649(knocker);
      }

      knocker.setObsessionPoints(points);
      knocker.choose(player);
      if (best != null && lastSpotIsTree) {
         knocker.startPeeking(player, 1200 + world.method_8409().method_43048(1200));
      }

      return knocker;
   }

   public static class_243 findHideSpot(class_3222 player, double minDist, double maxDist, boolean inView) {
      class_3218 world = player.method_51469();
      lastSpotIsTree = false;
      class_5819 rnd = world.method_8409();
      class_243 best = null;
      double lookAngle = Math.toRadians((double)(player.method_36454() + 90.0F));

      for(int attempt = 0; attempt < 60 && best == null; ++attempt) {
         double angle = inView ? lookAngle + (rnd.method_43058() - (double)0.5F) * Math.toRadians((double)100.0F) : rnd.method_43058() * Math.PI * (double)2.0F;
         double dist = minDist + rnd.method_43058() * (maxDist - minDist);
         int x = (int)Math.floor(player.method_23317() + Math.cos(angle) * dist);
         int z = (int)Math.floor(player.method_23321() + Math.sin(angle) * dist);
         int y = world.method_8624(class_2903.field_13203, x, z);
         class_2338 ground = new class_2338(x, y, z);
         class_2338 trunk = null;

         for(class_2338 p : class_2338.method_10097(ground.method_10069(-3, -1, -3), ground.method_10069(3, 2, 3))) {
            if (world.method_8320(p).method_26164(class_3481.field_15475)) {
               trunk = p.method_10062();
               break;
            }
         }

         if (trunk != null) {
            double dx = (double)trunk.method_10263() + (double)0.5F - player.method_23317();
            double dz = (double)trunk.method_10260() + (double)0.5F - player.method_23321();
            class_2350 away = class_2350.method_10142(dx, (double)0.0F, dz);
            int sx = trunk.method_10263() + away.method_10148();
            int sz = trunk.method_10260() + away.method_10165();
            int sy = world.method_8624(class_2903.field_13203, sx, sz);
            if (Math.abs((double)sy - player.method_23318()) < (double)12.0F && world.method_18026(KNOCKER.method_18386().method_30231((double)sx + (double)0.5F, (double)sy, (double)sz + (double)0.5F))) {
               best = new class_243((double)sx + (double)0.5F, (double)sy, (double)sz + (double)0.5F);
               lastSpotIsTree = true;
            }
         }

         if (best == null && attempt >= 40 && Math.abs((double)y - player.method_23318()) < (double)12.0F && world.method_18026(KNOCKER.method_18386().method_30231((double)x + (double)0.5F, (double)y, (double)z + (double)0.5F))) {
            best = new class_243((double)x + (double)0.5F, (double)y, (double)z + (double)0.5F);
         }
      }

      return best;
   }

   private static KnockerEntity spawnNear(class_3222 player, double dist) {
      class_3218 world = player.method_51469();
      KnockerEntity knocker = new KnockerEntity(KNOCKER, world);
      double[][] offsets = new double[][]{{dist, dist}, {-dist, dist}, {dist, -dist}, {-dist, -dist}, {(double)0.0F, (double)0.0F}};

      for(double[] o : offsets) {
         double x = player.method_23317() + o[0];
         double z = player.method_23321() + o[1];
         if (o[0] == (double)0.0F && o[1] == (double)0.0F || world.method_18026(KNOCKER.method_18386().method_30231(x, player.method_23318(), z))) {
            knocker.method_5808(x, player.method_23318(), z, player.method_36454() + 180.0F, 0.0F);
            break;
         }
      }

      world.method_8649(knocker);
      return knocker;
   }

   static {
      KNOCKER = (class_1299)class_2378.method_10230(class_7923.field_41177, class_2960.method_60655("aiknocker", "knocker"), FabricEntityTypeBuilder.create(class_1311.field_6294, KnockerEntity::new).dimensions(class_4048.method_18385(0.6F, 1.8F)).build());
      pendingSpawnChecks = new HashMap();
      respawnAt = new HashMap();
      missingChecks = new HashMap();
      lastSpotIsTree = false;
   }
}
