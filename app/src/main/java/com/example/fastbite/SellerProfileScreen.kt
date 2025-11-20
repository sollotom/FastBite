package com.example.fastbite
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Экран профиля пользователя (продавца)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerProfileScreen(
    restaurantName: String,
    email: String
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Профиль ресторана") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Добро пожаловать, $restaurantName!",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Email: $email",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                // Здесь можно добавить кнопки или информацию, связанную с управлением рестораном
                Button(
                    onClick = { /* Навигация к управлению меню, заказами и т.д. */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Управлять рестораном")
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { /* Выход из аккаунта */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Выйти")
                }
            }
        }
    }
}
