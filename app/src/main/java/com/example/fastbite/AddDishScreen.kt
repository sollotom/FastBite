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

// Строки для экрана добавления блюда
object AddDishStrings {
    var currentLanguage = Strings.currentLanguage

    val addDish: String get() = if (currentLanguage.value == Language.KAZAKH) "Тағам қосу" else "Добавить блюдо"
    val back: String get() = if (currentLanguage.value == Language.KAZAKH) "Артқа" else "Назад"
    val basicInfo: String get() = if (currentLanguage.value == Language.KAZAKH) "Негізгі ақпарат" else "Основная информация"
    val dishName: String get() = if (currentLanguage.value == Language.KAZAKH) "Тағам атауы *" else "Название блюда *"
    val price: String get() = if (currentLanguage.value == Language.KAZAKH) "Бағасы *" else "Цена *"
    val category: String get() = if (currentLanguage.value == Language.KAZAKH) "Санат" else "Категория"
    val description: String get() = if (currentLanguage.value == Language.KAZAKH) "Сипаттама" else "Описание"
    val photoUrl: String get() = if (currentLanguage.value == Language.KAZAKH) "Фото сілтемесі" else "Ссылка на фото"

    val characteristics: String get() = if (currentLanguage.value == Language.KAZAKH) "Сипаттамалары" else "Характеристики"
    val weightOrVolume: String get() = if (currentLanguage.value == Language.KAZAKH) "Салмағы / көлемі" else "Вес / объём"
    val ingredients: String get() = if (currentLanguage.value == Language.KAZAKH) "Құрамы" else "Ингредиенты"
    val calories: String get() = if (currentLanguage.value == Language.KAZAKH) "Калория" else "Калории"
    val cookingTime: String get() = if (currentLanguage.value == Language.KAZAKH) "Уақыт (мин)" else "Время (мин)"
    val proteins: String get() = if (currentLanguage.value == Language.KAZAKH) "Ақуыздар" else "Белки"
    val fats: String get() = if (currentLanguage.value == Language.KAZAKH) "Майлар" else "Жиры"
    val carbs: String get() = if (currentLanguage.value == Language.KAZAKH) "Көмірсулар" else "Углеводы"
    val spiciness: String get() = if (currentLanguage.value == Language.KAZAKH) "Ащылығы (1-5)" else "Острота (1-5)"
    val allergens: String get() = if (currentLanguage.value == Language.KAZAKH) "Аллергендер" else "Аллергены"

    val additional: String get() = if (currentLanguage.value == Language.KAZAKH) "Қосымша" else "Дополнительно"
    val addOns: String get() = if (currentLanguage.value == Language.KAZAKH) "Мүмкін қоспалар" else "Возможные добавки"
    val addOnsPrice: String get() = if (currentLanguage.value == Language.KAZAKH) "Қоспалар бағасы" else "Цена добавок"
    val portions: String get() = if (currentLanguage.value == Language.KAZAKH) "Порция саны" else "Количество порций"
    val costPrice: String get() = if (currentLanguage.value == Language.KAZAKH) "Өзіндік құны" else "Себестоимость"
    val discount: String get() = if (currentLanguage.value == Language.KAZAKH) "Жеңілдік (%)" else "Скидка (%)"
    val vegetarian: String get() = if (currentLanguage.value == Language.KAZAKH) "Вегетариандық" else "Вегетарианское"
    val popular: String get() = if (currentLanguage.value == Language.KAZAKH) "Танымал" else "Популярное"
    val available: String get() = if (currentLanguage.value == Language.KAZAKH) "Қолжетімді" else "Доступно"

    val saveDish: String get() = if (currentLanguage.value == Language.KAZAKH) "Тағамды сақтау" else "Сохранить блюдо"
    val fillRequired: String get() = if (currentLanguage.value == Language.KAZAKH) "Міндетті өрістерді толтырыңыз: атауы және бағасы" else "Заполните обязательные поля: название и цена"
    val saveError: String get() = if (currentLanguage.value == Language.KAZAKH) "Сақтау қатесі: " else "Ошибка сохранения: "
    val categoryNotFound: String get() = if (currentLanguage.value == Language.KAZAKH) "Мұндай санат жоқ, «Басқа» ретінде сақталады" else "Такой категории нет, будет сохранено как «Другое»"

    // Категории на двух языках
    val categoriesRu = listOf(
        "Супы", "Салаты", "Горячие блюда", "Закуски",
        "Пицца / Лепёшки / Хлеб", "Бургеры и сэндвичи",
        "Десерты", "Напитки", "Завтраки / Бранч",
        "Вегетарианские / Веганские блюда", "Суши и роллы",
        "Блюда на гриле / Барбекю", "Детское меню",
        "Комбо / Наборы", "Другое"
    )

