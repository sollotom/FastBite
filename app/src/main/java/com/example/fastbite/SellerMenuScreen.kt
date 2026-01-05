    package com.example.fastbite

    import androidx.compose.foundation.ExperimentalFoundationApi
    import androidx.compose.material.icons.filled.ArrowBack
    import androidx.compose.ui.text.style.TextOverflow
    import androidx.compose.foundation.shape.CircleShape
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.foundation.layout.*
    import androidx.compose.ui.text.TextStyle
    import androidx.compose.ui.text.style.TextDecoration
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.lazy.grid.GridCells
    import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
    import androidx.compose.foundation.lazy.grid.items
    import androidx.compose.foundation.rememberScrollState
    import androidx.compose.foundation.verticalScroll
    import androidx.compose.foundation.text.KeyboardOptions
    import androidx.compose.foundation.background
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.text.font.FontWeight
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
    import androidx.compose.ui.unit.sp
    import coil.compose.AsyncImage
    import com.google.firebase.firestore.ktx.firestore
    import com.google.firebase.ktx.Firebase
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    fun SellerMenuScreen(
        currentUserEmail: String,
        onAddDishClick: () -> Unit,
        onBackClick: () -> Unit // кнопка назад
    ) {
        val db = Firebase.firestore

        var dishes by remember { mutableStateOf(listOf<Dish>()) }
        var dishToEdit by remember { mutableStateOf<Dish?>(null) }
        var dishToDelete by remember { mutableStateOf<Dish?>(null) }
        var searchQuery by remember { mutableStateOf("") }
        var selectedDish by remember { mutableStateOf<Dish?>(null) }
        var showAddDishScreen by remember { mutableStateOf(false) }


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

        val filteredDishes = dishes.filter { it.name.contains(searchQuery, ignoreCase = true) }

        Box(Modifier.fillMaxSize()) {

            // Основное меню
            Column(Modifier.fillMaxSize().padding(12.dp)) {

                // Кнопка назад сверху
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(Modifier.width(12.dp))
                    Text("Ваше меню", style = MaterialTheme.typography.headlineMedium)
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Поиск блюда") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { showAddDishScreen = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Добавить блюдо") }

                Spacer(Modifier.height(12.dp))

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
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDish = dish }
                        ) {
                            Column(Modifier.padding(8.dp)) {

                                AsyncImage(
                                    model = dish.photoUrl,
                                    contentDescription = dish.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                )
                                Text(dish.name, fontWeight = FontWeight.Bold)

// 🔥 Блок цены со скидкой
                                val discountPercentage = dish.discount?.toDoubleOrNull() ?: 0.0
                                val originalPrice = dish.price.toDoubleOrNull() ?: 0.0
                                val discountedPrice = if (discountPercentage > 0) originalPrice * (1 - discountPercentage / 100) else originalPrice

                                if (discountPercentage > 0) {
                                    Column {
                                        Text(
                                            "${"%.0f".format(discountedPrice)} тг",
                                            color = Color.Red,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "${"%.0f".format(originalPrice)} тг",
                                            color = Color.Gray,
                                            fontSize = 12.sp,
                                            style = TextStyle(textDecoration = TextDecoration.LineThrough)
                                        )
                                    }
                                } else {
                                    Text("${"%.0f".format(originalPrice)} тг")
                                }


                                // Рейтинг
                                // Рейтинг блюда
                                val avgRating = dish.ratingAverage ?: 0.0
                                val totalRatings = dish.ratingCount ?: 0L

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val fullStars = avgRating.toInt()
                                    val hasHalfStar = (avgRating % 1) >= 0.5

                                    repeat(fullStars) {
                                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                                    }
                                    if (hasHalfStar) {
                                        Icon(Icons.Filled.StarHalf, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                                    }
                                    repeat(5 - fullStars - if (hasHalfStar) 1 else 0) {
                                        Icon(Icons.Outlined.Star, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                    }

                                    Spacer(Modifier.width(4.dp))
                                    Text("%.1f (%d)".format(avgRating, totalRatings), fontSize = 12.sp)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    IconButton(onClick = { dishToEdit = dish }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                                    }
                                    IconButton(onClick = { dishToDelete = dish }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Удалить")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Детали блюда
            // Детали блюда
            selectedDish?.let { dish ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { selectedDish = null }
                )

                Column(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Шапка с кнопкой назад и названием
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Кнопка назад
                        IconButton(
                            onClick = { selectedDish = null },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(50))
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Назад",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        // Название блюда
                        Text(
                            dish.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    // Фото блюда (увеличенное)
                    AsyncImage(
                        model = dish.photoUrl,
                        contentDescription = dish.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp) // Увеличенная высота
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.height(16.dp))

                    // Остальная информация
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Цена с учётом скидки
                        val discountPercentage = dish.discount?.toDoubleOrNull() ?: 0.0
                        val originalPrice = dish.price.toDoubleOrNull() ?: 0.0
                        val discountedPrice = if (discountPercentage > 0) originalPrice * (1 - discountPercentage / 100) else originalPrice

                        if (discountPercentage > 0) {
                            Column {
                                Text(
                                    "Цена: ${"%.0f".format(discountedPrice)} тг",
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                                Text(
                                    "Старая цена: ${"%.0f".format(originalPrice)} тг",
                                    color = Color.Gray,
                                    fontSize = 16.sp,
                                    style = TextStyle(textDecoration = TextDecoration.LineThrough)
                                )
                            }
                        } else {
                            Text(
                                "Цена: ${"%.0f".format(originalPrice)} тг",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Описание
                        if (dish.description.isNotBlank()) {
                            Column {
                                Text(
                                    "Описание",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(dish.description, fontSize = 16.sp)
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    Text("Категория: ${dish.category}")
                    Text("Вес / Объем: ${dish.weightOrVolume}")
                    Text("Ингредиенты: ${dish.ingredients}")
                    Text("Калории: ${dish.calories}")
                    Text("Белки: ${dish.proteins}, Жиры: ${dish.fats}, Углеводы: ${dish.carbs}")
                    Text("Время приготовления: ${dish.cookingTime}")
                    Text("Острота: ${dish.spiciness}")
                    Text("Вегетарианское: ${if (dish.vegetarian) "Да" else "Нет"}")
                    Text("Доступно: ${if (dish.availability) "Да" else "Нет"}")

                    // Рейтинг
                    // Рейтинг блюда
                    val avgRating = dish.ratingAverage ?: 0.0
                    val totalRatings = dish.ratingCount ?: 0L

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val fullStars = avgRating.toInt()
                        val hasHalfStar = (avgRating % 1) >= 0.5

                        repeat(fullStars) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                        }
                        if (hasHalfStar) {
                            Icon(Icons.Filled.StarHalf, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                        }
                        repeat(5 - fullStars - if (hasHalfStar) 1 else 0) {
                            Icon(Icons.Outlined.Star, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        }

                        Spacer(Modifier.width(4.dp))
                        Text("%.1f (%d)".format(avgRating, totalRatings), fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(16.dp))

                    // 📌 Блок отзывов
                    Text("Отзывы", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    if (dish.reviews.isNullOrEmpty()) {
                        Text("Пока отзывов нет")
                    } else {
                        dish.reviews.forEach { review ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(review.userName, fontWeight = FontWeight.Bold)

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val fullStars = review.rating.toInt()
                                        val hasHalfStar = review.rating % 1 >= 0.5
                                        repeat(fullStars) {
                                            Icon(Icons.Filled.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                                        }
                                        if (hasHalfStar) {
                                            Icon(Icons.Filled.StarHalf, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                                        }
                                        repeat(5 - fullStars - if (hasHalfStar) 1 else 0) {
                                            Icon(Icons.Outlined.Star, null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                                        }
                                    }

                                    Spacer(Modifier.height(4.dp))
                                    Text(review.comment)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
            }
            if (showAddDishScreen) {
                AddDishScreen(
                    currentUserEmail = currentUserEmail,
                    onBack = { showAddDishScreen = false }
                )
            }
            // ✏️ Редактирование блюда
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

            // ❌ Удаление блюда
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
    }


