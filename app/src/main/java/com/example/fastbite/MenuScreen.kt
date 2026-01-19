package com.example.fastbite

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import coil.compose.AsyncImage
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// Enum для сортировки
enum class SortOption(val displayName: String) {
    POPULARITY("По популярности"),
    PRICE_LOW_TO_HIGH("По возрастанию цены"),
    PRICE_HIGH_TO_LOW("По убыванию цены"),
    RATING("По рейтингу"),
    NAME("По названию")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onNavigateToRestaurantProfile: (String) -> Unit = {}
) {
    val db = Firebase.firestore
    var dishes by remember { mutableStateOf(listOf<Dish>()) }
    var selectedDish by remember { mutableStateOf<Dish?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var showFilterDialog by remember { mutableStateOf(false) }

    // Фильтры
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var minPrice by remember { mutableStateOf<Double?>(null) }
    var maxPrice by remember { mutableStateOf<Double?>(null) }
    var isVegetarian by remember { mutableStateOf(false) }
    var minRating by remember { mutableStateOf<Double?>(null) }
    var hasDiscount by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf<SortOption>(SortOption.POPULARITY) }

    // Временные фильтры для диалога
    var tempSelectedCategory by remember { mutableStateOf<String?>(null) }
    var tempMinPrice by remember { mutableStateOf<Double?>(null) }
    var tempMaxPrice by remember { mutableStateOf<Double?>(null) }
    var tempIsVegetarian by remember { mutableStateOf(false) }
    var tempMinRating by remember { mutableStateOf<Double?>(null) }
    var tempHasDiscount by remember { mutableStateOf(false) }
    var tempSortBy by remember { mutableStateOf<SortOption>(SortOption.POPULARITY) }

    // Загружаем все блюда из всех аккаунтов
    LaunchedEffect(Unit) {
        db.collection("dishes")
            .whereEqualTo("availability", true)
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

    BackHandler(enabled = selectedDish != null) {
        if (selectedDish != null) {
            selectedDish = null
        }
    }

    // Получаем уникальные категории для фильтра
    val categories = dishes.map { it.category }.distinct().filter { it.isNotBlank() }

    // Функция фильтрации блюд
    val filteredDishes = dishes
        .filter { dish ->
            // Поиск по тексту
            val matchesSearch = searchQuery.isBlank() ||
                    dish.name.contains(searchQuery, ignoreCase = true) ||
                    dish.description.contains(searchQuery, ignoreCase = true) ||
                    dish.category.contains(searchQuery, ignoreCase = true) ||
                    dish.ingredients.contains(searchQuery, ignoreCase = true)

            // Фильтр по категории
            val matchesCategory = selectedCategory?.let { dish.category == it } ?: true

            // Фильтр по цене
            val dishPrice = dish.price.toDoubleOrNull() ?: 0.0
            val discountPercentage = dish.discount?.toDoubleOrNull() ?: 0.0
            val finalPrice = if (discountPercentage > 0)
                dishPrice * (1 - discountPercentage / 100)
            else dishPrice

            val matchesMinPrice = minPrice?.let { finalPrice >= it } ?: true
            val matchesMaxPrice = maxPrice?.let { finalPrice <= it } ?: true

            // Фильтр по вегетарианскому
            val matchesVegetarian = !isVegetarian || dish.vegetarian

            // Фильтр по рейтингу
            val matchesRating = minRating?.let { (dish.ratingAverage ?: 0.0) >= it } ?: true

            // Фильтр по скидке
            val matchesDiscount = !hasDiscount || (dish.discount?.toDoubleOrNull() ?: 0.0) > 0

            matchesSearch && matchesCategory && matchesMinPrice && matchesMaxPrice &&
                    matchesVegetarian && matchesRating && matchesDiscount
        }
        .sortedWith(compareBy {
            when (sortBy) {
                SortOption.PRICE_LOW_TO_HIGH -> {
                    val price = it.price.toDoubleOrNull() ?: 0.0
                    val discount = it.discount?.toDoubleOrNull() ?: 0.0
                    if (discount > 0) price * (1 - discount / 100) else price
                }
                SortOption.PRICE_HIGH_TO_LOW -> {
                    val price = it.price.toDoubleOrNull() ?: 0.0
                    val discount = it.discount?.toDoubleOrNull() ?: 0.0
                    -(if (discount > 0) price * (1 - discount / 100) else price)
                }
                SortOption.RATING -> -(it.ratingAverage ?: 0.0)
                SortOption.NAME -> it.name
                SortOption.POPULARITY -> if (it.popular) 0 else 1
            }
        })

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
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Поле поиска
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Поиск блюда") },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Очистить поиск")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Отображение активных фильтров
            val hasActiveFilters = selectedCategory != null || minPrice != null || maxPrice != null ||
                    isVegetarian || minRating != null || hasDiscount

            if (hasActiveFilters) {
                Column(
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "Активные фильтры:",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Используем Column для чипов фильтров
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (selectedCategory != null) {
                            Row {
                                FilterChip(
                                    selected = true,
                                    onClick = { selectedCategory = null },
                                    label = { Text(selectedCategory!!) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Удалить фильтр",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }

                        if (isVegetarian) {
                            Row {
                                FilterChip(
                                    selected = true,
                                    onClick = { isVegetarian = false },
                                    label = { Text("Вегетарианское") },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Удалить фильтр",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }

                        if (hasDiscount) {
                            Row {
                                FilterChip(
                                    selected = true,
                                    onClick = { hasDiscount = false },
                                    label = { Text("Со скидкой") },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Удалить фильтр",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }

                        if (minRating != null) {
                            Row {
                                FilterChip(
                                    selected = true,
                                    onClick = { minRating = null },
                                    label = { Text("★ ${"%.1f".format(minRating)}+") },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Удалить фильтр",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }

                        if (minPrice != null || maxPrice != null) {
                            Row {
                                FilterChip(
                                    selected = true,
                                    onClick = {
                                        minPrice = null
                                        maxPrice = null
                                    },
                                    label = {
                                        Text(
                                            when {
                                                minPrice != null && maxPrice != null ->
                                                    "${"%.0f".format(minPrice)}-${"%.0f".format(maxPrice)} тг"
                                                minPrice != null -> "от ${"%.0f".format(minPrice)} тг"
                                                else -> "до ${"%.0f".format(maxPrice)} тг"
                                            }
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Удалить фильтр",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Кнопка очистки всех фильтров
                    TextButton(
                        onClick = {
                            selectedCategory = null
                            minPrice = null
                            maxPrice = null
                            isVegetarian = false
                            minRating = null
                            hasDiscount = false
                            sortBy = SortOption.POPULARITY
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Очистить все фильтры")
                    }
                }
            }

            // Информация о количестве найденных блюд и кнопка фильтров
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Найдено: ${filteredDishes.size} блюд",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                // Кнопка фильтров
                OutlinedButton(
                    onClick = {
                        // Синхронизируем текущие значения с временными
                        tempSelectedCategory = selectedCategory
                        tempMinPrice = minPrice
                        tempMaxPrice = maxPrice
                        tempIsVegetarian = isVegetarian
                        tempMinRating = minRating
                        tempHasDiscount = hasDiscount
                        tempSortBy = sortBy
                        showFilterDialog = true
                    },
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        Icons.Outlined.FilterList,
                        contentDescription = "Фильтры",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Фильтры")
                }
            }

            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Загрузка меню...")
                    }
                }
            } else if (filteredDishes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = "Ничего не найдено",
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Text(
                            "Блюда не найдены",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Попробуйте изменить параметры поиска или фильтры",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        if (hasActiveFilters) {
                            Button(
                                onClick = {
                                    selectedCategory = null
                                    minPrice = null
                                    maxPrice = null
                                    isVegetarian = false
                                    minRating = null
                                    hasDiscount = false
                                    sortBy = SortOption.POPULARITY
                                    searchQuery = ""
                                }
                            ) {
                                Text("Сбросить все фильтры")
                            }
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredDishes) { dish ->
                        MenuDishCard(dish = dish, onClick = { selectedDish = dish })
                    }
                }
            }
        }
    }

    // Диалог фильтров
    if (showFilterDialog) {
        FilterDialog(
            categories = categories,
            selectedCategory = tempSelectedCategory,
            onCategorySelect = { tempSelectedCategory = it },
            minPrice = tempMinPrice,
            onMinPriceChange = { tempMinPrice = it },
            maxPrice = tempMaxPrice,
            onMaxPriceChange = { tempMaxPrice = it },
            isVegetarian = tempIsVegetarian,
            onVegetarianChange = { tempIsVegetarian = it },
            minRating = tempMinRating,
            onMinRatingChange = { tempMinRating = it },
            hasDiscount = tempHasDiscount,
            onHasDiscountChange = { tempHasDiscount = it },
            sortBy = tempSortBy,
            onSortChange = { tempSortBy = it },
            onDismiss = {
                showFilterDialog = false
            },
            onApply = {
                // Применяем временные фильтры к основным
                selectedCategory = tempSelectedCategory
                minPrice = tempMinPrice
                maxPrice = tempMaxPrice
                isVegetarian = tempIsVegetarian
                minRating = tempMinRating
                hasDiscount = tempHasDiscount
                sortBy = tempSortBy
                showFilterDialog = false
            },
            onClearAll = {
                tempSelectedCategory = null
                tempMinPrice = null
                tempMaxPrice = null
                tempIsVegetarian = false
                tempMinRating = null
                tempHasDiscount = false
                tempSortBy = SortOption.POPULARITY
            }
        )
    }

    // Детали блюда
    selectedDish?.let { dish ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { selectedDish = null }
        )

        Column(
            modifier = Modifier
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

                if (dish.category.isNotBlank()) {
                    Text("Категория: ${dish.category}", fontSize = 16.sp)
                }

                if (dish.weightOrVolume.isNotBlank()) {
                    Text("Вес / Объем: ${dish.weightOrVolume}", fontSize = 16.sp)
                }

                if (dish.ingredients.isNotBlank()) {
                    Text("Ингредиенты: ${dish.ingredients}", fontSize = 16.sp)
                }

                if (dish.calories.isNotBlank()) {
                    Text("Калории: ${dish.calories}", fontSize = 16.sp)
                }

                if (dish.proteins.isNotBlank() || dish.fats.isNotBlank() || dish.carbs.isNotBlank()) {
                    Text("Белки: ${dish.proteins}, Жиры: ${dish.fats}, Углеводы: ${dish.carbs}", fontSize = 16.sp)
                }

                if (dish.cookingTime.isNotBlank()) {
                    Text("Время приготовления: ${dish.cookingTime}", fontSize = 16.sp)
                }

                if (dish.spiciness.isNotBlank()) {
                    Text("Острота: ${dish.spiciness}", fontSize = 16.sp)
                }

                Text("Вегетарианское: ${if (dish.vegetarian) "Да" else "Нет"}", fontSize = 16.sp)

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
                var restaurantRating by remember { mutableStateOf(0.0) }
                var restaurantRatingCount by remember { mutableStateOf(0L) }

                LaunchedEffect(dish.owner) {
                    if (dish.owner.isNotBlank()) {
                        db.collection("restaurants").document(dish.owner).get()
                            .addOnSuccessListener { doc ->
                                restaurantName = doc.getString("name") ?: "Неизвестный ресторан"
                                restaurantIcon = doc.getString("iconUrl") ?: ""
                                restaurantRating = doc.getDouble("rating") ?: 0.0
                                restaurantRatingCount = doc.getLong("ratingCount") ?: 0L
                            }
                    }
                }

                if (restaurantName.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onNavigateToRestaurantProfile(dish.owner)
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
                                    restaurantName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val fullStarsRestaurant = restaurantRating.toInt()
                                    val hasHalfStarRestaurant = restaurantRating % 1 >= 0.5

                                    repeat(fullStarsRestaurant) {
                                        Icon(
                                            Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFFC107),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    if (hasHalfStarRestaurant) {
                                        Icon(
                                            Icons.Filled.StarHalf,
                                            contentDescription = null,
                                            tint = Color(0xFFFFC107),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    repeat(5 - fullStarsRestaurant - if (hasHalfStarRestaurant) 1 else 0) {
                                        Icon(
                                            Icons.Outlined.Star,
                                            contentDescription = null,
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "%.1f (%d)".format(restaurantRating, restaurantRatingCount),
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "Перейти в профиль",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                Spacer(Modifier.height(24.dp))
                CartControlsCompact(
                    dish = dish,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// Диалог фильтров
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelect: (String?) -> Unit,
    minPrice: Double?,
    onMinPriceChange: (Double?) -> Unit,
    maxPrice: Double?,
    onMaxPriceChange: (Double?) -> Unit,
    isVegetarian: Boolean,
    onVegetarianChange: (Boolean) -> Unit,
    minRating: Double?,
    onMinRatingChange: (Double?) -> Unit,
    hasDiscount: Boolean,
    onHasDiscountChange: (Boolean) -> Unit,
    sortBy: SortOption,
    onSortChange: (SortOption) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    onClearAll: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Фильтрация и сортировка", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick = onClearAll,
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text("Очистить фильтр")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Сортировка
                Text("Сортировка", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Column(
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    SortOption.values().forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSortChange(option) }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = sortBy == option,
                                onClick = { onSortChange(option) }
                            )
                            Text(
                                option.displayName,
                                modifier = Modifier.padding(start = 8.dp),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Категория - список как в SellerMenuScreen
                Text("Категория", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Column(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // "Все категории"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategorySelect(null) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCategory == null,
                            onClick = { onCategorySelect(null) }
                        )
                        Text(
                            "Все категории",
                            modifier = Modifier.padding(start = 8.dp),
                            fontSize = 14.sp
                        )
                    }

                    // Список категорий
                    categories.forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCategorySelect(category) }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCategory == category,
                                onClick = { onCategorySelect(category) }
                            )
                            Text(
                                category,
                                modifier = Modifier.padding(start = 8.dp),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Цена
                Text("Цена, тг", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    OutlinedTextField(
                        value = minPrice?.toString() ?: "",
                        onValueChange = {
                            onMinPriceChange(it.toDoubleOrNull())
                        },
                        label = { Text("От", fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 14.sp)
                    )
                    OutlinedTextField(
                        value = maxPrice?.toString() ?: "",
                        onValueChange = {
                            onMaxPriceChange(it.toDoubleOrNull())
                        },
                        label = { Text("До", fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 14.sp)
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Дополнительные фильтры
                Text("Дополнительно", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Column(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isVegetarian,
                            onCheckedChange = onVegetarianChange
                        )
                        Text("Только вегетарианские",
                            modifier = Modifier.padding(start = 8.dp),
                            fontSize = 14.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = hasDiscount,
                            onCheckedChange = onHasDiscountChange
                        )
                        Text("Только со скидкой",
                            modifier = Modifier.padding(start = 8.dp),
                            fontSize = 14.sp)
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Рейтинг - список как категории
                Text("Минимальный рейтинг", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                val ratingOptions = listOf(4.0, 3.5, 3.0, 2.5, 2.0, 1.0)
                Column(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // "Любой"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMinRatingChange(null) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = minRating == null,
                            onClick = { onMinRatingChange(null) }
                        )
                        Text(
                            "Любой",
                            modifier = Modifier.padding(start = 8.dp),
                            fontSize = 14.sp
                        )
                    }

                    // Список рейтингов
                    ratingOptions.forEach { rating ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMinRatingChange(rating) }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = minRating == rating,
                                onClick = { onMinRatingChange(rating) }
                            )
                            Text(
                                "★ $rating+",
                                modifier = Modifier.padding(start = 8.dp),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Применить фильтры", fontSize = 16.sp)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Отмена", fontSize = 16.sp)
            }
        }
    )
}
// Упрощенная версия чипов категорий
@Composable
fun SimpleCategoryChips(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelect: (String?) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "Все категории"
        AssistChip(
            onClick = { onCategorySelect(null) },
            label = { Text("Все категории", fontSize = 14.sp) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (selectedCategory == null)
                    MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
        )

        // Категории - простой список
        val chunkedCategories = remember(categories) { categories.chunked(3) }

        chunkedCategories.forEachIndexed { index, chunk ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (category in chunk) {
                    AssistChip(
                        onClick = { onCategorySelect(category) },
                        label = {
                            Text(
                                category,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 14.sp
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (selectedCategory == category)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                // Заполняем оставшееся место пустыми чипами для выравнивания
                for (i in chunk.size until 3) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

// Упрощенная версия чипов рейтингов
@Composable
fun SimpleRatingChips(
    ratingOptions: List<Double>,
    minRating: Double?,
    onMinRatingChange: (Double?) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "Любой"
        AssistChip(
            onClick = { onMinRatingChange(null) },
            label = { Text("Любой", fontSize = 14.sp) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (minRating == null) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
        )

        // Рейтинги - простой список
        val chunkedRatings = remember(ratingOptions) { ratingOptions.chunked(3) }

        chunkedRatings.forEachIndexed { index, chunk ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (rating in chunk) {
                    AssistChip(
                        onClick = { onMinRatingChange(rating) },
                        label = { Text("★ $rating+", fontSize = 14.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (minRating == rating)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                // Заполняем оставшееся место пустыми чипами для выравнивания
                for (i in chunk.size until 3) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

// Карточка блюда для меню
@Composable
fun MenuDishCard(
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
        Box {
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

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                CartButtonCompact(dish = dish)
            }
        }
    }
}

// Кнопка корзины для карточки
@Composable
fun CartButtonCompact(
    dish: Dish,
    modifier: Modifier = Modifier
) {
    val quantity = observeCartItemQuantity(dish.id)

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = if (quantity > 0) 4.dp else 2.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clickable { CartManager.addToCart(dish) }
        ) {
            if (quantity > 0) {
                Badge(
                    containerColor = Color.Red,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        quantity.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Icon(
                if (quantity > 0) Icons.Default.ShoppingCart else Icons.Default.AddShoppingCart,
                contentDescription = "Добавить в корзину",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun CartControlsCompact(
    dish: Dish,
    modifier: Modifier = Modifier
) {
    val quantity = observeCartItemQuantity(dish.id)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        if (quantity == 0) {
            Button(
                onClick = { CartManager.addToCart(dish) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Добавить в корзину")
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        quantity.toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("в корзине", fontSize = 12.sp, color = Color.Gray)
                }

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
}