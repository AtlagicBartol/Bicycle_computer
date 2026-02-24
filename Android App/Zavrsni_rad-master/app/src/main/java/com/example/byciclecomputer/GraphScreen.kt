package com.example.byciclecomputer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GraphScreen(week : Week){
    WeekStats(week)
}

@Composable
fun WeekStats(week: Week) {
    var selectedChart by remember { mutableStateOf("Distanca") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF273043))
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Distanca", "Kalorije", "Brzina", "Vrijeme").forEach { label ->
                Button(
                    onClick = { selectedChart = label },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedChart == label) Color(0xFF0E79B2) else Color.DarkGray
                    )
                ) {
                    Text(label, color = Color.White, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedChart) {
            "Distanca" -> {
                DistanceChart(week)
            }
            "Kalorije" -> {
                CaloriesChart(week)
            }
            "Brzina" -> {
                VelocityChart(week)
            }
            "Vrijeme" -> {
                TimeChart(week)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}
