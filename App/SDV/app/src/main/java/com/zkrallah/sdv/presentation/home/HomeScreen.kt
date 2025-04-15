package com.zkrallah.sdv.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zkrallah.sdv.BROKER_URL
import com.zkrallah.sdv.R
import com.zkrallah.sdv.domain.models.Message
import com.zkrallah.sdv.presentation.intro.LoaderIntro
import com.zkrallah.sdv.showToast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(homeViewModel: HomeViewModel = hiltViewModel()) {
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
                showToast(context, it)
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
        TopAppBar(title = { Text(text = "SDV") }, actions = {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.Settings, contentDescription = "Settings"
                )
            }
        })
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
                    homeViewModel.connect(BROKER_URL, false)
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.car),
                                contentDescription = "",
                                modifier = Modifier
                                    .width(44.dp)
                                    .height(33.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Tesla Model 3",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Connected",
                                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Green)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        if (true) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Row {
                                    Text(
                                        text = "Software Update",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )

                                    Text(
                                        text = "Available",
                                        style = MaterialTheme.typography.bodyLarge.copy(color = Color.Green)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(text = "Version 2025.4.2")

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(text = "• Enhanced Autopilot features")
                                Text(text = "• Improved battery optimizations")
                                Text(text = "• Bug fixes and performance improvements")

                                Spacer(modifier = Modifier.height(8.dp))

                                Row {
                                    Button(
                                        onClick = { message = "" },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                                    ) {
                                        Text(text = "Install Update", color = Color.White)
                                    }
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Row {
                                    Text(
                                        text = "Software Update",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )

                                    Text(
                                        text = "Up to date",
                                        style = MaterialTheme.typography.bodyLarge.copy(color = Color.Blue)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                LoaderIntro(
                                    modifier = Modifier
                                        .size(200.dp)
                                        .fillMaxWidth()
                                        .align(alignment = Alignment.CenterHorizontally), R.raw.animation3
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "Quick Actions",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    IconButton(
                                        onClick = { /* Handle click */ },
                                        modifier = Modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = CircleShape
                                            )
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.lock),
                                            contentDescription = "",
                                            modifier = Modifier
                                                .width(44.dp)
                                                .height(33.dp)
                                        )
                                    }
                                    Text(
                                        text = "Lock",
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    IconButton(
                                        onClick = { /* Handle click */ },
                                        modifier = Modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = CircleShape
                                            )
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.climate),
                                            contentDescription = "",
                                            modifier = Modifier
                                                .width(44.dp)
                                                .height(33.dp)
                                        )
                                    }
                                    Text(
                                        text = "Climate",
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    IconButton(
                                        onClick = { /* Handle click */ },
                                        modifier = Modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = CircleShape
                                            )
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.location),
                                            contentDescription = "",
                                            modifier = Modifier
                                                .width(44.dp)
                                                .height(33.dp)
                                        )
                                    }
                                    Text(
                                        text = "Location",
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
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