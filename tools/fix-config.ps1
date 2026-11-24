# Включить локальный ИИ и убрать офлайн-ответы «Хорошо».
# Запуск: powershell -ExecutionPolicy Bypass -File .\tools\fix-config.ps1
param(
    [string]$MinecraftDir = "$env:APPDATA\.minecraft"
)

$cfg = Join-Path $MinecraftDir 'config\aiknocker.properties'
if (-not (Test-Path $cfg)) {
    throw "Не найден $cfg — запусти Minecraft с модом хотя бы один раз."
}

$lines = [System.IO.File]::ReadAllLines($cfg)

function Set-Property([string]$key, [string]$value) {
    $script:lines = $script:lines | ForEach-Object {
        if ($_ -match "^\s*$([regex]::Escape($key))\s*=") { "$key=$value" } else { $_ }
    }
    if (-not ($script:lines | Where-Object { $_ -match "^\s*$([regex]::Escape($key))\s*=" })) {
        $script:lines += "$key=$value"
    }
}

Set-Property 'ai_mode'  'api'
Set-Property 'api_key'  'ollama'
Set-Property 'api_url'  'http://127.0.0.1:11434/v1/chat/completions'
Set-Property 'model'    'gemma3:4b'
Set-Property 'debug'    'true'

[System.IO.File]::WriteAllLines($cfg, $lines, (New-Object System.Text.UTF8Encoding($false)))
Write-Host "_config обновлён: $cfg" -ForegroundColor Green
Write-Host "В игре набери /knockerreload" -ForegroundColor Yellow
Write-Host "Проверь Ollama: ollama list ; ollama pull gemma3:4b" -ForegroundColor Yellow
