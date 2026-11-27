package com.trackcar.installer

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF0B1424)
private val Panel = Color(0xFF17263B)
private val Accent = Color(0xFF38C7D8)
private val Muted = Color(0xFF9BAEC5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { InstallerScreen() }
    }
}

@Composable
private fun InstallerScreen() {
    val deviceName = listOf(Build.MANUFACTURER, Build.MODEL)
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString(" ")
    MaterialTheme(colorScheme = darkColorScheme(primary = Accent, background = Background, surface = Panel)) {
        Surface(modifier = Modifier.fillMaxSize(), color = Background) {
            Column(
                modifier = Modifier.fillMaxSize().padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(Modifier.height(12.dp))
                Text("TRACK CAR", color = Accent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Device setup", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                Text("This setup app identifies the Android device so the platform can select a compatible installation path.", color = Muted, fontSize = 14.sp)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Panel)
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Device profile", color = Color.White, fontWeight = FontWeight.SemiBold)
                        InfoRow("Manufacturer / model", deviceName.ifBlank { "Unknown" })
                        InfoRow("Android version", Build.VERSION.RELEASE ?: "Unknown")
                        InfoRow("Android SDK", Build.VERSION.SDK_INT.toString())
                        InfoRow("Build fingerprint", Build.FINGERPRINT.take(72))
                    }
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Panel)
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Installation status", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Diagnostics foundation installed", color = Accent, fontWeight = FontWeight.Medium)
                        Text("Secure enrollment, signed Agent download, and device-specific root checks will be enabled after the backend and release verification are connected.", color = Muted, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.weight(1f))
                Text("No firmware is changed and no root method is attempted by this diagnostic screen.", color = Muted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Muted, fontSize = 12.sp, modifier = Modifier.weight(0.9f))
        Text(value, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1.1f))
    }
}
