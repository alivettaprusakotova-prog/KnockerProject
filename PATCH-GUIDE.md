# Как чинить AI Knocker через PowerShell — памятка

Всё ниже уже применено в коде. Скрипты готовы на случай, если понадобится поменять настройки или текст вручную. **Ничего из этого вручную запускать не обязательно** — пришли мне, что нужно, и я сделаю патч и сборку.

## 0. Где что лежит (Windows)

| Что | Путь |
|---|---|
| Мод | `%APPDATA%\.minecraft\mods\AI-Knocker-DarkRomance-1.0.0.jar` |
| Конфиг | `%APPDATA%\.minecraft\config\aiknocker.properties` |
| Память/дневник | `%APPDATA%\.minecraft\config\aiknocker\*.json` |
| Логи | `%APPDATA%\.minecraft\logs\latest.log` |
| Исходник | `D:\Minecraft\game\AI-Knocker-Build` |
| Готовый jar | `D:\Minecraft\game\AI-Knocker-Build\AI-Knocker-DarkRomance-1.0.0.jar` |

## 1. Конфиг и режим ИИ

`ai_mode`: `auto` (по умолчанию — сам пробует ИИ, при ошибке уходит в офлайн), `api` (только ИИ), `offline` (только скрипты, «хорошо» вернутся).

```powershell
# автоматический патч конфига (ai_mode=api, debug=true и т.д.)
powershell -ExecutionPolicy Bypass -File .\tools\fix-config.ps1 -MinecraftDir "$env:APPDATA\.minecraft"
```

В игре после правки: `/knockerreload`. Диагностика: `debug=true` в конфиге, команда `/knockerai status`, лог `%APPDATA%\.minecraft\logs\latest.log`.

Если ИИ не отвечает:
1. `ollama list` — есть ли `gemma3:4b`, иначе `ollama pull gemma3:4b`.
2. Скрипт `tools\fix-config.ps1` уже пишет `api_key=ollama` (иначе 401 Unauthorized).
3. `api_url` должен оканчиваться на `/v1/chat/completions`.
4. Если ответ длинный и обрывается — таймаут увеличен до 180 с в `KnockerAI.ask(...)`; `num_predict` не задаётся.

## 2. Запрет «хорошо»

Основные источники: `KnockerBrain.java` (массивы `AGREE`, `ANSWER`, `ANSWER_YES/NO/OTHER`), `KnockerExtraLines.java`, `KnockerActions.java`, `KnockerTopics.java`, `KnockerMessages.java`, `AiKnocker.java:235`.

Что сделано:
- `KnockerAI.clean(...)` вырезает сухие короткие ответы из reply ИИ — см. `KnockerChat.java`.
- `KnockerChat.say(...)` ловит точные «Хорошо/ок/угу/да/нет/понятно/ясно» от старых модулей и заменяет их на `brain.ambient()`.
- В промпте ИИ прямо запрещены короткие сухие ответы.

Если «хорошо» появится снова: найди источник
```powershell
Select-String -Path .\src\main\java\com\theneru\knocker\**\*.java -Pattern 'Хорошо|хорошо|угу|ладно'
```
и замени фразу через `tools\patch-text.ps1`, например:
```powershell
powershell -ExecutionPolicy Bypass -File .\tools\patch-text.ps1 `
  -File src\main\java\com\theneru\knocker\entity\KnockerBrain.java `
  -Find 'Хорошо...' -Replace 'Как ты красива, когда молчишь...'
```

## 3. Промпт и стиль

`KnockerAI.java`: `SYSTEM_PROMPT` — яндере-муж, игрок — девушка, «ты/тебя/моя», `*действия*` разрешены, эмодзи/смайлики запрещены, без цензуры. Лимит ответа 800 символов, `maxTokens=450`.

Быстрый патч (пример):
```powershell
powershell -ExecutionPolicy Bypass -File .\tools\patch-text.ps1 `
  -File src\main\java\com\theneru\knocker\entity\KnockerAI.java `
  -Find 'без цензуры' -Replace 'без цензуры, но без сухих коротких ответов'
```

## 4. Кулдауны и подарки

- ПКМ Кнокера: кулдаун убран в `KnockerEntity.java` (`touchCooldown` удалён).
- Подарки: `KnockerItems.java` — без `requestCooldownUntil`/`nextDiamondTick`/`nextFoodPresentTick` и без проверки фазы.
- Теги: `[действие: подарок]` и `[подарок: алмаз]` парсятся в `KnockerChat.onAiResult`.
- Прямые просьбы «дай/принеси/подари/хочу/плюшка/алмаз/кушать/еда/тотем/цветок» принудительно выдают предмет даже без тега ИИ (`KnockerItems.giftFromRequest`).
- Если Кнокер далеко/невидим — `comeToGift()` ставит его на 2–3 блока перед игроком, предмет падает к ногам.

Чтобы добавить предмет: `KnockerItems.namedItem(...)` + field ID из маппинга (например `DIAMOND field_8477`).

## 5. Сборка

```powershell
.\build.ps1
```
Скрипт: `clean`, `--no-daemon`, `build`, копирует jar в корень. Готовый файл: `AI-Knocker-DarkRomance-1.0.0.jar`.
Если сборка падает с `class_*`/`package software.bernie.geckolib... not exist`: проверь `build.gradle` — плагин `net.fabricmc.fabric-loom-remap`, GeckoLib подключён в `repositories`/`dependencies`. Это уже исправлено.

## 6. Откат

```powershell
git log --oneline -5
git checkout -- <файл>          # откат одного файла к HEAD
git reset --hard HEAD~1         # откат последнего коммита (осторожно!)
```

## 7. Офлайн-фразы и память

`KnockerBrain.java` — `LINES`, `AMBIENT`; `KnockerExtraLines.java`, `KnockerTopics.java` — запасные реплики. Память: `KnockerMemoryStore.java` — `HISTORY_LIMIT=120`, `DIARY_LIMIT=200`, `PROMPT_DIARY=80`; фильтр `JUNK` выключен.
