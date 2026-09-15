package com.codex.karrdefense;

import java.util.UUID;

/**
 * Represents a safe Proof of Concept (PoC) payload for a specific vehicle vulnerability.
 * Designed to be modular so new PoCs can be added as updates (Subscription model).
 */
public interface VerificationPoCPayload {
    String getVulnerabilityName();
    String getSafeActionDescription(); // e.g., "Flash headlights" or "Beep horn"
    UUID getTargetServiceUuid();
    UUID getTargetCharacteristicUuid();
    byte[] getTriggerPayload();
}
