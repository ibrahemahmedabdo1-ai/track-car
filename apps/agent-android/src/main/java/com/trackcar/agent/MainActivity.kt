package com.trackcar.agent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AgentReadyScreen() }
    }
}

@Composable
private fun AgentReadyScreen() {
    MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF38C7D8), background = Color(0xFF0B1424))) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0B1424)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("TRACK CAR", color = Color(0xFF38C7D8), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Vehicle Agent", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                Text("Not enrolled", color = Color(0xFFFFC46B), fontWeight = FontWeight.SemiBold)
                Text(
                    "This is the Android Agent foundation. Location tracking, background sync and remote support remain disabled until secure enrollment, user disclosure and backend authorization are implemented.",
                    color = Color(0xFF9BAEC5),
                    fontSize = 14.sp
                )
            }
        }
    }
}
