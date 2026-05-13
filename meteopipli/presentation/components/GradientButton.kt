package com.example.meteopipli.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GradientButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    gradient: Brush = Brush.horizontalGradient(
        colors = listOf(Color(0xFF6C5CE7), Color(0xFFA389F4))
    )
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(50.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50.dp))
                .background(gradient)
                .padding(vertical = 12.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(text, fontSize = 18.sp, color = Color.White)
        }
    }
}