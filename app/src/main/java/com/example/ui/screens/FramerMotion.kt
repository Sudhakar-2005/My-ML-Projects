package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun AnimatePresence(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400, easing = EaseOutCubic)) +
                slideInVertically(initialOffsetY = { 30 }, animationSpec = tween(400, easing = EaseOutCubic)) +
                scaleIn(initialScale = 0.98f, animationSpec = tween(400, easing = EaseOutCubic)),
        exit = fadeOut(animationSpec = tween(300, easing = EaseInCubic)) +
                slideOutVertically(targetOffsetY = { -30 }, animationSpec = tween(300, easing = EaseInCubic)) +
                scaleOut(targetScale = 0.98f, animationSpec = tween(300, easing = EaseInCubic)),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun <T> AnimatePresence(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            (fadeIn(animationSpec = tween(500, easing = EaseOutCubic)) +
                    slideInVertically(initialOffsetY = { 40 }, animationSpec = tween(500, easing = EaseOutCubic)) +
                    scaleIn(initialScale = 0.97f, animationSpec = tween(500, easing = EaseOutCubic)))
                .togetherWith(
                    fadeOut(animationSpec = tween(350, easing = EaseInCubic)) +
                            slideOutVertically(targetOffsetY = { -40 }, animationSpec = tween(350, easing = EaseInCubic)) +
                            scaleOut(targetScale = 0.97f, animationSpec = tween(350, easing = EaseInCubic))
                )
        },
        modifier = modifier,
        label = "AnimatePresence"
    ) { state ->
        content(state)
    }
}

@Composable
fun MotionDiv(
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    durationMillis: Int = 500,
    yOffset: Float = 30f,
    initialScale: Float = 0.96f,
    content: @Composable () -> Unit
) {
    FramerMotionEntrance(
        modifier = modifier,
        delayMillis = delayMillis,
        durationMillis = durationMillis,
        yOffset = yOffset,
        initialScale = initialScale,
        content = content
    )
}
