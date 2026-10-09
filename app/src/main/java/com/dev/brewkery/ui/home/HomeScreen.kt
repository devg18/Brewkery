package com.dev.brewkery.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dev.brewkery.di.ServiceLocator
import com.dev.brewkery.domain.Category
import com.dev.brewkery.domain.Menu
import com.dev.brewkery.domain.MenuItem
import com.dev.brewkery.domain.Order
import com.dev.brewkery.domain.StoreMeta
import com.dev.brewkery.ui.cart.CartViewModel
import com.dev.brewkery.ui.common.AnimatedPrice
import com.dev.brewkery.ui.common.BadgePill
import com.dev.brewkery.ui.common.BrewImage
import com.dev.brewkery.ui.common.ErrorState
import com.dev.brewkery.ui.common.ShimmerBox
import com.dev.brewkery.ui.common.StaggeredEntrance
import com.dev.brewkery.ui.common.asPrice
import com.dev.brewkery.ui.common.bounceClick
import com.dev.brewkery.ui.common.verticalSwap
import com.dev.brewkery.ui.theme.BrewGradient
import com.dev.brewkery.ui.theme.Caramel
import com.dev.brewkery.ui.theme.Espresso
import com.dev.brewkery.ui.theme.Mocha
import com.dev.brewkery.ui.theme.StarYellow

@Composable
fun HomeScreen(
    cartVm: CartViewModel,
    onItemClick: (Int) -> Unit,
    onCartClick: () -> Unit,
    onOrderClick: () -> Unit
) {
    val vm: HomeViewModel = viewModel(
        factory = viewModelFactory { initializer { HomeViewModel(ServiceLocator.repository) } }
    )
    val state by vm.state.collectAsStateWithLifecycle()
    val selected by vm.selectedCategory.collectAsStateWithLifecycle()
    val totals by cartVm.totals.collectAsStateWithLifecycle()
    val order by cartVm.order.collectAsStateWithLifecycle()

    val tagline = (state as? HomeUiState.Success)?.menu?.meta?.tagline
        ?: "Artisanal Coffee & Fresh Oven Bakes"

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            HomeHeader(tagline = tagline, cartCount = totals.itemCount, onCartClick = onCartClick)
            Box(Modifier.weight(1f)) {
                when (val s = state) {
                    HomeUiState.Loading -> HomeLoading()
                    is HomeUiState.Error -> ErrorState(
                        title = "Couldn't load the menu",
                        message = s.message,
                        onRetry = vm::load
                    )
                    is HomeUiState.Success -> HomeContent(
                        menu = s.menu,
                        selected = selected,
                        order = order,
                        onSelect = vm::selectCategory,
                        onItemClick = onItemClick,
                        onOrderClick = onOrderClick
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = totals.itemCount > 0,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(spring(Spring.DampingRatioMediumBouncy)) { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            CartBar(count = totals.itemCount, total = totals.total, onClick = onCartClick)
        }
    }
}

// ---------------------------------------------------------------- header

@Composable
private fun HomeHeader(tagline: String, cartCount: Int, onCartClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("Brewkery ☕", style = MaterialTheme.typography.headlineMedium)
            Text(
                tagline,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            Modifier
                .size(48.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .bounceClick(onCartClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart", tint = MaterialTheme.colorScheme.primary)
        }
        // Badge sits outside the clipped circle so it is not cut off.
        AnimatedVisibility(
            visible = cartCount > 0,
            enter = scaleIn(spring(Spring.DampingRatioMediumBouncy)),
            exit = fadeOut()
        ) {
            Box(
                Modifier
                    .padding(start = 4.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Caramel),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = cartCount,
                    transitionSpec = { verticalSwap(targetState > initialState) },
                    label = "badge"
                ) { n ->
                    Text(n.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ---------------------------------------------------------------- content

@Composable
private fun HomeContent(
    menu: Menu,
    selected: String?,
    order: Order?,
    onSelect: (String?) -> Unit,
    onItemClick: (Int) -> Unit,
    onOrderClick: () -> Unit
) {
    val items = remember(menu, selected) {
        if (selected == null) menu.items else menu.items.filter { it.categoryId == selected }
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "banner") { AnnouncementBanner(menu.meta) }
        if (order != null) {
            item(key = "order") { ActiveOrderCard(order, onOrderClick) }
        }
        item(key = "categories") { CategoryRow(menu.categories, selected, onSelect) }
        item(key = "title") {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Our menu", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                Text(
                    "${items.size} items",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
            StaggeredEntrance(index, Modifier.animateItem().padding(horizontal = 20.dp)) {
                MenuItemCard(item = item, onClick = { onItemClick(item.id) })
            }
        }
    }
}

@Composable
private fun AnnouncementBanner(meta: StoreMeta) {
    val transition = rememberInfiniteTransition(label = "banner")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 500f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Reverse),
        label = "shift"
    )
    val bob by transition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob"
    )
    Box(
        Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Espresso, Mocha, Caramel),
                    start = Offset(shift, 0f),
                    end = Offset(shift + 900f, 500f)
                )
            )
    ) {
        Text(
            "🥐",
            fontSize = 84.sp,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp)
                .graphicsLayer {
                    translationY = bob
                    rotationZ = -12f
                    alpha = 0.92f
                }
        )
        Column(Modifier.padding(22.dp).fillMaxWidth(0.72f)) {
            Text(
                "📣  STORE UPDATE",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Fresh from the oven, hot from the grinder",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Delivery in ${meta.estimatedDelivery} · ${meta.deliveryFee.asPrice()} flat fee",
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ActiveOrderCard(order: Order, onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "dot")
    val pulse by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "pulse"
    )
    Row(
        Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .bounceClick(onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(12.dp)
                .graphicsLayer { alpha = pulse }
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiary)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "Order ${order.ticketId}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                "${order.status.name} · arriving in ${order.estimatedDelivery}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
            )
        }
        Text(
            "Track  ›",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

// ---------------------------------------------------------------- categories

@Composable
private fun CategoryRow(categories: List<Category>, selected: String?, onSelect: (String?) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "all") { CategoryChip("🍽️", "All", selected == null) { onSelect(null) } }
        items(categories, key = { it.id }) { c ->
            CategoryChip(c.icon, c.name, selected == c.id) { onSelect(c.id) }
        }
    }
}

@Composable
private fun CategoryChip(icon: String, name: String, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val bg by animateColorAsState(if (selected) scheme.primary else scheme.surface, tween(300), label = "chipBg")
    val fg by animateColorAsState(if (selected) scheme.onPrimary else scheme.onSurface, tween(300), label = "chipFg")
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "chipScale"
    )
    Row(
        Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(if (selected) 6.dp else 1.dp, CircleShape)
            .clip(CircleShape)
            .background(bg)
            .bounceClick(onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 18.sp)
        Spacer(Modifier.width(8.dp))
        Text(name, color = fg, style = MaterialTheme.typography.labelLarge)
    }
}

