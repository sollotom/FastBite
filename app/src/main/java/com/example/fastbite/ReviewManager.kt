package com.example.fastbite

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

object ReviewManager {
    private val db = Firebase.firestore

    private fun getCurrentDate(): String {
        return SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
    }

    // Строки ошибок
    private val addReviewError: String get() = if (Strings.currentLanguage.value == Language.KAZAKH)
        "Пікір қосу кезінде қате: " else "Ошибка при добавлении отзыва: "
    private val updateReviewError: String get() = if (Strings.currentLanguage.value == Language.KAZAKH)
        "Пікірді жаңарту кезінде қате: " else "Ошибка при обновлении отзыва: "
    private val anonymous: String get() = if (Strings.currentLanguage.value == Language.KAZAKH)
        "Аноним" else "Аноним"

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
            val dishDoc = db.collection("dishes").document(dishId).get().await()
            val dishName = dishDoc.getString("name") ?: ""
            val restaurantId = dishDoc.getString("owner") ?: ""

            val reviewId = db.collection("reviews").document().id
            val review = hashMapOf(
                "id" to reviewId,
                "userName" to userName.ifEmpty { userEmail.split("@")[0] },
                "userEmail" to userEmail,
                "rating" to rating,
                "comment" to comment,
                "date" to getCurrentDate(),
                "dishId" to dishId,
                "dishName" to dishName,
                "restaurantId" to restaurantId
            )

            db.collection("reviews").document(reviewId).set(review).await()

            // Обновляем рейтинг блюда
            updateDishRating(dishId)
            updateRestaurantRating(restaurantId)

            onSuccess()
        } catch (e: Exception) {
            onError(addReviewError + e.message)
        }
    }

    suspend fun updateReview(
        reviewId: String,
        rating: Double,
        comment: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val reviewDoc = db.collection("reviews").document(reviewId).get().await()
            val dishId = reviewDoc.getString("dishId") ?: ""
            val restaurantId = reviewDoc.getString("restaurantId") ?: ""

            val updates = hashMapOf<String, Any>(
                "rating" to rating,
                "comment" to comment,
                "date" to getCurrentDate()
            )

            db.collection("reviews").document(reviewId).update(updates).await()

            // Обновляем рейтинг блюда
            updateDishRating(dishId)
            updateRestaurantRating(restaurantId)

            onSuccess()
        } catch (e: Exception) {
            onError(updateReviewError + e.message)
        }
    }

    private suspend fun updateDishRating(dishId: String) {
        val reviewsSnapshot = db.collection("reviews")
            .whereEqualTo("dishId", dishId)
            .get()
            .await()

        val reviews = reviewsSnapshot.documents
        if (reviews.isEmpty()) {
            db.collection("dishes").document(dishId).update(
                mapOf(
                    "ratingAverage" to 0.0,
                    "ratingCount" to 0
                )
            ).await()
            return
        }

        var totalRating = 0.0
        reviews.forEach { doc ->
            totalRating += doc.getDouble("rating") ?: 0.0
        }
        val avgRating = totalRating / reviews.size

        db.collection("dishes").document(dishId).update(
            mapOf(
                "ratingAverage" to avgRating,
                "ratingCount" to reviews.size
            )
        ).await()
    }

    private suspend fun updateRestaurantRating(restaurantId: String) {
        val dishesSnapshot = db.collection("dishes")
            .whereEqualTo("owner", restaurantId)
            .get()
            .await()

        if (dishesSnapshot.isEmpty) return

        var totalRating = 0.0
        var totalCount = 0L

        dishesSnapshot.documents.forEach { doc ->
            val rating = doc.getDouble("ratingAverage") ?: 0.0
            val count = doc.getLong("ratingCount") ?: 0L
            totalRating += rating * count
            totalCount += count
        }

        val restaurantRating = if (totalCount > 0) totalRating / totalCount else 0.0

        db.collection("restaurants").document(restaurantId).update(
            mapOf(
                "rating" to restaurantRating,
                "ratingCount" to totalCount
            )
        ).await()
    }

    suspend fun getReviewsForDish(dishId: String): List<Review> {
        return try {
            val snapshot = db.collection("reviews")
                .whereEqualTo("dishId", dishId)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                Review(
                    id = doc.id,
                    userName = doc.getString("userName") ?: anonymous,
                    userEmail = doc.getString("userEmail") ?: "",
                    rating = doc.getDouble("rating") ?: 0.0,
                    comment = doc.getString("comment") ?: "",
                    date = doc.getString("date") ?: "",
                    dishId = dishId,
                    dishName = doc.getString("dishName") ?: "",
                    restaurantId = doc.getString("restaurantId") ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}