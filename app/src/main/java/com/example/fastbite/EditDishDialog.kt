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

// Строки для диалога редактирования блюда
object EditDishStrings {
    var currentLanguage = Strings.currentLanguage

    val editDish: String get() = if (currentLanguage.value == Language.KAZAKH) "Тағамды өңдеу" else "Редактировать блюдо"
    val name: String get() = if (currentLanguage.value == Language.KAZAKH) "Атауы" else "Название"
    val price: String get() = if (currentLanguage.value == Language.KAZAKH) "Бағасы" else "Цена"
    val description: String get() = if (currentLanguage.value == Language.KAZAKH) "Сипаттама" else "Описание"
    val photoUrl: String get() = if (currentLanguage.value == Language.KAZAKH) "Фото сілтемесі" else "Ссылка на фото"
    val category: String get() = if (currentLanguage.value == Language.KAZAKH) "Санат" else "Категория"
    val weightOrVolume: String get() = if (currentLanguage.value == Language.KAZAKH) "Салмағы/Көлемі" else "Вес/Объём"
    val ingredients: String get() = if (currentLanguage.value == Language.KAZAKH) "Құрамы" else "Ингредиенты"
    val calories: String get() = if (currentLanguage.value == Language.KAZAKH) "Калория" else "Калории"
    val proteins: String get() = if (currentLanguage.value == Language.KAZAKH) "Ақуыздар" else "Белки"
    val fats: String get() = if (currentLanguage.value == Language.KAZAKH) "Майлар" else "Жиры"
    val carbs: String get() = if (currentLanguage.value == Language.KAZAKH) "Көмірсулар" else "Углеводы"
    val cookingTime: String get() = if (currentLanguage.value == Language.KAZAKH) "Дайындау уақыты" else "Время приготовления"
    val spiciness: String get() = if (currentLanguage.value == Language.KAZAKH) "Ащылығы" else "Острота"
    val vegetarian: String get() = if (currentLanguage.value == Language.KAZAKH) "Вегетариандық" else "Вегетарианское"
    val allergens: String get() = if (currentLanguage.value == Language.KAZAKH) "Аллергендер" else "Аллергены"
    val addOns: String get() = if (currentLanguage.value == Language.KAZAKH) "Қоспалар" else "Добавки"
    val addOnsPrice: String get() = if (currentLanguage.value == Language.KAZAKH) "Қоспалар бағасы" else "Цена добавок"
    val available: String get() = if (currentLanguage.value == Language.KAZAKH) "Қолжетімді" else "Доступно"
    val rating: String get() = if (currentLanguage.value == Language.KAZAKH) "Рейтинг" else "Рейтинг"
    val portions: String get() = if (currentLanguage.value == Language.KAZAKH) "Порциялар" else "Порции"
    val costPrice: String get() = if (currentLanguage.value == Language.KAZAKH) "Өзіндік құны" else "Себестоимость"
    val discount: String get() = if (currentLanguage.value == Language.KAZAKH) "Жеңілдік" else "Скидка"
    val popularDish: String get() = if (currentLanguage.value == Language.KAZAKH) "Танымал тағам" else "Популярное блюдо"
    val save: String get() = if (currentLanguage.value == Language.KAZAKH) "Сақтау" else "Сохранить"
    val cancel: String get() = if (currentLanguage.value == Language.KAZAKH) "Бас тарту" else "Отмена"
    val fillRequired: String get() = if (currentLanguage.value == Language.KAZAKH) "Міндетті өрістерді толтырыңыз" else "Заполните обязательные поля"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDishDialog(
    dish: Dish,
    onDismiss: () -> Unit,
    onSave: (Dish) -> Unit
) {
    // ====== ПЕРЕМЕННЫЕ СОСТОЯНИЯ ======
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
    var portions by remember { mutableStateOf(dish.portions) }
    var costPrice by remember { mutableStateOf(dish.costPrice) }
    var discount by remember { mutableStateOf(dish.discount) }
    var popular by remember { mutableStateOf(dish.popular) }

    // Рейтинг (редактируем как строку, потом преобразуем в Double)
    var ratingAverage by remember { mutableStateOf(dish.ratingAverage.toString()) }

    var error by remember { mutableStateOf("") }

    val db = Firebase.firestore

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(EditDishStrings.editDish) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(EditDishStrings.name) })
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text(EditDishStrings.price) })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text(EditDishStrings.description) })
                OutlinedTextField(value = photoUrl, onValueChange = { photoUrl = it }, label = { Text(EditDishStrings.photoUrl) })
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text(EditDishStrings.category) })
                OutlinedTextField(value = weightOrVolume, onValueChange = { weightOrVolume = it }, label = { Text(EditDishStrings.weightOrVolume) })
                OutlinedTextField(value = ingredients, onValueChange = { ingredients = it }, label = { Text(EditDishStrings.ingredients) })
                OutlinedTextField(value = calories, onValueChange = { calories = it }, label = { Text(EditDishStrings.calories) })
                OutlinedTextField(value = proteins, onValueChange = { proteins = it }, label = { Text(EditDishStrings.proteins) })
                OutlinedTextField(value = fats, onValueChange = { fats = it }, label = { Text(EditDishStrings.fats) })
                OutlinedTextField(value = carbs, onValueChange = { carbs = it }, label = { Text(EditDishStrings.carbs) })
                OutlinedTextField(value = cookingTime, onValueChange = { cookingTime = it }, label = { Text(EditDishStrings.cookingTime) })
                OutlinedTextField(value = spiciness, onValueChange = { spiciness = it }, label = { Text(EditDishStrings.spiciness) })

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = vegetarian, onCheckedChange = { vegetarian = it })
                    Text(EditDishStrings.vegetarian)
                }

                OutlinedTextField(value = allergens, onValueChange = { allergens = it }, label = { Text(EditDishStrings.allergens) })
                OutlinedTextField(value = addOns, onValueChange = { addOns = it }, label = { Text(EditDishStrings.addOns) })
                OutlinedTextField(value = addOnsPrice, onValueChange = { addOnsPrice = it }, label = { Text(EditDishStrings.addOnsPrice) })

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = availability, onCheckedChange = { availability = it })
                    Text(EditDishStrings.available)
                }

                OutlinedTextField(value = ratingAverage, onValueChange = { ratingAverage = it }, label = { Text(EditDishStrings.rating) })
                OutlinedTextField(value = portions, onValueChange = { portions = it }, label = { Text(EditDishStrings.portions) })
                OutlinedTextField(value = costPrice, onValueChange = { costPrice = it }, label = { Text(EditDishStrings.costPrice) })
                OutlinedTextField(value = discount, onValueChange = { discount = it }, label = { Text(EditDishStrings.discount) })

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = popular, onCheckedChange = { popular = it })
                    Text(EditDishStrings.popularDish)
                }

                if (error.isNotEmpty()) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank() || price.isBlank()) {
                    error = EditDishStrings.fillRequired
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
                    ratingAverage = ratingAverage.toDoubleOrNull() ?: 0.0,
                    ratingCount = dish.ratingCount,
                    portions = portions,
                    costPrice = costPrice,
                    discount = discount,
                    popular = popular
                )

                onSave(updatedDish)
            }) {
                Text(EditDishStrings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(EditDishStrings.cancel)
            }
        }
    )
}