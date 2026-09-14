package com.codex.karrdefense;

import android.net.wifi.ScanResult;
import android.text.TextUtils;
import java.nio.charset.StandardCharsets;

public class WifiDecoder {
    private final ScanResult result;

    public WifiDecoder(ScanResult result) {
        this.result = result;
    }

    public String ssid() {
        if (result == null) return "";
        String s = result.SSID;
        return TextUtils.isEmpty(s) ? "(hidden SSID)" : s;
    }

    public String bssid() {
        if (result == null) return "";
        return result.BSSID;
    }

    public int rssi() {
        if (result == null) return 0;
        return result.level;
    }

    public String rawCapabilities() {
        if (result == null) return "";
        return result.capabilities;
    }

    public boolean isOpen() {
        String caps = rawCapabilities();
        if (TextUtils.isEmpty(caps)) return true;
        String upper = caps.toUpperCase();
        return !upper.contains("WEP") && !upper.contains("WPA") && !upper.contains("RSN")
                && !upper.contains("WPA2") && !upper.contains("WPA3")
                && !upper.contains("802.11W") && !upper.contains("SAE")
                && !upper.contains("CCMP") && !upper.contains("TKIP")
                && !upper.contains("WPA2-") && !upper.contains("WPA3-");
    }

    public boolean isWep() {
        String caps = rawCapabilities();
        return !TextUtils.isEmpty(caps) && caps.toUpperCase().contains("WEP");
    }

    public boolean isWpa() {
        String caps = rawCapabilities();
        String upper = caps.toUpperCase();
        return upper.contains("WPA");
    }

    public boolean isWpa2() {
        String caps = rawCapabilities();
        String upper = caps.toUpperCase();
        return upper.contains("WPA2") || upper.contains("RSN");
    }

    public boolean isWpa3() {
        String caps = rawCapabilities();
        String upper = caps.toUpperCase();
        return upper.contains("WPA3") || upper.contains("SAE");
    }

    public boolean isWmm() {
        String caps = rawCapabilities();
        return !TextUtils.isEmpty(caps) && caps.toUpperCase().contains("WMM");
    }

    public boolean isDtim() {
        String caps = rawCapabilities();
        return !TextUtils.isEmpty(caps) && caps.toUpperCase().contains("DTIM");
    }

    public boolean isPreambleShort() {
        String caps = rawCapabilities();
        return !TextUtils.isEmpty(caps) && caps.toUpperCase().contains("SHORT");
    }

    public boolean isPrivacyEnabled() {
        return !isOpen();
    }

    public int channel() {
        if (result == null) return -1;
        int freq = result.frequency;
        if (freq < 2400) return -1;
        if (freq < 2484) {
            return (freq - 2407) / 5 + 1;
        }
        if (freq < 2600) {
            return 11;
        }
        if (freq < 2712) {
            return (int) ((freq - 2412) / 20.0);
        }
        if (freq < 5825) {
            int ch = (int) ((freq - 5000) / 5.0);
            if (ch >= 1 && ch <= 165) return ch;
        }
        return -1;
    }

    public int frequency() {
        if (result == null) return -1;
        return result.frequency;
    }

    public String channelInfo() {
        int ch = channel();
        if (ch < 1) return "unknown channel/freq";
        return "ch " + ch + " / " + frequency() + " MHz";
    }

    public String securitySummary() {
        if (isOpen()) return "Open (no encryption)";
        if (isWep()) return "WEP (obsolete, vulnerable)";
        if (isWpa3()) return "WPA3";
        if (isWpa2()) return "WPA2";
        if (isWpa()) return "WPA";
        return "WPA-family (unspecified)";
    }

    public String hiddenFlag() {
        return TextUtils.isEmpty(result.SSID) ? "HIDDEN" : "visible";
    }

    public String openFlag() {
        return isOpen() ? "OPEN" : "encrypted";
    }

    public String wepFlag() {
        return isWep() ? "WEP" : "-";
    }

    public String wpaFlag() {
        return isWpa() ? "WPA" : "-";
    }

    public String wpa2Flag() {
        return isWpa2() ? "WPA2" : "-";
    }

    public String wpa3Flag() {
        return isWpa3() ? "WPA3" : "-";
    }

    public String detailString() {
        return "SSID: " + ssid() + "\n"
                + "BSSID: " + bssid() + "\n"
                + "RSSI: " + rssi() + " dBm\n"
                + "Channel/Freq: " + channelInfo() + "\n"
                + "Security: " + securitySummary() + "\n"
                + "Flags: Hidden=" + hiddenFlag() + " | Open=" + openFlag()
                + " | WEP=" + wepFlag() + " | WPA=" + wpaFlag()
                + " | WPA2=" + wpa2Flag() + " | WPA3=" + wpa3Flag()
                + " | WMM=" + isWmm() + " | W11w=" + isPreambleShort()
                + "\n"
                + "OUI/Vendor: " + OuiLookup.lookup(bssid()) + "\n"
                + "Capabilities raw: " + rawCapabilities() + "\n"
                + "Note: Passive Wi-Fi scan only. No association attempted.";
    }

    public static String capabilitiesFlags(String caps) {
        if (TextUtils.isEmpty(caps)) return "(none)";
        StringBuilder b = new StringBuilder();
        String upper = caps.toUpperCase();
        addFlag(b, upper, "WIP", "WIP (Wi-Fi Protected Setup)");
        addFlag(b, upper, "WMM", "WMM (QoS)");
        addFlag(b, upper, "DSSS", "DSSS (DSSS/OFDM)");
        addFlag(b, upper, "SHORT", "SHORT PREAMBLE");
        addFlag(b, upper, "PTK", "PTK");
        addFlag(b, upper, "GTK", "GTK");
        addFlag(b, upper, "MFP", "MFP (Management Frame Protection)");
        if (upper.contains("WEP")) addFlag(b, upper, "WEP", "WEP");
        if (upper.contains("WPA")) addFlag(b, upper, "WPA", "WPA");
        if (upper.contains("WPA2") || upper.contains("RSN")) addFlag(b, upper, "WPA2/RSN", "WPA2/RSN");
        if (upper.contains("WPA3") || upper.contains("SAE")) addFlag(b, upper, "WPA3/SAE", "WPA3/SAE");
        addFlag(b, upper, "802.11W", "802.11w mgmt frame protection");
        return b.length() == 0 ? "(no parsed flags)" : b.toString();
    }

    private static void addFlag(StringBuilder b, String upper, String key, String label) {
        if (upper.contains(key)) {
            if (b.length() > 0) b.append(", ");
            b.append(label);
        }
    }
}
