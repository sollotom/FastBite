package com.example.fastbite

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CartScreen(onNavigateToMenu: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Корзина",
            style = MaterialTheme.typography.headlineSmall
        )
        Text("Ваша корзина пуста.")
        Button(
            onClick = { onNavigateToMenu() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Добавить блюда")
        }
    }
}
