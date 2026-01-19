package com.example.fastbite

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBackClick: () -> Unit,
    onOrderConfirmed: () -> Unit = {}
) {
    val db = Firebase.firestore
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val currentUserEmail = currentUser?.email ?: ""
    val coroutineScope = rememberCoroutineScope()

    // Состояния для UI
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val cartItems = CartManager.cartItems
    val totalPrice by derivedStateOf { CartManager.getTotalPrice() }
    val totalItems by derivedStateOf { CartManager.getTotalItems() }

    // Состояние для формы
    var deliveryAddress by remember { mutableStateOf("") }
    var apartment by remember { mutableStateOf("") }
    var entrance by remember { mutableStateOf("") }
    var floor by remember { mutableStateOf("") }
    var intercom by remember { mutableStateOf("") }
    var deliveryComment by remember { mutableStateOf("") }
    var leaveAtDoor by remember { mutableStateOf(false) }
    var noCall by remember { mutableStateOf(false) }

    // Выбор метода оплаты - начинаем с null
    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }

    // Поля для карты (только для оплаты картой)
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCVC by remember { mutableStateOf("") }
    var cardholderName by remember { mutableStateOf("") }
    var saveCard by remember { mutableStateOf(false) }

    // Контактная информация
    var userPhone by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf(currentUserEmail) }

    // Состояние для выбора сохраненных адресов/карт
    var showSavedAddresses by remember { mutableStateOf(false) }
    var showSavedCards by remember { mutableStateOf(false) }
    var savedAddresses by remember { mutableStateOf(listOf<String>()) }
    var savedCards by remember { mutableStateOf(listOf<String>()) }

    // Загружаем сохраненные адреса и карты пользователя
    LaunchedEffect(currentUserEmail) {
        if (currentUserEmail.isNotBlank()) {
            // Загружаем сохраненные адреса
            db.collection("users").document(currentUserEmail)
                .collection("addresses")
                .get()
                .addOnSuccessListener { result ->
                    savedAddresses = result.documents.mapNotNull { it.getString("address") }
                }

            // Загружаем сохраненные карты
            db.collection("users").document(currentUserEmail)
                .collection("cards")
                .get()
                .addOnSuccessListener { result ->
                    savedCards = result.documents.mapNotNull {
                        val last4 = it.getString("last4") ?: "****"
                        "Карта **** $last4"
                    }
                }
        }
    }

    // Вычисляем, можно ли подтвердить заказ
    val canConfirmOrder = remember(
        deliveryAddress, selectedPaymentMethod, cardNumber,
        cardExpiry, cardCVC, cardholderName, userPhone, userEmail
    ) {
        val hasValidAddress = deliveryAddress.isNotBlank() && deliveryAddress.length >= 5
        val hasValidContact = userPhone.isNotBlank() && userPhone.length >= 10
        val hasValidEmail = userEmail.isNotBlank() && userEmail.contains("@")

        val hasValidPayment = when (selectedPaymentMethod) {
            PaymentMethod.CASH -> true
            PaymentMethod.ONLINE -> true
            PaymentMethod.CARD -> {
                cardNumber.replace(" ", "").length >= 16 &&
                        cardExpiry.matches(Regex("\\d{2}/\\d{2}")) &&
                        cardCVC.length == 3 &&
                        cardholderName.isNotBlank()
            }
            null -> false
        }

        cartItems.isNotEmpty() && hasValidAddress && hasValidContact && hasValidEmail && hasValidPayment
    }

    // Функция сохранения заказа в Firebase
    fun saveOrderToFirebase() {
        if (currentUserEmail.isBlank()) {
            errorMessage = "Пользователь не авторизован"
            showErrorDialog = true
            return
        }

        isLoading = true

        // Создаем объект заказа с явным указанием типа
        val order = hashMapOf<String, Any>(
            "userId" to currentUserEmail,
            "userEmail" to userEmail,
            "userPhone" to userPhone,
            "deliveryAddress" to deliveryAddress,
            "apartment" to apartment,
            "entrance" to entrance,
            "floor" to floor,
            "intercom" to intercom,
            "deliveryComment" to deliveryComment,
            "leaveAtDoor" to leaveAtDoor,
            "noCall" to noCall,
            "paymentMethod" to (selectedPaymentMethod?.name ?: "CASH"),
            "totalPrice" to totalPrice,
            "totalItems" to totalItems,
            "status" to "НОВЫЙ",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        // Если оплата картой, сохраняем информацию о карте
        if (selectedPaymentMethod == PaymentMethod.CARD && saveCard) {
            order["cardLast4"] = cardNumber.takeLast(4)
        }

        // Сохраняем заказ в коллекцию orders
        db.collection("orders").add(order)
            .addOnSuccessListener { orderDoc ->
                // Сохраняем товары заказа в подколлекцию
                val orderId = orderDoc.id
                val batch = db.batch()

                cartItems.forEach { cartItem ->
                    val orderItem = hashMapOf<String, Any>(
                        "orderId" to orderId,
                        "dishId" to cartItem.dish.id,
                        "dishName" to cartItem.dish.name,
                        "quantity" to cartItem.quantity,
                        "price" to (cartItem.dish.price.toDoubleOrNull() ?: 0.0),
                        "discount" to (cartItem.dish.discount?.toDoubleOrNull() ?: 0.0),
                        "totalPrice" to ((cartItem.dish.price.toDoubleOrNull() ?: 0.0) * cartItem.quantity)
                    )

                    val itemRef = db.collection("orders").document(orderId)
                        .collection("items").document(cartItem.dish.id)
                    batch.set(itemRef, orderItem)
                }

                batch.commit()
                    .addOnSuccessListener {
                        // Очищаем корзину
                        CartManager.clearCart()

                        // Сохраняем адрес, если нужно
                        if (deliveryAddress.isNotBlank() && savedAddresses.none { it == deliveryAddress }) {
                            val addressData = hashMapOf<String, Any>(
                                "address" to deliveryAddress,
                                "apartment" to apartment,
                                "entrance" to entrance,
                                "floor" to floor,
                                "intercom" to intercom,
                                "createdAt" to FieldValue.serverTimestamp()
                            )

                            db.collection("users").document(currentUserEmail)
                                .collection("addresses")
                                .add(addressData)
                        }

                        // Сохраняем карту, если нужно
                        if (selectedPaymentMethod == PaymentMethod.CARD && saveCard && cardNumber.isNotBlank()) {
                            val cardData = hashMapOf<String, Any>(
                                "last4" to cardNumber.takeLast(4),
                                "expiry" to cardExpiry,
                                "cardholderName" to cardholderName,
                                "createdAt" to FieldValue.serverTimestamp()
                            )

                            db.collection("users").document(currentUserEmail)
                                .collection("cards")
                                .add(cardData)
                        }

                        isLoading = false
                        showSuccessDialog = true
                    }
                    .addOnFailureListener { e ->
                        isLoading = false
                        errorMessage = "Ошибка при сохранении товаров: ${e.message}"
                        showErrorDialog = true
                    }
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = "Ошибка при создании заказа: ${e.message}"
                showErrorDialog = true
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Оформление заказа",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Итого: ${"%.0f".format(totalPrice)} тг",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "$totalItems ${getItemsText(totalItems)}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    saveOrderToFirebase()
                                }
                            },
                            modifier = Modifier.height(50.dp),
                            enabled = canConfirmOrder && !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Подтвердить заказ", fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Секция с товарами
            item {
                Text("Ваш заказ", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            items(cartItems) { cartItem ->
                CheckoutItemCard(cartItem = cartItem)
            }

            item { Spacer(Modifier.height(8.dp)) }

            // Секция контактной информации
            item {
                Text("Контактная информация", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = userEmail,
                    onValueChange = { userEmail = it },
                    label = { Text("Email*") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Outlined.Email, contentDescription = null)
                    },
                    enabled = false // Email из профиля, не редактируемый
                )

                OutlinedTextField(
                    value = userPhone,
                    onValueChange = {
                        if (it.length <= 11) userPhone = it
                    },
                    label = { Text("Телефон*") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Outlined.Phone, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    placeholder = { Text("+7 (XXX) XXX-XX-XX") }
                )
            }

            item { Spacer(Modifier.height(8.dp)) }

            // Секция доставки
            item {
                Text("Доставка", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                // Кнопка выбора сохраненного адреса
                if (savedAddresses.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showSavedAddresses = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Выбрать сохраненный адрес")
                        }
                    }
                }

                OutlinedTextField(
                    value = deliveryAddress,
                    onValueChange = { deliveryAddress = it },
                    label = { Text("Адрес доставки*") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null)
                    }
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = apartment,
                        onValueChange = { apartment = it },
                        label = { Text("Квартира") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = entrance,
                        onValueChange = { entrance = it },
                        label = { Text("Подъезд") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = floor,
                        onValueChange = { floor = it },
                        label = { Text("Этаж") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = intercom,
                        onValueChange = { intercom = it },
                        label = { Text("Домофон") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Дополнительные опции доставки
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = deliveryComment,
                        onValueChange = { deliveryComment = it },
                        label = { Text("Комментарий для курьера") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Outlined.Comment, contentDescription = null)
                        },
                        maxLines = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = leaveAtDoor,
                            onCheckedChange = { leaveAtDoor = it }
                        )
                        Text(
                            "Оставить у двери",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = noCall,
                            onCheckedChange = { noCall = it }
                        )
                        Text(
                            "Не звонить, сообщить в чат",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }

            // Секция оплаты
            item {
                Text("Оплата", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                // Кнопка выбора сохраненной карты
                if (savedCards.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showSavedCards = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Выбрать сохраненную карту")
                        }
                    }
                }

                // Методы оплаты
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentMethod.values().forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPaymentMethod = method }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedPaymentMethod == method,
                                onClick = { selectedPaymentMethod = method }
                            )
                            Icon(
                                when (method) {
                                    PaymentMethod.CARD -> Icons.Outlined.CreditCard
                                    PaymentMethod.CASH -> Icons.Outlined.Money
                                    PaymentMethod.ONLINE -> Icons.Outlined.Payment
                                },
                                contentDescription = null,
                                modifier = Modifier.padding(start = 8.dp).size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                method.displayName,
                                modifier = Modifier.padding(start = 12.dp),
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // Форма для карты
                if (selectedPaymentMethod == PaymentMethod.CARD) {
                    Column(
                        modifier = Modifier.padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = {
                                if (it.length <= 19) cardNumber = it.formatCardNumber()
                            },
                            label = { Text("Номер карты*") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Outlined.CreditCard, contentDescription = null)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = { Text("1234 5678 9012 3456") }
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = cardExpiry,
                                onValueChange = {
                                    if (it.length <= 5) cardExpiry = it.formatExpiryDate()
                                },
                                label = { Text("Срок действия*") },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("MM/YY") }
                            )
                            OutlinedTextField(
                                value = cardCVC,
                                onValueChange = {
                                    if (it.length <= 3) cardCVC = it
                                },
                                label = { Text("CVC*") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                placeholder = { Text("123") }
                            )
                        }

                        OutlinedTextField(
                            value = cardholderName,
                            onValueChange = { cardholderName = it },
                            label = { Text("Имя держателя карты*") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("IVAN IVANOV") }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = saveCard,
                                onCheckedChange = { saveCard = it }
                            )
                            Text(
                                "Сохранить карту для будущих покупок",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }

    // Диалог выбора сохраненных адресов
    if (showSavedAddresses) {
        AlertDialog(
            onDismissRequest = { showSavedAddresses = false },
            title = { Text("Выберите адрес") },
            text = {
                Column {
                    savedAddresses.forEach { address ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    deliveryAddress = address
                                    showSavedAddresses = false
                                }
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(address)
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showSavedAddresses = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Добавить новый адрес")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSavedAddresses = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Диалог выбора сохраненных карт
    if (showSavedCards) {
        AlertDialog(
            onDismissRequest = { showSavedCards = false },
            title = { Text("Выберите карту") },
            text = {
                Column {
                    savedCards.forEachIndexed { index, card ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Здесь можно предзаполнить данные карты
                                    // В реальном приложении нужно загружать полные данные карты из БД
                                    cardNumber = "**** **** **** ${card.takeLast(4)}"
                                    showSavedCards = false
                                }
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.CreditCard,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(card)
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showSavedCards = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Добавить новую карту")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSavedCards = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Диалог успешного заказа
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Заказ успешно оформлен!") },
            text = {
                Column {
                    Text("Ваш заказ принят в обработку.")
                    Spacer(Modifier.height(8.dp))
                    Text("Номер заказа будет отправлен на ваш email.")
                    Spacer(Modifier.height(8.dp))
                    Text("Спасибо за покупку!")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onOrderConfirmed()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Отлично")
                }
            }
        )
    }

    // Диалог ошибки
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Ошибка") },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun CheckoutItemCard(cartItem: CartItem) {
    val dish = cartItem.dish
    val currentQuantity = cartItem.quantity  // ← используем напрямую

    // Расчет цены с учетом скидки
    val discountPercentage = dish.discount?.toDoubleOrNull() ?: 0.0
    val originalPrice = dish.price.toDoubleOrNull() ?: 0.0
    val discountedPrice = if (discountPercentage > 0)
        originalPrice * (1 - discountPercentage / 100)
    else originalPrice

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = dish.photoUrl,
                contentDescription = dish.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    dish.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )

                Text(
                    "${"%.0f".format(discountedPrice)} тг × $currentQuantity",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Text(
                "${"%.0f".format(discountedPrice * currentQuantity)} тг",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

enum class PaymentMethod(val displayName: String) {
    CARD("Банковская карта"),
    CASH("Наличными при получении"),
    ONLINE("Онлайн-оплата")
}

// Вспомогательные функции форматирования
private fun String.formatCardNumber(): String {
    val cleaned = this.filter { it.isDigit() }
    return cleaned.chunked(4).joinToString(" ")
}

private fun String.formatExpiryDate(): String {
    val cleaned = this.filter { it.isDigit() }
    if (cleaned.length >= 2) {
        return "${cleaned.take(2)}/${cleaned.drop(2).take(2)}"
    }
    return cleaned
}

@Composable
private fun getItemsText(count: Int): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> "товар"
        count % 10 in 2..4 && count % 100 !in 12..14 -> "товара"
        else -> "товаров"
    }
}