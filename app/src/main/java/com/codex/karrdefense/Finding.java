package com.codex.karrdefense;

import android.text.TextUtils;

public class Finding {
    public final String ruleId;
    public final String title;
    public final String detail;
    public final String confidence;

    public Finding(String ruleId, String title, String detail, String confidence) {
        this.ruleId = ruleId;
        this.title = title;
        this.detail = detail;
        this.confidence = confidence;
    }

    public String toJson(String sessionId, Observation observation) {
        return "{\"type\":\"finding\","
                + "\"session_id\":" + Util.json(sessionId)
                + ",\"time\":" + Util.json(Util.now())
                + ",\"rule_id\":" + Util.json(ruleId)
                + ",\"target_type\":" + Util.json(observation.type())
                + ",\"target_id\":" + Util.json(observation.identity())
                + ",\"target_summary\":" + Util.json(observation.summary())
                + ",\"title\":" + Util.json(title)
                + ",\"detail\":" + Util.json(detail)
                + ",\"confidence\":" + Util.json(confidence)
                + "}";
    }
}
