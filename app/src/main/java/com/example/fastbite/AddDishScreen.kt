package com.example.fastbite

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val db = Firebase.firestore
    val dateAdded = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val categories = listOf(
        "Супы", "Салаты", "Горячие блюда", "Закуски",
        "Пицца / Лепёшки / Хлеб", "Бургеры и сэндвичи",
        "Десерты", "Напитки", "Завтраки / Бранч",
        "Вегетарианские / Веганские блюда", "Суши и роллы",
        "Блюда на гриле / Барбекю", "Детское меню",
        "Комбо / Наборы", "Другое"
    )

    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var categorySelected by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
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
    var portions by remember { mutableStateOf("") }
    var costPrice by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("") }
    var popular by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val filteredCategories = remember(category) {
        if (!categorySelected && category.isNotBlank())
            categories.filter { it.contains(category, ignoreCase = true) }
        else emptyList()
    }

    val categoryValid = categories.contains(category)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавить блюдо", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Основная информация",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название блюда *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    isError = name.isBlank()
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Цена *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    isError = price.isBlank()
                )

                Column {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {
                            category = it
                            categorySelected = false
                        },
                        label = { Text("Категория") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )

                    filteredCategories.take(5).forEach { cat ->
                        Text(
                            text = cat,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    category = cat
                                    categorySelected = true
                                }
                                .padding(vertical = 8.dp, horizontal = 12.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (category.isNotBlank() && filteredCategories.isEmpty() && !categoryValid) {
                        Text(
                            text = "Такой категории нет, будет сохранено как «Другое»",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = MaterialTheme.shapes.medium
                )

                OutlinedTextField(
                    value = photoUrl,
                    onValueChange = { photoUrl = it },
                    label = { Text("Ссылка на фото") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
            }

            item {
                Text(
                    "Характеристики",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                OutlinedTextField(
                    value = weightOrVolume,
                    onValueChange = { weightOrVolume = it },
                    label = { Text("Вес / объём") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                OutlinedTextField(
                    value = ingredients,
                    onValueChange = { ingredients = it },
                    label = { Text("Ингредиенты") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = MaterialTheme.shapes.medium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = calories,
                        onValueChange = { calories = it },
                        label = { Text("Калории") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = cookingTime,
                        onValueChange = { cookingTime = it },
                        label = { Text("Время (мин)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = proteins,
                        onValueChange = { proteins = it },
                        label = { Text("Белки") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = fats,
                        onValueChange = { fats = it },
                        label = { Text("Жиры") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text("Углеводы") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                }

                OutlinedTextField(
                    value = spiciness,
                    onValueChange = { spiciness = it },
                    label = { Text("Острота (1-5)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                OutlinedTextField(
                    value = allergens,
                    onValueChange = { allergens = it },
                    label = { Text("Аллергены") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
            }

            item {
                Text(
                    "Дополнительно",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = addOns,
                        onValueChange = { addOns = it },
                        label = { Text("Возможные добавки") },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = addOnsPrice,
                        onValueChange = { addOnsPrice = it },
                        label = { Text("Цена добавок") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = portions,
                        onValueChange = { portions = it },
                        label = { Text("Количество порций") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = costPrice,
                        onValueChange = { costPrice = it },
                        label = { Text("Себестоимость") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                }

                OutlinedTextField(
                    value = discount,
                    onValueChange = { discount = it },
                    label = { Text("Скидка (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = vegetarian,
                        onCheckedChange = { vegetarian = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF4CAF50)
                        )
                    )
                    Text("Вегетарианское", modifier = Modifier.padding(start = 8.dp))

                    Spacer(modifier = Modifier.width(24.dp))

                    Checkbox(
                        checked = popular,
                        onCheckedChange = { popular = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFFF9800)
                        )
                    )
                    Text("Популярное", modifier = Modifier.padding(start = 8.dp))

                    Spacer(modifier = Modifier.width(24.dp))

                    Checkbox(
                        checked = availability,
                        onCheckedChange = { availability = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF2196F3)
                        )
                    )
                    Text("Доступно", modifier = Modifier.padding(start = 8.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))

                if (error.isNotEmpty()) {
                    Text(
                        error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        if (name.isBlank() || price.isBlank()) {
                            error = "Заполните обязательные поля: название и цена"
                            return@Button
                        }

                        isLoading = true
                        error = ""

                        val finalCategory = if (categoryValid) category else "Другое"

                        val newDish = hashMapOf<String, Any>(
                            "name" to name,
                            "price" to price,
                            "description" to description,
                            "photoUrl" to photoUrl,
                            "category" to finalCategory,
                            "weightOrVolume" to weightOrVolume,
                            "ingredients" to ingredients,
                            "calories" to calories,
                            "proteins" to proteins,
                            "fats" to fats,
                            "carbs" to carbs,
                            "cookingTime" to cookingTime,
                            "spiciness" to spiciness,
                            "vegetarian" to vegetarian,
                            "allergens" to allergens,
                            "addOns" to addOns,
                            "addOnsPrice" to addOnsPrice,
                            "availability" to availability,
                            "ratingAverage" to 0.0,
                            "ratingCount" to 0L,
                            "portions" to portions,
                            "costPrice" to costPrice,
                            "discount" to discount,
                            "popular" to popular,
                            "dateAdded" to dateAdded,
                            "owner" to currentUserEmail
                        )

                        db.collection("dishes")
                            .add(newDish)
                            .addOnSuccessListener {
                                isLoading = false
                                onBack()
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                error = "Ошибка сохранения: ${e.message}"
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = MaterialTheme.shapes.large,
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Сохранить блюдо", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}