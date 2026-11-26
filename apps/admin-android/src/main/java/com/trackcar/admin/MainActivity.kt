package com.trackcar.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Navy = Color(0xFF101B31)
private val Panel = Color(0xFF1B2942)
private val Cyan = Color(0xFF36C5D8)
private val Muted = Color(0xFF9AAAC2)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TrackCarAdminApp() }
    }
}

@Composable
private fun TrackCarAdminApp() {
    MaterialTheme(colorScheme = darkColorScheme(
        primary = Cyan,
        background = Navy,
        surface = Panel,
        onBackground = Color.White,
        onSurface = Color.White
    )) {
        Surface(modifier = Modifier.fillMaxSize(), color = Navy) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(Modifier.height(22.dp))
                    Text("TRACK CAR", color = Cyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Fleet overview", fontSize = 29.sp, fontWeight = FontWeight.Bold)
                    Text("Admin control center", color = Muted, fontSize = 14.sp)
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricCard("Vehicles", "0", "Registered", Modifier.weight(1f))
                        MetricCard("Online", "0", "Connected now", Modifier.weight(1f))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricCard("Alerts", "0", "Needs review", Modifier.weight(1f))
                        MetricCard("Trips today", "0", "Recorded", Modifier.weight(1f))
                    }
                }
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Panel),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            Text("Live fleet map", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Map and live vehicle positions will appear after the secure fleet API is connected.",
                                color = Muted,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                            Spacer(Modifier.height(18.dp))
                            BoxPlaceholder()
                        }
                    }
                }
                item {
                    Text("Management", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    ManagementRow("Vehicles & devices", "Enroll vehicles and manage device status")
                    ManagementRow("Trips & history", "Review routes and trip records")
                    ManagementRow("Users & permissions", "Manage admins and fleet operators")
                    ManagementRow("Alerts & audit log", "Review incidents and administrative actions")
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, hint: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Muted, fontSize = 13.sp)
            Text(value, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text(hint, color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun BoxPlaceholder() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxWidth().height(150.dp)
            .background(Color(0xFF14243B), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("MAP NOT CONNECTED", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ManagementRow(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = Muted, fontSize = 12.sp)
            }
            Text("›", color = Cyan, fontSize = 26.sp)
        }
    }
}