    val categoriesKz = listOf(
        "Сорпалар", "Салаттар", "Ыстық тағамдар", "Тіскебасарлар",
        "Пицца / Нан / Торт", "Бургерлер және сэндвичтер",
        "Десерттер", "Сусындар", "Таңғы ас / Бранч",
        "Вегетариандық / Вегандық тағамдар", "Суши және роллдар",
        "Гриль / Барбекю тағамдары", "Балалар мәзірі",
        "Комбо / Жиынтықтар", "Басқа"
    )

    fun getCategories(): List<String> = if (currentLanguage.value == Language.KAZAKH) categoriesKz else categoriesRu
    val other: String get() = if (currentLanguage.value == Language.KAZAKH) "Басқа" else "Другое"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDishScreen(
    currentUserEmail: String,
    onBack: () -> Unit
) {
    val db = Firebase.firestore
    val dateAdded = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val isKazakh = Strings.getLanguage() == Language.KAZAKH

    val categories = if (isKazakh) AddDishStrings.categoriesKz else AddDishStrings.categoriesRu

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

    val filteredCategories = remember(category, isKazakh) {
        if (!categorySelected && category.isNotBlank())
            categories.filter { it.contains(category, ignoreCase = true) }
        else emptyList()
    }

    val categoryValid = categories.contains(category)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(AddDishStrings.addDish, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = AddDishStrings.back)
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
                    AddDishStrings.basicInfo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(AddDishStrings.dishName) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    isError = name.isBlank()
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text(AddDishStrings.price) },
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
                        label = { Text(AddDishStrings.category) },
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
                            text = AddDishStrings.categoryNotFound,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(AddDishStrings.description) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = MaterialTheme.shapes.medium
                )

                OutlinedTextField(
                    value = photoUrl,
                    onValueChange = { photoUrl = it },
                    label = { Text(AddDishStrings.photoUrl) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
            }

            item {
                Text(
                    AddDishStrings.characteristics,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                OutlinedTextField(
                    value = weightOrVolume,
                    onValueChange = { weightOrVolume = it },
                    label = { Text(AddDishStrings.weightOrVolume) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                OutlinedTextField(
                    value = ingredients,
                    onValueChange = { ingredients = it },
                    label = { Text(AddDishStrings.ingredients) },
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
                        label = { Text(AddDishStrings.calories) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = cookingTime,
                        onValueChange = { cookingTime = it },
                        label = { Text(AddDishStrings.cookingTime) },
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
                        label = { Text(AddDishStrings.proteins) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = fats,
                        onValueChange = { fats = it },
                        label = { Text(AddDishStrings.fats) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text(AddDishStrings.carbs) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                }

                OutlinedTextField(
                    value = spiciness,
                    onValueChange = { spiciness = it },
                    label = { Text(AddDishStrings.spiciness) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                OutlinedTextField(
                    value = allergens,
                    onValueChange = { allergens = it },
                    label = { Text(AddDishStrings.allergens) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
            }

            item {
                Text(
                    AddDishStrings.additional,
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
                        label = { Text(AddDishStrings.addOns) },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = addOnsPrice,
                        onValueChange = { addOnsPrice = it },
                        label = { Text(AddDishStrings.addOnsPrice) },
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
                        label = { Text(AddDishStrings.portions) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = costPrice,
                        onValueChange = { costPrice = it },
                        label = { Text(AddDishStrings.costPrice) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                }

                OutlinedTextField(
                    value = discount,
                    onValueChange = { discount = it },
                    label = { Text(AddDishStrings.discount) },
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
                    Text(AddDishStrings.vegetarian, modifier = Modifier.padding(start = 8.dp))

                    Spacer(modifier = Modifier.width(24.dp))

                    Checkbox(
                        checked = popular,
                        onCheckedChange = { popular = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFFF9800)
                        )
                    )
                    Text(AddDishStrings.popular, modifier = Modifier.padding(start = 8.dp))

                    Spacer(modifier = Modifier.width(24.dp))

                    Checkbox(
                        checked = availability,
                        onCheckedChange = { availability = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF2196F3)
                        )
                    )
                    Text(AddDishStrings.available, modifier = Modifier.padding(start = 8.dp))
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
                            error = AddDishStrings.fillRequired
                            return@Button
                        }

                        isLoading = true
                        error = ""

                        val finalCategory = if (categoryValid) category else AddDishStrings.other

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
                                error = AddDishStrings.saveError + e.message
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
                        Text(AddDishStrings.saveDish, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}