$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$sdk = "C:\Users\mrrob\Documents\Codex\2026-09-09\referenced-chatgpt-conversation-this-is-an\work\android-sdk"
$buildTools = Join-Path $sdk "build-tools\35.0.1"
$androidJar = Join-Path $sdk "platforms\android-35\android.jar"
$jdk = if ($env:JAVA_HOME) { $env:JAVA_HOME } else { "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot" }
$manual = Join-Path $root "manual-build"
$out = Join-Path $root "app\build\outputs\apk\debug"

$classes = Join-Path $manual "classes"
$compiled = Join-Path $manual "compiled.zip"
$gen = Join-Path $manual "gen"
$dex = Join-Path $manual "dex"
$unsigned = Join-Path $manual "unsigned.apk"
$aligned = Join-Path $manual "aligned.apk"
$signed = Join-Path $out "karr-defense-inspector-debug.apk"
$keystore = "C:\Users\mrrob\Documents\Codex\2026-09-09\referenced-chatgpt-conversation-this-is-an\outputs\crypto-ticker-android\debug.keystore"

if (!(Test-Path $jdk)) {
  throw "JDK not found. Install JDK 17 or set JAVA_HOME."
}

if (!(Test-Path $androidJar) -or !(Test-Path $buildTools)) {
  throw "Android SDK API 35/build-tools 35.0.1 not found."
}

$env:JAVA_HOME = $jdk
$env:Path = "$jdk\bin;$buildTools;$env:Path"

$javacArgs = @("-source", "8", "-target", "8", "-bootclasspath", $androidJar, "-encoding", "UTF-8", "-d", $classes)

function Invoke-Checked {
  param(
    [Parameter(Mandatory = $true)][string]$FilePath,
    [Parameter(Mandatory = $true)][string[]]$Arguments
  )
  & $FilePath @Arguments
  if ($LASTEXITCODE -ne 0) {
    throw "Command failed with exit code $LASTEXITCODE`: $FilePath $Arguments"
  }
}

New-Item -ItemType Directory -Force -Path $manual, $out | Out-Null
Remove-Item -LiteralPath (Join-Path $manual "*") -Recurse -Force -ErrorAction SilentlyContinue

New-Item -ItemType Directory -Force -Path $gen, $classes, $dex | Out-Null

Invoke-Checked -FilePath (Join-Path $buildTools "aapt2.exe") -Arguments @("compile", "--dir", (Join-Path $root "app\src\main\res"), "-o", $compiled)
Invoke-Checked -FilePath (Join-Path $buildTools "aapt2.exe") -Arguments @(
  "link",
  "-I", $androidJar,
  "--manifest", (Join-Path $root "app\src\main\AndroidManifest.xml"),
  "--java", $gen,
  "-o", $unsigned,
  "--auto-add-overlay",
  "--min-sdk-version", "23",
  "--target-sdk-version", "35",
  $compiled
)

$javaFiles = @()
$javaFiles += Get-ChildItem -LiteralPath (Join-Path $root "app\src\main\java") -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
$javaFiles += Get-ChildItem -LiteralPath $gen -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }

Invoke-Checked -FilePath (Join-Path $jdk "bin\javac.exe") -Arguments (@($javacArgs) + $javaFiles)
Invoke-Checked -FilePath (Join-Path $jdk "bin\jar.exe") -Arguments @("cf", (Join-Path $manual "classes.jar"), "-C", $classes, ".")
Invoke-Checked -FilePath (Join-Path $jdk "bin\java.exe") -Arguments @(
  "-cp", (Join-Path $buildTools "lib\d8.jar"),
  "com.android.tools.r8.D8",
  "--min-api", "23",
  "--output", $dex,
  (Join-Path $manual "classes.jar")
)
Invoke-Checked -FilePath (Join-Path $jdk "bin\jar.exe") -Arguments @("uf", $unsigned, "-C", $dex, "classes.dex")
Invoke-Checked -FilePath (Join-Path $buildTools "zipalign.exe") -Arguments @("-f", "-p", "4", $unsigned, $aligned)
Invoke-Checked -FilePath (Join-Path $buildTools "apksigner.bat") -Arguments @(
  "sign",
  "--ks", $keystore,
  "--ks-pass", "pass:android",
  "--key-pass", "pass:android",
  "--out", $signed,
  $aligned
)

Invoke-Checked -FilePath (Join-Path $buildTools "apksigner.bat") -Arguments @("verify", $signed)
Get-Item $signed
