package com.codex.karrdefense;

import java.util.UUID;

public class MockTeslaPoC implements VerificationPoCPayload {
    // Standard Tesla BLE Service UUID often used for Phone Key (Documentation/Mock)
    private static final UUID TESLA_SERVICE_UUID = UUID.fromString("00000211-0000-1000-8000-00805f9b34fb");
    private static final UUID TESLA_CHAR_UUID = UUID.fromString("00000212-0000-1000-8000-00805f9b34fb");
    
    @Override
    public String getVulnerabilityName() {
        return "Tesla Phone Key (Unauthenticated Broadcast)";
    }

    @Override
    public String getSafeActionDescription() {
        return "Flash Headlights (PoC)";
    }

    @Override
    public UUID getTargetServiceUuid() {
        return TESLA_SERVICE_UUID;
    }

    @Override
    public UUID getTargetCharacteristicUuid() {
        return TESLA_CHAR_UUID;
    }

    @Override
    public byte[] getTriggerPayload() {
        // Mock payload. In reality, this would be the specific hex sequence 
        // to trigger the vehicle action via the vulnerable endpoint.
        return new byte[] { (byte)0xDE, (byte)0xAD, (byte)0xBE, (byte)0xEF };
    }
}
