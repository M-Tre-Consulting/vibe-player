package com.vibe.core.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Interactive vertical swipe-to-dismiss gesture container that smoothly morphs
 * and minimizes full screen player down into the floating mini-player capsule anchor
 * with bouncy elastic spring physics.
 */
@Composable
fun SwipeDismissContainer(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        offsetY.snapTo(0f)
    }

    val draggableState = rememberDraggableState { delta ->
        if (enabled) {
            coroutineScope.launch {
                val newOffset = (offsetY.value + delta).coerceAtLeast(0f)
                offsetY.snapTo(newOffset)
            }
        }
    }

    Box(
        modifier = modifier
            .offset { IntOffset(0, offsetY.value.roundToInt()) }
            .graphicsLayer {
                val progress = (offsetY.value / 900f).coerceIn(0f, 1f)
                alpha = (1f - progress * 0.9f).coerceIn(0f, 1f)
                val scale = (1f - progress * 0.22f).coerceIn(0.78f, 1f)
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0.5f, 0.95f)
                shadowElevation = (16.dp * progress).toPx()
            }
            .clip(RoundedCornerShape(28.dp * (offsetY.value / 500f).coerceIn(0f, 1f)))
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                enabled = enabled,
                onDragStopped = { velocity ->
                    if (offsetY.value > 160f || velocity > 450f) {
                        coroutineScope.launch {
                            offsetY.animateTo(
                                targetValue = 1000f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                            onDismiss()
                        }
                    } else {
                        coroutineScope.launch {
                            offsetY.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        }
                    }
                }
            )
    ) {
        content()
    }
}
