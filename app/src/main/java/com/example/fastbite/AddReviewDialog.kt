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
                "Оставить отзыв",
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
                    "Блюдо: ${dish.name}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                // Рейтинг звездами
                Column {
                    Text(
                        "Ваша оценка",
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
                                    contentDescription = "Рейтинг ${index + 1}",
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
                    label = { Text("Ваш отзыв") },
                    placeholder = { Text("Поделитесь впечатлениями о блюде...") },
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
                        error = "Пожалуйста, поставьте оценку"
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
                    Text("Отправить отзыв")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Отмена")
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}