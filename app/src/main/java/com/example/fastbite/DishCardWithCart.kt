package com.example.fastbite

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// Карточка блюда с корзиной (упрощенная версия)
@Composable
fun DishCardWithCartSimple(
    dish: Dish,
    onClick: () -> Unit
) {
    // Используем простой наблюдатель для мгновенного обновления
    val quantity = observeCartItemQuantity(dish.id)

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onClick)
            ) {
                AsyncImage(
                    model = dish.photoUrl,
                    contentDescription = dish.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                dish.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable(onClick = onClick)
            )

            val discountPercentage = dish.discount?.toDoubleOrNull() ?: 0.0
            val originalPrice = dish.price.toDoubleOrNull() ?: 0.0
            val discountedPrice = if (discountPercentage > 0)
                originalPrice * (1 - discountPercentage / 100)
            else originalPrice

            if (discountPercentage > 0) {
                Column(modifier = Modifier.clickable(onClick = onClick)) {
                    Text(
                        "${"%.0f".format(discountedPrice)} тг",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        "${"%.0f".format(originalPrice)} тг",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        style = androidx.compose.ui.text.TextStyle(textDecoration = TextDecoration.LineThrough)
                    )
                }
            } else {
                Text(
                    "${"%.0f".format(originalPrice)} тг",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable(onClick = onClick)
                )
            }

            // Кнопка корзины - мгновенное обновление
            CartButtonWithInstantUpdate(dish = dish, currentQuantity = quantity)
        }
    }
}

// Кнопка корзины с мгновенным обновлением
@Composable
fun CartButtonWithInstantUpdate(
    dish: Dish,
    currentQuantity: Int,
    modifier: Modifier = Modifier
) {
    if (currentQuantity == 0) {
        // Кнопка добавления
        Button(
            onClick = { CartManager.addToCart(dish) },
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Добавить", fontWeight = FontWeight.Medium)
        }
    } else {
        // Управление количеством
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Кнопка уменьшения
            IconButton(
                onClick = { CartManager.decrementQuantity(dish.id) },
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "Уменьшить",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Количество
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    currentQuantity.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "в корзине",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            // Кнопка увеличения
            IconButton(
                onClick = { CartManager.incrementQuantity(dish.id) },
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Увеличить",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}