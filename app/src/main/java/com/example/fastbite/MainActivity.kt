package com.example.fastbite

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fastbite.ui.theme.FastBiteTheme
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.launch

// =================== USER BOTTOM NAV ===================
sealed class BottomNavItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Menu : BottomNavItem("Меню", Icons.Default.Restaurant)
    object Cart : BottomNavItem("Корзина", Icons.Default.ShoppingCart)
    object Profile : BottomNavItem("Профиль", Icons.Default.Person)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        val sharedPrefs = getSharedPreferences("FastBitePrefs", Context.MODE_PRIVATE)

        setContent {
            FastBiteTheme {
                var loggedInEmail by remember { mutableStateOf(sharedPrefs.getString("user_email", null)) }
                var loggedInRole by remember { mutableStateOf(sharedPrefs.getString("user_role", null)) }

                // Для пользователя - управление навигацией
                val userNavController = rememberNavController()
                val coroutineScope = rememberCoroutineScope()

                // Общее количество товаров в корзине (автоматически обновляется)
                // Простой способ - используем наблюдаемое состояние
                var cartItemCount by remember { mutableStateOf(CartManager.getTotalItems()) }

                // Обновляем счетчик при изменении корзины
                LaunchedEffect(CartManager.cartItems) {
                    cartItemCount = CartManager.getTotalItems()
                }

                // Для продавца - управление навигацией
                val sellerNavController = rememberNavController()

                if (loggedInEmail == null || loggedInRole == null) {
                    AuthScreenNew(
                        navToUser = { email, role ->
                            loggedInEmail = email
                            loggedInRole = role
                            sharedPrefs.edit().putString("user_email", email)
                                .putString("user_role", role).apply()
                        },
                        navToSeller = { email, role ->
                            loggedInEmail = email
                            loggedInRole = role
                            sharedPrefs.edit().putString("user_email", email)
                                .putString("user_role", role).apply()
                        }
                    )
                } else {
                    when (loggedInRole) {
                        // ================= USER =================
                        "user" -> {
                            Scaffold(
                                bottomBar = {
                                    NavigationBar {
                                        listOf(
                                            BottomNavItem.Menu,
                                            BottomNavItem.Cart,
                                            BottomNavItem.Profile
                                        ).forEach { item ->
                                            NavigationBarItem(
                                                selected = userNavController.currentDestination?.route == when(item) {
                                                    BottomNavItem.Menu -> "user_menu"
                                                    BottomNavItem.Cart -> "user_cart"
                                                    BottomNavItem.Profile -> "user_profile"
                                                },
                                                onClick = {
                                                    when(item) {
                                                        BottomNavItem.Menu -> userNavController.navigate("user_menu")
                                                        BottomNavItem.Cart -> userNavController.navigate("user_cart")
                                                        BottomNavItem.Profile -> userNavController.navigate("user_profile")
                                                    }
                                                },
                                                icon = {
                                                    Box {
                                                        Icon(item.icon, contentDescription = item.title)
                                                        if (item == BottomNavItem.Cart && cartItemCount > 0) {
                                                            Badge(
                                                                modifier = Modifier.align(Alignment.TopEnd)
                                                            ) {
                                                                Text(cartItemCount.toString())
                                                            }
                                                        }
                                                    }
                                                },
                                                label = { Text(item.title) }
                                            )
                                        }
                                    }
                                }
                            ) { padding ->
                                NavHost(
                                    navController = userNavController,
                                    startDestination = "user_menu",
                                    modifier = Modifier.padding(padding)
                                ) {
                                    composable("user_menu") {
                                        MenuScreen(
                                            onNavigateToRestaurantProfile = { restaurantId ->
                                                userNavController.navigate("user_restaurant_profile/$restaurantId")
                                            }
                                        )
                                    }

                                    composable("user_restaurant_profile/{restaurantId}") { backStackEntry ->
                                        val restaurantId = backStackEntry.arguments?.getString("restaurantId") ?: ""
                                        RestaurantProfileScreen(
                                            restaurantId = restaurantId,
                                            onBack = { userNavController.popBackStack() }
                                        )
                                    }

                                    composable("user_cart") {
                                        CartScreen(
                                            onNavigateToMenu = { userNavController.navigate("user_menu") },
                                            onNavigateToCheckout = {
                                                userNavController.navigate("user_checkout")
                                            }
                                        )
                                    }

                                    composable("user_checkout") {
                                        CheckoutScreen(
                                            onBackClick = { userNavController.popBackStack() },
                                            onOrderConfirmed = {
                                                // После успешного оформления заказа возвращаемся в корзину
                                                userNavController.navigate("user_cart") {
                                                    popUpTo("user_menu") { inclusive = false }
                                                }
                                            }
                                        )
                                    }

                                    composable("user_profile") {
                                        ProfileScreen(
                                            userEmail = loggedInEmail!!,
                                            onLogout = {
                                                CartManager.clearCart()
                                                sharedPrefs.edit().clear().apply()
                                                loggedInEmail = null
                                                loggedInRole = null
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // ================= SELLER =================
                        "seller" -> {
                            var selectedSellerItem by remember { mutableStateOf<SellerBottomNavItem>(SellerBottomNavItem.Menu) }

                            Scaffold(
                                bottomBar = {
                                    NavigationBar {
                                        listOf(
                                            SellerBottomNavItem.Menu,
                                            SellerBottomNavItem.Orders,
                                            SellerBottomNavItem.Profile
                                        ).forEach { item ->
                                            NavigationBarItem(
                                                selected = selectedSellerItem == item,
                                                onClick = {
                                                    selectedSellerItem = item
                                                    when (item) {
                                                        SellerBottomNavItem.Menu -> sellerNavController.navigate("seller_menu") {
                                                            popUpTo("seller_menu") { inclusive = true }
                                                        }
                                                        SellerBottomNavItem.Orders -> sellerNavController.navigate("seller_orders") {
                                                            popUpTo("seller_orders") { inclusive = true }
                                                        }
                                                        SellerBottomNavItem.Profile -> sellerNavController.navigate("seller_profile") {
                                                            popUpTo("seller_profile") { inclusive = true }
                                                        }
                                                    }
                                                },
                                                icon = { Icon(item.icon, contentDescription = item.title) },
                                                label = { Text(item.title) }
                                            )
                                        }
                                    }
                                }
                            ) { padding ->
                                NavHost(
                                    navController = sellerNavController,
                                    startDestination = "seller_menu",
                                    modifier = Modifier.padding(padding)
                                ) {
                                    composable("seller_menu") {
                                        SellerMenuScreen(
                                            currentUserEmail = loggedInEmail!!,
                                            onAddDishClick = {
                                                sellerNavController.navigate("seller_add_dish")
                                            },
                                            onBackClick = {
                                                sellerNavController.popBackStack()
                                            },
                                            onProfileClick = {
                                                sellerNavController.navigate("seller_profile") {
                                                    popUpTo("seller_menu")
                                                }
                                            }
                                        )
                                    }

                                    composable("seller_add_dish") {
                                        AddDishScreen(
                                            currentUserEmail = loggedInEmail!!,
                                            onBack = { sellerNavController.popBackStack() }
                                        )
                                    }

                                    composable("seller_orders") {
                                        SellerOrdersScreen()
                                    }

                                    composable("seller_profile") {
                                        SellerProfileScreen(
                                            currentUserEmail = loggedInEmail!!,
                                            onLogout = {
                                                sharedPrefs.edit().clear().apply()
                                                loggedInEmail = null
                                                loggedInRole = null
                                            },
                                            onBack = { sellerNavController.popBackStack() }
                                        )
                                    }
                                }
                            }
                        }

                        else -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Неизвестная роль")
                            }
                        }
                    }
                }
            }
        }
    }
}