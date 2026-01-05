package com.example.fastbite

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// ===== ВСПОМОГАТЕЛЬНЫЙ КЛАСС ДЛЯ ОТОБРАЖЕНИЯ =====
data class DishWithReviews(
    val dish: Dish,
    val reviews: List<Review> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerProfileScreen(
    currentUserEmail: String,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val db = Firebase.firestore

    var restaurantName by remember { mutableStateOf("Неизвестно") }
    var restaurantDescription by remember { mutableStateOf("") }
    var restaurantIcon by remember { mutableStateOf("") }
    var restaurantCover by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }
    var tempDescription by remember { mutableStateOf("") }
    var tempIcon by remember { mutableStateOf("") }
    var tempCover by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    var dishesWithReviews by remember { mutableStateOf<List<DishWithReviews>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("Все") }
    var selectedDish by remember { mutableStateOf<DishWithReviews?>(null) }
    var showReviews by remember { mutableStateOf(false) }

    // 🔹 Загрузка данных ресторана
    LaunchedEffect(currentUserEmail) {
        db.collection("restaurants").document(currentUserEmail).get()
            .addOnSuccessListener { doc ->
                restaurantName = doc.getString("name") ?: "Неизвестно"
                restaurantDescription = doc.getString("description") ?: ""
                restaurantIcon = doc.getString("iconUrl") ?: ""
                restaurantCover = doc.getString("coverUrl") ?: ""

                tempName = restaurantName
                tempDescription = restaurantDescription
                tempIcon = restaurantIcon
                tempCover = restaurantCover
            }

        // 🔹 Загрузка блюд с отзывами
        loadDishesWithReviews(db, currentUserEmail) { loadedDishes ->
            dishesWithReviews = loadedDishes
        }
    }

    // ⭐ Средний рейтинг ресторана и количество оценок
    val restaurantRating: Double
    val restaurantRatingCount: Long

    if (dishesWithReviews.isNotEmpty()) {
        val totalRatings = dishesWithReviews.map { it.dish.ratingAverage * it.dish.ratingCount }.sum()
        val totalCounts = dishesWithReviews.map { it.dish.ratingCount }.sum()
        restaurantRating = if (totalCounts > 0) totalRatings / totalCounts else 0.0
        restaurantRatingCount = totalCounts
    } else {
        restaurantRating = 0.0
        restaurantRatingCount = 0L
    }

    // 🔹 категории ТОЛЬКО из блюд + "Все"
    val categories = listOf("Все") +
            dishesWithReviews.map { it.dish.category }
                .filter { it.isNotBlank() }
                .distinct()

    val filteredDishes =
        if (selectedCategory == "Все") dishesWithReviews
        else dishesWithReviews.filter { it.dish.category == selectedCategory }

    // 🔹 Все отзывы ресторана
    val allRestaurantReviews = remember(dishesWithReviews) {
        dishesWithReviews.flatMap { it.reviews }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        // ===== ОБЛОЖКА =====
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    if (restaurantCover.isNotEmpty())
                        restaurantCover
                    else "https://via.placeholder.com/800x300"
                ),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            )

            // ===== ВЕРХ ЛЕВО: КНОПКА НАЗАД + АВАТАР =====
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {


                Image(
                    painter = rememberAsyncImagePainter(
                        if (restaurantIcon.isNotEmpty())
                            restaurantIcon
                        else "https://via.placeholder.com/150"
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        text = restaurantName,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    // ⭐ Звёзды и средний рейтинг
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val fullStars = restaurantRating.toInt()
                        val hasHalfStar = restaurantRating % 1 >= 0.5

                        repeat(fullStars) {
                            Icon(
                                Icons.Filled.Star,
                                null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        if (hasHalfStar) {
                            Icon(
                                Icons.Filled.StarHalf,
                                null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        repeat(5 - fullStars - if (hasHalfStar) 1 else 0) {
                            Icon(
                                Icons.Outlined.Star,
                                null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "%.1f (%d)".format(restaurantRating, restaurantRatingCount),
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // ===== ВЕРХ ПРАВО: ТОЛЬКО КНОПКИ РЕДАКТИРОВАНИЯ И ВЫХОДА =====
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                IconButton(
                    onClick = { isEditing = !isEditing },
                    modifier = Modifier.background(Color.Black.copy(0.4f), CircleShape)
                ) {
                    Icon(Icons.Default.Edit, null, tint = Color.White)
                }

                Spacer(Modifier.width(8.dp))

                IconButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.background(Color.Black.copy(0.4f), CircleShape)
                ) {
                    Icon(Icons.Default.Logout, null, tint = Color.White)
                }
            }
        }

        // ===== ОСНОВНОЙ КОНТЕНТ =====
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                if (isEditing) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            tempName,
                            { tempName = it },
                            label = { Text("Название") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            tempIcon,
                            { tempIcon = it },
                            label = { Text("Иконка (URL)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            tempCover,
                            { tempCover = it },
                            label = { Text("Обложка (URL)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            tempDescription,
                            { tempDescription = it },
                            label = { Text("Описание") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    db.collection("restaurants").document(currentUserEmail)
                                        .set(
                                            mapOf(
                                                "name" to tempName,
                                                "description" to tempDescription,
                                                "iconUrl" to tempIcon,
                                                "coverUrl" to tempCover
                                            )
                                        )
                                    restaurantName = tempName
                                    restaurantDescription = tempDescription
                                    restaurantIcon = tempIcon
                                    restaurantCover = tempCover
                                    isEditing = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Сохранить")
                            }

                            Button(
                                onClick = { isEditing = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Gray
                                )
                            ) {
                                Text("Отмена")
                            }
                        }
                    }
                } else {
                    if (restaurantDescription.isNotBlank()) {
                        Text(
                            restaurantDescription,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            item {
                // ===== КАТЕГОРИИ =====
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Категории",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
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
                }
            }

            // ===== БЛЮДА =====
            items(filteredDishes.size) { index ->
                val dishWithReviews = filteredDishes[index]
                DishCard(
                    dishWithReviews = dishWithReviews,
                    onClick = { selectedDish = dishWithReviews }
                )
                Spacer(Modifier.height(8.dp))
            }

            item {
                // ===== КНОПКА "ВСЕ ОТЗЫВЫ" ВНИЗУ =====
                if (allRestaurantReviews.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showReviews = true },
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Все отзывы",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "${allRestaurantReviews.size} отзывов",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                "📝",
                                fontSize = 20.sp
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showReviews = true },
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.LightGray.copy(alpha = 0.2f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Пока нет отзывов",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "📝",
                                fontSize = 20.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(80.dp))
            }
        }
    }
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text("Выход")
            },
            text = {
                Text("Вы уверены, что хотите выйти?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout() // Вызываем функцию выхода
                    }
                ) {
                    Text("Выйти", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
    // ===== ДИАЛОГ ДЕТАЛЕЙ БЛЮДА (ПОЛНЫЙ ЭКРАН) =====
    if (selectedDish != null) {
        val dishWithReviews = selectedDish!!
        FullScreenDishDialog(
            dishWithReviews = dishWithReviews,
            onClose = { selectedDish = null }
        )
    }

    // ===== ДИАЛОГ ВСЕХ ОТЗЫВОВ РЕСТОРАНА =====
    if (showReviews) {
        FullScreenReviewsDialog(
            reviews = allRestaurantReviews,
            restaurantName = restaurantName,
            onClose = { showReviews = false }
        )
    }
}

// ===== КОМПОНЕНТ КАРТОЧКИ БЛЮДА =====
@Composable
fun DishCard(
    dishWithReviews: DishWithReviews,
    onClick: () -> Unit
) {
    val dish = dishWithReviews.dish
    val reviews = dishWithReviews.reviews

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = dish.photoUrl.ifEmpty { "https://via.placeholder.com/120" },
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    dish.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1
                )

                val discountPercentage = dish.discount.toDoubleOrNull() ?: 0.0
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
                            fontSize = 16.sp
                        )
                        Text(
                            "Старая цена: ${"%.0f".format(originalPrice)} тг",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            style = TextStyle(textDecoration = TextDecoration.LineThrough)
                        )
                    }
                } else {
                    Text(
                        "Цена: ${"%.0f".format(originalPrice)} тг",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (dish.category.isNotBlank()) {
                    Text(
                        dish.category,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Рейтинг блюда
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    val fullStars = dish.ratingAverage.toInt()
                    val hasHalfStar = (dish.ratingAverage % 1) >= 0.5

                    repeat(fullStars) {
                        Icon(
                            Icons.Filled.Star,
                            null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    if (hasHalfStar) {
                        Icon(
                            Icons.Filled.StarHalf,
                            null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    repeat(5 - fullStars - if (hasHalfStar) 1 else 0) {
                        Icon(
                            Icons.Outlined.Star,
                            null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(Modifier.width(4.dp))
                    Text(
                        "%.1f".format(dish.ratingAverage),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    // Количество отзывов
                    if (reviews.isNotEmpty()) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${reviews.size} отзывов",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

// ===== ДИАЛОГ ДЕТАЛЕЙ БЛЮДА (ПОЛНЫЙ ЭКРАН) =====
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenDishDialog(
    dishWithReviews: DishWithReviews,
    onClose: () -> Unit
) {
    val dish = dishWithReviews.dish
    val reviews = dishWithReviews.reviews

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Шапка с кнопкой назад
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, null)
                    }

                    Spacer(Modifier.width(12.dp))

                    Text(
                        dish.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Контент
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    item {
                        // Фото блюда
                        AsyncImage(
                            model = dish.photoUrl.ifEmpty { "https://via.placeholder.com/200" },
                            contentDescription = dish.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    item {
                        // Цена со скидкой
                        val discountPercentage = dish.discount.toDoubleOrNull() ?: 0.0
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
                    }

                    item {
                        // Рейтинг блюда
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val fullStars = dish.ratingAverage.toInt()
                            val hasHalfStar = (dish.ratingAverage % 1) >= 0.5
                            repeat(fullStars) {
                                Icon(
                                    Icons.Filled.Star,
                                    null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            if (hasHalfStar) {
                                Icon(
                                    Icons.Filled.StarHalf,
                                    null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            repeat(5 - fullStars - if (hasHalfStar) 1 else 0) {
                                Icon(
                                    Icons.Outlined.Star,
                                    null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "%.1f (%d отзывов)".format(dish.ratingAverage, dish.ratingCount),
                                fontSize = 16.sp
                            )
                        }
                    }

                    // Детали блюда
                    if (dish.description.isNotBlank()) {
                        item {
                            Column {
                                Text(
                                    "Описание",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(dish.description, fontSize = 16.sp)
                            }
                        }
                    }

                    if (dish.category.isNotBlank()) {
                        item {
                            Text("Категория: ${dish.category}", fontSize = 16.sp)
                        }
                    }

                    if (dish.weightOrVolume.isNotBlank()) {
                        item {
                            Text("Вес/Объем: ${dish.weightOrVolume}", fontSize = 16.sp)
                        }
                    }

                    if (dish.ingredients.isNotBlank()) {
                        item {
                            Column {
                                Text(
                                    "Ингредиенты",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(dish.ingredients, fontSize = 16.sp)
                            }
                        }
                    }

                    if (dish.calories.isNotBlank()) {
                        item {
                            Text("Калории: ${dish.calories}", fontSize = 16.sp)
                        }
                    }

                    if (dish.proteins.isNotBlank() || dish.fats.isNotBlank() || dish.carbs.isNotBlank()) {
                        item {
                            Text("БЖУ: ${dish.proteins} б / ${dish.fats} ж / ${dish.carbs} у", fontSize = 16.sp)
                        }
                    }

                    if (dish.cookingTime.isNotBlank()) {
                        item {
                            Text("Время приготовления: ${dish.cookingTime}", fontSize = 16.sp)
                        }
                    }

                    if (dish.spiciness.isNotBlank()) {
                        item {
                            Text("Острота: ${dish.spiciness}", fontSize = 16.sp)
                        }
                    }

                    item {
                        Text("Вегетарианское: ${if (dish.vegetarian) "Да" else "Нет"}", fontSize = 16.sp)
                        Text("Доступно: ${if (dish.availability) "Да" else "Нет"}", fontSize = 16.sp)
                    }

                    // ===== ОТЗЫВЫ БЛЮДА =====
                    if (reviews.isNotEmpty()) {
                        item {
                            Column {
                                Text(
                                    "Отзывы (${reviews.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(top = 16.dp)
                                )

                                Spacer(Modifier.height(8.dp))

                                reviews.forEach { review ->
                                    ReviewCard(review = review)
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                        }
                    } else {
                        item {
                            Text(
                                "Пока нет отзывов",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 24.dp)
                            )
                        }
                    }

                    item {
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

// ===== ДИАЛОГ ВСЕХ ОТЗЫВОВ РЕСТОРАНА =====
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenReviewsDialog(
    reviews: List<Review>,
    restaurantName: String,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Шапка
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, null)
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            "Все отзывы",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            "$restaurantName (${reviews.size})",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Список отзывов
                if (reviews.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "😊",
                            fontSize = 48.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Пока нет отзывов",
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                        Text(
                            "Ваши клиенты смогут оставлять отзывы после заказов",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(reviews.size) { index ->
                            val review = reviews[index]
                            ReviewCard(review = review)
                        }

                        item {
                            Spacer(Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}

// ===== КОМПОНЕНТ КАРТОЧКИ ОТЗЫВА =====
@Composable
fun ReviewCard(review: Review) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Заголовок отзыва
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        review.userName.ifEmpty { "Аноним" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    if (review.date.isNotBlank()) {
                        Text(
                            review.date,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Звёзды рейтинга
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val fullStars = review.rating.toInt()
                    val hasHalfStar = review.rating % 1 >= 0.5
                    repeat(fullStars) {
                        Icon(
                            Icons.Filled.Star,
                            null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (hasHalfStar) {
                        Icon(
                            Icons.Filled.StarHalf,
                            null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    repeat(5 - fullStars - if (hasHalfStar) 1 else 0) {
                        Icon(
                            Icons.Outlined.Star,
                            null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Текст отзыва
            if (review.comment.isNotBlank()) {
                Text(
                    review.comment,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// ===== ФУНКЦИЯ ЗАГРУЗКИ БЛЮД С ОТЗЫВАМИ =====
fun loadDishesWithReviews(
    db: com.google.firebase.firestore.FirebaseFirestore,
    ownerEmail: String,
    onLoaded: (List<DishWithReviews>) -> Unit
) {
    db.collection("dishes")
        .whereEqualTo("owner", ownerEmail)
        .get()
        .addOnSuccessListener { dishResult ->
            val dishesWithReviews = mutableListOf<DishWithReviews>()

            if (dishResult.isEmpty) {
                onLoaded(emptyList())
                return@addOnSuccessListener
            }

            dishResult.documents.forEachIndexed { index, dishDoc ->
                val dish = Dish(
                    id = dishDoc.id,
                    name = dishDoc.getString("name") ?: "",
                    price = dishDoc.getString("price") ?: "",
                    category = dishDoc.getString("category") ?: "",
                    description = dishDoc.getString("description") ?: "",
                    photoUrl = dishDoc.getString("photoUrl") ?: "",
                    ratingAverage = dishDoc.getDouble("ratingAverage") ?: 0.0,
                    ratingCount = dishDoc.getLong("ratingCount") ?: 0L,
                    owner = ownerEmail,
                    discount = dishDoc.getString("discount") ?: "",
                    weightOrVolume = dishDoc.getString("weightOrVolume") ?: "",
                    ingredients = dishDoc.getString("ingredients") ?: "",
                    calories = dishDoc.getString("calories") ?: "",
                    proteins = dishDoc.getString("proteins") ?: "",
                    fats = dishDoc.getString("fats") ?: "",
                    carbs = dishDoc.getString("carbs") ?: "",
                    cookingTime = dishDoc.getString("cookingTime") ?: "",
                    spiciness = dishDoc.getString("spiciness") ?: "",
                    vegetarian = dishDoc.getBoolean("vegetarian") ?: false,
                    availability = dishDoc.getBoolean("availability") ?: true
                )

                // Загружаем отзывы для этого блюда
                db.collection("reviews")
                    .whereEqualTo("dishId", dishDoc.id)
                    .get()
                    .addOnSuccessListener { reviewResult ->
                        val reviews = reviewResult.documents.map { reviewDoc ->
                            Review(
                                id = reviewDoc.id,
                                userName = reviewDoc.getString("userName") ?: "Аноним",
                                userEmail = reviewDoc.getString("userEmail") ?: "",
                                rating = reviewDoc.getDouble("rating") ?: 0.0,
                                comment = reviewDoc.getString("comment") ?: "",
                                date = reviewDoc.getString("date") ?: "",
                                dishId = reviewDoc.getString("dishId") ?: dishDoc.id
                            )
                        }

                        dishesWithReviews.add(DishWithReviews(dish = dish, reviews = reviews))

                        // Если загрузили все блюда, вызываем колбэк
                        if (dishesWithReviews.size == dishResult.size()) {
                            onLoaded(dishesWithReviews)
                        }
                    }
                    .addOnFailureListener {
                        // Если не удалось загрузить отзывы, добавляем блюдо без них
                        dishesWithReviews.add(DishWithReviews(dish = dish, reviews = emptyList()))

                        if (dishesWithReviews.size == dishResult.size()) {
                            onLoaded(dishesWithReviews)
                        }
                    }
            }
        }
}