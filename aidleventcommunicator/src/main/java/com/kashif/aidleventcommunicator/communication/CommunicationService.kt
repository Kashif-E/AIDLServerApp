// ServerApp/src/main/java/com/kashif/aidleventcommunicator/communication/CommunicationService.kt
package com.kashif.aidleventcommunicator.communication

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteCallbackList
import android.os.RemoteException
import android.util.Log
import com.kashif.aidleventcommunicator.communication.ICommunicationService
import com.kashif.aidleventcommunicator.communication.IMessageListener
import com.kashif.aidleventcommunicator.message.IMessage

class CommunicationService : Service() {

    // RemoteCallbackList to manage listeners in a thread-safe manner
    private val listeners = RemoteCallbackList<IMessageListener>()

    // Binder implementation of the AIDL interface
    private val binder = object : ICommunicationService.Stub() {
        override fun sendMessage(message: IMessage) {
            Log.d("CommunicationService", "sendMessage called with: ${message.content}")
            // Broadcast the message to all registered listeners
            val count = listeners.beginBroadcast()
            for (i in 0 until count) {
                try {
                    listeners.getBroadcastItem(i)?.onMessageReceived(message)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
            listeners.finishBroadcast()
        }

        override fun registerListener(listener: IMessageListener) {
            if (listener != null) {
                listeners.register(listener)
                Log.d("CommunicationService", "Listener registered")
            }
        }

        override fun unregisterListener(listener: IMessageListener) {
            if (listener != null) {
                listeners.unregister(listener)
                Log.d("CommunicationService", "Listener unregistered")
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("CommunicationService", "Service Bound")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("CommunicationService", "Service Unbound")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.d("CommunicationService", "Service Destroyed")
        listeners.kill()
        super.onDestroy()
    }
}