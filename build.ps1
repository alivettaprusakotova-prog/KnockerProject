# Сборка мода AI Knocker Dark Romance на Windows (PowerShell).
# Запуск из папки проекта: .\build.ps1
$ErrorActionPreference = 'Stop'
Set-Location -Path $PSScriptRoot

Write-Host "== Очистка старых артефактов ==" -ForegroundColor Cyan
if (Test-Path .\build) { Remove-Item -Recurse -Force .\build }

Write-Host "== Сборка мода ==" -ForegroundColor Cyan
& .\gradlew.bat --no-daemon clean build
if ($LASTEXITCODE -ne 0) { throw "Gradle завершился с кодом $LASTEXITCODE" }

$jar = Get-ChildItem .\build\libs\*.jar | Where-Object { $_.Name -notmatch '-sources\.jar$' } | Select-Object -First 1
if (-not $jar) { throw "Готовый jar не найден в build\libs" }

$out = Join-Path $PSScriptRoot 'AI-Knocker-DarkRomance-1.0.0.jar'
Copy-Item -Path $jar.FullName -Destination $out -Force
Write-Host "== ГОТОВО: $out ==" -ForegroundColor Green
Write-Host "Положи этот файл в папку mods (без '-sources')." -ForegroundColor Yellow
