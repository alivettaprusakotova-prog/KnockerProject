# Заменить текст в исходнике и пересобрать мод.
# Пример:
#   powershell -ExecutionPolicy Bypass -File .\tools\patch-text.ps1 `
#     -File src\main\java\com\theneru\knocker\entity\KnockerBrain.java `
#     -Find 'Хорошо... я верю' -Replace 'Я твой. Всегда.'
param(
    [Parameter(Mandatory = $true)][string]$File,
    [Parameter(Mandatory = $true)][string]$Find,
    [Parameter(Mandatory = $true)][string]$Replace
)

$path = (Resolve-Path $File).Path
$text = [System.IO.File]::ReadAllText($path)
if (-not $text.Contains($Find)) { throw "В $File не найдено: $Find" }

$text = $text.Replace($Find, $Replace)
[System.IO.File]::WriteAllText($path, $text, (New-Object System.Text.UTF8Encoding($false)))
Write-Host "Патч применён: $File" -ForegroundColor Green

if (Test-Path (Join-Path $PSScriptRoot '..\build.ps1')) {
    & (Join-Path $PSScriptRoot '..\build.ps1')
}
