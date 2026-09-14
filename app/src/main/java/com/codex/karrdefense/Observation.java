package com.codex.karrdefense;

public interface Observation {
    String type();
    String searchableText();
    String identity();
    int rssi();
    String summary();
    String detail();
}
