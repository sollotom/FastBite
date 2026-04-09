package com.example.fastbite

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.window.Dialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerOrdersScreen() {
    val db = Firebase.firestore
    val currentUser = FirebaseAuth.getInstance().currentUser
    val restaurantId = currentUser?.email ?: ""

    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var selectedOrder by remember { mutableStateOf<Order?>(null) }
    var selectedStatus by remember { mutableStateOf<OrderStatus?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(restaurantId) {
        db.collection("orders")
            .whereEqualTo("restaurantId", restaurantId)
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
                                                dishName = item["dishName"] as? String ?: "Без названия",
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

                        if (allItems.isNotEmpty()) {
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
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                orders = loadedOrders
                isLoading = false
            }
    }

    val filteredOrders = if (selectedStatus == null) orders else orders.filter { it.status == selectedStatus }

    val ordersByDate = filteredOrders.groupBy { order ->
        SimpleDateFormat("dd MMMM yyyy", Locale("ru")).format(order.createdAt)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Заказы", fontWeight = FontWeight.Bold) },
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
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedStatus == null,
                    onClick = { selectedStatus = null },
                    label = { Text("Все") }
                )
                OrderStatus.values().forEach { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { selectedStatus = status },
                        label = { Text(status.displayName) }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (orders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                        Spacer(Modifier.height(16.dp))
                        Text("Нет заказов", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ordersByDate.forEach { (date, dateOrders) ->
                        item {
                            Text(
                                date,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(dateOrders) { order ->
                            SellerOrderCard(
                                order = order,
                                onClick = { selectedOrder = order }
                            )
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (selectedOrder != null) {
        SellerOrderDetailsDialog(
            order = selectedOrder!!,
            onDismiss = { selectedOrder = null },
            onStatusChange = { newStatus ->
                updateOrderStatus(selectedOrder!!.id, newStatus, db)
                selectedOrder = null
            },
            onMarkDelivered = {
                updateOrderStatus(selectedOrder!!.id, OrderStatus.DELIVERED, db)
                selectedOrder = null
            }
        )
    }
}

@Composable
fun SellerOrderCard(
    order: Order,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                OrderStatusChip(status = order.status)
            }

            Text("Клиент: ${order.userName}", fontSize = 14.sp)
            Text("Телефон: ${order.userPhone}", fontSize = 14.sp)
            Text("Адрес: ${order.deliveryAddress.address}", fontSize = 14.sp, maxLines = 1)

            Divider()

            order.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${item.dishName} x${item.quantity}",
                        fontSize = 14.sp
                    )
                    Text(
                        "${"%.0f".format(item.totalPrice)} тг",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Итого:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    "${"%.0f".format(order.totalAmount)} тг",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                "Время: ${SimpleDateFormat("HH:mm", Locale("ru")).format(order.createdAt)}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun SellerOrderDetailsDialog(
    order: Order,
    onDismiss: () -> Unit,
    onStatusChange: (OrderStatus) -> Unit,
    onMarkDelivered: () -> Unit
) {
    var showStatusDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 550.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "Детали заказа #${order.id.takeLast(6)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }

                    item { Divider() }

                    item {
                        Text("Клиент", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Имя: ${order.userName}")
                        Text("Телефон: ${order.userPhone}")
                        Text("Email: ${order.userId}")
                    }

                    item {
                        Text("Адрес доставки", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(order.deliveryAddress.address)

                        val addressDetails = listOfNotNull(
                            order.deliveryAddress.apartment.takeIf { it.isNotBlank() }?.let { "Кв. $it" },
                            order.deliveryAddress.entrance.takeIf { it.isNotBlank() }?.let { "Подъезд $it" },
                            order.deliveryAddress.floor.takeIf { it.isNotBlank() }?.let { "Этаж $it" },
                            order.deliveryAddress.intercom.takeIf { it.isNotBlank() }?.let { "Домофон $it" }
                        ).joinToString(", ")

                        if (addressDetails.isNotBlank()) {
                            Text(addressDetails, fontSize = 14.sp, color = Color.Gray)
                        }
                    }

                    item {
                        Text("Оплата: ${order.paymentMethod}", fontSize = 14.sp)
                    }

                    if (order.comment.isNotBlank()) {
                        item {
                            Text("Комментарий", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(order.comment, fontSize = 14.sp)
                        }
                    }

                    item {
                        Text("Состав заказа", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    items(order.items) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.dishName} x${item.quantity}")
                            Text("${"%.0f".format(item.totalPrice)} тг")
                        }
                    }

                    item {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Итого:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(
                                "${"%.0f".format(order.totalAmount)} тг",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    item {
                        Text("Текущий статус", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OrderStatusChip(status = order.status)

                            if (order.status != OrderStatus.DELIVERED && order.status != OrderStatus.CANCELLED) {
                                Button(
                                    onClick = { showStatusDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Изменить статус")
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Закрыть")
                }
            }
        }
    }

    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Изменить статус заказа", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Выберите новый статус:", fontSize = 14.sp)

                    val availableStatuses = OrderStatus.values().filter {
                        it != order.status && it != OrderStatus.CANCELLED
                    }

                    availableStatuses.forEach { status ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showStatusDialog = false
                                    if (status == OrderStatus.DELIVERED) {
                                        onMarkDelivered()
                                    } else {
                                        onStatusChange(status)
                                    }
                                }
                                .padding(8.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when (status) {
                                    OrderStatus.DELIVERED -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    status.displayName,
                                    fontWeight = FontWeight.Medium,
                                    color = when (status) {
                                        OrderStatus.DELIVERED -> Color(0xFF4CAF50)
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                                Icon(
                                    when (status) {
                                        OrderStatus.CONFIRMED -> Icons.Default.CheckCircle
                                        OrderStatus.PREPARING -> Icons.Default.Kitchen
                                        OrderStatus.DELIVERING -> Icons.Default.DeliveryDining
                                        OrderStatus.DELIVERED -> Icons.Default.CheckCircle
                                        else -> Icons.Default.ArrowForward
                                    },
                                    contentDescription = null,
                                    tint = when (status) {
                                        OrderStatus.DELIVERED -> Color(0xFF4CAF50)
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStatusDialog = false }) {
                    Text("Отмена")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}

@Composable
fun OrderStatusChip(status: OrderStatus) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(status.color),
        modifier = Modifier.wrapContentSize()
    ) {
        Text(
            status.displayName,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun updateOrderStatus(
    orderId: String,
    newStatus: OrderStatus,
    db: com.google.firebase.firestore.FirebaseFirestore
) {
    db.collection("orders").document(orderId)
        .update(
            mapOf(
                "status" to newStatus.name,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )
        )
}