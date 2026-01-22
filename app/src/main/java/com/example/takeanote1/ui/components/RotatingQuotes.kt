package com.example.takeanote1.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RotatingQuotesWithIcons(
    quotesWithIcons: List<Pair<String, Int>>,
    rotationTime: Long = 2000L // 2 seconds
) {
    var currentIndex by remember { mutableStateOf(0) }

    // Rotate quotes automatically
    LaunchedEffect(Unit) {
        while (true) {
            delay(rotationTime)
            currentIndex = (currentIndex + 1) % quotesWithIcons.size
        }
    }

    val (quote, iconRes) = quotesWithIcons[currentIndex]

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        // Image at top inside a Box
        Box(
            modifier = Modifier
                .size(100.dp)
                .padding(bottom = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = "Icon",
                modifier = Modifier.fillMaxSize()
            )
        }

        // Quote text below image, bold
        AnimatedContent(
            targetState = quote,
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) with
                        fadeOut(animationSpec = tween(500))
            }
        ) { targetQuote ->
            Text(
                text = targetQuote,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Dots for pagination
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            quotesWithIcons.forEachIndexed { index, _ ->
                val isSelected = index == currentIndex
                val dotColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .clickable {
                            currentIndex = index
                        }
                        .padding(4.dp)
                )
                if (index != quotesWithIcons.lastIndex) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}