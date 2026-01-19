package com.example.fastbite

import androidx.compose.runtime.*

// Простой наблюдатель за количеством конкретного товара
@Composable
fun observeCartItemQuantity(dishId: String): Int {
    val quantity = remember {
        derivedStateOf { CartManager.getItemQuantity(dishId) }
    }
    return quantity.value
}

// Наблюдатель за общим количеством товаров
@Composable
fun observeTotalItems(): Int {
    val totalItems = remember {
        derivedStateOf { CartManager.getTotalItems() }
    }
    return totalItems.value
}

// Наблюдатель за общей суммой
@Composable
fun observeTotalPrice(): Double {
    val totalPrice = remember {
        derivedStateOf { CartManager.getTotalPrice() }
    }
    return totalPrice.value
}