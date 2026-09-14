package com.codex.karrdefense;

public class ObservationState {
    public long lastLoggedMs;
    public int lastRssi;

    public ObservationState(long lastLoggedMs, int lastRssi) {
        this.lastLoggedMs = lastLoggedMs;
        this.lastRssi = lastRssi;
    }
}
