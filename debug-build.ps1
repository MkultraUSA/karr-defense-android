$ErrorActionPreference = "Stop"

$root = "C:/Users/mrrob/Documents/Codex/2026-09-13/referenced-chatgpt-conversation-this-is-an/work/karr-defense-android"
$sdk = "C:\Users\mrrob\Documents\Codex\2026-09-09\referenced-chatgpt-conversation-this-is-an\work\android-sdk"
$buildTools = Join-Path $sdk "build-tools\35.0.1"
$androidJar = Join-Path $sdk "platforms\android-35\android.jar"
$jdk = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
$manual = Join-Path $root "manual-build"
$out = Join-Path $root "app\build\outputs\apk\debug"

$env:JAVA_HOME = $jdk
$env:Path = "$jdk\bin;$buildTools;$env:Path"

Write-Host "=== javac ===" (Join-Path $jdk "bin\javac.exe")
Write-Host "=== androidJar ===" $androidJar
Write-Host "=== classes dir ===" (Join-Path $manual "classes")
Write-Host ""

$classes = Join-Path $manual "classes"
$gen = Join-Path $manual "gen"

$javacArgs = @("-source", "8", "-target", "8", "-bootclasspath", $androidJar, "-encoding", "UTF-8", "-d", $classes)
Write-Host "javacArgs count:" $javacArgs.Count
foreach ($a in $javacArgs) { Write-Host "  ARG: [$a]" }

Write-Host ""
Write-Host "=== Collecting source java files ==="
$srcFiles = Get-ChildItem -LiteralPath (Join-Path $root "app\src\main\java") -Recurse -Filter "*.java" -ErrorAction Stop | ForEach-Object { $_.FullName }
Write-Host "Source files found:" $srcFiles.Count
foreach ($f in $srcFiles) { Write-Host "  $f" }

Write-Host ""
Write-Host "=== Collecting generated java files ==="
if (Test-Path $gen) {
    $genFiles = Get-ChildItem -LiteralPath $gen -Recurse -Filter "*.java" -ErrorAction Stop | ForEach-Object { $_.FullName }
    Write-Host "Gen files found:" $genFiles.Count
    foreach ($f in $genFiles) { Write-Host "  $f" }
} else {
    Write-Host "GEN DIR MISSING"
    $genFiles = @()
}

$javaFiles = @()
$javaFiles += $srcFiles
$javaFiles += $genFiles
Write-Host ""
Write-Host "=== Combined javaFiles count:" $javaFiles.Count "==="
foreach ($f in $javaFiles) { Write-Host "  $f" }

Write-Host ""
Write-Host "=== About to invoke javac ==="
Write-Host "FilePath:" (Join-Path $jdk "bin\javac.exe")
Write-Host "Arguments count:" (@($javacArgs) + $javaFiles).Count
$allArgs = @($javacArgs) + $javaFiles
foreach ($a in $allArgs) { Write-Host "  ALLARG: [$a]" }

& (Join-Path $jdk "bin\javac.exe") @allArgs
Write-Host "javac exit code:" $LASTEXITCODE
