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
import com.example.fastbite.ui.theme.FastBiteTheme
import com.google.firebase.FirebaseApp

sealed class BottomNavItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Profile : BottomNavItem("Профиль", Icons.Default.Person)
    object Cart : BottomNavItem("Корзина", Icons.Default.ShoppingCart)
    object Menu : BottomNavItem("Меню", Icons.Default.Restaurant)
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
                var selectedSellerItem by remember { mutableStateOf<SellerBottomNavItem>(
                    SellerBottomNavItem.Menu) }

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

                        // ---------------- USER MODE ----------------
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
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .padding(innerPadding)
                                        .padding(16.dp)
                                        .fillMaxSize()
                                ) {
                                    when (selectedUserItem) {
                                        is BottomNavItem.Menu -> MenuScreen()
                                        is BottomNavItem.Cart -> CartScreen(onNavigateToMenu = {
                                            selectedUserItem = BottomNavItem.Menu
                                        })
                                        is BottomNavItem.Profile -> ProfileScreen(
                                            userEmail = loggedInEmail!!,
                                            onLogout = {
                                                sharedPrefs.edit()
                                                    .clear()
                                                    .apply()
                                                loggedInEmail = null
                                                loggedInRole = null
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // ---------------- SELLER MODE ----------------
                        "seller" -> {
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
                                                onClick = { selectedSellerItem = item },
                                                icon = { Icon(item.icon, contentDescription = item.title) },
                                                label = { Text(item.title) }
                                            )
                                        }
                                    }
                                }
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .padding(innerPadding)
                                        .padding(16.dp)
                                        .fillMaxSize()
                                ) {
                                    when (selectedSellerItem) {
                                        is SellerBottomNavItem.Menu -> SellerMenuScreen()
                                        is SellerBottomNavItem.Orders -> SellerOrdersScreen()
                                        is SellerBottomNavItem.Profile -> SellerProfileScreen(
                                            restaurantName = "Мой ресторан",
                                            email = loggedInEmail!!,
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

                        else -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Неизвестная роль")
                            }
                        }
                    }
                }
            }
        }
    }
}
