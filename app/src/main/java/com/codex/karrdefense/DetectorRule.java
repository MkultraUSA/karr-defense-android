package com.codex.karrdefense;

public interface DetectorRule {
    String id();
    Finding evaluate(Observation observation);
}
