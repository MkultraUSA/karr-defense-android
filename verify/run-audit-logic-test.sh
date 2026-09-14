#!/usr/bin/env bash
# Host-side logic checks for the vehicle-audit code — no emulator required.
#
# Compiles the shipped Android-free classes (VehicleClassifier, AuditFinding,
# Util) together with verify/AuditLogicTest.java against the platform
# android.jar and runs them on the plain JVM. Useful because a runner without
# /dev/kvm cannot boot an emulator, and keeping the harness out of
# app/src/main/java means it is never packaged into the APK.
#
# Usage:  ANDROID_SDK_ROOT=/path/to/sdk verify/run-audit-logic-test.sh
set -euo pipefail

SDK="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-/opt/android-sdk}}"
AJ="$SDK/platforms/android-35/android.jar"
SRC=app/src/main/java/com/codex/karrdefense
OUT=manual-build/verify-classes

[ -f "$AJ" ] || { echo "android.jar not found at $AJ"; exit 1; }

rm -rf "$OUT"
mkdir -p "$OUT"
javac -source 8 -target 8 -classpath "$AJ" -d "$OUT" \
  "$SRC/VehicleClassifier.java" "$SRC/AuditFinding.java" "$SRC/Util.java" \
  verify/AuditLogicTest.java
java -classpath "$OUT:$AJ" com.codex.karrdefense.AuditLogicTest
