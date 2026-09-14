package com.codex.karrdefense;

import android.text.TextUtils;
import java.util.List;

public class KarrSwdsResearchPanel {

    public static String panelText(KarrSwdsResearchState state, MainActivity.BleObservation obs,
                                    List<TargetHistory.RssiPoint> history) {
        StringBuilder b = new StringBuilder();
        b.append("KARR/SWDS RESEARCH PANEL\n");
        b.append("==========================\n\n");
        b.append("Keyword match: ").append(state.matchedKeyword).append("\n");
        b.append("Keyword hits: ").append(state.keywordMatchCount).append("\n");
        b.append("Location in data:\n");
        b.append("  Name: ").append(state.matchedInName ? "YES" : "no").append("\n");
        b.append("  MAC address: ").append(state.matchedInAddress ? "YES" : "no").append("\n");
        b.append("  Vendor/advertisement data: ").append(state.matchedInVendorData ? "YES" : "no").append("\n\n");
        b.append("Why this matched:\n");
        b.append("  - "); appendIf(b, "karr", obs); b.append("\n");
        b.append("  - "); appendIf(b, "swds", obs); b.append("\n");
        b.append("  - "); appendIf(b, "southwest", obs); b.append("\n");
        b.append("  - "); appendIf(b, "south west", obs); b.append("\n");
        b.append("  - "); appendIf(b, "acrisure", obs); b.append("\n");
        b.append("\n");
        b.append("Significance: KARR/SWDS is a known anti-theft module with a\n");
        b.append("shared Bluetooth authentication key vulnerability (UCSD/WIRED).\n");
        b.append("Acrisure released a firmware update (~July 2026). This detection\n");
        b.append("is a candidate flag — not a confirmed vulnerable device.\n");
        b.append("Authorized verification required before any determination.\n\n");
        b.append("Detection tier: keyword (Tier 1). Upgrade to service UUID /\n");
        b.append("manufacturer-data fingerprinting (Tier 2) once firmware analysis\n");
        b.append("of patched vs unpatched KARR/SWDS is available.\n");
        return b.toString();
    }

    private static void appendIf(StringBuilder b, String keyword, MainActivity.BleObservation obs) {
        if (TextUtils.isEmpty(keyword)) return;
        String text = obs.searchableText().toLowerCase();
        if (text.contains(keyword)) {
            b.append("'").append(keyword).append("' found in flattened advertisement data");
        } else {
            b.append("'").append(keyword).append("' not matched");
        }
    }

    public static String observationResearchText(KarrSwdsResearchState state, MainActivity.BleObservation obs,
                                                 List<TargetHistory.RssiPoint> history) {
        StringBuilder b = new StringBuilder();
        b.append("DETECTION RESEARCH\n");
        b.append("==================\n\n");
        if (state.matchedKeyword == null) {
            b.append("No KARR/SWDS keyword matched. This candidate is not flagged\n");
            b.append("by the KARR/SWDS detector rule.\n");
        } else {
            b.append("Matched keyword: ").append(state.matchedKeyword).append("\n");
            b.append("Keyword hits: ").append(state.keywordMatchCount).append("\n\n");
            b.append("Where the match occurred:\n");
            b.append("  Name field: ").append(state.matchedInName ? "YES" : "no").append("\n");
            b.append("  MAC address field: ").append(state.matchedInAddress ? "YES" : "no").append("\n");
            b.append("  Vendor/advertisement data: ").append(state.matchedInVendorData ? "YES" : "no").append("\n\n");

            b.append("Raw advertisement payload:\n");
                b.append(obs.rawHex());

            if (obs.raw.length > 0) {
                b.append("\n\nPrintable ASCII from advertisement:\n");
                b.append(obs.printableAscii());
                b.append("\n\n");
            }

            BleDecoder bDec = new BleDecoder(obs.raw);
            b.append("  ").append(bDec.manufacturerIdString()).append("\n\n");

            b.append("Service UUIDs (16-bit): ").append(bDec.serviceUUIDs16String()).append("\n");
            b.append("Service UUIDs (32-bit): ").append(bDec.serviceUUIDs32String()).append("\n\n");
            int tpv = bDec.txPowerValue();
            b.append("Tx power: ").append(tpv >= 0 ? tpv + " dBm" : "not present").append("\n\n");

            b.append("RSSI history:\n");
            if (history != null && !history.isEmpty()) {
                for (TargetHistory.RssiPoint p : history) {
                    b.append("  ").append(p.time).append("  ").append(p.rssi).append(" dBm\n");
                }
                b.append("\nRange: ").append(history.isEmpty() ? 0 : history.get(0).rssi).append(" to ").append(history.isEmpty() ? 0 : history.get(history.size()-1).rssi).append(" dBm\n");
                b.append("Readings: ").append(history.size()).append("\n");
            } else {
                b.append("  No RSSI history recorded for this session.\n");
            }
            b.append("\n");
            b.append("Confidence: medium (keyword match only).\n");
            b.append("Action: candidate for authorized verification.\n");
        }
        return b.toString();
    }

    public static String bleedThroughPanelText(KarrSwdsResearchState state, MainActivity.BleObservation obs,
                                               List<TargetHistory.RssiPoint> history) {
        return panelText(state, obs, history);
    }
}
