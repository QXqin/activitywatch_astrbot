package net.activitywatch.android.watcher

import android.content.Context
import android.util.Log
import androidx.work.*
import net.activitywatch.android.AWPreferences
import net.activitywatch.android.RustInterface
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

private const val TAG = "RemoteSyncWorker"
private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

class RemoteSyncWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    private val prefs = AWPreferences(appContext)
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun doWork(): Result {
        // 清理 URL：去掉 /#/ 及尾部路径，确保是干净的基础地址
        val rawUrl = prefs.getRemoteServerUrl()
        val remoteUrl = rawUrl.trimEnd('/').removeSuffix("/#").removeSuffix("#").trimEnd('/')
        if (rawUrl != remoteUrl) {
            prefs.setRemoteServerUrl(remoteUrl)
            Log.i(TAG, "Cleaned remote URL: $rawUrl -> $remoteUrl")
        }
        if (!prefs.isRemoteSyncEnabled() || remoteUrl.isEmpty()) {
            Log.d(TAG, "Remote sync disabled or URL not set, skipping.")
            return Result.success()
        }

        return try {
            syncAllBuckets(remoteUrl)
            prefs.setLastSyncTimestamp(System.currentTimeMillis())
            Log.i(TAG, "Remote sync completed successfully.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Remote sync failed: ${e.message}")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private fun syncAllBuckets(remoteUrl: String) {
        val ri = RustInterface(applicationContext)
        val bucketsJson = ri.getBuckets()
        val bucketsObj = JSONObject(bucketsJson)
        val lastSyncMs = prefs.getLastSyncTimestamp()

        bucketsObj.keys().forEach { bucketId ->
            try {
                syncBucket(ri, remoteUrl, bucketId, lastSyncMs)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync bucket $bucketId: ${e.message}")
            }
        }
    }

    private fun syncBucket(
        ri: RustInterface,
        remoteUrl: String,
        bucketId: String,
        lastSyncMs: Long
    ) {
        val eventsJson = ri.getEvents(bucketId, 500)
        val eventsArray = JSONArray(eventsJson)

        if (eventsArray.length() == 0) {
            Log.d(TAG, "No events in bucket $bucketId, skipping.")
            return
        }

        val toUpload = JSONArray()
        for (i in 0 until eventsArray.length()) {
            val event = eventsArray.getJSONObject(i)
            val ts = event.optString("timestamp", "")
            if (lastSyncMs == 0L || isNewerThan(ts, lastSyncMs)) {
                toUpload.put(event)
            }
        }

        if (toUpload.length() == 0) {
            Log.d(TAG, "No new events in bucket $bucketId since last sync.")
            return
        }

        Log.i(TAG, "Uploading ${toUpload.length()} events for bucket $bucketId")
        ensureRemoteBucket(remoteUrl, bucketId)

        val url = "$remoteUrl/api/0/buckets/${encode(bucketId)}/events"
        val body = toUpload.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder().url(url).post(body).build()
        client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) {
                throw Exception("POST events failed: HTTP ${resp.code}")
            }
        }
    }

    private fun ensureRemoteBucket(remoteUrl: String, bucketId: String) {
        val checkUrl = "$remoteUrl/api/0/buckets/${encode(bucketId)}"
        val checkReq = Request.Builder().url(checkUrl).get().build()
        val exists = client.newCall(checkReq).execute().use { it.isSuccessful }
        if (!exists) {
            val payload = JSONObject().apply {
                put("id", bucketId)
                put("type", "currentwindow")
                put("client", "aw-android")
                put("hostname", android.os.Build.MODEL)
            }
            val body = payload.toString().toRequestBody(JSON_MEDIA_TYPE)
            val req = Request.Builder().url(checkUrl).post(body).build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful && resp.code != 304) {
                    Log.w(TAG, "Create bucket $bucketId returned ${resp.code}")
                }
            }
        }
    }

    private fun isNewerThan(isoTimestamp: String, sinceMs: Long): Boolean {
        return try {
            val ts = isoTimestamp.replace("Z", "+00:00")
            val instant = java.time.OffsetDateTime.parse(ts).toInstant().toEpochMilli()
            instant > sinceMs
        } catch (e: Exception) {
            true
        }
    }

    private fun encode(s: String): String =
        URLEncoder.encode(s, "UTF-8").replace("+", "%20")

    companion object {
        const val WORK_NAME = "aw_remote_sync_periodic"
    }
}
