package com.express.appnoti

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("app_noti", Application.MODE_PRIVATE)
    private val repository = DeviceRepository()

    var userId = androidx.compose.runtime.mutableStateOf(prefs.getString("user_id", "") ?: "")
        private set
    var appName = androidx.compose.runtime.mutableStateOf(prefs.getString("app_name", AppConfig.APP_NAME) ?: AppConfig.APP_NAME)
        private set
    var fcmToken = androidx.compose.runtime.mutableStateOf(prefs.getString("fcm_token", "") ?: "")
        private set
    var loading = androidx.compose.runtime.mutableStateOf(false)
        private set
    var notificationLoading = androidx.compose.runtime.mutableStateOf(false)
        private set
    var notificationError = androidx.compose.runtime.mutableStateOf<String?>(null)
        private set
    var notifications = androidx.compose.runtime.mutableStateOf<List<NotificationItem>>(emptyList())
        private set
    var notificationTotal = androidx.compose.runtime.mutableStateOf(0)
        private set

    val logs: StateFlow<List<String>> = AppLogStore.logs

    init {
        AppLogStore.add("App ready. Firebase app=${AppConfig.APP_NAME}")
        refreshToken()
    }

    fun setUserId(value: String) {
        userId.value = value.filter { it.isDigit() }
    }

    fun setAppName(value: String) {
        appName.value = value.trim()
    }

    fun refreshToken() {
        AppLogStore.add("TOKEN: requesting native FCM token...")
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                fcmToken.value = token
                prefs.edit().putString("fcm_token", token).apply()
                AppLogStore.add("TOKEN OK: ${token.take(32)}...")
            }
            .addOnFailureListener { error ->
                AppLogStore.add("TOKEN FAIL: ${error.message}")
            }
    }

    fun subscribe() {
        val uid = userId.value.trim()
        val app = appName.value.trim()
        val token = fcmToken.value.trim()
        if (uid.isEmpty()) {
            AppLogStore.add("ERR: Nhap User ID")
            return
        }
        if (app.isEmpty()) {
            AppLogStore.add("ERR: Nhap app")
            return
        }
        if (token.isEmpty()) {
            AppLogStore.add("ERR: Chua co FCM token")
            return
        }

        loading.value = true
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.subscribe(uid, app, token)
            }
            prefs.edit()
                .putString("user_id", uid)
                .putString("app_name", app)
                .apply()
            loading.value = false
            if (result.error) {
                AppLogStore.add("SUB FAIL: ${result.message}")
            } else {
                AppLogStore.add("SUB OK: rows=${result.data ?: "-"}")
                loadNotifications()
            }
        }
    }

    fun unsubscribe() {
        val uid = userId.value.trim()
        val app = appName.value.trim()
        if (uid.isEmpty() || app.isEmpty()) {
            AppLogStore.add("ERR: Can User ID va app de unsubscribe")
            return
        }

        loading.value = true
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.unsubscribe(uid, app)
            }
            loading.value = false
            if (result.error) {
                AppLogStore.add("UNSUB FAIL: ${result.message}")
            } else {
                AppLogStore.add("UNSUB OK: rows=${result.data ?: "-"}")
            }
        }
    }

    fun loadNotifications() {
        val uid = userId.value.trim()
        if (uid.isEmpty()) {
            notificationError.value = "Nhap User ID o man Home truoc"
            notifications.value = emptyList()
            notificationTotal.value = 0
            return
        }

        notificationLoading.value = true
        notificationError.value = null
        viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    repository.notifications(uid)
                }
            }
            notificationLoading.value = false
            result
                .onSuccess { data ->
                    if (data.error) {
                        notificationError.value = data.message
                        notifications.value = emptyList()
                        notificationTotal.value = 0
                    } else {
                        notifications.value = data.items
                        notificationTotal.value = data.total
                        AppLogStore.add("LIST OK: ${data.items.size}/${data.total}")
                    }
                }
                .onFailure { error ->
                    notificationError.value = error.message ?: "Load failed"
                    notifications.value = emptyList()
                    notificationTotal.value = 0
                    AppLogStore.add("LIST FAIL: ${error.message}")
                }
        }
    }

    fun clearLogs() {
        AppLogStore.clear()
    }

    fun markAsSeen(notificationId: Long, createDate: Long) {
        val uid = userId.value.trim()
        if (uid.isEmpty()) return

        viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    repository.markAsSeen(uid, notificationId, createDate)
                }
            }
            result.onSuccess { data ->
                if (!data.error) {
                    notifications.value = notifications.value.map {
                        if (it.id == notificationId) it.copy(seen = true) else it
                    }
                    AppLogStore.add("SEEN OK: id=$notificationId")
                } else {
                    AppLogStore.add("SEEN FAIL: ${data.message}")
                }
            }.onFailure { error ->
                AppLogStore.add("SEEN FAIL: ${error.message}")
            }
        }
    }

    var selectedTabTarget = androidx.compose.runtime.mutableStateOf<AppTab?>(null)
        private set
    var selectedNotificationTarget = androidx.compose.runtime.mutableStateOf<Long?>(null)
        private set

    fun clearNavigationTargets() {
        selectedTabTarget.value = null
    }

    fun clearNotificationTarget() {
        selectedNotificationTarget.value = null
    }

    fun handleIntent(intent: Intent?) {
        AppLogStore.add("INTENT: action=${intent?.action} hasExtras=${intent?.extras != null}")
        if (intent == null) return
        val extras = intent.extras ?: return
        val data = extras.keySet().associateWith { key -> extras.get(key).toString() }
        if (data.isNotEmpty()) {
            AppLogStore.add("TAP: $data")
            
            val screen = data["screen"] ?: data["destination"] ?: data["action"]
            val notiIdStr = data["notification_id"] ?: data["id"]
            
            if (screen != null) {
                when {
                    screen.equals("notifications", ignoreCase = true) || screen.equals("notification", ignoreCase = true) -> {
                        selectedTabTarget.value = AppTab.Notifications
                    }
                    screen.equals("home", ignoreCase = true) -> {
                        selectedTabTarget.value = AppTab.Home
                    }
                }
            }
            
            if (notiIdStr != null) {
                val notiId = notiIdStr.toLongOrNull()
                if (notiId != null) {
                    selectedTabTarget.value = AppTab.Notifications
                    selectedNotificationTarget.value = notiId
                }
            }
        }
    }
}
