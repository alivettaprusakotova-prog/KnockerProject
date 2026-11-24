package com.theneru.knocker;

import com.theneru.knocker.entity.KnockerEntity;
import com.theneru.knocker.entity.KnockerGender;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.class_3222;

public final class KnockerBugReport {
   private KnockerBugReport() {
   }

   public static Path create(class_3222 player, String description) throws Exception {
      FabricLoader fl = FabricLoader.getInstance();
      StringBuilder b = new StringBuilder();
      String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
      b.append("=== AI KNOCKER — БАГ-РЕПОРТ ").append(time).append(" ===\n");
      b.append("Что случилось: ").append(description != null && !description.isBlank() ? description : "(не написано)").append("\n\n");
      b.append("--- Версии ---\n");
      b.append("Мод: ").append(version(fl, "aiknocker")).append('\n');
      b.append("Minecraft: ").append(version(fl, "minecraft")).append('\n');
      b.append("Fabric Loader: ").append(version(fl, "fabricloader")).append('\n');
      b.append("Java: ").append(System.getProperty("java.version")).append(", ОС: ").append(System.getProperty("os.name")).append('\n');
      b.append("Моды: ");

      for(ModContainer m : fl.getAllMods()) {
         String id = m.getMetadata().getId();
         if (!id.startsWith("fabric-") && !id.equals("java") && !id.equals("minecraft") && !id.equals("fabricloader")) {
            b.append(id).append(' ').append(m.getMetadata().getVersion().getFriendlyString()).append(", ");
         }
      }

      b.append("\n\n--- Настройки (ключ скрыт) ---\n");
      Path cfg = fl.getConfigDir().resolve("aiknocker.properties");
      if (Files.exists(cfg, new LinkOption[0])) {
         for(String line : Files.readAllLines(cfg, StandardCharsets.UTF_8)) {
            if (!line.trim().startsWith("#") && !line.isBlank()) {
               b.append(line.startsWith("api_key=") && line.length() > 8 && !line.equals("api_key=ollama") ? "api_key=***" : line).append('\n');
            }
         }
      }

      b.append("ИИ: ").append(KnockerAISetup.status()).append("\n\n");
      b.append("--- Кнокер ---\n");
      KnockerEntity k = AiKnocker.findKnocker(player);
      if (k == null) {
         b.append("не найден рядом\n");
      } else {
         b.append("Очки: ").append(k.getObsessionPoints()).append(" (").append(k.getPhase()).append(")\n");
         b.append("HP: ").append(k.method_6032()).append('/').append(k.method_6063()).append(", до игрока: ").append((int)k.method_5739(player)).append(" блоков\n");
         b.append("Позиция: ").append(k.method_24515().method_23854()).append(", дом: ").append(k.getHomePos()).append('\n');
         b.append("В руке: ").append(k.method_6047()).append('\n');
         b.append("События:\n");

         for(String e : k.getEvents()) {
            b.append("  ").append(e).append('\n');
         }
      }

      b.append("Пол игрока: ").append(KnockerGender.get(player.method_5667())).append('\n');
      b.append("Факты: ").append(KnockerMemoryStore.facts(player.method_5667())).append("\n\n");
      b.append("--- Последние сообщения ---\n");

      for(String[] m : KnockerMemoryStore.recent(player.method_5667(), 30)) {
         b.append(m[0].equals("user") ? "Игрок: " : "Кнокер: ").append(m[1]).append('\n');
      }

      b.append("\n--- Лог игры (конец) ---\n");
      b.append(tail(fl.getGameDir().resolve("logs").resolve("latest.log"), 300));
      Path ollamaLog = fl.getGameDir().resolve("aiknocker").resolve("ollama.log");
      if (Files.exists(ollamaLog, new LinkOption[0])) {
         b.append("\n--- Лог ИИ (конец) ---\n").append(tail(ollamaLog, 60));
      }

      Path dir = fl.getGameDir().resolve("aiknocker_bugreports");
      Files.createDirectories(dir);
      Path file = dir.resolve("bug_" + time + ".txt");
      Files.writeString(file, b.toString(), StandardCharsets.UTF_8);
      return file;
   }

   private static String version(FabricLoader fl, String id) {
      return (String)fl.getModContainer(id).map((m) -> m.getMetadata().getVersion().getFriendlyString()).orElse("?");
   }

   private static String tail(Path file, int lines) {
      try {
         if (!Files.exists(file, new LinkOption[0])) {
            return "(нет файла)\n";
         } else {
            List<String> all = Files.readAllLines(file, StandardCharsets.UTF_8);
            String var6 = String.join("\n", all.subList(Math.max(0, all.size() - lines), all.size()));
            return var6 + "\n";
         }
      } catch (Exception var5) {
         try {
            List<String> all = Files.readAllLines(file, Charset.defaultCharset());
            String var10000 = String.join("\n", all.subList(Math.max(0, all.size() - lines), all.size()));
            return var10000 + "\n";
         } catch (Exception e2) {
            return "(не удалось прочитать: " + e2.getMessage() + ")\n";
         }
      }
   }
}
