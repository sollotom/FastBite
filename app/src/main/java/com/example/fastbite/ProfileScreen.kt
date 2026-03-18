package com.example.fastbite

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userEmail: String,
    onLogout: () -> Unit,
    onNavigateToOrders: () -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val coroutineScope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf<ProfileScreenType>(ProfileScreenType.Main) }
    var previousScreens by remember { mutableStateOf(listOf<ProfileScreenType>()) }

    var selectedAddressForEdit by remember { mutableStateOf<Address?>(null) }
    var addressToDelete by remember { mutableStateOf<Address?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var userName by remember { mutableStateOf("") }
    var userPhone by remember { mutableStateOf("") }
    var addresses by remember { mutableStateOf<List<Address>>(emptyList()) }
    var selectedAddressId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Настройки
    var notificationsEnabled by remember { mutableStateOf(true) }
    var emailEnabled by remember { mutableStateOf(false) }
    var language by remember { mutableStateOf("Русский") }
    var theme by remember { mutableStateOf("Светлая") }

    // Функция для навигации
    fun navigateTo(screen: ProfileScreenType) {
        previousScreens = previousScreens + currentScreen
        currentScreen = screen
    }

    fun navigateBack() {
        if (previousScreens.isNotEmpty()) {
            currentScreen = previousScreens.last()
            previousScreens = previousScreens.dropLast(1)
        } else {
            currentScreen = ProfileScreenType.Main
        }
    }

    // Обработка кнопки "Назад"
    BackHandler {
        if (currentScreen != ProfileScreenType.Main) {
            navigateBack()
        }
    }

    // Загрузка данных пользователя
    LaunchedEffect(userEmail) {
        if (userEmail.isBlank()) {
            return@LaunchedEffect
        }

        isLoading = true
        try {
            // Загружаем данные пользователя
            val userDoc = db.collection("users").document(userEmail).get().await()
            if (userDoc.exists()) {
                userName = userDoc.getString("name") ?: ""
                userPhone = userDoc.getString("phone") ?: ""
            }

            // Загружаем адреса из коллекции "address"
            loadAddressesFromFirestore(
                userEmail = userEmail,
                db = db,
                onResult = { loadedAddresses ->
                    addresses = loadedAddresses
                    selectedAddressId = loadedAddresses.find { it.isDefault }?.id
                        ?: loadedAddresses.firstOrNull()?.id
                    isLoading = false
                },
                onError = { errorMessage ->
                    isLoading = false
                }
            )

        } catch (e: Exception) {
            e.printStackTrace()
            isLoading = false
        }
    }

    // Диалог подтверждения удаления
    if (showDeleteDialog && addressToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                addressToDelete = null
            },
            title = {
                Text(
                    "Удалить адрес",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Вы уверены, что хотите удалить адрес:\n${addressToDelete!!.address}?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                deleteAddressFromFirestore(userEmail, addressToDelete!!.id, db)
                                Toast.makeText(context, "Адрес удален", Toast.LENGTH_SHORT).show()
                                // Перезагружаем адреса после удаления
                                loadAddressesFromFirestore(
                                    userEmail = userEmail,
                                    db = db,
                                    onResult = { loadedAddresses ->
                                        addresses = loadedAddresses
                                        selectedAddressId = loadedAddresses.find { it.isDefault }?.id
                                            ?: loadedAddresses.firstOrNull()?.id
                                    },
                                    onError = { errorMessage ->
                                    }
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                showDeleteDialog = false
                                addressToDelete = null
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    addressToDelete = null
                }) {
                    Text("Отмена")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (currentScreen) {
                            ProfileScreenType.Main -> "Профиль"
                            ProfileScreenType.Settings -> "Настройки"
                            ProfileScreenType.EditProfile -> "Редактировать профиль"
                            ProfileScreenType.Addresses -> "Адреса доставки"
                            ProfileScreenType.AddAddress -> "Добавить адрес"
                            ProfileScreenType.EditAddress -> "Редактировать адрес"
                            ProfileScreenType.Help -> "Помощь и поддержка"
                            ProfileScreenType.FAQ -> "Часто задаваемые вопросы"
                            ProfileScreenType.ContactSupport -> "Связаться с поддержкой"
                            ProfileScreenType.TermsAndConditions -> "Правила и условия"
                            ProfileScreenType.AboutApp -> "О приложении"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                navigationIcon = {
                    if (currentScreen != ProfileScreenType.Main) {
                        IconButton(onClick = { navigateBack() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Назад"
                            )
                        }
                    }
                },
                actions = {
                    when (currentScreen) {
                        ProfileScreenType.Main -> {
                            IconButton(onClick = { navigateTo(ProfileScreenType.Settings) }) {
                                Icon(
                                    Icons.Outlined.Settings,
                                    contentDescription = "Настройки"
                                )
                            }
                        }
                        else -> {}
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    ProfileScreenType.Main -> {
                        MainProfileScreen(
                            userName = userName,
                            userEmail = userEmail,
                            userPhone = userPhone,
                            addresses = addresses,
                            onNavigateToOrders = onNavigateToOrders,
                            onNavigateToSettings = { navigateTo(ProfileScreenType.Settings) },
                            onNavigateToAddresses = { navigateTo(ProfileScreenType.Addresses) },
                            onNavigateToHelp = { navigateTo(ProfileScreenType.Help) }
                        )
                    }
                    ProfileScreenType.Settings -> {
                        SettingsScreen(
                            userName = userName,
                            userPhone = userPhone,
                            userEmail = userEmail,
                            notificationsEnabled = notificationsEnabled,
                            emailEnabled = emailEnabled,
                            language = language,
                            theme = theme,
                            onNotificationsChange = { notificationsEnabled = it },
                            onEmailChange = { emailEnabled = it },
                            onLanguageChange = { language = it },
                            onThemeChange = { theme = it },
                            onEditProfile = { navigateTo(ProfileScreenType.EditProfile) },
                            onLogout = onLogout,
                            onBack = { navigateBack() }
                        )
                    }
                    ProfileScreenType.EditProfile -> {
                        EditProfileScreen(
                            userName = userName,
                            userPhone = userPhone,
                            userEmail = userEmail,
                            onSave = { name, phone ->
                                coroutineScope.launch {
                                    saveUserToFirestore(userEmail, name, phone, db)
                                    userName = name
                                    userPhone = phone
                                    navigateBack()
                                }
                            },
                            onBack = { navigateBack() }
                        )
                    }
                    ProfileScreenType.Addresses -> {
                        AddressesScreen(
                            addresses = addresses,
                            onAddAddress = { navigateTo(ProfileScreenType.AddAddress) },
                            onEditAddress = { address ->
                                selectedAddressForEdit = address
                                navigateTo(ProfileScreenType.EditAddress)
                            },
                            onDeleteAddress = { address ->
                                addressToDelete = address
                                showDeleteDialog = true
                            },
                            onSetDefaultAddress = { address ->
                                coroutineScope.launch {
                                    try {
                                        setDefaultAddress(userEmail, address.id, db)
                                        Toast.makeText(context, "Основной адрес изменен", Toast.LENGTH_SHORT).show()
                                        // Перезагружаем адреса после установки основного
                                        loadAddressesFromFirestore(
                                            userEmail = userEmail,
                                            db = db,
                                            onResult = { loadedAddresses ->
                                                addresses = loadedAddresses
                                                selectedAddressId = loadedAddresses.find { it.isDefault }?.id
                                            },
                                            onError = { errorMessage ->
                                            }
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            },
                            onBack = { navigateBack() }
                        )
                    }
                    ProfileScreenType.AddAddress -> {
                        AddEditAddressScreen(
                            address = null,
                            onSave = { newAddress ->
                                coroutineScope.launch {
                                    try {
                                        addAddressToFirestore(userEmail, newAddress, db)
                                        Toast.makeText(context, "Адрес добавлен", Toast.LENGTH_SHORT).show()
                                        // Перезагружаем адреса после добавления
                                        loadAddressesFromFirestore(
                                            userEmail = userEmail,
                                            db = db,
                                            onResult = { loadedAddresses ->
                                                addresses = loadedAddresses
                                                selectedAddressId = loadedAddresses.find { it.isDefault }?.id
                                                    ?: loadedAddresses.firstOrNull()?.id
                                            },
                                            onError = { errorMessage ->
                                            }
                                        )
                                        navigateBack()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            },
                            onBack = { navigateBack() }
                        )
                    }
                    ProfileScreenType.EditAddress -> {
                        if (selectedAddressForEdit != null) {
                            AddEditAddressScreen(
                                address = selectedAddressForEdit,
                                onSave = { updatedAddress ->
                                    coroutineScope.launch {
                                        try {
                                            updateAddressInFirestore(userEmail, updatedAddress, db)
                                            Toast.makeText(context, "Адрес обновлен", Toast.LENGTH_SHORT).show()
                                            // Перезагружаем адреса после обновления
                                            loadAddressesFromFirestore(
                                                userEmail = userEmail,
                                                db = db,
                                                onResult = { loadedAddresses ->
                                                    addresses = loadedAddresses
                                                    selectedAddressId = loadedAddresses.find { it.isDefault }?.id
                                                        ?: loadedAddresses.firstOrNull()?.id
                                                },
                                                onError = { errorMessage ->
                                                }
                                            )
                                            navigateBack()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                },
                                onBack = {
                                    navigateBack()
                                    selectedAddressForEdit = null
                                }
                            )
                        }
                    }
                    ProfileScreenType.Help -> {
                        HelpScreen(
                            onNavigateToFAQ = { navigateTo(ProfileScreenType.FAQ) },
                            onNavigateToContactSupport = { navigateTo(ProfileScreenType.ContactSupport) },
                            onNavigateToTerms = { navigateTo(ProfileScreenType.TermsAndConditions) },
                            onNavigateToAbout = { navigateTo(ProfileScreenType.AboutApp) },
                            onBack = { navigateBack() }
                        )
                    }
                    ProfileScreenType.FAQ -> {
                        FAQScreen(onBack = { navigateBack() })
                    }
                    ProfileScreenType.ContactSupport -> {
                        ContactSupportScreen(
                            userEmail = userEmail,
                            userName = userName,
                            onBack = { navigateBack() }
                        )
                    }
                    ProfileScreenType.TermsAndConditions -> {
                        TermsAndConditionsScreen(onBack = { navigateBack() })
                    }
                    ProfileScreenType.AboutApp -> {
                        AboutAppScreen(onBack = { navigateBack() })
                    }
                }
            }
        }
    }
}

// Функция для загрузки адресов из Firestore
fun loadAddressesFromFirestore(
    userEmail: String,
    db: FirebaseFirestore,
    onResult: (List<Address>) -> Unit,
    onError: (String) -> Unit
) {
    db.collection("users")
        .document(userEmail)
        .collection("address")
        .get()
        .addOnSuccessListener { documents ->
            val loadedAddresses = documents.mapNotNull { doc ->
                try {
                    Address(
                        id = doc.id,
                        address = doc.getString("address") ?: return@mapNotNull null,
                        apartment = doc.getString("apartment") ?: "",
                        entrance = doc.getString("entrance") ?: "",
                        floor = doc.getString("floor") ?: "",
                        intercom = doc.getString("intercom") ?: "",
                        isDefault = doc.getBoolean("isDefault") ?: false
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            // Сортируем вручную: сначала основные
            val sortedAddresses = loadedAddresses.sortedByDescending { it.isDefault }
            onResult(sortedAddresses)
        }
        .addOnFailureListener { exception ->
            exception.printStackTrace()
            onError(exception.message ?: "Unknown error")
        }
}

// Функция для установки основного адреса
private suspend fun setDefaultAddress(
    email: String,
    addressId: String,
    db: FirebaseFirestore
) {
    try {
        val addressesSnapshot = db.collection("users")
            .document(email)
            .collection("address")
            .whereEqualTo("isDefault", true)
            .get()
            .await()

        for (doc in addressesSnapshot.documents) {
            doc.reference.update("isDefault", false).await()
        }

        db.collection("users")
            .document(email)
            .collection("address")
            .document(addressId)
            .update("isDefault", true)
            .await()
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
}

// Функция для добавления адреса
private suspend fun addAddressToFirestore(
    email: String,
    address: Address,
    db: FirebaseFirestore
) {
    try {
        if (address.isDefault) {
            val addressesSnapshot = db.collection("users")
                .document(email)
                .collection("address")
                .whereEqualTo("isDefault", true)
                .get()
                .await()

            for (doc in addressesSnapshot.documents) {
                doc.reference.update("isDefault", false).await()
            }
        }

        val addressData = hashMapOf(
            "address" to address.address,
            "apartment" to address.apartment,
            "entrance" to address.entrance,
            "floor" to address.floor,
            "intercom" to address.intercom,
            "isDefault" to address.isDefault,
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        db.collection("users")
            .document(email)
            .collection("address")
            .add(addressData)
            .await()
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
}

// Функция для обновления адреса
private suspend fun updateAddressInFirestore(
    email: String,
    address: Address,
    db: FirebaseFirestore
) {
    try {
        if (address.isDefault) {
            val addressesSnapshot = db.collection("users")
                .document(email)
                .collection("address")
                .whereEqualTo("isDefault", true)
                .get()
                .await()

            for (doc in addressesSnapshot.documents) {
                if (doc.id != address.id) {
                    doc.reference.update("isDefault", false).await()
                }
            }
        }

        val addressData = hashMapOf(
            "address" to address.address,
            "apartment" to address.apartment,
            "entrance" to address.entrance,
            "floor" to address.floor,
            "intercom" to address.intercom,
            "isDefault" to address.isDefault
        )

        db.collection("users")
            .document(email)
            .collection("address")
            .document(address.id)
            .set(addressData)
            .await()
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
}

// Функция для удаления адреса
private suspend fun deleteAddressFromFirestore(
    email: String,
    addressId: String,
    db: FirebaseFirestore
) {
    try {
        db.collection("users")
            .document(email)
            .collection("address")
            .document(addressId)
            .delete()
            .await()
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
}

// Функция для сохранения пользователя
private suspend fun saveUserToFirestore(
    email: String,
    name: String,
    phone: String,
    db: FirebaseFirestore
) {
    try {
        val userData = hashMapOf(
            "email" to email,
            "name" to name,
            "phone" to phone,
            "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        db.collection("users").document(email)
            .set(userData, com.google.firebase.firestore.SetOptions.merge())
            .await()
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
}

enum class ProfileScreenType {
    Main, Settings, EditProfile, Addresses, AddAddress, EditAddress, Help, FAQ, ContactSupport, TermsAndConditions, AboutApp
}

@Composable
fun ProfileHeader(
    userName: String,
    userEmail: String,
    userPhone: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (userName.isNotBlank()) {
                        userName.take(1).uppercase()
                    } else {
                        userEmail.take(1).uppercase()
                    },
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (userName.isNotBlank()) userName else "Пользователь",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = userEmail,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                if (userPhone.isNotBlank()) {
                    Text(
                        text = userPhone,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun MainProfileScreen(
    userName: String,
    userEmail: String,
    userPhone: String,
    addresses: List<Address>,
    onNavigateToOrders: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAddresses: () -> Unit,
    onNavigateToHelp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ProfileHeader(
            userName = userName,
            userEmail = userEmail,
            userPhone = userPhone
        )

        Spacer(modifier = Modifier.height(8.dp))

        ProfileMenuItem(
            icon = Icons.Outlined.ShoppingBag,
            title = "Мои заказы",
            subtitle = "История и статус заказов",
            onClick = onNavigateToOrders
        )

        ProfileMenuItem(
            icon = Icons.Outlined.FavoriteBorder,
            title = "Избранное",
            subtitle = "Сохраненные рестораны и блюда",
            onClick = { /* Навигация в избранное */ }
        )

        ProfileMenuItem(
            icon = Icons.Outlined.RateReview,
            title = "Мои отзывы",
            subtitle = "Ваши отзывы о ресторанах",
            onClick = { /* Навигация к отзывам */ }
        )

        ProfileMenuItem(
            icon = Icons.Outlined.LocationOn,
            title = "Адреса доставки",
            subtitle = when {
                addresses.isEmpty() -> "Добавьте адрес"
                addresses.size == 1 -> "1 адрес"
                addresses.size in 2..4 -> "${addresses.size} адреса"
                else -> "${addresses.size} адресов"
            },
            onClick = onNavigateToAddresses
        )

        ProfileMenuItem(
            icon = Icons.Outlined.Payment,
            title = "Способы оплаты",
            subtitle = "Карты, наличные",
            onClick = { /* Навигация к способам оплаты */ }
        )

        ProfileMenuItem(
            icon = Icons.Outlined.Help,
            title = "Помощь",
            subtitle = "Часто задаваемые вопросы и поддержка",
            onClick = onNavigateToHelp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "FastBite v1.0.0",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    badge: String? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent,
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (badge != null) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = badge,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    userName: String,
    userPhone: String,
    userEmail: String,
    notificationsEnabled: Boolean,
    emailEnabled: Boolean,
    language: String,
    theme: String,
    onNotificationsChange: (Boolean) -> Unit,
    onEmailChange: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    var showLogoutConfirm by remember { mutableStateOf(false) }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = {
                Text(
                    "Выход из аккаунта",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Вы уверены, что хотите выйти?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirm = false
                        onLogout()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Выйти")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Отмена")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SettingsItem(
            icon = Icons.Outlined.Person,
            title = "Редактировать профиль",
            subtitle = if (userName.isNotBlank() || userPhone.isNotBlank())
                "$userName • $userPhone"
            else
                "Заполните информацию",
            onClick = onEditProfile
        )

        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        SettingsSwitchItem(
            icon = Icons.Outlined.Notifications,
            title = "Уведомления",
            subtitle = "Получать уведомления о заказах",
            checked = notificationsEnabled,
            onCheckedChange = onNotificationsChange
        )

        SettingsSwitchItem(
            icon = Icons.Outlined.Email,
            title = "Email-рассылка",
            subtitle = "Получать новости и акции",
            checked = emailEnabled,
            onCheckedChange = onEmailChange
        )

        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        SettingsSelectItem(
            icon = Icons.Outlined.Language,
            title = "Язык",
            value = language,
            onClick = { /* Показать выбор языка */ }
        )

        SettingsSelectItem(
            icon = Icons.Outlined.DarkMode,
            title = "Тема",
            value = theme,
            onClick = { /* Показать выбор темы */ }
        )

        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        SettingsItem(
            icon = Icons.Outlined.Logout,
            title = "Выйти из аккаунта",
            subtitle = "Завершить сеанс",
            onClick = { showLogoutConfirm = true },
            isDestructive = true
        )
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val contentColor = if (isDestructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isDestructive) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(24.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = contentColor
                    )
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = if (isDestructive) {
                            contentColor.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (isDestructive) {
                    contentColor.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun SettingsSelectItem(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EditProfileScreen(
    userName: String,
    userPhone: String,
    userEmail: String,
    onSave: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(userName) }
    var phone by remember { mutableStateOf(userPhone) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Имя") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Телефон") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            OutlinedTextField(
                value = userEmail,
                onValueChange = {},
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                enabled = false,
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }

        Button(
            onClick = { onSave(name, phone) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Сохранить")
        }
    }
}

@Composable
fun AddressesScreen(
    addresses: List<Address>,
    onAddAddress: () -> Unit,
    onEditAddress: (Address) -> Unit,
    onDeleteAddress: (Address) -> Unit,
    onSetDefaultAddress: (Address) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Сохраненные адреса",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (addresses.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        "У вас пока нет сохраненных адресов",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        "Добавьте адрес для быстрого оформления заказа",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(addresses) { address ->
                    AddressCard(
                        address = address,
                        onEdit = { onEditAddress(address) },
                        onDelete = { onDeleteAddress(address) },
                        onSetDefault = { onSetDefaultAddress(address) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAddAddress,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Добавить новый адрес")
        }
    }
}

@Composable
fun AddressCard(
    address: Address,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )

                    Text(
                        text = address.address,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = "Редактировать",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Удалить",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            val details = mutableListOf<String>()
            if (address.apartment.isNotBlank()) details.add("кв. ${address.apartment}")
            if (address.entrance.isNotBlank()) details.add("под. ${address.entrance}")
            if (address.floor.isNotBlank()) details.add("эт. ${address.floor}")
            if (address.intercom.isNotBlank()) details.add("домофон ${address.intercom}")

            if (details.isNotEmpty()) {
                Text(
                    text = details.joinToString(" • "),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 36.dp, top = 4.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 36.dp, top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (address.isDefault) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            "Основной адрес",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                } else {
                    TextButton(
                        onClick = onSetDefault,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            "Сделать основным",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditAddressScreen(
    address: Address?,
    onSave: (Address) -> Unit,
    onBack: () -> Unit
) {
    var addressText by remember { mutableStateOf(address?.address ?: "") }
    var apartment by remember { mutableStateOf(address?.apartment ?: "") }
    var entrance by remember { mutableStateOf(address?.entrance ?: "") }
    var floor by remember { mutableStateOf(address?.floor ?: "") }
    var intercom by remember { mutableStateOf(address?.intercom ?: "") }
    var isDefault by remember { mutableStateOf(address?.isDefault ?: false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (address == null) "Новый адрес" else "Редактировать адрес",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = addressText,
                onValueChange = { addressText = it },
                label = { Text("Адрес *") },
                placeholder = { Text("Город, улица, дом") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                isError = addressText.isBlank(),
                leadingIcon = {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            if (addressText.isBlank()) {
                Text(
                    text = "Адрес обязателен для заполнения",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            OutlinedTextField(
                value = apartment,
                onValueChange = { apartment = it },
                label = { Text("Квартира/офис") },
                placeholder = { Text("Необязательно") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            OutlinedTextField(
                value = entrance,
                onValueChange = { entrance = it },
                label = { Text("Подъезд") },
                placeholder = { Text("Необязательно") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            OutlinedTextField(
                value = floor,
                onValueChange = { floor = it },
                label = { Text("Этаж") },
                placeholder = { Text("Необязательно") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            OutlinedTextField(
                value = intercom,
                onValueChange = { intercom = it },
                label = { Text("Домофон") },
                placeholder = { Text("Необязательно") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Сделать основным адресом",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = isDefault,
                    onCheckedChange = { isDefault = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Отмена")
            }

            Button(
                onClick = {
                    val newAddress = Address(
                        id = address?.id ?: System.currentTimeMillis().toString(),
                        address = addressText,
                        apartment = apartment,
                        entrance = entrance,
                        floor = floor,
                        intercom = intercom,
                        isDefault = isDefault
                    )
                    onSave(newAddress)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                enabled = addressText.isNotBlank()
            ) {
                Text(if (address == null) "Сохранить" else "Обновить")
            }
        }
    }
}

// ЭКРАН: Помощь и поддержка
@Composable
fun HelpScreen(
    onNavigateToFAQ: () -> Unit,
    onNavigateToContactSupport: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Заголовок
        Text(
            text = "Чем мы можем помочь?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Выберите интересующий вас раздел",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Карточка с часто задаваемыми вопросами
        HelpCard(
            icon = Icons.Outlined.QuestionAnswer,
            title = "Часто задаваемые вопросы",
            description = "Ответы на популярные вопросы о заказах, доставке и оплате",
            color = MaterialTheme.colorScheme.primary,
            onClick = onNavigateToFAQ
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Карточка с контактами поддержки
        HelpCard(
            icon = Icons.Outlined.SupportAgent,
            title = "Связаться с поддержкой",
            description = "Напишите нам, и мы поможем решить вашу проблему",
            color = MaterialTheme.colorScheme.tertiary,
            onClick = onNavigateToContactSupport
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Карточка с правилами и условиями
        HelpCard(
            icon = Icons.Outlined.Description,
            title = "Правила и условия",
            description = "Условия использования сервиса и доставки",
            color = MaterialTheme.colorScheme.secondary,
            onClick = onNavigateToTerms
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Карточка с информацией о приложении
        HelpCard(
            icon = Icons.Outlined.Info,
            title = "О приложении",
            description = "Версия 1.0.0 • Политика конфиденциальности",
            color = MaterialTheme.colorScheme.outline,
            onClick = onNavigateToAbout
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Контактная информация
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Контактная информация",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                ContactInfoRow(
                    icon = Icons.Outlined.Email,
                    text = "support@fastbite.com"
                )

                ContactInfoRow(
                    icon = Icons.Outlined.Phone,
                    text = "+7 (999) 123-45-67"
                )

                ContactInfoRow(
                    icon = Icons.Outlined.Schedule,
                    text = "Ежедневно с 10:00 до 22:00"
                )
            }
        }
    }
}

// ЭКРАН: Часто задаваемые вопросы
@Composable
fun FAQScreen(
    onBack: () -> Unit
) {
    // Список вопросов и ответов
    val faqItems = listOf(
        FAQItem(
            question = "Как сделать заказ?",
            answer = "Выберите ресторан, добавьте блюда в корзину, укажите адрес доставки и выберите способ оплаты. После подтверждения заказа вы получите уведомление."
        ),
        FAQItem(
            question = "Сколько времени занимает доставка?",
            answer = "Среднее время доставки составляет 30-60 минут в зависимости от загруженности ресторана и расстояния. Вы можете отслеживать статус заказа в реальном времени."
        ),
        FAQItem(
            question = "Как оплатить заказ?",
            answer = "Вы можете оплатить заказ наличными курьеру, банковской картой при получении или онлайн на сайте. Также доступна оплата через Apple Pay и Google Pay."
        ),
        FAQItem(
            question = "Можно ли изменить или отменить заказ?",
            answer = "Вы можете изменить или отменить заказ до того, как ресторан начал его готовить. Для этого перейдите в раздел 'Мои заказы' и выберите нужный заказ."
        ),
        FAQItem(
            question = "Что делать, если заказ не привезли вовремя?",
            answer = "Если заказ задерживается, вы можете связаться с поддержкой через чат в приложении или по телефону. Мы обязательно решим эту проблему."
        ),
        FAQItem(
            question = "Как оставить отзыв о ресторане?",
            answer = "После получения заказа вы можете оценить ресторан и оставить отзыв в разделе 'Мои заказы'. Ваше мнение помогает нам становиться лучше!"
        ),
        FAQItem(
            question = "Безопасно ли платить онлайн?",
            answer = "Да, все платежи защищены современными протоколами шифрования. Мы не храним данные ваших карт и соблюдаем стандарты безопасности PCI DSS."
        ),
        FAQItem(
            question = "Как изменить личные данные?",
            answer = "Перейдите в раздел 'Профиль' → 'Настройки' → 'Редактировать профиль'. Там вы можете изменить имя, телефон и другие данные."
        )
    )

    var expandedItemIndex by remember { mutableStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(faqItems.size) { index ->
                FAQItemCard(
                    item = faqItems[index],
                    isExpanded = expandedItemIndex == index,
                    onExpandChange = {
                        expandedItemIndex = if (expandedItemIndex == index) -1 else index
                    }
                )
            }
        }
    }
}

// ЭКРАН: Связаться с поддержкой
@Composable
fun ContactSupportScreen(
    userEmail: String,
    userName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var message by remember { mutableStateOf("") }
    var selectedTopic by remember { mutableStateOf(SupportTopic.ORDER) }
    var showTopicDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Напишите нам",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Опишите вашу проблему, и мы ответим в ближайшее время",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Информация о пользователе
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (userName.isNotBlank()) userName else userEmail,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Выбор темы
        Text(
            text = "Тема обращения",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Кастомный выпадающий список
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTopicDialog = true },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedTopic.title,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Выбрать тему",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Диалог выбора темы
        if (showTopicDialog) {
            AlertDialog(
                onDismissRequest = { showTopicDialog = false },
                title = {
                    Text(
                        "Выберите тему",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        SupportTopic.values().forEach { topic ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedTopic = topic
                                        showTopicDialog = false
                                    },
                                color = if (selectedTopic == topic)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    Color.Transparent
                            ) {
                                Text(
                                    text = topic.title,
                                    modifier = Modifier.padding(16.dp),
                                    color = if (selectedTopic == topic)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showTopicDialog = false }) {
                        Text("Отмена")
                    }
                },
                shape = RoundedCornerShape(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Поле для сообщения
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Сообщение") },
            placeholder = { Text("Опишите вашу проблему подробнее...") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            minLines = 8,
            maxLines = 12,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка отправки
        Button(
            onClick = {
                if (message.isNotBlank()) {
                    Toast.makeText(
                        context,
                        "Сообщение отправлено! Мы ответим вам на $userEmail",
                        Toast.LENGTH_LONG
                    ).show()
                    message = ""
                } else {
                    Toast.makeText(
                        context,
                        "Пожалуйста, напишите сообщение",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = message.isNotBlank()
        ) {
            Icon(Icons.Outlined.Send, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Отправить")
        }
    }
}

// ЭКРАН: Правила и условия
@Composable
fun TermsAndConditionsScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Контент с прокруткой
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Общие положения
            item {
                TermsSection(
                    title = "1. Общие положения",
                    icon = Icons.Outlined.Info,
                    content = listOf(
                        "1.1. Используя приложение FastBite, вы соглашаетесь с настоящими Правилами и условиями.",
                        "1.2. FastBite предоставляет платформу для заказа еды из ресторанов-партнеров.",
                        "1.3. Мы оставляем за собой право изменять правила в любое время с уведомлением пользователей.",
                        "1.4. Если вы не согласны с правилами, пожалуйста, прекратите использование приложения."
                    )
                )
            }

            // 2. Регистрация и аккаунт
            item {
                TermsSection(
                    title = "2. Регистрация и аккаунт",
                    icon = Icons.Outlined.Person,
                    content = listOf(
                        "2.1. Для оформления заказа необходима регистрация с указанием действительного email и номера телефона.",
                        "2.2. Вы несете ответственность за сохранность своих учетных данных.",
                        "2.3. Запрещено передавать доступ к аккаунту третьим лицам.",
                        "2.4. Мы имеем право заблокировать аккаунт при подозрении на мошенничество."
                    )
                )
            }

            // 3. Оформление заказа
            item {
                TermsSection(
                    title = "3. Оформление заказа",
                    icon = Icons.Outlined.ShoppingCart,
                    content = listOf(
                        "3.1. Оформляя заказ, вы подтверждаете правильность указанной информации.",
                        "3.2. Цены в приложении могут отличаться от цен в ресторане.",
                        "3.3. Ресторан может отказать в выполнении заказа при отсутствии необходимых продуктов.",
                        "3.4. Время доставки является приблизительным и может варьироваться."
                    )
                )
            }

            // 4. Оплата
            item {
                TermsSection(
                    title = "4. Оплата",
                    icon = Icons.Outlined.Payment,
                    content = listOf(
                        "4.1. Доступны следующие способы оплаты: наличные, банковские карты, Apple Pay, Google Pay.",
                        "4.2. При онлайн-оплате средства списываются после подтверждения заказа.",
                        "4.3. Возврат средств осуществляется на ту же карту в течение 3-10 рабочих дней.",
                        "4.4. Все платежи защищены современными протоколами шифрования."
                    )
                )
            }

            // 5. Доставка
            item {
                TermsSection(
                    title = "5. Доставка",
                    icon = Icons.Outlined.DeliveryDining,
                    content = listOf(
                        "5.1. Доставка осуществляется по указанному вами адресу.",
                        "5.2. Минимальная сумма заказа зависит от ресторана.",
                        "5.3. Стоимость доставки рассчитывается автоматически и зависит от расстояния.",
                        "5.4. Курьер вправе ожидать получателя не более 10 минут."
                    )
                )
            }

            // 6. Отмена и возврат
            item {
                TermsSection(
                    title = "6. Отмена и возврат",
                    icon = Icons.Outlined.Cancel,
                    content = listOf(
                        "6.1. Отменить заказ можно до начала его приготовления.",
                        "6.2. При отмене после приготовления взимается компенсация ресторану.",
                        "6.3. Возврат осуществляется при несоответствии заказа или его ненадлежащем качестве.",
                        "6.4. Для возврата свяжитесь с поддержкой в течение 24 часов."
                    )
                )
            }

            // 7. Ответственность
            item {
                TermsSection(
                    title = "7. Ответственность",
                    icon = Icons.Outlined.Gavel,
                    content = listOf(
                        "7.1. FastBite не несет ответственности за качество блюд, приготовленных ресторанами.",
                        "7.2. Мы не отвечаем за задержки доставки, вызванные внешними факторами.",
                        "7.3. В случае форс-мажорных обстоятельств обязательства приостанавливаются.",
                        "7.4. Максимальная ответственность ограничена суммой заказа."
                    )
                )
            }

            // 8. Конфиденциальность
            item {
                TermsSection(
                    title = "8. Конфиденциальность",
                    icon = Icons.Outlined.PrivacyTip,
                    content = listOf(
                        "8.1. Мы собираем только необходимую для работы сервиса информацию.",
                        "8.2. Ваши данные не передаются третьим лицам без вашего согласия.",
                        "8.3. Мы используем cookie для улучшения работы приложения.",
                        "8.4. Подробнее в Политике конфиденциальности."
                    )
                )
            }

            // 9. Контактная информация
            item {
                TermsSection(
                    title = "9. Контактная информация",
                    icon = Icons.Outlined.SupportAgent,
                    content = listOf(
                        "9.1. По всем вопросам обращайтесь в службу поддержки:",
                        "   • Email: support@fastbite.com",
                        "   • Телефон: +7 (999) 123-45-67",
                        "   • Часы работы: круглосуточно"
                    )
                )
            }

            // Дата последнего обновления
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "Последнее обновление: 15 марта 2024 г.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ЭКРАН: О приложении
@Composable
fun AboutAppScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Контент с прокруткой
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Логотип приложения
            Card(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "FB",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Название приложения
            Text(
                text = "FastBite",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Версия 1.0.0 (Build 100)",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Описание приложения
            AboutSection(
                title = "О нас",
                icon = Icons.Outlined.Info,
                content = "FastBite - это сервис доставки еды из лучших ресторанов вашего города. Мы объединяем тысячи ресторанов и миллионы пользователей, делая процесс заказа еды быстрым, удобным и приятным."
            )

            AboutSection(
                title = "Наша миссия",
                icon = Icons.Outlined.EmojiObjects,
                content = "Делать вкусную еду доступной каждому в любое время. Мы стремимся создавать лучший опыт заказа еды, объединяя технологии и гастрономию."
            )

            AboutSection(
                title = "Преимущества",
                icon = Icons.Outlined.Star,
                content = "• Более 500 ресторанов-партнеров\n• Быстрая доставка от 30 минут\n• Удобные способы оплаты\n• Программа лояльности и скидки\n• Круглосуточная поддержка"
            )

            // Техническая информация
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Техническая информация",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    InfoRow("Платформа", "Android")
                    InfoRow("Минимальная версия", "Android 6.0 (API 23)")
                    InfoRow("Последнее обновление", "18 марта 2024")
                    InfoRow("Размер", "24 MB")
                }
            }

            // Контакты разработчика
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Разработчик",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    InfoRow("Компания", "FastBite Technologies")
                    InfoRow("Сайт", "www.fastbite.com")
                    InfoRow("Email", "dev@fastbite.com")
                }
            }

            // Лицензии
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Лицензии",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = "© 2024 FastBite. Все права защищены.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Используемые библиотеки:\n• Jetpack Compose\n• Firebase\n• Kotlin Coroutines\n• Material Design 3",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Компонент для отображения секции в правилах
@Composable
fun TermsSection(
    title: String,
    icon: ImageVector,
    content: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            content.forEach { text ->
                Text(
                    text = text,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

// Компонент для отображения секции в "О приложении"
@Composable
fun AboutSection(
    title: String,
    icon: ImageVector,
    content: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = content,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Компонент для отображения строки информации
@Composable
fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Вспомогательные компоненты для экрана помощи
@Composable
fun HelpCard(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ContactInfoRow(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Data class для FAQ
data class FAQItem(
    val question: String,
    val answer: String
)

// Компонент для отображения вопроса и ответа
@Composable
fun FAQItemCard(
    item: FAQItem,
    isExpanded: Boolean,
    onExpandChange: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onExpandChange),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.question,
                    fontSize = 16.sp,
                    fontWeight = if (isExpanded) FontWeight.Bold else FontWeight.Medium,
                    color = if (isExpanded)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = if (isExpanded)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.answer,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

// Enum для тем обращения в поддержку
enum class SupportTopic(val title: String) {
    ORDER("Проблема с заказом"),
    PAYMENT("Оплата"),
    DELIVERY("Доставка"),
    RESTAURANT("Ресторан"),
    APP("Приложение"),
    OTHER("Другое")
}

data class Address(
    val id: String,
    val address: String,
    val apartment: String,
    val entrance: String,
    val floor: String,
    val intercom: String,
    val isDefault: Boolean = false
)