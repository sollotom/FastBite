package com.example.fastbite

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDishDialog(
    dish: Dish,
    onDismiss: () -> Unit,
    onSave: (Dish) -> Unit
) {
    var name by remember { mutableStateOf(dish.name) }
    var price by remember { mutableStateOf(dish.price) }
    var description by remember { mutableStateOf(dish.description) }
    var photoUrl by remember { mutableStateOf(dish.photoUrl) }
    var category by remember { mutableStateOf(dish.category) }
    var weightOrVolume by remember { mutableStateOf(dish.weightOrVolume) }
    var ingredients by remember { mutableStateOf(dish.ingredients) }
    var calories by remember { mutableStateOf(dish.calories) }
    var proteins by remember { mutableStateOf(dish.proteins) }
    var fats by remember { mutableStateOf(dish.fats) }
    var carbs by remember { mutableStateOf(dish.carbs) }
    var cookingTime by remember { mutableStateOf(dish.cookingTime) }
    var spiciness by remember { mutableStateOf(dish.spiciness) }
    var vegetarian by remember { mutableStateOf(dish.vegetarian) }
    var allergens by remember { mutableStateOf(dish.allergens) }
    var addOns by remember { mutableStateOf(dish.addOns) }
    var addOnsPrice by remember { mutableStateOf(dish.addOnsPrice) }
    var availability by remember { mutableStateOf(dish.availability) }
    var rating by remember { mutableStateOf(dish.rating) }
    var portions by remember { mutableStateOf(dish.portions) }
    var costPrice by remember { mutableStateOf(dish.costPrice) }
    var discount by remember { mutableStateOf(dish.discount) }
    var popular by remember { mutableStateOf(dish.popular) }

    var error by remember { mutableStateOf("") }

    val db = Firebase.firestore

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать блюдо") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Название") })
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Цена") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Описание") })
                OutlinedTextField(value = photoUrl, onValueChange = { photoUrl = it }, label = { Text("Ссылка на фото") })
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Категория") })
                OutlinedTextField(value = weightOrVolume, onValueChange = { weightOrVolume = it }, label = { Text("Вес/Объём") })
                OutlinedTextField(value = ingredients, onValueChange = { ingredients = it }, label = { Text("Ингредиенты") })
                OutlinedTextField(value = calories, onValueChange = { calories = it }, label = { Text("Калории") })
                OutlinedTextField(value = proteins, onValueChange = { proteins = it }, label = { Text("Белки") })
                OutlinedTextField(value = fats, onValueChange = { fats = it }, label = { Text("Жиры") })
                OutlinedTextField(value = carbs, onValueChange = { carbs = it }, label = { Text("Углеводы") })
                OutlinedTextField(value = cookingTime, onValueChange = { cookingTime = it }, label = { Text("Время приготовления") })
                OutlinedTextField(value = spiciness, onValueChange = { spiciness = it }, label = { Text("Острота") })

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = vegetarian, onCheckedChange = { vegetarian = it })
                    Text("Вегетарианское")
                }

                OutlinedTextField(value = allergens, onValueChange = { allergens = it }, label = { Text("Аллергены") })
                OutlinedTextField(value = addOns, onValueChange = { addOns = it }, label = { Text("Добавки") })
                OutlinedTextField(value = addOnsPrice, onValueChange = { addOnsPrice = it }, label = { Text("Цена добавок") })

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = availability, onCheckedChange = { availability = it })
                    Text("Доступно")
                }

                OutlinedTextField(value = rating, onValueChange = { rating = it }, label = { Text("Рейтинг") })
                OutlinedTextField(value = portions, onValueChange = { portions = it }, label = { Text("Порции") })
                OutlinedTextField(value = costPrice, onValueChange = { costPrice = it }, label = { Text("Себестоимость") })
                OutlinedTextField(value = discount, onValueChange = { discount = it }, label = { Text("Скидка") })

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = popular, onCheckedChange = { popular = it })
                    Text("Популярное блюдо")
                }

                if (error.isNotEmpty()) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank() || price.isBlank()) {
                    error = "Заполните обязательные поля"
                    return@TextButton
                }
                val updatedDish = dish.copy(
                    name = name,
                    price = price,
                    description = description,
                    photoUrl = photoUrl,
                    category = category,
                    weightOrVolume = weightOrVolume,
                    ingredients = ingredients,
                    calories = calories,
                    proteins = proteins,
                    fats = fats,
                    carbs = carbs,
                    cookingTime = cookingTime,
                    spiciness = spiciness,
                    vegetarian = vegetarian,
                    allergens = allergens,
                    addOns = addOns,
                    addOnsPrice = addOnsPrice,
                    availability = availability,
                    rating = rating,
                    portions = portions,
                    costPrice = costPrice,
                    discount = discount,
                    popular = popular
                )
                onSave(updatedDish)
            }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
