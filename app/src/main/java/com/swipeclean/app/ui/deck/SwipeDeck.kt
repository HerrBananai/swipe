package com.swipeclean.app.ui.deck

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
import androidx.compose.animation.core.Spring.StiffnessMedium
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlinx.coroutines.launch

enum class SwipeDirection { LEFT, RIGHT }

/**
 * Tinder-style swipeable card deck.
 *
 * - The top card follows the finger (horizontal drag with a bit of vertical give and rotation).
 * - Drag past [thresholdFraction] of the deck width and the card flies off-screen,
 *   then [onSwiped] fires so the caller can update its data source.
 * - Below threshold it springs back to center.
 * - One card is stacked behind the top one for depth.
 *
 * @param items the deck contents, top card first.
 * @param onSwiped called after the top card has animated out; remove that item from [items].
 * @param leftStamp overlay shown while dragging left (alpha follows drag progress).
 * @param rightStamp overlay shown while dragging right.
 * @param cardContent renders a single item inside the card.
 */
@Composable
fun <T : Any> SwipeDeck(
    items: List<T>,
    onSwiped: (SwipeDirection) -> Unit,
    modifier: Modifier = Modifier,
    keyOf: (T) -> Any = { it },
    cardShape: Dp = 24.dp,
    thresholdFraction: Float = 0.3f,
    leftStamp: @Composable () -> Unit = {},
    rightStamp: @Composable () -> Unit = {},
    cardContent: @Composable (T) -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        if (items.isEmpty()) return@BoxWithConstraints

        val cardWidthPx = with(androidx.compose.ui.platform.LocalDensity.current) {
            maxWidth.toPx().takeIf { it.isFinite() } ?: 1f
        }
        val shape = RoundedCornerShape(cardShape)

        // Second card in the stack (peeking behind).
        if (items.size > 1) {
            key(keyOf(items[1])) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationY = 14.dp.toPx()
                            scaleX = 0.95f
                            scaleY = 0.95f
                        }
                        .shadow(8.dp, shape, clip = false)
                        .clip(shape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    cardContent(items[1])
                }
            }
        }

        // Top card.
        key(keyOf(items[0])) {
            SwipeableTopCard(
                item = items[0],
                cardWidthPx = cardWidthPx,
                cardShapeDp = cardShape,
                thresholdFraction = thresholdFraction,
                onSwiped = onSwiped,
                leftStamp = leftStamp,
                rightStamp = rightStamp,
                cardContent = cardContent,
            )
        }
    }
}

@Composable
private fun <T : Any> SwipeableTopCard(
    item: T,
    cardWidthPx: Float,
    cardShapeDp: Dp,
    thresholdFraction: Float,
    onSwiped: (SwipeDirection) -> Unit,
    leftStamp: @Composable () -> Unit,
    rightStamp: @Composable () -> Unit,
    cardContent: @Composable (T) -> Unit,
) {
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val shape = RoundedCornerShape(cardShapeDp)
    val stampAlphaDivisor = (cardWidthPx * 0.35f).coerceAtLeast(1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(item) {
                detectDragGestures(
                    onDrag = { change, dragAmount: Offset ->
                        change.consume()
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y * 0.35f)
                        }
                    },
                    onDragEnd = {
                        scope.launch {
                            if (abs(offsetX.value) > cardWidthPx * thresholdFraction) {
                                val direction =
                                    if (offsetX.value > 0f) SwipeDirection.RIGHT else SwipeDirection.LEFT
                                val targetX = (if (direction == SwipeDirection.RIGHT) 1.5f else -1.5f) * cardWidthPx
                                offsetY.animateTo(offsetY.value * 2f, tween(220, easing = LinearOutSlowInEasing))
                                offsetX.animateTo(targetX, tween(220, easing = LinearOutSlowInEasing))
                                onSwiped(direction)
                            } else {
                                launch {
                                    offsetX.animateTo(
                                        0f,
                                        spring(dampingRatio = DampingRatioMediumBouncy, stiffness = StiffnessMedium),
                                    )
                                }
                                offsetY.animateTo(
                                    0f,
                                    spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = StiffnessMedium),
                                )
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            launch { offsetX.animateTo(0f, spring()) }
                            offsetY.animateTo(0f, spring())
                        }
                    },
                )
            }
            .graphicsLayer {
                translationX = offsetX.value
                translationY = offsetY.value
                rotationZ = (offsetX.value / cardWidthPx) * 12f
            }
            .shadow(12.dp, shape, clip = false)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        cardContent(item)

        // Drag stamps: DELETE on left drag, KEEP on right drag.
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp)
                .graphicsLayer {
                    alpha = (-offsetX.value / stampAlphaDivisor).coerceIn(0f, 1f)
                    rotationZ = -12f
                },
        ) {
            leftStamp()
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .graphicsLayer {
                    alpha = (offsetX.value / stampAlphaDivisor).coerceIn(0f, 1f)
                    rotationZ = 12f
                },
        ) {
            rightStamp()
        }
    }
}
