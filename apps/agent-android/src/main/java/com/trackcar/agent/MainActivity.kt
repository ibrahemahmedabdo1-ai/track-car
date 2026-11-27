package com.trackcar.agent

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgentScreen(
                onStart = { requestTrackingPermissions() },
                onEnableAutoStart = { requestBackgroundPermission() },
                onStop = {
                    getSharedPreferences("trackcar_agent", MODE_PRIVATE).edit().putBoolean("auto_start", false).apply()
                    stopService(Intent(this, TrackingService::class.java))
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("trackcar_agent", MODE_PRIVATE)
        if (prefs.getBoolean("pending_auto_start", false)) {
            val granted = hasBackgroundLocationPermission()
            prefs.edit().putBoolean("pending_auto_start", false).putBoolean("auto_start", granted).apply()
        }
    }

    private fun requestTrackingPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= 33) permissions += Manifest.permission.POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= 23) requestPermissions(permissions.toTypedArray(), 4101)
        else startTrackingService()
    }

    private fun hasBackgroundLocationPermission(): Boolean =
        Build.VERSION.SDK_INT < 29 ||
            checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestBackgroundPermission() {
        if (Build.VERSION.SDK_INT >= 30 &&
            checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            getSharedPreferences("trackcar_agent", MODE_PRIVATE).edit().putBoolean("pending_auto_start", true).apply()
            val settingsIntent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + packageName)
            )
            startActivity(settingsIntent)
        } else if (Build.VERSION.SDK_INT == 29 &&
            checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 4102)
        } else {
            getSharedPreferences("trackcar_agent", MODE_PRIVATE).edit().putBoolean("auto_start", true).apply()
        }
    }

    private fun startTrackingService() {
        val intent = Intent(this, TrackingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent) else startService(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 4101) {
            val fine = Build.VERSION.SDK_INT < 23 || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val coarse = Build.VERSION.SDK_INT < 23 || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (fine || coarse) startTrackingService()
        } else if (requestCode == 4102 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getSharedPreferences("trackcar_agent", MODE_PRIVATE).edit().putBoolean("auto_start", true).apply()
        }
    }
}

@androidx.compose.runtime.Composable
private fun AgentScreen(onStart: () -> Unit, onEnableAutoStart: () -> Unit, onStop: () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF38C7D8), background = Color(0xFF0B1424))) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0B1424)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("TRACK CAR", color = Color(0xFF38C7D8))
                Text("Vehicle Agent", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                Text("Automatic fleet location service", color = Color(0xFFB7C7DA))
                Text(
                    "Location starts only after an authorized operator enables it. Android shows an ongoing notification while tracking is active.",
                    color = Color(0xFF9BAEC5)
                )
                Button(onClick = onStart) { Text("Start location service") }
                Button(onClick = onEnableAutoStart) { Text("Allow restart after reboot") }
                Button(onClick = onStop) { Text("Stop location service") }
                Text(
                    "Current stage: GPS samples are stored locally. Secure enrollment and live server sync are not connected yet.",
                    color = Color(0xFFFFC46B)
                )
            }
        }
    }
}
