package com.codex.karrdefense;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

/**
 * Handles the surgical GATT connection to verify if a vehicle is vulnerable
 * and optionally sends a harmless PoC payload (e.g. flash lights) to prove it.
 */
public class VerificationPoCManager {
    private static final String TAG = "PoCManager";
    private BluetoothGatt bluetoothGatt;
    private VerificationPoCPayload activePoC;
    private PoCResultCallback callback;

    public interface PoCResultCallback {
        void onConnectionStateChange(String state);
        void onVulnerabilityVerified(boolean isOpen);
        void onPayloadDelivered(boolean success);
        void onError(String error);
    }

    public void verifyAndTrigger(Context context, BluetoothDevice device, VerificationPoCPayload poc, PoCResultCallback cb) {
        this.activePoC = poc;
        this.callback = cb;
        this.callback.onConnectionStateChange("Connecting...");
        
        // Connect to the device to check if the specific service is exposed
        this.bluetoothGatt = device.connectGatt(context, false, gattCallback);
    }

    public void disconnect() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                callback.onConnectionStateChange("Connected. Discovering services...");
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                callback.onConnectionStateChange("Disconnected");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS && activePoC != null) {
                BluetoothGattService service = gatt.getService(activePoC.getTargetServiceUuid());
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(activePoC.getTargetCharacteristicUuid());
                    if (characteristic != null) {
                        callback.onVulnerabilityVerified(true);
                        // The door is open. Send the harmless PoC payload.
                        characteristic.setValue(activePoC.getTriggerPayload());
                        gatt.writeCharacteristic(characteristic);
                        return;
                    }
                }
                callback.onVulnerabilityVerified(false);
                disconnect();
            } else {
                callback.onError("Service discovery failed");
                disconnect();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                callback.onPayloadDelivered(true);
            } else {
                callback.onPayloadDelivered(false);
            }
            disconnect(); // Auto-disconnect after sending PoC
        }
    };
}
