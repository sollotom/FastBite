package com.example.fastbite

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import coil.compose.AsyncImage
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kotlin.math.roundToInt

// Enum для сортировки с иконками
enum class SortOption(val displayName: String, val icon: ImageVector) {
    POPULARITY("По популярности", Icons.Default.TrendingUp),
    PRICE_LOW_TO_HIGH("По возрастанию цены", Icons.Default.ArrowUpward),
    PRICE_HIGH_TO_LOW("По убыванию цены", Icons.Default.ArrowDownward),
    RATING("По рейтингу", Icons.Default.Star),
    NAME("По названию", Icons.Default.Sort)
}

// Кастомный компонент для обертывания элементов
@Composable
fun WrapColumn(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 0.dp,
    verticalSpacing: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val horizontalSpacingPx = horizontalSpacing.roundToPx()
        val verticalSpacingPx = verticalSpacing.roundToPx()

        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }

        val width = constraints.maxWidth
        var currentX = 0
        var currentY = 0
        var rowHeight = 0
        val positions = mutableListOf<Pair<Int, Int>>()

        placeables.forEach { placeable ->
            if (currentX + placeable.width > width && currentX > 0) {
                currentX = 0
                currentY += rowHeight + verticalSpacingPx
                rowHeight = 0
            }

            positions.add(Pair(currentX, currentY))

            currentX += placeable.width + horizontalSpacingPx
            rowHeight = maxOf(rowHeight, placeable.height)
        }

        val totalHeight = currentY + rowHeight

        layout(width, totalHeight) {
            positions.forEachIndexed { index, (x, y) ->
                placeables[index].place(x, y)
            }
        }
    }
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
    var showBottomSheet by remember { mutableStateOf(false) }
    var activeFilterCount by remember { mutableStateOf(0) }
    var viewMode by remember { mutableStateOf(ViewMode.GRID) }

    // Фильтры
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var priceRange by remember { mutableStateOf<ClosedFloatingPointRange<Float>?>(null) }
    var isVegetarian by remember { mutableStateOf(false) }
    var minRating by remember { mutableStateOf<Int?>(null) }
    var hasDiscount by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf<SortOption>(SortOption.POPULARITY) }

    // Загружаем блюда
    LaunchedEffect(Unit) {
        try {
            val result = db.collection("dishes")
                .whereEqualTo("availability", true)
                .get()
                .await()

            dishes = result.documents.map { doc ->
                Dish(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    price = doc.getString("price") ?: "0",
                    description = doc.getString("description") ?: "",
                    photoUrl = doc.getString("photoUrl") ?: "",
                    category = doc.getString("category") ?: "",
                    weightOrVolume = doc.getString("weightOrVolume") ?: "",
                    ingredients = doc.getString("ingredients") ?: "",
                    calories = doc.getString("calories") ?: "",
                    proteins = doc.getString("proteins") ?: "",
                    fats = doc.getString("fats") ?: "",
                    carbs = doc.getString("carbs") ?: "",
                    cookingTime = doc.getString("cookingTime") ?: "",
                    spiciness = doc.getString("spiciness") ?: "",
                    vegetarian = doc.getBoolean("vegetarian") ?: false,
                    allergens = doc.getString("allergens") ?: "",
                    addOns = doc.getString("addOns") ?: "",
                    addOnsPrice = doc.getString("addOnsPrice") ?: "",
                    availability = doc.getBoolean("availability") ?: true,
                    ratingAverage = doc.getDouble("ratingAverage") ?: 0.0,
                    ratingCount = doc.getLong("ratingCount") ?: 0L,
                    portions = doc.getString("portions") ?: "",
                    costPrice = doc.getString("costPrice") ?: "",
                    discount = doc.getString("discount") ?: "",
                    popular = doc.getBoolean("popular") ?: false,
                    dateAdded = doc.getString("dateAdded") ?: "",
                    owner = doc.getString("owner") ?: ""
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            loading = false
        }
    }

    // Подсчет активных фильтров
    LaunchedEffect(selectedCategory, priceRange, isVegetarian, minRating, hasDiscount) {
        activeFilterCount = listOf(
            selectedCategory != null,
            priceRange != null,
            isVegetarian,
            minRating != null,
            hasDiscount
        ).count { it }
    }

    BackHandler(enabled = selectedDish != null) {
        selectedDish = null
    }

    val categories = dishes.map { it.category }.distinct().filter { it.isNotBlank() }

    // Фильтрация и сортировка блюд
    val filteredDishes = remember(dishes, searchQuery, selectedCategory, priceRange, isVegetarian, minRating, hasDiscount, sortBy) {
        dishes
            .filter { dish ->
                val matchesSearch = searchQuery.isBlank() ||
                        dish.name.contains(searchQuery, ignoreCase = true) ||
                        dish.description.contains(searchQuery, ignoreCase = true) ||
                        dish.category.contains(searchQuery, ignoreCase = true)

                val matchesCategory = selectedCategory?.let { dish.category == it } ?: true

                val dishPrice = dish.price.toDoubleOrNull() ?: 0.0
                val discountPercentage = dish.discount?.toDoubleOrNull() ?: 0.0
                val finalPrice = if (discountPercentage > 0)
                    dishPrice * (1 - discountPercentage / 100)
                else dishPrice

                val matchesPrice = priceRange?.let { finalPrice in it } ?: true
                val matchesVegetarian = !isVegetarian || dish.vegetarian
                val matchesRating = minRating?.let { (dish.ratingAverage ?: 0.0).roundToInt() >= it } ?: true
                val matchesDiscount = !hasDiscount || (dish.discount?.toDoubleOrNull() ?: 0.0) > 0

                matchesSearch && matchesCategory && matchesPrice && matchesVegetarian && matchesRating && matchesDiscount
            }
            .sortedWith(compareBy { dish ->
                when (sortBy) {
                    SortOption.PRICE_LOW_TO_HIGH -> {
                        val price = dish.price.toDoubleOrNull() ?: 0.0
                        val discount = dish.discount?.toDoubleOrNull() ?: 0.0
                        if (discount > 0) price * (1 - discount / 100) else price
                    }
                    SortOption.PRICE_HIGH_TO_LOW -> {
                        val price = dish.price.toDoubleOrNull() ?: 0.0
                        val discount = dish.discount?.toDoubleOrNull() ?: 0.0
                        -(if (discount > 0) price * (1 - discount / 100) else price)
                    }
                    SortOption.RATING -> -(dish.ratingAverage ?: 0.0)
                    SortOption.NAME -> dish.name
                    SortOption.POPULARITY -> if (dish.popular) 0 else 1
                }
            })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Меню",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (filteredDishes.isNotEmpty()) {
                            Text(
                                "${filteredDishes.size} ${getWordForm(filteredDishes.size, "блюдо", "блюда", "блюд")}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                },
                actions = {
                    // Переключатель режима отображения
                    IconButton(onClick = { viewMode = if (viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID }) {
                        Icon(
                            if (viewMode == ViewMode.GRID) Icons.Outlined.ViewList else Icons.Outlined.GridView,
                            contentDescription = "Сменить режим"
                        )
                    }

                    Box {
                        IconButton(onClick = { showBottomSheet = true }) {
                            Icon(
                                Icons.Outlined.FilterList,
                                contentDescription = "Фильтры"
                            )
                        }
                        if (activeFilterCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-4).dp, y = 4.dp)
                                    .size(18.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    )
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    activeFilterCount.toString(),
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Поле поиска с улучшенным дизайном
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 2.dp,
                    tonalElevation = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Поиск блюд...", color = Color.Gray) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            AnimatedVisibility(
                                visible = searchQuery.isNotBlank(),
                                enter = fadeIn() + scaleIn(),
                                exit = fadeOut() + scaleOut()
                            ) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Очистить",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Активные фильтры
                AnimatedContent(
                    targetState = activeFilterCount > 0,
                    transitionSpec = {
                        fadeIn() + slideInVertically() togetherWith
                                fadeOut() + slideOutVertically()
                    }
                ) { hasFilters ->
                    if (hasFilters) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                        ) {
                            Text(
                                "Активные фильтры",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            WrapColumn(
                                horizontalSpacing = 8.dp,
                                verticalSpacing = 8.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                selectedCategory?.let { category ->
                                    FilterChip(
                                        selected = true,
                                        onClick = { selectedCategory = null },
                                        label = { Text(category, fontSize = 13.sp) },
                                        trailingIcon = {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Удалить",
                                                modifier = Modifier.size(14.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            selectedLabelColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }

                                if (isVegetarian) {
                                    FilterChip(
                                        selected = true,
                                        onClick = { isVegetarian = false },
                                        label = { Text("🌱 Вегетарианское", fontSize = 13.sp) },
                                        trailingIcon = {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Удалить",
                                                modifier = Modifier.size(14.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF4CAF50).copy(alpha = 0.1f),
                                            selectedLabelColor = Color(0xFF4CAF50)
                                        )
                                    )
                                }

                                if (hasDiscount) {
                                    FilterChip(
                                        selected = true,
                                        onClick = { hasDiscount = false },
                                        label = { Text("🏷️ Со скидкой", fontSize = 13.sp) },
                                        trailingIcon = {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Удалить",
                                                modifier = Modifier.size(14.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFFF9800).copy(alpha = 0.1f),
                                            selectedLabelColor = Color(0xFFFF9800)
                                        )
                                    )
                                }

                                minRating?.let { rating ->
                                    FilterChip(
                                        selected = true,
                                        onClick = { minRating = null },
                                        label = { Text("⭐ $rating+", fontSize = 13.sp) },
                                        trailingIcon = {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Удалить",
                                                modifier = Modifier.size(14.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFFFC107).copy(alpha = 0.1f),
                                            selectedLabelColor = Color(0xFFFFC107)
                                        )
                                    )
                                }

                                priceRange?.let { range ->
                                    FilterChip(
                                        selected = true,
                                        onClick = { priceRange = null },
                                        label = {
                                            Text(
                                                "💰 ${range.start.toInt()}-${range.endInclusive.toInt()} тг",
                                                fontSize = 13.sp
                                            )
                                        },
                                        trailingIcon = {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Удалить",
                                                modifier = Modifier.size(14.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            selectedLabelColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }

                            TextButton(
                                onClick = {
                                    selectedCategory = null
                                    priceRange = null
                                    isVegetarian = false
                                    minRating = null
                                    hasDiscount = false
                                    sortBy = SortOption.POPULARITY
                                },
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 8.dp)
                            ) {
                                Text("Очистить все", fontSize = 12.sp)
                            }

                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }

                // Контент
                if (loading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text("Загрузка меню...", color = Color.Gray)
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
                                Icons.Outlined.RestaurantMenu,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = Color.Gray.copy(alpha = 0.5f)
                            )
                            Text(
                                "Блюда не найдены",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Попробуйте изменить параметры поиска",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            if (activeFilterCount > 0 || searchQuery.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        selectedCategory = null
                                        priceRange = null
                                        isVegetarian = false
                                        minRating = null
                                        hasDiscount = false
                                        searchQuery = ""
                                    },
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Text("Сбросить все фильтры")
                                }
                            }
                        }
                    }
                } else {
                    // Отображение в зависимости от режима
                    if (viewMode == ViewMode.GRID) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredDishes) { dish ->
                                EnhancedMenuDishCard(
                                    dish = dish,
                                    onClick = { selectedDish = dish }
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(1),
                            contentPadding = PaddingValues(bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredDishes) { dish ->
                                EnhancedListDishCard(
                                    dish = dish,
                                    onClick = { selectedDish = dish }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Современный Bottom Sheet для фильтров
    if (showBottomSheet) {
        ModernFilterBottomSheet(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelect = { selectedCategory = it },
            priceRange = priceRange,
            onPriceRangeChange = { priceRange = it },
            isVegetarian = isVegetarian,
            onVegetarianChange = { isVegetarian = it },
            minRating = minRating,
            onMinRatingChange = { minRating = it },
            hasDiscount = hasDiscount,
            onHasDiscountChange = { hasDiscount = it },
            sortBy = sortBy,
            onSortChange = { sortBy = it },
            activeFilterCount = activeFilterCount,
            onDismiss = { showBottomSheet = false },
            onApply = { showBottomSheet = false },
            onClearAll = {
                selectedCategory = null
                priceRange = null
                isVegetarian = false
                minRating = null
                hasDiscount = false
                sortBy = SortOption.POPULARITY
            }
        )
    }

    // Детали блюда
    selectedDish?.let { dish ->
        EnhancedDishDetailsSheet(
            dish = dish,
            onDismiss = { selectedDish = null },
            onNavigateToRestaurantProfile = onNavigateToRestaurantProfile
        )
    }
}

// Режимы просмотра
enum class ViewMode {
    GRID, LIST
}

// Вспомогательная функция для склонения слов
fun getWordForm(number: Int, form1: String, form2: String, form3: String): String {
    val n = number % 100
    return if (n in 11..19) form3 else when (n % 10) {
        1 -> form1
        2, 3, 4 -> form2
        else -> form3
    }
}

// Улучшенная карточка блюда для сетки
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedMenuDishCard(
    dish: Dish,
    onClick: () -> Unit
) {
    val discountPercentage = dish.discount?.toDoubleOrNull() ?: 0.0
    val originalPrice = dish.price.toDoubleOrNull() ?: 0.0
    val discountedPrice = if (discountPercentage > 0)
        originalPrice * (1 - discountPercentage / 100)
    else originalPrice
    val avgRating = dish.ratingAverage ?: 0.0
    val cookingTime = dish.cookingTime.toIntOrNull() ?: 0

    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Изображение с градиентом и бейджами
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                AsyncImage(
                    model = dish.photoUrl,
                    contentDescription = dish.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentScale = ContentScale.Crop
                )

                // Градиент для лучшей читаемости бейджей
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )

                // Бейдж со скидкой
                if (discountPercentage > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFF9800),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            "-${"%.0f".format(discountPercentage)}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Бейдж популярности
                if (dish.popular) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE91E63),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Filled.Whatshot,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color.White
                            )
                            Text(
                                "Популярное",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }

                // Бейдж вегетарианское
                if (dish.vegetarian) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF4CAF50),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            "🌱 Вегетарианское",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Кнопка корзины
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    EnhancedCartButton(dish = dish)
                }
            }

            // Информация о блюде
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Название и рейтинг
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        dish.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (avgRating > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFC107).copy(alpha = 0.1f),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = Color(0xFFFFC107)
                                )
                                Text(
                                    "%.1f".format(avgRating),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFFFC107)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Краткое описание
                if (dish.description.isNotBlank()) {
                    Text(
                        dish.description,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // Характеристики
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (dish.weightOrVolume.isNotBlank()) {
                        EnhancedInfoChip(
                            text = dish.weightOrVolume,
                            icon = Icons.Outlined.FitnessCenter,
                            size = 10
                        )
                    }

                    if (cookingTime > 0) {
                        EnhancedInfoChip(
                            text = "$cookingTime мин",
                            icon = Icons.Outlined.Schedule,
                            size = 10
                        )
                    }

                    if (dish.calories.isNotBlank()) {
                        val calories = dish.calories.toIntOrNull() ?: 0
                        if (calories > 0) {
                            EnhancedInfoChip(
                                text = "$calories ккал",
                                icon = Icons.Outlined.LocalFireDepartment,
                                size = 10
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Цена и кнопка добавления
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (discountPercentage > 0) {
                            Text(
                                "${"%.0f".format(discountedPrice)} ₸",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                            Text(
                                "${"%.0f".format(originalPrice)} ₸",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textDecoration = TextDecoration.LineThrough
                            )
                        } else {
                            Text(
                                "${"%.0f".format(originalPrice)} ₸",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    EnhancedAddButton(dish = dish)
                }
            }
        }
    }
}

// Улучшенная карточка блюда для списка
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedListDishCard(
    dish: Dish,
    onClick: () -> Unit
) {
    val discountPercentage = dish.discount?.toDoubleOrNull() ?: 0.0
    val originalPrice = dish.price.toDoubleOrNull() ?: 0.0
    val discountedPrice = if (discountPercentage > 0)
        originalPrice * (1 - discountPercentage / 100)
    else originalPrice
    val avgRating = dish.ratingAverage ?: 0.0
    val cookingTime = dish.cookingTime.toIntOrNull() ?: 0

    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Изображение
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                AsyncImage(
                    model = dish.photoUrl,
                    contentDescription = dish.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (discountPercentage > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFF9800),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                    ) {
                        Text(
                            "-${"%.0f".format(discountPercentage)}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                if (dish.vegetarian) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF4CAF50),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(4.dp)
                    ) {
                        Text(
                            "🌱",
                            fontSize = 10.sp,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            // Информация
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        dish.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (avgRating > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFFFFC107)
                            )
                            Text(
                                "%.1f".format(avgRating),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                if (dish.description.isNotBlank()) {
                    Text(
                        dish.description,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // Характеристики
                WrapColumn(
                    horizontalSpacing = 8.dp,
                    verticalSpacing = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (dish.weightOrVolume.isNotBlank()) {
                        EnhancedInfoChip(
                            text = dish.weightOrVolume,
                            icon = Icons.Outlined.FitnessCenter,
                            size = 10
                        )
                    }

                    if (cookingTime > 0) {
                        EnhancedInfoChip(
                            text = "$cookingTime мин",
                            icon = Icons.Outlined.Schedule,
                            size = 10
                        )
                    }

                    if (dish.calories.isNotBlank()) {
                        val calories = dish.calories.toIntOrNull() ?: 0
                        if (calories > 0) {
                            EnhancedInfoChip(
                                text = "$calories ккал",
                                icon = Icons.Outlined.LocalFireDepartment,
                                size = 10
                            )
                        }
                    }

                    if (dish.spiciness.isNotBlank()) {
                        val spicinessLevel = dish.spiciness.toIntOrNull() ?: 0
                        if (spicinessLevel > 0) {
                            EnhancedInfoChip(
                                text = "🌶️ $spicinessLevel/5",
                                icon = Icons.Outlined.LocalFireDepartment,
                                size = 10
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Цена и кнопка добавления
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (discountPercentage > 0) {
                            Text(
                                "${"%.0f".format(discountedPrice)} ₸",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                            Text(
                                "${"%.0f".format(originalPrice)} ₸",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                textDecoration = TextDecoration.LineThrough
                            )
                        } else {
                            Text(
                                "${"%.0f".format(originalPrice)} ₸",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    EnhancedAddButton(dish = dish, size = 36)
                }
            }
        }
    }
}

// Улучшенная кнопка добавления в корзину
@Composable
fun EnhancedAddButton(
    dish: Dish,
    size: Int = 40,
    modifier: Modifier = Modifier
) {
    val quantity = rememberCartItemQuantity(dish.id)

    if (quantity == 0) {
        FloatingActionButton(
            onClick = { CartManager.addToCart(dish) },
            modifier = modifier.size(size.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(4.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Добавить",
                modifier = Modifier.size((size * 0.6).dp)
            )
        }
    } else {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp,
            modifier = modifier
        ) {
            Row(
                modifier = Modifier
                    .height(size.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { CartManager.decrementQuantity(dish.id) },
                    modifier = Modifier.size((size * 0.6).dp)
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Уменьшить",
                        modifier = Modifier.size((size * 0.4).dp),
                        tint = Color.White
                    )
                }

                Text(
                    quantity.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size * 0.4).sp
                )

                IconButton(
                    onClick = { CartManager.incrementQuantity(dish.id) },
                    modifier = Modifier.size((size * 0.6).dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Увеличить",
                        modifier = Modifier.size((size * 0.4).dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

// Улучшенная кнопка корзины на карточке
@Composable
fun EnhancedCartButton(dish: Dish, modifier: Modifier = Modifier) {
    val quantity = rememberCartItemQuantity(dish.id)

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = if (quantity > 0) 8.dp else 4.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clickable { CartManager.addToCart(dish) }
        ) {
            if (quantity > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(20.dp)
                        .background(
                            color = Color.Red,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (quantity > 99) "99+" else quantity.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Icon(
                if (quantity > 0) Icons.Default.ShoppingCart else Icons.Default.AddShoppingCart,
                contentDescription = "Корзина",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// Улучшенный информационный чип
@Composable
fun EnhancedInfoChip(
    text: String,
    icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Int = 12
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.wrapContentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(size.dp),
                tint = color
            )
            Text(
                text,
                fontSize = size.sp,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Расширенные детали блюда
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedDishDetailsSheet(
    dish: Dish,
    onDismiss: () -> Unit,
    onNavigateToRestaurantProfile: (String) -> Unit
) {
    val db = Firebase.firestore
    val scrollState = rememberScrollState()
    val discountPercentage = dish.discount?.toDoubleOrNull() ?: 0.0
    val originalPrice = dish.price.toDoubleOrNull() ?: 0.0
    val discountedPrice = if (discountPercentage > 0)
        originalPrice * (1 - discountPercentage / 100)
    else originalPrice
    val avgRating = dish.ratingAverage ?: 0.0
    val totalRatings = dish.ratingCount ?: 0L
    val cookingTime = dish.cookingTime.toIntOrNull() ?: 0
    val calories = dish.calories.toIntOrNull() ?: 0
    val proteins = dish.proteins.toDoubleOrNull() ?: 0.0
    val fats = dish.fats.toDoubleOrNull() ?: 0.0
    val carbs = dish.carbs.toDoubleOrNull() ?: 0.0
    val spicinessLevel = dish.spiciness.toIntOrNull() ?: 0

    // Загрузка информации о ресторане
    var restaurantName by remember { mutableStateOf("") }
    var restaurantIcon by remember { mutableStateOf("") }
    var restaurantRating by remember { mutableStateOf(0.0) }
    var restaurantRatingCount by remember { mutableStateOf(0L) }

    LaunchedEffect(dish.owner) {
        if (dish.owner.isNotBlank()) {
            try {
                val doc = db.collection("restaurants").document(dish.owner).get().await()
                restaurantName = doc.getString("name") ?: "Неизвестный ресторан"
                restaurantIcon = doc.getString("iconUrl") ?: ""
                restaurantRating = doc.getDouble("rating") ?: 0.0
                restaurantRatingCount = doc.getLong("ratingCount") ?: 0L
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        // Заголовок с кнопкой назад
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDismiss,
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
                fontSize = 24.sp,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Изображение с эффектом
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(horizontal = 16.dp)
        ) {
            AsyncImage(
                model = dish.photoUrl,
                contentDescription = dish.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )

            // Бейджи на изображении
            if (discountPercentage > 0) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFF9800),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Text(
                        "-${"%.0f".format(discountPercentage)}%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            if (dish.popular) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE91E63),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Filled.Whatshot,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                        Text(
                            "Популярное",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Основная информация
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // Цена
            if (discountPercentage > 0) {
                Column {
                    Text(
                        "${"%.0f".format(discountedPrice)} ₸",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    )
                    Text(
                        "${"%.0f".format(originalPrice)} ₸",
                        color = Color.Gray,
                        fontSize = 18.sp,
                        style = TextStyle(textDecoration = TextDecoration.LineThrough)
                    )
                }
            } else {
                Text(
                    "${"%.0f".format(originalPrice)} ₸",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(16.dp))

            // Рейтинг и время приготовления
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (avgRating > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFC107).copy(alpha = 0.1f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "%.1f ($totalRatings)".format(avgRating),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (cookingTime > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "$cookingTime мин",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Описание
            if (dish.description.isNotBlank()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Описание",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            dish.description,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            lineHeight = 20.sp
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Ингредиенты
            if (dish.ingredients.isNotBlank()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Ингредиенты",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            dish.ingredients,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            lineHeight = 20.sp
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Пищевая ценность
            if (calories > 0 || proteins > 0 || fats > 0 || carbs > 0) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Пищевая ценность (на 100г)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            if (calories > 0) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Outlined.LocalFireDepartment,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = Color(0xFFFF9800)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "$calories",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        "ккал",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            if (proteins > 0) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "🥩",
                                        fontSize = 20.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "${"%.1f".format(proteins)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        "белки",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            if (fats > 0) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "🧈",
                                        fontSize = 20.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "${"%.1f".format(fats)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        "жиры",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            if (carbs > 0) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "🍚",
                                        fontSize = 20.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "${"%.1f".format(carbs)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        "углеводы",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Характеристики
            val hasAdditionalInfo = dish.weightOrVolume.isNotBlank() ||
                    dish.vegetarian ||
                    spicinessLevel > 0 ||
                    dish.allergens.isNotBlank()

            if (hasAdditionalInfo) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Характеристики",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(12.dp))

                        WrapColumn(
                            horizontalSpacing = 8.dp,
                            verticalSpacing = 8.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (dish.weightOrVolume.isNotBlank()) {
                                EnhancedInfoChip(
                                    text = "Вес: ${dish.weightOrVolume}",
                                    icon = Icons.Outlined.FitnessCenter,
                                    size = 12
                                )
                            }

                            if (dish.vegetarian) {
                                EnhancedInfoChip(
                                    text = "Вегетарианское",
                                    icon = Icons.Outlined.EmojiFoodBeverage,
                                    color = Color(0xFF4CAF50),
                                    size = 12
                                )
                            }

                            if (spicinessLevel > 0) {
                                val spicinessText = when (spicinessLevel) {
                                    1 -> "🌶️ Слабая острота"
                                    2 -> "🌶️🌶️ Средняя острота"
                                    3 -> "🌶️🌶️🌶️ Острая"
                                    4 -> "🌶️🌶️🌶️🌶️ Очень острая"
                                    5 -> "🌶️🌶️🌶️🌶️🌶️ Экстра острая"
                                    else -> "🌶️ $spicinessLevel/5"
                                }
                                EnhancedInfoChip(
                                    text = spicinessText,
                                    icon = Icons.Outlined.LocalFireDepartment,
                                    color = Color(0xFFE91E63),
                                    size = 12
                                )
                            }

                            if (dish.allergens.isNotBlank()) {
                                EnhancedInfoChip(
                                    text = "⚠️ Аллергены: ${dish.allergens}",
                                    icon = Icons.Outlined.Warning,
                                    color = Color(0xFFFF9800),
                                    size = 12
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Информация о ресторане
            if (restaurantName.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onNavigateToRestaurantProfile(dish.owner)
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (restaurantIcon.isNotEmpty()) {
                            AsyncImage(
                                model = restaurantIcon,
                                contentDescription = "Иконка ресторана",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Restaurant,
                                        contentDescription = "Иконка ресторана",
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                restaurantName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (restaurantRating > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFC107),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        "%.1f ($restaurantRatingCount)".format(restaurantRating),
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "Перейти",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // Кнопки управления корзиной
            EnhancedCartControls(dish = dish, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(32.dp))
        }
    }
}

// Улучшенные контролы корзины
@Composable
fun EnhancedCartControls(dish: Dish, modifier: Modifier = Modifier) {
    val quantity = rememberCartItemQuantity(dish.id)
    val discountPercentage = dish.discount?.toDoubleOrNull() ?: 0.0
    val originalPrice = dish.price.toDoubleOrNull() ?: 0.0
    val discountedPrice = if (discountPercentage > 0)
        originalPrice * (1 - discountPercentage / 100)
    else originalPrice

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        if (quantity == 0) {
            Button(
                onClick = { CartManager.addToCart(dish) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.AddShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Добавить в корзину",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "В вашей корзине",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(
                            onClick = { CartManager.decrementQuantity(dish.id) },
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Уменьшить",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                quantity.toString(),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "шт.",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        IconButton(
                            onClick = { CartManager.incrementQuantity(dish.id) },
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Увеличить",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Итого",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            "${"%.0f".format(discountedPrice * quantity)} ₸",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernFilterBottomSheet(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelect: (String?) -> Unit,
    priceRange: ClosedFloatingPointRange<Float>?,
    onPriceRangeChange: (ClosedFloatingPointRange<Float>?) -> Unit,
    isVegetarian: Boolean,
    onVegetarianChange: (Boolean) -> Unit,
    minRating: Int?,
    onMinRatingChange: (Int?) -> Unit,
    hasDiscount: Boolean,
    onHasDiscountChange: (Boolean) -> Unit,
    sortBy: SortOption,
    onSortChange: (SortOption) -> Unit,
    activeFilterCount: Int,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    onClearAll: () -> Unit
) {
    var localPriceRange by remember(priceRange) { mutableStateOf(priceRange) }
    var localSelectedCategory by remember(selectedCategory) { mutableStateOf(selectedCategory) }
    var localIsVegetarian by remember(isVegetarian) { mutableStateOf(isVegetarian) }
    var localMinRating by remember(minRating) { mutableStateOf(minRating) }
    var localHasDiscount by remember(hasDiscount) { mutableStateOf(hasDiscount) }
    var localSortBy by remember(sortBy) { mutableStateOf(sortBy) }
    var minPriceValue by remember { mutableStateOf(priceRange?.start?.toInt() ?: 0) }
    var maxPriceValue by remember { mutableStateOf(priceRange?.endInclusive?.toInt() ?: 5000) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Фильтры",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                if (activeFilterCount > 0) {
                    TextButton(onClick = onClearAll) {
                        Text("Очистить все", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            FilterSection(
                title = "Сортировка",
                icon = Icons.Default.Sort
            ) {
                WrapColumn(
                    horizontalSpacing = 8.dp,
                    verticalSpacing = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SortOption.values().forEach { option ->
                        FilterChip(
                            selected = localSortBy == option,
                            onClick = { localSortBy = option },
                            label = { Text(option.displayName, fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(
                                    option.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            if (categories.isNotEmpty()) {
                FilterSection(
                    title = "Категория",
                    icon = Icons.Outlined.Category
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = localSelectedCategory == null,
                                onClick = { localSelectedCategory = null },
                                label = { Text("Все", fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        items(categories) { category ->
                            FilterChip(
                                selected = localSelectedCategory == category,
                                onClick = { localSelectedCategory = category },
                                label = { Text(category, fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }

            FilterSection(
                title = "Цена (₸)",
                icon = Icons.Outlined.AttachMoney
            ) {
                Column {
                    RangeSlider(
                        value = minPriceValue.toFloat()..maxPriceValue.toFloat(),
                        onValueChange = { range ->
                            minPriceValue = range.start.roundToInt()
                            maxPriceValue = range.endInclusive.roundToInt()
                            localPriceRange = minPriceValue.toFloat()..maxPriceValue.toFloat()
                        },
                        valueRange = 0f..10000f,
                        steps = 20,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${minPriceValue} ₸",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${maxPriceValue} ₸",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            FilterSection(
                title = "Рейтинг",
                icon = Icons.Outlined.Star
            ) {
                WrapColumn(
                    horizontalSpacing = 8.dp,
                    verticalSpacing = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val ratings = listOf(4, 3, 2, 1)
                    FilterChip(
                        selected = localMinRating == null,
                        onClick = { localMinRating = null },
                        label = { Text("Любой", fontSize = 13.sp) }
                    )
                    ratings.forEach { rating ->
                        FilterChip(
                            selected = localMinRating == rating,
                            onClick = { localMinRating = rating },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color(0xFFFFC107)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("$rating+", fontSize = 13.sp)
                                }
                            }
                        )
                    }
                }
            }

            FilterSection(
                title = "Дополнительно",
                icon = Icons.Outlined.Tune
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = localIsVegetarian,
                            onCheckedChange = { localIsVegetarian = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF4CAF50)
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("🌱 Только вегетарианские", fontSize = 14.sp)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = localHasDiscount,
                            onCheckedChange = { localHasDiscount = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFFFF9800)
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("🏷️ Только со скидкой", fontSize = 14.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    onCategorySelect(localSelectedCategory)
                    onPriceRangeChange(localPriceRange)
                    onVegetarianChange(localIsVegetarian)
                    onMinRatingChange(localMinRating)
                    onHasDiscountChange(localHasDiscount)
                    onSortChange(localSortBy)
                    onApply()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    "Применить фильтры",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                if (activeFilterCount > 0) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                color = Color.White,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            activeFilterCount.toString(),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun FilterSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        content()
    }
}