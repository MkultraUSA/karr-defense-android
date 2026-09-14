package com.codex.karrdefense;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.text.TextUtils;

public class Util {
    public static String now() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).format(new Date());
    }

    public static String nowShort() {
        return new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
    }

    public static String safe(String value) {
        return value == null ? "" : value;
    }

    public static String json(String value) {
        return "\"" + safe(value)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r") + "\"";
    }

    public static String hex(byte[] data) {
        StringBuilder b = new StringBuilder();
        for (byte v : data) {
            b.append(String.format(Locale.US, "%02x", v & 0xff));
        }
        return b.toString();
    }

    public static String printableAscii(byte[] data) {
        StringBuilder b = new StringBuilder();
        for (byte v : data) {
            int val = v & 0xff;
            if (val >= 32 && val <= 126) {
                b.append((char) val);
            } else {
                b.append(' ');
            }
        }
        return b.toString();
    }

    public static String truncate(String value, int maxChars) {
        if (value == null || value.length() <= maxChars) return safe(value);
        return value.substring(0, maxChars) + "...";
    }

    public static String shortAddress(String value) {
        if (TextUtils.isEmpty(value) || value.length() <= 5) return value;
        return value.substring(value.length() - 5);
    }

    public static String displaySsid(String ssid) {
        return TextUtils.isEmpty(ssid) ? "(hidden SSID)" : ssid;
    }
}
