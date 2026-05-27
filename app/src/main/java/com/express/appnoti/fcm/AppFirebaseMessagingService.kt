package com.express.appnoti.fcm

import com.express.appnoti.AppLogStore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AppFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        getSharedPreferences("app_noti", MODE_PRIVATE)
            .edit()
            .putString("fcm_token", token)
            .apply()
        AppLogStore.add("TOKEN REFRESH: ${token.take(32)}...")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        val title = message.notification?.title ?: data["title"] ?: "Notification"
        val body = message.notification?.body ?: data["content"] ?: data["body"] ?: ""

        AppLogStore.add("FCM: $title - $body")
        if (data.isNotEmpty()) {
            AppLogStore.add("DATA: $data")
        }
        NotificationHelper.show(this, title, body, data)
    }
}
