package com.example.fastbite

data class Dish(
    val id: String = "",
    val name: String = "",
    val price: String = "",
    val description: String = "",
    val photoUrl: String = "",
    val category: String = "",
    val weightOrVolume: String = "",
    val ingredients: String = "",
    val calories: String = "",
    val proteins: String = "",
    val fats: String = "",
    val carbs: String = "",
    val cookingTime: String = "",
    val spiciness: String = "",
    val vegetarian: Boolean = false,
    val allergens: String = "",
    val addOns: String = "",
    val addOnsPrice: String = "",
    val availability: Boolean = true,

    // ⭐ РЕЙТИНГ (ТОЛЬКО ПОЛЬЗОВАТЕЛИ)
    val ratingAverage: Double = 0.0,
    val ratingCount: Long = 0,

    val portions: String = "",
    val costPrice: String = "",
    val discount: String = "",
    val popular: Boolean = false,
    val dateAdded: String = "",
    val owner: String = ""
)
