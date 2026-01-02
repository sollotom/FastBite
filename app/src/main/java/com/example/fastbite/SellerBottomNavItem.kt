package com.example.fastbite

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class SellerBottomNavItem(val title: String, val icon: ImageVector) {
    object Menu : SellerBottomNavItem("Меню", Icons.Default.Restaurant)
    object Orders : SellerBottomNavItem("Заказы", Icons.Default.ShoppingCart)
    object Profile : SellerBottomNavItem("Профиль", Icons.Default.Person)
}
