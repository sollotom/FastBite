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
import kotlinx.coroutines.tasks.await

// Строки для экрана оформления заказа
object CheckoutStrings {
    var currentLanguage = Strings.currentLanguage

    val checkout: String get() = if (currentLanguage.value == Language.KAZAKH) "Тапсырыс беру" else "Оформление заказа"
    val back: String get() = if (currentLanguage.value == Language.KAZAKH) "Артқа" else "Назад"
    val total: String get() = if (currentLanguage.value == Language.KAZAKH) "Барлығы" else "Итого"
    val confirmOrder: String get() = if (currentLanguage.value == Language.KAZAKH) "Тапсырысты растау" else "Подтвердить заказ"
    val yourOrder: String get() = if (currentLanguage.value == Language.KAZAKH) "Сіздің тапсырысыңыз" else "Ваш заказ"
    val contactInfo: String get() = if (currentLanguage.value == Language.KAZAKH) "Байланыс ақпараты" else "Контактная информация"
    val name: String get() = if (currentLanguage.value == Language.KAZAKH) "Аты *" else "Имя *"
    val email: String get() = if (currentLanguage.value == Language.KAZAKH) "Email *" else "Email *"
    val phone: String get() = if (currentLanguage.value == Language.KAZAKH) "Телефон *" else "Телефон *"
    val phonePlaceholder: String get() = if (currentLanguage.value == Language.KAZAKH) "+7 (XXX) XXX-XX-XX" else "+7 (XXX) XXX-XX-XX"
    val delivery: String get() = if (currentLanguage.value == Language.KAZAKH) "Жеткізу" else "Доставка"
    val deliveryAddress: String get() = if (currentLanguage.value == Language.KAZAKH) "Жеткізу мекенжайы *" else "Адрес доставки *"
    val apartment: String get() = if (currentLanguage.value == Language.KAZAKH) "Пәтер" else "Квартира"
    val entrance: String get() = if (currentLanguage.value == Language.KAZAKH) "Кіреберіс" else "Подъезд"
    val floor: String get() = if (currentLanguage.value == Language.KAZAKH) "Қабат" else "Этаж"
    val intercom: String get() = if (currentLanguage.value == Language.KAZAKH) "Домофон" else "Домофон"
    val courierComment: String get() = if (currentLanguage.value == Language.KAZAKH) "Курьерге түсініктеме" else "Комментарий для курьера"
    val payment: String get() = if (currentLanguage.value == Language.KAZAKH) "Төлем" else "Оплата"
    val cashOnDelivery: String get() = if (currentLanguage.value == Language.KAZAKH) "Алған кезде қолма-қол ақша" else "Наличными при получении"
    val bankCard: String get() = if (currentLanguage.value == Language.KAZAKH) "Банк картасы" else "Банковская карта"
    val cardNumber: String get() = if (currentLanguage.value == Language.KAZAKH) "Карта нөмірі *" else "Номер карты *"
    val cardNumberPlaceholder: String get() = if (currentLanguage.value == Language.KAZAKH) "1234 5678 9012 3456" else "1234 5678 9012 3456"
    val expiryDate: String get() = if (currentLanguage.value == Language.KAZAKH) "Жарамдылық мерзімі *" else "Срок действия *"
    val expiryPlaceholder: String get() = if (currentLanguage.value == Language.KAZAKH) "АА/ЖЖ" else "MM/YY"
    val cvc: String get() = if (currentLanguage.value == Language.KAZAKH) "CVC *" else "CVC *"
    val cardholderName: String get() = if (currentLanguage.value == Language.KAZAKH) "Карта иесінің аты *" else "Имя держателя карты *"
    val cardholderPlaceholder: String get() = if (currentLanguage.value == Language.KAZAKH) "IVAN IVANOV" else "IVAN IVANOV"

