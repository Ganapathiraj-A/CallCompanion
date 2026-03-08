package com.callcompanion

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.launch
import java.io.File
import com.callcompanion.ui.theme.CallCompanionTheme
import com.callcompanion.ui.theme.OrangePrimary

enum class Screen {
    MAIN,
    SETTINGS,
    REQUIREMENTS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CallCompanionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf(Screen.MAIN) }

    Crossfade(targetState = currentScreen, label = "Screen Transition") { screen ->
        when (screen) {
            Screen.MAIN -> MainScreen(
                onNavigateSettings = { currentScreen = Screen.SETTINGS },
                onNavigateRequirements = { currentScreen = Screen.REQUIREMENTS }
            )
            Screen.SETTINGS -> SettingsScreen(
                onBack = { currentScreen = Screen.MAIN }
            )
            Screen.REQUIREMENTS -> RequirementsScreen(
                onBack = { currentScreen = Screen.MAIN }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onNavigateSettings: () -> Unit, onNavigateRequirements: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scrollState = rememberScrollState()

    val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasPhonePermission by remember { mutableStateOf(false) }
    var hasOverlayPermission by remember { mutableStateOf(false) }
    var hasStoragePermission by remember { mutableStateOf(false) }

    val updatePermissions = {
        hasPhonePermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
        hasOverlayPermission = Settings.canDrawOverlays(context)
        hasStoragePermission = ContextCompat.checkSelfPermission(
            context, storagePermission
        ) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(Unit) { updatePermissions() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) updatePermissions()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val requirementsMet = hasPhonePermission && hasOverlayPermission && hasStoragePermission

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Call Companion", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = OrangePrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 1.dp
            ) {
                Text(
                    text = "Version 1.1.3",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color(0xFF4CAF50),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    "RELEASE 1.1.3 FORCE-UPDATE",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your smart post-call assistant",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Simulation Button
            OutlinedButton(
                onClick = {
                    val postCallIntent = Intent(context, PostCallActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    context.startActivity(postCallIntent)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simulate Phone Ring")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Modular Requirements Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateRequirements() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Requirements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (requirementsMet) "All requirements are enabled." else "Some requirements are missing.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                    if (requirementsMet) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "OK", tint = Color(0xFF4CAF50), modifier = Modifier.size(32.dp))
                    } else {
                        Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color.Red, modifier = Modifier.size(32.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Unified Settings Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateSettings() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Manage share text, video link, and recordings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = OrangePrimary)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequirementsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasPhonePermission by remember { mutableStateOf(false) }
    var hasOverlayPermission by remember { mutableStateOf(false) }
    var hasStoragePermission by remember { mutableStateOf(false) }

    val updatePermissions = {
        hasPhonePermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
        hasOverlayPermission = Settings.canDrawOverlays(context)
        hasStoragePermission = ContextCompat.checkSelfPermission(
            context, storagePermission
        ) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(Unit) { updatePermissions() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) updatePermissions()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val phoneLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { hasPhonePermission = it }

    val storageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { hasStoragePermission = it }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Requirements", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Enable these to help the companion appear after your calls.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            PermissionItem(
                title = "Phone State",
                isGranted = hasPhonePermission,
                onClick = { phoneLauncher.launch(Manifest.permission.READ_PHONE_STATE) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PermissionItem(
                title = "Overlay Permission",
                isGranted = hasOverlayPermission,
                onClick = {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PermissionItem(
                title = "Storage (for Audio)",
                isGranted = hasStoragePermission,
                onClick = { storageLauncher.launch(storagePermission) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    var shareText by remember { mutableStateOf(settingsManager.getShareText()) }
    var videoLink by remember { mutableStateOf(settingsManager.getVideoLink()) }
    var recordingPath by remember { mutableStateOf(settingsManager.getRecordingPath()) }

    var showFolderPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Configuration", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {

            OutlinedTextField(
                value = shareText,
                onValueChange = { 
                    shareText = it
                    settingsManager.setShareText(it)
                },
                label = { Text("Default Share Text") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = videoLink,
                onValueChange = { 
                    videoLink = it
                    settingsManager.setVideoLink(it)
                },
                label = { Text("Featured Video Link") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Recording Folder Path", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showFolderPicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(recordingPath.ifBlank { "Select Folder..." })
            }
        }
    }

    if (showFolderPicker) {
        FolderPickerDialog(
            initialPath = recordingPath,
            onFolderSelected = { path ->
                recordingPath = path
                settingsManager.setRecordingPath(path)
                showFolderPicker = false
            },
            onDismiss = { showFolderPicker = false }
        )
    }
}

@Composable
fun FolderPickerDialog(initialPath: String, onFolderSelected: (String) -> Unit, onDismiss: () -> Unit) {
    val rootDir = Environment.getExternalStorageDirectory()
    var currentDir by remember { mutableStateOf(rootDir) }
    
    // Attempt to parse initial path cleanly relative to root
    LaunchedEffect(Unit) {
        if (initialPath.isNotBlank()) {
            val formattedPath = if (initialPath.startsWith("/")) initialPath else "/$initialPath"
            val target = File(rootDir, formattedPath)
            if (target.exists() && target.isDirectory) {
                currentDir = target
            }
        }
    }

    val directories = currentDir.listFiles { file: File -> file.isDirectory && !file.isHidden }?.sortedBy { it.name } ?: emptyList()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Select Folder")
                Text(
                    text = currentDir.absolutePath.replace(rootDir.absolutePath, "Storage"),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                if (currentDir.absolutePath != rootDir.absolutePath) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentDir = currentDir.parentFile ?: rootDir
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = OrangePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Up", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                items(directories) { dir ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currentDir = dir }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(dir.name)
                    }
                }
                if (directories.isEmpty()) {
                    item {
                        Text("No subdirectories", modifier = Modifier.padding(12.dp), color = Color.Gray)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val relativePath = currentDir.absolutePath.replace(rootDir.absolutePath, "")
                    onFolderSelected(if (relativePath.isEmpty()) "/" else relativePath)
                }
            ) {
                Text("Select This Folder", color = OrangePrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun PermissionItem(
    title: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isGranted) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (isGranted) Color(0xFF166534) else Color(0xFF991B1B)
        )
        
        Button(
            onClick = onClick,
            enabled = !isGranted,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OrangePrimary,
                disabledContainerColor = Color(0xFF22C55E)
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                if (isGranted) "Enabled" else "Setup",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
