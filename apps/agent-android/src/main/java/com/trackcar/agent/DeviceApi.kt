package com.trackcar.agent

import android.content.Context
import android.os.Build
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object DeviceApi {
    // Stable project URL is updated after the Vercel project is created.
    const val BASE_URL = "https://track-car-platform.vercel.app"

    fun enroll(context: Context, pairingCode: String, callback: (Boolean, String) -> Unit) {
        Thread {
            try {
                val info = JSONObject().apply {
                    put("manufacturer", Build.MANUFACTURER ?: "Unknown")
                    put("model", Build.MODEL ?: "Unknown")
                    put("codename", Build.DEVICE ?: "")
                    put("android_release", Build.VERSION.RELEASE ?: "")
                    put("sdk_int", Build.VERSION.SDK_INT)
                    put("build_fingerprint", Build.FINGERPRINT ?: "")
                    put("security_patch", if (Build.VERSION.SDK_INT >= 23) Build.VERSION.SECURITY_PATCH else "")
                    put("cpu_abis", JSONArray(Build.SUPPORTED_ABIS.toList()))
                    put("agent_version", "1.0.0")
                }
                val body = JSONObject().apply {
                    put("pairing_code", pairingCode)
                    put("device_label", Build.MODEL ?: "Android vehicle device")
                    put("device_info", info)
                    put("location_consent", true)
                }
                val response = request("/api/device/enroll", "POST", null, body)
                val token = response.optString("device_token")
                val deviceId = response.optString("device_id")
                if (token.isBlank() || deviceId.isBlank()) throw IllegalStateException(response.optString("error", "Enrollment response incomplete"))
                context.getSharedPreferences("trackcar_agent", Context.MODE_PRIVATE).edit()
                    .putString("device_token", token)
                    .putString("device_id", deviceId)
                    .putString("device_code", response.optString("device_code"))
                    .apply()
                callback(true, "Vehicle enrolled successfully. GPS sync is active.")
            } catch (e: Exception) {
                callback(false, "Enrollment failed: " + (e.message ?: "Network error"))
            }
        }.start()
    }

    fun heartbeat(context: Context) {
        val token = context.getSharedPreferences("trackcar_agent", Context.MODE_PRIVATE).getString("device_token", null) ?: return
        Thread {
            try {
                val battery = context.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
                val level = battery?.getIntExtra("level", -1) ?: -1
                val scale = battery?.getIntExtra("scale", -1) ?: -1
                val percent = if (level >= 0 && scale > 0) level * 100 / scale else -1
                val body = JSONObject().put("battery_percent", percent).put("agent_version", "1.0.0")
                request("/api/device/heartbeat", "POST", token, body)
            } catch (_: Exception) { }
        }.start()
    }

    fun sendLocation(context: Context, latitude: Double, longitude: Double, accuracy: Float, speedKmh: Double?) {
        val prefs = context.getSharedPreferences("trackcar_agent", Context.MODE_PRIVATE)
        val token = prefs.getString("device_token", null) ?: return
        val point = JSONObject().apply {
            put("latitude", latitude)
            put("longitude", longitude)
            put("accuracy_m", accuracy.toDouble())
            if (speedKmh != null) put("speed_kmh", speedKmh)
            put("recorded_at", java.time.Instant.now().toString())
        }
        Thread {
            try {
                val pending = prefs.getString("pending_locations", null)
                val points = JSONArray()
                if (!pending.isNullOrBlank()) {
                    val old = JSONArray(pending)
                    for (i in 0 until old.length()) points.put(old.getJSONObject(i))
                }
                points.put(point)
                while (points.length() > 100) points.remove(0)
                val body = JSONObject().put("locations", points)
                request("/api/device/locations", "POST", token, body)
                prefs.edit().remove("pending_locations").apply()
                heartbeat(context)
            } catch (_: Exception) {
                val old = prefs.getString("pending_locations", null)
                val points = try { if (old.isNullOrBlank()) JSONArray() else JSONArray(old) } catch (_: Exception) { JSONArray() }
                points.put(point)
                while (points.length() > 100) points.remove(0)
                prefs.edit().putString("pending_locations", points.toString()).apply()
            }
        }.start()
    }

    private fun request(path: String, method: String, token: String?, body: JSONObject): JSONObject {
        val connection = URL(BASE_URL + path).openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.connectTimeout = 10000
        connection.readTimeout = 15000
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("Content-Type", "application/json")
        if (!token.isNullOrBlank()) connection.setRequestProperty("Authorization", "Bearer " + token)
        connection.doOutput = true
        connection.outputStream.use { it.write(body.toString().toByteArray(StandardCharsets.UTF_8)) }
        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val text = stream?.bufferedReader()?.use { it.readText() } ?: "{}"
        connection.disconnect()
        val json = JSONObject(text)
        if (code !in 200..299) throw IllegalStateException(json.optString("error", "HTTP " + code))
        return json
    }
}
