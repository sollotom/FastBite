package com.example.fastbite

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// Строки для SellerMenuScreen
object SellerMenuStrings {
    val myMenu: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Сіздің мәзіріңіз" else "Ваше меню"
    val searchDish: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Тағамды іздеу" else "Поиск блюда"
    val addDish: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Тағам қосу" else "Добавить блюдо"
    val edit: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Өңдеу" else "Редактировать"
    val delete: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Жою" else "Удалить"
    val deleteDish: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Тағамды жою?" else "Удалить блюдо?"
    fun deleteConfirm(dishName: String): String = if (Strings.currentLanguage.value == Language.KAZAKH)
        "Сіз шынымен \"$dishName\" жойғыңыз келе ме?" else
        "Вы уверены, что хотите удалить \"$dishName\"?"
    val cancel: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Бас тарту" else "Отмена"
    val back: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Артқа" else "Назад"
    val myRestaurant: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Менің мейрамханам" else "Мой ресторан"
    val goToProfile: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Профильге өту" else "Перейти в профиль"
    val reviews: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Пікірлер" else "Отзывы"
    val noReviews: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Әзірге пікірлер жоқ" else "Пока отзывов нет"
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
    val available: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Қолжетімді" else "Доступно"
    val yes: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Иә" else "Да"
    val no: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Жоқ" else "Нет"
    val save: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Сақтау" else "Сохранить"
    val editDish: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Тағамды өңдеу" else "Редактировать блюдо"
    val name: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Атауы" else "Название"
    val photoUrl: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Фото URL" else "Фото URL"
    val allergens: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Аллергендер" else "Аллергены"
    val addOns: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Қоспалар" else "Добавки"
    val addOnsPrice: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Қоспалар бағасы" else "Цена добавок"
    val portions: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Порциялар" else "Порции"
    val costPrice: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Өзіндік құны" else "Себестоимость"
    val discount: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Жеңілдік" else "Скидка"
    val popularDish: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Танымал тағам" else "Популярное блюдо"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SellerMenuScreen(
    currentUserEmail: String,
    onAddDishClick: () -> Unit,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val db = Firebase.firestore

