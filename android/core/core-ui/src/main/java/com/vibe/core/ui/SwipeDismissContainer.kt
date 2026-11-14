package com.vibe.core.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Interactive vertical swipe-to-dismiss gesture container for full-screen overlays.
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
    var containerHeightPx by remember { mutableIntStateOf(0) }

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

    val dismissDistance = if (containerHeightPx > 0) containerHeightPx.toFloat() else 1600f
    val dragProgress = (offsetY.value / (dismissDistance * 0.45f)).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .onSizeChanged { containerHeightPx = it.height }
            .offset { IntOffset(0, offsetY.value.roundToInt()) }
            .graphicsLayer {
                val scaleVal = (1f - dragProgress * 0.06f).coerceIn(0.94f, 1f)
                scaleX = scaleVal
                scaleY = scaleVal
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            }
            .clip(RoundedCornerShape((dragProgress * 28f).dp))
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                enabled = enabled,
                onDragStopped = { velocity ->
                    val threshold = (dismissDistance * 0.16f).coerceAtLeast(160f)
                    if (offsetY.value > threshold || velocity > 650f) {
                        coroutineScope.launch {
                            offsetY.animateTo(
                                targetValue = dismissDistance,
                                animationSpec = tween(
                                    durationMillis = 200,
                                    easing = FastOutLinearInEasing
                                )
                            )
                            onDismiss()
                        }
                    } else {
                        coroutineScope.launch {
                            offsetY.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
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
