package com.codex.karrdefense;

import android.text.TextUtils;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class BleDecoder {

    private final byte[] payload;
    private final int offset;
    private final int length;

    public BleDecoder(byte[] payload) {
        this.payload = payload == null ? new byte[0] : payload;
        this.offset = 0;
        this.length = this.payload.length;
    }

    public String rawHex() {
        return Util.hex(payload);
    }

    public String printableAscii() {
        return Util.printableAscii(payload);
    }

    public String localName() {
        if (length < 2) return "";
        int next = offset;
        if (next >= length) return "";
        int len = payload[next] & 0xff;
        if (len == 0) return "";
        if (next + 1 + len > length) return "";
        return new String(payload, next + 1, len, StandardCharsets.UTF_8);
    }

    public int localNameLength() {
        int next = offset;
        if (next >= length) return 0;
        int len = payload[next] & 0xff;
        if (len == 0) return 0;
        if (next + 1 + len > length) return 0;
        return len;
    }

    public boolean scanResponseHasAid() {
        if (length < 1) return false;
        return (payload[offset] & 0xff) != 0x09;
    }

    public String scanResponseName() {
        int next = offset;
        if (next >= length) return "";
        int len = payload[next] & 0xff;
        if (len == 0) return "";
        if (next + 1 + len > length) return "";
        return new String(payload, next + 1, len, StandardCharsets.UTF_8);
    }

    public int scanResponseNameLength() {
        int next = offset;
        if (next >= length) return 0;
        int len = payload[next] & 0xff;
        if (len == 0) return 0;
        if (next + 1 + len > length) return 0;
        return len;
    }

    public int localNameTypeFlag() {
        if (length < 1) return -1;
        return payload[offset] & 0xff;
    }

    public List<Uuid16> serviceUuids16() {
        List<Uuid16> list = new ArrayList<>();
        int pos = offset;
        while (pos + 2 <= length) {
            int len = payload[pos] & 0xff;
            if (len == 0) break;
            int type = payload[pos + 1] & 0xff;
            if (type == 0x03) {
                if (pos + 3 + 2 <= length) {
                    int u = ((payload[pos + 2] & 0xff) << 8) | (payload[pos + 3] & 0xff);
                    list.add(new Uuid16(String.format("%04X", u), u));
                }
            }
            pos += len + 1;
        }
        return list;
    }

    public String serviceUUIDs16String() {
        List<Uuid16> uuids = serviceUuids16();
        if (uuids.isEmpty()) return "(none in advertisement)";
        StringBuilder b = new StringBuilder();
        for (Uuid16 u : uuids) {
            if (b.length() > 0) b.append(", ");
            b.append(u.value);
        }
        return b.toString();
    }

    public List<Uuid32> serviceUuids32() {
        List<Uuid32> list = new ArrayList<>();
        int pos = offset;
        while (pos + 2 <= length) {
            int len = payload[pos] & 0xff;
            if (len == 0) break;
            int type = payload[pos + 1] & 0xff;
            if (type == 0x07) {
                if (pos + 3 + 4 <= length) {
                    long u = (((long) payload[pos + 2] & 0xff) << 24)
                            | (((long) payload[pos + 3] & 0xff) << 16)
                            | (((long) payload[pos + 4] & 0xff) << 8)
                            | ((long) payload[pos + 5] & 0xff);
                    list.add(new Uuid32(String.format("%08X", u), u));
                }
            }
            pos += len + 1;
        }
        return list;
    }

    public String serviceUUIDs32String() {
        List<Uuid32> uuids = serviceUuids32();
        if (uuids.isEmpty()) return "(none in advertisement)";
        StringBuilder b = new StringBuilder();
        for (Uuid32 u : uuids) {
            if (b.length() > 0) b.append(", ");
            b.append(u.value);
        }
        return b.toString();
    }

    public int txPowerFlag() {
        int pos = offset;
        int remaining = length - pos;
        while (remaining >= 2) {
            int len = payload[pos] & 0xff;
            if (len == 0) break;
            int type = payload[pos + 1] & 0xff;
            if (type == 0x0A) {
                if (pos + 2 <= length) {
                    return payload[pos + 2] & 0xff;
                }
            }
            pos += len + 1;
            remaining = length - pos;
        }
        return -1;
    }

    public int txPowerValue() {
        if (length >= 2 && (payload[offset] & 0xff) == 0x0A) {
            if (length >= 3) {
                return payload[offset + 2] & 0xff;
            }
        }
        return -1;
    }

    public int manufacturerDataStart() {
        int pos = offset;
        while (pos + 2 <= length) {
            int len = payload[pos] & 0xff;
            if (len == 0) break;
            int type = payload[pos + 1] & 0xff;
            if (type == 0xFF) {
                return pos + 1;
            }
            pos += len + 1;
        }
        return -1;
    }

    public int manufacturerId() {
        int start = manufacturerDataStart();
        if (start < 0) return -1;
        if (start + 3 > length) return -1;
        return (((payload[start] & 0xff) << 8) | (payload[start + 1] & 0xff));
    }

    public String manufacturerIdString() {
        int id = manufacturerId();
        if (id < 0) return "(none)";
        String hexId = String.format("%04X", id);
        String company = OuiLookup.lookup(hexId);
        return String.format("%04X  %s", id, company);
    }

    public byte[] manufacturerData() {
        int start = manufacturerDataStart();
        if (start < 0) return new byte[0];
        int adLen = payload[start] & 0xff;
        int dataStart = start + 2;
        if (dataStart + adLen - 2 > length) return new byte[0];
        int dataLen = adLen - 2;
        byte[] data = new byte[dataLen];
        System.arraycopy(payload, dataStart, data, 0, dataLen);
        return data;
    }

    public String serviceDataUuidsString() {
        int pos = offset;
        StringBuilder b = new StringBuilder();
        while (pos + 2 <= length) {
            int len = payload[pos] & 0xff;
            if (len == 0) break;
            int type = payload[pos + 1] & 0xff;
            if (type == 0x02 || type == 0x03) {
                int u16 = ((payload[pos + 2] & 0xff) << 8) | (payload[pos + 3] & 0xff);
                if (b.length() > 0) b.append(", ");
                b.append(String.format("%04X", u16)).append(" (type ").append(type == 0x02 ? "16-bit" : "complete 16-bit").append(")");
            } else if (type == 0x06 || type == 0x07) {
                long u32 = (((long) payload[pos + 2] & 0xff) << 24)
                        | (((long) payload[pos + 3] & 0xff) << 16)
                        | (((long) payload[pos + 4] & 0xff) << 8)
                        | ((long) payload[pos + 5] & 0xff);
                if (b.length() > 0) b.append(", ");
                b.append(String.format("%08X", u32)).append(" (type ").append(type == 0x06 ? "16-bit partial" : "32-bit").append(")");
            }
            pos += len + 1;
        }
        return b.length() == 0 ? "(none)" : b.toString();
    }

    public String vendorSpecificDataHex() {
        byte[] data = manufacturerData();
        return data.length == 0 ? "(none)" : Util.hex(data);
    }

    public String vendorSpecificDataAscii() {
        byte[] data = manufacturerData();
        return data.length == 0 ? "(none)" : Util.printableAscii(data);
    }

    public int rssi() {
        return -1;
    }

    public String summaryFields() {
        StringBuilder b = new StringBuilder();
        b.append("Local name: ").append(truncate(localName(), 60)).append("\n");
        b.append("Name type flag: 0x").append(Integer.toHexString(localNameTypeFlag() & 0xff)).append("\n");
        b.append("Tx power: ").append(txPowerValue() >= 0 ? txPowerValue() + " dBm" : "not present").append("\n");
        b.append("Service UUIDs (16-bit): ").append(truncate(serviceUUIDs16String(), 96)).append("\n");
        b.append("Service UUIDs (32-bit): ").append(truncate(serviceUUIDs32String(), 96)).append("\n");
        b.append("Manufacturer ID: ").append(manufacturerIdString()).append("\n");
        b.append("Service data: ").append(truncate(serviceDataUuidsString(), 96)).append("\n");
        b.append("Vendor data hex: ").append(truncate(vendorSpecificDataHex(), 64)).append("\n");
        b.append("Vendor data ASCII: ").append(truncate(vendorSpecificDataAscii(), 40)).append("\n");
        return b.toString();
    }

    static String truncate(String value, int maxChars) {
        return value == null || value.length() <= maxChars ? value : value.substring(0, maxChars) + "...";
    }

    public static class Uuid16 {
        public final String value;
        public final int numeric;

        public Uuid16(String value, int numeric) {
            this.value = value;
            this.numeric = numeric;
        }
    }

    public static class Uuid32 {
        public final String value;
        public final long numeric;

        public Uuid32(String value, long numeric) {
            this.value = value;
            this.numeric = numeric;
        }
    }

    public static class ManufacturerDataSection {
        public final int companyId;
        public final byte[] data;

        public ManufacturerDataSection(int companyId, byte[] data) {
            this.companyId = companyId;
            this.data = data;
        }
    }
}
