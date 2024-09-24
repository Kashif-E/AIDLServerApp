// ServerApp/src/main/java/com/kashif/aidlmessage/MainActivity.kt
package com.kashif.aidlmessage

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.kashif.aidleventcommunicator.communication.ICommunicationService
import com.kashif.aidlmessage.ui.theme.AIDLMessageTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private var communicationService: ICommunicationService? = null
    private var isBound: Boolean = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            communicationService = ICommunicationService.Stub.asInterface(service)
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            communicationService = null
            isBound = false
        }

        override fun onBindingDied(name: ComponentName?) {
            communicationService = null
            isBound = false
        }

        override fun onNullBinding(name: ComponentName?) {
            communicationService = null
            isBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        bindToCommunicationService()
    }

    override fun onStop() {
        super.onStop()
        unbindFromCommunicationService()
    }

    private fun bindToCommunicationService() {
        val intent = Intent("com.kashif.aidleventcommunicator.communication.ICommunicationService").apply {
            setPackage("com.kashif.aidlmessage") // Ensure this matches the server's package name
        }
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindFromCommunicationService() {
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AIDLMessageTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ServerMainScreen(
                        onSendMessage = { message ->
                            sendMessageToClients(message)
                        }
                    )
                }
            }
        }
    }

    private fun sendMessageToClients(message: String) {
        if (isBound && communicationService != null) {
            // Launch a coroutine to perform IPC on a background thread
            lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val msg = com.kashif.aidleventcommunicator.message.IMessage(message)
                        communicationService?.sendMessage(msg)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Optionally, handle the error (e.g., show a toast)
                }
            }
        } else {
            // Optionally, inform the user that the service is not bound
        }
    }
}

@Composable
fun ServerMainScreen(onSendMessage: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val sentMessages = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Server App",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Enter Message") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                if (text.isNotBlank()) {
                    onSendMessage(text)
                    sentMessages.add("Sent: $text")
                    text = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send to Client")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Sent Messages:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Divider()

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 8.dp)
        ) {
            items(sentMessages) { message ->
                Text(text = message, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}