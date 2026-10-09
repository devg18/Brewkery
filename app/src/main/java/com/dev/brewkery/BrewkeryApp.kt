package com.dev.brewkery

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dev.brewkery.di.ServiceLocator
import com.dev.brewkery.ui.cart.CartScreen
import com.dev.brewkery.ui.cart.CartViewModel
import com.dev.brewkery.ui.detail.DetailScreen
import com.dev.brewkery.ui.home.HomeScreen
import com.dev.brewkery.ui.order.OrderScreen

private object Routes {
    const val HOME = "home"
    const val DETAIL = "detail/{id}"
    const val CART = "cart"
    const val ORDER = "order"
    fun detail(id: Int) = "detail/$id"
}

@Composable
fun BrewkeryApp() {
    val nav = rememberNavController()
    // Activity-scoped, so the cart + active order survive navigation between screens.
    val cartVm: CartViewModel = viewModel(
        factory = viewModelFactory { initializer { CartViewModel(ServiceLocator.repository) } }
    )

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            enterTransition = { slideInHorizontally(tween(350)) { it / 4 } + fadeIn(tween(350)) },
            exitTransition = { slideOutHorizontally(tween(350)) { -it / 6 } + fadeOut(tween(250)) },
            popEnterTransition = { slideInHorizontally(tween(350)) { -it / 6 } + fadeIn(tween(350)) },
            popExitTransition = { slideOutHorizontally(tween(350)) { it / 4 } + fadeOut(tween(250)) }
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    cartVm = cartVm,
                    onItemClick = { nav.navigate(Routes.detail(it)) },
                    onCartClick = { nav.navigate(Routes.CART) },
                    onOrderClick = { nav.navigate(Routes.ORDER) }
                )
            }
            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { entry ->
                val id = entry.arguments?.getInt("id") ?: 0
                DetailScreen(itemId = id, cartVm = cartVm, onBack = { nav.popBackStack() })
            }
            composable(Routes.CART) {
                CartScreen(
                    cartVm = cartVm,
                    onBack = { nav.popBackStack() },
                    onBrowse = { nav.popBackStack() },
                    onPlaceOrder = {
                        if (cartVm.placeOrder() != null) {
                            nav.navigate(Routes.ORDER) { popUpTo(Routes.HOME) }
                        }
                    }
                )
            }
            composable(Routes.ORDER) {
                OrderScreen(
                    cartVm = cartVm,
                    onDone = { nav.popBackStack(Routes.HOME, inclusive = false) }
                )
            }
        }
    }
}
