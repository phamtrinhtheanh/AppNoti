package com.express.appnoti

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.express.appnoti.fcm.NotificationHelper
import com.express.appnoti.ui.theme.AppNotiTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Notion & Linear-inspired Calm Premium Minimal Palette
private val PageBg = Color(0xFFFBFBFB)      // Soft clean Notion-like off-white background
private val CardBg = Color(0xFFFFFFFF)      // Pure white card canvas
private val Ink = Color(0xFF171719)         // Deep soft graphite text (not harsh black)
private val Muted = Color(0xFF7E7E86)       // Quiet gray for subtitles and captions
private val Line = Color(0xFFEBEBEF)        // Ultra-thin soft warm gray separator line
private val Subtle = Color(0xFFF4F4F6)      // Flat light gray for secondary container backgrounds
private val Primary = Color(0xFF171719)     // Monochrome charcoal black accent
private val Accent = Color(0xFF4F46E5)      // Tiny soft indigo drop for important status highlights

private val Success = Color(0xFF0D9488)     // Classy muted teal
private val Warning = Color(0xFFD97706)     // Classy muted amber
private val Danger = Color(0xFFE11D48)      // Classy muted rose

enum class AppTab(val label: String) {
    Home("Home"),
    Notifications("Notifications")
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.ensureChannel(this)

        setContent {
            AppNotiTheme(dynamicColor = false) {
                FcmTestScreen(viewModel)
            }
        }
        viewModel.handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.handleIntent(intent)
    }
}

@Composable
private fun FcmTestScreen(viewModel: MainViewModel) {
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.Home) }

    LaunchedEffect(viewModel.selectedTabTarget.value) {
        val target = viewModel.selectedTabTarget.value
        if (target != null) {
            selectedTab = target
            viewModel.clearNavigationTargets()
        }
    }

    Scaffold(
        containerColor = PageBg,
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = CardBg,
                tonalElevation = 0.dp,
                modifier = Modifier.border(width = 1.dp, color = Line, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = {
                            selectedTab = tab
                            if (tab == AppTab.Notifications) {
                                viewModel.loadNotifications()
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    AppTab.Home -> Icons.Filled.Home
                                    AppTab.Notifications -> Icons.Filled.Notifications
                                },
                                contentDescription = tab.label,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { 
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium
                            ) 
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Ink,
                            selectedTextColor = Ink,
                            indicatorColor = Subtle,
                            unselectedIconColor = Muted,
                            unselectedTextColor = Muted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = selectedTab,
            animationSpec = tween(durationMillis = 180),
            label = "tab-content"
        ) { tab ->
            when (tab) {
                AppTab.Home -> HomeScreen(viewModel, Modifier.padding(innerPadding))
                AppTab.Notifications -> NotificationScreen(viewModel, Modifier.padding(innerPadding))
            }
        }
    }
}

@Composable
private fun HomeScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val logs by viewModel.logs.collectAsState()
    val hasToken = viewModel.fcmToken.value.isNotBlank()
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        AppLogStore.add(if (granted) "PERMISSION OK" else "PERMISSION DENIED")
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Header(hasToken = hasToken, isLoading = viewModel.loading.value)

        ConfigPanel(viewModel = viewModel)

        TokenPanel(
            token = viewModel.fcmToken.value,
            onRefresh = viewModel::refreshToken,
            onCopy = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("FCM Token", viewModel.fcmToken.value))
                AppLogStore.add("COPY: token copied")
            }
        )

        LogPanel(logs = logs, onClear = viewModel::clearLogs)
    }
}

