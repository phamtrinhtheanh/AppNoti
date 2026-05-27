package com.express.appnoti

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class DeviceRepository(
    private val client: OkHttpClient = OkHttpClient()
) {
    fun subscribe(userId: String, app: String, token: String): ApiResult {
        val url = "${AppConfig.BASE_URL}/public/api/device/subscribe" +
            "?userId=${userId.encode()}&app=${app.encode()}&token=${token.encode()}"
        return get(url)
    }

    fun unsubscribe(userId: String, app: String): ApiResult {
        val url = "${AppConfig.BASE_URL}/public/api/device/unsubscribe" +
            "?userId=${userId.encode()}&app=${app.encode()}"
        return get(url)
    }

    fun notifications(userId: String, page: Int = 1, limit: Int = 20): NotificationListResult {
        val url = "${AppConfig.BASE_URL}/public/api/notification/list" +
            "?userId=${userId.encode()}&page=$page&limit=$limit&seen=-1"
        val body = executeGet(url)
        val json = JSONObject(body)
        if (json.optBoolean("error", false)) {
            return NotificationListResult(
                error = true,
                message = json.optString("message", "Load failed")
            )
        }

        val data = json.optJSONObject("data")
        val pageJson = data?.optJSONObject("page")
        val rows = data?.optJSONArray("data")
        val items = buildList {
            if (rows != null) {
                for (i in 0 until rows.length()) {
                    val row = rows.optJSONObject(i) ?: continue
                    add(
                        NotificationItem(
                            id = row.optLong("id"),
                            title = row.optString("title"),
                            content = row.optString("content"),
                            action = row.optString("action"),
                            icon = row.optString("icon"),
                            createDate = row.optLong("createDate"),
                            seen = row.optBoolean("seen", false),
                            mapExt = row.optJSONObject("mapExt")?.let { ext ->
                                buildMap {
                                    val keys = ext.keys()
                                    while (keys.hasNext()) {
                                        val key = keys.next()
                                        put(key, ext.optString(key))
                                    }
                                }
                            }.orEmpty()
                        )
                    )
                }
            }
        }

        return NotificationListResult(
            error = false,
            message = json.optString("message", "OK"),
            items = items,
            total = pageJson?.optInt("total") ?: items.size
        )
    }

    fun markAsSeen(userId: String, notificationId: Long, createDate: Long): ApiResult {
        val url = "${AppConfig.BASE_URL}/public/api/notification/seen/${notificationId}" +
                "?userId=${userId.encode()}&createDate=$createDate"
        val requestBody = "".toRequestBody(null)
        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .build()
        val body = executeRequest(request)
        val json = JSONObject(body)
        return ApiResult(
            error = json.optBoolean("error", false),
            message = json.optString("message", "OK"),
            data = json.opt("data")?.toString()
        )
    }

    private fun get(url: String): ApiResult {
        val request = Request.Builder().url(url).get().build()
        val body = executeRequest(request)
        val json = JSONObject(body)
        return ApiResult(
            error = json.optBoolean("error", false),
            message = json.optString("message", "OK"),
            data = json.opt("data")?.toString()
        )
    }

    private fun executeGet(url: String): String {
        val request = Request.Builder().url(url).get().build()
        return executeRequest(request)
    }

    private fun executeRequest(request: Request): String {
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("HTTP ${response.code}: $body")
            }
            return body
        }
    }

    private fun String.encode(): String =
        URLEncoder.encode(this, StandardCharsets.UTF_8.toString())
}

data class ApiResult(
    val error: Boolean,
    val message: String,
    val data: String? = null
)

data class NotificationListResult(
    val error: Boolean,
    val message: String,
    val items: List<NotificationItem> = emptyList(),
    val total: Int = 0
)

data class NotificationItem(
    val id: Long,
    val title: String,
    val content: String,
    val action: String,
    val icon: String,
    val createDate: Long,
    val seen: Boolean,
    val mapExt: Map<String, String> = emptyMap()
)
