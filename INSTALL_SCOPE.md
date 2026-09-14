# KARR Defense Android App — Tablet Install Scope

**App**: Field Security Inspector (`com.codex.karrdefense`)
**APK**: `app/build/outputs/apk/debug/karr-defense-inspector-debug.apk`
**Tablet**: K12202309044538 (connected via adb)
**Build script**: `build-manual.ps1` (manual toolchain, JDK 17, API 35, debug keystore)

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
10. **Disclosure Report Mode** — Mercedes-Benz VDP template with Mercedes-specific fields (VIN, model year, trim, market/region, affected component, communication protocol, vulnerability class, severity, reporter contact, disclosure timeline) + generic VDP template fallback

---

## Data model

- `Observation` interface: type(), searchableText(), identity(), rssi(), summary(), detail()
- `BleObservation` / `WifiObservation` inner classes (MainActivity)
- `Finding` — ruleId, title, detail, confidence
- `DetectorRule` interface — 3 rules: known_ble_karr_swds_keyword, known_wifi_wep_network, known_vehicle_head_unit_signal
- `ObservationState` — lastLoggedMs, lastRssi per target
- `TargetHistory` — RSSI trend points per target
- `TargetTags` — tag set per target
- `TargetNotes` — free-text notes per target
- `BleDecoder` — full BLE advertisement payload decoder (288 lines)
- `WifiDecoder` — WiFi scan result decoder (194 lines)
- `OuiLookup` — private static VENDORS map, MAC prefix lookup
- `KarrSwdsResearchState` — matchedKeyword, keywordMatchCount, raw data, RSSI trend, repeated sightings
- `KarrSwdsResearchPanel` — panelText() rendering for research panel
- `EvidencePacket` — observation + decoded fields + screenshots/session metadata + notes aggregation
- `DisclosureReport` — Mercedes-Benz + Generic vendor enum, box-drawing report templates, Mercedes-specific fields
- `RuleIdToResearchText` — research text mapping per rule ID
- `Util` — now(), datetime formatting helpers

---

## Build status

Build runs javac + aapt2 + apkbuilder + signapk + zipalign via `build-manual.ps1`. Last build: exit 1, 117 compilation errors all in MainActivity.java from line 2668 — a stray `}` at line 2667 prematurely closed the class, leaving saveReport(), appendEvidence(), getRemovableEvidenceDir(), and helpers floating outside the class body.

**Fix applied**: replaced the stray `}` at line 2667 with `private void saveReport(String reason) {` — pulls all that code back inside the class.

**Next step**: rebuild and verify. If clean, install to tablet.

---

## Install requirements

- User MUST be present to tap Play Protect dialog on K12 tablet
- ADB auth is not sticky — device prompt must be accepted each session
- `adb install -r -d` flags for debug build replacement
- APK path: `app/build/outputs/apk/debug/karr-defense-inspector-debug.apk`

---

## Art assets

- `app/src/main/res/drawable-nodpi/racer_zero_splash.png` — ANIMAE splash
- `app/src/main/res/drawable-nodpi/racer_zero_splash_fantasy.png` — ANIMAE fantasy variant
- `app/src/main/res/drawable-nodpi/racer_zero_assistant.png` — ANIMAE assistant image

---

## Mercedes VDP fields (Disclosure Report Mode)

Product identifier, hardware model, software version, firmware version, affected component/system, communication protocol, vulnerability class, severity, reporter contact, date discovered, status, VIN, model year, trim, market/region, observed behavior, reproduction steps, impact hypothesis, evidence, disclosure timeline.

---

## Safety constraints (enforced in app)

- Passive BLE scan only — no GATT writes, no control commands
- Wi-Fi scan metadata only — no packet injection
- Customer must actively participate in remediation (operator guides, customer acts)
- Defensive/authorized inspection only
