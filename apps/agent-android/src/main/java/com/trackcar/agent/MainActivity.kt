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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private var pendingPairingCode: String = ""
    private var statusCallback: ((String) -> Unit)? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            Build.VERSION.SDK_INT < 23
        ) enrollPendingDevice()
        else statusCallback?.invoke("Location permission is required to enroll this vehicle.")
    }

    private val backgroundPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        getSharedPreferences("trackcar_agent", MODE_PRIVATE).edit().putBoolean("auto_start", granted).apply()
        statusCallback?.invoke(if (granted) "Automatic restart permission enabled." else "Background location permission was not granted.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgentScreen(
                onEnroll = { code, update ->
                    statusCallback = update
                    pendingPairingCode = code.trim()
                    if (pendingPairingCode.length < 20) update("Enter a valid pairing code from the fleet administrator.")
                    else requestTrackingPermissions()
                },
                onEnableAutoStart = { requestBackgroundPermission() },
                onStop = {
                    getSharedPreferences("trackcar_agent", MODE_PRIVATE).edit().putBoolean("auto_start", false).apply()
                    stopService(Intent(this, TrackingService::class.java))
                    statusCallback?.invoke("Location service stopped.")
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("trackcar_agent", MODE_PRIVATE)
        if (prefs.getBoolean("pending_auto_start", false) && Build.VERSION.SDK_INT >= 30) {
            val granted = checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
            prefs.edit().putBoolean("pending_auto_start", false).putBoolean("auto_start", granted).apply()
        }
    }

    private fun requestTrackingPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= 33) permissions += Manifest.permission.POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= 23) locationPermissionLauncher.launch(permissions.toTypedArray())
        else enrollPendingDevice()
    }

    private fun enrollPendingDevice() {
        statusCallback?.invoke("Connecting securely to Track Car…")
        DeviceApi.enroll(this, pendingPairingCode) { success, message ->
            runOnUiThread {
                statusCallback?.invoke(message)
                if (success) {
                    getSharedPreferences("trackcar_agent", MODE_PRIVATE).edit().putBoolean("auto_start", true).apply()
                    val intent = Intent(this, TrackingService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent) else startService(intent)
                }
            }
        }
    }

    private fun requestBackgroundPermission() {
        if (Build.VERSION.SDK_INT >= 30 &&
            checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            getSharedPreferences("trackcar_agent", MODE_PRIVATE).edit().putBoolean("pending_auto_start", true).apply()
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + packageName)))
        } else if (Build.VERSION.SDK_INT == 29 &&
            checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            getSharedPreferences("trackcar_agent", MODE_PRIVATE).edit().putBoolean("auto_start", true).apply()
            statusCallback?.invoke("Automatic restart permission enabled.")
        }
    }
}

@Composable
private fun AgentScreen(
    onEnroll: (String, (String) -> Unit) -> Unit,
    onEnableAutoStart: () -> Unit,
    onStop: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var consent by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Not enrolled. Request a pairing code from your fleet administrator.") }
    MaterialTheme(colorScheme = androidx.compose.material3.darkColorScheme(primary = Color(0xFF38C7D8), background = Color(0xFF0B1424))) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0B1424)) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("TRACK CAR", color = Color(0xFF38C7D8))
                Text("Vehicle Agent", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                Text("Secure vehicle enrollment and GPS sync", color = Color(0xFFB7C7DA))
                TextField(value = code, onValueChange = { code = it }, label = { Text("Fleet pairing code") }, singleLine = true)
                androidx.compose.foundation.layout.Row {
                    Checkbox(checked = consent, onCheckedChange = { consent = it })
                    Text("I authorize this company vehicle device to share its location with the fleet administrator.", color = Color(0xFFB7C7DA))
                }
                Button(onClick = { if (consent) onEnroll(code) { status = it } else status = "Please confirm the vehicle location notice first." }) { Text("Enroll vehicle and start") }
                Button(onClick = onEnableAutoStart) { Text("Allow background restart") }
                Button(onClick = onStop) { Text("Stop location service") }
                Text(status, color = if (status.contains("failed") || status.contains("required")) Color(0xFFFF9A9A) else Color(0xFF9BE4D1))
                Text("While tracking is active, Android displays a location-service notification. Some vehicle head units may restrict automatic restart.", color = Color(0xFF8299B2))
            }
        }
    }
}
