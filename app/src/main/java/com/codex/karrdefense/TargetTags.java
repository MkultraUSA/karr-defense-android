package com.codex.karrdefense;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TargetTags {

    public static final String[] ALL_TAGS = {
            "KARR?", "Vehicle?", "Head Unit?", "Hotspot?",
            "Aftermarket?", "False Positive", "Follow Up"
    };

    private final Set<String> tags = new LinkedHashSet<>();

    public void add(String tag) {
        if (!TextUtils.isEmpty(tag)) tags.add(tag);
    }

    public void remove(String tag) {
        tags.remove(tag);
    }

    public boolean has(String tag) {
        return tags.contains(tag);
    }

    public void toggle(String tag) {
        if (has(tag)) remove(tag);
        else add(tag);
    }

    public List<String> all() {
        return new ArrayList<>(tags);
    }

    public boolean isEmpty() {
        return tags.isEmpty();
    }

    public String tagsString() {
        return TextUtils.join(", ", tags);
    }
}
