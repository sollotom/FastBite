package com.example.fastbite

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SellerMenuScreen(
    currentUserEmail: String,
    onAddDishClick: () -> Unit
) {
    val db = Firebase.firestore

    var dishes by remember { mutableStateOf(listOf<Dish>()) }
    var dishToEdit by remember { mutableStateOf<Dish?>(null) }
    var dishToDelete by remember { mutableStateOf<Dish?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Загрузка блюд
    LaunchedEffect(currentUserEmail) {
        db.collection("dishes")
            .whereEqualTo("owner", currentUserEmail)
            .get()
            .addOnSuccessListener { result ->
                dishes = result.map {
                    Dish(
                        id = it.id,
                        name = it.getString("name") ?: "",
                        price = it.getString("price") ?: "",
                        description = it.getString("description") ?: "",
                        photoUrl = it.getString("photoUrl") ?: "",
                        category = it.getString("category") ?: "",
                        weightOrVolume = it.getString("weightOrVolume") ?: "",
                        ingredients = it.getString("ingredients") ?: "",
                        calories = it.getString("calories") ?: "",
                        proteins = it.getString("proteins") ?: "",
                        fats = it.getString("fats") ?: "",
                        carbs = it.getString("carbs") ?: "",
                        cookingTime = it.getString("cookingTime") ?: "",
                        spiciness = it.getString("spiciness") ?: "",
                        vegetarian = it.getBoolean("vegetarian") ?: false,
                        allergens = it.getString("allergens") ?: "",
                        addOns = it.getString("addOns") ?: "",
                        addOnsPrice = it.getString("addOnsPrice") ?: "",
                        availability = it.getBoolean("availability") ?: true,
                        ratingAverage = it.getDouble("ratingAverage") ?: 0.0,
                        ratingCount = it.getLong("ratingCount") ?: 0L,
                        portions = it.getString("portions") ?: "",
                        costPrice = it.getString("costPrice") ?: "",
                        discount = it.getString("discount") ?: "",
                        popular = it.getBoolean("popular") ?: false,
                        dateAdded = it.getString("dateAdded") ?: "",
                        owner = currentUserEmail
                    )
                }
            }
    }

    val filteredDishes = dishes.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Text("Ваше меню", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))

        // Поле поиска
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Поиск блюда") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Кнопка добавления блюда
        Button(
            onClick = onAddDishClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Добавить блюдо")
        }

        Spacer(Modifier.height(12.dp))

        // Сетка блюд 2 колонки
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredDishes) { dish ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(8.dp)) {
                        AsyncImage(
                            model = dish.photoUrl,
                            contentDescription = dish.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(dish.name, style = MaterialTheme.typography.titleMedium)
                        Text("${dish.price} тг", color = MaterialTheme.colorScheme.primary)

                        Spacer(Modifier.height(4.dp))

                        // ⭐ рейтинг
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val full = dish.ratingAverage.toInt()
                            val half = (dish.ratingAverage % 1) >= 0.5

                            repeat(full) { Icon(Icons.Filled.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp)) }
                            if (half) Icon(Icons.Filled.StarHalf, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                            repeat(5 - full - if (half) 1 else 0) { Icon(Icons.Outlined.Star, null, tint = Color.Gray, modifier = Modifier.size(14.dp)) }

                            Text(" ${"%.1f".format(dish.ratingAverage)}", fontSize = MaterialTheme.typography.bodySmall.fontSize)
                        }

                        Spacer(Modifier.height(4.dp))

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = { dishToEdit = dish }) { Icon(Icons.Default.Edit, null, modifier = Modifier.size(20.dp)) }
                            IconButton(onClick = { dishToDelete = dish }) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) }
                        }
                    }
                }
            }
        }
    }

    // ✏️ РЕДАКТИРОВАНИЕ БЛЮДА
    dishToEdit?.let { dish ->
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

        AlertDialog(
            onDismissRequest = { dishToEdit = null },
            title = { Text("Редактировать блюдо") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 600.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(name, { name = it }, label = { Text("Название") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(price, { price = it }, label = { Text("Цена") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(description, { description = it }, label = { Text("Описание") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(photoUrl, { photoUrl = it }, label = { Text("Фото URL") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(category, { category = it }, label = { Text("Категория") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(weightOrVolume, { weightOrVolume = it }, label = { Text("Вес / объём") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(ingredients, { ingredients = it }, label = { Text("Ингредиенты") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(calories, { calories = it }, label = { Text("Калории") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(proteins, { proteins = it }, label = { Text("Белки") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(fats, { fats = it }, label = { Text("Жиры") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(carbs, { carbs = it }, label = { Text("Углеводы") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(cookingTime, { cookingTime = it }, label = { Text("Время приготовления") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(spiciness, { spiciness = it }, label = { Text("Острота") }, modifier = Modifier.fillMaxWidth())

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(vegetarian, { vegetarian = it })
                        Text("Вегетарианское")
                    }

                    OutlinedTextField(allergens, { allergens = it }, label = { Text("Аллергены") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(addOns, { addOns = it }, label = { Text("Добавки") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(addOnsPrice, { addOnsPrice = it }, label = { Text("Цена добавок") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(availability, { availability = it })
                        Text("Доступно")
                    }

                    OutlinedTextField(portions, { portions = it }, label = { Text("Порции") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(costPrice, { costPrice = it }, label = { Text("Себестоимость") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(discount, { discount = it }, label = { Text("Скидка") }, modifier = Modifier.fillMaxWidth())

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(popular, { popular = it })
                        Text("Популярное блюдо")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val data = mapOf(
                        "name" to name,
                        "price" to price,
                        "description" to description,
                        "photoUrl" to photoUrl,
                        "category" to category,
                        "weightOrVolume" to weightOrVolume,
                        "ingredients" to ingredients,
                        "calories" to calories,
                        "proteins" to proteins,
                        "fats" to fats,
                        "carbs" to carbs,
                        "cookingTime" to cookingTime,
                        "spiciness" to spiciness,
                        "vegetarian" to vegetarian,
                        "allergens" to allergens,
                        "addOns" to addOns,
                        "addOnsPrice" to addOnsPrice,
                        "availability" to availability,
                        "portions" to portions,
                        "costPrice" to costPrice,
                        "discount" to discount,
                        "popular" to popular
                    )

                    db.collection("dishes").document(dish.id).update(data)
                    dishes = dishes.map {
                        if (it.id == dish.id)
                            it.copy(
                                name=name, price=price, description=description, photoUrl=photoUrl,
                                category=category, weightOrVolume=weightOrVolume, ingredients=ingredients,
                                calories=calories, proteins=proteins, fats=fats, carbs=carbs,
                                cookingTime=cookingTime, spiciness=spiciness, vegetarian=vegetarian,
                                allergens=allergens, addOns=addOns, addOnsPrice=addOnsPrice,
                                availability=availability, portions=portions, costPrice=costPrice,
                                discount=discount, popular=popular
                            )
                        else it
                    }

                    dishToEdit = null
                }) { Text("Сохранить") }
            },
            dismissButton = {
                TextButton(onClick = { dishToEdit = null }) { Text("Отмена") }
            }
        )
    }

    // ❌ УДАЛЕНИЕ БЛЮДА
    dishToDelete?.let { dish ->
        AlertDialog(
            onDismissRequest = { dishToDelete = null },
            title = { Text("Удалить блюдо?") },
            text = { Text("Удалить ${dish.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    db.collection("dishes").document(dish.id).delete()
                    dishes = dishes.filter { it.id != dish.id }
                    dishToDelete = null
                }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { dishToDelete = null }) { Text("Отмена") }
            }
        )
    }
}
