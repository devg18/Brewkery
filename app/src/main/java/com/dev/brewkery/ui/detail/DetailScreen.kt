package com.dev.brewkery.ui.detail

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dev.brewkery.di.ServiceLocator
import com.dev.brewkery.domain.MenuItem
import com.dev.brewkery.ui.cart.CartViewModel
import com.dev.brewkery.ui.common.AnimatedPrice
import com.dev.brewkery.ui.common.BadgePill
import com.dev.brewkery.ui.common.BrewImage
import com.dev.brewkery.ui.common.CircleIconButton
import com.dev.brewkery.ui.common.ErrorState
import com.dev.brewkery.ui.common.GradientButton
import com.dev.brewkery.ui.common.QtyStepper
import com.dev.brewkery.ui.common.ShimmerBox
import com.dev.brewkery.ui.common.asPrice
import com.dev.brewkery.ui.common.bounceClick
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DetailScreen(itemId: Int, cartVm: CartViewModel, onBack: () -> Unit) {
    val vm: DetailViewModel = viewModel(
        key = "detail_$itemId",
        factory = viewModelFactory { initializer { DetailViewModel(ServiceLocator.repository, itemId) } }
    )
    val state by vm.state.collectAsStateWithLifecycle()
    val item = state.item

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when {
            item != null -> DetailContent(
                item = item,
                state = state,
                vm = vm,
                onAdd = {
                    cartVm.add(item, state.size, state.sugar, state.milk, state.quantity)
                },
                onBack = onBack
            )
            state.error != null -> {
                ErrorState(
                    title = "Couldn't load this item",
                    message = state.error.orEmpty(),
                    onRetry = vm::load,
                    modifier = Modifier.statusBarsPadding()
                )
                BackButton(onBack)
            }
            else -> {
                DetailLoading()
                BackButton(onBack)
            }
        }
    }
}

@Composable
private fun BackButton(onBack: () -> Unit) {
    CircleIconButton(onClick = onBack, modifier = Modifier.statusBarsPadding().padding(16.dp)) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun DetailLoading() {
    Column(Modifier.fillMaxSize()) {
        ShimmerBox(Modifier.fillMaxWidth().height(340.dp))
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            ShimmerBox(Modifier.width(110.dp).height(22.dp).clip(CircleShape))
            ShimmerBox(Modifier.fillMaxWidth(0.8f).height(30.dp).clip(RoundedCornerShape(8.dp)))
            ShimmerBox(Modifier.fillMaxWidth().height(70.dp).clip(RoundedCornerShape(16.dp)))
            ShimmerBox(Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(16.dp)))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(
    item: MenuItem,
    state: DetailUiState,
    vm: DetailViewModel,
    onAdd: () -> Unit,
    onBack: () -> Unit
) {
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    var added by remember { mutableStateOf(false) }

    val thresholdPx = with(LocalDensity.current) { 260.dp.toPx() }
    val showBar by remember { derivedStateOf { scroll.value > thresholdPx } }
    val barAlpha by animateFloatAsState(if (showBar) 1f else 0f, tween(200), label = "barAlpha")

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
            // ---- hero with parallax
            Box(Modifier.fillMaxWidth().height(340.dp).clipToBounds()) {
                BrewImage(
                    item.imageUrl,
                    Modifier.fillMaxSize().graphicsLayer { translationY = scroll.value * 0.5f }
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.55f to Color.Transparent,
                                1f to Color.Black.copy(alpha = 0.35f)
                            )
                        )
                )
            }

            // ---- sheet that overlaps the hero
            Column(
                Modifier
                    .offset(y = (-32).dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 150.dp)
            ) {
                if (item.badge.isNotBlank()) {
                    BadgePill(item.badge)
                    Spacer(Modifier.height(10.dp))
                }
                Text(item.name, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(6.dp))
                Text(
                    item.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatChip("⭐", "${item.rating} (${item.reviewCount})")
                    if (item.prepTime.isNotBlank()) StatChip("⏱️", item.prepTime)
                    StatChip("🔥", "${item.calories} kcal")
                }

                SectionTitle("About")
                Text(
                    item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (item.ingredients.isNotEmpty()) {
                    SectionTitle("Ingredients")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item.ingredients.forEach { ing ->
                            Text(
                                ing,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                if (item.sizes.isNotEmpty()) {
                    SectionTitle("Size")
                    OptionChips(
                        options = item.sizes,
                        selected = state.size,
                        label = { it.label },
                        extra = { it.extraPrice },
                        onSelect = vm::selectSize
                    )
                }
                if (item.milkOptions.isNotEmpty()) {
                    SectionTitle(milkTitle(item.categoryId))
                    OptionChips(
                        options = item.milkOptions,
                        selected = state.milk,
                        label = { it.name },
                        extra = { it.extraPrice },
                        onSelect = vm::selectMilk
                    )
                }
                if (item.sugarLevels.isNotEmpty()) {
                    SectionTitle(sugarTitle(item.categoryId))
                    OptionChips(
                        options = item.sugarLevels,
                        selected = state.sugar,
                        label = { it.label },
                        extra = { it.extraPrice },
                        onSelect = vm::selectSugar
                    )
                }
            }
        }

        // ---- floating top bar (turns solid after scrolling)
        Box(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background.copy(alpha = barAlpha))
                .statusBarsPadding()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircleIconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.alpha(barAlpha)
                )
            }
        }

        // ---- bottom bar: quantity + add to cart with live total
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 16.dp,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Row(
                Modifier.navigationBarsPadding().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QtyStepper(state.quantity, onMinus = vm::decrement, onPlus = vm::increment)
                Spacer(Modifier.width(14.dp))
                GradientButton(
                    onClick = {
                        if (!added) {
                            added = true
                            onAdd()
                            scope.launch {
                                delay(800)
                                onBack()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Crossfade(targetState = added, label = "addState") { done ->
                        if (done) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Check, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Added to cart", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Add to cart", fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(10.dp))
                                Text("·", fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(10.dp))
                                AnimatedPrice(state.total, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- small pieces

private fun milkTitle(categoryId: String) = when (categoryId) {
    "cat_bakery" -> "Spread & toppings"
    "cat_cold_brews" -> "Foam & milk"
    else -> "Milk"
}

private fun sugarTitle(categoryId: String) = if (categoryId == "cat_bakery") "Serving" else "Sweetness"

@Composable
private fun SectionTitle(text: String) {
    Spacer(Modifier.height(22.dp))
    Text(text, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun StatChip(icon: String, text: String) {
    Row(
        Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon)
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> OptionChips(
    options: List<T>,
    selected: T?,
    label: (T) -> String,
    extra: (T) -> Double,
    onSelect: (T) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            ChoiceChip(
                label = label(option),
                extra = extra(option),
                selected = option == selected,
                onClick = { onSelect(option) }
            )
        }
    }
}

@Composable
private fun ChoiceChip(label: String, extra: Double, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val bg by animateColorAsState(if (selected) scheme.primary else scheme.surface, tween(250), label = "bg")
    val fg by animateColorAsState(if (selected) scheme.onPrimary else scheme.onSurface, tween(250), label = "fg")
    val border by animateColorAsState(if (selected) scheme.primary else scheme.outline, tween(250), label = "border")
    Row(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .bounceClick(onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = fg, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        if (extra > 0) {
            Spacer(Modifier.width(6.dp))
            Text("+" + extra.asPrice(), color = fg.copy(alpha = 0.75f), style = MaterialTheme.typography.labelMedium)
        }
    }
}
