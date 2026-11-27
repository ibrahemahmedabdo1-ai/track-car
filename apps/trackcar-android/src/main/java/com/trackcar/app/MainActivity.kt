package com.trackcar.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val Bg = Color(0xFF0B1424)
private val Panel = Color(0xFF17263B)
private val Cyan = Color(0xFF38C7D8)
private val Muted = Color(0xFF9BAEC5)

class MainActivity : ComponentActivity() {
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val token = intent.data?.lastPathSegment ?: return
        if (token.matches(Regex("[a-fA-F0-9]{64}"))) {
            startActivity(
                Intent(this, com.trackcar.agent.MainActivity::class.java)
                    .putExtra("install_invite_token", token)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inviteToken = intent?.data?.lastPathSegment
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Cyan, background = Bg, surface = Panel)) {
                Surface(Modifier.fillMaxSize(), color = Bg) {
                    Column(
                        Modifier.fillMaxSize().padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Spacer(Modifier.height(18.dp))
                        Text("TRACK CAR", color = Cyan, fontWeight = FontWeight.Bold)
                        Text("One app. Three workspaces.", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("Choose the workspace assigned to your role. Vehicle location is shared only after the device user approves the location notice and Android permissions.", color = Muted)
                        WorkspaceCard("Fleet administrator", "Manage registered devices, pairing codes and fleet status.") {
                            startActivity(Intent(this@MainActivity, com.trackcar.admin.MainActivity::class.java))
                        }
                        WorkspaceCard("Vehicle device", "Connect this authorized vehicle and manage visible GPS tracking.") {
                            val i = Intent(this@MainActivity, com.trackcar.agent.MainActivity::class.java)
                            if (!inviteToken.isNullOrBlank()) i.putExtra("install_invite_token", inviteToken)
                            startActivity(i)
                        }
                        WorkspaceCard("Device diagnostics", "View Android model, version and compatibility information.") {
                            startActivity(Intent(this@MainActivity, com.trackcar.installer.MainActivity::class.java))
                        }
                        Spacer(Modifier.weight(1f))
                        Text("Tracking is never hidden. Android displays an ongoing location notification while GPS collection is active.", color = Muted, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkspaceCard(title: String, description: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
        onClick = onClick
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(description, color = Muted, style = MaterialTheme.typography.bodySmall)
            Text("Open workspace →", color = Cyan, fontWeight = FontWeight.Bold)
        }
    }
}