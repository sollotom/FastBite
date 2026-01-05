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

                var selectedUserItem by remember { mutableStateOf<BottomNavItem>(BottomNavItem.Menu) }
                var selectedSellerItem by remember { mutableStateOf<SellerBottomNavItem>(SellerBottomNavItem.Menu) }

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
                                                selected = selectedUserItem == item,
                                                onClick = { selectedUserItem = item },
                                                icon = { Icon(item.icon, contentDescription = item.title) },
                                                label = { Text(item.title) }
                                            )
                                        }
                                    }
                                }
                            ) { padding ->
                                Box(
                                    modifier = Modifier
                                        .padding(padding)
                                        .fillMaxSize()
                                ) {
                                    when (selectedUserItem) {
                                        BottomNavItem.Menu -> MenuScreen()
                                        BottomNavItem.Cart -> CartScreen {
                                            selectedUserItem = BottomNavItem.Menu
                                        }
                                        BottomNavItem.Profile -> ProfileScreen(
                                            userEmail = loggedInEmail!!,
                                            onLogout = {
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

                            val navController = rememberNavController()

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
                                                        SellerBottomNavItem.Menu -> navController.navigate("menu") {
                                                            popUpTo("menu") { inclusive = true }
                                                        }
                                                        SellerBottomNavItem.Orders -> navController.navigate("orders") {
                                                            popUpTo("orders") { inclusive = true }
                                                        }
                                                        SellerBottomNavItem.Profile -> navController.navigate("profile") {
                                                            popUpTo("profile") { inclusive = true }
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
                                    navController = navController,
                                    startDestination = "menu",
                                    modifier = Modifier.padding(padding)
                                ) {

                                    composable("menu") {
                                        SellerMenuScreen(
                                            currentUserEmail = loggedInEmail!!,
                                            onAddDishClick = {
                                                navController.navigate("add_dish")
                                            },
                                            onBackClick = {
                                                // Пустое действие, так как это главный экран
                                            }
                                        )
                                    }

                                    composable("add_dish") {
                                        AddDishScreen(
                                            currentUserEmail = loggedInEmail!!,
                                            onBack = { navController.popBackStack() }
                                        )
                                    }

                                    composable("orders") {
                                        SellerOrdersScreen()
                                    }

                                    composable("profile") {
                                        SellerProfileScreen(
                                            currentUserEmail = loggedInEmail!!,
                                            onLogout = {
                                                sharedPrefs.edit().clear().apply()
                                                loggedInEmail = null
                                                loggedInRole = null
                                            },
                                            onBack = { navController.popBackStack() }
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