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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
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

// Strictly minimalist black and white (monochrome) color scheme
private val PageBg = Color(0xFFF8FAFC)      // Slate 50 (airy clean background)
private val CardBg = Color(0xFFFFFFFF)      // Pure white card surfaces
private val Ink = Color(0xFF0F172A)         // Slate 900 (deep charcoal text)
private val Muted = Color(0xFF64748B)       // Slate 500 (soft muted text)
private val Line = Color(0xFFE2E8F0)        // Slate 200 (subtle separator lines)
private val Subtle = Color(0xFFF1F5F9)      // Slate 100 (light gray containers)
private val Primary = Color(0xFF0F172A)     // Slate 900 (primary monochrome accent)
private val Accent = Color(0xFF475569)      // Slate 600 (secondary monochrome accent)

private val Success = Color(0xFF0F172A)     // Monochrome Success (Black)
private val Warning = Color(0xFF475569)     // Monochrome Warning (Slate Gray)
private val Danger = Color(0xFF94A3B8)      // Monochrome Danger (Light Gray)

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Surface(
                    color = CardBg.copy(alpha = 0.98f),
                    tonalElevation = 8.dp,
                    shadowElevation = 16.dp,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Line,
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab
                            val contentColor = if (isSelected) Primary else Muted
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable(
                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            selectedTab = tab
                                            if (tab == AppTab.Notifications) {
                                                viewModel.loadNotifications()
                                            }
                                        }
                                    )
                                    .padding(vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = when (tab) {
                                        AppTab.Home -> Icons.Filled.Home
                                        AppTab.Notifications -> Icons.Filled.Notifications
                                    },
                                    contentDescription = tab.label,
                                    tint = contentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = contentColor
                                )
                                
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .size(width = 16.dp, height = 3.dp)
                                            .clip(RoundedCornerShape(99.dp))
                                            .background(Primary)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .size(width = 16.dp, height = 3.dp)
                                            .background(Color.Transparent)
                                    )
                                }
                            }
                        }
                    }
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
        Header(
            hasToken = hasToken,
            isLoading = viewModel.loading.value,
            apiUrl = viewModel.apiUrl.value,
            onApiUrlChange = viewModel::setApiUrl
        )

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
    
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf("All") }

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

    // Filter notifications locally based on search query and selected filter tab
    val filteredNotifications = remember(viewModel.notifications.value, searchQuery, selectedFilter) {
        viewModel.notifications.value.filter { item ->
            val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) ||
                    item.content.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "Unread" -> !item.seen
                "Action" -> item.action.isNotBlank()
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Redesigned modern bright header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Inbox",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Ink
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val unreadCount = viewModel.notifications.value.count { !it.seen }
                        Text(
                            text = "$unreadCount unread",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Muted.copy(alpha = 0.5f))
                        )
                        Text(
                            text = "${viewModel.notificationTotal.value} total",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Muted
                        )
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MinimalButton(
                        text = "Mark Read",
                        onClick = { viewModel.markAllAsSeen() },
                        enabled = viewModel.notifications.value.any { !it.seen } && !viewModel.notificationLoading.value,
                        style = ButtonStyle.Outline
                    )
                    MinimalButton(
                        text = "Refresh",
                        onClick = viewModel::loadNotifications,
                        enabled = !viewModel.notificationLoading.value,
                        style = ButtonStyle.Solid
                    )
                }
            }
        }

        // Search Bar Item
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search notifications...", color = Muted) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Muted
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Muted
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Line,
                    focusedContainerColor = CardBg,
                    unfocusedContainerColor = CardBg,
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink,
                    cursorColor = Primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
        }

        // Filter Chips Row
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                listOf("All", "Unread", "Action").forEach { filter ->
                    val active = selectedFilter == filter
                    val chipBg = if (active) Primary else Subtle
                    val chipTextColor = if (active) Color.White else Muted
                    val chipBorderColor = if (active) Primary else Line
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(chipBg)
                            .border(1.dp, chipBorderColor, RoundedCornerShape(10.dp))
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filter,
                            color = chipTextColor,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (viewModel.notificationLoading.value) {
            item {
                MinimalCard(title = "Syncing Inbox", subtitle = "Checking backend database...") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp,
                            color = Primary
                        )
                    }
                }
            }
        } else if (viewModel.notificationError.value != null) {
            item {
                MinimalCard(title = "Connection problem", subtitle = "Sync failed") {
                    Text(
                        text = viewModel.notificationError.value.orEmpty(),
                        color = Danger,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else if (filteredNotifications.isEmpty()) {
            item {
                EmptyNotificationsState()
            }
        } else {
            // Render individual cards with spacing
            items(filteredNotifications) { item ->
                NotificationRow(
                    item = item,
                    onClick = { selectedNotification = item.id }
                )
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
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Primary.copy(alpha = 0.05f))
                .border(1.dp, Primary.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Your inbox is clear",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
            Text(
                text = "No notifications match the filter. Send a test message or check your subscription.",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
private fun Header(
    hasToken: Boolean,
    isLoading: Boolean,
    apiUrl: String,
    onApiUrlChange: (String) -> Unit
) {
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

        EndpointBar(endpoint = apiUrl, onEndpointChange = onApiUrlChange)
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
private fun EndpointBar(endpoint: String, onEndpointChange: (String) -> Unit) {
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
        androidx.compose.foundation.text.BasicTextField(
            value = endpoint,
            onValueChange = onEndpointChange,
            textStyle = MaterialTheme.typography.bodySmall.copy(color = Ink),
            singleLine = true,
            modifier = Modifier.weight(1f),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(Ink)
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
    onClick: () -> Unit
) {
    // Beautiful clean monochrome style: light slate-gray for unread, white for read
    val cardBgColor = if (!item.seen) Subtle else CardBg
    val borderColor = Line
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = cardBgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Monochrome icon selection based on item.icon
                val iconVector = when (item.icon.lowercase()) {
                    "success" -> Icons.Filled.CheckCircle
                    "warning" -> Icons.Filled.Warning
                    "danger", "error" -> Icons.Filled.Error
                    "info" -> Icons.Filled.Info
                    else -> Icons.Filled.Notifications
                }
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Subtle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = Ink,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!item.seen) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Ink)
                            )
                        }
                        
                        Text(
                            text = item.title.ifBlank { "Untitled Notification" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (!item.seen) FontWeight.Bold else FontWeight.SemiBold,
                            color = Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = formatCreateDateCompact(item.createDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                            fontWeight = if (!item.seen) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    
                    Spacer(Modifier.height(4.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.content.ifBlank { "No body content" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (!item.seen) Ink.copy(alpha = 0.85f) else Muted,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        
                        if (item.action.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Subtle)
                                    .border(1.dp, Line, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = item.action,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Muted.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
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
    val context = LocalContext.current
    var copiedKey by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

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
                        .size(40.dp)
                        .border(1.dp, Line, CircleShape)
                        .background(CardBg)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Ink,
                        modifier = Modifier.size(18.dp)
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
                                    .background(Primary.copy(alpha = 0.08f))
                                    .border(1.dp, Primary.copy(alpha = 0.2f), RoundedCornerShape(999.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Unread",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                            }
                        }
                        if (item.action.isNotBlank()) {
                            MetaPill(label = "Action", value = item.action)
                        }
                        if (item.icon.isNotBlank()) {
                            MetaPill(label = "Icon Type", value = item.icon)
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
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = value,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Muted
                                    )
                                }
                                
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText(key, value))
                                        copiedKey = key
                                        AppLogStore.add("COPY: payload '$key' copied")
                                        coroutineScope.launch {
                                            delay(2000)
                                            if (copiedKey == key) copiedKey = null
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (copiedKey == key) Icons.Default.Done else Icons.Default.ContentCopy,
                                        contentDescription = "Copy Value",
                                        tint = if (copiedKey == key) Success else Muted,
                                        modifier = Modifier.size(16.dp)
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
        
        val label = when {
            message.contains("TOKEN OK") -> "TOKEN_OK"
            message.contains("TOKEN FAIL") -> "TOKEN_ERR"
            message.contains("TOKEN") -> "TOKEN"
            message.contains("SUB OK") -> "SUB_OK"
            message.contains("SUB FAIL") -> "SUB_ERR"
            message.contains("UNSUB OK") -> "UNSUB_OK"
            message.contains("UNSUB FAIL") -> "UNSUB_ERR"
            message.contains("LIST OK") -> "LIST_OK"
            message.contains("LIST FAIL") -> "LIST_ERR"
            message.contains("SEEN OK") -> "SEEN_OK"
            message.contains("SEEN FAIL") -> "SEEN_ERR"
            message.contains("TAP") -> "TAP"
            message.contains("PERMISSION OK") -> "PERM_OK"
            message.contains("PERMISSION DENIED") -> "PERM_DENIED"
            else -> "SYS"
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = timestamp,
                color = Muted,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall
            )
            
            Text(
                text = "[$label]",
                color = Ink,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(90.dp)
            )
            
            Text(
                text = message.substringAfter(":").trim(),
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
        }
    } else {
        Text(
            text = line,
            color = Ink,
            fontFamily = FontFamily.Monospace,
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
