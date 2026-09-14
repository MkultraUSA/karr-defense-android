package com.codex.karrdefense;

/**
 * One documented finding row for the local vehicle-audit database.
 *
 * A finding is a human-authored record: an operator picked a scan result,
 * chose an issue category + severity, and wrote notes. This is the local,
 * offline record of that work — the same record is also appended to the
 * session evidence JSONL so it survives outside the app.
 */
public class AuditFinding {
    public long id;
    public String createdAt;
    public String sessionId;
    public String targetType;
    public String targetId;
    public String targetName;
    public int rssi;
    public String category;
    public String severity;
    public String notes;

    public AuditFinding(long id, String createdAt, String sessionId, String targetType,
                        String targetId, String targetName, int rssi,
                        String category, String severity, String notes) {
        this.id = id;
        this.createdAt = createdAt;
        this.sessionId = sessionId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.targetName = targetName;
        this.rssi = rssi;
        this.category = category;
        this.severity = severity;
        this.notes = notes;
    }

    /** Evidence-log rendering, kept in the same JSONL shape as the other record types. */
    public String toJson() {
        return "{\"type\":\"documented_finding\""
                + ",\"id\":" + id
                + ",\"time\":" + Util.json(createdAt)
                + ",\"session_id\":" + Util.json(sessionId)
                + ",\"target_type\":" + Util.json(targetType)
                + ",\"target_id\":" + Util.json(targetId)
                + ",\"target_name\":" + Util.json(targetName)
                + ",\"rssi\":" + rssi
                + ",\"category\":" + Util.json(category)
                + ",\"severity\":" + Util.json(severity)
                + ",\"notes\":" + Util.json(notes) + "}";
    }
}