package com.dev.brewkery.ui.cart

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.brewkery.domain.CartLine
import com.dev.brewkery.domain.CartTotals
import com.dev.brewkery.domain.StoreMeta
import com.dev.brewkery.ui.common.AnimatedPrice
import com.dev.brewkery.ui.common.BrewImage
import com.dev.brewkery.ui.common.CircleIconButton
import com.dev.brewkery.ui.common.GradientButton
import com.dev.brewkery.ui.common.QtyStepper
import com.dev.brewkery.ui.common.asPrice
import com.dev.brewkery.ui.common.bounceClick

@Composable
fun CartScreen(
    cartVm: CartViewModel,
    onBack: () -> Unit,
    onBrowse: () -> Unit,
    onPlaceOrder: () -> Unit
) {
    val lines by cartVm.lines.collectAsStateWithLifecycle()
    val totals by cartVm.totals.collectAsStateWithLifecycle()
    val meta by cartVm.meta.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleIconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.width(14.dp))
            Text("Your cart", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            if (lines.isNotEmpty()) {
                Text(
                    if (totals.itemCount == 1) "1 item" else "${totals.itemCount} items",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (lines.isEmpty()) {
            EmptyCart(onBrowse)
        } else {
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(lines, key = { it.key }) { line ->
                    CartLineCard(
                        line = line,
                        onMinus = { cartVm.decrement(line.key) },
                        onPlus = { cartVm.increment(line.key) },
                        onRemove = { cartVm.remove(line.key) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
            SummaryPanel(totals = totals, meta = meta, onPlaceOrder = onPlaceOrder)
        }
    }
}

@Composable
private fun EmptyCart(onBrowse: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "empty")
    val bob by transition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob"
    )
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🛍️", fontSize = 72.sp, modifier = Modifier.graphicsLayer { translationY = bob })
        Spacer(Modifier.height(16.dp))
        Text("Your cart is empty", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "Add a coffee or a fresh bake and it will show up here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        GradientButton(onClick = onBrowse) { Text("Browse menu", fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun CartLineCard(
    line: CartLine,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BrewImage(line.item.imageUrl, Modifier.size(78.dp).clip(RoundedCornerShape(16.dp)))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                line.item.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            val options = listOfNotNull(line.size?.label, line.milk?.name, line.sugar?.label).joinToString(" · ")
            if (options.isNotBlank()) {
                Text(
                    options,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                QtyStepper(line.quantity, onMinus = onMinus, onPlus = onPlus, compact = true)
                Spacer(Modifier.weight(1f))
                AnimatedPrice(
                    line.lineTotal,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Icon(
            Icons.Filled.Delete,
            contentDescription = "Remove",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(start = 8.dp)
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .bounceClick(onRemove)
                .padding(8.dp)
        )
    }
}

@Composable
private fun SummaryPanel(totals: CartTotals, meta: StoreMeta, onPlaceOrder: () -> Unit) {
    val taxLabel = meta.taxRatePercent.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(Modifier.navigationBarsPadding().padding(20.dp)) {
            PriceRow("Subtotal", totals.subtotal)
            Spacer(Modifier.height(8.dp))
            PriceRow("Delivery fee", totals.deliveryFee)
            Spacer(Modifier.height(8.dp))
            PriceRow("Tax ($taxLabel%)", totals.tax)
            Spacer(Modifier.height(12.dp))
            Spacer(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline))
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                AnimatedPrice(
                    totals.total,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "🛵  Estimated delivery ${meta.estimatedDelivery}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            GradientButton(onClick = onPlaceOrder, modifier = Modifier.fillMaxWidth()) {
                Text("Place order", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(10.dp))
                Text("·", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(10.dp))
                Text(totals.total.asPrice(), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PriceRow(label: String, value: Double) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        AnimatedPrice(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
