package com.example.masjd2.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.masjd2.services.AthanService

/**
 * BroadcastReceiver to handle stopping Athan from notification or volume key
 */
class AthanStopReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AthanStopReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received stop Athan request")
        
        when (intent.action) {
            AthanService.ACTION_STOP_ATHAN -> {
                // Stop the Athan service
                val serviceIntent = Intent(context, AthanService::class.java).apply {
                    action = AthanService.ACTION_STOP_ATHAN
                }
                ContextCompat.startForegroundService(context, serviceIntent)
                Log.d(TAG, "Sent stop command to AthanService")
            }
        }
    }
}
