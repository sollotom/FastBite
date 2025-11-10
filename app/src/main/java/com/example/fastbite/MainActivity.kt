package com.example.fastbite

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fastbite.ui.theme.FastBiteTheme

sealed class BottomNavItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Profile : BottomNavItem("Профиль", Icons.Default.Person)
    object Cart : BottomNavItem("Корзина", Icons.Default.ShoppingCart)
    object Menu : BottomNavItem("Меню", Icons.Default.Restaurant)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FastBiteTheme {

                // ✅ Чтение сохранённого email
                val sharedPrefs = getSharedPreferences("FastBitePrefs", Context.MODE_PRIVATE)
                var loggedInUser by remember {
                    mutableStateOf(sharedPrefs.getString("user_email", null))
                }

                var selectedItem by remember { mutableStateOf<BottomNavItem>(BottomNavItem.Menu) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (loggedInUser == null) {
                        AuthScreen(onLoginSuccess = { email ->
                            loggedInUser = email

                            // ✅ Сохраняем email при входе
                            sharedPrefs.edit()
                                .putString("user_email", email)
                                .apply()
                        })
                    } else {
                        Scaffold(
                            bottomBar = {
                                NavigationBar {
                                    listOf(
                                        BottomNavItem.Menu,
                                        BottomNavItem.Cart,
                                        BottomNavItem.Profile
                                    ).forEach { item ->
                                        NavigationBarItem(
                                            selected = selectedItem == item,
                                            onClick = { selectedItem = item },
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
                                    .padding(top = 20.dp, start = 16.dp, end = 16.dp)
                                    .fillMaxSize()
                            ) {
                                when (selectedItem) {
                                    is BottomNavItem.Menu -> MenuScreen()
                                    is BottomNavItem.Cart -> CartScreen(onNavigateToMenu = {
                                        selectedItem = BottomNavItem.Menu
                                    })
                                    is BottomNavItem.Profile -> ProfileScreen(
                                        userEmail = loggedInUser!!,
                                        onLogout = {
                                            // ✅ Удаляем email при выходе
                                            sharedPrefs.edit()
                                                .remove("user_email")
                                                .apply()
                                            loggedInUser = null
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
