package com.example.data.analytics

import android.util.Log

object CrashlyticsHelper {
    private const val TAG = "CrashlyticsHelper"

    fun log(message: String) {
        Log.d(TAG, "Log: $message")
        try {
            // Placeholder/Bridge to Real Firebase Crashlytics
            // com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().log(message)
        } catch (e: Exception) {
            Log.e(TAG, "Crashlytics is not fully initialized.", e)
        }
    }

    fun recordException(throwable: Throwable) {
        Log.e(TAG, "Recording non-fatal exception", throwable)
        try {
            // Placeholder/Bridge to Real Firebase Crashlytics
            // com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().recordException(throwable)
        } catch (e: Exception) {
            Log.e(TAG, "Crashlytics is not fully initialized.", e)
        }
    }

    fun setCustomKey(key: String, value: String) {
        Log.d(TAG, "Custom Key: $key = $value")
        try {
            // Placeholder/Bridge to Real Firebase Crashlytics
            // com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().setCustomKey(key, value)
        } catch (e: Exception) {
            Log.e(TAG, "Crashlytics is not fully initialized.", e)
        }
    }

    fun setUserId(userId: String) {
        Log.d(TAG, "User ID set: $userId")
        try {
            // Placeholder/Bridge to Real Firebase Crashlytics
            // com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().setUserId(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Crashlytics is not fully initialized.", e)
        }
    }
}
