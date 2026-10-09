package com.dev.brewkery.ui.order

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.brewkery.domain.Order
import com.dev.brewkery.domain.OrderStatus
import com.dev.brewkery.ui.cart.CartViewModel
import com.dev.brewkery.ui.common.GradientButton
import com.dev.brewkery.ui.common.asPrice
import com.dev.brewkery.ui.theme.BrewGradient
import com.dev.brewkery.ui.theme.Caramel

@Composable
fun OrderScreen(cartVm: CartViewModel, onDone: () -> Unit) {
    val order by cartVm.order.collectAsStateWithLifecycle()
    BackHandler(onBack = onDone)

    val current = order
    if (current == null) {
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No active order", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            GradientButton(onClick = onDone) { Text("Back to menu", fontWeight = FontWeight.Bold) }
        }
    } else {
        OrderContent(current, onDone)
    }
}

@Composable
private fun OrderContent(order: Order, onDone: () -> Unit) {
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { show = true }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PulsingBadge()
        Spacer(Modifier.height(8.dp))
        Text("Order placed!", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            "Our baristas are on it.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))

        AnimatedVisibility(
            visible = show,
            enter = scaleIn(spring(Spring.DampingRatioMediumBouncy), initialScale = 0.6f) + fadeIn()
        ) {
            TicketCard(order)
        }

        Spacer(Modifier.height(24.dp))
        AnimatedVisibility(
            visible = show,
            enter = slideInVertically(tween(600, delayMillis = 250)) { it / 3 } + fadeIn(tween(600, delayMillis = 250))
        ) {
            Column {
                Timeline(order.status)
                Spacer(Modifier.height(24.dp))
                OrderSummary(order)
            }
        }

        Spacer(Modifier.height(24.dp))
        GradientButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text("Back to menu", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PulsingBadge() {
    val transition = rememberInfiniteTransition(label = "ripple")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearOutSlowInEasing)),
        label = "t"
    )
    val ringColor = Caramel
    Box(Modifier.size(180.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            for (k in 0..1) {
                val p = (t + k * 0.5f) % 1f
                drawCircle(
                    color = ringColor.copy(alpha = (1f - p) * 0.35f),
                    radius = size.minDimension / 2f * (0.5f + 0.5f * p)
                )
            }
        }
        Box(
            Modifier.size(96.dp).clip(CircleShape).background(Brush.linearGradient(BrewGradient)),
            contentAlignment = Alignment.Center
        ) {
            Text("☕", fontSize = 44.sp)
        }
    }
}

@Composable
private fun TicketCard(order: Order) {
    val outline = MaterialTheme.colorScheme.outline
    val transition = rememberInfiniteTransition(label = "chip")
    val pulse by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "pulse"
    )
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .drawBehind {
                drawRoundRect(
                    color = outline,
                    cornerRadius = CornerRadius(24.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f)))
                )
            }
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "TICKET ID",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            order.ticketId,
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Row(
            Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(10.dp)
                    .graphicsLayer { alpha = pulse }
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                order.status.name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                letterSpacing = 1.sp
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Estimated delivery · ${order.estimatedDelivery}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Timeline(status: OrderStatus) {
    val steps = listOf("Order received", "Preparing", "On the way", "Delivered")
    val activeIndex = when (status) {
        OrderStatus.PREPARING -> 1
    }
    Column(Modifier.fillMaxWidth()) {
        steps.forEachIndexed { index, title ->
            TimelineStep(
                title = title,
                done = index < activeIndex,
                active = index == activeIndex,
                isLast = index == steps.lastIndex
            )
        }
    }
}

@Composable
private fun TimelineStep(title: String, done: Boolean, active: Boolean, isLast: Boolean) {
    val transition = rememberInfiniteTransition(label = "step")
    val ring by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "ring"
    )
    val scheme = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().height(52.dp)) {
        Column(Modifier.width(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                if (active) {
                    Box(
                        Modifier
                            .size(18.dp)
                            .graphicsLayer {
                                scaleX = ring
                                scaleY = ring
                                alpha = 2f - ring
                            }
                            .clip(CircleShape)
                            .background(Caramel.copy(alpha = 0.5f))
                    )
                }
                Box(
                    Modifier
                        .size(if (done || active) 20.dp else 14.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                done -> scheme.tertiary
                                active -> Caramel
                                else -> scheme.outline
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (done) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }
            if (!isLast) {
                Box(
                    Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(if (done) scheme.tertiary else scheme.outline)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
            color = if (done || active) scheme.onSurface else scheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun OrderSummary(order: Order) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(18.dp)
    ) {
        Text("Your order", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))
        order.lines.forEach { line ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(
                    "${line.quantity} × ${line.item.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(line.lineTotal.asPrice(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(Modifier.height(10.dp))
        Spacer(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline))
        Spacer(Modifier.height(10.dp))
        SummaryLine("Delivery fee", order.totals.deliveryFee.asPrice())
        SummaryLine("Tax", order.totals.tax.asPrice())
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth()) {
            Text("Total paid", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text(
                order.totals.total.asPrice(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
