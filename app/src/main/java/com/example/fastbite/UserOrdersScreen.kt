package com.example.fastbite

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

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
                title = { Text("Мои заказы") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
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
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "У вас пока нет заказов",
                        fontSize = 18.sp,
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
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Заказ #${order.id.takeLast(6)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                StatusChip(status = order.status)
                            }

                            Text(
                                "Ресторан: ${order.restaurantName}",
                                fontSize = 14.sp
                            )

                            Text(
                                "Сумма: ${"%.0f".format(order.totalAmount)} тг",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                "Дата: ${SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru")).format(order.createdAt)}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: OrderStatus) {
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