# KARR Defense Android Inspector

Tablet-first defensive vehicle security inspection app for the Android K12.

Scope:
- Passive BLE advertisement scanning.
- Wi-Fi surface inventory.
- Evidence JSONL logging.
- Pluggable detector rules for researched, known issues.
- No vehicle-control functions and no BLE write/control commands.

The first active rule is KARR/SWDS BLE keyword detection, carried forward from the CYD firmware research. Wi-Fi scanning is implemented as collection infrastructure only until specific vehicle Wi-Fi vulnerability signatures are researched and documented.

Build:

```powershell
.\build-manual.ps1
```

The debug APK is written to:

```text
app\build\outputs\apk\debug\karr-defense-inspector-debug.apk
```
