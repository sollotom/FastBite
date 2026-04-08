package com.example.fastbite

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
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

    // Загружаем заказы для этого ресторана
    LaunchedEffect(restaurantId) {
        db.collection("orders")
            .whereEqualTo("restaurantId", restaurantId)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isLoading = false
                    return@addSnapshotListener
                }

                orders = snapshot?.documents?.mapNotNull { doc ->
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
                                                photoUrl = item["photoUrl"] as? String ?: ""
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // ФИЛЬТРУЕМ ТОЛЬКО БЛЮДА ЭТОГО РЕСТОРАНА
                        // ВАЖНО: Заказ уже отфильтрован по restaurantId,
                        // но на всякий случай фильтруем и items
                        // Если в заказе могут быть блюда из разных ресторанов,
                        // нужно фильтровать по dishId, получая restaurantId из блюда

                        // Для этого нам нужно знать, какие dishId принадлежат этому ресторану
                        // Пока оставляем все items, т.к. заказ уже отфильтрован по restaurantId
                        // и все блюда в нем должны быть из этого ресторана

                        Order(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            userName = doc.getString("userName") ?: "",
                            userPhone = doc.getString("userPhone") ?: "",
                            userAddress = doc.getString("userAddress") ?: "",
                            restaurantId = doc.getString("restaurantId") ?: "",
                            restaurantName = doc.getString("restaurantName") ?: "",
                            items = allItems, // Здесь все блюда из заказа
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
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                isLoading = false
            }
    }

    // Фильтруем заказы по статусу
    val filteredOrders = if (selectedStatus == null) orders else orders.filter { it.status == selectedStatus }

    // Группируем заказы по дате
    val ordersByDate = filteredOrders.groupBy { order ->
        SimpleDateFormat("dd MMMM yyyy", Locale("ru")).format(order.createdAt)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Заказы ресторана", fontWeight = FontWeight.Bold) },
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
            // Фильтры по статусам
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
                FilterChip(
                    selected = selectedStatus == OrderStatus.PENDING,
                    onClick = { selectedStatus = OrderStatus.PENDING },
                    label = { Text("Новые") }
                )
                FilterChip(
                    selected = selectedStatus == OrderStatus.CONFIRMED,
                    onClick = { selectedStatus = OrderStatus.CONFIRMED },
                    label = { Text("Подтверждены") }
                )
                FilterChip(
                    selected = selectedStatus == OrderStatus.PREPARING,
                    onClick = { selectedStatus = OrderStatus.PREPARING },
                    label = { Text("Готовятся") }
                )
                FilterChip(
                    selected = selectedStatus == OrderStatus.READY_FOR_PICKUP,
                    onClick = { selectedStatus = OrderStatus.READY_FOR_PICKUP },
                    label = { Text("Готов к выдаче") }
                )
                FilterChip(
                    selected = selectedStatus == OrderStatus.DELIVERING,
                    onClick = { selectedStatus = OrderStatus.DELIVERING },
                    label = { Text("В доставке") }
                )
                FilterChip(
                    selected = selectedStatus == OrderStatus.DELIVERED,
                    onClick = { selectedStatus = OrderStatus.DELIVERED },
                    label = { Text("Доставлены") }
                )
                FilterChip(
                    selected = selectedStatus == OrderStatus.CANCELLED,
                    onClick = { selectedStatus = OrderStatus.CANCELLED },
                    label = { Text("Отменены") }
                )
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
                        Text("Когда появятся новые заказы, они отобразятся здесь", fontSize = 14.sp, color = Color.Gray)
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
                            SellerOrderCard(order = order, onClick = { selectedOrder = order })
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Диалог деталей заказа
    selectedOrder?.let { order ->
        SellerOrderDetailsDialog(
            order = order,
            onDismiss = { selectedOrder = null },
            onStatusChange = { newStatus ->
                updateOrderStatus(order.id, newStatus, db)
                selectedOrder = null
            }
        )
    }
}

@Composable
fun SellerOrderCard(order: Order, onClick: () -> Unit) {
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

            // Показываем ВСЕ блюда из заказа (они все принадлежат этому ресторану)
            order.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${item.dishName} x${item.quantity}", fontSize = 14.sp)
                    Text("${"%.0f".format(item.totalPrice)} тг", fontSize = 14.sp, fontWeight = FontWeight.Medium)
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

@Composable
fun SellerOrderDetailsDialog(
    order: Order,
    onDismiss: () -> Unit,
    onStatusChange: (OrderStatus) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            // Используем Surface для правильной работы с прокруткой
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()), // verticalScroll должен работать
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Заголовок
                    Text(
                        "Детали заказа #${order.id.takeLast(6)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    Divider()

                    // Информация о клиенте
                    Text("Клиент", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Имя: ${order.userName}")
                    Text("Телефон: ${order.userPhone}")
                    Text("Email: ${order.userId}")

                    // Адрес доставки
                    Text("Адрес доставки", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
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

                    // Способ оплаты
                    Text("Оплата: ${order.paymentMethod}", fontSize = 14.sp)

                    // Комментарий
                    if (order.comment.isNotBlank()) {
                        Text("Комментарий", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
                        Text(order.comment, fontSize = 14.sp)
                    }

                    // Состав заказа
                    Text("Состав заказа", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))

                    order.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.dishName} x${item.quantity}")
                            Text("${"%.0f".format(item.totalPrice)} тг")
                        }
                    }

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

                    // Изменение статуса
                    Text("Изменить статус", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))

                    // Горизонтальная прокрутка для статусов
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OrderStatus.values().forEach { status ->
                            Button(
                                onClick = { onStatusChange(status) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (status == order.status)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                )
                            ) {
                                Text(status.displayName, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("Закрыть")
                    }
                }
            }
        }
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

private fun getItemsWord(count: Int): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> "блюдо"
        count % 10 in 2..4 && count % 100 !in 12..14 -> "блюда"
        else -> "блюд"
    }
}