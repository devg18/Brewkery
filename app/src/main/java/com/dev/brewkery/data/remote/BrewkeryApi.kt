package com.dev.brewkery.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface BrewkeryApi {

    @GET("data.json")
    suspend fun getMenu(): MenuResponseDto

    @GET("api/items/{id}.json")
    suspend fun getItem(@Path("id") id: Int): ItemDto

    companion object {
        const val BASE_URL = "https://raw.githubusercontent.com/VivekShah138/Brewkery/main/"
    }
}
