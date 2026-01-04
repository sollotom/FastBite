package com.example.fastbite

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    // Подсказки категорий фильтруются по введенному тексту
    val filteredCategories = remember(category) {
        if (!categorySelected && category.isNotBlank())
            categories.filter { it.contains(category, ignoreCase = true) }
        else emptyList()
    }

    // Проверка: введённая категория есть в списке
    val categoryValid = categories.contains(category)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавить блюдо") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Назад") } }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    if (name.isBlank() || price.isBlank()) return@Button

                    val finalCategory = if (categoryValid) category else "Другое"

                    val newDish = Dish(
                        name = name,
                        price = price,
                        description = description,
                        photoUrl = photoUrl,
                        category = finalCategory,
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
                        ratingAverage = 0.0,
                        ratingCount = 0L,
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
                        .addOnFailureListener { /* Можно добавить Toast */ }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) { Text("Сохранить") }
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

                // --- Категория с автоподсказками ---
                Column {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {
                            category = it
                            categorySelected = false
                        },
                        label = { Text("Категория") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Показываем подсказки только если они есть
                    filteredCategories.forEach { cat ->
                        Text(
                            text = cat,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    category = cat
                                    categorySelected = true
                                }
                                .padding(vertical = 6.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Если подсказок нет, текст не пустой, и категория не выбрана корректно — показываем предупреждение
                    if (category.isNotBlank() && filteredCategories.isEmpty() && !categoryValid) {
                        Text(
                            text = "Такой категории нет, выберите «Другое»",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Остальные поля
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

                OutlinedTextField(weightOrVolume, { weightOrVolume = it }, label = { Text("Вес / объём") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(ingredients, { ingredients = it }, label = { Text("Ингредиенты") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(calories, { calories = it }, label = { Text("Калорийность") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(proteins, { proteins = it }, label = { Text("Белки") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(fats, { fats = it }, label = { Text("Жиры") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(carbs, { carbs = it }, label = { Text("Углеводы") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(cookingTime, { cookingTime = it }, label = { Text("Время приготовления") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(spiciness, { spiciness = it }, label = { Text("Острота") }, modifier = Modifier.fillMaxWidth())

                Row {
                    Checkbox(checked = vegetarian, onCheckedChange = { vegetarian = it })
                    Text("Вегетарианское", modifier = Modifier.padding(start = 4.dp))
                }

                OutlinedTextField(allergens, { allergens = it }, label = { Text("Аллергены") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(addOns, { addOns = it }, label = { Text("Возможные добавки") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(addOnsPrice, { addOnsPrice = it }, label = { Text("Цена добавок") }, modifier = Modifier.fillMaxWidth())

                Row {
                    Checkbox(checked = availability, onCheckedChange = { availability = it })
                    Text("Доступно", modifier = Modifier.padding(start = 4.dp))
                }

                OutlinedTextField(portions, { portions = it }, label = { Text("Количество порций") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(costPrice, { costPrice = it }, label = { Text("Себестоимость") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(discount, { discount = it }, label = { Text("Скидка / акция") }, modifier = Modifier.fillMaxWidth())

                Row {
                    Checkbox(checked = popular, onCheckedChange = { popular = it })
                    Text("Популярное блюдо", modifier = Modifier.padding(start = 4.dp))
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
