package net.defined.mobile_nebula

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ExternalVpnBroadcastReceiver : BroadcastReceiver() {

    companion object{
        const val ACTION_START_NEBULA = "net.defined.mobile_nebula.START_NEBULA"
        const val ACTION_STOP_NEBULA = "net.defined.mobile_nebula.STOP_NEBULA"
    }

    private val scope = CoroutineScope(SupervisorJob())

    override fun onReceive(context: Context,intent: Intent?) {
        val pendingResult: PendingResult = goAsync()
        scope.launch(Dispatchers.Default) {
            try{
                Log.d(TAG,intent?.toString()!!)

                if (intent.action == ACTION_START_NEBULA){
                    // Get the last site information
                    val (id,path) = LastSiteInformation.getLastSite(context)
                    if (id != null && path != null){
                        Log.d(TAG,"Starting VPN Service")
                        val startIntent = Intent(context, NebulaVpnService::class.java).apply {
                            action = NebulaVpnService.ACTION_START_EXTERNAL
                            putExtra("path", path)
                            putExtra("id", id)
                        }
                        context.startForegroundService(startIntent)
                    }
                }

                if (intent.action == ACTION_STOP_NEBULA){
                    Log.d(TAG,"Stopping VPN Service")
                    val stopIntent = Intent(context, NebulaVpnService::class.java).apply{
                        action = NebulaVpnService.ACTION_STOP_EXTERNAL
                    }
                    context.startForegroundService(stopIntent)
                }
            }finally {
                pendingResult.finish()
            }
        }
    }
}