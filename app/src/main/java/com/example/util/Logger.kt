package com.example.util

import android.util.Log

/**
 * A centralized logging utility to provide consistent log outputs across the application,
 * separating debug messages, user-facing errors, and telemetry/analytics events.
 */
object AppLogger {
    private const val GLOBAL_TAG = "EduVideoApp"

    fun d(tag: String, message: String) {
        Log.d("$GLOBAL_TAG:$tag", message)
    }

    fun i(tag: String, message: String) {
        Log.i("$GLOBAL_TAG:$tag", message)
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        Log.w("$GLOBAL_TAG:$tag", message, throwable)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e("$GLOBAL_TAG:$tag", message, throwable)
    }

    /**
     * Logs an analytics/milestone event cleanly without exposing sensitive user information.
     */
    fun event(eventName: String, parameters: Map<String, Any> = emptyMap()) {
        val paramsString = parameters.entries.joinToString(", ") { "${it.key}=${it.value}" }
        Log.i("$GLOBAL_TAG:Analytics", "Event: $eventName | Params: {$paramsString}")
    }
}
