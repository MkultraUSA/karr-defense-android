# KARR Defense Android App — Tablet Install Scope

**App**: Field Security Inspector (`com.codex.karrdefense`)
**APK**: `app/build/outputs/apk/debug/karr-defense-inspector-debug.apk`
**Tablet**: K12202309044538 (connected via adb)
**Build**: VPS Hostinger manual toolchain (JDK 17, build-tools 35.0.1, platform android-35, debug keystore)

---

## What's in the app

10 field tools, all wired into a single MainActivity with RacerX HUD styling (midnight indigo #0d0d1a bg, crimson #e60000 primary buttons, cyan #00ffff scan controls, yellow #ffcc00 report actions, monospace) and ANIMAE art integration (racer_zero_splash.png, racer_zero_assistant.png in drawable-nodpi):

1. **Target Freezer** — freezes a target's detail view for inspection
2. **KARR/SWDS Research Panel** — keyword-based detection research (karr, swds, southwest, acrisure) with Tier 1/Tier 2 framing
3. **BLE Deep Detail** — full advertisement payload decode: service UUIDs (16/32-bit), manufacturer ID + OUI lookup, local name, tx power, service data, raw hex, printable ASCII
4. **Wi-Fi Deep Detail** — SSID, BSSID, OUI/vendor, channel/frequency, security mode, RSSI trend, hidden/open/WEP/WPA flags
5. **OUI/Vendor Lookup** — embedded vendor database (few dozen OUI entries to start) via OuiLookup.java
6. **Session Timeline** — chronological event log of all observations and actions
7. **Interesting Target Tags** — per-target tags: KARR?, Vehicle?, Head Unit?, Hotspot?, Aftermarket?, False Positive, Follow Up
8. **Evidence Packet Export** — structured evidence packet with decoded fields + session metadata + notes, JSONL export
9. **Research Notes** — free-text per-target research notes
10. **Disclosure Report Mode** — Mercedes-Benz VDP template + generic VDP template fallback

---

## Build status

Build on VPS Hostinger: aapt2 compile → aapt2 link → javac → d8 → zipalign → apksigner. Clean: 39 classes, 0 errors, signed v1/v2/v3.

**APK**: `app/build/outputs/apk/debug/karr-defense-inspector-debug.apk`
- versionCode 5, versionName "0.5-vehicle-audit"
- package: com.codex.karrdefense, minSdk 23, targetSdk 35
- 6.5MB, signed debug keystore

**v0.5 fixes**: H1 (VehicleClassifier token matching), H2 (Util.json() escapes all C0 control chars), CI (correct aapt2/d8/signing flow).

**Deployed**: v0.5 installed and running on K12202309044538 (MainActivity top resumed, no crashes).

---

## Install requirements

- User present for Play Protect tap on K12 tablet
- ADB auth not sticky — accept prompt each session
- `adb install -r -d` for debug replacement
- APK: `app/build/outputs/apk/debug/karr-defense-inspector-debug.apk`

---

## Safety constraints

- Passive BLE scan only — no GATT writes, no control commands
- Wi-Fi scan metadata only — no packet injection
- Customer actively participates in remediation (operator guides, customer acts)
- Defensive/authorized inspection only

---

## CI

GitHub Actions: builds + signs + smoke-tests on push to main, nightly 02:00 UTC, manual dispatch. Functional — corrected aapt2/d8/signing, real emulator install/launch smoke test.
