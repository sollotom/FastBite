package com.example.fastbite

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerProfileScreen(
    currentUserEmail: String,
    onLogout: () -> Unit
) {
    val db = Firebase.firestore

    var restaurantName by remember { mutableStateOf("Неизвестно") }
    var restaurantDescription by remember { mutableStateOf("Описание ресторана отсутствует") }
    var restaurantIcon by remember { mutableStateOf("") }
    var isEditingProfile by remember { mutableStateOf(false) }

    var tempName by remember { mutableStateOf(restaurantName) }
    var tempDescription by remember { mutableStateOf(restaurantDescription) }
    var tempIcon by remember { mutableStateOf(restaurantIcon) }

    var dishes by remember { mutableStateOf(listOf<Dish>()) }

    val categories = listOf(
        "Супы", "Салаты", "Горячие блюда", "Закуски",
        "Пицца / Лепёшки / Хлеб", "Бургеры и сэндвичи",
        "Десерты", "Напитки", "Завтраки / Бранч",
        "Вегетарианские / Веганские блюда", "Суши и роллы",
        "Блюда на гриле / Барбекю", "Детское меню",
        "Комбо / Наборы", "Другое"
    )

    // Загрузка данных ресторана
    LaunchedEffect(currentUserEmail) {
        db.collection("restaurants").document(currentUserEmail).get()
            .addOnSuccessListener { doc ->
                restaurantName = doc.getString("name") ?: "Неизвестно"
                restaurantDescription = doc.getString("description") ?: "Описание ресторана отсутствует"
                restaurantIcon = doc.getString("iconUrl") ?: ""

                tempName = restaurantName
                tempDescription = restaurantDescription
                tempIcon = restaurantIcon
            }

        db.collection("dishes")
            .whereEqualTo("owner", currentUserEmail)
            .get()
            .addOnSuccessListener { result ->
                dishes = result.map {
                    Dish(
                        id = it.id,
                        name = it.getString("name") ?: "",
                        price = it.getString("price") ?: "",
                        category = it.getString("category") ?: "",
                        description = it.getString("description") ?: "",
                        photoUrl = it.getString("photoUrl") ?: "",
                        ratingAverage = it.getDouble("ratingAverage") ?: 0.0,
                        owner = currentUserEmail
                    )
                }
            }
    }

    val restaurantRating = if (dishes.isNotEmpty()) dishes.map { it.ratingAverage }.average() else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Верхняя панель с кнопками
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { isEditingProfile = !isEditingProfile }) {
                Icon(Icons.Default.Edit, contentDescription = "Редактировать профиль")
            }
            IconButton(onClick = onLogout) {
                Icon(Icons.Default.Logout, contentDescription = "Выйти")
            }
        }

        // Иконка ресторана
        Image(
            painter = rememberAsyncImagePainter(if (tempIcon.isNotEmpty()) tempIcon else "https://via.placeholder.com/150"),
            contentDescription = "Иконка ресторана",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.Gray)
                .align(Alignment.CenterHorizontally),
            contentScale = ContentScale.Crop
        )

        // Название и рейтинг
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isEditingProfile) {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("Название ресторана") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tempIcon,
                    onValueChange = { tempIcon = it },
                    label = { Text("Ссылка на иконку") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tempDescription,
                    onValueChange = { tempDescription = it },
                    label = { Text("Описание ресторана") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = {
                    // Сохраняем в Firestore
                    val data = mapOf(
                        "name" to tempName,
                        "description" to tempDescription,
                        "iconUrl" to tempIcon
                    )
                    db.collection("restaurants").document(currentUserEmail).set(data)

                    restaurantName = tempName
                    restaurantDescription = tempDescription
                    restaurantIcon = tempIcon
                    isEditingProfile = false
                }) {
                    Text("Сохранить")
                }
            } else {
                Text(
                    text = restaurantName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )

                // ⭐ рейтинг в стиле SellerMenuScreen
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val full = restaurantRating.toInt()
                    val half = (restaurantRating % 1) >= 0.5

                    repeat(full) { Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp)) }
                    if (half) Icon(Icons.Filled.StarHalf, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                    repeat(5 - full - if (half) 1 else 0) { Icon(Icons.Outlined.Star, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp)) }

                    Text(" ${"%.1f".format(restaurantRating)}", fontSize = 14.sp)
                }

                Text(
                    text = restaurantDescription,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // --- Блюда ---
        categories.forEach { category ->
            val categoryDishes = dishes.filter { it.category == category }
            if (categoryDishes.isNotEmpty()) {
                Text(category, style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    categoryDishes.forEach { dish ->
                        Card(
                            modifier = Modifier
                                .width(180.dp)
                                .height(200.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(dish.photoUrl),
                                    contentDescription = dish.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Text(dish.name, style = MaterialTheme.typography.bodyLarge)
                                Text("${dish.price} тг", color = MaterialTheme.colorScheme.primary)
                                // ⭐ рейтинг блюда
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val full = dish.ratingAverage.toInt()
                                    val half = (dish.ratingAverage % 1) >= 0.5
                                    repeat(full) { Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp)) }
                                    if (half) Icon(Icons.Filled.StarHalf, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                                    repeat(5 - full - if (half) 1 else 0) { Icon(Icons.Outlined.Star, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp)) }
                                    Text(" ${"%.1f".format(dish.ratingAverage)}", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Отзывы ---
        Spacer(Modifier.height(16.dp))
        Text("Отзывы", style = MaterialTheme.typography.titleMedium)
        Text("Пока отзывов нет", style = MaterialTheme.typography.bodyMedium)
    }
}