    var dishes by remember { mutableStateOf(listOf<Dish>()) }
    var dishToEdit by remember { mutableStateOf<Dish?>(null) }
    var dishToDelete by remember { mutableStateOf<Dish?>(null) }
    var selectedDish by remember { mutableStateOf<Dish?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDishScreen by remember { mutableStateOf(false) }

    var restaurantName by remember { mutableStateOf(SellerMenuStrings.myRestaurant) }
    var restaurantIcon by remember { mutableStateOf("") }

    androidx.activity.compose.BackHandler(
        enabled = selectedDish != null || dishToEdit != null || dishToDelete != null
    ) {
        when {
            selectedDish != null -> selectedDish = null
            dishToEdit != null -> dishToEdit = null
            dishToDelete != null -> dishToDelete = null
            else -> onBackClick()
        }
    }

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
                        ratingAverage = it.getDouble("ratingAverage") ?: 0.0,
                        ratingCount = it.getLong("ratingCount") ?: 0L,
                        portions = it.getString("portions") ?: "",
                        costPrice = it.getString("costPrice") ?: "",
                        discount = it.getString("discount") ?: "",
                        popular = it.getBoolean("popular") ?: false,
                        dateAdded = it.getString("dateAdded") ?: "",
                        owner = currentUserEmail
                    )
                }
            }

        db.collection("restaurants").document(currentUserEmail).get()
            .addOnSuccessListener { doc ->
                restaurantName = doc.getString("name") ?: SellerMenuStrings.myRestaurant
                restaurantIcon = doc.getString("iconUrl") ?: ""
            }
    }

    val restaurantRating: Double
    val restaurantRatingCount: Long

    if (dishes.isNotEmpty()) {
        val totalRatings = dishes.map {
            (it.ratingAverage ?: 0.0) * (it.ratingCount?.toDouble() ?: 0.0)
        }.sum()
        val totalCounts = dishes.map { it.ratingCount?.toLong() ?: 0L }.sum()
        restaurantRating = if (totalCounts > 0) totalRatings / totalCounts else 0.0
        restaurantRatingCount = totalCounts
    } else {
        restaurantRating = 0.0
        restaurantRatingCount = 0L
    }

    val filteredDishes = dishes.filter { it.name.contains(searchQuery, ignoreCase = true) }

    if (showAddDishScreen) {
        AddDishScreen(
            currentUserEmail = currentUserEmail,
            onBack = { showAddDishScreen = false }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        SellerMenuStrings.myMenu,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
                label = { Text(SellerMenuStrings.searchDish) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { showAddDishScreen = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(SellerMenuStrings.addDish, fontSize = 16.sp)
            }

            Spacer(Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredDishes) { dish ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedDish = dish }
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

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(
                                    onClick = { dishToEdit = dish },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0x1A2196F3), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = SellerMenuStrings.edit,
                                        tint = Color(0xFF2196F3),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { dishToDelete = dish },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0x1AF44336), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = SellerMenuStrings.delete,
                                        tint = Color(0xFFF44336),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    dishToEdit?.let { dish ->
        EditDishDialog(
            dish = dish,
            db = db,
            onDismiss = { dishToEdit = null },
            onSave = { updatedDish ->
                dishes = dishes.map { if (it.id == updatedDish.id) updatedDish else it }
                dishToEdit = null
            }
        )
    }

    dishToDelete?.let { dish ->
        AlertDialog(
            onDismissRequest = { dishToDelete = null },
            title = { Text(SellerMenuStrings.deleteDish) },
            text = { Text(SellerMenuStrings.deleteConfirm(dish.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        db.collection("dishes").document(dish.id).delete()
                        dishes = dishes.filter { it.id != dish.id }
                        dishToDelete = null
                    }
                ) {
                    Text(SellerMenuStrings.delete, color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { dishToDelete = null }) {
                    Text(SellerMenuStrings.cancel)
                }
            }
        )
    }

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
                        contentDescription = SellerMenuStrings.back,
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
                val discountedPrice = if (discountPercentage > 0) originalPrice * (1 - discountPercentage / 100) else originalPrice

                if (discountPercentage > 0) {
                    Column {
                        Text(
                            "${SellerMenuStrings.price}: ${"%.0f".format(discountedPrice)} тг",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                        Text(
                            "${SellerMenuStrings.oldPrice}: ${"%.0f".format(originalPrice)} тг",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            style = TextStyle(textDecoration = TextDecoration.LineThrough)
                        )
                    }
                } else {
                    Text(
                        "${SellerMenuStrings.price}: ${"%.0f".format(originalPrice)} тг",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(16.dp))

                if (dish.description.isNotBlank()) {
                    Column {
                        Text(
                            SellerMenuStrings.description,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(dish.description, fontSize = 16.sp)
                        Spacer(Modifier.height(16.dp))
                    }
                }

                Text("${SellerMenuStrings.category}: ${dish.category}")
                Text("${SellerMenuStrings.weightOrVolume}: ${dish.weightOrVolume}")
                Text("${SellerMenuStrings.ingredients}: ${dish.ingredients}")
                Text("${SellerMenuStrings.calories}: ${dish.calories}")
                Text("${SellerMenuStrings.proteins}: ${dish.proteins}, ${SellerMenuStrings.fats}: ${dish.fats}, ${SellerMenuStrings.carbs}: ${dish.carbs}")
                Text("${SellerMenuStrings.cookingTime}: ${dish.cookingTime}")
                Text("${SellerMenuStrings.spiciness}: ${dish.spiciness}")
                Text("${SellerMenuStrings.vegetarian}: ${if (dish.vegetarian) SellerMenuStrings.yes else SellerMenuStrings.no}")
                Text("${SellerMenuStrings.available}: ${if (dish.availability) SellerMenuStrings.yes else SellerMenuStrings.no}")

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

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onProfileClick),
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
                                Icons.Default.Person,
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

                            Spacer(Modifier.height(4.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val fullStars = restaurantRating.toInt()
                                val hasHalfStar = restaurantRating % 1 >= 0.5

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
                                    "%.1f (%d)".format(restaurantRating, restaurantRatingCount),
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = SellerMenuStrings.goToProfile,
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer { rotationZ = 180f },
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(SellerMenuStrings.reviews, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                if (dish.reviews.isNullOrEmpty()) {
                    Text(SellerMenuStrings.noReviews)
                } else {
                    dish.reviews.forEach { review ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(review.userName, fontWeight = FontWeight.Bold)

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val fullStars = review.rating.toInt()
                                    val hasHalfStar = review.rating % 1 >= 0.5
                                    repeat(fullStars) {
                                        Icon(Icons.Filled.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                                    }
                                    if (hasHalfStar) {
                                        Icon(Icons.Filled.StarHalf, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                                    }
                                    repeat(5 - fullStars - if (hasHalfStar) 1 else 0) {
                                        Icon(Icons.Outlined.Star, null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                                    }
                                }

                                Spacer(Modifier.height(4.dp))
                                Text(review.comment)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDishDialog(
    dish: Dish,
    db: com.google.firebase.firestore.FirebaseFirestore,
    onDismiss: () -> Unit,
    onSave: (Dish) -> Unit
) {
    var name by remember { mutableStateOf(dish.name) }
    var price by remember { mutableStateOf(dish.price) }
    var description by remember { mutableStateOf(dish.description) }
    var photoUrl by remember { mutableStateOf(dish.photoUrl) }
    var category by remember { mutableStateOf(dish.category) }
    var weightOrVolume by remember { mutableStateOf(dish.weightOrVolume) }
    var ingredients by remember { mutableStateOf(dish.ingredients) }
    var calories by remember { mutableStateOf(dish.calories) }
    var proteins by remember { mutableStateOf(dish.proteins) }
    var fats by remember { mutableStateOf(dish.fats) }
    var carbs by remember { mutableStateOf(dish.carbs) }
    var cookingTime by remember { mutableStateOf(dish.cookingTime) }
    var spiciness by remember { mutableStateOf(dish.spiciness) }
    var vegetarian by remember { mutableStateOf(dish.vegetarian) }
    var allergens by remember { mutableStateOf(dish.allergens) }
    var addOns by remember { mutableStateOf(dish.addOns) }
    var addOnsPrice by remember { mutableStateOf(dish.addOnsPrice) }
    var availability by remember { mutableStateOf(dish.availability) }
    var portions by remember { mutableStateOf(dish.portions) }
    var costPrice by remember { mutableStateOf(dish.costPrice) }
    var discount by remember { mutableStateOf(dish.discount) }
    var popular by remember { mutableStateOf(dish.popular) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(SellerMenuStrings.editDish) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(name, { name = it }, label = { Text(SellerMenuStrings.name) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(price, { price = it }, label = { Text(SellerMenuStrings.price) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(description, { description = it }, label = { Text(SellerMenuStrings.description) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(photoUrl, { photoUrl = it }, label = { Text(SellerMenuStrings.photoUrl) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(category, { category = it }, label = { Text(SellerMenuStrings.category) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(weightOrVolume, { weightOrVolume = it }, label = { Text(SellerMenuStrings.weightOrVolume) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(ingredients, { ingredients = it }, label = { Text(SellerMenuStrings.ingredients) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(calories, { calories = it }, label = { Text(SellerMenuStrings.calories) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(proteins, { proteins = it }, label = { Text(SellerMenuStrings.proteins) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(fats, { fats = it }, label = { Text(SellerMenuStrings.fats) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(carbs, { carbs = it }, label = { Text(SellerMenuStrings.carbs) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(cookingTime, { cookingTime = it }, label = { Text(SellerMenuStrings.cookingTime) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(spiciness, { spiciness = it }, label = { Text(SellerMenuStrings.spiciness) }, modifier = Modifier.fillMaxWidth())

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(vegetarian, { vegetarian = it })
                    Text(SellerMenuStrings.vegetarian)
                }

                OutlinedTextField(allergens, { allergens = it }, label = { Text(SellerMenuStrings.allergens) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(addOns, { addOns = it }, label = { Text(SellerMenuStrings.addOns) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(addOnsPrice, { addOnsPrice = it }, label = { Text(SellerMenuStrings.addOnsPrice) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(availability, { availability = it })
                    Text(SellerMenuStrings.available)
                }

                OutlinedTextField(portions, { portions = it }, label = { Text(SellerMenuStrings.portions) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(costPrice, { costPrice = it }, label = { Text(SellerMenuStrings.costPrice) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(discount, { discount = it }, label = { Text(SellerMenuStrings.discount) }, modifier = Modifier.fillMaxWidth())

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(popular, { popular = it })
                    Text(SellerMenuStrings.popularDish)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedDish = dish.copy(
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
                        portions = portions,
                        costPrice = costPrice,
                        discount = discount,
                        popular = popular
                    )

                    val data = mapOf(
                        "name" to name,
                        "price" to price,
                        "description" to description,
                        "photoUrl" to photoUrl,
                        "category" to category,
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
                        "portions" to portions,
                        "costPrice" to costPrice,
                        "discount" to discount,
                        "popular" to popular
                    )

                    db.collection("dishes").document(dish.id).update(data)
                    onSave(updatedDish)
                }
            ) {
                Text(SellerMenuStrings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(SellerMenuStrings.cancel)
            }
        }
    )
}