// ---------------------------------------------------------------- list item

@Composable
private fun MenuItemCard(item: MenuItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .bounceClick(onClick)
            .padding(12.dp)
    ) {
        BrewImage(item.imageUrl, Modifier.size(100.dp).clip(RoundedCornerShape(18.dp)))
        Spacer(Modifier.width(14.dp))
        Column(
            Modifier.weight(1f).heightIn(min = 100.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                if (item.badge.isNotBlank()) {
                    BadgePill(item.badge)
                    Spacer(Modifier.height(6.dp))
                }
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = StarYellow, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(item.rating.toString(), style = MaterialTheme.typography.labelLarge)
                    Text(
                        "  (${item.reviewCount})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    item.basePrice.asPrice(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(BrewGradient)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "View", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ---------------------------------------------------------------- cart bar

@Composable
private fun CartBar(count: Int, total: Double, onClick: () -> Unit) {
    Row(
        Modifier
            .navigationBarsPadding()
            .padding(16.dp)
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.horizontalGradient(BrewGradient))
            .bounceClick(onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = count,
                transitionSpec = { verticalSwap(targetState > initialState) },
                label = "barCount"
            ) { n -> Text(n.toString(), color = Color.White, fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            if (count == 1) "1 item" else "$count items",
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.weight(1f)
        )
        Text("View cart", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(10.dp))
        AnimatedPrice(total, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

// ---------------------------------------------------------------- loading skeleton

@Composable
private fun HomeLoading() {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ShimmerBox(Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(28.dp)))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(3) { ShimmerBox(Modifier.size(width = 100.dp, height = 44.dp).clip(CircleShape)) }
        }
        Text(
            "Brewing your menu…",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        repeat(4) { ShimmerBox(Modifier.fillMaxWidth().height(124.dp).clip(RoundedCornerShape(24.dp))) }
    }
}
