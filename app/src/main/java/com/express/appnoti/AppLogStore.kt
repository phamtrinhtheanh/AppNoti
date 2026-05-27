package com.express.appnoti

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLogStore {
    private val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs

    fun add(message: String) {
        val line = "[${formatter.format(Date())}] $message"
        _logs.value = (_logs.value + line).takeLast(100)
    }

    fun clear() {
        _logs.value = emptyList()
    }
}
