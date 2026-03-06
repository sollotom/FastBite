package com.example.fastbite

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AuthScreen(
    navToUser: (email: String, role: String) -> Unit,
    navToSeller: (email: String, role: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isSeller by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = if (isRegistering) "Регистрация" else "Вход",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                placeholder = { Text("example@mail.com") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                placeholder = { Text("Минимум 6 символов") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = icon, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (isRegistering) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Название ресторана / Имя") },
                    placeholder = { Text(if (isSeller) "Название ресторана" else "Иван") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isSeller,
                        onCheckedChange = { isSeller = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Зарегистрироваться как ресторан", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    error = ""
                    if (isRegistering) {
                        // РЕГИСТРАЦИЯ
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener {
                                val role = if (isSeller) "seller" else "user"

                                // Сохраняем в коллекцию users
                                val userData = hashMapOf<String, Any>(
                                    "role" to role,
                                    "email" to email
                                )
                                if (userName.isNotBlank()) {
                                    userData["name"] = userName
                                }

                                db.collection("users").document(email)
                                    .set(userData)
                                    .addOnSuccessListener {
                                        if (role == "seller") {
                                            // Для продавца создаем запись в restaurants
                                            val restaurantData = hashMapOf<String, Any>(
                                                "name" to (if (userName.isNotBlank()) userName else "Мой ресторан"),
                                                "email" to email,
                                                "description" to "",
                                                "iconUrl" to "",
                                                "coverUrl" to "",
                                                "rating" to 0.0,
                                                "ratingCount" to 0L,
                                                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                            )

                                            db.collection("restaurants").document(email)
                                                .set(restaurantData)
                                                .addOnSuccessListener {
                                                    // Переходим в профиль продавца
                                                    navToSeller(email, role)
                                                }
                                                .addOnFailureListener { e ->
                                                    error = "Ошибка создания ресторана: ${e.message}"
                                                }
                                        } else {
                                            // Переходим в профиль пользователя
                                            navToUser(email, role)
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        error = "Ошибка сохранения данных: ${e.message}"
                                    }
                            }
                            .addOnFailureListener { e ->
                                error = "Ошибка регистрации: ${e.message}"
                            }
                    } else {
                        // ВХОД
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener {
                                db.collection("users").document(email).get()
                                    .addOnSuccessListener { doc ->
                                        val role = doc.getString("role") ?: "user"
                                        if (role == "seller") {
                                            navToSeller(email, role)
                                        } else {
                                            navToUser(email, role)
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        error = "Ошибка получения роли: ${e.message}"
                                    }
                            }
                            .addOnFailureListener { e ->
                                error = "Неверный email или пароль"
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(if (isRegistering) "Зарегистрироваться" else "Войти", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = {
                    isRegistering = !isRegistering
                    userName = ""
                    isSeller = false
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = if (isRegistering) "Есть аккаунт? Войти" else "Нет аккаунта? Регистрация",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (error.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}