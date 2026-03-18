package com.example.fastbite

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Data class для элемента корзины
data class CartItem(
    val dish: Dish,
    var quantity: Int = 1,
    val addedAt: Long = System.currentTimeMillis()
)

// Firestore модель для корзины
data class FirestoreCartItem(
    val dishId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val photoUrl: String = "",
    val quantity: Int = 1,
    val addedAt: Long = System.currentTimeMillis(),
    val discount: String = "",
    val restaurantId: String = ""
)

// Глобальное состояние корзины с синхронизацией Firebase
object CartManager {
    private val _cartItems = mutableStateListOf<CartItem>()
    val cartItems: SnapshotStateList<CartItem> = _cartItems

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    // Для выполнения асинхронных операций
    private val ioScope = CoroutineScope(Dispatchers.IO)

    // ID пользователя
    private val userId: String?
        get() = auth.currentUser?.uid

    // Инициализация загрузки корзины из Firebase
    init {
        loadCartFromFirebase()
    }

    // Загрузить корзину из Firebase
    private fun loadCartFromFirebase() {
        userId?.let { uid ->
            db.collection("carts").document(uid)
                .collection("items")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }

                    snapshot?.let { querySnapshot ->
                        val items = mutableListOf<CartItem>()
                        for (document in querySnapshot.documents) {
                            val firestoreItem = document.toObject(FirestoreCartItem::class.java)
                            firestoreItem?.let { item ->
                                // Создаем объект Dish на основе данных из Firestore
                                val dish = Dish(
                                    id = item.dishId,
                                    name = item.name,
                                    price = item.price.toString(),
                                    photoUrl = item.photoUrl,
                                    discount = item.discount,
                                    owner = item.restaurantId
                                )
                                items.add(CartItem(dish, item.quantity, item.addedAt))
                            }
                        }
                        // Обновляем локальную корзину
                        _cartItems.clear()
                        _cartItems.addAll(items.sortedBy { it.addedAt })
                    }
                }
        }
    }

    // Сохранить элемент корзины в Firebase
    private suspend fun saveCartItemToFirebase(dish: Dish, quantity: Int) {
        val uid = userId ?: return

        val firestoreItem = FirestoreCartItem(
            dishId = dish.id,
            name = dish.name,
            price = dish.price.toDoubleOrNull() ?: 0.0,
            photoUrl = dish.photoUrl,
            quantity = quantity,
            discount = dish.discount ?: "",
            restaurantId = dish.owner
        )

        db.collection("carts").document(uid)
            .collection("items")
            .document(dish.id)
            .set(firestoreItem)
            .await()
    }

    // Удалить элемент корзины из Firebase
    private suspend fun removeCartItemFromFirebase(dishId: String) {
        val uid = userId ?: return

        db.collection("carts").document(uid)
            .collection("items")
            .document(dishId)
            .delete()
            .await()
    }

    // Обновить количество в Firebase
    private suspend fun updateQuantityInFirebase(dishId: String, quantity: Int) {
        val uid = userId ?: return

        db.collection("carts").document(uid)
            .collection("items")
            .document(dishId)
            .update("quantity", quantity)
            .await()
    }

    // Очистить корзину в Firebase
    private suspend fun clearCartInFirebase() {
        val uid = userId ?: return

        val snapshot = db.collection("carts").document(uid)
            .collection("items")
            .get()
            .await()

        for (document in snapshot.documents) {
            document.reference.delete().await()
        }
    }

    // Добавить блюдо в корзину
    fun addToCart(dish: Dish) {
        val existingItem = _cartItems.find { it.dish.id == dish.id }
        if (existingItem != null) {
            incrementQuantity(dish.id)
        } else {
            val newItem = CartItem(dish)
            _cartItems.add(newItem)

            ioScope.launch {
                saveCartItemToFirebase(dish, 1)
            }
        }
    }

    // Удалить блюдо из корзины
    fun removeFromCart(dishId: String) {
        _cartItems.removeAll { it.dish.id == dishId }

        ioScope.launch {
            removeCartItemFromFirebase(dishId)
        }
    }

    // Обновить количество
    fun updateQuantity(dishId: String, quantity: Int) {
        val item = _cartItems.find { it.dish.id == dishId }
        if (item != null) {
            if (quantity > 0) {
                item.quantity = quantity
                ioScope.launch {
                    updateQuantityInFirebase(dishId, quantity)
                }
            } else {
                removeFromCart(dishId)
            }
        }
    }

    // Увеличить количество на 1
    fun incrementQuantity(dishId: String) {
        val item = _cartItems.find { it.dish.id == dishId }
        item?.let {
            it.quantity++
            ioScope.launch {
                updateQuantityInFirebase(dishId, it.quantity)
            }
        }
    }

    // Уменьшить количество на 1
    fun decrementQuantity(dishId: String) {
        val item = _cartItems.find { it.dish.id == dishId }
        item?.let {
            if (it.quantity > 1) {
                it.quantity--
                ioScope.launch {
                    updateQuantityInFirebase(dishId, it.quantity)
                }
            } else {
                removeFromCart(dishId)
            }
        }
    }

    // Алиас для decrementQuantity (для обратной совместимости)
    fun decreaseQuantity(dishId: String) {
        decrementQuantity(dishId)
    }

    // Очистить корзину
    fun clearCart() {
        _cartItems.clear()
        ioScope.launch {
            clearCartInFirebase()
        }
    }

    // Получить общую сумму
    fun getTotalPrice(): Double {
        return _cartItems.sumOf { item ->
            val discountPercentage = item.dish.discount?.toDoubleOrNull() ?: 0.0
            val originalPrice = item.dish.price.toDoubleOrNull() ?: 0.0
            val discountedPrice = if (discountPercentage > 0)
                originalPrice * (1 - discountPercentage / 100)
            else originalPrice
            discountedPrice * item.quantity
        }
    }

    // Получить общее количество товаров
    fun getTotalItems(): Int {
        return _cartItems.sumOf { it.quantity }
    }

    // Получить размер корзины (количество уникальных товаров)
    fun getCartSize(): Int {
        return _cartItems.size
    }

    // Получить количество конкретного товара
    fun getItemQuantity(dishId: String): Int {
        return _cartItems.find { it.dish.id == dishId }?.quantity ?: 0
    }

    // Проверить, есть ли блюдо в корзине
    fun isInCart(dishId: String): Boolean {
        return _cartItems.any { it.dish.id == dishId }
    }

    // Получить все элементы корзины
    fun getCartItems(): List<CartItem> {
        return _cartItems.toList()
    }
}

// Composable функция для наблюдения за количеством товара
@Composable
fun rememberCartItemQuantity(dishId: String): Int {
    var quantity by remember { mutableStateOf(CartManager.getItemQuantity(dishId)) }

    // Обновляем при изменении корзины (простой способ - перечитывать при рекомпозиции)
    LaunchedEffect(dishId, CartManager.getCartSize()) {
        quantity = CartManager.getItemQuantity(dishId)
    }

    return quantity
}