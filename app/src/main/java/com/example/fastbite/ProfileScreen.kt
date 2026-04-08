package com.example.fastbite

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ==================== ENUMS AND DATA CLASSES ====================

enum class ProfileScreenType {
    Main, Settings, EditProfile, Addresses, AddAddress, EditAddress,
    Help, FAQ, ContactSupport, TermsAndConditions, AboutApp,
    MyReviews, EditReview
}

enum class SupportTopic(val title: String) {
    ORDER("Проблема с заказом"),
    PAYMENT("Оплата"),
    DELIVERY("Доставка"),
    RESTAURANT("Ресторан"),
    APP("Приложение"),
    OTHER("Другое")
}

data class FAQItem(
    val question: String,
    val answer: String
)

// ==================== MAIN PROFILE SCREEN ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userEmail: String,
    onLogout: () -> Unit,
    onNavigateToOrders: () -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val coroutineScope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf(ProfileScreenType.Main) }
    var previousScreens by remember { mutableStateOf(listOf<ProfileScreenType>()) }

    var selectedAddressForEdit by remember { mutableStateOf<Address?>(null) }
    var selectedReviewForEdit by remember { mutableStateOf<Review?>(null) }
    var addressToDelete by remember { mutableStateOf<Address?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var userName by remember { mutableStateOf("") }
    var userPhone by remember { mutableStateOf("") }
    var addresses by remember { mutableStateOf<List<Address>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var notificationsEnabled by remember { mutableStateOf(true) }
    var emailEnabled by remember { mutableStateOf(false) }
    var language by remember { mutableStateOf("Русский") }
    var theme by remember { mutableStateOf("Светлая") }

    fun navigateTo(screen: ProfileScreenType) {
        previousScreens = previousScreens + currentScreen
        currentScreen = screen
    }

    fun navigateBack() {
        if (previousScreens.isNotEmpty()) {
            currentScreen = previousScreens.last()
            previousScreens = previousScreens.dropLast(1)
        } else {
            currentScreen = ProfileScreenType.Main
        }
    }

    BackHandler {
        if (currentScreen != ProfileScreenType.Main) {
            navigateBack()
        }
    }

    LaunchedEffect(userEmail) {
        if (userEmail.isBlank()) return@LaunchedEffect

        isLoading = true
        try {
            val userDoc = db.collection("users").document(userEmail).get().await()
            if (userDoc.exists()) {
                userName = userDoc.getString("name") ?: ""
                userPhone = userDoc.getString("phone") ?: ""
            }
            loadAddresses(userEmail, db) { addresses = it }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    if (showDeleteDialog && addressToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                addressToDelete = null
            },
            title = { Text("Удалить адрес", fontWeight = FontWeight.Bold) },
            text = { Text("Вы уверены, что хотите удалить адрес:\n${addressToDelete!!.address}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                deleteAddress(userEmail, addressToDelete!!.id, db)
                                Toast.makeText(context, "Адрес удален", Toast.LENGTH_SHORT).show()
                                loadAddresses(userEmail, db) { addresses = it }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                showDeleteDialog = false
                                addressToDelete = null
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    addressToDelete = null
                }) { Text("Отмена") }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (currentScreen) {
                            ProfileScreenType.Main -> "Профиль"
                            ProfileScreenType.Settings -> "Настройки"
                            ProfileScreenType.EditProfile -> "Редактировать профиль"
                            ProfileScreenType.Addresses -> "Адреса доставки"
                            ProfileScreenType.AddAddress -> "Добавить адрес"
                            ProfileScreenType.EditAddress -> "Редактировать адрес"
                            ProfileScreenType.Help -> "Помощь и поддержка"
                            ProfileScreenType.FAQ -> "Часто задаваемые вопросы"
                            ProfileScreenType.ContactSupport -> "Связаться с поддержкой"
                            ProfileScreenType.TermsAndConditions -> "Правила и условия"
                            ProfileScreenType.AboutApp -> "О приложении"
                            ProfileScreenType.MyReviews -> "Мои отзывы"
                            ProfileScreenType.EditReview -> "Редактировать отзыв"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                navigationIcon = {
                    if (currentScreen != ProfileScreenType.Main) {
                        IconButton(onClick = { navigateBack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                        }
                    }
                },
                actions = {
                    when (currentScreen) {
                        ProfileScreenType.Main -> {
                            IconButton(onClick = { navigateTo(ProfileScreenType.Settings) }) {
                                Icon(Icons.Outlined.Settings, contentDescription = "Настройки")
                            }
                        }
                        else -> {}
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (isLoading && currentScreen == ProfileScreenType.Main) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                when (currentScreen) {
                    ProfileScreenType.Main -> {
                        MainProfileContent(
                            userName = userName,
                            userEmail = userEmail,
                            userPhone = userPhone,
                            addresses = addresses,
                            onNavigateToOrders = onNavigateToOrders,
                            onNavigateToSettings = { navigateTo(ProfileScreenType.Settings) },
                            onNavigateToAddresses = { navigateTo(ProfileScreenType.Addresses) },
                            onNavigateToHelp = { navigateTo(ProfileScreenType.Help) },
                            onNavigateToReviews = { navigateTo(ProfileScreenType.MyReviews) }
                        )
                    }
                    ProfileScreenType.Settings -> {
                        SettingsContent(
                            userName = userName,
                            userPhone = userPhone,
                            userEmail = userEmail,
                            notificationsEnabled = notificationsEnabled,
                            emailEnabled = emailEnabled,
                            language = language,
                            theme = theme,
                            onNotificationsChange = { notificationsEnabled = it },
                            onEmailChange = { emailEnabled = it },
                            onLanguageChange = { language = it },
                            onThemeChange = { theme = it },
                            onEditProfile = { navigateTo(ProfileScreenType.EditProfile) },
                            onLogout = onLogout
                        )
                    }
                    ProfileScreenType.EditProfile -> {
                        EditProfileContent(
                            userName = userName,
                            userPhone = userPhone,
                            userEmail = userEmail,
                            onSave = { name, phone ->
                                coroutineScope.launch {
                                    saveUser(userEmail, name, phone, db)
                                    userName = name
                                    userPhone = phone
                                    navigateBack()
                                }
                            }
                        )
                    }
                    ProfileScreenType.Addresses -> {
                        AddressesContent(
                            addresses = addresses,
                            onAddAddress = { navigateTo(ProfileScreenType.AddAddress) },
                            onEditAddress = { address ->
                                selectedAddressForEdit = address
                                navigateTo(ProfileScreenType.EditAddress)
                            },
                            onDeleteAddress = { address ->
                                addressToDelete = address
                                showDeleteDialog = true
                            },
                            onSetDefaultAddress = { address ->
                                coroutineScope.launch {
                                    try {
                                        setDefaultAddress(userEmail, address.id, db)
                                        Toast.makeText(context, "Основной адрес изменен", Toast.LENGTH_SHORT).show()
                                        loadAddresses(userEmail, db) { addresses = it }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        )
                    }
                    ProfileScreenType.AddAddress -> {
                        AddEditAddressContent(
                            address = null,
                            onSave = { newAddress ->
                                coroutineScope.launch {
                                    try {
                                        addAddress(userEmail, newAddress, db)
                                        Toast.makeText(context, "Адрес добавлен", Toast.LENGTH_SHORT).show()
                                        loadAddresses(userEmail, db) { addresses = it }
                                        navigateBack()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        )
                    }
                    ProfileScreenType.EditAddress -> {
                        selectedAddressForEdit?.let { address ->
                            AddEditAddressContent(
                                address = address,
                                onSave = { updatedAddress ->
                                    coroutineScope.launch {
                                        try {
                                            updateAddress(userEmail, updatedAddress, db)
                                            Toast.makeText(context, "Адрес обновлен", Toast.LENGTH_SHORT).show()
                                            loadAddresses(userEmail, db) { addresses = it }
                                            navigateBack()
                                            selectedAddressForEdit = null
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            )
                        }
                    }
                    ProfileScreenType.Help -> {
                        HelpContent(
                            onNavigateToFAQ = { navigateTo(ProfileScreenType.FAQ) },
                            onNavigateToContactSupport = { navigateTo(ProfileScreenType.ContactSupport) },
                            onNavigateToTerms = { navigateTo(ProfileScreenType.TermsAndConditions) },
                            onNavigateToAbout = { navigateTo(ProfileScreenType.AboutApp) }
                        )
                    }
                    ProfileScreenType.FAQ -> FAQContent()
                    ProfileScreenType.ContactSupport -> {
                        ContactSupportContent(
                            userEmail = userEmail,
                            userName = userName
                        )
                    }
                    ProfileScreenType.TermsAndConditions -> TermsContent()
                    ProfileScreenType.AboutApp -> AboutContent()
                    ProfileScreenType.MyReviews -> {
                        MyReviewsContent(
                            userEmail = userEmail,
                            userName = userName,
                            onEditReview = { review ->
                                selectedReviewForEdit = review
                                navigateTo(ProfileScreenType.EditReview)
                            }
                        )
                    }
                    ProfileScreenType.EditReview -> {
                        selectedReviewForEdit?.let { review ->
                            EditReviewContent(
                                review = review,
                                onSave = { rating, comment ->
                                    coroutineScope.launch {
                                        try {
                                            updateReviewInFirebase(review.id, rating, comment)
                                            Toast.makeText(context, "Отзыв обновлен", Toast.LENGTH_SHORT).show()
                                            navigateBack()
                                            selectedReviewForEdit = null
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Ошибка при обновлении", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onDelete = {
                                    coroutineScope.launch {
                                        try {
                                            deleteReviewFromFirebase(review.id)
                                            Toast.makeText(context, "Отзыв удален", Toast.LENGTH_SHORT).show()
                                            navigateBack()
                                            selectedReviewForEdit = null
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Ошибка при удалении", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onBack = {
                                    navigateBack()
                                    selectedReviewForEdit = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== FIRESTORE FUNCTIONS ====================

// --- Адреса ---
private fun loadAddresses(email: String, db: FirebaseFirestore, onResult: (List<Address>) -> Unit) {
    db.collection("users").document(email).collection("address").get()
        .addOnSuccessListener { documents ->
            val addresses = documents.mapNotNull { doc ->
                try {
                    Address(
                        id = doc.id,
                        address = doc.getString("address") ?: return@mapNotNull null,
                        apartment = doc.getString("apartment") ?: "",
                        entrance = doc.getString("entrance") ?: "",
                        floor = doc.getString("floor") ?: "",
                        intercom = doc.getString("intercom") ?: "",
                        isDefault = doc.getBoolean("isDefault") ?: false
                    )
                } catch (e: Exception) { null }
            }.sortedByDescending { it.isDefault }
            onResult(addresses)
        }
        .addOnFailureListener { onResult(emptyList()) }
}

private suspend fun setDefaultAddress(email: String, addressId: String, db: FirebaseFirestore) {
    val snapshot = db.collection("users").document(email).collection("address")
        .whereEqualTo("isDefault", true).get().await()
    for (doc in snapshot.documents) {
        doc.reference.update("isDefault", false).await()
    }
    db.collection("users").document(email).collection("address").document(addressId)
        .update("isDefault", true).await()
}

private suspend fun addAddress(email: String, address: Address, db: FirebaseFirestore) {
    if (address.isDefault) {
        val snapshot = db.collection("users").document(email).collection("address")
            .whereEqualTo("isDefault", true).get().await()
        for (doc in snapshot.documents) {
            doc.reference.update("isDefault", false).await()
        }
    }
    val data = hashMapOf(
        "address" to address.address,
        "apartment" to address.apartment,
        "entrance" to address.entrance,
        "floor" to address.floor,
        "intercom" to address.intercom,
        "isDefault" to address.isDefault,
        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
    )
    db.collection("users").document(email).collection("address").add(data).await()
}

private suspend fun updateAddress(email: String, address: Address, db: FirebaseFirestore) {
    if (address.isDefault) {
        val snapshot = db.collection("users").document(email).collection("address")
            .whereEqualTo("isDefault", true).get().await()
        for (doc in snapshot.documents) {
            if (doc.id != address.id) {
                doc.reference.update("isDefault", false).await()
            }
        }
    }
    val data = hashMapOf(
        "address" to address.address,
        "apartment" to address.apartment,
        "entrance" to address.entrance,
        "floor" to address.floor,
        "intercom" to address.intercom,
        "isDefault" to address.isDefault
    )
    db.collection("users").document(email).collection("address").document(address.id).set(data).await()
}

private suspend fun deleteAddress(email: String, addressId: String, db: FirebaseFirestore) {
    db.collection("users").document(email).collection("address").document(addressId).delete().await()
}

private suspend fun saveUser(email: String, name: String, phone: String, db: FirebaseFirestore) {
    val data = hashMapOf(
        "email" to email,
        "name" to name,
        "phone" to phone,
        "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
    )
    db.collection("users").document(email).set(data, com.google.firebase.firestore.SetOptions.merge()).await()
}

// --- Отзывы (НОВЫЕ ФУНКЦИИ) ---

// Загрузка отзывов пользователя из Firebase (ИСПРАВЛЕНО)
private suspend fun loadUserReviews(userEmail: String): List<Review> {
    val db = FirebaseFirestore.getInstance()
    return try {
        android.util.Log.d("ProfileScreen", "Загрузка отзывов для: $userEmail")

        val snapshot = db.collection("reviews")
            .whereEqualTo("userEmail", userEmail)
            .get()
            .await()

        android.util.Log.d("ProfileScreen", "Найдено документов: ${snapshot.size()}")

        val reviews = snapshot.documents.mapNotNull { doc: com.google.firebase.firestore.DocumentSnapshot ->
            try {
                val data = doc.data
                android.util.Log.d("ProfileScreen", "Документ ${doc.id}: $data")

                Review(
                    id = doc.id,
                    userName = doc.getString("userName") ?: "",
                    userEmail = doc.getString("userEmail") ?: "",
                    rating = doc.getDouble("rating") ?: 0.0,
                    comment = doc.getString("comment") ?: "",
                    date = doc.getString("date") ?: "",
                    dishId = doc.getString("dishId") ?: "",
                    dishName = doc.getString("dishName") ?: "",
                    restaurantId = doc.getString("restaurantId") ?: ""
                )
            } catch (e: Exception) {
                android.util.Log.e("ProfileScreen", "Ошибка парсинга документа ${doc.id}: ${e.message}")
                null
            }
        }

        // Сортируем вручную по дате (от новых к старым)
        val sortedReviews = reviews.sortedByDescending { review: Review ->
            try {
                val parts = review.date.split(" ")
                val dateParts = parts[0].split(".")
                val timeParts = if (parts.size > 1) parts[1].split(":") else listOf("0", "0")

                val day = dateParts[0].toIntOrNull() ?: 1
                val month = dateParts[1].toIntOrNull() ?: 1
                val year = dateParts[2].toIntOrNull() ?: 2024
                val hour = timeParts[0].toIntOrNull() ?: 0
                val minute = timeParts[1].toIntOrNull() ?: 0

                year * 100000000L + month * 1000000L + day * 10000L + hour * 100L + minute
            } catch (e: Exception) {
                0L
            }
        }

        android.util.Log.d("ProfileScreen", "Итоговое количество отзывов: ${sortedReviews.size}")
        sortedReviews
    } catch (e: Exception) {
        android.util.Log.e("ProfileScreen", "Ошибка загрузки: ${e.message}")
        e.printStackTrace()
        emptyList()
    }
}

private suspend fun updateReviewInFirebase(reviewId: String, rating: Float, comment: String) {
    val db = FirebaseFirestore.getInstance()
    val currentDate = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(java.util.Date())

    val updates = hashMapOf<String, Any>(
        "rating" to rating.toDouble(),
        "comment" to comment,
        "date" to currentDate
    )

    db.collection("reviews").document(reviewId).update(updates).await()

    // Обновляем рейтинг блюда
    val reviewDoc = db.collection("reviews").document(reviewId).get().await()
    val dishId = reviewDoc.getString("dishId") ?: return
    updateDishRating(dishId)
}

// Удаление отзыва из Firebase
private suspend fun deleteReviewFromFirebase(reviewId: String) {
    val db = FirebaseFirestore.getInstance()

    // Получаем dishId перед удалением
    val reviewDoc = db.collection("reviews").document(reviewId).get().await()
    val dishId = reviewDoc.getString("dishId") ?: ""

    // Удаляем отзыв
    db.collection("reviews").document(reviewId).delete().await()

    // Удаляем ID отзыва из блюда и обновляем рейтинг
    if (dishId.isNotBlank()) {
        db.collection("dishes").document(dishId)
            .update("reviewsIds", com.google.firebase.firestore.FieldValue.arrayRemove(reviewId))
            .await()
        updateDishRating(dishId)
    }
}

// Обновление среднего рейтинга блюда
private suspend fun updateDishRating(dishId: String) {
    val db = FirebaseFirestore.getInstance()

    try {
        val dishDoc = db.collection("dishes").document(dishId).get().await()
        val reviewsIds = dishDoc.get("reviewsIds") as? List<String> ?: emptyList()

        if (reviewsIds.isNotEmpty()) {
            var totalRating = 0.0
            var count = 0

            for (reviewId in reviewsIds) {
                try {
                    val reviewDoc = db.collection("reviews").document(reviewId).get().await()
                    val rating = reviewDoc.getDouble("rating") ?: 0.0
                    totalRating += rating
                    count++
                } catch (e: Exception) {
                    // Пропускаем удаленные отзывы
                }
            }

            val avgRating = if (count > 0) totalRating / count else 0.0
            db.collection("dishes").document(dishId).update(
                mapOf(
                    "ratingAverage" to avgRating,
                    "ratingCount" to count
                )
            ).await()
        } else {
            db.collection("dishes").document(dishId).update(
                mapOf(
                    "ratingAverage" to 0.0,
                    "ratingCount" to 0
                )
            ).await()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// ==================== SCREEN CONTENTS ====================

@Composable
fun MainProfileContent(
    userName: String,
    userEmail: String,
    userPhone: String,
    addresses: List<Address>,
    onNavigateToOrders: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAddresses: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToReviews: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ProfileHeader(userName = userName, userEmail = userEmail, userPhone = userPhone)
        Spacer(modifier = Modifier.height(8.dp))

        ProfileMenuItem(
            icon = Icons.Outlined.ShoppingBag,
            title = "Мои заказы",
            subtitle = "История и статус заказов",
            onClick = onNavigateToOrders
        )
        ProfileMenuItem(
            icon = Icons.Outlined.RateReview,
            title = "Мои отзывы",
            subtitle = "Ваши отзывы о блюдах",
            onClick = onNavigateToReviews
        )
        ProfileMenuItem(
            icon = Icons.Outlined.LocationOn,
            title = "Адреса доставки",
            subtitle = when {
                addresses.isEmpty() -> "Добавьте адрес"
                addresses.size == 1 -> "1 адрес"
                addresses.size in 2..4 -> "${addresses.size} адреса"
                else -> "${addresses.size} адресов"
            },
            onClick = onNavigateToAddresses
        )
        ProfileMenuItem(
            icon = Icons.Outlined.Payment,
            title = "Способы оплаты",
            subtitle = "Карты, наличные",
            onClick = { }
        )
        ProfileMenuItem(
            icon = Icons.Outlined.Help,
            title = "Помощь",
            subtitle = "FAQ и поддержка",
            onClick = onNavigateToHelp
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "FastBite v1.0.0",
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun ProfileHeader(userName: String, userEmail: String, userPhone: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(70.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (userName.isNotBlank()) userName.take(1).uppercase() else userEmail.take(1).uppercase(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (userName.isNotBlank()) userName else "Пользователь",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = userEmail,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                if (userPhone.isNotBlank()) {
                    Text(
                        text = userPhone,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    badge: String? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (badge != null) {
                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                        Text(text = badge, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun SettingsContent(
    userName: String, userPhone: String, userEmail: String,
    notificationsEnabled: Boolean, emailEnabled: Boolean, language: String, theme: String,
    onNotificationsChange: (Boolean) -> Unit, onEmailChange: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit, onThemeChange: (String) -> Unit,
    onEditProfile: () -> Unit, onLogout: () -> Unit
) {
    var showLogoutConfirm by remember { mutableStateOf(false) }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Выход из аккаунта", fontWeight = FontWeight.Bold) },
            text = { Text("Вы уверены, что хотите выйти?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = { showLogoutConfirm = false; onLogout() }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Выйти")
                }
            },
            dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text("Отмена") } },
            shape = RoundedCornerShape(28.dp)
        )
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        SettingsItem(icon = Icons.Outlined.Person, title = "Редактировать профиль", subtitle = if (userName.isNotBlank() || userPhone.isNotBlank()) "$userName • $userPhone" else "Заполните информацию", onClick = onEditProfile)
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
        SettingsSwitchItem(icon = Icons.Outlined.Notifications, title = "Уведомления", subtitle = "Получать уведомления о заказах", checked = notificationsEnabled, onCheckedChange = onNotificationsChange)
        SettingsSwitchItem(icon = Icons.Outlined.Email, title = "Email-рассылка", subtitle = "Получать новости и акции", checked = emailEnabled, onCheckedChange = onEmailChange)
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
        SettingsSelectItem(icon = Icons.Outlined.Language, title = "Язык", value = language, onClick = {})
        SettingsSelectItem(icon = Icons.Outlined.DarkMode, title = "Тема", value = theme, onClick = {})
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
        SettingsItem(icon = Icons.Outlined.Logout, title = "Выйти из аккаунта", subtitle = "Завершить сеанс", onClick = { showLogoutConfirm = true }, isDestructive = true)
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit, isDestructive: Boolean = false) {
    val contentColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), color = Color.Transparent, shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = contentColor)
                    Text(text = subtitle, fontSize = 13.sp, color = if (isDestructive) contentColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = if (isDestructive) contentColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun SettingsSwitchItem(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary))
    }
}

@Composable
fun SettingsSelectItem(icon: ImageVector, title: String, value: String, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), color = Color.Transparent, shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 8.dp))
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun EditProfileContent(userName: String, userPhone: String, userEmail: String, onSave: (String, String) -> Unit) {
    var name by remember { mutableStateOf(userName) }
    var phone by remember { mutableStateOf(userPhone) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Имя") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(16.dp), leadingIcon = { Icon(Icons.Outlined.Person, null, tint = MaterialTheme.colorScheme.primary) })
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Телефон") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(16.dp), leadingIcon = { Icon(Icons.Outlined.Phone, null, tint = MaterialTheme.colorScheme.primary) })
            OutlinedTextField(value = userEmail, onValueChange = {}, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(16.dp), enabled = false, leadingIcon = { Icon(Icons.Outlined.Email, null, tint = MaterialTheme.colorScheme.primary) })
        }
        Button(onClick = { onSave(name, phone) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("Сохранить") }
    }
}

@Composable
fun AddressesContent(
    addresses: List<Address>,
    onAddAddress: () -> Unit,
    onEditAddress: (Address) -> Unit,
    onDeleteAddress: (Address) -> Unit,
    onSetDefaultAddress: (Address) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Сохраненные адреса", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 16.dp))

        if (addresses.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.LocationOn, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Text("У вас пока нет сохраненных адресов", fontSize = 16.sp, color = MaterialTheme.colorScheme.outline)
                    Text("Добавьте адрес для быстрого оформления заказа", fontSize = 14.sp, color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                items(addresses) { address ->
                    AddressCard(address = address, onEdit = { onEditAddress(address) }, onDelete = { onDeleteAddress(address) }, onSetDefault = { onSetDefaultAddress(address) })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAddAddress, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Outlined.Add, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Добавить новый адрес")
        }
    }
}

@Composable
fun AddressCard(address: Address, onEdit: () -> Unit, onDelete: () -> Unit, onSetDefault: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Text(text = address.address, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) { Icon(Icons.Outlined.Edit, "Редактировать", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) { Icon(Icons.Outlined.Delete, "Удалить", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }
                }
            }
            val details = mutableListOf<String>()
            if (address.apartment.isNotBlank()) details.add("кв. ${address.apartment}")
            if (address.entrance.isNotBlank()) details.add("под. ${address.entrance}")
            if (address.floor.isNotBlank()) details.add("эт. ${address.floor}")
            if (address.intercom.isNotBlank()) details.add("домофон ${address.intercom}")
            if (details.isNotEmpty()) {
                Text(text = details.joinToString(" • "), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.padding(start = 36.dp, top = 4.dp))
            }
            Row(modifier = Modifier.fillMaxWidth().padding(start = 36.dp, top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                if (address.isDefault) {
                    Badge(containerColor = MaterialTheme.colorScheme.primary) { Text("Основной адрес", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimary) }
                } else {
                    TextButton(onClick = onSetDefault, modifier = Modifier.height(32.dp)) { Text("Сделать основным", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary) }
                }
            }
        }
    }
}

@Composable
fun AddEditAddressContent(address: Address?, onSave: (Address) -> Unit) {
    var addressText by remember { mutableStateOf(address?.address ?: "") }
    var apartment by remember { mutableStateOf(address?.apartment ?: "") }
    var entrance by remember { mutableStateOf(address?.entrance ?: "") }
    var floor by remember { mutableStateOf(address?.floor ?: "") }
    var intercom by remember { mutableStateOf(address?.intercom ?: "") }
    var isDefault by remember { mutableStateOf(address?.isDefault ?: false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = if (address == null) "Новый адрес" else "Редактировать адрес", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            OutlinedTextField(value = addressText, onValueChange = { addressText = it }, label = { Text("Адрес *") }, placeholder = { Text("Город, улица, дом") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(16.dp), isError = addressText.isBlank(), leadingIcon = { Icon(Icons.Outlined.LocationOn, null, tint = MaterialTheme.colorScheme.primary) })
            if (addressText.isBlank()) { Text(text = "Адрес обязателен для заполнения", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp)) }
            OutlinedTextField(value = apartment, onValueChange = { apartment = it }, label = { Text("Квартира/офис") }, placeholder = { Text("Необязательно") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(16.dp))
            OutlinedTextField(value = entrance, onValueChange = { entrance = it }, label = { Text("Подъезд") }, placeholder = { Text("Необязательно") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(16.dp))
            OutlinedTextField(value = floor, onValueChange = { floor = it }, label = { Text("Этаж") }, placeholder = { Text("Необязательно") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(16.dp))
            OutlinedTextField(value = intercom, onValueChange = { intercom = it }, label = { Text("Домофон") }, placeholder = { Text("Необязательно") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(16.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Сделать основным адресом", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Switch(checked = isDefault, onCheckedChange = { isDefault = it }, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onSave(Address(id = address?.id ?: System.currentTimeMillis().toString(), address = addressText, apartment = apartment, entrance = entrance, floor = floor, intercom = intercom, isDefault = isDefault)) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = addressText.isNotBlank()
        ) { Text(if (address == null) "Сохранить" else "Обновить") }
    }
}

// ==================== HELP SCREENS ====================

@Composable
fun HelpContent(
    onNavigateToFAQ: () -> Unit,
    onNavigateToContactSupport: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text(text = "Чем мы можем помочь?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "Выберите интересующий вас раздел", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 24.dp))

        HelpCard(icon = Icons.Outlined.QuestionAnswer, title = "Часто задаваемые вопросы", description = "Ответы на популярные вопросы", color = MaterialTheme.colorScheme.primary, onClick = onNavigateToFAQ)
        Spacer(modifier = Modifier.height(12.dp))
        HelpCard(icon = Icons.Outlined.SupportAgent, title = "Связаться с поддержкой", description = "Напишите нам, и мы поможем", color = MaterialTheme.colorScheme.tertiary, onClick = onNavigateToContactSupport)
        Spacer(modifier = Modifier.height(12.dp))
        HelpCard(icon = Icons.Outlined.Description, title = "Правила и условия", description = "Условия использования сервиса", color = MaterialTheme.colorScheme.secondary, onClick = onNavigateToTerms)
        Spacer(modifier = Modifier.height(12.dp))
        HelpCard(icon = Icons.Outlined.Info, title = "О приложении", description = "Версия 1.0.0 • Политика конфиденциальности", color = MaterialTheme.colorScheme.outline, onClick = onNavigateToAbout)

        Spacer(modifier = Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(text = "Контактная информация", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
                ContactInfoRow(icon = Icons.Outlined.Email, text = "support@fastbite.com")
                ContactInfoRow(icon = Icons.Outlined.Phone, text = "+7 (999) 123-45-67")
                ContactInfoRow(icon = Icons.Outlined.Schedule, text = "Ежедневно с 10:00 до 22:00")
            }
        }
    }
}

@Composable
fun HelpCard(icon: ImageVector, title: String, description: String, color: Color, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ContactInfoRow(icon: ImageVector, text: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun FAQContent() {
    val faqItems = listOf(
        FAQItem("Как сделать заказ?", "Выберите ресторан, добавьте блюда в корзину, укажите адрес доставки и выберите способ оплаты."),
        FAQItem("Сколько времени занимает доставка?", "Среднее время доставки составляет 30-60 минут."),
        FAQItem("Как оплатить заказ?", "Наличными курьеру, банковской картой при получении или онлайн."),
        FAQItem("Можно ли изменить или отменить заказ?", "Да, до того, как ресторан начал его готовить."),
        FAQItem("Что делать, если заказ не привезли вовремя?", "Свяжитесь с поддержкой через чат или по телефону."),
        FAQItem("Как оставить отзыв о блюде?", "После получения заказа в разделе 'Мои заказы'."),
        FAQItem("Безопасно ли платить онлайн?", "Да, все платежи защищены шифрованием."),
        FAQItem("Как изменить личные данные?", "Профиль → Настройки → Редактировать профиль.")
    )

    var expandedIndex by remember { mutableStateOf(-1) }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(faqItems.size) { index ->
            FAQCard(item = faqItems[index], isExpanded = expandedIndex == index, onExpandChange = { expandedIndex = if (expandedIndex == index) -1 else index })
        }
    }
}

@Composable
fun FAQCard(item: FAQItem, isExpanded: Boolean, onExpandChange: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onExpandChange), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = if (isExpanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = item.question, fontSize = 16.sp, fontWeight = if (isExpanded) FontWeight.Bold else FontWeight.Medium, color = if (isExpanded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                Icon(if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null, tint = if (isExpanded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = item.answer, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun ContactSupportContent(userEmail: String, userName: String) {
    val context = LocalContext.current
    var message by remember { mutableStateOf("") }
    var selectedTopic by remember { mutableStateOf(SupportTopic.ORDER) }
    var showTopicDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Напишите нам", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 8.dp))
        Text(text = "Опишите вашу проблему, и мы ответим в ближайшее время", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 24.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (userName.isNotBlank()) userName else userEmail, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Тема обращения", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 8.dp))

        Card(modifier = Modifier.fillMaxWidth().clickable { showTopicDialog = true }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = selectedTopic.title, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Icon(Icons.Default.ArrowDropDown, "Выбрать тему", tint = MaterialTheme.colorScheme.primary)
            }
        }

        if (showTopicDialog) {
            AlertDialog(onDismissRequest = { showTopicDialog = false }, title = { Text("Выберите тему", fontWeight = FontWeight.Bold) }, text = {
                Column { SupportTopic.values().forEach { topic ->
                    Surface(modifier = Modifier.fillMaxWidth().clickable { selectedTopic = topic; showTopicDialog = false }, color = if (selectedTopic == topic) MaterialTheme.colorScheme.primaryContainer else Color.Transparent) {
                        Text(text = topic.title, modifier = Modifier.padding(16.dp), color = if (selectedTopic == topic) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                    }
                } }
            }, confirmButton = { TextButton(onClick = { showTopicDialog = false }) { Text("Отмена") } }, shape = RoundedCornerShape(28.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Сообщение") }, placeholder = { Text("Опишите вашу проблему подробнее...") }, modifier = Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(16.dp), minLines = 8)

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (message.isNotBlank()) {
                Toast.makeText(context, "Сообщение отправлено! Мы ответим вам на $userEmail", Toast.LENGTH_LONG).show()
                message = ""
            } else {
                Toast.makeText(context, "Пожалуйста, напишите сообщение", Toast.LENGTH_SHORT).show()
            }
        }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = message.isNotBlank()) {
            Icon(Icons.Outlined.Send, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Отправить")
        }
    }
}

@Composable
fun TermsContent() {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { TermsSection(title = "1. Общие положения", icon = Icons.Outlined.Info, content = listOf("1.1. Используя приложение FastBite, вы соглашаетесь с настоящими Правилами.", "1.2. FastBite предоставляет платформу для заказа еды из ресторанов-партнеров.", "1.3. Мы оставляем за собой право изменять правила в любое время.")) }
        item { TermsSection(title = "2. Регистрация и аккаунт", icon = Icons.Outlined.Person, content = listOf("2.1. Для оформления заказа необходима регистрация.", "2.2. Вы несете ответственность за сохранность своих учетных данных.")) }
        item { TermsSection(title = "3. Оформление заказа", icon = Icons.Outlined.ShoppingCart, content = listOf("3.1. Оформляя заказ, вы подтверждаете правильность информации.", "3.2. Цены в приложении могут отличаться от цен в ресторане.")) }
        item { TermsSection(title = "4. Оплата", icon = Icons.Outlined.Payment, content = listOf("4.1. Доступны способы оплаты: наличные, карты, Apple Pay, Google Pay.", "4.2. Возврат средств осуществляется в течение 3-10 рабочих дней.")) }
        item { TermsSection(title = "5. Доставка", icon = Icons.Outlined.DeliveryDining, content = listOf("5.1. Доставка осуществляется по указанному адресу.", "5.2. Минимальная сумма заказа зависит от ресторана.")) }
        item { TermsSection(title = "6. Контактная информация", icon = Icons.Outlined.SupportAgent, content = listOf("Email: support@fastbite.com", "Телефон: +7 (999) 123-45-67")) }
        item {
            Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(text = "Последнее обновление: 15 марта 2024 г.", modifier = Modifier.fillMaxWidth().padding(16.dp), textAlign = TextAlign.Center, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun TermsSection(title: String, icon: ImageVector, content: List<String>) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            content.forEach { text -> Text(text = text, fontSize = 14.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp)) }
        }
    }
}

@Composable
fun AboutContent() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Card(modifier = Modifier.size(120.dp), shape = CircleShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "FB", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "FastBite", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(text = "Версия 1.0.0 (Build 100)", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        Spacer(modifier = Modifier.height(24.dp))
        AboutSection(title = "О нас", icon = Icons.Outlined.Info, content = "FastBite - это сервис доставки еды из лучших ресторанов вашего города.")
        AboutSection(title = "Наша миссия", icon = Icons.Outlined.EmojiObjects, content = "Делать вкусную еду доступной каждому в любое время.")
        AboutSection(title = "Контакты", icon = Icons.Outlined.Email, content = "Email: dev@fastbite.com\nСайт: www.fastbite.com")
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "© 2024 FastBite. Все права защищены.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun AboutSection(title: String, icon: ImageVector, content: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Text(text = content, fontSize = 14.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ==================== REVIEWS SCREENS (ИСПОЛЬЗУЮТ FIREBASE) ====================

@Composable
fun MyReviewsContent(userEmail: String, userName: String, onEditReview: (Review) -> Unit) {
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var reviewToDelete by remember { mutableStateOf<Review?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(userEmail) {
        isLoading = true
        reviews = loadUserReviews(userEmail)
        isLoading = false
    }

    fun refreshReviews() {
        coroutineScope.launch {
            isLoading = true
            reviews = loadUserReviews(userEmail)
            isLoading = false
        }
    }

    if (showDeleteDialog && reviewToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; reviewToDelete = null },
            title = { Text("Удалить отзыв", fontWeight = FontWeight.Bold) },
            text = { Text("Вы уверены, что хотите удалить этот отзыв?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                reviewToDelete?.let { review ->
                                    deleteReviewFromFirebase(review.id)
                                    Toast.makeText(context, "Отзыв удален", Toast.LENGTH_SHORT).show()
                                    refreshReviews()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Ошибка при удалении", Toast.LENGTH_SHORT).show()
                            } finally {
                                showDeleteDialog = false
                                reviewToDelete = null
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; reviewToDelete = null }) { Text("Отмена") }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = when {
                    isLoading -> "Загрузка отзывов..."
                    reviews.isEmpty() -> "У вас пока нет отзывов"
                    reviews.size == 1 -> "1 отзыв"
                    reviews.size in 2..4 -> "${reviews.size} отзыва"
                    else -> "${reviews.size} отзывов"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (reviews.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Outlined.RateReview,
                        null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        "Нет отзывов",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Ваши отзывы о блюдах будут отображаться здесь",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reviews, key = { it.id }) { review ->
                    MyReviewCard(
                        review = review,
                        onEdit = { onEditReview(review) },
                        onDelete = {
                            reviewToDelete = review
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
}
@Composable
fun MyReviewCard(review: Review, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.dishName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Outlined.Edit,
                            "Редактировать",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Outlined.Delete,
                            "Удалить",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(5) { index ->
                    Icon(
                        if (index < review.rating) Icons.Filled.Star else Icons.Outlined.Star,
                        null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "%.1f".format(review.rating),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFFC107),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (review.comment.isNotBlank()) {
                Text(
                    text = review.comment,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.date,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun EditReviewContent(
    review: Review,
    onSave: (Float, String) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    var rating by remember { mutableStateOf(review.rating.toFloat()) }
    var comment by remember { mutableStateOf(review.comment) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Заголовок
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Назад")
            }
            Text(
                "Редактировать отзыв",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Информация о блюде
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = review.dishName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Дата: ${review.date}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Оценка
            Text(
                text = "Ваша оценка",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) { index ->
                    IconButton(
                        onClick = { rating = (index + 1).toFloat() },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            if (index < rating) Icons.Filled.Star else Icons.Outlined.Star,
                            "${index + 1} звезд",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
            Text(
                text = when (rating.toInt()) {
                    1 -> "Очень плохо"
                    2 -> "Плохо"
                    3 -> "Нормально"
                    4 -> "Хорошо"
                    5 -> "Отлично"
                    else -> ""
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFFFC107),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Комментарий
            Text(
                text = "Ваш комментарий",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                placeholder = { Text("Расскажите о ваших впечатлениях...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Кнопки
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Отмена")
                }

                Button(
                    onClick = { onSave(rating, comment) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = rating > 0
                ) {
                    Text("Сохранить")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка удаления
            OutlinedButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Outlined.Delete, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Удалить отзыв")
            }
        }
    }

    // Диалог подтверждения удаления
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Удалить отзыв", fontWeight = FontWeight.Bold) },
            text = { Text("Вы уверены, что хотите удалить этот отзыв? Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Отмена")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}