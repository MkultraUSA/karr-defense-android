package com.codex.karrdefense;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;

public class RuleIdToResearchText {

    public static String researchTextForRuleId(String ruleId, MainActivity.BleObservation bleObs,
                                                 MainActivity.WifiObservation wifiObs) {
        if (TextUtils.isEmpty(ruleId)) return "";
        if ("known_ble_karr_swds_keyword".equals(ruleId)) {
            return KarrSwdsResearchPanel.observationResearchText(
                    buildKarrState(bleObs), bleObs,
                    null);
        }
        if ("known_wifi_wep_network".equals(ruleId)) {
            if (!(wifiObs instanceof MainActivity.WifiObservation)) return "";
            MainActivity.WifiObservation w = (MainActivity.WifiObservation) wifiObs;
            return "RESEARCH — WEP WI-FI DETECTED\n"
                    + "================================\n\n"
                    + "Detected a Wi-Fi network (" + w.ssid + ") advertising WEP security.\n\n"
                    + "WHAT WEP IS:\n"
                    + "  Wired Equivalent Privacy (WEP) is an obsolete, broken Wi-Fi\n"
                    + "  encryption protocol. Attackers can capture traffic and recover\n"
                    + "  the key in minutes.\n\n"
                    + "WHY THIS MATTERS FOR VEHICLES:\n"
                    + "  - In-vehicle Wi-Fi hotspots with WEP expose occupants and\n"
                    + "    connected systems to traffic interception.\n"
                    + "  - Many dealer-installed hotspots default to weak or open\n"
                    + "    security.\n\n"
                    + "CONFIDENCE: HIGH for the WEP signature; impact depends on\n"
                    + "whether the network is a vehicle system, customer hotspot,\n"
                    + "or unrelated nearby network.\n\n"
                    + "NEXT STEP: Confirm ownership / authorization before raising\n"
                    + "with the vehicle owner. Recommend WPA2/WPA3 migration.\n";
        }
        if ("known_wifi_open_network".equals(ruleId)) {
            if (!(wifiObs instanceof MainActivity.WifiObservation)) return "";
            MainActivity.WifiObservation w = (MainActivity.WifiObservation) wifiObs;
            return "RESEARCH — OPEN WI-FI NETWORK DETECTED\n"
                    + "==========================================\n\n"
                    + "Detected an open (unencrypted) Wi-Fi network: " + w.ssid + "\n\n"
                    + "WHAT OPEN MEANS:\n"
                    + "  No encryption — any nearby device can see all traffic and\n"
                    + "  attempt to associate if the AP allows it.\n\n"
                    + "WHY THIS MATTERS FOR VEHICLES:\n"
                    + "  - In-vehicle Wi-Fi hotspots or telematics with open SSIDs\n"
                    + "    expose occupants and any bridged systems.\n"
                    + "  - Misconfigured customer hotspots are a common field finding.\n\n"
                    + "CONFIDENCE: HIGH for the open-network signature; impact\n"
                    + "depends on ownership and whether the network is vehicle-hosted.\n\n"
                    + "NEXT STEP: Confirm ownership / authorization. Recommend\n"
                    + "enabling WPA2/WPA3.\n";
        }
        return "RESEARCH NOTE\n"
                + "================\n\n"
                + "Rule triggered: " + ruleId + "\n";
    }

    private static KarrSwdsResearchState buildKarrState(MainActivity.BleObservation bleObs) {
        if (bleObs == null) return new KarrSwdsResearchState(null, 0, false, false, false);
        String text = bleObs.searchableText().toLowerCase();
        String kw = null;
        int count = 0;
        boolean inName = false, inAddr = false, inVendor = false;
        if (text.contains("karr")) { kw = "karr"; count++; inName = true; }
        if (text.contains("swds")) { kw = "swds"; count++; inName = true; }
        if (text.contains("southwest")) { kw = "southwest"; count++; inName = true; }
        if (text.contains("south west")) { kw = "south west"; count++; inName = true; }
        if (text.contains("acrisure")) { kw = "acrisure"; count++; inName = true; }
        return new KarrSwdsResearchState(kw, count, inName, inAddr, inVendor);
    }
}
