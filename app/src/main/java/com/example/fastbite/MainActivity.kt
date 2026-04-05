package com.example.fastbite

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.example.fastbite.ui.theme.FastBiteTheme
import com.google.firebase.FirebaseApp

sealed class BottomNavItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
) {
    object Menu : BottomNavItem("Меню", Icons.Default.Restaurant, "user_menu")
    object Cart : BottomNavItem("Корзина", Icons.Default.ShoppingCart, "user_cart")
    object Profile : BottomNavItem("Профиль", Icons.Default.Person, "user_profile")
}


// Добавляем SellerBottomNavItem
sealed class SellerBottomNavItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
) {
    object Menu : SellerBottomNavItem("Меню", Icons.Default.Restaurant, "seller_menu")
    object Orders : SellerBottomNavItem("Заказы", Icons.Default.ShoppingCart, "seller_orders")
    object Profile : SellerBottomNavItem("Профиль", Icons.Default.Person, "seller_profile")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        val sharedPrefs = getSharedPreferences("FastBitePrefs", Context.MODE_PRIVATE)

        setContent {
            FastBiteTheme {

                var loggedInEmail by remember {
                    mutableStateOf(sharedPrefs.getString("user_email", null))
                }
                var loggedInRole by remember {
                    mutableStateOf(sharedPrefs.getString("user_role", null))
                }

                if (loggedInEmail == null || loggedInRole == null) {
                    AuthScreen(
                        navToUser = { email, role ->
                            loggedInEmail = email
                            loggedInRole = role
                            sharedPrefs.edit()
                                .putString("user_email", email)
                                .putString("user_role", role)
                                .apply()
                        },
                        navToSeller = { email, role ->
                            loggedInEmail = email
                            loggedInRole = role
                            sharedPrefs.edit()
                                .putString("user_email", email)
                                .putString("user_role", role)
                                .apply()
                        }
                    )
                } else {
                    when (loggedInRole) {
                        "user" -> UserApp(loggedInEmail!!, sharedPrefs) {
                            loggedInEmail = null
                            loggedInRole = null
                        }
                        "seller" -> SellerApp(loggedInEmail!!, sharedPrefs) {
                            loggedInEmail = null
                            loggedInRole = null
                        }
                        else -> ErrorScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun UserApp(
    userEmail: String,
    sharedPrefs: android.content.SharedPreferences,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    // 💥 БЕЗ route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route ?: ""

    var cartItemCount by remember { mutableStateOf(CartManager.getTotalItems()) }

    LaunchedEffect(CartManager.cartItems) {
        cartItemCount = CartManager.getTotalItems()
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(
                    BottomNavItem.Menu,
                    BottomNavItem.Cart,
                    BottomNavItem.Profile
                ).forEach { item ->

                    NavigationBarItem(
                        selected = currentDestination == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo("user_menu") { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        icon = {
                            Box {
                                Icon(item.icon, contentDescription = item.title)

                                if (item == BottomNavItem.Cart && cartItemCount > 0) {
                                    Badge(
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Text(
                                            if (cartItemCount > 99) "99+"
                                            else cartItemCount.toString()
                                        )
                                    }
                                }
                            }
                        },
                        label = { Text(item.title) }
                    )
                }
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = "user_menu",
            modifier = Modifier.padding(paddingValues)
        ) {

            composable("user_menu") {
                MenuScreen(
                    onNavigateToRestaurantProfile = {
                        navController.navigate("user_restaurant_profile/$it")
                    }
                )
            }

            composable("user_restaurant_profile/{restaurantId}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("restaurantId") ?: ""
                RestaurantProfileScreen(
                    restaurantId = id,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("user_cart") {
                CartScreen(
                    onNavigateToMenu = { navController.navigate("user_menu") },
                    onNavigateToCheckout = { navController.navigate("user_checkout") }
                )
            }

            composable("user_checkout") {
                CheckoutScreen(
                    onBackClick = { navController.popBackStack() },
                    onOrderConfirmed = {
                        navController.navigate("user_cart") {
                            popUpTo("user_menu") { inclusive = false }
                        }
                    }
                )
            }

            composable("user_profile") {
                ProfileScreen(
                    userEmail = userEmail,
                    onLogout = {
                        CartManager.clearCart()
                        sharedPrefs.edit().clear().apply()
                        onLogout()
                    },
                    onNavigateToOrders = {
                        navController.navigate("user_orders")
                    }
                )
            }

            composable("user_orders") {
                UserOrdersScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun SellerApp(
    userEmail: String,
    sharedPrefs: android.content.SharedPreferences,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route ?: ""

    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(
                    SellerBottomNavItem.Menu,
                    SellerBottomNavItem.Orders,
                    SellerBottomNavItem.Profile
                ).forEach { item ->

                    NavigationBarItem(
                        selected = currentDestination == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo("seller_menu") { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        icon = {
                            Icon(item.icon, contentDescription = item.title)
                        },
                        label = { Text(item.title) }
                    )
                }
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = "seller_menu",
            modifier = Modifier.padding(paddingValues)
        ) {

            composable("seller_menu") {
                SellerMenuScreen(
                    currentUserEmail = userEmail,
                    onAddDishClick = { navController.navigate("seller_add_dish") },
                    onBackClick = { navController.popBackStack() },
                    onProfileClick = { navController.navigate("seller_profile") }
                )
            }

            composable("seller_add_dish") {
                AddDishScreen(
                    currentUserEmail = userEmail,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("seller_orders") {
                SellerOrdersScreen()
            }

            composable("seller_profile") {
                SellerProfileScreen(
                    currentUserEmail = userEmail,
                    onLogout = {
                        sharedPrefs.edit().clear().apply()
                        onLogout()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun ErrorScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Неизвестная роль пользователя")
    }
}