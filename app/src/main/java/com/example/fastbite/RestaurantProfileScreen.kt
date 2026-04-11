package com.example.fastbite

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.background
import androidx.compose.ui.text.TextStyle
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
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
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import coil.compose.AsyncImage
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// Строки для экрана
object RestaurantStrings {
    val addToCart: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Себетке қосу" else "Добавить в корзину"
    val inCart: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Себетте" else "В корзине"
    val pcs: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "дана" else "шт."
    val sum: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Сомасы" else "Сумма"
    val decrease: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Азайту" else "Уменьшить"
    val increase: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Көбейту" else "Увеличить"
    val unknownRestaurant: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Белгісіз мейрамхана" else "Неизвестный ресторан"
    val restaurantProfile: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Мейрамхана профилі" else "Профиль ресторана"
    val back: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Артқа" else "Назад"
    val restaurantNotFound: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Мейрамхана табылмады" else "Ресторан не найден"
    val menu: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Мәзір" else "Меню"
    val all: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Барлығы" else "Все"
    val noDishes: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Қолжетімді тағамдар жоқ" else "Нет доступных блюд"
    val price: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Бағасы" else "Цена"
    val oldPrice: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Ескі баға" else "Старая цена"
    val description: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Сипаттама" else "Описание"
    val category: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Санат" else "Категория"
    val weightOrVolume: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Салмағы / Көлемі" else "Вес / Объем"
    val ingredients: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Құрамы" else "Ингредиенты"
    val calories: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Калория" else "Калории"
    val proteins: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Ақуыздар" else "Белки"
    val fats: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Майлар" else "Жиры"
    val carbs: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Көмірсулар" else "Углеводы"
    val cookingTime: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Дайындау уақыты" else "Время приготовления"
    val spiciness: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Ащылығы" else "Острота"
    val vegetarian: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Вегетариандық" else "Вегетарианское"
    val yes: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Иә" else "Да"
    val no: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Жоқ" else "Нет"
}

