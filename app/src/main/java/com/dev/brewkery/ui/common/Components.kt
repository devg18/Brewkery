package com.dev.brewkery.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.dev.brewkery.ui.theme.BrewGradient
import kotlinx.coroutines.delay
import java.util.Locale

fun Double.asPrice(): String = "$" + String.format(Locale.US, "%.2f", this)

// ---------- Motion helpers ----------

/** Number roll animation: new value slides in from the top when it grows, bottom when it shrinks. */
fun verticalSwap(up: Boolean): ContentTransform =
    if (up) {
        (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
    } else {
        (slideInVertically { -it } + fadeIn()) togetherWith (slideOutVertically { it } + fadeOut())
    }

/** Springy "press" feedback + click. */
fun Modifier.bounceClick(onClick: () -> Unit): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "press"
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(interactionSource = interaction, indication = null, onClick = onClick)
}

/** Fades + slides a list item in. Keeps its layout size so scrolling never jumps. */
@Composable
fun StaggeredEntrance(index: Int, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(minOf(index, 6) * 70L)
        progress.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
    }
    Box(
        modifier.graphicsLayer {
            alpha = progress.value
            translationY = (1f - progress.value) * 40.dp.toPx()
        }
    ) { content() }
}

// ---------- Loading ----------

@Composable
fun shimmerBrush(): Brush {
    val base = MaterialTheme.colorScheme.surfaceVariant
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shift by transition.animateFloat(
        initialValue = -400f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing)),
        label = "shift"
    )
    return Brush.linearGradient(
        colors = listOf(base, base.copy(alpha = 0.35f), base),
        start = Offset(shift, 0f),
        end = Offset(shift + 400f, 200f)
    )
}

@Composable
fun ShimmerBox(modifier: Modifier = Modifier) {
    Box(modifier.background(shimmerBrush()))
}

// ---------- Error ----------

@Composable
fun ErrorState(title: String, message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "err")
    val wobble by transition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "wobble"
    )
    Column(
        modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("☕", fontSize = 64.sp, modifier = Modifier.graphicsLayer { rotationZ = wobble })
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        GradientButton(onClick = onRetry) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("Try again", fontWeight = FontWeight.Bold)
        }
    }
}

// ---------- Buttons & chips ----------

@Composable
fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(BrewGradient))
            .bounceClick(onClick)
            .padding(horizontal = 22.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            content()
        }
    }
}

@Composable
fun CircleIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier
            .size(44.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .bounceClick(onClick),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun BadgePill(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(CircleShape)
            .background(Brush.horizontalGradient(BrewGradient))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text.uppercase(),
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
private fun StepperButton(label: String, size: androidx.compose.ui.unit.Dp, onClick: () -> Unit) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .bounceClick(onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun QtyStepper(
    quantity: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val buttonSize = if (compact) 30.dp else 38.dp
    Row(
        modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepperButton("−", buttonSize, onMinus)
        AnimatedContent(
            targetState = quantity,
            transitionSpec = { verticalSwap(targetState > initialState) },
            label = "qty"
        ) { q ->
            Text(
                q.toString(),
                modifier = Modifier.widthIn(min = 30.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
        StepperButton("+", buttonSize, onPlus)
    }
}

@Composable
fun AnimatedPrice(
    value: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null
) {
    AnimatedContent(
        targetState = value,
        modifier = modifier,
        transitionSpec = { verticalSwap(targetState > initialState) },
        label = "price"
    ) { v ->
        Text(v.asPrice(), style = style, color = color, fontWeight = fontWeight)
    }
}

// ---------- Images ----------

@Composable
fun BrewImage(url: String, modifier: Modifier = Modifier, contentScale: ContentScale = ContentScale.Crop) {
    val context = LocalContext.current
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context).data(url).crossfade(true).build(),
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale,
        loading = { ShimmerBox(Modifier.fillMaxSize()) },
        error = {
            Box(
                Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) { Text("☕", fontSize = 28.sp) }
        }
    )
}
