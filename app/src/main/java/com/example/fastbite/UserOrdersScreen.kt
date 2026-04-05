package com.example.fastbite

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedOrderForReview by remember { mutableStateOf<Order?>(null) }
    var selectedDishForReview by remember { mutableStateOf<Dish?>(null) }
    var showReviewDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        db.collection("orders")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isLoading = false
                    return@addSnapshotListener
                }

                orders = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Order(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            userName = doc.getString("userName") ?: "",
                            userPhone = doc.getString("userPhone") ?: "",
                            userAddress = doc.getString("userAddress") ?: "",
                            restaurantId = doc.getString("restaurantId") ?: "",
                            restaurantName = doc.getString("restaurantName") ?: "",
                            items = (doc.get("items") as? List<Map<String, Any>>)?.map { item ->
                                OrderItem(
                                    dishId = item["dishId"] as? String ?: "",
                                    dishName = item["dishName"] as? String ?: "",
                                    quantity = (item["quantity"] as? Long)?.toInt() ?: 1,
                                    price = (item["price"] as? Double) ?: 0.0,
                                    totalPrice = (item["totalPrice"] as? Double) ?: 0.0,
                                    photoUrl = item["photoUrl"] as? String ?: ""
                                )
                            } ?: emptyList(),
                            totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                            status = OrderStatus.values().find {
                                it.name == doc.getString("status")
                            } ?: OrderStatus.PENDING,
                            createdAt = (doc.getTimestamp("createdAt")?.toDate() ?: Date()),
                            updatedAt = (doc.getTimestamp("updatedAt")?.toDate() ?: Date()),
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
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
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
                    OrderCard(
                        order = order,
                        onReviewClick = { dish ->
                            selectedDishForReview = dish
                            showReviewDialog = true
                        },
                        canReview = order.status == OrderStatus.DELIVERED
                    )
                }

                item {
                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }

    // Диалог добавления отзыва
    if (showReviewDialog && selectedDishForReview != null) {
        AddReviewDialog(
            dish = selectedDishForReview!!,
            userEmail = userId,
            userName = userName,
            onDismiss = {
                showReviewDialog = false
                selectedDishForReview = null
            },
            onReviewAdded = {
                // Обновляем данные
                showReviewDialog = false
                selectedDishForReview = null
            }
        )
    }
}

@Composable
fun OrderCard(
    order: Order,
    onReviewClick: (Dish) -> Unit,
    canReview: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Шапка заказа
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
                UserOrderStatusChip(status = order.status)
            }

            // Краткая информация
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

            // Развернутая информация
            if (expanded) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Состав заказа
                Text(
                    "Состав заказа:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                order.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                        }

                        // Кнопка для отзыва (только для доставленных заказов)
                        if (canReview) {
                            Button(
                                onClick = {
                                    val dish = Dish(
                                        id = item.dishId,
                                        name = item.dishName,
                                        price = item.price.toString(),
                                        photoUrl = item.photoUrl
                                    )
                                    onReviewClick(dish)
                                },
                                modifier = Modifier
                                    .height(32.dp)
                                    .padding(start = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFC107),
                                    contentColor = Color.Black
                                )
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Оставить отзыв",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Оценить", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Адрес доставки
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    "Адрес доставки:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    order.deliveryAddress.address,
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                if (order.deliveryAddress.apartment.isNotBlank() ||
                    order.deliveryAddress.entrance.isNotBlank() ||
                    order.deliveryAddress.floor.isNotBlank()) {
                    Text(
                        buildString {
                            if (order.deliveryAddress.apartment.isNotBlank()) append("кв. ${order.deliveryAddress.apartment}")
                            if (order.deliveryAddress.entrance.isNotBlank()) append(", под. ${order.deliveryAddress.entrance}")
                            if (order.deliveryAddress.floor.isNotBlank()) append(", эт. ${order.deliveryAddress.floor}")
                            if (order.deliveryAddress.intercom.isNotBlank()) append(", домофон ${order.deliveryAddress.intercom}")
                        },
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Text(
                    "Способ оплаты: ${order.paymentMethod}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                if (order.comment.isNotBlank()) {
                    Text(
                        "Комментарий: ${order.comment}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// Функция для отображения статуса заказа в виде цветного чипа (для пользователя)
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