// Компактные контролы корзины
@Composable
fun CartControlsCompact(
    dish: Dish,
    modifier: Modifier = Modifier
) {
    val quantity = rememberCartItemQuantity(dish.id)
    val discountPercentage = dish.discount?.toDoubleOrNull() ?: 0.0
    val originalPrice = dish.price.toDoubleOrNull() ?: 0.0
    val discountedPrice = if (discountPercentage > 0)
        originalPrice * (1 - discountPercentage / 100)
    else originalPrice

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        if (quantity == 0) {
            Button(
                onClick = { CartManager.addToCart(dish) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
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
                    RestaurantStrings.addToCart,
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
                    RestaurantStrings.inCart,
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = { CartManager.decrementQuantity(dish.id) },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = RestaurantStrings.decrease,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                quantity.toString(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                RestaurantStrings.pcs,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        IconButton(
                            onClick = { CartManager.incrementQuantity(dish.id) },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = RestaurantStrings.increase,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            RestaurantStrings.sum,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            "${"%.0f".format(discountedPrice * quantity)} ₸",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// Компактная кнопка корзины
@Composable
fun CartButtonCompact(dish: Dish, modifier: Modifier = Modifier) {
    val quantity = rememberCartItemQuantity(dish.id)

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        tonalElevation = if (quantity > 0) 4.dp else 2.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clickable { CartManager.addToCart(dish) }
        ) {
            if (quantity > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(16.dp)
                        .background(
                            color = Color.Red,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (quantity > 99) "99+" else quantity.toString(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Icon(
                if (quantity > 0) Icons.Default.ShoppingCart else Icons.Default.AddShoppingCart,
                contentDescription = RestaurantStrings.addToCart,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantProfileScreen(
    restaurantId: String,
    onBack: () -> Unit
) {
    val db = Firebase.firestore

    var restaurant by remember { mutableStateOf<Restaurant?>(null) }
    var dishes by remember { mutableStateOf<List<Dish>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf(RestaurantStrings.all) }
    var selectedDish by remember { mutableStateOf<Dish?>(null) }

    // Загружаем данные ресторана
    LaunchedEffect(restaurantId) {
        db.collection("restaurants").document(restaurantId).get()
            .addOnSuccessListener { doc ->
                restaurant = Restaurant(
                    id = doc.id,
                    name = doc.getString("name") ?: RestaurantStrings.unknownRestaurant,
                    description = doc.getString("description") ?: "",
                    iconUrl = doc.getString("iconUrl") ?: "",
                    coverUrl = doc.getString("coverUrl") ?: "",
                    rating = doc.getDouble("rating") ?: 0.0,
                    ratingCount = doc.getLong("ratingCount") ?: 0L,
                    email = doc.id
                )

                // Загружаем блюда ресторана
                db.collection("dishes")
                    .whereEqualTo("owner", restaurantId)
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
                                availability = it.getBoolean("availability") ?: true,
                                ratingAverage = it.getDouble("ratingAverage") ?: 0.0,
                                ratingCount = it.getLong("ratingCount") ?: 0L,
                                owner = restaurantId,
                                discount = it.getString("discount") ?: ""
                            )
                        }
                        loading = false
                    }
                    .addOnFailureListener {
                        loading = false
                    }
            }
            .addOnFailureListener {
                loading = false
            }
    }

    // Категории из блюд
    val categories = listOf(RestaurantStrings.all) +
            dishes.map { it.category }
                .filter { it.isNotBlank() }
                .distinct()

    val filteredDishes = if (selectedCategory == RestaurantStrings.all) dishes
    else dishes.filter { it.category == selectedCategory }

    // Обработка кнопки "Назад" на устройстве
    BackHandler(enabled = selectedDish != null) {
        if (selectedDish != null) {
            selectedDish = null
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (restaurant != null) {
                        Text(restaurant!!.name, maxLines = 1)
                    } else {
                        Text(RestaurantStrings.restaurantProfile)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = RestaurantStrings.back)
                    }
                }
            )
        }
    ) { paddingValues ->
        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (restaurant == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(RestaurantStrings.restaurantNotFound)
            }
        } else {
            // ВЕСЬ КОНТЕНТ ПРОКРУЧИВАЕТСЯ ВНИЗ
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
            ) {
                // Обложка ресторана
                if (restaurant!!.coverUrl.isNotEmpty()) {
                    AsyncImage(
                        model = restaurant!!.coverUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                // Информация о ресторане
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = restaurant!!.iconUrl,
                        contentDescription = "Иконка ресторана",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.width(16.dp))

                    Column {
                        Text(
                            restaurant!!.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )

                        // Рейтинг ресторана
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val rating = restaurant!!.rating
                            val fullStars = rating.toInt()
                            val hasHalfStar = rating % 1 >= 0.5

                            repeat(fullStars) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            if (hasHalfStar) {
                                Icon(
                                    Icons.Filled.StarHalf,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            repeat(5 - fullStars - if (hasHalfStar) 1 else 0) {
                                Icon(
                                    Icons.Outlined.Star,
                                    contentDescription = null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(Modifier.width(4.dp))
                            Text(
                                "%.1f (%d)".format(rating, restaurant!!.ratingCount),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Описание ресторана
                restaurant!!.description?.let { description ->
                    if (description.isNotBlank()) {
                        Text(
                            description,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }

                // Категории
                Text(
                    RestaurantStrings.menu,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    categories.forEach { category ->
                        Text(
                            text = category,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (selectedCategory == category)
                                        MaterialTheme.colorScheme.primary
                                    else Color.LightGray
                                )
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Список блюд
                if (filteredDishes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            RestaurantStrings.noDishes,
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    // Используем Column вместо LazyColumn для плавной прокрутки ВСЕГО контента
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        filteredDishes.forEach { dish ->
                            RestaurantDishCard(
                                dish = dish,
                                onClick = { selectedDish = dish }
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                        // Добавляем отступ внизу для удобства прокрутки
                        Spacer(Modifier.height(32.dp))
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
                        contentDescription = RestaurantStrings.back,
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
                            "${RestaurantStrings.price}: ${"%.0f".format(discountedPrice)} ₸",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                        Text(
                            "${RestaurantStrings.oldPrice}: ${"%.0f".format(originalPrice)} ₸",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            style = TextStyle(textDecoration = TextDecoration.LineThrough)
                        )
                    }
                } else {
                    Text(
                        "${RestaurantStrings.price}: ${"%.0f".format(originalPrice)} ₸",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(16.dp))

                if (dish.description.isNotBlank()) {
                    Column {
                        Text(
                            RestaurantStrings.description,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(dish.description, fontSize = 16.sp)
                        Spacer(Modifier.height(16.dp))
                    }
                }

                if (dish.category.isNotBlank()) {
                    Text("${RestaurantStrings.category}: ${dish.category}", fontSize = 16.sp)
                }

                if (dish.weightOrVolume.isNotBlank()) {
                    Text("${RestaurantStrings.weightOrVolume}: ${dish.weightOrVolume}", fontSize = 16.sp)
                }

                if (dish.ingredients.isNotBlank()) {
                    Text("${RestaurantStrings.ingredients}: ${dish.ingredients}", fontSize = 16.sp)
                }

                if (dish.calories.isNotBlank()) {
                    Text("${RestaurantStrings.calories}: ${dish.calories}", fontSize = 16.sp)
                }

                if (dish.proteins.isNotBlank() || dish.fats.isNotBlank() || dish.carbs.isNotBlank()) {
                    Text("${RestaurantStrings.proteins}: ${dish.proteins}, ${RestaurantStrings.fats}: ${dish.fats}, ${RestaurantStrings.carbs}: ${dish.carbs}", fontSize = 16.sp)
                }

                if (dish.cookingTime.isNotBlank()) {
                    Text("${RestaurantStrings.cookingTime}: ${dish.cookingTime}", fontSize = 16.sp)
                }

                if (dish.spiciness.isNotBlank()) {
                    Text("${RestaurantStrings.spiciness}: ${dish.spiciness}", fontSize = 16.sp)
                }

                Text("${RestaurantStrings.vegetarian}: ${if (dish.vegetarian) RestaurantStrings.yes else RestaurantStrings.no}", fontSize = 16.sp)

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

                // Кнопка добавления в корзину с выбором количества
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

// Data class для ресторана
data class Restaurant(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val iconUrl: String = "",
    val coverUrl: String = "",
    val rating: Double = 0.0,
    val ratingCount: Long = 0L,
    val email: String = ""
)

// Функция для карточки блюда в ресторане
@Composable
fun RestaurantDishCard(
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
                            "${"%.0f".format(discountedPrice)} ₸",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "${"%.0f".format(originalPrice)} ₸",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            style = TextStyle(textDecoration = TextDecoration.LineThrough)
                        )
                    }
                } else {
                    Text(
                        "${"%.0f".format(originalPrice)} ₸",
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

            // Кнопка корзины в правом верхнем углу
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