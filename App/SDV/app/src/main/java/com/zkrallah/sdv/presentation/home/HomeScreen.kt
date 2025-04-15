package com.zkrallah.sdv.presentation.home

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zkrallah.sdv.domain.models.Message

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(homeViewModel: HomeViewModel = hiltViewModel()) {
    var mqttBroker by remember { mutableStateOf("") }
    var secureConnection by remember { mutableStateOf(false) }
    var subscribeToTopic by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    val receivedMessage = homeViewModel.receivedMessage.collectAsState()
    val connectionStatus = homeViewModel.connectionStatus.collectAsState()
    var messages by remember { mutableStateOf(listOf<Message>()) }

    val context = LocalContext.current
    val uiMessage by homeViewModel.uiMessage.collectAsState()

    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            if (it.isNotEmpty() && it.isNotBlank()) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                homeViewModel.clearUiMessage()
            }
        }
    }

    // Update messages whenever a new message is received
    LaunchedEffect(receivedMessage.value) {
        receivedMessage.value?.let { msg ->
            messages = messages + msg
        }
    }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text("Z-MQTT") })
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (!connectionStatus.value) {
                    Text(
                        text = "Connect to a Broker:",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = mqttBroker,
                        onValueChange = { mqttBroker = it },
                        placeholder = { Text("Enter MQTT Broker URL") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = secureConnection,
                            onCheckedChange = { secureConnection = it }
                        )
                        Text("Use Secure Connection (SSL/TLS)")
                    }

                    OutlinedButton(
                        onClick = {
                            homeViewModel.connect(mqttBroker, secureConnection)
                            mqttBroker = ""
                        }, modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Connect")
                    }
                } else {
                    Text(text = "Z-Client", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = subscribeToTopic,
                            onValueChange = { subscribeToTopic = it },
                            placeholder = { Text("Topic to subscribe") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )

                        OutlinedButton(
                            onClick = {
                                homeViewModel.subscribeToTopic(subscribeToTopic)
                                subscribeToTopic = ""
                            }, modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Text(text = "Subscribe")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            placeholder = { Text("Message") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )

                        OutlinedTextField(
                            value = topic,
                            onValueChange = { topic = it },
                            placeholder = { Text("Topic") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            homeViewModel.publishMessage(message, topic)
                            message = ""
                        }, modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Publish")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Received Messages:", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(messages) { msg ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Topic: ${msg.topic}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = msg.payload,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Disconnect button (only shown when connected)
            if (connectionStatus.value) {
                OutlinedButton(
                    onClick = { homeViewModel.disconnect() },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text(text = "Disconnect", color = MaterialTheme.colorScheme.onError)
                }
            }
        }
    }
}

@Preview(name = "Home Screen", showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}