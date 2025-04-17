package com.zkrallah.sdv.presentation.home

import android.content.Context
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.zkrallah.sdv.BROKER_URL
import com.zkrallah.sdv.domain.models.Message

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(homeViewModel: HomeViewModel = hiltViewModel()) {
    val receivedMessage = homeViewModel.receivedMessage.collectAsState()
    val connectionStatus = homeViewModel.connectionStatus.collectAsState()
    val isConnecting = homeViewModel.isConnecting.collectAsState()
    val errorDialogMessage = homeViewModel.errorDialogMessage.collectAsState()

    val context = LocalContext.current
    val uiMessage by homeViewModel.uiMessage.collectAsState()

    val imageLoader = ImageLoader.Builder(context)
        .components {
            add(SvgDecoder.Factory())
        }
        .build()

    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            if (it.isNotEmpty() && it.isNotBlank()) {
//                showToast(context, it)
                homeViewModel.clearUiMessage()
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
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
                .padding(paddingValues),
            verticalArrangement = Arrangement.Top
        ) {
            Column(modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())) {
                if (!connectionStatus.value) {
                    DisconnectedCard(homeViewModel::connect, isConnecting, errorDialogMessage, homeViewModel::clearErrorMessage)
                } else {
                    homeViewModel.subscribeToTopic("ota/update_possible")
                    ConnectionStatusCard(context, imageLoader)

                    UpdateStatusCard(receivedMessage, homeViewModel::publishMessage)

                    QuickActionsCard(context, imageLoader)
                }
            }
        }
    }
}

@Composable
fun QuickActionsCard(context: Context, imageLoader: ImageLoader) {
    val quickActions = listOf(
        "lock" to "Lock",
        "temp" to "Weather",
        "location" to "Location"
    )

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
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                quickActions.forEach { (resName, label) ->
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
                                .padding(8.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data("android.resource://${context.packageName}/raw/$resName")
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                imageLoader = imageLoader,
                                modifier = Modifier.size(56.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UpdateStatusCard(receivedMessage: State<Message?>, publishMessage: (String, String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Software Update",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = if (receivedMessage.value != null) "Available" else "Up to date",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = if (receivedMessage.value != null) Color(0xFF4CAF50) else Color(0xFF2196F3)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            receivedMessage.value?.let { msg ->
                when (msg.payload) {
                    "" -> UpToDateView()
                    "updating" -> UpdatingView()
                    else -> UptateAvailableView(msg.payload, publishMessage)
                }
            } ?: run {
                UpToDateView()
            }
        }
    }
}


@Composable
fun ConnectionStatusCard(context: Context, imageLoader: ImageLoader) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("android.resource://${context.packageName}/raw/car")
                    .crossfade(true)
                    .build(),
                contentDescription = "Car image",
                imageLoader = imageLoader,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tesla Model 3",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Connected",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF4CAF50))
                )
            }
        }
    }
}

@Composable
fun DisconnectedCard(connect: (String, Boolean) -> Unit, isConnecting: State<Boolean>, errorDialogMessage: State<String?>, clearErrorMessage: () -> Unit) {

    connect(BROKER_URL, false)

    if (isConnecting.value) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            title = { Text("Connecting...") },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Trying to connect to your car.")
                }
            }
        )
    }

    if (errorDialogMessage.value != null) {
        AlertDialog(
            onDismissRequest = { clearErrorMessage() },
            confirmButton = {
                TextButton(onClick = { clearErrorMessage() }) {
                    Text("OK")
                }
            },
            title = { Text("Connection Failed") },
            text = { Text(errorDialogMessage.value ?: "") }
        )
    }
}


@Composable
fun UpToDateView() {
    // Define size range for pulsing animation
    val minSize = 40f
    val maxSize = 60f

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val size by infiniteTransition.animateFloat(
        initialValue = minSize,
        targetValue = maxSize,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse animation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Container with fixed maximum size to prevent layout shifts
        Box(
            modifier = Modifier.size(maxSize.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Up to date",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(size.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your system is up to date!",
            style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF4CAF50))
        )
    }
}

@Composable
fun UptateAvailableView(version: String, publishMessage: (String, String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Version $version",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(text = "• Enhanced Autopilot features")
        Text(text = "• Improved battery optimizations")
        Text(text = "• Bug fixes and performance improvements")

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { publishMessage("yes", "ota/response") },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            Text(text = "Install Update", color = Color.White)
        }
    }
}

@Composable
fun UpdatingView() {
    // Animation parameters
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation animation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse animation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Container with progress indicator and rotating download icon
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background progress indicator
            CircularProgressIndicator(
                modifier = Modifier.size(80.dp),
                color = Color.LightGray,
                strokeWidth = 2.dp
            )

            // Rotating download icon with pulse effect
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Downloading update",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(40.dp)
                    .scale(pulseScale)
                    .rotate(rotation)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Installing system update...",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "This may take several minutes",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Preview(name = "Home Screen", showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}