    // Диалоги
    val orderSuccess: String get() = if (currentLanguage.value == Language.KAZAKH) "Тапсырыс сәтті рәсімделді!" else "Заказ успешно оформлен!"
    val orderAccepted: String get() = if (currentLanguage.value == Language.KAZAKH) "Сіздің тапсырысыңыз өңдеуге қабылданды. Сатып алғаныңыз үшін рахмет!" else "Ваш заказ принят в обработку. Спасибо за покупку!"
    val great: String get() = if (currentLanguage.value == Language.KAZAKH) "Керемет" else "Отлично"
    val error: String get() = if (currentLanguage.value == Language.KAZAKH) "Қате" else "Ошибка"
    val ok: String get() = if (currentLanguage.value == Language.KAZAKH) "OK" else "OK"
    val userNotAuthorized: String get() = if (currentLanguage.value == Language.KAZAKH) "Пайдаланушы авторландырылмаған" else "Пользователь не авторизован"
    val orderCreationError: String get() = if (currentLanguage.value == Language.KAZAKH) "Тапсырыс жасау қатесі: " else "Ошибка при создании заказа: "
    val restaurant: String get() = if (currentLanguage.value == Language.KAZAKH) "Мейрамхана" else "Ресторан"

    fun getItemsWord(count: Int): String {
        return if (currentLanguage.value == Language.KAZAKH) {
            "тауар"
        } else {
            when {
                count % 10 == 1 && count % 100 != 11 -> "товар"
                count % 10 in 2..4 && count % 100 !in 12..14 -> "товара"
                else -> "товаров"
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBackClick: () -> Unit,
    onOrderConfirmed: () -> Unit
) {
    val db = Firebase.firestore
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val currentUserEmail = currentUser?.email ?: ""
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val cartItems = CartManager.cartItems
    val totalPrice by derivedStateOf { CartManager.getTotalPrice() }
    val totalItems by derivedStateOf { CartManager.getTotalItems() }

    var deliveryAddress by remember { mutableStateOf("") }
    var apartment by remember { mutableStateOf("") }
    var entrance by remember { mutableStateOf("") }
    var floor by remember { mutableStateOf("") }
    var intercom by remember { mutableStateOf("") }
    var deliveryComment by remember { mutableStateOf("") }

    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }

    // Поля для карты
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCVC by remember { mutableStateOf("") }
    var cardholderName by remember { mutableStateOf("") }

    var userPhone by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf(currentUserEmail) }
    var userName by remember { mutableStateOf("") }

    var savedAddresses by remember { mutableStateOf<List<Address>>(emptyList()) }

    LaunchedEffect(currentUserEmail) {
        if (currentUserEmail.isNotBlank()) {
            try {
                val userDoc = db.collection("users").document(currentUserEmail).get().await()
                userName = userDoc.getString("name") ?: ""
                userPhone = userDoc.getString("phone") ?: ""

                val addressesSnapshot = db.collection("users").document(currentUserEmail)
                    .collection("address")
                    .get()
                    .await()
                savedAddresses = addressesSnapshot.documents.mapNotNull { doc ->
                    Address(
                        id = doc.id,
                        address = doc.getString("address") ?: return@mapNotNull null,
                        apartment = doc.getString("apartment") ?: "",
                        entrance = doc.getString("entrance") ?: "",
                        floor = doc.getString("floor") ?: "",
                        intercom = doc.getString("intercom") ?: "",
                        isDefault = doc.getBoolean("isDefault") ?: false
                    )
                }

                val defaultAddress = savedAddresses.find { it.isDefault }
                defaultAddress?.let {
                    deliveryAddress = it.address
                    apartment = it.apartment
                    entrance = it.entrance
                    floor = it.floor
                    intercom = it.intercom
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val canConfirmOrder = remember(
        deliveryAddress, selectedPaymentMethod, userPhone, userEmail,
        cardNumber, cardExpiry, cardCVC, cardholderName
    ) {
        val hasValidAddress = deliveryAddress.isNotBlank() && deliveryAddress.length >= 5
        val hasValidContact = userPhone.isNotBlank() && userPhone.length >= 10
        val hasValidEmail = userEmail.isNotBlank() && userEmail.contains("@")

        val hasValidPayment = when (selectedPaymentMethod) {
            PaymentMethod.CASH -> true
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

    fun saveOrderToFirebase() {
        if (currentUserEmail.isBlank()) {
            errorMessage = CheckoutStrings.userNotAuthorized
            showErrorDialog = true
            return
        }

        isLoading = true

        val ordersByRestaurant = cartItems.groupBy { it.dish.owner }

        val ordersToCreate = ordersByRestaurant.map { (restaurantId, items) ->
            val restaurantName = items.firstOrNull()?.dish?.owner?.split("@")?.first() ?: CheckoutStrings.restaurant

            val orderItems = items.map { cartItem ->
                val discountPercentage = cartItem.dish.discount?.toDoubleOrNull() ?: 0.0
                val originalPrice = cartItem.dish.price.toDoubleOrNull() ?: 0.0
                val discountedPrice = if (discountPercentage > 0)
                    originalPrice * (1 - discountPercentage / 100)
                else originalPrice

                mapOf(
                    "dishId" to cartItem.dish.id,
                    "dishName" to cartItem.dish.name,
                    "quantity" to cartItem.quantity,
                    "price" to discountedPrice,
                    "totalPrice" to discountedPrice * cartItem.quantity,
                    "photoUrl" to cartItem.dish.photoUrl,
                    "restaurantId" to restaurantId,
                    "isDelivered" to false
                )
            }

            val totalAmount = orderItems.sumOf { (it["totalPrice"] as Double) }

            hashMapOf<String, Any>(
                "userId" to currentUserEmail,
                "userName" to userName.ifEmpty { currentUserEmail.split("@")[0] },
                "userPhone" to userPhone,
                "restaurantId" to restaurantId,
                "restaurantName" to restaurantName,
                "items" to orderItems,
                "totalAmount" to totalAmount,
                "status" to "PENDING",
                "deliveryAddress" to mapOf(
                    "address" to deliveryAddress,
                    "apartment" to apartment,
                    "entrance" to entrance,
                    "floor" to floor,
                    "intercom" to intercom
                ),
                "paymentMethod" to (selectedPaymentMethod?.displayName ?: CheckoutStrings.cashOnDelivery),
                "comment" to deliveryComment,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        }

        val batch = db.batch()
        ordersToCreate.forEach { orderData ->
            val orderRef = db.collection("orders").document()
            batch.set(orderRef, orderData)
        }

        batch.commit()
            .addOnSuccessListener {
                CartManager.clearCart()
                isLoading = false
                showSuccessDialog = true
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = CheckoutStrings.orderCreationError + e.message
                showErrorDialog = true
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = CheckoutStrings.checkout,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = CheckoutStrings.back)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
                                text = "${CheckoutStrings.total}: ${"%.0f".format(totalPrice)} тг",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$totalItems ${CheckoutStrings.getItemsWord(totalItems)}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        Button(
                            onClick = { saveOrderToFirebase() },
                            modifier = Modifier.height(50.dp),
                            enabled = canConfirmOrder && !isLoading,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(text = CheckoutStrings.confirmOrder, fontSize = 16.sp)
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
            item {
                Text(text = CheckoutStrings.yourOrder, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            items(cartItems) { cartItem ->
                CheckoutItemCard(cartItem = cartItem)
            }

            item { Spacer(Modifier.height(8.dp)) }

            item {
                Text(text = CheckoutStrings.contactInfo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text(CheckoutStrings.name) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = userEmail,
                    onValueChange = { userEmail = it },
                    label = { Text(CheckoutStrings.email) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                    enabled = false,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = userPhone,
                    onValueChange = { if (it.length <= 15) userPhone = it },
                    label = { Text(CheckoutStrings.phone) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    placeholder = { Text(CheckoutStrings.phonePlaceholder) },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item { Spacer(Modifier.height(8.dp)) }

            item {
                Text(text = CheckoutStrings.delivery, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = deliveryAddress,
                    onValueChange = { deliveryAddress = it },
                    label = { Text(CheckoutStrings.deliveryAddress) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    isError = deliveryAddress.isBlank()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = apartment,
                        onValueChange = { apartment = it },
                        label = { Text(CheckoutStrings.apartment) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = entrance,
                        onValueChange = { entrance = it },
                        label = { Text(CheckoutStrings.entrance) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = floor,
                        onValueChange = { floor = it },
                        label = { Text(CheckoutStrings.floor) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = intercom,
                        onValueChange = { intercom = it },
                        label = { Text(CheckoutStrings.intercom) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = deliveryComment,
                    onValueChange = { deliveryComment = it },
                    label = { Text(CheckoutStrings.courierComment) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Outlined.Comment, contentDescription = null) },
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item { Spacer(Modifier.height(8.dp)) }

            item {
                Text(text = CheckoutStrings.payment, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Наличными
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPaymentMethod = PaymentMethod.CASH }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPaymentMethod == PaymentMethod.CASH,
                            onClick = { selectedPaymentMethod = PaymentMethod.CASH }
                        )
                        Icon(
                            Icons.Outlined.Money,
                            contentDescription = null,
                            modifier = Modifier.padding(start = 8.dp).size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = CheckoutStrings.cashOnDelivery,
                            modifier = Modifier.padding(start = 12.dp),
                            fontSize = 16.sp
                        )
                    }

                    // Банковская карта
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPaymentMethod = PaymentMethod.CARD }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPaymentMethod == PaymentMethod.CARD,
                            onClick = { selectedPaymentMethod = PaymentMethod.CARD }
                        )
                        Icon(
                            Icons.Outlined.CreditCard,
                            contentDescription = null,
                            modifier = Modifier.padding(start = 8.dp).size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = CheckoutStrings.bankCard,
                            modifier = Modifier.padding(start = 12.dp),
                            fontSize = 16.sp
                        )
                    }
                }

                // Поля для карты
                if (selectedPaymentMethod == PaymentMethod.CARD) {
                    Column(
                        modifier = Modifier.padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = { if (it.length <= 19) cardNumber = it.formatCardNumber() },
                            label = { Text(CheckoutStrings.cardNumber) },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Outlined.CreditCard, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = { Text(CheckoutStrings.cardNumberPlaceholder) },
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = cardExpiry,
                                onValueChange = { if (it.length <= 5) cardExpiry = it.formatExpiryDate() },
                                label = { Text(CheckoutStrings.expiryDate) },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text(CheckoutStrings.expiryPlaceholder) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = cardCVC,
                                onValueChange = { if (it.length <= 3) cardCVC = it },
                                label = { Text(CheckoutStrings.cvc) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                placeholder = { Text("123") },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        OutlinedTextField(
                            value = cardholderName,
                            onValueChange = { cardholderName = it },
                            label = { Text(CheckoutStrings.cardholderName) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(CheckoutStrings.cardholderPlaceholder) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text(CheckoutStrings.orderSuccess) },
            text = { Text(CheckoutStrings.orderAccepted) },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onOrderConfirmed()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(CheckoutStrings.great)
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text(CheckoutStrings.error) },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(CheckoutStrings.ok)
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}

@Composable
fun CheckoutItemCard(cartItem: CartItem) {
    val dish = cartItem.dish
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
                    text = dish.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                Text(
                    text = "${"%.0f".format(discountedPrice)} тг × ${cartItem.quantity}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Text(
                text = "${"%.0f".format(discountedPrice * cartItem.quantity)} тг",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

enum class PaymentMethod(val displayName: String) {
    CARD("Банковская карта"),
    CASH("Наличными при получении")
}

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