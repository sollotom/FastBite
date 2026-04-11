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
import androidx.compose.material.icons.filled.Settings
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

// Строки для SellerProfileScreen
// Строки для SellerProfileScreen
object SellerProfileStrings {
    val unknown: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Белгісіз" else "Неизвестно"
    val name: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Атауы" else "Название"
    val icon: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Белгіше (URL)" else "Иконка (URL)"
    val cover: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Мұқаба (URL)" else "Обложка (URL)"
    val description: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Сипаттама" else "Описание"
    val save: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Сақтау" else "Сохранить"
    val cancel: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Бас тарту" else "Отмена"
    val close: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Жабу" else "Закрыть"
    val categories: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Санаттар" else "Категории"
    val all: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Барлығы" else "Все"
    val allReviews: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Барлық пікірлер" else "Все отзывы"
    val noReviews: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Әзірге пікірлер жоқ" else "Пока нет отзывов"
    val reviewsCount: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "пікір" else "отзывов"
    val price: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Бағасы" else "Цена"
    val oldPrice: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Ескі баға" else "Старая цена"
    val category: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Санат" else "Категория"
    val weightOrVolume: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Салмағы/Көлемі" else "Вес/Объем"
    val ingredients: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Құрамы" else "Ингредиенты"
    val calories: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Калория" else "Калории"
    val bju: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "АМК" else "БЖУ"
    val cookingTime: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Дайындау уақыты" else "Время приготовления"
    val spiciness: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Ащылығы" else "Острота"
    val vegetarian: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Вегетариандық" else "Вегетарианское"
    val available: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Қолжетімді" else "Доступно"
    val yes: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Иә" else "Да"
    val no: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Жоқ" else "Нет"
    val logout: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Шығу" else "Выход"
    val logoutConfirm: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Сіз шынымен шыққыңыз келе ме?" else "Вы уверены, что хотите выйти?"
    val exit: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Шығу" else "Выйти"
    val settings: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Баптаулар" else "Настройки"
    val language: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Тіл" else "Язык"
    val anonymous: String get() = if (Strings.currentLanguage.value == Language.KAZAKH) "Аноним" else "Аноним"
    val yourClientsCanLeaveReviews: String get() = if (Strings.currentLanguage.value == Language.KAZAKH)
        "Сіздің клиенттеріңіз тапсырыстардан кейін пікір қалдыра алады" else
        "Ваши клиенты смогут оставлять отзывы после заказов"
}

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

    var restaurantName by remember { mutableStateOf(SellerProfileStrings.unknown) }
    var restaurantDescription by remember { mutableStateOf("") }
    var restaurantIcon by remember { mutableStateOf("") }
    var restaurantCover by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }
    var tempDescription by remember { mutableStateOf("") }
    var tempIcon by remember { mutableStateOf("") }
    var tempCover by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var currentLanguage by remember { mutableStateOf(Strings.getLanguage()) }

    var dishesWithReviews by remember { mutableStateOf<List<DishWithReviews>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf(SellerProfileStrings.all) }
    var selectedDish by remember { mutableStateOf<DishWithReviews?>(null) }
    var showReviews by remember { mutableStateOf(false) }

    // Загрузка данных ресторана
    LaunchedEffect(currentUserEmail) {
        db.collection("restaurants").document(currentUserEmail).get()
            .addOnSuccessListener { doc ->
                restaurantName = doc.getString("name") ?: SellerProfileStrings.unknown
                restaurantDescription = doc.getString("description") ?: ""
                restaurantIcon = doc.getString("iconUrl") ?: ""
                restaurantCover = doc.getString("coverUrl") ?: ""

                tempName = restaurantName
                tempDescription = restaurantDescription
                tempIcon = restaurantIcon
                tempCover = restaurantCover
            }

        loadDishesWithReviews(db, currentUserEmail) { loadedDishes ->
            dishesWithReviews = loadedDishes
        }
    }

    // Средний рейтинг ресторана
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

    val categories = listOf(SellerProfileStrings.all) +
            dishesWithReviews.map { it.dish.category }
                .filter { it.isNotBlank() }
                .distinct()

    val filteredDishes =
        if (selectedCategory == SellerProfileStrings.all) dishesWithReviews
        else dishesWithReviews.filter { it.dish.category == selectedCategory }

    val allRestaurantReviews = remember(dishesWithReviews) {
        dishesWithReviews.flatMap { it.reviews }
    }

    // Диалог выбора языка
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(SellerProfileStrings.language, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Language.values().forEach { language ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable {
                                currentLanguage = language
                                Strings.setLanguage(language)
                                showLanguageDialog = false
                            },
                            color = if (currentLanguage == language) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        ) {
                            Text(
                                text = language.displayName,
                                modifier = Modifier.padding(16.dp),
                                color = if (currentLanguage == language) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(SellerProfileStrings.cancel)
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    // Диалог настроек
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text(SellerProfileStrings.settings, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showSettingsDialog = false
                                showLanguageDialog = true
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                SellerProfileStrings.language,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    currentLanguage.displayName,
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Icon(
                                    Icons.Default.ArrowBack,
                                    null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text(SellerProfileStrings.close)
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Обложка
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

            // Верх лево: кнопка назад + аватар
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(Color.Black.copy(0.4f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }

                Spacer(Modifier.width(12.dp))

                AsyncImage(
                    model = if (restaurantIcon.isNotEmpty())
                        restaurantIcon
                    else "https://via.placeholder.com/150",
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

            // Верх право: кнопки настроек, редактирования и выхода
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                IconButton(
                    onClick = { showSettingsDialog = true },
                    modifier = Modifier.background(Color.Black.copy(0.4f), CircleShape)
                ) {
                    Icon(Icons.Default.Settings, null, tint = Color.White)
                }

                Spacer(Modifier.width(8.dp))

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

        // Основной контент
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
                            label = { Text(SellerProfileStrings.name) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            tempIcon,
                            { tempIcon = it },
                            label = { Text(SellerProfileStrings.icon) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            tempCover,
                            { tempCover = it },
                            label = { Text(SellerProfileStrings.cover) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            tempDescription,
                            { tempDescription = it },
                            label = { Text(SellerProfileStrings.description) },
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
                                Text(SellerProfileStrings.save)
                            }

                            Button(
                                onClick = { isEditing = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Gray
                                )
                            ) {
                                Text(SellerProfileStrings.cancel)
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
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        SellerProfileStrings.categories,
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

            items(filteredDishes.size) { index ->
                val dishWithReviews = filteredDishes[index]
                DishCard(
                    dishWithReviews = dishWithReviews,
                    onClick = { selectedDish = dishWithReviews }
                )
                Spacer(Modifier.height(8.dp))
            }

            item {
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
                                    SellerProfileStrings.allReviews,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "${allRestaurantReviews.size} ${SellerProfileStrings.reviewsCount}",
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
                                SellerProfileStrings.noReviews,
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
                Text(SellerProfileStrings.logout)
            },
            text = {
                Text(SellerProfileStrings.logoutConfirm)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text(SellerProfileStrings.exit, color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(SellerProfileStrings.cancel)
                }
            }
        )
    }

    if (selectedDish != null) {
        val dishWithReviews = selectedDish!!
        FullScreenDishDialog(
            dishWithReviews = dishWithReviews,
            restaurantName = restaurantName,
            restaurantIcon = restaurantIcon,
            restaurantRating = restaurantRating,
            restaurantRatingCount = restaurantRatingCount,
            onClose = { selectedDish = null },
            onProfileClick = {
                selectedDish = null
                isEditing = true
            }
        )
    }

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
        Column(modifier = Modifier.padding(12.dp)) {
            Row {
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
                                "${SellerProfileStrings.price}: ${"%.0f".format(discountedPrice)} тг",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                "${SellerProfileStrings.oldPrice}: ${"%.0f".format(originalPrice)} тг",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                style = TextStyle(textDecoration = TextDecoration.LineThrough)
                            )
                        }
                    } else {
                        Text(
                            "${SellerProfileStrings.price}: ${"%.0f".format(originalPrice)} тг",
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
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
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

                if (reviews.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${reviews.size} ${SellerProfileStrings.reviewsCount}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// ===== ДИАЛОГ ДЕТАЛЕЙ БЛЮДА =====
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenDishDialog(
    dishWithReviews: DishWithReviews,
    restaurantName: String,
    restaurantIcon: String,
    restaurantRating: Double,
    restaurantRatingCount: Long,
    onClose: () -> Unit,
    onProfileClick: () -> Unit
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

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
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
                        val discountPercentage = dish.discount.toDoubleOrNull() ?: 0.0
                        val originalPrice = dish.price.toDoubleOrNull() ?: 0.0
                        val discountedPrice = if (discountPercentage > 0)
                            originalPrice * (1 - discountPercentage / 100)
                        else originalPrice

                        if (discountPercentage > 0) {
                            Column {
                                Text(
                                    "${SellerProfileStrings.price}: ${"%.0f".format(discountedPrice)} тг",
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                                Text(
                                    "${SellerProfileStrings.oldPrice}: ${"%.0f".format(originalPrice)} тг",
                                    color = Color.Gray,
                                    fontSize = 16.sp,
                                    style = TextStyle(textDecoration = TextDecoration.LineThrough)
                                )
                            }
                        } else {
                            Text(
                                "${SellerProfileStrings.price}: ${"%.0f".format(originalPrice)} тг",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    item {
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
                                "%.1f (%d ${SellerProfileStrings.reviewsCount})".format(dish.ratingAverage, dish.ratingCount),
                                fontSize = 16.sp
                            )
                        }
                    }

                    if (dish.description.isNotBlank()) {
                        item {
                            Column {
                                Text(
                                    SellerProfileStrings.description,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(dish.description, fontSize = 16.sp)
                            }
                        }
                    }

                    if (dish.category.isNotBlank()) {
                        item {
                            Text("${SellerProfileStrings.category}: ${dish.category}", fontSize = 16.sp)
                        }
                    }

                    if (dish.weightOrVolume.isNotBlank()) {
                        item {
                            Text("${SellerProfileStrings.weightOrVolume}: ${dish.weightOrVolume}", fontSize = 16.sp)
                        }
                    }

                    if (dish.ingredients.isNotBlank()) {
                        item {
                            Column {
                                Text(
                                    SellerProfileStrings.ingredients,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(dish.ingredients, fontSize = 16.sp)
                            }
                        }
                    }

                    if (dish.calories.isNotBlank()) {
                        item {
                            Text("${SellerProfileStrings.calories}: ${dish.calories}", fontSize = 16.sp)
                        }
                    }

                    if (dish.proteins.isNotBlank() || dish.fats.isNotBlank() || dish.carbs.isNotBlank()) {
                        item {
                            Text("${SellerProfileStrings.bju}: ${dish.proteins} б / ${dish.fats} ж / ${dish.carbs} у", fontSize = 16.sp)
                        }
                    }

                    if (dish.cookingTime.isNotBlank()) {
                        item {
                            Text("${SellerProfileStrings.cookingTime}: ${dish.cookingTime}", fontSize = 16.sp)
                        }
                    }

                    if (dish.spiciness.isNotBlank()) {
                        item {
                            Text("${SellerProfileStrings.spiciness}: ${dish.spiciness}", fontSize = 16.sp)
                        }
                    }

                    item {
                        Text("${SellerProfileStrings.vegetarian}: ${if (dish.vegetarian) SellerProfileStrings.yes else SellerProfileStrings.no}", fontSize = 16.sp)
                        Text("${SellerProfileStrings.available}: ${if (dish.availability) SellerProfileStrings.yes else SellerProfileStrings.no}", fontSize = 16.sp)
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
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
                                        Icons.Default.ArrowBack,
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
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    if (reviews.isNotEmpty()) {
                        item {
                            Column {
                                Text(
                                    "${SellerProfileStrings.allReviews} (${reviews.size})",
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
                                SellerProfileStrings.noReviews,
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

// ===== ДИАЛОГ ВСЕХ ОТЗЫВОВ =====
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
                            SellerProfileStrings.allReviews,
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
                            SellerProfileStrings.noReviews,
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                        Text(
                            SellerProfileStrings.yourClientsCanLeaveReviews,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        review.userName.ifEmpty { SellerProfileStrings.anonymous },
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

            val remaining = dishResult.size()
            var completed = 0

            dishResult.documents.forEach { dishDoc ->
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

                db.collection("dishes")
                    .document(dishDoc.id)
                    .collection("reviews")
                    .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { reviewResult ->
                        val reviews = reviewResult.documents.map { reviewDoc ->
                            Review(
                                id = reviewDoc.id,
                                userName = reviewDoc.getString("userName") ?: SellerProfileStrings.anonymous,
                                userEmail = reviewDoc.getString("userEmail") ?: "",
                                rating = reviewDoc.getDouble("rating") ?: 0.0,
                                comment = reviewDoc.getString("comment") ?: "",
                                date = reviewDoc.getString("date") ?: "",
                                dishId = dishDoc.id
                            )
                        }

                        dishesWithReviews.add(DishWithReviews(dish = dish, reviews = reviews))
                        completed++

                        if (completed == remaining) {
                            onLoaded(dishesWithReviews.sortedByDescending {
                                it.reviews.size
                            })
                        }
                    }
                    .addOnFailureListener {
                        dishesWithReviews.add(DishWithReviews(dish = dish, reviews = emptyList()))
                        completed++

                        if (completed == remaining) {
                            onLoaded(dishesWithReviews)
                        }
                    }
            }
        }
        .addOnFailureListener {
            onLoaded(emptyList())
        }
}