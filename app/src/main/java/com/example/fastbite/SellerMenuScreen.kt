package com.example.fastbite

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun SellerMenuScreen(
    currentUserEmail: String,
    onAddDishClick: () -> Unit
) {
    val db = Firebase.firestore
    var dishes by remember { mutableStateOf(listOf<Dish>()) }
    var dishToEdit by remember { mutableStateOf<Dish?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Dish?>(null) }

    // Загрузка данных
    LaunchedEffect(currentUserEmail) {
        db.collection("dishes")
            .whereEqualTo("owner", currentUserEmail)
            .get()
            .addOnSuccessListener { result ->
                dishes = result.map {
                    Dish(
                        id = it.id,
                        name = it.getString("name") ?: "",
                        price = it.getString("price") ?: "",
                        description = it.getString("description") ?: "",
                        photoUrl = it.getString("photoUrl") ?: "",
                        category = it.getString("category") ?: "",
                        weightOrVolume = it.getString("weightOrVolume") ?: "",
                        ingredients = it.getString("ingredients") ?: "",
                        calories = it.getString("calories") ?: "",
                        proteins = it.getString("proteins") ?: "",
                        fats = it.getString("fats") ?: "",
                        carbs = it.getString("carbs") ?: "",
                        cookingTime = it.getString("cookingTime") ?: "",
                        spiciness = it.getString("spiciness") ?: "",
                        vegetarian = it.getBoolean("vegetarian") ?: false,
                        allergens = it.getString("allergens") ?: "",
                        addOns = it.getString("addOns") ?: "",
                        addOnsPrice = it.getString("addOnsPrice") ?: "",
                        availability = it.getBoolean("availability") ?: true,
                        rating = it.getString("rating") ?: "",
                        portions = it.getString("portions") ?: "",
                        costPrice = it.getString("costPrice") ?: "",
                        discount = it.getString("discount") ?: "",
                        popular = it.getBoolean("popular") ?: false,
                        dateAdded = it.getString("dateAdded") ?: ""
                    )
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, start = 12.dp, end = 12.dp)
    ) {
        // Заголовок
        Text(
            text = "Ваше меню",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(dishes) { dish ->
                    // Серый полупрозрачный фон для блока
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Gray.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                        ) {
                            // Изображение блюда
                            AsyncImage(
                                model = dish.photoUrl,
                                contentDescription = dish.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Название и цена в одной строке
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = dish.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${dish.price} тг",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Кнопки редактирования и удаления
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = { dishToEdit = dish }) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Редактировать"
                                    )
                                }

                                IconButton(onClick = { showDeleteDialog = dish }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Удалить",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Кнопка "Добавить блюдо"
            Button(
                onClick = onAddDishClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text("Добавить блюдо")
            }
        }
    }

    // Диалог удаления
    showDeleteDialog?.let { dish ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Удалить блюдо?") },
            text = { Text("Вы уверены, что хотите удалить ${dish.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    db.collection("dishes").document(dish.id).delete()
                    dishes = dishes.filter { it.id != dish.id }
                    showDeleteDialog = null
                }) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Отмена") }
            }
        )
    }

    // Редактирование блюда
    dishToEdit?.let { dish ->
        EditDishDialog(
            dish = dish,
            onDismiss = { dishToEdit = null },
            onSave = { updatedDish ->
                db.collection("dishes").document(dish.id)
                    .set(updatedDish)
                    .addOnSuccessListener {
                        dishes = dishes.map { if (it.id == dish.id) updatedDish.copy(id = dish.id) else it }
                        dishToEdit = null
                    }
            }
        )
    }
}
