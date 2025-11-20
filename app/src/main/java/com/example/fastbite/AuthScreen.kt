package com.example.fastbite

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AuthScreen(
    onLoginSuccess: (String, String) -> Unit // email и роль
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = if (isRegistering) "Регистрация" else "Вход",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    errorMessage = ""
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Заполните все поля"
                        return@Button
                    }

                    if (isRegistering) {
                        // Регистрация пользователя с ролью user
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val user = task.result?.user
                                    if (user != null) {
                                        val data = hashMapOf("role" to "user")
                                        db.collection("users").document(user.uid)
                                            .set(data)
                                            .addOnSuccessListener {
                                                onLoginSuccess(user.email ?: "", "user")
                                            }
                                            .addOnFailureListener {
                                                errorMessage = "Ошибка при сохранении роли"
                                            }
                                    } else {
                                        errorMessage = "Не удалось получить пользователя после регистрации"
                                    }
                                } else {
                                    errorMessage = getAuthErrorMessage(task.exception!!)
                                }
                            }
                    } else {
                        // Вход
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    if (user != null) {
                                        db.collection("users").document(user.uid).get()
                                            .addOnSuccessListener { document ->
                                                val role = document.getString("role") ?: "user"
                                                onLoginSuccess(user.email ?: "", role)
                                            }
                                            .addOnFailureListener {
                                                errorMessage = "Ошибка при получении роли"
                                            }
                                    } else {
                                        errorMessage = "Пользователь не найден"
                                    }
                                } else {
                                    errorMessage = getAuthErrorMessage(task.exception!!)
                                }
                            }
                    }

                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (isRegistering) "Зарегистрироваться" else "Войти")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { isRegistering = !isRegistering }) {
                Text(if (isRegistering) "Уже есть аккаунт? Войти" else "Нет аккаунта? Зарегистрироваться")
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun getAuthErrorMessage(e: Exception): String {
    return when {
        e.message?.contains("ERROR_INVALID_EMAIL") == true -> "Неверный формат email"
        e.message?.contains("ERROR_EMAIL_ALREADY_IN_USE") == true -> "Этот email уже используется"
        e.message?.contains("ERROR_WEAK_PASSWORD") == true -> "Пароль должен содержать минимум 6 символов"
        e.message?.contains("ERROR_OPERATION_NOT_ALLOWED") == true -> "Регистрация по email отключена в Firebase"
        e.message?.contains("ERROR_TOO_MANY_REQUESTS") == true -> "Слишком много попыток. Попробуйте позже"
        else -> "Ошибка: ${e.localizedMessage}"
    }
}
