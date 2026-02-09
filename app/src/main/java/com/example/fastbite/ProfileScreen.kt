package com.example.fastbite

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class UserProfile(
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var address: String = ""
)

data class Order(
    val id: String = "",
    val orderNumber: String = "",
    val status: String = "",
    val totalPrice: Double = 0.0,
    val totalItems: Int = 0,
    val createdAt: Date = Date(),
    val deliveryAddress: String = "",
    val paymentMethod: String = "",
    val restaurantName: String = ""
) {
    fun getStatusText(): String {
        return when (status) {
            "NEW" -> "🆕 Новый"
            "PROCESSING" -> "🔄 Обрабатывается"
            "PREPARING" -> "👨‍🍳 Готовится"
            "READY" -> "✅ Готов к доставке"
            "DELIVERING" -> "🚚 Доставляется"
            "DELIVERED" -> "🎉 Доставлен"
            "CANCELLED" -> "❌ Отменен"
            else -> status
        }
    }

    fun getStatusColor(): Color {
        return when (status) {
            "NEW" -> Color(0xFF2196F3) // Синий
            "PROCESSING" -> Color(0xFF4CAF50) // Зеленый
            "PREPARING" -> Color(0xFFFF9800) // Оранжевый
            "READY" -> Color(0xFF8BC34A) // Светло-зеленый
            "DELIVERING" -> Color(0xFF3F51B5) // Индиго
            "DELIVERED" -> Color(0xFF009688) // Бирюзовый
            "CANCELLED" -> Color(0xFFF44336) // Красный
            else -> Color.Gray
        }
    }
}

// Вспомогательная функция для создания Order из документа
private fun createOrderFromDocument(document: DocumentSnapshot): Order? {
    return try {
        Order(
            id = document.id,
            orderNumber = document.getString("orderNumber") ?: document.getString("orderId") ?: document.id.take(8).uppercase(),
            status = document.getString("status") ?: "NEW",
            totalPrice = document.getDouble("totalPrice") ?: 0.0,
            totalItems = document.getLong("totalItems")?.toInt() ?: 0,
            createdAt = document.getDate("createdAt") ?: document.getDate("timestamp") ?: Date(),
            deliveryAddress = document.getString("deliveryAddress") ?: "",
            paymentMethod = document.getString("paymentMethod") ?: "CASH",
            restaurantName = document.getString("restaurantName") ?: document.getString("restaurant") ?: "FastBite"
        )
    } catch (e: Exception) {
        null
    }
}

