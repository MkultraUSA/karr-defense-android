$root = "C:/Users/mrrob/Documents/Codex/2026-09-13/referenced-chatgpt-conversation-this-is-an/work/karr-defense-android"
Get-ChildItem -LiteralPath (Join-Path $root "app\src\main\java") -Recurse -Filter "*.java" | ForEach-Object { Write-Output $_.FullName }
