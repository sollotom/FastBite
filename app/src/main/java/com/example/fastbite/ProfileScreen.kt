package com.example.fastbite

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userEmail: String,
    onLogout: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    FirebaseAuth.getInstance()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    var userName by remember { mutableStateOf("") }
    var userPhone by remember { mutableStateOf("") }
    var userAddress by remember { mutableStateOf("") }
    var userApartment by remember { mutableStateOf("") }
    var userEntrance by remember { mutableStateOf("") }
    var userFloor by remember { mutableStateOf("") }
    var userIntercom by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userEmail) {
        if (userEmail.isBlank()) return@LaunchedEffect

        try {
            val userDoc = db.collection("users").document(userEmail).get().await()
            if (userDoc.exists()) {
                userName = userDoc.getString("name") ?: ""
                userPhone = userDoc.getString("phone") ?: ""
            }

            val addressQuery = db.collection("users")
                .document(userEmail)
                .collection("addresses")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get().await()

            if (!addressQuery.isEmpty) {
                val a = addressQuery.documents.first()
                userAddress = a.getString("address") ?: ""
                userApartment = a.getString("apartment") ?: ""
                userEntrance = a.getString("entrance") ?: ""
                userFloor = a.getString("floor") ?: ""
                userIntercom = a.getString("intercom") ?: ""
            }
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            Card(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Личная информация", fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, null)
                        }
                    }

                    Divider()

                    ProfileRow("Имя", userName)
                    ProfileRow("Email", userEmail)
                    ProfileRow("Телефон", userPhone)

                    Text("Адрес доставки", fontSize = 14.sp, color = Color.Gray)
                    if (userAddress.isNotBlank()) {
                        Text(userAddress)
                        Text(
                            listOfNotNull(
                                userApartment.takeIf { it.isNotBlank() }?.let { "Кв. $it" },
                                userEntrance.takeIf { it.isNotBlank() }?.let { "Подъезд $it" },
                                userFloor.takeIf { it.isNotBlank() }?.let { "Этаж $it" },
                                userIntercom.takeIf { it.isNotBlank() }?.let { "Домофон $it" }
                            ).joinToString(", "),
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    } else {
                        Text("Не указан", color = Color.Gray)
                    }
                }
            }

            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Icon(Icons.Default.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Выйти")
            }
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            userName, userPhone, userAddress,
            userApartment, userEntrance, userFloor, userIntercom,
            userEmail,
            onDismiss = { showEditDialog = false }
        ) { n, p, a, ap, e, f, i ->
            userName = n
            userPhone = p
            userAddress = a
            userApartment = ap
            userEntrance = e
            userFloor = f
            userIntercom = i

            saveProfileToFirestore(
                userEmail, n, p, a, ap, e, f, i, db
            )
            showEditDialog = false
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Выход") },
            text = { Text("Выйти из аккаунта?") },
            confirmButton = {
                TextButton(onClick = onLogout) { Text("Выйти") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun ProfileRow(title: String, value: String) {
    Column {
        Text(title, fontSize = 13.sp, color = Color.Gray)
        Text(if (value.isNotBlank()) value else "Не указано")
    }
}

@Composable
fun EditProfileDialog(
    userName: String,
    userPhone: String,
    userAddress: String,
    userApartment: String,
    userEntrance: String,
    userFloor: String,
    userIntercom: String,
    userEmail: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(userName) }
    var phone by remember { mutableStateOf(userPhone) }
    var address by remember { mutableStateOf(userAddress) }
    var apartment by remember { mutableStateOf(userApartment) }
    var entrance by remember { mutableStateOf(userEntrance) }
    var floor by remember { mutableStateOf(userFloor) }
    var intercom by remember { mutableStateOf(userIntercom) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать профиль") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Имя") })
                OutlinedTextField(phone, { phone = it }, label = { Text("Телефон") })
                OutlinedTextField(address, { address = it }, label = { Text("Адрес") })
                OutlinedTextField(apartment, { apartment = it }, label = { Text("Квартира") })
                OutlinedTextField(entrance, { entrance = it }, label = { Text("Подъезд") })
                OutlinedTextField(floor, { floor = it }, label = { Text("Этаж") })
                OutlinedTextField(intercom, { intercom = it }, label = { Text("Домофон") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(name, phone, address, apartment, entrance, floor, intercom)
            }) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

private fun saveProfileToFirestore(
    email: String,
    name: String,
    phone: String,
    address: String,
    apartment: String,
    entrance: String,
    floor: String,
    intercom: String,
    db: FirebaseFirestore
) {
    val userData = hashMapOf(
        "email" to email,
        "name" to name,
        "phone" to phone,
        "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
    )

    db.collection("users").document(email).set(userData)

    if (address.isNotBlank()) {
        db.collection("users")
            .document(email)
            .collection("addresses")
            .add(
                mapOf(
                    "address" to address,
                    "apartment" to apartment,
                    "entrance" to entrance,
                    "floor" to floor,
                    "intercom" to intercom,
                    "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
            )
    }
}
