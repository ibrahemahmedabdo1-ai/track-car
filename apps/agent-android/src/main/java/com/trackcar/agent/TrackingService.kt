package com.trackcar.agent

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper

class TrackingService : Service(), LocationListener {
    private lateinit var locationManager: LocationManager

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            getSharedPreferences("trackcar_agent", MODE_PRIVATE).edit().putBoolean("auto_start", false).apply()
            stopSelf()
            return START_NOT_STICKY
        }
        startForeground(NOTIFICATION_ID, buildNotification("Waiting for GPS fix"))
        DeviceApi.heartbeat(this)
        beginLocationUpdates()
        return START_STICKY
    }

    private fun hasLocationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < 23) return true
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun beginLocationUpdates() {
        if (!hasLocationPermission()) {
            stopSelf()
            return
        }
        try {
            val providers = locationManager.getProviders(true)
            val fine = Build.VERSION.SDK_INT < 23 ||
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val provider = when {
                providers.contains(LocationManager.GPS_PROVIDER) && fine -> LocationManager.GPS_PROVIDER
                providers.contains(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                else -> null
            } ?: return
            locationManager.requestLocationUpdates(provider, 30_000L, 25f, this, Looper.getMainLooper())
            locationManager.getLastKnownLocation(provider)?.let { onLocationChanged(it) }
        } catch (_: SecurityException) {
            stopSelf()
        } catch (_: IllegalArgumentException) {
            stopSelf()
        }
    }

    override fun onLocationChanged(location: Location) {
        getSharedPreferences("trackcar_agent", MODE_PRIVATE).edit()
            .putLong("last_location_at", System.currentTimeMillis())
            .putString("last_latitude", location.latitude.toString())
            .putString("last_longitude", location.longitude.toString())
            .putFloat("last_accuracy_m", location.accuracy)
            .apply()
        val speed = if (location.hasSpeed()) location.speed.toDouble() * 3.6 else null
        DeviceApi.sendLocation(this, location.latitude, location.longitude, location.accuracy, speed)
        val accuracy = location.accuracy.toInt()
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification("GPS active · accuracy about " + accuracy + "m"))
    }

    @Deprecated("Deprecated by Android; retained for API 21 compatibility")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
    override fun onProviderEnabled(provider: String) = Unit
    override fun onProviderDisabled(provider: String) = Unit
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        try { locationManager.removeUpdates(this) } catch (_: Exception) { }
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Vehicle location", NotificationManager.IMPORTANCE_LOW)
            channel.description = "Visible status while Track Car is collecting vehicle location"
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(status: String): Notification {
        val immutableFlag = if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
        val openApp = PendingIntent.getActivity(
            this, 1, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag
        )
        val stop = PendingIntent.getService(
            this, 2, Intent(this, TrackingService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag
        )
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        return builder
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Track Car location service")
            .setContentText(status)
            .setContentIntent(openApp)
            .setOngoing(true)
            .addAction(Notification.Action.Builder(android.R.drawable.ic_media_pause, "Stop", stop).build())
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "trackcar_location"
        private const val NOTIFICATION_ID = 6101
        private const val ACTION_STOP = "com.trackcar.agent.STOP_TRACKING"
    }
}
