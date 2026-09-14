# KARR Defense Android Inspector

Tablet-first defensive vehicle security inspection app for the Android K12.

Scope:
- Passive BLE advertisement scanning.
- Wi-Fi surface inventory.
- Evidence JSONL logging.
- Pluggable detector rules for researched, known issues.
- No vehicle-control functions and no BLE write/control commands.

The first active rule is KARR/SWDS BLE keyword detection, carried forward from the CYD firmware research. Wi-Fi scanning is implemented as collection infrastructure only until specific vehicle Wi-Fi vulnerability signatures are researched and documented.

## Vehicle audit (v0.5)

The inspector doubles as a field vehicle-audit tool for cars, trucks and
delivery/autonomous robots:

- **Start Scan** runs passive BLE + Wi-Fi discovery from the main screen.
- **Scan Results** lists every discovered device with type, name, MAC/BSSID and
  RSSI, plus a passive car / truck / robot classification heuristic.
- Tapping a device opens **Device Detail** with the OUI vendor and a
  **Document Finding** button.
- **Document Finding** records an issue category, a severity and free-form
  notes, and saves the record to a local SQLite database
  (`AuditDatabase` / `karr_vehicle_audit.db`).
- **Documented Findings** lists every saved record straight from the database.

Documented findings are also appended to the session evidence JSONL, so the
on-tablet database and the exported evidence agree. No new permissions are
required: BLE and Wi-Fi discovery reuse the passive permissions already
declared in `AndroidManifest.xml`.

Build:

```powershell
.\build-manual.ps1
```

The debug APK is written to:

```text
app\build\outputs\apk\debug\karr-defense-inspector-debug.apk
```
