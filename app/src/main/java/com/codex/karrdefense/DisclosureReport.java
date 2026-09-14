package com.codex.karrdefense;

import android.text.TextUtils;

public class DisclosureReport {

    public enum Vendor {
        MERCEDES_BENZ,
        GENERIC
    }

    public static class ReportFields {
        public String vendor = "Unknown";
        public String affectedSystem = "";
        public String observedBehavior = "";
        public String reproductionSteps = "";
        public String impactHypothesis = "";
        public String evidence = "";
        public String productIdentifier = "";
        public String hwModel = "";
        public String swVersion = "";
        public String firmwareVersion = "";
        public String communicationProtocol = "";
        public String vulnerabilityClass = "";
        public String severity = "Undetermined";
        public String reporterContact = "";
        public String dateDiscovered = "";
        public String status = "Draft";
    }

    public static String genericVdpTemplate(ReportFields fields) {
        StringBuilder b = new StringBuilder();
        b.append("══════════════════════════════════════════════════════════════\n");
        b.append("  VDP DISCLOSURE REPORT — DRAFT\n");
        b.append("  Vendor: ").append(fields.vendor).append("\n");
        b.append("  Product: ").append(fields.productIdentifier).append("\n");
        b.append("  Date discovered: ").append(fields.dateDiscovered).append("\n");
        b.append("  Status: ").append(fields.status).append("\n");
        b.append("══════════════════════════════════════════════════════════════\n\n");

        b.append("1. AFFECTED SYSTEM\n");
        b.append("──────────────────────────────────────────────────────────────\n");
        b.append(fields.affectedSystem).append("\n\n");

        b.append("2. OBSERVED BEHAVIOR\n");
        b.append("──────────────────────────────────────────────────────────────\n");
        b.append(fields.observedBehavior).append("\n\n");

        b.append("3. REPRODUCTION STEPS\n");
        b.append("──────────────────────────────────────────────────────────────\n");
        b.append(fields.reproductionSteps).append("\n\n");

        b.append("4. IMPACT HYPOTHESIS\n");
        b.append("──────────────────────────────────────────────────────────────\n");
        b.append(fields.impactHypothesis).append("\n\n");

        b.append("5. EVIDENCE\n");
        b.append("──────────────────────────────────────────────────────────────\n");
        b.append(fields.evidence).append("\n\n");

        b.append("6. CONFIGURATION AT TIME OF OBSERVATION\n");
        b.append("──────────────────────────────────────────────────────────────\n");
        b.append("  Product identifier: ").append(fields.productIdentifier).append("\n");
        b.append("  Hardware model: ").append(fields.hwModel).append("\n");
        b.append("  Software version: ").append(fields.swVersion).append("\n");
        b.append("  Firmware version: ").append(fields.firmwareVersion).append("\n");
        b.append("  Communication protocol: ").append(fields.communicationProtocol).append("\n\n");

        b.append("7. CLASSIFICATION\n");
        b.append("──────────────────────────────────────────────────────────────\n");
        b.append("  Vulnerability class: ").append(fields.vulnerabilityClass).append("\n");
        b.append("  Severity (draft): ").append(fields.severity).append("\n");
        b.append("  Reporter: ").append(fields.reporterContact).append("\n\n");

        b.append("──────────────────────────────────────────────────────────────\n");
        b.append("  DRAFT — NOT FOR PUBLIC DISCLOSURE UNTIL VENDOR ACKNOWLEDGED\n");
        b.append("──────────────────────────────────────────────────────────────\n");
        return b.toString();
    }

