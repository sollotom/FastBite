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

@Composable
fun AuthScreen(
    onLoginSuccess: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(true) } // сначала регистрация
    var errorMessage by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

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

            if (isRegistering) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Повторите пароль") },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    errorMessage = ""
                    if (email.isBlank() || password.isBlank() || (isRegistering && confirmPassword.isBlank())) {
                        errorMessage = "Заполните все поля"
                        return@Button
                    }

                    if (isRegistering) {
                        if (password != confirmPassword) {
                            errorMessage = "Пароли не совпадают"
                            return@Button
                        }

                        // Регистрация Firebase
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    onLoginSuccess(email)
                                } else {
                                    errorMessage = task.exception?.message ?: "Ошибка регистрации"
                                }
                            }
                    } else {
                        // Вход Firebase
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    onLoginSuccess(email)
                                } else {
                                    errorMessage = "Аккаунт не найден. Зарегистрируйтесь"
                                }
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (isRegistering) "Зарегистрироваться" else "Войти")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = {
                isRegistering = !isRegistering
                errorMessage = ""
            }) {
                Text(
                    text = if (isRegistering)
                        "Уже есть аккаунт? Войти"
                    else
                        "Нет аккаунта? Зарегистрироваться"
                )
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
