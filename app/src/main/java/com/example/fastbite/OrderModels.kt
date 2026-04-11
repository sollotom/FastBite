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

enum class OrderStatus(val displayNameRu: String, val displayNameKz: String, val color: Long) {
    PENDING("Ожидает", "Күтуде", 0xFFFF9800),
    CONFIRMED("Подтвержден", "Расталды", 0xFF2196F3),
    PREPARING("Готовится", "Дайындалуда", 0xFF9C27B0),
    DELIVERING("В доставке", "Жеткізілуде", 0xFF00BCD4),
    DELIVERED("Доставлен", "Жеткізілді", 0xFF4CAF50),
    CANCELLED("Отменен", "Болдырылмады", 0xFFF44336);

    fun localizedName(): String = if (Strings.currentLanguage.value == Language.KAZAKH) displayNameKz else displayNameRu
}

data class OrderItem(
    val dishId: String,
    val dishName: String,
    val quantity: Int,
    val price: Double,
    val totalPrice: Double,
    val photoUrl: String = "",
    val restaurantId: String = "",
    val isDelivered: Boolean = false
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