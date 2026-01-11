package com.example.fastbite

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.TextStyle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.background
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.graphicsLayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onRestaurantProfileClick: (String) -> Unit = {} // Параметр для перехода в профиль ресторана
) {
    val db = Firebase.firestore
    var dishes by remember { mutableStateOf(listOf<Dish>()) }
    var selectedDish by remember { mutableStateOf<Dish?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }

    // Загружаем все блюда из всех аккаунтов
    LaunchedEffect(Unit) {
        db.collection("dishes")
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
                        ratingAverage = it.getDouble("ratingAverage") ?: 0.0,
                        ratingCount = it.getLong("ratingCount") ?: 0L,
                        portions = it.getString("portions") ?: "",
                        costPrice = it.getString("costPrice") ?: "",
                        discount = it.getString("discount") ?: "",
                        popular = it.getBoolean("popular") ?: false,
                        dateAdded = it.getString("dateAdded") ?: "",
                        owner = it.getString("owner") ?: ""
                    )
                }
                loading = false
            }
            .addOnFailureListener {
                loading = false
            }
    }

    // Обработка кнопки "Назад" на устройстве
    BackHandler(enabled = selectedDish != null) {
        if (selectedDish != null) {
            selectedDish = null
        }
    }

    val filteredDishes = if (searchQuery.isBlank()) {
        dishes
    } else {
        dishes.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Все блюда",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Убраны navigationIcon и actions
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Поиск блюда") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredDishes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Блюда не найдены")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredDishes) { dish ->
                        DishCard(
                            dish = dish,
                            onClick = { selectedDish = dish }
                        )
                    }
                }
            }
        }
    }

    // Детали блюда
    selectedDish?.let { dish ->
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { selectedDish = null }
        )

        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { selectedDish = null },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(50))
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    dish.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.weight(1f),
                )
            }

            AsyncImage(
                model = dish.photoUrl,
                contentDescription = dish.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                val discountPercentage = dish.discount?.toDoubleOrNull() ?: 0.0
                val originalPrice = dish.price.toDoubleOrNull() ?: 0.0
                val discountedPrice = if (discountPercentage > 0)
                    originalPrice * (1 - discountPercentage / 100)
                else originalPrice

                if (discountPercentage > 0) {
                    Column {
                        Text(
                            "Цена: ${"%.0f".format(discountedPrice)} тг",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                        Text(
                            "Старая цена: ${"%.0f".format(originalPrice)} тг",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            style = TextStyle(textDecoration = TextDecoration.LineThrough)
                        )
                    }
                } else {
                    Text(
                        "Цена: ${"%.0f".format(originalPrice)} тг",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(16.dp))

                if (dish.description.isNotBlank()) {
                    Column {
                        Text(
                            "Описание",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(dish.description, fontSize = 16.sp)
                        Spacer(Modifier.height(16.dp))
                    }
                }

                Text("Категория: ${dish.category}", fontSize = 16.sp)
                Text("Вес / Объем: ${dish.weightOrVolume}", fontSize = 16.sp)
                Text("Ингредиенты: ${dish.ingredients}", fontSize = 16.sp)
                Text("Калории: ${dish.calories}", fontSize = 16.sp)
                Text("Белки: ${dish.proteins}, Жиры: ${dish.fats}, Углеводы: ${dish.carbs}", fontSize = 16.sp)
                Text("Время приготовления: ${dish.cookingTime}", fontSize = 16.sp)
                Text("Острота: ${dish.spiciness}", fontSize = 16.sp)
                Text("Вегетарианское: ${if (dish.vegetarian) "Да" else "Нет"}", fontSize = 16.sp)
                Text("Доступно: ${if (dish.availability) "Да" else "Нет"}", fontSize = 16.sp)

                // Рейтинг блюда
                val avgRating = dish.ratingAverage ?: 0.0
                val totalRatings = dish.ratingCount ?: 0L

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val fullStars = avgRating.toInt()
                    val hasHalfStar = (avgRating % 1) >= 0.5

                    repeat(fullStars) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                    }
                    if (hasHalfStar) {
                        Icon(Icons.Filled.StarHalf, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                    }
                    repeat(5 - fullStars - if (hasHalfStar) 1 else 0) {
                        Icon(Icons.Outlined.Star, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    }

                    Spacer(Modifier.width(4.dp))
                    Text("%.1f (%d)".format(avgRating, totalRatings), fontSize = 12.sp)
                }
                Spacer(Modifier.height(16.dp))

                // Добавляем информацию о ресторане с кнопкой перехода
                var restaurantName by remember { mutableStateOf("") }
                var restaurantIcon by remember { mutableStateOf("") }

                LaunchedEffect(dish.owner) {
                    if (dish.owner.isNotBlank()) {
                        db.collection("restaurants").document(dish.owner).get()
                            .addOnSuccessListener { doc ->
                                restaurantName = doc.getString("name") ?: "Неизвестный ресторан"
                                restaurantIcon = doc.getString("iconUrl") ?: ""
                            }
                    }
                }

                if (restaurantName.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Переход в профиль ресторана
                                onRestaurantProfileClick(dish.owner)
                            },
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Иконка ресторана
                            if (restaurantIcon.isNotEmpty()) {
                                AsyncImage(
                                    model = restaurantIcon,
                                    contentDescription = "Иконка ресторана",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.Restaurant,
                                    contentDescription = "Иконка ресторана",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color.Gray.copy(alpha = 0.2f), CircleShape)
                                        .padding(12.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "Ресторан",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    restaurantName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Стрелка для перехода
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Перейти в профиль",
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer { rotationZ = 180f },
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun DishCard(
    dish: Dish,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = dish.photoUrl,
                contentDescription = dish.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(8.dp))

            Text(
                dish.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val discountPercentage = dish.discount?.toDoubleOrNull() ?: 0.0
            val originalPrice = dish.price.toDoubleOrNull() ?: 0.0
            val discountedPrice = if (discountPercentage > 0)
                originalPrice * (1 - discountPercentage / 100)
            else originalPrice

            if (discountPercentage > 0) {
                Column {
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
                        style = TextStyle(textDecoration = TextDecoration.LineThrough)
                    )
                }
            } else {
                Text(
                    "${"%.0f".format(originalPrice)} тг",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (dish.category.isNotBlank()) {
                Text(
                    dish.category,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Рейтинг блюда
            val avgRating = dish.ratingAverage ?: 0.0
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val fullStars = avgRating.toInt()
                val hasHalfStar = (avgRating % 1) >= 0.5

                repeat(fullStars) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(14.dp)
                    )
                }
                if (hasHalfStar) {
                    Icon(
                        Icons.Filled.StarHalf,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(14.dp)
                    )
                }
                repeat(5 - fullStars - if (hasHalfStar) 1 else 0) {
                    Icon(
                        Icons.Outlined.Star,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(Modifier.width(4.dp))
                Text(
                    "%.1f".format(avgRating),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}