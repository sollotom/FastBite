package com.example.fastbite

import java.util.Date

// Модель заказа
data class Order(
    val id: String = "",
    val userId: String = "",           // Email покупателя
    val userName: String = "",          // Имя покупателя
    val userPhone: String = "",         // Телефон покупателя
    val userAddress: String = "",       // Адрес доставки
    val restaurantId: String = "",      // Email ресторана
    val restaurantName: String = "",    // Название ресторана
    val items: List<OrderItem> = emptyList(), // Товары в заказе
    val totalAmount: Double = 0.0,      // Общая сумма
    val status: OrderStatus = OrderStatus.PENDING, // Статус заказа
    val createdAt: Date = Date(),       // Дата создания
    val updatedAt: Date = Date(),       // Дата обновления
    val deliveryAddress: DeliveryAddress = DeliveryAddress(), // Адрес доставки
    val paymentMethod: String = "Наличными", // Способ оплаты
    val comment: String = ""            // Комментарий к заказу
)

// Статусы заказа
enum class OrderStatus(val displayName: String) {
    PENDING("Ожидает подтверждения"),
    CONFIRMED("Подтвержден"),
    PREPARING("Готовится"),
    READY_FOR_PICKUP("Готов к выдаче"),
    DELIVERING("Доставляется"),
    DELIVERED("Доставлен"),
    CANCELLED("Отменен")
}

// Элемент заказа
data class OrderItem(
    val dishId: String = "",
    val dishName: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0,
    val totalPrice: Double = 0.0,
    val photoUrl: String = ""
)

// Адрес доставки
data class DeliveryAddress(
    val address: String = "",
    val apartment: String = "",
    val entrance: String = "",
    val floor: String = "",
    val intercom: String = ""
)