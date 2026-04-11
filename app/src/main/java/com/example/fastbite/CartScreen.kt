package com.example.fastbite

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// Строки для экрана корзины
object CartStrings {
    var currentLanguage = Strings.currentLanguage

    val cart: String get() = if (currentLanguage.value == Language.KAZAKH) "Себет" else "Корзина"
    val back: String get() = if (currentLanguage.value == Language.KAZAKH) "Артқа" else "Назад"
    val total: String get() = if (currentLanguage.value == Language.KAZAKH) "Барлығы" else "Итого"
    val checkout: String get() = if (currentLanguage.value == Language.KAZAKH) "Тапсырыс беру" else "Оформить заказ"
    val emptyCart: String get() = if (currentLanguage.value == Language.KAZAKH) "Себет бос" else "Корзина пуста"
    val addItemsFromMenu: String get() = if (currentLanguage.value == Language.KAZAKH) "Мәзірден тауарлар қосыңыз" else "Добавьте товары из меню"
    val goToMenu: String get() = if (currentLanguage.value == Language.KAZAKH) "Мәзірге өту" else "Перейти в меню"
    val decrease: String get() = if (currentLanguage.value == Language.KAZAKH) "Азайту" else "Уменьшить"
    val increase: String get() = if (currentLanguage.value == Language.KAZAKH) "Көбейту" else "Увеличить"

    fun getItemsWord(count: Int): String {
        return if (currentLanguage.value == Language.KAZAKH) {
            when {
                count % 10 == 1 && count % 100 != 11 -> "тауар"
                count % 10 in 2..4 && count % 100 !in 12..14 -> "тауар"
                else -> "тауар"
            }
        } else {
            when {
                count % 10 == 1 && count % 100 != 11 -> "товар"
                count % 10 in 2..4 && count % 100 !in 12..14 -> "товара"
                else -> "товаров"
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onNavigateToMenu: () -> Unit,
    onNavigateToCheckout: () -> Unit
) {
    val cartItems = CartManager.cartItems
    val totalPrice by derivedStateOf { CartManager.getTotalPrice() }
    val totalItems by derivedStateOf { CartManager.getTotalItems() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = CartStrings.cart,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToMenu) {
                        Icon(Icons.Default.ArrowBack, contentDescription = CartStrings.back)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${CartStrings.total}: ${"%.0f".format(totalPrice)} тг",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$totalItems ${CartStrings.getItemsWord(totalItems)}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        Button(
                            onClick = onNavigateToCheckout,
                            modifier = Modifier.height(50.dp),
                            enabled = cartItems.isNotEmpty(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = CartStrings.checkout,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = CartStrings.emptyCart,
                    modifier = Modifier.size(80.dp),
                    tint = Color.Gray
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = CartStrings.emptyCart,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = CartStrings.addItemsFromMenu,
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onNavigateToMenu,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = CartStrings.goToMenu
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems) { cartItem ->
                    CartItemCard(cartItem = cartItem)
                }

                item {
                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun CartItemCard(cartItem: CartItem) {
    val dish = cartItem.dish
    val currentQuantity = cartItem.quantity

    val discountPercentage = dish.discount?.toDoubleOrNull() ?: 0.0
    val originalPrice = dish.price.toDoubleOrNull() ?: 0.0
    val discountedPrice = if (discountPercentage > 0)
        originalPrice * (1 - discountPercentage / 100)
    else originalPrice
    val itemTotal = discountedPrice * currentQuantity

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = dish.photoUrl,
                contentDescription = dish.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = dish.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 2
                )

                Spacer(Modifier.height(4.dp))

                if (discountPercentage > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${"%.0f".format(discountedPrice)} тг",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${"%.0f".format(originalPrice)} тг",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                } else {
                    Text(
                        text = "${"%.0f".format(originalPrice)} тг",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (currentQuantity > 1) {
                                CartManager.decrementQuantity(dish.id)
                            } else {
                                CartManager.removeFromCart(dish.id)
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = CartStrings.decrease,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "$currentQuantity",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .width(32.dp)
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )

                    IconButton(
                        onClick = {
                            CartManager.addToCart(dish)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = CartStrings.increase,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "${"%.0f".format(itemTotal)} тг",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}