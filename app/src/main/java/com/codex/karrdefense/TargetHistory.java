package com.codex.karrdefense;

import java.util.ArrayList;
import java.util.List;

public class TargetHistory {

    public static class RssiPoint {
        public final String time;
        public final int rssi;

        public RssiPoint(String time, int rssi) {
            this.time = time;
            this.rssi = rssi;
        }
    }

    private final List<RssiPoint> points = new ArrayList<>();

    public void add(String time, int rssi) {
        points.add(new RssiPoint(time, rssi));
    }

    public int count() {
        return points.size();
    }

    public int minRssi() {
        if (points.isEmpty()) return 0;
        int m = points.get(0).rssi;
        for (RssiPoint p : points) {
            if (p.rssi < m) m = p.rssi;
        }
        return m;
    }

    public int maxRssi() {
        if (points.isEmpty()) return 0;
        int m = points.get(0).rssi;
        for (RssiPoint p : points) {
            if (p.rssi > m) m = p.rssi;
        }
        return m;
    }

    public String timelineString() {
        if (points.isEmpty()) return "(no readings recorded)";
        int start = Math.max(0, points.size() - 24);
        StringBuilder b = new StringBuilder();
        for (int i = start; i < points.size(); i++) {
            if (i > start) b.append("; ");
            b.append(points.get(i).time);
            b.append(" RSSI ");
            b.append(points.get(i).rssi);
        }
        if (points.size() > 24) {
            b.append("; ... (showing last 24 of ").append(points.size()).append(")");
        }
        return b.toString();
    }

    public List<RssiPoint> allPoints() {
        return new ArrayList<>(points);
    }
}
