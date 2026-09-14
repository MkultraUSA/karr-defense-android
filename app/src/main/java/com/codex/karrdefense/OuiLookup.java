package com.codex.karrdefense;

import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.Map;

public class OuiLookup {

    private static final Map<String, String> VENDORS = new LinkedHashMap<>();

    static {
        // Curated chipset / vendor OUI assignments.
        // Expand with the IEEE OUI listing for OEM-specific MAC blocks.
        put("00:1A:7D", "Apple, Inc.");
        put("3C:22:F2", "Apple, Inc.");
        put("A4:34:D9", "Apple, Inc.");
        put("5C:53:54", "Apple, Inc.");
        put("FC:F8:AE", "Apple, Inc.");
        put("D4:6D:6E", "Apple, Inc.");
        put("40:C8:D5", "Apple, Inc.");
        put("7C:BA:9C", "Google, Inc.");
        put("30:F8:D9", "Google, Inc. (Nest)");
        put("00:26:5A", "Tesla, Inc.");
        put("00:50:49", "NXP Semiconductors");
        put("00:14:D1", "NXP Semiconductors");
        put("00:20:E5", "NXP Semiconductors");
        put("00:60:37", "Microchip Technology");
        put("00:3A:94", "Microchip Technology (legacy)");
        put("00:1E:7E", "STMicroelectronics");
        put("00:E0:4C", "Broadcom Corporation");
        put("00:23:A2", "Broadcom Corporation");
        put("00:1A:A0", "Broadcom Corporation");
        put("00:50:F2", "Broadcom (accessory/HW)");
        put("00:1D:A1", "Broadcom Corporation");
        put("00:27:10", "Broadcom Corporation");
        put("00:18:39", "Marvell Technology Group");
        put("00:24:0B", "Texas Instruments");
        put("00:1E:68", "Renesas Electronics");
        put("00:17:F2", "Sony Corporation");
        put("00:18:28", "Samsung Electronics");
        put("00:19:5B", "Realtek Semiconductor");
        put("00:25:5D", "MediaTek Inc.");
        put("00:24:D6", "Atheros Communications");
        put("00:19:B3", "Atheros Communications");
        put("00:1E:98", "Atheros Communications");
        put("00:25:84", "Atheros Communications");
        put("00:26:F2", "Atheros Communications");
        put("00:40:9C", "Atheros Communications");
        put("00:21:5A", "Ralink Technology (MediaTek)");
        put("00:26:55", "Ralink Technology (MediaTek)");
        put("00:12:1C", "Cypress Semiconductor");
        put("00:1A:22", "Infineon Technologies (Cypress)");
        put("00:1B:21", "Infineon Technologies");
        put("00:21:26", "Intel Corporation");
        put("00:30:4F", "Broadcom Corporation (legacy)");
        put("00:1F:44", "Qualcomm Atheros");
        put("00:24:2D", "Foxconn");
    }

    public static String lookup(String bssid) {
        if (bssid == null) return "Unknown";
        String[] parts = bssid.split(":");
        if (parts.length < 3) {
            parts = bssid.split("[-.]");
        }
        if (parts.length < 3) return "Unknown";
        StringBuilder oui = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (i > 0) oui.append(':');
            oui.append(parts[i].toUpperCase(Locale.US));
        }
        String key = oui.toString();
        String vendor = VENDORS.get(key);
        return vendor != null ? vendor : "Unknown";
    }

    public static int vendorCount() {
        return VENDORS.size();
    }

    private static void put(String oui, String vendor) {
        VENDORS.put(oui, vendor);
    }
}