@Composable
fun ProfileScreen(
    userEmail: String,
    onLogout: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var showSaveSuccess by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf("Профиль", "История заказов")

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Инициализируем Firebase
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Состояния для профиля
    val userProfile = remember { mutableStateOf(UserProfile(email = userEmail)) }

    // Состояния для редактирования
    var editedName by remember { mutableStateOf("") }
    var editedPhone by remember { mutableStateOf("") }
    var editedAddress by remember { mutableStateOf("") }

    // Состояния для заказов
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoadingOrders by remember { mutableStateOf(false) }

    // Функция для загрузки профиля из Firebase
    fun loadProfileFromFirebase() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val userId = currentUser.uid
                    val docRef = db.collection("users").document(userId)
                    val document = docRef.get().await()

                    if (document.exists()) {
                        val profile = document.toObject(UserProfile::class.java)
                        profile?.let {
                            userProfile.value = it
                            editedName = it.name
                            editedPhone = it.phone
                            editedAddress = it.address
                        }
                    } else {
                        // Создаем новый профиль если не существует
                        userProfile.value = UserProfile(email = userEmail)
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Ошибка загрузки профиля: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Функция для загрузки заказов из Firebase
    fun loadOrdersFromFirebase() {
        coroutineScope.launch {
            isLoadingOrders = true
            errorMessage = null

            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val userId = currentUser.uid

                    println("Загрузка заказов для пользователя UID: $userId")
                    println("Email пользователя: $userEmail")

                    val ordersList = mutableListOf<Order>()

                    // Основной запрос - по userId (UID)
                    try {
                        // Создаем запрос по userId
                        val query = db.collection("orders")
                            .whereEqualTo("userId", userId)

                        val snapshot = query.get().await()

                        println("Найдено документов по userId: ${snapshot.size()}")

                        for (document in snapshot.documents) {
                            println("Найден документ заказа: ${document.id}")
                            val order = createOrderFromDocument(document)
                            if (order != null) {
                                ordersList.add(order)
                                println("Добавлен заказ: ${order.orderNumber}, статус: ${order.status}")
                            }
                        }
                    } catch (e: Exception) {
                        println("Ошибка при запросе по userId: ${e.message}")
                        errorMessage = "Ошибка загрузки заказов: ${e.message}"
                    }

                    // Альтернативный запрос - по userEmail (для совместимости со старыми заказами)
                    try {
                        val queryByEmail = db.collection("orders")
                            .whereEqualTo("userEmail", userEmail)

                        val snapshotByEmail = queryByEmail.get().await()

                        println("Найдено документов по userEmail: ${snapshotByEmail.size()}")

                        for (document in snapshotByEmail.documents) {
                            val order = createOrderFromDocument(document)
                            if (order != null && !ordersList.any { it.id == order.id }) {
                                ordersList.add(order)
                            }
                        }
                    } catch (e: Exception) {
                        println("Ошибка при запросе по userEmail: ${e.message}")
                    }

                    // Сортируем по дате создания
                    orders = ordersList.sortedByDescending { it.createdAt }

                    if (orders.isEmpty()) {
                        errorMessage = "У вас еще нет заказов. Сделайте первый заказ!"
                        println("Заказов не найдено. Проверьте наличие документов в Firebase.")
                    } else {
                        println("Всего загружено заказов: ${orders.size}")
                    }
                } else {
                    errorMessage = "Пользователь не авторизован"
                }
            } catch (e: Exception) {
                errorMessage = "Ошибка загрузки заказов: ${e.message}"
                orders = emptyList()
            } finally {
                isLoadingOrders = false
            }
        }
    }

    // Функция для отладки Firebase заказов
    fun debugFirebaseOrders() {
        coroutineScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val userId = currentUser.uid

                    println("=== ОТЛАДКА FIREBASE ЗАКАЗОВ ===")
                    println("Текущий пользователь:")
                    println("  UID: $userId")
                    println("  Email: $userEmail")

                    // Получим ВСЕ заказы для проверки
                    val allOrders = db.collection("orders").get().await()
                    println("Всего заказов в коллекции 'orders': ${allOrders.size()}")

                    if (allOrders.isEmpty) {
                        println("⚠ ВНИМАНИЕ: Коллекция 'orders' пуста!")
                        println("Создайте тестовый заказ через приложение")
                        return@launch
                    }

                    var userOrdersCount = 0
                    println("\n=== Поиск заказов текущего пользователя ===")

                    for (document in allOrders.documents) {
                        val docUserId = document.getString("userId") ?: "НЕТ"
                        val docUserEmail = document.getString("userEmail") ?: "НЕТ"
                        val docOrderNumber = document.getString("orderNumber") ?: "БЕЗ НОМЕРА"

                        // Проверяем, относится ли заказ к текущему пользователю
                        val isUserOrder = docUserId == userId || docUserEmail == userEmail

                        if (isUserOrder) {
                            userOrdersCount++
                            println("✅ НАЙДЕН ЗАКАЗ ПОЛЬЗОВАТЕЛЯ:")
                            println("   Документ ID: ${document.id}")
                            println("   Номер заказа: $docOrderNumber")
                            println("   userId в документе: $docUserId")
                            println("   userEmail в документе: $docUserEmail")
                            println("   Статус: ${document.getString("status") ?: "НЕТ"}")
                            println("   Сумма: ${document.getDouble("totalPrice") ?: 0.0}")
                            println("   Дата: ${document.getDate("createdAt") ?: "НЕТ"}")
                            println("   ---")
                        }
                    }

                    println("\n=== ИТОГИ ===")
                    println("Всего заказов у пользователя: $userOrdersCount")

                    if (userOrdersCount == 0) {
                        println("\n⚠ ПРОБЛЕМА: Заказы текущего пользователя не найдены!")
                        println("\nПРОВЕРЬТЕ В КОНСОЛИ FIREBASE:")
                        println("1. Откройте Firestore Database")
                        println("2. Перейдите в коллекцию 'orders'")
                        println("3. Проверьте наличие документов")
                        println("4. Убедитесь, что в документах есть поля:")
                        println("   - userId: должно быть '$userId'")
                        println("   - userEmail: должно быть '$userEmail'")
                        println("\nЕсли документов нет - создайте заказ через приложение")
                    }
                }
            } catch (e: Exception) {
                println("Ошибка отладки: ${e.message}")
            }
        }
    }

    // Загружаем профиль и заказы при первом отображении
    LaunchedEffect(Unit) {
        loadProfileFromFirebase()
        loadOrdersFromFirebase()
    }

    // Обновляем заказы при переключении на вкладку истории
    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) {
            loadOrdersFromFirebase()
        }
    }

    // Функция для сохранения профиля в Firebase
    fun saveProfileToFirebase() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val userId = currentUser.uid

                    // Обновляем локальный профиль
                    val updatedProfile = UserProfile(
                        name = editedName,
                        email = userEmail,
                        phone = editedPhone,
                        address = editedAddress
                    )

                    // Сохраняем в Firestore
                    db.collection("users").document(userId)
                        .set(updatedProfile)
                        .await()

                    // Обновляем локальное состояние
                    userProfile.value = updatedProfile
                    isEditing = false
                    showSaveSuccess = true
                } else {
                    errorMessage = "Пользователь не авторизован"
                }
            } catch (e: Exception) {
                errorMessage = "Ошибка сохранения: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 🔹 Верхняя панель с заголовком
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Профиль",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            TextButton(onClick = { showLogoutDialog = true }) {
                Text("Выйти", color = MaterialTheme.colorScheme.primary)
            }
        }

        // 🔹 Табы
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> ProfileTabContent(
                userEmail = userEmail,
                userProfile = userProfile.value,
                isEditing = isEditing,
                isLoading = isLoading,
                editedName = editedName,
                editedPhone = editedPhone,
                editedAddress = editedAddress,
                onEditedNameChange = { editedName = it },
                onEditedPhoneChange = { editedPhone = it },
                onEditedAddressChange = { editedAddress = it },
                onEditClick = {
                    isEditing = true
                    editedName = userProfile.value.name
                    editedPhone = userProfile.value.phone
                    editedAddress = userProfile.value.address
                },
                onSaveClick = { saveProfileToFirebase() },
                onCancelClick = {
                    editedName = userProfile.value.name
                    editedPhone = userProfile.value.phone
                    editedAddress = userProfile.value.address
                    isEditing = false
                }
            )

            1 -> OrdersHistoryTabContent(
                orders = orders,
                isLoading = isLoadingOrders,
                onRefresh = { loadOrdersFromFirebase() },
                onDebug = { debugFirebaseOrders() }
            )
        }
    }

    // 🔹 Отображение ошибок
    errorMessage?.let { message ->
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(5000)
            errorMessage = null
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.errorContainer,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                    IconButton(
                        onClick = { errorMessage = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text("×", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }

    // 🔹 Диалог подтверждения выхода
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Выход из аккаунта") },
            text = { Text("Вы уверены, что хотите выйти?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        auth.signOut()
                        onLogout()
                    }
                ) {
                    Text("Да", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    // 🔹 Уведомление об успешном сохранении
    if (showSaveSuccess) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            showSaveSuccess = false
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✅ Данные сохранены успешно!",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun OrdersHistoryTabContent(
    orders: List<Order>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onDebug: () -> Unit = {} // Добавили параметр для отладки
) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "История заказов",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row {
                // Кнопка для отладки (временно)
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            onDebug()
                        }
                    }
                ) {
                    Icon(Icons.Default.BugReport, contentDescription = "Отладка", tint = Color.Gray)
                }

                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (orders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = "Нет заказов",
                        modifier = Modifier.size(80.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "У вас еще нет заказов",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Сделайте первый заказ!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Кнопка для отладки
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                onDebug()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray
                        )
                    ) {
                        Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Проверить данные в Firebase")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders) { order ->
                    OrderCard(order = order)
                }
            }
        }
    }
}

