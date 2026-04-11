package com.example.fastbite

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Строки для экрана авторизации
object AuthStrings {
    var currentLanguage = Strings.currentLanguage

    val createAccount: String get() = if (currentLanguage.value == Language.KAZAKH) "Аккаунт құру" else "Создать аккаунт"
    val welcome: String get() = if (currentLanguage.value == Language.KAZAKH) "Қош келдіңіз!" else "Добро пожаловать!"
    val registerToOrder: String get() = if (currentLanguage.value == Language.KAZAKH) "Тапсырыс беруді бастау үшін тіркеліңіз" else "Зарегистрируйтесь, чтобы начать заказывать"
    val loginToAccount: String get() = if (currentLanguage.value == Language.KAZAKH) "Аккаунтыңызға кіріңіз" else "Войдите в свой аккаунт"
    val email: String get() = if (currentLanguage.value == Language.KAZAKH) "Email" else "Email"
    val emailPlaceholder: String get() = if (currentLanguage.value == Language.KAZAKH) "example@mail.com" else "example@mail.com"
    val password: String get() = if (currentLanguage.value == Language.KAZAKH) "Құпия сөз" else "Пароль"
    val passwordPlaceholder: String get() = if (currentLanguage.value == Language.KAZAKH) "Кемінде 6 таңба" else "Минимум 6 символов"
    val restaurantName: String get() = if (currentLanguage.value == Language.KAZAKH) "Мейрамхана атауы" else "Название ресторана"
    val yourName: String get() = if (currentLanguage.value == Language.KAZAKH) "Сіздің атыңыз" else "Ваше имя"
    val enterRestaurantName: String get() = if (currentLanguage.value == Language.KAZAKH) "Мейрамхана атауын енгізіңіз" else "Введите название ресторана"
    val enterYourName: String get() = if (currentLanguage.value == Language.KAZAKH) "Атыңызды енгізіңіз" else "Введите ваше имя"
    val registerAsRestaurant: String get() = if (currentLanguage.value == Language.KAZAKH) "Мейрамхана ретінде тіркелу" else "Зарегистрироваться как ресторан"
    val register: String get() = if (currentLanguage.value == Language.KAZAKH) "Тіркелу" else "Зарегистрироваться"
    val login: String get() = if (currentLanguage.value == Language.KAZAKH) "Кіру" else "Войти"
    val haveAccount: String get() = if (currentLanguage.value == Language.KAZAKH) "Аккаунтыңыз бар ма? Кіру" else "Уже есть аккаунт? Войти"
    val noAccount: String get() = if (currentLanguage.value == Language.KAZAKH) "Аккаунтыңыз жоқ па? Тіркелу" else "Нет аккаунта? Регистрация"

    // Ошибки
    val fillAllFields: String get() = if (currentLanguage.value == Language.KAZAKH) "Барлық өрістерді толтырыңыз" else "Заполните все поля"
    val passwordMinLength: String get() = if (currentLanguage.value == Language.KAZAKH) "Құпия сөз кемінде 6 таңбадан тұруы керек" else "Пароль должен быть не менее 6 символов"
    val emailInUse: String get() = if (currentLanguage.value == Language.KAZAKH) "Email бос емес" else "Email уже используется"
    val registrationError: String get() = if (currentLanguage.value == Language.KAZAKH) "Тіркеу қатесі: " else "Ошибка регистрации: "
    val restaurantCreationError: String get() = if (currentLanguage.value == Language.KAZAKH) "Мейрамхана құру қатесі: " else "Ошибка создания ресторана: "
    val dataSaveError: String get() = if (currentLanguage.value == Language.KAZAKH) "Деректерді сақтау қатесі: " else "Ошибка сохранения данных: "
    val dataFetchError: String get() = if (currentLanguage.value == Language.KAZAKH) "Деректерді алу қатесі: " else "Ошибка получения данных: "
    val invalidCredentials: String get() = if (currentLanguage.value == Language.KAZAKH) "Email немесе құпия сөз дұрыс емес" else "Неверный email или пароль"
}

@OptIn(ExperimentalMaterial3Api::class)
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
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var isSeller by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            // Логотип
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(28.dp)),
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "FB",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isRegistering) AuthStrings.createAccount else AuthStrings.welcome,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = if (isRegistering) AuthStrings.registerToOrder else AuthStrings.loginToAccount,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(AuthStrings.email) },
                        placeholder = { Text(AuthStrings.emailPlaceholder) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(AuthStrings.password) },
                        placeholder = { Text(AuthStrings.passwordPlaceholder) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )

                    if (isRegistering) {
                        OutlinedTextField(
                            value = userName,
                            onValueChange = { userName = it },
                            label = { Text(if (isSeller) AuthStrings.restaurantName else AuthStrings.yourName) },
                            placeholder = { Text(if (isSeller) AuthStrings.enterRestaurantName else AuthStrings.enterYourName) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = isSeller,
                                onCheckedChange = { isSeller = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                AuthStrings.registerAsRestaurant,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank() || (isRegistering && userName.isBlank())) {
                                error = AuthStrings.fillAllFields
                                return@Button
                            }
                            if (isRegistering && password.length < 6) {
                                error = AuthStrings.passwordMinLength
                                return@Button
                            }

                            error = ""
                            isLoading = true

                            if (isRegistering) {
                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnSuccessListener {
                                        val role = if (isSeller) "seller" else "user"
                                        val userData = hashMapOf<String, Any>(
                                            "role" to role,
                                            "email" to email,
                                            "name" to userName,
                                            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                        )

                                        db.collection("users").document(email)
                                            .set(userData)
                                            .addOnSuccessListener {
                                                if (role == "seller") {
                                                    val restaurantData = hashMapOf<String, Any>(
                                                        "name" to userName,
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
                                                            isLoading = false
                                                            navToSeller(email, role)
                                                        }
                                                        .addOnFailureListener { e ->
                                                            isLoading = false
                                                            error = AuthStrings.restaurantCreationError + e.message
                                                        }
                                                } else {
                                                    isLoading = false
                                                    navToUser(email, role)
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                isLoading = false
                                                error = AuthStrings.dataSaveError + e.message
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        error = when {
                                            e.message?.contains("email address is already in use") == true -> AuthStrings.emailInUse
                                            else -> AuthStrings.registrationError + e.message
                                        }
                                    }
                            } else {
                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnSuccessListener {
                                        db.collection("users").document(email).get()
                                            .addOnSuccessListener { doc ->
                                                isLoading = false
                                                val role = doc.getString("role") ?: "user"
                                                if (role == "seller") {
                                                    navToSeller(email, role)
                                                } else {
                                                    navToUser(email, role)
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                isLoading = false
                                                error = AuthStrings.dataFetchError + e.message
                                            }
                                    }
                                    .addOnFailureListener {
                                        isLoading = false
                                        error = AuthStrings.invalidCredentials
                                    }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                if (isRegistering) AuthStrings.register else AuthStrings.login,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    isRegistering = !isRegistering
                    userName = ""
                    isSeller = false
                    error = ""
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = if (isRegistering) AuthStrings.haveAccount else AuthStrings.noAccount,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (error.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}