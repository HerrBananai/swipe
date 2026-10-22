package com.swipeclean.app.ui.deck

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Big rotated label shown over a card while dragging, Tinder-style. */
@Composable
fun SwipeStamp(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = color,
        fontSize = 30.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 2.sp,
        modifier = modifier
            .border(BorderStroke(4.dp, color), RectangleShape)
            .padding(horizontal = 18.dp, vertical = 6.dp),
    )
}

/** Default stamps used by the swipe screens. */
@Composable
fun DeleteStamp(modifier: Modifier = Modifier) =
    SwipeStamp("DELETE", MaterialTheme.colorScheme.error, modifier)

@Composable
fun KeepStamp(modifier: Modifier = Modifier) =
    SwipeStamp("KEEP", MaterialTheme.colorScheme.primary, modifier)
