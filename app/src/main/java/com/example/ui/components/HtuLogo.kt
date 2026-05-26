package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun HtuLogo(
    modifier: Modifier = Modifier,
    showSubtext: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Elegant Minimalist Rounded Square Logo (from HTML spec: w-10 h-10 bg-[#005FB0] rounded-xl)
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(HTUNavy, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "HTU",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
        }

        if (showSubtext) {
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Exam Library",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.3).sp,
                    lineHeight = 18.sp
                )
                Text(
                    text = "Ho Technical University",
                    color = HTUTextGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

