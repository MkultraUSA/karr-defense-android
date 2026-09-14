package com.codex.karrdefense;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;

public class EvidencePacket {
    public final Observation observation;
    public final String sessionId;
    public final String captureTime;
    public final TargetTags tags;
    public final TargetNotes notes;

    public EvidencePacket(Observation observation, String sessionId,
                           String captureTime, TargetTags tags, TargetNotes notes) {
        this.observation = observation;
        this.sessionId = sessionId;
        this.captureTime = captureTime;
        this.tags = tags;
        this.notes = notes;
    }

    public String packetText(String title) {
        StringBuilder b = new StringBuilder();
        b.append("═════════════════════════════════════════════════════════\n");
        b.append("  ").append(title).append("\n");
        b.append("  Evidence Packet — ").append(sessionId).append("\n");
        b.append("  Captured: ").append(captureTime).append("\n");
        b.append("═════════════════════════════════════════════════════════\n\n");
        b.append("TARGET SUMMARY\n");
        b.append("---------------\n");
        b.append(observation.summary()).append("\n\n");

        b.append("RAW DATA\n");
        b.append("--------\n");
        if (observation instanceof MainActivity.BleObservation) {
            MainActivity.BleObservation ble = (MainActivity.BleObservation) observation;
            BleDecoder dec = new BleDecoder(ble.raw);
            b.append("  Manufacturer data:\n");
            b.append("    ").append(dec.manufacturerIdString()).append("\n");
            b.append("    Vendor data hex: ").append(dec.vendorSpecificDataHex()).append("\n");
            b.append("    Vendor data ASCII: ").append(BleDecoder.truncate(dec.vendorSpecificDataAscii(), 40)).append("\n");
            b.append("    Local name: ").append(BleDecoder.truncate(dec.localName(), 40)).append("\n");
            b.append("    Service UUIDs: ").append(BleDecoder.truncate(dec.serviceUUIDs16String(), 96)).append("\n");
            b.append("    Service data: ").append(BleDecoder.truncate(dec.serviceDataUuidsString(), 96)).append("\n");
        }

        b.append("DECODED FIELDS\n");
        b.append("--------------\n");
        b.append(observation.detail()).append("\n\n");

        if (!tags.isEmpty()) {
            b.append("TAGS\n");
            b.append("----\n");
            for (String t : tags.all()) {
                b.append("  ").append(t).append("\n");
            }
            b.append("\n");
        }

        if (!notes.get().isEmpty()) {
            b.append("RESEARCH NOTES\n");
            b.append("--------------\n");
            b.append(notes.get()).append("\n\n");
        }

        b.append("SESSION / CAPTURE METADATA\n");
        b.append("--------------------------\n");
        b.append("Session ID: ").append(sessionId).append("\n");
        b.append("Capture time: ").append(captureTime).append("\n");
        b.append("App: Field Security Inspector (com.codex.karrdefense)\n");
        b.append("Scope: authorized defensive inspection only\n");
        b.append("Telemetry collected: BLE advertisement + Wi-Fi scan metadata\n");
        b.append("BLE GATT writes: none\n");
        b.append("Vehicle-control commands: none\n");

        b.append("\n\n─────────────────────────────────────────────────────────────\n");
        b.append("  END OF EVIDENCE PACKET\n");
        b.append("─────────────────────────────────────────────────────────────\n");
        return b.toString();
    }

    public String packetText(String title, String customHeader) {
        StringBuilder b = new StringBuilder();
        b.append(customHeader).append("\n");
        b.append(packetText(title));
        return b.toString();
    }

    public String toJson() {
        StringBuilder b = new StringBuilder();
        b.append("{\n");
        b.append("  \"type\": \"evidence_packet\",\n");
        b.append("  \"session_id\": \"").append(sessionId).append("\",\n");
        b.append("  \"capture_time\": \"").append(captureTime).append("\",\n");
        b.append("  \"target_type\": \"").append(observation.type()).append("\",\n");
        b.append("  \"target_id\": \"").append(observation.identity()).append("\",\n");
        b.append("  \"summary\": \"").append(observation.summary()).append("\",\n");
        b.append("  \"tags\": ");
        b.append(tags.isEmpty() ? "[]" : jsonList(tags.all()));
        b.append(",\n");
        b.append("  \"notes\": \"").append(notes.get()).append("\",\n");
        b.append("  observation_summary: ").append(observation.summary()).append("\\n");
        b.append("  observation_type: ").append(observation.type()).append("\\n");
        b.append("  observation_id: ").append(observation.identity()).append("\\n");
        b.append("\n}\n");
        return b.toString();
    }

    private String jsonList(List<String> items) {
        StringBuilder b = new StringBuilder("[");
        boolean first = true;
        for (String s : items) {
            if (!first) b.append(", ");
            b.append("\"").append(s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")).append("\"");
            first = false;
        }
        b.append("]");
        return b.toString();
    }
}
