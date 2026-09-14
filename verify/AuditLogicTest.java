package com.codex.karrdefense;

/**
 * Plain-JVM verification harness for the audit app's Android-free logic.
 *
 * Runs on the build host with no emulator: exercises VehicleClassifier (the
 * car/truck/robot heuristic behind the scan-result labels) and AuditFinding's
 * evidence-JSON rendering, which is the record shape persisted alongside the
 * SQLite row. Both are pure Java, so this is a real execution of the shipped
 * code, not a re-implementation.
 */
public final class AuditLogicTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        // --- VehicleClassifier: robot beats truck beats car, unknown otherwise ---
        eq("starship delivery robot #12", null, VehicleClassifier.ROBOT);
        eq("Kiwibot sidewalk unit", null, VehicleClassifier.ROBOT);
        eq("F-150 fleet truck", null, VehicleClassifier.TRUCK);
        eq("box truck beacon", null, VehicleClassifier.TRUCK);
        eq("Tesla Model 3 media", null, VehicleClassifier.CAR);
        eq("uconnect head unit", null, VehicleClassifier.CAR);
        eq("random-ble-beacon", null, VehicleClassifier.UNKNOWN);
        eq(null, null, VehicleClassifier.UNKNOWN);

        // Case-insensitive: advertisements are lower-cased before matching.
        eq("STARSHIP ROBOT", null, VehicleClassifier.ROBOT);

        // Classification searches the vendor/OUI text too, via searchableText.
        eq("", "OnStar", VehicleClassifier.UNKNOWN); // vendor alone is not searched (MVP)

        // A device advertising both robot and car wording is labelled robot.
        eq("autonomous car", null, VehicleClassifier.ROBOT);

        // --- Labels rendered in the UI ---
        eq(VehicleClassifier.badge(VehicleClassifier.CAR), "CAR?");
        eq(VehicleClassifier.badge(VehicleClassifier.ROBOT), "ROBOT?");
        eq(VehicleClassifier.badge(VehicleClassifier.UNKNOWN), "--");
        ok("describe(ROBOT) mentions robot",
                VehicleClassifier.describe(VehicleClassifier.ROBOT).toLowerCase().contains("robot"));
        ok("describe(UNKNOWN) is the fallback",
                VehicleClassifier.describe(VehicleClassifier.UNKNOWN).contains("Unclassified"));

        // --- AuditFinding evidence JSON: same shape as the SQLite columns ---
        AuditFinding finding = new AuditFinding(7L, "2026-09-14T16:00:00.000+0000",
                "session-1", "ble", "AA:BB:CC:DD:EE:FF", "Robot \"3\"", -62,
                "unauthenticated control", "high", "line1\nline2");
        String json = finding.toJson();
        ok("finding json type tag", json.startsWith("{\"type\":\"documented_finding\""));
        ok("finding json id", json.contains("\"id\":7"));
        ok("finding json rssi", json.contains("\"rssi\":-62"));
        ok("finding json escapes quotes", json.contains("Robot \\\"3\\\""));
        ok("finding json escapes newline", json.contains("line1\\nline2"));
        ok("finding json severity", json.contains("\"severity\":\"high\""));
        ok("finding json closes", json.endsWith("}"));

        System.out.println("AuditLogicTest: " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.out.println("AUDIT_LOGIC_FAILED");
            System.exit(1);
        }
        System.out.println("AUDIT_LOGIC_OK");
    }

    private static void eq(String input, String vendor, String expected) {
        String actual = VehicleClassifier.classify(input, vendor);
        ok("classify(" + input + ") == " + expected, expected.equals(actual));
    }

    private static void eq(String actual, String expected) {
        ok("equals(" + actual + ", " + expected + ")", expected.equals(actual));
    }

    private static void ok(String what, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  ok   " + what);
        } else {
            failed++;
            System.out.println("  FAIL " + what);
        }
    }
}