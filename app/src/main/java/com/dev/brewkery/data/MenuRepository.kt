package com.dev.brewkery.data

import com.dev.brewkery.data.remote.BrewkeryApi
import com.dev.brewkery.domain.Menu
import com.dev.brewkery.domain.MenuItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class MenuRepository(private val api: BrewkeryApi) {

    private val _menu = MutableStateFlow<Menu?>(null)

    /** Last menu loaded from the network (also used for store fees by the cart). */
    val menu: StateFlow<Menu?> = _menu.asStateFlow()

    suspend fun refreshMenu(): Result<Menu> = safeCall {
        val menu = api.getMenu().toDomain()
        if (menu.items.isEmpty()) error("The menu is empty right now.")
        _menu.value = menu
        menu
    }

    fun cachedItem(id: Int): MenuItem? = _menu.value?.items?.firstOrNull { it.id == id }

    suspend fun fetchItem(id: Int): Result<MenuItem> = safeCall {
        api.getItem(id).toDomainOrNull() ?: error("Unexpected response for item $id")
    }
}

private suspend inline fun <T> safeCall(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

/** Plain-English error text for the UI. */
fun Throwable.friendlyMessage(): String = when (this) {
    is UnknownHostException -> "No internet connection. Check your network and try again."
    is SocketTimeoutException -> "The server took too long to answer. Please try again."
    is IOException -> "Couldn't reach the server. Please try again."
    else -> message ?: "Something went wrong. Please try again."
}