// Остальные функции остаются без изменений...
@Composable
fun ProfileTabContent(
    userEmail: String,
    userProfile: UserProfile,
    isEditing: Boolean,
    isLoading: Boolean,
    editedName: String,
    editedPhone: String,
    editedAddress: String,
    onEditedNameChange: (String) -> Unit,
    onEditedPhoneChange: (String) -> Unit,
    onEditedAddressChange: (String) -> Unit,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 🔹 Карточка с информацией о профиле
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 🔹 Email пользователя (не редактируемый)
                ProfileItem(
                    label = "Email",
                    value = userEmail,
                    isEditable = false
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 🔹 Имя пользователя
                if (isEditing) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = onEditedNameChange,
                        label = { Text("Имя") },
                        placeholder = { Text("Введите ваше имя") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                } else {
                    ProfileItem(
                        label = "Имя",
                        value = if (userProfile.name.isBlank()) "Введите данные" else userProfile.name,
                        isEditable = true,
                        isEmpty = userProfile.name.isBlank()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🔹 Телефон
                if (isEditing) {
                    OutlinedTextField(
                        value = editedPhone,
                        onValueChange = onEditedPhoneChange,
                        label = { Text("Телефон") },
                        placeholder = { Text("Введите ваш телефон") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                } else {
                    ProfileItem(
                        label = "Телефон",
                        value = if (userProfile.phone.isBlank()) "Введите данные" else userProfile.phone,
                        isEditable = true,
                        isEmpty = userProfile.phone.isBlank()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🔹 Адрес
                if (isEditing) {
                    OutlinedTextField(
                        value = editedAddress,
                        onValueChange = onEditedAddressChange,
                        label = { Text("Адрес") },
                        placeholder = { Text("Введите ваш адрес") },
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        enabled = !isLoading,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                } else {
                    ProfileItem(
                        label = "Адрес",
                        value = if (userProfile.address.isBlank()) "Введите данные" else userProfile.address,
                        isEditable = true,
                        isEmpty = userProfile.address.isBlank(),
                        isMultiline = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🔹 Кнопки редактирования/сохранения
                if (isEditing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onCancelClick,
                            enabled = !isLoading
                        ) {
                            Text("Отмена", color = MaterialTheme.colorScheme.error)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = onSaveClick,
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Сохранить")
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = onEditClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Редактировать профиль")
                    }
                }
            }
        }

        // 🔹 Подсказка при первом входе (если нет сохраненных данных)
        if (userProfile.name.isBlank() &&
            userProfile.phone.isBlank() &&
            userProfile.address.isBlank() &&
            !isEditing &&
            !isLoading) {

            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Заполните профиль",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Нажмите 'Редактировать профиль' и заполните ваши данные. Они сохранятся в вашем аккаунте.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: Order) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Заголовок с номером заказа и датой
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Заказ #${order.orderNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = dateFormat.format(order.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Статус заказа
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(order.getStatusColor(), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = order.getStatusText(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = order.getStatusColor()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Информация о заказе
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OrderInfoRow(
                    icon = Icons.Default.Restaurant,
                    text = order.restaurantName
                )

                OrderInfoRow(
                    icon = Icons.Default.LocationOn,
                    text = order.deliveryAddress.take(50) + if (order.deliveryAddress.length > 50) "..." else ""
                )

                OrderInfoRow(
                    icon = Icons.Default.Payments,
                    text = when (order.paymentMethod) {
                        "CARD" -> "Оплата картой"
                        "CASH" -> "Оплата наличными"
                        "ONLINE" -> "Онлайн-оплата"
                        else -> "Неизвестно"
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Итоговая информация
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${order.totalItems} ${getItemsText(order.totalItems)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "Итого: ${"%.0f".format(order.totalPrice)} тг",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Кнопка повторить заказ
                Button(
                    onClick = { /* TODO: Повторить заказ */ },
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Повторить")
                }
            }
        }
    }
}

@Composable
fun OrderInfoRow(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun ProfileItem(
    label: String,
    value: String,
    isEditable: Boolean,
    isEmpty: Boolean = false,
    isMultiline: Boolean = false
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (isMultiline) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isEmpty) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onBackground
            )
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isEmpty) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onBackground
            )
        }
        if (isEditable && isEmpty) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "⚠ Нажмите 'Редактировать профиль' чтобы заполнить",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun getItemsText(count: Int): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> "товар"
        count % 10 in 2..4 && count % 100 !in 12..14 -> "товара"
        else -> "товаров"
    }
}