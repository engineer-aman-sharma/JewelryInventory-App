package com.amansharma.jewelryinventory.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.amansharma.jewelryinventory.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val density = LocalDensity.current
    val titleAlpha = remember { Animatable(0f) }
    val titleOffset = remember { Animatable(with(density) { -36.dp.toPx() }) }
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.72f) }
    val taglineAlpha = remember { Animatable(0f) }
    val taglineOffset = remember { Animatable(with(density) { 36.dp.toPx() }) }

    LaunchedEffect(Unit) {
        launch {
            titleAlpha.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        }
        launch {
            titleOffset.animateTo(0f, tween(700, easing = FastOutSlowInEasing))
        }
        launch {
            delay(180)
            logoAlpha.animateTo(1f, tween(650, easing = FastOutSlowInEasing))
        }
        launch {
            delay(180)
            logoScale.animateTo(
                1f,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            delay(420)
            taglineAlpha.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        }
        launch {
            delay(420)
            taglineOffset.animateTo(0f, tween(700, easing = FastOutSlowInEasing))
        }
        delay(2400)
        onFinished()
    }

    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        colors.background,
                        colors.primaryContainer.copy(alpha = 0.45f),
                        colors.background
                    )
                )
            )
            .systemBarsPadding()
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmall,
            color = colors.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp, start = 24.dp, end = 24.dp)
                .graphicsLayer {
                    alpha = titleAlpha.value
                    translationY = titleOffset.value
                }
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(220.dp)
                    .graphicsLayer {
                        alpha = logoAlpha.value
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                    }
                    .clip(CircleShape)
                    .background(colors.surface.copy(alpha = 0.92f))
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_app_logo),
                    contentDescription = null,
                    modifier = Modifier.size(176.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 48.dp, start = 24.dp, end = 24.dp)
                .graphicsLayer {
                    alpha = taglineAlpha.value
                    translationY = taglineOffset.value
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .size(width = 48.dp, height = 2.dp)
                    .background(colors.primary.copy(alpha = 0.7f))
            )
            Text(
                text = "Built by Aman Sharma",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onBackground.copy(alpha = 0.78f),
                textAlign = TextAlign.Center
            )
        }
    }
}
