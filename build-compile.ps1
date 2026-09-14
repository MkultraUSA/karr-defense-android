$ErrorActionPreference = "Stop"

$jdk = "C:/Program Files/Eclipse Adoptium/jdk-17.0.20.101-hotspot"
$buildTools = "C:/Users/mrrob/Documents/Codex/2026-09-09/referenced-chatgpt-conversation-this-is-an/work/android-sdk/build-tools/35.0.1"
$androidJar = "C:/Users/mrrob/Documents/Codex/2026-09-09/referenced-chatgpt-conversation-this-is-an/work/android-sdk/platforms/android-35/android.jar"
$root = "C:/Users/mrrob/Documents/Codex/2026-09-13/referenced-chatgpt-conversation-this-is-an/work/karr-defense-android"
$manual = "$root/manual-build"
$classes = "$manual/classes"
$gen = "$manual/gen"
$dex = "$manual/dex"
$compiled = "$manual/compiled.zip"
$unsigned = "$manual/unsigned.apk"
$signed = "$root/app/build/outputs/apk/debug/karr-defense-inspector-debug.apk"
$outDir = "$root/app/build/outputs/apk/debug"

Write-Host "=== KARR DEFENSE BUILD ===" -ForegroundColor Cyan

# Check tools exist
foreach ($p in ($jdk, $buildTools, $androidJar)) {
    if (!(Test-Path $p)) { throw "Missing: $p" }
}
Write-Host "Tools OK"

# Clean + prepare dirs
Remove-Item -LiteralPath $classes -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $classes, $gen, $dex | Out-Null
Write-Host "Dirs ready"

# aapt2 compile resources
Write-Host "aapt2 compile..."
& "$buildTools/aapt2.exe" compile --dir "$root/app/src/main/res" -o $compiled
if ($LASTEXITCODE -ne 0) { throw "aapt2 compile failed" }

# aapt2 link
Write-Host "aapt2 link..."
& "$buildTools/aapt2.exe" link `
    -I $androidJar `
    --manifest "$root/app/src/main/AndroidManifest.xml" `
    --java $gen `
    -o $unsigned `
    --auto-add-overlay `
    --min-sdk-version 23 `
    --target-sdk-version 35 `
    $compiled
if ($LASTEXITCODE -ne 0) { throw "aapt2 link failed" }

# javac
Write-Host "javac..."
$javaFiles = Get-ChildItem -LiteralPath "$root/app/src/main/java" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
$javaFiles += Get-ChildItem -LiteralPath $gen -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
& "$jdk/bin/javac.exe" -source 8 -target 8 -bootclasspath $androidJar -encoding UTF-8 -d $classes @javaFiles
if ($LASTEXITCODE -ne 0) { throw "javac failed" }
Write-Host "javac OK"

# jar classes
& "$jdk/bin/jar.exe" cf "$manual/classes.jar" -C $classes .
if ($LASTEXITCODE -ne 0) { throw "jar failed" }

# d8
Write-Host "d8..."
& "$jdk/bin/java.exe" -cp "$buildTools/lib/d8.jar" com.android.tools.r8.D8 `
    --min-api 23 `
    --output $dex `
    "$manual/classes.jar"
if ($LASTEXITCODE -ne 0) { throw "d8 failed" }

# pack unsigned apk
& "$jdk/bin/jar.exe" uf $unsigned -C $dex classes.dex
if ($LASTEXITCODE -ne 0) { throw "jar uf failed" }

# zipalign
Write-Host "zipalign..."
& "$buildTools/zipalign.exe" -f -p 4 $unsigned "$manual/aligned.apk"
if ($LASTEXITCODE -ne 0) { throw "zipalign failed" }

# sign
Write-Host "sign..."
& "$buildTools/apksigner.bat" sign `
    --ks "C:/Users/mrrob/Documents/Codex/2026-09-09/referenced-chatgpt-conversation-this-is-an/outputs/crypto-ticker-android/debug.keystore" `
    --ks-pass pass:android `
    --key-pass pass:android `
    --out $signed `
    "$manual/aligned.apk"
if ($LASTEXITCODE -ne 0) { throw "apksigner sign failed" }

# verify
Write-Host "verify..."
& "$buildTools/apksigner.bat" verify $signed
if ($LASTEXITCODE -ne 0) { throw "apksigner verify failed" }

New-Item -ItemType Directory -Force -Path $outDir | Out-Null
Get-Item $signed | Select-Object Name, Length, LastWriteTime
Write-Host "=== BUILD OK: $signed ===" -ForegroundColor Green
