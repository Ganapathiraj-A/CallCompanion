package com.callcompanion

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callcompanion.ui.theme.CallCompanionTheme
import com.callcompanion.ui.theme.OrangePrimary
import com.callcompanion.ui.theme.OrangeSecondary
import kotlinx.coroutines.delay

class PostCallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure white status bar text for the dark background dim
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            CallCompanionTheme {
                PostCallScreen(
                    onDismiss = { finish() },
                    onAction = { actionType ->
                        handleAction(actionType)
                        finish()
                    }
                )
            }
        }
    }

    private fun handleAction(type: String) {
        val settingsManager = SettingsManager(this)
        val text = when (type) {
            "text" -> settingsManager.getShareText()
            "video" -> settingsManager.getVideoLink()
            "audio" -> {
                android.widget.Toast.makeText(this, "Searching for latest call recording...", android.widget.Toast.LENGTH_SHORT).show()
                // Placeholder: In a real app, we would scan /Recordings/Call/ for the newest file
                "Check out my latest call recording (Placeholder)"
            }
            else -> ""
        }
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            this.type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "Share via"))
    }
}

@Composable
fun PostCallScreen(onDismiss: () -> Unit, onAction: (String) -> Unit) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
        delay(10000) // Auto-dismiss after 10 seconds
        isVisible = false
        delay(500) // Wait for animation
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onDismiss() }, // Dismiss on background click
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .padding(bottom = 60.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .clickable(enabled = false) { /* Prevent click through */ },
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header with Close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Call Finished",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimary
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Follow-up Actions",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ActionButton(
                            icon = Icons.Default.Share,
                            label = "Share Text",
                            onClick = { onAction("text") }
                        )
                        ActionButton(
                            icon = Icons.Default.VideoLibrary,
                            label = "Share Video",
                            onClick = { onAction("video") }
                        )
                        ActionButton(
                            icon = Icons.Default.Mic,
                            label = "Latest Audio",
                            onClick = { onAction("audio") }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Progress Bar for auto-dismiss
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape),
                        color = OrangePrimary,
                        trackColor = Color.LightGray.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
fun ActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(OrangePrimary, OrangeSecondary)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