    public static String mercedesVdpTemplate(ReportFields fields) {
        StringBuilder b = new StringBuilder();
        b.append("══════════════════════════════════════════════════════════════\n");
        b.append("  MERCEDES-BENZ VDP DISCLOSURE REPORT — DRAFT\n");
        b.append("  Product: ").append(fields.productIdentifier).append("\n");
        b.append("  Date discovered: ").append(fields.dateDiscovered).append("\n");
        b.append("  Status: ").append(fields.status).append("\n");
        b.append("══════════════════════════════════════════════════════════════\n\n");

        b.append("1. AFFECTED VEHICLE / COMPONENT\n");
        b.append("──────────────────────────────────────────────────────────────\n");
        b.append("  Product / model: ").append(fields.productIdentifier).append("\n");
        b.append("  Hardware model: ").append(fields.hwModel).append("\n");
        b.append("  Software version: ").append(fields.swVersion).append("\n");
        b.append("  Firmware version: ").append(fields.firmwareVersion).append("\n");
        b.append("  Affected component: ").append(fields.affectedSystem).append("\n\n");

        b.append("2. VEHICLE CONTEXT (fill in after owner consent)\n");
        b.append("──────────────────────────────────────────────────────────────\n");
        b.append("  VIN: _______________________________________________\n");
        b.append("  Model year: ______\n");
        b.append("  Trim: _______________________________________________\n");
        b.append("  Market / region: ___________________________________\n\n");

        b.append("3. OBSERVED BEHAVIOR\n");
        b.append("──────────────────────────────────────────────────────────────\n");
        b.append(fields.observedBehavior).append("\n\n");

        b.append("4. REPRODUCTION STEPS\n");
        b.append("──────────────────────────────────────────────────────────────\n");
        b.append(fields.reproductionSteps).append("\n\n");

        b.append("5. IMPACT HYPOTHESIS\n");
        b.append("──────────────────────────────────────────────────────────────\n");
        b.append(fields.impactHypothesis).append("\n\n");

        b.append("6. EVIDENCE\n");
        b.append("──────────────────────────────────────────────────────────────\n");
        b.append(fields.evidence).append("\n\n");

        b.append("7. TECHNICAL DETAILS\n");
        b.append("──────────────────────────────────────────────────────────────\n");
        b.append("  Affected system: ").append(fields.affectedSystem).append("\n");
        b.append("  Communication protocol: ").append(fields.communicationProtocol).append("\n");
        b.append("  Vulnerability class: ").append(fields.vulnerabilityClass).append("\n");
        b.append("  Severity (draft): ").append(fields.severity).append("\n\n");

        b.append("8. REPORTING / DISCLOSURE CHANNEL (Mercedes-Benz)\n");
        b.append("──────────────────────────────────────────────────────────────\n");
        b.append("  Vendor: Mercedes-Benz Group AG\n");
        b.append("  Product Security / Vulnerability Disclosure:\n");
        b.append("    https://www.mercedes-benz-tech-innovation.com/en/security\n");
        b.append("  Submissions: typically via the vendor's coordinated disclosure\n");
        b.append("    process; confirm current channel before sending.\n\n");

        b.append("9. REPORTER INFORMATION\n");
        b.append("──────────────────────────────────────────────────────────────\n");
        b.append("  Reporter: ").append(fields.reporterContact).append("\n");
        b.append("  Contact: _______________________________________________\n");
        b.append("  Preferred disclosure timeline: ___________________________________\n\n");

        b.append("──────────────────────────────────────────────────────────────\n");
        b.append("  DRAFT — DO NOT DISCLOSE PUBLICLY BEFORE VENDOR ACKNOWLEDGED\n");
        b.append("  Mercedes-Benz coordinated disclosure process applies.\n");
        b.append("──────────────────────────────────────────────────────────────\n");
        return b.toString();
    }

    public static ReportFields mercedesFieldsPreFilled(String targetId, String summary, String rawEvidence) {
        ReportFields f = new ReportFields();
        f.vendor = "Mercedes-Benz";
        f.productIdentifier = targetId;
        f.affectedSystem = "Pending — observed " + summary;
        f.observedBehavior = "Pending — recorded " + summary;
        f.reproductionSteps = "Pending — capture methodology:\n"
                + "  1. Passive BLE/Wi-Fi scan with Field Security Inspector (K12 tablet)\n"
                + "  2. Recorded advertisement + scan metadata; no GATT writes\n"
                + "  3. Evidence packet archived from this session\n";
        f.impactHypothesis = "Pending — assess against known vehicle cybersecurity concerns.\n"
                + "  Note: KARR/SWDS is a researched case. Do not assume impact\n"
                + "  without opposite-party verification or documented signature.\n";
        f.evidence = rawEvidence;
        f.dateDiscovered = Util.now();
        f.status = "Draft";
        f.reporterContact = "Field Security Inspector operator (pending owner authorization)";
        return f;
    }

    public static String mercedesVdpFromObservation(String targetId, String observationSummary,
                                                     String evidenceText, ReportFields prefs) {
        ReportFields f = (prefs == null) ? new ReportFields() : prefs;
        if (TextUtils.isEmpty(f.vendor)) f.vendor = "Mercedes-Benz";
        if (TextUtils.isEmpty(f.productIdentifier)) f.productIdentifier = targetId;
        if (TextUtils.isEmpty(f.affectedSystem))
            f.affectedSystem = "Observed " + observationSummary;
        if (TextUtils.isEmpty(f.observedBehavior))
            f.observedBehavior = "Passive scan observed " + observationSummary;
        if (TextUtils.isEmpty(f.reproductionSteps))
            f.reproductionSteps = "Field Security Inspector passive scan (no GATT writes).\n"
                    + "Evidence packet archived from session.";
        if (TextUtils.isEmpty(f.impactHypothesis))
            f.impactHypothesis = "To be determined after vendor review of evidence.";
        if (TextUtils.isEmpty(f.evidence)) f.evidence = evidenceText;
        if (TextUtils.isEmpty(f.dateDiscovered)) f.dateDiscovered = Util.now();
        return mercedesVdpTemplate(f);
    }
}
