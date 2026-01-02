package com.example.fastbite

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDishScreen(
    currentUserEmail: String,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var weightOrVolume by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var proteins by remember { mutableStateOf("") }
    var fats by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var cookingTime by remember { mutableStateOf("") }
    var spiciness by remember { mutableStateOf("") }
    var vegetarian by remember { mutableStateOf(false) }
    var allergens by remember { mutableStateOf("") }
    var addOns by remember { mutableStateOf("") }
    var addOnsPrice by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf(true) }
    var rating by remember { mutableStateOf("") }
    var portions by remember { mutableStateOf("") }
    var costPrice by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("") }
    var popular by remember { mutableStateOf(false) }

    var error by remember { mutableStateOf("") }
    val db = Firebase.firestore
    val dateAdded = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавить блюдо") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Назад") }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    if (name.isBlank() || price.isBlank()) {
                        error = "Заполните обязательные поля"
                        return@Button
                    }

                    val newDish = Dish(
                        name = name,
                        price = price,
                        description = description,
                        photoUrl = photoUrl,
                        category = category,
                        weightOrVolume = weightOrVolume,
                        ingredients = ingredients,
                        calories = calories,
                        proteins = proteins,
                        fats = fats,
                        carbs = carbs,
                        cookingTime = cookingTime,
                        spiciness = spiciness,
                        vegetarian = vegetarian,
                        allergens = allergens,
                        addOns = addOns,
                        addOnsPrice = addOnsPrice,
                        availability = availability,
                        rating = rating,
                        portions = portions,
                        costPrice = costPrice,
                        discount = discount,
                        popular = popular,
                        dateAdded = dateAdded,
                        owner = currentUserEmail
                    )

                    db.collection("dishes")
                        .add(newDish)
                        .addOnSuccessListener { onBack() }
                        .addOnFailureListener { error = it.message ?: "Ошибка сохранения" }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Сохранить")
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название блюда") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Цена") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = photoUrl,
                    onValueChange = { photoUrl = it },
                    label = { Text("Ссылка на фото") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Категория") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = weightOrVolume,
                    onValueChange = { weightOrVolume = it },
                    label = { Text("Вес / объём") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ingredients,
                    onValueChange = { ingredients = it },
                    label = { Text("Ингредиенты") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Калорийность") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = proteins,
                    onValueChange = { proteins = it },
                    label = { Text("Белки") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fats,
                    onValueChange = { fats = it },
                    label = { Text("Жиры") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = carbs,
                    onValueChange = { carbs = it },
                    label = { Text("Углеводы") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = cookingTime,
                    onValueChange = { cookingTime = it },
                    label = { Text("Время приготовления") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = spiciness,
                    onValueChange = { spiciness = it },
                    label = { Text("Острота") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = vegetarian, onCheckedChange = { vegetarian = it })
                    Text("Вегетарианское")
                }

                OutlinedTextField(
                    value = allergens,
                    onValueChange = { allergens = it },
                    label = { Text("Аллергены") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = addOns,
                    onValueChange = { addOns = it },
                    label = { Text("Возможные добавки") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = addOnsPrice,
                    onValueChange = { addOnsPrice = it },
                    label = { Text("Цена добавок") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = availability, onCheckedChange = { availability = it })
                    Text("Доступно")
                }

                OutlinedTextField(
                    value = rating,
                    onValueChange = { rating = it },
                    label = { Text("Рейтинг") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = portions,
                    onValueChange = { portions = it },
                    label = { Text("Количество порций") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = costPrice,
                    onValueChange = { costPrice = it },
                    label = { Text("Себестоимость") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = discount,
                    onValueChange = { discount = it },
                    label = { Text("Скидка / акция") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = popular, onCheckedChange = { popular = it })
                    Text("Популярное блюдо")
                }

                if (error.isNotEmpty()) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(80.dp)) // Отступ для кнопки
            }
        }
    }
}
