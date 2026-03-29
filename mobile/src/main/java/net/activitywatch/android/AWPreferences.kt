package net.activitywatch.android

import android.content.Context
import android.content.SharedPreferences

class AWPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AWPreferences", Context.MODE_PRIVATE)

    // To check if it is the first time the app is being run
    // Set to false when user finishes onboarding
    fun isFirstTime(): Boolean {
        return sharedPreferences.getBoolean("isFirstTime", true)
    }

    // To set the first time flag to false after the first run
    fun setFirstTimeRunFlag() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isFirstTime", false)
        editor.apply()
    }

    // Optional: To reset the first time flag to true (for debugging, perhaps)
    fun resetFirstTimeRunFlag() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isFirstTime", true)
        editor.apply()
    }

    // 远程同步服务器 URL（空字符串表示未配置）
    fun getRemoteServerUrl(): String {
        return sharedPreferences.getString("remoteServerUrl", "") ?: ""
    }

    fun setRemoteServerUrl(url: String) {
        sharedPreferences.edit().putString("remoteServerUrl", url.trimEnd('/')).apply()
    }

    // 上次同步时间戳（毫秒），用于增量同步
    fun getLastSyncTimestamp(): Long {
        return sharedPreferences.getLong("lastSyncTimestamp", 0L)
    }

    fun setLastSyncTimestamp(ts: Long) {
        sharedPreferences.edit().putLong("lastSyncTimestamp", ts).apply()
    }

    // 远程同步开关
    fun isRemoteSyncEnabled(): Boolean {
        return sharedPreferences.getBoolean("remoteSyncEnabled", false)
    }

    fun setRemoteSyncEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("remoteSyncEnabled", enabled).apply()
    }
}