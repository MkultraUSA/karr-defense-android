package com.codex.karrdefense;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

/**
 * Handles the UI flow for verifying a target vehicle.
 * Presents the safety/consent dialog to the operator before executing a PoC payload.
 */
public class TargetVerificationUI {

    public static void promptForVerification(final Context context, final Observation observation, final VerificationPoCPayload poc, final VerificationPoCManager pocManager) {
        new AlertDialog.Builder(context)
                .setTitle("Verify Target: " + observation.summary())
                .setMessage("DANGER: You are about to initiate an active connection to verify a vulnerability.\n\n" +
                            "Target: " + poc.getVulnerabilityName() + "\n" +
                            "Action: " + poc.getSafeActionDescription() + "\n\n" +
                            "Do you have explicit authorization to audit this vehicle?")
                .setPositiveButton("AUTHORIZE & EXECUTE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "Initiating Surgical PoC...", Toast.LENGTH_SHORT).show();
                        // In a real implementation, we need the BluetoothDevice from the observation.
                        // pocManager.verifyAndTrigger(context, device, poc, new VerificationPoCManager.PoCResultCallback() { ... });
                    }
                })
                .setNegativeButton("CANCEL", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
