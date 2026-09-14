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

    /**
     * JSON-escape: backslash, double-quote, and all C0 control chars.
     * Produces valid JSONL. Handles TAB and other control chars (H2 fix).
     */
    public static String json(String value) {
        String s = safe(value);
        StringBuilder sb = new StringBuilder(s.length() * 2 + 2);
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' || c == '\\') {
                sb.append('\\');
                sb.append(c);
            } else if (c <= 0x1f) {
                sb.append('\\');
                sb.append('u');
                sb.append(hexDigit((c >> 12) & 0xf));
                sb.append(hexDigit((c >> 8) & 0xf));
                sb.append(hexDigit((c >> 4) & 0xf));
                sb.append(hexDigit(c & 0xf));
            } else {
                sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }

    private static char hexDigit(int n) {
        return (char) (n < 10 ? '0' + n : 'a' + (n - 10));
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
