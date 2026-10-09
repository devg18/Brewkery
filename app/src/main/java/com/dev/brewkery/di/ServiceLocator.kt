package com.dev.brewkery.di

import com.dev.brewkery.data.MenuRepository
import com.dev.brewkery.data.remote.BrewkeryApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Tiny manual DI - plenty for a 4 screen prototype. */
object ServiceLocator {

    private val api: BrewkeryApi by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl(BrewkeryApi.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BrewkeryApi::class.java)
    }

    val repository: MenuRepository by lazy { MenuRepository(api) }
}
