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
    val ratingAverage: Double = 0.0,
    val ratingCount: Long = 0L,
    val portions: String = "",
    val costPrice: String = "",
    val discount: String = "",
    val popular: Boolean = false,
    val dateAdded: String = "",
    val owner: String = "",
    val reviews: List<Review> = emptyList()  // Добавляем поле reviews
)

data class Review(
    val id: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val rating: Double = 0.0,
    val comment: String = "",
    val date: String = "",
    val dishId: String = ""
)