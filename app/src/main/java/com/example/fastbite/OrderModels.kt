package com.example.fastbite

import java.util.Date

data class Order(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val userAddress: String = "",
    val restaurantId: String = "",
    val restaurantName: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val deliveryAddress: DeliveryAddress = DeliveryAddress(),
    val paymentMethod: String = "Наличными",
    val comment: String = ""
)

enum class OrderStatus(val displayName: String, val color: Long) {
    PENDING("Ожидает подтверждения", 0xFFFFA000),
    CONFIRMED("Подтвержден", 0xFF2196F3),
    PREPARING("Готовится", 0xFFFF9800),
    READY_FOR_PICKUP("Готов к выдаче", 0xFF4CAF50),
    DELIVERING("Доставляется", 0xFF9C27B0),
    DELIVERED("Доставлен", 0xFF4CAF50),
    CANCELLED("Отменен", 0xFFF44336)
}

data class OrderItem(
    val dishId: String = "",
    val dishName: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0,
    val totalPrice: Double = 0.0,
    val photoUrl: String = ""
)

data class DeliveryAddress(
    val address: String = "",
    val apartment: String = "",
    val entrance: String = "",
    val floor: String = "",
    val intercom: String = ""
)

data class Address(
    val id: String = "",
    val address: String = "",
    val apartment: String = "",
    val entrance: String = "",
    val floor: String = "",
    val intercom: String = "",
    val isDefault: Boolean = false
)