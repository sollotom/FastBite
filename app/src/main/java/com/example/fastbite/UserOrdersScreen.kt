package com.example.fastbite

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserOrdersScreen(
    onBackClick: () -> Unit
) {
    val db = Firebase.firestore
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.email ?: ""
    val userName = currentUser?.displayName ?: currentUser?.email?.split("@")?.first() ?: ""
    val coroutineScope = rememberCoroutineScope()

    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedDishForReview by remember { mutableStateOf<Dish?>(null) }
    var existingReview by remember { mutableStateOf<Review?>(null) }
    var showReviewDialog by remember { mutableStateOf(false) }

    var userReviews by remember { mutableStateOf<Map<String, Review>>(emptyMap()) }

    LaunchedEffect(userId) {
        val reviewsSnapshot = db.collection("reviews")
            .whereEqualTo("userEmail", userId)
            .get()
            .await()

        userReviews = reviewsSnapshot.documents.mapNotNull { doc ->
            val dishId = doc.getString("dishId") ?: return@mapNotNull null
            dishId to Review(
                id = doc.id,
                userName = doc.getString("userName") ?: "",
                userEmail = doc.getString("userEmail") ?: "",
                rating = doc.getDouble("rating") ?: 0.0,
                comment = doc.getString("comment") ?: "",
                date = doc.getString("date") ?: "",
                dishId = dishId,
                dishName = doc.getString("dishName") ?: "",
                restaurantId = doc.getString("restaurantId") ?: ""
            )
        }.toMap()
    }

    LaunchedEffect(userId) {
        db.collection("orders")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isLoading = false
                    return@addSnapshotListener
                }

                val loadedOrders = mutableListOf<Order>()

                snapshot?.documents?.forEach { doc ->
                    try {
                        val allItems = mutableListOf<OrderItem>()
                        val itemsField = doc.get("items")

                        when (itemsField) {
                            is List<*> -> {
                                itemsField.forEach { item ->
                                    if (item is Map<*, *>) {
                                        allItems.add(
                                            OrderItem(
                                                dishId = item["dishId"] as? String ?: "",
                                                dishName = item["dishName"] as? String ?: "",
                                                quantity = (item["quantity"] as? Long)?.toInt() ?: 1,
                                                price = (item["price"] as? Double) ?: 0.0,
                                                totalPrice = (item["totalPrice"] as? Double) ?: 0.0,
                                                photoUrl = item["photoUrl"] as? String ?: "",
                                                restaurantId = item["restaurantId"] as? String ?: "",
                                                isDelivered = item["isDelivered"] as? Boolean ?: false
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        val order = Order(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            userName = doc.getString("userName") ?: "",
                            userPhone = doc.getString("userPhone") ?: "",
                            userAddress = doc.getString("userAddress") ?: "",
                            restaurantId = doc.getString("restaurantId") ?: "",
                            restaurantName = doc.getString("restaurantName") ?: "",
                            items = allItems,
                            totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                            status = OrderStatus.values().find { it.name == doc.getString("status") } ?: OrderStatus.PENDING,
                            createdAt = doc.getTimestamp("createdAt")?.toDate() ?: Date(),
                            updatedAt = doc.getTimestamp("updatedAt")?.toDate() ?: Date(),
                            deliveryAddress = DeliveryAddress(
                                address = doc.getString("deliveryAddress.address") ?: "",
                                apartment = doc.getString("deliveryAddress.apartment") ?: "",
                                entrance = doc.getString("deliveryAddress.entrance") ?: "",
                                floor = doc.getString("deliveryAddress.floor") ?: "",
                                intercom = doc.getString("deliveryAddress.intercom") ?: ""
                            ),
                            paymentMethod = doc.getString("paymentMethod") ?: "Наличными",
                            comment = doc.getString("comment") ?: ""
                        )
                        loadedOrders.add(order)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                orders = loadedOrders
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Мои заказы",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.Gray
                    )
                    Text(
                        "У вас пока нет заказов",
                        fontSize = 18.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Сделайте первый заказ, чтобы увидеть его здесь",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders) { order ->
                    OrderCardForUser(
                        order = order,
                        userReviews = userReviews,
                        onReviewClick = { dish, existingReviewData ->
                            selectedDishForReview = dish
                            existingReview = existingReviewData
                            showReviewDialog = true
                        }
                    )
                }

                item {
                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }

    if (showReviewDialog && selectedDishForReview != null) {
        AddOrEditReviewDialog(
            dish = selectedDishForReview!!,
            existingReview = existingReview,
            userEmail = userId,
            userName = userName,
            onDismiss = {
                showReviewDialog = false
                selectedDishForReview = null
                existingReview = null
            },
            onReviewAdded = {
                showReviewDialog = false
                selectedDishForReview = null
                existingReview = null
                coroutineScope.launch {
                    val reviewsSnapshot = db.collection("reviews")
                        .whereEqualTo("userEmail", userId)
                        .get()
                        .await()
                    userReviews = reviewsSnapshot.documents.mapNotNull { doc ->
                        val dishId = doc.getString("dishId") ?: return@mapNotNull null
                        dishId to Review(
                            id = doc.id,
                            userName = doc.getString("userName") ?: "",
                            userEmail = doc.getString("userEmail") ?: "",
                            rating = doc.getDouble("rating") ?: 0.0,
                            comment = doc.getString("comment") ?: "",
                            date = doc.getString("date") ?: "",
                            dishId = dishId,
                            dishName = doc.getString("dishName") ?: "",
                            restaurantId = doc.getString("restaurantId") ?: ""
                        )
                    }.toMap()
                }
            }
        )
    }
}

@Composable
fun OrderCardForUser(
    order: Order,
    userReviews: Map<String, Review>,
    onReviewClick: (Dish, Review?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val allItemsDelivered = order.items.isNotEmpty() && order.items.all { it.isDelivered }
    val someItemsDelivered = order.items.any { it.isDelivered } && !allItemsDelivered

    val customStatus = when {
        allItemsDelivered -> "Все доставлено"
        someItemsDelivered -> "Частично доставлено"
        else -> null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Заказ #${order.id.takeLast(6)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru")).format(order.createdAt),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                if (customStatus != null) {
                    UserOrderStatusChipCustom(
                        statusText = customStatus,
                        statusColor = when {
                            allItemsDelivered -> Color(0xFF4CAF50)
                            else -> Color(0xFFFF9800)
                        }
                    )
                } else {
                    UserOrderStatusChip(status = order.status)
                }
            }

            Text(
                "Ресторан: ${order.restaurantName}",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Сумма: ${"%.0f".format(order.totalAmount)} тг",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Свернуть" else "Развернуть",
                    tint = Color.Gray
                )
            }

            if (expanded) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    "Состав заказа:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                order.items.forEach { item ->
                    val hasReview = userReviews.containsKey(item.dishId)
                    val review = userReviews[item.dishId]

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "${item.dishName} × ${item.quantity}",
                                fontSize = 14.sp
                            )
                            Text(
                                "${"%.0f".format(item.price)} тг",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )

                            if (item.isDelivered) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "Доставлено",
                                        fontSize = 11.sp,
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                            } else {
                                Text(
                                    "Ожидает доставки",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        if (item.isDelivered) {
                            Button(
                                onClick = {
                                    val dish = Dish(
                                        id = item.dishId,
                                        name = item.dishName,
                                        price = item.price.toString(),
                                        photoUrl = item.photoUrl
                                    )
                                    onReviewClick(dish, review)
                                },
                                modifier = Modifier
                                    .height(36.dp)
                                    .padding(start = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (hasReview) Color(0xFF4CAF50) else Color(0xFFFFC107),
                                    contentColor = Color.Black
                                )
                            ) {
                                Icon(
                                    if (hasReview) Icons.Default.Edit else Icons.Default.Star,
                                    contentDescription = if (hasReview) "Редактировать отзыв" else "Оставить отзыв",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (hasReview) "Изменить" else "Оценить",
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Адрес доставки:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(order.deliveryAddress.address, fontSize = 14.sp, color = Color.Gray)
                Text("Способ оплаты: ${order.paymentMethod}", fontSize = 14.sp, color = Color.Gray)
                if (order.comment.isNotBlank()) {
                    Text("Комментарий: ${order.comment}", fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun AddOrEditReviewDialog(
    dish: Dish,
    existingReview: Review?,
    userEmail: String,
    userName: String,
    onDismiss: () -> Unit,
    onReviewAdded: () -> Unit
) {
    var rating by remember { mutableStateOf(existingReview?.rating?.toInt() ?: 0) }
    var comment by remember { mutableStateOf(existingReview?.comment ?: "") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val isEditing = existingReview != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isEditing) "Редактировать отзыв" else "Оставить отзыв",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Блюдо: ${dish.name}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Column {
                    Text(
                        "Ваша оценка",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(5) { index ->
                            IconButton(
                                onClick = { rating = index + 1 },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    if (index < rating) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = "Рейтинг ${index + 1}",
                                    tint = if (index < rating) Color(0xFFFFC107) else Color.Gray,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Ваш отзыв") },
                    placeholder = { Text("Поделитесь впечатлениями о блюде...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )

                if (error.isNotEmpty()) {
                    Text(
                        error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (rating == 0) {
                        error = "Пожалуйста, поставьте оценку"
                        return@Button
                    }

                    isLoading = true
                    error = ""

                    coroutineScope.launch {
                        if (isEditing && existingReview != null) {
                            ReviewManager.updateReview(
                                reviewId = existingReview.id,
                                rating = rating.toDouble(),
                                comment = comment,
                                onSuccess = {
                                    isLoading = false
                                    onReviewAdded()
                                    onDismiss()
                                },
                                onError = { err ->
                                    isLoading = false
                                    error = err
                                }
                            )
                        } else {
                            ReviewManager.addReview(
                                dishId = dish.id,
                                userEmail = userEmail,
                                userName = userName,
                                rating = rating.toDouble(),
                                comment = comment,
                                onSuccess = {
                                    isLoading = false
                                    onReviewAdded()
                                    onDismiss()
                                },
                                onError = { err ->
                                    isLoading = false
                                    error = err
                                }
                            )
                        }
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (isEditing) "Сохранить изменения" else "Отправить отзыв")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Отмена")
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun UserOrderStatusChipCustom(statusText: String, statusColor: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = statusColor,
        modifier = Modifier.wrapContentSize()
    ) {
        Text(
            statusText,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun UserOrderStatusChip(status: OrderStatus) {
    val (backgroundColor, textColor) = when (status) {
        OrderStatus.PENDING -> Color(0xFFFFA000) to Color.White
        OrderStatus.CONFIRMED -> Color(0xFF2196F3) to Color.White
        OrderStatus.PREPARING -> Color(0xFFFF9800) to Color.White
        OrderStatus.READY_FOR_PICKUP -> Color(0xFF4CAF50) to Color.White
        OrderStatus.DELIVERING -> Color(0xFF9C27B0) to Color.White
        OrderStatus.DELIVERED -> Color(0xFF4CAF50) to Color.White
        OrderStatus.CANCELLED -> Color(0xFFF44336) to Color.White
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        modifier = Modifier.wrapContentSize()
    ) {
        Text(
            status.displayName,
            color = textColor,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}