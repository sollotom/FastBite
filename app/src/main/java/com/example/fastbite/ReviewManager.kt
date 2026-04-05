package com.example.fastbite

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

object ReviewManager {
    private val db = Firebase.firestore

    // Форматирование даты
    private fun getCurrentDate(): String {
        return SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
    }

    // Добавление отзыва к блюду
    suspend fun addReview(
        dishId: String,
        userEmail: String,
        userName: String,
        rating: Double,
        comment: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // 1. Получаем информацию о блюде
            val dishDoc = db.collection("dishes").document(dishId).get().await()
            val dish = Dish(
                id = dishDoc.id,
                name = dishDoc.getString("name") ?: "",
                owner = dishDoc.getString("owner") ?: ""
            )

            // 2. Создаем объект отзыва
            val reviewId = db.collection("reviews").document().id
            val review = hashMapOf(
                "id" to reviewId,
                "userName" to userName.ifEmpty { userEmail.split("@")[0] },
                "userEmail" to userEmail,
                "rating" to rating,
                "comment" to comment,
                "date" to getCurrentDate(),
                "dishId" to dishId,
                "dishName" to dish.name,
                "restaurantId" to dish.owner
            )

            // 3. Сохраняем отзыв в коллекцию reviews
            db.collection("reviews").document(reviewId).set(review).await()

            // 4. Получаем текущие отзывы блюда
            val dishReviewsSnapshot = db.collection("dishes")
                .document(dishId)
                .collection("reviews")
                .get()
                .await()

            val currentReviews = dishReviewsSnapshot.documents.mapNotNull { doc ->
                Review(
                    id = doc.id,
                    rating = doc.getDouble("rating") ?: 0.0
                )
            }

            // 5. Вычисляем новый средний рейтинг
            val totalRatings = currentReviews.sumOf { it.rating } + rating
            val newRatingCount = currentReviews.size + 1
            val newRatingAverage = totalRatings / newRatingCount

            // 6. Сохраняем отзыв в подколлекцию блюда
            val subReview = hashMapOf(
                "userName" to userName.ifEmpty { userEmail.split("@")[0] },
                "userEmail" to userEmail,
                "rating" to rating,
                "comment" to comment,
                "date" to getCurrentDate()
            )
            db.collection("dishes")
                .document(dishId)
                .collection("reviews")
                .document(reviewId)
                .set(subReview)
                .await()

            // 7. Обновляем рейтинг в самом блюде
            db.collection("dishes").document(dishId).update(
                mapOf(
                    "ratingAverage" to newRatingAverage,
                    "ratingCount" to newRatingCount
                )
            ).await()

            // 8. Обновляем рейтинг ресторана
            updateRestaurantRating(dish.owner)

            onSuccess()
        } catch (e: Exception) {
            onError("Ошибка при добавлении отзыва: ${e.message}")
        }
    }

    // Обновление рейтинга ресторана
    private suspend fun updateRestaurantRating(restaurantId: String) {
        try {
            // Получаем все блюда ресторана
            val dishesSnapshot = db.collection("dishes")
                .whereEqualTo("owner", restaurantId)
                .get()
                .await()

            if (dishesSnapshot.isEmpty) return

            // Вычисляем общий рейтинг
            var totalRating = 0.0
            var totalCount = 0L

            dishesSnapshot.documents.forEach { doc ->
                val rating = doc.getDouble("ratingAverage") ?: 0.0
                val count = doc.getLong("ratingCount") ?: 0L
                totalRating += rating * count
                totalCount += count
            }

            val restaurantRating = if (totalCount > 0) totalRating / totalCount else 0.0

            // Обновляем рейтинг ресторана
            db.collection("restaurants").document(restaurantId).update(
                mapOf(
                    "rating" to restaurantRating,
                    "ratingCount" to totalCount
                )
            ).await()
        } catch (e: Exception) {
            // Логируем ошибку, но не прерываем выполнение
            e.printStackTrace()
        }
    }

    // Получение всех отзывов для блюда
    suspend fun getReviewsForDish(dishId: String): List<Review> {
        return try {
            val snapshot = db.collection("dishes")
                .document(dishId)
                .collection("reviews")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                Review(
                    id = doc.id,
                    userName = doc.getString("userName") ?: "Аноним",
                    userEmail = doc.getString("userEmail") ?: "",
                    rating = doc.getDouble("rating") ?: 0.0,
                    comment = doc.getString("comment") ?: "",
                    date = doc.getString("date") ?: "",
                    dishId = dishId
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Получение всех отзывов для ресторана
    suspend fun getReviewsForRestaurant(restaurantId: String): List<Review> {
        return try {
            val snapshot = db.collection("reviews")
                .whereEqualTo("restaurantId", restaurantId)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                Review(
                    id = doc.id,
                    userName = doc.getString("userName") ?: "Аноним",
                    userEmail = doc.getString("userEmail") ?: "",
                    rating = doc.getDouble("rating") ?: 0.0,
                    comment = doc.getString("comment") ?: "",
                    date = doc.getString("date") ?: "",
                    dishId = doc.getString("dishId") ?: "",
                    dishName = doc.getString("dishName") ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Проверка, может ли пользователь оставить отзыв (заказ должен быть доставлен)
    suspend fun canUserReview(
        userEmail: String,
        dishId: String,
        orderId: String? = null
    ): Boolean {
        return try {
            val query = if (orderId != null) {
                db.collection("orders")
                    .document(orderId)
                    .collection("items")
                    .whereEqualTo("dishId", dishId)
                    .get()
                    .await()
            } else {
                db.collection("orders")
                    .whereEqualTo("userId", userEmail)
                    .whereEqualTo("status", "DELIVERED")
                    .get()
                    .await()
            }

            query.documents.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    // Проверка, оставлял ли пользователь уже отзыв на это блюдо
    suspend fun hasUserReviewed(userEmail: String, dishId: String): Boolean {
        return try {
            val snapshot = db.collection("dishes")
                .document(dishId)
                .collection("reviews")
                .whereEqualTo("userEmail", userEmail)
                .get()
                .await()

            snapshot.documents.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}