@Composable
private fun NotificationScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    var selectedNotification by rememberSaveable { mutableStateOf<Long?>(null) }
    val detailItem = viewModel.notifications.value.firstOrNull { it.id == selectedNotification }

    LaunchedEffect(viewModel.selectedNotificationTarget.value) {
        val target = viewModel.selectedNotificationTarget.value
        if (target != null) {
            selectedNotification = target
            viewModel.clearNotificationTarget()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    if (detailItem != null) {
        LaunchedEffect(detailItem.id) {
            if (!detailItem.seen) {
                viewModel.markAsSeen(detailItem.id, detailItem.createDate)
            }
        }
        NotificationDetailScreen(
            item = detailItem,
            onBack = { selectedNotification = null },
            modifier = modifier
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${viewModel.notificationTotal.value} total",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Muted
                        )
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(Line)
                        )
                        Text(
                            text = "user ${viewModel.userId.value.ifBlank { "-" }}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                MinimalButton(
                    text = "Refresh",
                    onClick = viewModel::loadNotifications,
                    enabled = !viewModel.notificationLoading.value,
                    style = ButtonStyle.Outline
                )
            }
        }

        if (viewModel.notificationLoading.value) {
            item {
                MinimalCard(title = "Syncing", subtitle = "Checking server mailbox") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Ink
                        )
                    }
                }
            }
        } else if (viewModel.notificationError.value != null) {
            item {
                MinimalCard(title = "Connection problem", subtitle = "Failed to sync") {
                    Text(
                        text = viewModel.notificationError.value.orEmpty(),
                        color = Danger,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else if (viewModel.notifications.value.isEmpty()) {
            item {
                EmptyNotificationsState()
            }
        } else {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Line),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        val items = viewModel.notifications.value
                        items.forEachIndexed { index, item ->
                            NotificationRow(
                                item = item,
                                onClick = { selectedNotification = item.id },
                                showDivider = index < items.lastIndex
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyNotificationsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Subtle)
                .border(1.dp, Line, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = null,
                tint = Muted.copy(alpha = 0.7f),
                modifier = Modifier.size(26.dp)
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Your inbox is empty",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
            Text(
                text = "Send a test notification from the home tab or use the curl CLI to see incoming signals.",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
private fun Header(hasToken: Boolean, isLoading: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "App Noti",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Text(
                    text = "FCM push receiver portal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted
                )
            }
            StatusPill(
                text = when {
                    isLoading -> "Syncing"
                    hasToken -> "Ready"
                    else -> "No token"
                },
                color = when {
                    isLoading -> Warning
                    hasToken -> Success
                    else -> Danger
                }
            )
        }

        EndpointBar(AppConfig.BASE_URL)
    }
}

@Composable
private fun StatusPill(text: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun EndpointBar(endpoint: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Subtle)
            .border(1.dp, Line, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "API",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Muted
        )
        Text(
            text = endpoint,
            style = MaterialTheme.typography.bodySmall,
            color = Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ConfigPanel(viewModel: MainViewModel) {
    MinimalCard(title = "Device registration", subtitle = "Link emulator tokens to backend users") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MinimalTextField(
                    value = viewModel.userId.value,
                    onValueChange = viewModel::setUserId,
                    label = "User ID",
                    modifier = Modifier.weight(1f)
                )
                MinimalTextField(
                    value = viewModel.appName.value,
                    onValueChange = viewModel::setAppName,
                    label = "App",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                MinimalButton(
                    text = "Subscribe",
                    onClick = viewModel::subscribe,
                    enabled = !viewModel.loading.value,
                    style = ButtonStyle.Solid,
                    loading = viewModel.loading.value,
                    modifier = Modifier.weight(1f)
                )
                MinimalButton(
                    text = "Unsubscribe",
                    onClick = viewModel::unsubscribe,
                    enabled = !viewModel.loading.value,
                    style = ButtonStyle.Outline,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TokenPanel(token: String, onRefresh: () -> Unit, onCopy: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }

    MinimalCard(title = "Firebase Token", subtitle = "FCM push token for this emulator") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SelectionContainer {
                Text(
                    text = token.ifBlank { "Awaiting FCM credentials..." },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Subtle)
                        .border(1.dp, Line, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (token.isBlank()) Muted else Ink
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MinimalButton(
                    text = "Refresh",
                    onClick = onRefresh,
                    style = ButtonStyle.Outline,
                    modifier = Modifier.weight(1f)
                )
                MinimalButton(
                    text = if (isCopied) "Copied! ✓" else "Copy Token",
                    onClick = {
                        onCopy()
                        isCopied = true
                        coroutineScope.launch {
                            delay(2000)
                            isCopied = false
                        }
                    },
                    enabled = token.isNotBlank(),
                    style = ButtonStyle.Solid,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LogPanel(logs: List<String>, onClear: () -> Unit) {
    var selectedFilter by remember { mutableStateOf("All") }
    
    val filteredLogs = remember(logs, selectedFilter) {
        when (selectedFilter) {
            "Subs" -> logs.filter { it.contains("SUB") || it.contains("UNSUB") }
            "Errors" -> logs.filter { it.contains("FAIL") || it.contains("ERR") }
            else -> logs
        }
    }

    MinimalCard(
        title = "Console logs",
        subtitle = "${logs.size} transactions",
        trailing = {
            MinimalButton(
                text = "Clear",
                onClick = onClear,
                style = ButtonStyle.Outline
            )
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Notion-like Minimal Filter Toggles
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                listOf("All", "Subs", "Errors").forEach { filter ->
                    val active = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (active) Primary else Subtle)
                            .clickable { selectedFilter = filter }
                            .border(1.dp, if (active) Primary else Line, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (active) Color.White else Muted,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Subtle,
                border = androidx.compose.foundation.BorderStroke(1.dp, Line)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (filteredLogs.isEmpty()) {
                        item {
                            LogLine("Log terminal empty. Awaiting signals...")
                        }
                    } else {
                        items(filteredLogs) { line ->
                            LogRow(line)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(
    item: NotificationItem,
    onClick: () -> Unit,
    showDivider: Boolean
) {
    val unreadBgColor = if (!item.seen) Accent.copy(alpha = 0.03f) else Color.Transparent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(unreadBgColor)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!item.seen) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Accent)
                    )
                }
                
                Text(
                    text = item.title.ifBlank { "Untitled Notification" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (!item.seen) FontWeight.Bold else FontWeight.Medium,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = formatCreateDateCompact(item.createDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted.copy(alpha = 0.8f)
                )
                
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Muted.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Spacer(Modifier.height(4.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = if (!item.seen) 14.dp else 0.dp)
            ) {
                Text(
                    text = item.content.ifBlank { "No body content" },
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                if (item.action.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Subtle)
                            .border(0.5.dp, Line, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = item.action,
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Line)
            )
        }
    }
}

private fun formatCreateDateCompact(value: Long): String {
    if (value <= 0L) return "-"
    return SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(value))
}

@Composable
private fun NotificationDetailScreen(
    item: NotificationItem,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.dp, Line, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Ink,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Notification Detail",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                    Text(
                        text = formatCreateDate(item.createDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted
                    )
                }
            }
        }

        item {
            MinimalCard(
                title = item.title.ifBlank { "Untitled Notification" },
                subtitle = "Mailbox ID ${item.id}"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = item.content.ifBlank { "No content body" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = Ink
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!item.seen) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Accent.copy(alpha = 0.08f))
                                    .border(1.dp, Accent.copy(alpha = 0.2f), RoundedCornerShape(999.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Unread",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Accent
                                )
                            }
                        }
                        if (item.action.isNotBlank()) {
                            MetaPill(label = "Action", value = item.action)
                        }
                    }
                }
            }
        }

        if (item.mapExt.isNotEmpty()) {
            item {
                MinimalCard(title = "System Payload", subtitle = "${item.mapExt.size} fields transferred") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item.mapExt.forEach { (key, value) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Subtle)
                                    .border(1.dp, Line, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = key,
                                    modifier = Modifier.weight(0.35f),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink
                                )
                                Text(
                                    text = value,
                                    modifier = Modifier.weight(0.65f),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Muted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaPill(label: String, value: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Subtle)
            .border(1.dp, Line, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Muted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LogLine(line: String) {
    Text(
        text = line,
        color = Muted,
        fontFamily = FontFamily.Monospace,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun LogRow(line: String) {
    val bracketIndex = line.indexOf(']')
    if (bracketIndex != -1 && bracketIndex + 1 < line.length) {
        val timestamp = line.substring(1, bracketIndex) // HH:mm:ss
        val message = line.substring(bracketIndex + 1).trim()
        
        val (icon, color, label) = when {
            message.contains("TOKEN OK") -> Triple("🔑", Success, "TOKEN")
            message.contains("TOKEN FAIL") -> Triple("🔑", Danger, "TOKEN")
            message.contains("TOKEN") -> Triple("🔑", Accent, "TOKEN")
            message.contains("SUB OK") -> Triple("🎯", Success, "SUB")
            message.contains("SUB FAIL") -> Triple("🎯", Danger, "SUB")
            message.contains("UNSUB OK") -> Triple("🔌", Success, "UNSUB")
            message.contains("UNSUB FAIL") -> Triple("🔌", Danger, "UNSUB")
            message.contains("LIST OK") -> Triple("📬", Success, "LIST")
            message.contains("LIST FAIL") -> Triple("📬", Danger, "LIST")
            message.contains("SEEN OK") -> Triple("👁", Success, "SEEN")
            message.contains("SEEN FAIL") -> Triple("👁", Danger, "SEEN")
            message.contains("TAP") -> Triple("🖱", Accent, "TAP")
            message.contains("PERMISSION OK") -> Triple("🛡", Success, "PERM")
            message.contains("PERMISSION DENIED") -> Triple("🛡", Danger, "PERM")
            else -> Triple("⚙", Muted, "SYS")
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = timestamp,
                color = Muted.copy(alpha = 0.7f),
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall
            )
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(color.copy(alpha = 0.06f))
                    .border(1.dp, color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = icon, style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = label,
                        color = color,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Text(
                text = message.substringAfter(":").trim(),
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall
            )
        }
    } else {
        Text(
            text = line,
            color = Ink,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

// PREMIUM MINIMALIST DESIGN COMPONENT LIBRARY

enum class ButtonStyle {
    Solid, Outline
}

@Composable
private fun MinimalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: ButtonStyle = ButtonStyle.Solid,
    loading: Boolean = false
) {
    val shape = RoundedCornerShape(18.dp)
    
    if (style == ButtonStyle.Solid) {
        Button(
            onClick = onClick,
            enabled = enabled && !loading,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary,
                contentColor = Color.White,
                disabledContainerColor = Primary.copy(alpha = 0.3f),
                disabledContentColor = Color.White.copy(alpha = 0.5f)
            ),
            modifier = modifier.height(44.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            border = androidx.compose.foundation.BorderStroke(1.dp, Line),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Ink,
                disabledContentColor = Muted
            ),
            modifier = modifier.height(44.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MinimalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = Muted
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            unfocusedBorderColor = Line,
            focusedLabelColor = Primary,
            unfocusedLabelColor = Muted,
            focusedTextColor = Ink,
            unfocusedTextColor = Ink,
            cursorColor = Primary,
            focusedContainerColor = CardBg,
            unfocusedContainerColor = CardBg
        ),
        modifier = modifier
    )
}

@Composable
private fun MinimalCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = CardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, Line),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (trailing != null) {
                    Spacer(Modifier.width(8.dp))
                    trailing()
                }
            }
            content()
        }
    }
}

private fun formatCreateDate(value: Long): String {
    if (value <= 0L) return "-"
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(value))
}
