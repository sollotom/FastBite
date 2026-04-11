package com.example.fastbite

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// Строки для диалога добавления отзыва
object AddReviewStrings {
    var currentLanguage = Strings.currentLanguage

    val leaveReview: String get() = if (currentLanguage.value == Language.KAZAKH) "Пікір қалдыру" else "Оставить отзыв"
    val dish: String get() = if (currentLanguage.value == Language.KAZAKH) "Тағам" else "Блюдо"
    val yourRating: String get() = if (currentLanguage.value == Language.KAZAKH) "Сіздің бағаңыз" else "Ваша оценка"
    val ratingStar: String get() = if (currentLanguage.value == Language.KAZAKH) "Бағалау" else "Рейтинг"
    val yourReview: String get() = if (currentLanguage.value == Language.KAZAKH) "Сіздің пікіріңіз" else "Ваш отзыв"
    val shareImpressions: String get() = if (currentLanguage.value == Language.KAZAKH) "Тағам туралы әсерлеріңізбен бөлісіңіз..." else "Поделитесь впечатлениями о блюде..."
    val pleaseRate: String get() = if (currentLanguage.value == Language.KAZAKH) "Баға қойыңыз" else "Пожалуйста, поставьте оценку"
    val sendReview: String get() = if (currentLanguage.value == Language.KAZAKH) "Пікір жіберу" else "Отправить отзыв"
    val cancel: String get() = if (currentLanguage.value == Language.KAZAKH) "Бас тарту" else "Отмена"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReviewDialog(
    dish: Dish,
    userEmail: String,
    userName: String,
    onDismiss: () -> Unit,
    onReviewAdded: () -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                AddReviewStrings.leaveReview,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "${AddReviewStrings.dish}: ${dish.name}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                // Рейтинг звездами
                Column {
                    Text(
                        AddReviewStrings.yourRating,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(5) { index ->
                            IconButton(
                                onClick = { rating = index + 1 },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    if (index < rating) Icons.Default.Star else Icons.Outlined.Star,
                                    contentDescription = "${AddReviewStrings.ratingStar} ${index + 1}",
                                    tint = if (index < rating) Color(0xFFFFC107) else Color.Gray,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }

                // Поле для комментария
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text(AddReviewStrings.yourReview) },
                    placeholder = { Text(AddReviewStrings.shareImpressions) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )

                if (error.isNotEmpty()) {
                    Text(
                        error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (rating == 0) {
                        error = AddReviewStrings.pleaseRate
                        return@Button
                    }

                    isLoading = true
                    error = ""

                    coroutineScope.launch {
                        ReviewManager.addReview(
                            dishId = dish.id,
                            userEmail = userEmail,
                            userName = userName,
                            rating = rating.toDouble(),
                            comment = comment,
                            onSuccess = {
                                isLoading = false
                                onReviewAdded()
                                onDismiss()
                            },
                            onError = { err ->
                                isLoading = false
                                error = err
                            }
                        )
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(AddReviewStrings.sendReview)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text(AddReviewStrings.cancel)
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}