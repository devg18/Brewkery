package com.dev.brewkery.data.remote

import com.google.gson.annotations.SerializedName

// Every field is nullable on purpose: a bad/missing field must never crash the app.

data class MenuResponseDto(
    val meta: MetaDto?,
    val categories: List<CategoryDto>?,
    val items: List<ItemDto>?
)

data class MetaDto(
    val app: String?,
    val tagline: String?,
    @SerializedName("delivery_fee") val deliveryFee: Double?,
    @SerializedName("tax_rate_percent") val taxRatePercent: Double?,
    @SerializedName("estimated_delivery_time") val estimatedDeliveryTime: String?
)

data class CategoryDto(val id: String?, val name: String?, val icon: String?)

data class ItemDto(
    val id: Int?,
    @SerializedName("category_id") val categoryId: String?,
    val name: String?,
    val tagline: String?,
    val description: String?,
    @SerializedName("base_price") val basePrice: Double?,
    val rating: Double?,
    @SerializedName("review_count") val reviewCount: Int?,
    @SerializedName("prep_time") val prepTime: String?,
    val calories: Int?,
    @SerializedName("image_url") val imageUrl: String?,
    val badge: String?,
    val ingredients: List<String>?,
    val customizations: CustomizationsDto?
)

data class CustomizationsDto(
    val sizes: List<SizeDto>?,
    @SerializedName("sugar_levels") val sugarLevels: List<String>?,
    @SerializedName("milk_options") val milkOptions: List<MilkDto>?
)

data class SizeDto(
    val id: String?,
    val label: String?,
    @SerializedName("extra_price") val extraPrice: Double?
)

data class MilkDto(
    val id: String?,
    val name: String?,
    @SerializedName("extra_price") val extraPrice: Double?
)
