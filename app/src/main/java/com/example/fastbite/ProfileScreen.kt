package com.example.fastbite

import android.widget.Toast
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ==================== ENUMS AND DATA CLASSES ====================

enum class ProfileScreenType {
    Main, Settings, EditProfile, Addresses, AddAddress, EditAddress,
    Help, FAQ, ContactSupport, TermsAndConditions, AboutApp,
    MyReviews, EditReview
}

enum class SupportTopic(val title: String, val titleKz: String) {
    ORDER("Проблема с заказом", "Тапсырыс мәселесі"),
    PAYMENT("Оплата", "Төлем"),
    DELIVERY("Доставка", "Жеткізу"),
    RESTAURANT("Ресторан", "Мейрамхана"),
    APP("Приложение", "Қосымша"),
    OTHER("Другое", "Басқа")
}

enum class Language(val code: String, val displayName: String) {
    RUSSIAN("ru", "Русский"),
    KAZAKH("kz", "Қазақша")
}

data class FAQItem(
    val question: String,
    val answer: String,
    val questionKz: String,
    val answerKz: String
)

// ==================== STRING RESOURCES ====================

object Strings {
    var currentLanguage = mutableStateOf(Language.RUSSIAN)

    fun setLanguage(language: Language) {
        currentLanguage.value = language
    }

    fun getLanguage(): Language = currentLanguage.value

    // Profile Screen Titles
    val profile: String get() = if (currentLanguage.value == Language.KAZAKH) "Профиль" else "Профиль"
    val settings: String get() = if (currentLanguage.value == Language.KAZAKH) "Баптаулар" else "Настройки"
    val editProfile: String get() = if (currentLanguage.value == Language.KAZAKH) "Профильді өңдеу" else "Редактировать профиль"
    val deliveryAddresses: String get() = if (currentLanguage.value == Language.KAZAKH) "Жеткізу мекенжайлары" else "Адреса доставки"
    val addAddress: String get() = if (currentLanguage.value == Language.KAZAKH) "Мекенжай қосу" else "Добавить адрес"
    val editAddress: String get() = if (currentLanguage.value == Language.KAZAKH) "Мекенжайды өңдеу" else "Редактировать адрес"
    val helpAndSupport: String get() = if (currentLanguage.value == Language.KAZAKH) "Көмек және қолдау" else "Помощь и поддержка"
    val faq: String get() = if (currentLanguage.value == Language.KAZAKH) "Жиі қойылатын сұрақтар" else "Часто задаваемые вопросы"
    val contactSupport: String get() = if (currentLanguage.value == Language.KAZAKH) "Қолдау қызметіне хабарласу" else "Связаться с поддержкой"
    val termsAndConditions: String get() = if (currentLanguage.value == Language.KAZAKH) "Ережелер мен шарттар" else "Правила и условия"
    val aboutApp: String get() = if (currentLanguage.value == Language.KAZAKH) "Қосымша туралы" else "О приложении"
    val myReviews: String get() = if (currentLanguage.value == Language.KAZAKH) "Менің пікірлерім" else "Мои отзывы"
    val editReview: String get() = if (currentLanguage.value == Language.KAZAKH) "Пікірді өңдеу" else "Редактировать отзыв"

    // Common
    val back: String get() = if (currentLanguage.value == Language.KAZAKH) "Артқа" else "Назад"
    val save: String get() = if (currentLanguage.value == Language.KAZAKH) "Сақтау" else "Сохранить"
    val cancel: String get() = if (currentLanguage.value == Language.KAZAKH) "Бас тарту" else "Отмена"
    val delete: String get() = if (currentLanguage.value == Language.KAZAKH) "Жою" else "Удалить"
    val edit: String get() = if (currentLanguage.value == Language.KAZAKH) "Өңдеу" else "Редактировать"
    val update: String get() = if (currentLanguage.value == Language.KAZAKH) "Жаңарту" else "Обновить"

    // Profile Menu
    val myOrders: String get() = if (currentLanguage.value == Language.KAZAKH) "Менің тапсырыстарым" else "Мои заказы"
    val orderHistory: String get() = if (currentLanguage.value == Language.KAZAKH) "Тапсырыстар тарихы мен күйі" else "История и статус заказов"
    val yourReviews: String get() = if (currentLanguage.value == Language.KAZAKH) "Тағамдар туралы пікірлеріңіз" else "Ваши отзывы о блюдах"
    val paymentMethods: String get() = if (currentLanguage.value == Language.KAZAKH) "Төлем тәсілдері" else "Способы оплаты"
    val cardsCash: String get() = if (currentLanguage.value == Language.KAZAKH) "Карталар, қолма-қол ақша" else "Карты, наличные"
    val help: String get() = if (currentLanguage.value == Language.KAZAKH) "Көмек" else "Помощь"
    val faqAndSupport: String get() = if (currentLanguage.value == Language.KAZAKH) "ЖҚС және қолдау" else "FAQ и поддержка"

    // Addresses
    val addAddressButton: String get() = if (currentLanguage.value == Language.KAZAKH) "Мекенжай қосу" else "Добавить адрес"
    val savedAddresses: String get() = if (currentLanguage.value == Language.KAZAKH) "Сақталған мекенжайлар" else "Сохраненные адреса"
    val noAddresses: String get() = if (currentLanguage.value == Language.KAZAKH) "Сізде әлі сақталған мекенжайлар жоқ" else "У вас пока нет сохраненных адресов"
    val addAddressForQuickOrder: String get() = if (currentLanguage.value == Language.KAZAKH) "Тапсырысты жылдам рәсімдеу үшін мекенжай қосыңыз" else "Добавьте адрес для быстрого оформления заказа"
    val setAsDefault: String get() = if (currentLanguage.value == Language.KAZAKH) "Негізгі ету" else "Сделать основным"
    val defaultAddress: String get() = if (currentLanguage.value == Language.KAZAKH) "Негізгі мекенжай" else "Основной адрес"
    val deleteAddressTitle: String get() = if (currentLanguage.value == Language.KAZAKH) "Мекенжайды жою" else "Удалить адрес"
    val deleteAddressConfirm: String get() = if (currentLanguage.value == Language.KAZAKH) "Бұл мекенжайды жойғыңыз келетініне сенімдісіз бе?" else "Вы уверены, что хотите удалить адрес?"
    val addressDeleted: String get() = if (currentLanguage.value == Language.KAZAKH) "Мекенжай жойылды" else "Адрес удален"
    val addressAdded: String get() = if (currentLanguage.value == Language.KAZAKH) "Мекенжай қосылды" else "Адрес добавлен"
    val addressUpdated: String get() = if (currentLanguage.value == Language.KAZAKH) "Мекенжай жаңартылды" else "Адрес обновлен"
    val defaultAddressChanged: String get() = if (currentLanguage.value == Language.KAZAKH) "Негізгі мекенжай өзгертілді" else "Основной адрес изменен"

    // Settings
    val notifications: String get() = if (currentLanguage.value == Language.KAZAKH) "Хабарландырулар" else "Уведомления"
    val receiveOrderNotifications: String get() = if (currentLanguage.value == Language.KAZAKH) "Тапсырыстар туралы хабарландырулар алу" else "Получать уведомления о заказах"
    val emailNewsletter: String get() = if (currentLanguage.value == Language.KAZAKH) "Email-таратылым" else "Email-рассылка"
    val receiveNewsAndOffers: String get() = if (currentLanguage.value == Language.KAZAKH) "Жаңалықтар мен акцияларды алу" else "Получать новости и акции"
    val language_setting: String get() = if (currentLanguage.value == Language.KAZAKH) "Тіл" else "Язык"
    val logout: String get() = if (currentLanguage.value == Language.KAZAKH) "Аккаунттан шығу" else "Выйти из аккаунта"
    val endSession: String get() = if (currentLanguage.value == Language.KAZAKH) "Сеансты аяқтау" else "Завершить сеанс"
    val logoutConfirm: String get() = if (currentLanguage.value == Language.KAZAKH) "Аккаунттан шығу" else "Выход из аккаунта"
    val logoutConfirmMessage: String get() = if (currentLanguage.value == Language.KAZAKH) "Шыққыңыз келетініне сенімдісіз бе?" else "Вы уверены, что хотите выйти?"
    val exit: String get() = if (currentLanguage.value == Language.KAZAKH) "Шығу" else "Выйти"

    // Edit Profile
    val name: String get() = if (currentLanguage.value == Language.KAZAKH) "Аты" else "Имя"
    val phone: String get() = if (currentLanguage.value == Language.KAZAKH) "Телефон" else "Телефон"
    val email: String get() = if (currentLanguage.value == Language.KAZAKH) "Email" else "Email"
    val fillInformation: String get() = if (currentLanguage.value == Language.KAZAKH) "Ақпаратты толтырыңыз" else "Заполните информацию"
    val user: String get() = if (currentLanguage.value == Language.KAZAKH) "Пайдаланушы" else "Пользователь"

    // Address Form
    val newAddress: String get() = if (currentLanguage.value == Language.KAZAKH) "Жаңа мекенжай" else "Новый адрес"
    val addressRequired: String get() = if (currentLanguage.value == Language.KAZAKH) "Мекенжай *" else "Адрес *"
    val addressPlaceholder: String get() = if (currentLanguage.value == Language.KAZAKH) "Қала, көше, үй" else "Город, улица, дом"
    val addressRequiredError: String get() = if (currentLanguage.value == Language.KAZAKH) "Мекенжай міндетті түрде толтырылуы керек" else "Адрес обязателен для заполнения"
    val apartment: String get() = if (currentLanguage.value == Language.KAZAKH) "Пәтер/кеңсе" else "Квартира/офис"
    val optional: String get() = if (currentLanguage.value == Language.KAZAKH) "Міндетті емес" else "Необязательно"
    val entrance: String get() = if (currentLanguage.value == Language.KAZAKH) "Кіреберіс" else "Подъезд"
    val floor: String get() = if (currentLanguage.value == Language.KAZAKH) "Қабат" else "Этаж"
    val intercom: String get() = if (currentLanguage.value == Language.KAZAKH) "Домофон" else "Домофон"
    val makeDefaultAddress: String get() = if (currentLanguage.value == Language.KAZAKH) "Негізгі мекенжай ету" else "Сделать основным адресом"

    // Help
    val howCanWeHelp: String get() = if (currentLanguage.value == Language.KAZAKH) "Сізге қалай көмектесе аламыз?" else "Чем мы можем помочь?"
    val chooseSection: String get() = if (currentLanguage.value == Language.KAZAKH) "Сізді қызықтыратын бөлімді таңдаңыз" else "Выберите интересующий вас раздел"
    val faqDescription: String get() = if (currentLanguage.value == Language.KAZAKH) "Танымал сұрақтарға жауаптар" else "Ответы на популярные вопросы"
    val contactSupportDescription: String get() = if (currentLanguage.value == Language.KAZAKH) "Бізге жазыңыз, біз көмектесеміз" else "Напишите нам, и мы поможем"
    val termsDescription: String get() = if (currentLanguage.value == Language.KAZAKH) "Сервисті пайдалану шарттары" else "Условия использования сервиса"
    val aboutDescription: String get() = if (currentLanguage.value == Language.KAZAKH) "Нұсқа 1.0.0 • Құпиялылық саясаты" else "Версия 1.0.0 • Политика конфиденциальности"
    val contactInformation: String get() = if (currentLanguage.value == Language.KAZAKH) "Байланыс ақпараты" else "Контактная информация"
    val daily: String get() = if (currentLanguage.value == Language.KAZAKH) "Күн сайын" else "Ежедневно"

    // Contact Support
    val writeToUs: String get() = if (currentLanguage.value == Language.KAZAKH) "Бізге жазыңыз" else "Напишите нам"
    val describeProblem: String get() = if (currentLanguage.value == Language.KAZAKH) "Мәселеңізді сипаттаңыз, біз жақын арада жауап береміз" else "Опишите вашу проблему, и мы ответим в ближайшее время"
    val topic: String get() = if (currentLanguage.value == Language.KAZAKH) "Өтініш тақырыбы" else "Тема обращения"
    val selectTopic: String get() = if (currentLanguage.value == Language.KAZAKH) "Тақырыпты таңдаңыз" else "Выберите тему"
    val message: String get() = if (currentLanguage.value == Language.KAZAKH) "Хабарлама" else "Сообщение"
    val describeProblemDetailed: String get() = if (currentLanguage.value == Language.KAZAKH) "Мәселеңізді толығырақ сипаттаңыз..." else "Опишите вашу проблему подробнее..."
    val send: String get() = if (currentLanguage.value == Language.KAZAKH) "Жіберу" else "Отправить"
    val messageSent: String get() = if (currentLanguage.value == Language.KAZAKH) "Хабарлама жіберілді!" else "Сообщение отправлено!"
    val weWillReply: String get() = if (currentLanguage.value == Language.KAZAKH) "Біз сізге жауап береміз" else "Мы ответим вам на"
    val pleaseWriteMessage: String get() = if (currentLanguage.value == Language.KAZAKH) "Хабарлама жазыңыз" else "Пожалуйста, напишите сообщение"

    // Reviews
    val noReviews: String get() = if (currentLanguage.value == Language.KAZAKH) "Пікірлер жоқ" else "Нет отзывов"
    val yourReviewsWillAppear: String get() = if (currentLanguage.value == Language.KAZAKH) "Тағамдар туралы пікірлеріңіз осында көрсетіледі" else "Ваши отзывы о блюдах будут отображаться здесь"
    val deleteReviewTitle: String get() = if (currentLanguage.value == Language.KAZAKH) "Пікірді жою" else "Удалить отзыв"
    val deleteReviewConfirm: String get() = if (currentLanguage.value == Language.KAZAKH) "Бұл пікірді жойғыңыз келетініне сенімдісіз бе?" else "Вы уверены, что хотите удалить этот отзыв?"
    val reviewDeleted: String get() = if (currentLanguage.value == Language.KAZAKH) "Пікір жойылды" else "Отзыв удален"
    val reviewUpdated: String get() = if (currentLanguage.value == Language.KAZAKH) "Пікір жаңартылды" else "Отзыв обновлен"
    val errorUpdating: String get() = if (currentLanguage.value == Language.KAZAKH) "Жаңарту кезінде қате" else "Ошибка при обновлении"
    val errorDeleting: String get() = if (currentLanguage.value == Language.KAZAKH) "Жою кезінде қате" else "Ошибка при удалении"
    val yourRating: String get() = if (currentLanguage.value == Language.KAZAKH) "Сіздің бағаңыз" else "Ваша оценка"
    val yourComment: String get() = if (currentLanguage.value == Language.KAZAKH) "Сіздің пікіріңіз" else "Ваш комментарий"
    val shareImpressions: String get() = if (currentLanguage.value == Language.KAZAKH) "Әсерлеріңізбен бөлісіңіз..." else "Расскажите о ваших впечатлениях..."
    val veryBad: String get() = if (currentLanguage.value == Language.KAZAKH) "Өте нашар" else "Очень плохо"
    val bad: String get() = if (currentLanguage.value == Language.KAZAKH) "Нашар" else "Плохо"
    val normal: String get() = if (currentLanguage.value == Language.KAZAKH) "Қалыпты" else "Нормально"
    val good: String get() = if (currentLanguage.value == Language.KAZAKH) "Жақсы" else "Хорошо"
    val excellent: String get() = if (currentLanguage.value == Language.KAZAKH) "Өте жақсы" else "Отлично"
    val deleteReviewPermanently: String get() = if (currentLanguage.value == Language.KAZAKH) "Бұл әрекетті болдырмау мүмкін емес." else "Это действие нельзя отменить."
    val loadingReviews: String get() = if (currentLanguage.value == Language.KAZAKH) "Пікірлер жүктелуде..." else "Загрузка отзывов..."
    val reviews_one: String get() = if (currentLanguage.value == Language.KAZAKH) "1 пікір" else "1 отзыв"
    val reviews_few: String get() = if (currentLanguage.value == Language.KAZAKH) "пікір" else "отзыва"
    val reviews_many: String get() = if (currentLanguage.value == Language.KAZAKH) "пікір" else "отзывов"
    val date: String get() = if (currentLanguage.value == Language.KAZAKH) "Күні" else "Дата"

    // About
    val aboutUs: String get() = if (currentLanguage.value == Language.KAZAKH) "Біз туралы" else "О нас"
    val aboutContent: String get() = if (currentLanguage.value == Language.KAZAKH)
        "FastBite - бұл сіздің қалаңыздағы ең үздік мейрамханалардан тағам жеткізу сервисі." else
        "FastBite - это сервис доставки еды из лучших ресторанов вашего города."
    val ourMission: String get() = if (currentLanguage.value == Language.KAZAKH) "Біздің миссиямыз" else "Наша миссия"
    val missionContent: String get() = if (currentLanguage.value == Language.KAZAKH)
        "Дәмді тағамды кез келген уақытта әркімге қолжетімді ету." else
        "Делать вкусную еду доступной каждому в любое время."
    val contacts: String get() = if (currentLanguage.value == Language.KAZAKH) "Байланыстар" else "Контакты"
    val version: String get() = if (currentLanguage.value == Language.KAZAKH) "Нұсқа" else "Версия"
    val allRightsReserved: String get() = if (currentLanguage.value == Language.KAZAKH)
        "© 2024 FastBite. Барлық құқықтар қорғалған." else
        "© 2024 FastBite. Все права защищены."
}

// ==================== КОМПОНЕНТ ЗВЕЗД ====================

@Composable
fun RatingStarsVisual(
    rating: Double,
    modifier: Modifier = Modifier,
    starSize: Dp = 16.dp
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
    ) {
        val fullStars = rating.toInt()
        val hasHalfStar = (rating - fullStars) >= 0.5

        repeat(fullStars) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(starSize)
            )
        }

        if (hasHalfStar) {
            Icon(
                Icons.Filled.StarHalf,
                contentDescription = null,
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(starSize)
            )
        }

        val emptyStars = 5 - fullStars - if (hasHalfStar) 1 else 0
        repeat(emptyStars) {
            Icon(
                Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(starSize)
            )
        }
    }
}

@Composable
fun RatingStarsWithText(
    rating: Double,
    modifier: Modifier = Modifier,
    starSize: Dp = 16.dp
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        RatingStarsVisual(rating = rating, starSize = starSize)
        Text(
            text = "%.1f".format(rating),
            fontSize = (starSize.value - 2).sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFFFC107)
        )
    }
}

// ==================== MAIN PROFILE SCREEN ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userEmail: String,
    onLogout: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToDish: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val coroutineScope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf(ProfileScreenType.Main) }
    var previousScreens by remember { mutableStateOf(listOf<ProfileScreenType>()) }

    var selectedAddressForEdit by remember { mutableStateOf<Address?>(null) }
    var selectedReviewForEdit by remember { mutableStateOf<Review?>(null) }
    var addressToDelete by remember { mutableStateOf<Address?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var userName by remember { mutableStateOf("") }
    var userPhone by remember { mutableStateOf("") }
    var addresses by remember { mutableStateOf<List<Address>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var notificationsEnabled by remember { mutableStateOf(true) }
    var emailEnabled by remember { mutableStateOf(false) }
    var currentLanguage by remember { mutableStateOf(Language.RUSSIAN) }
    var showLanguageDialog by remember { mutableStateOf(false) }

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

    BackHandler {
        if (currentScreen != ProfileScreenType.Main) {
            navigateBack()
        }
    }

    LaunchedEffect(userEmail) {
        if (userEmail.isBlank()) return@LaunchedEffect

        isLoading = true
        try {
            val userDoc = db.collection("users").document(userEmail).get().await()
            if (userDoc.exists()) {
                userName = userDoc.getString("name") ?: ""
                userPhone = userDoc.getString("phone") ?: ""
            }
            loadAddresses(userEmail, db) { addresses = it }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    // Language selection dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(Strings.language_setting, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Language.values().forEach { language ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable {
                                currentLanguage = language
                                Strings.setLanguage(language)
                                showLanguageDialog = false
                            },
                            color = if (currentLanguage == language) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        ) {
                            Text(
                                text = language.displayName,
                                modifier = Modifier.padding(16.dp),
                                color = if (currentLanguage == language) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(Strings.cancel)
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showDeleteDialog && addressToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                addressToDelete = null
            },
            title = { Text(Strings.deleteAddressTitle, fontWeight = FontWeight.Bold) },
            text = { Text("${Strings.deleteAddressConfirm}\n${addressToDelete!!.address}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                deleteAddress(userEmail, addressToDelete!!.id, db)
                                Toast.makeText(context, Strings.addressDeleted, Toast.LENGTH_SHORT).show()
                                loadAddresses(userEmail, db) { addresses = it }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                showDeleteDialog = false
                                addressToDelete = null
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text(Strings.delete) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    addressToDelete = null
                }) { Text(Strings.cancel) }
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
                            ProfileScreenType.Main -> Strings.profile
                            ProfileScreenType.Settings -> Strings.settings
                            ProfileScreenType.EditProfile -> Strings.editProfile
                            ProfileScreenType.Addresses -> Strings.deliveryAddresses
                            ProfileScreenType.AddAddress -> Strings.addAddress
                            ProfileScreenType.EditAddress -> Strings.editAddress
                            ProfileScreenType.Help -> Strings.helpAndSupport
                            ProfileScreenType.FAQ -> Strings.faq
                            ProfileScreenType.ContactSupport -> Strings.contactSupport
                            ProfileScreenType.TermsAndConditions -> Strings.termsAndConditions
                            ProfileScreenType.AboutApp -> Strings.aboutApp
                            ProfileScreenType.MyReviews -> Strings.myReviews
                            ProfileScreenType.EditReview -> Strings.editReview
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                navigationIcon = {
                    if (currentScreen != ProfileScreenType.Main) {
                        IconButton(onClick = { navigateBack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = Strings.back)
                        }
                    }
                },
                actions = {
                    when (currentScreen) {
                        ProfileScreenType.Main -> {
                            IconButton(onClick = { navigateTo(ProfileScreenType.Settings) }) {
                                Icon(Icons.Outlined.Settings, contentDescription = Strings.settings)
                            }
                        }
                        else -> {}
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (isLoading && currentScreen == ProfileScreenType.Main) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                when (currentScreen) {
                    ProfileScreenType.Main -> {
                        MainProfileContent(
                            userName = userName,
                            userEmail = userEmail,
                            userPhone = userPhone,
                            addresses = addresses,
                            onNavigateToOrders = onNavigateToOrders,
                            onNavigateToSettings = { navigateTo(ProfileScreenType.Settings) },
                            onNavigateToAddresses = { navigateTo(ProfileScreenType.Addresses) },
                            onNavigateToHelp = { navigateTo(ProfileScreenType.Help) },
                            onNavigateToReviews = { navigateTo(ProfileScreenType.MyReviews) }
                        )
                    }
                    ProfileScreenType.Settings -> {
                        SettingsContent(
                            userName = userName,
                            userPhone = userPhone,
                            userEmail = userEmail,
                            notificationsEnabled = notificationsEnabled,
                            emailEnabled = emailEnabled,
                            currentLanguage = currentLanguage,
                            onNotificationsChange = { notificationsEnabled = it },
                            onEmailChange = { emailEnabled = it },
                            onLanguageClick = { showLanguageDialog = true },
                            onEditProfile = { navigateTo(ProfileScreenType.EditProfile) },
                            onLogout = onLogout
                        )
                    }
                    ProfileScreenType.EditProfile -> {
                        EditProfileContent(
                            userName = userName,
                            userPhone = userPhone,
                            userEmail = userEmail,
                            onSave = { name, phone ->
                                coroutineScope.launch {
                                    saveUser(userEmail, name, phone, db)
                                    userName = name
                                    userPhone = phone
                                    navigateBack()
                                }
                            }
                        )
                    }
                    ProfileScreenType.Addresses -> {
                        AddressesContent(
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
                                        Toast.makeText(context, Strings.defaultAddressChanged, Toast.LENGTH_SHORT).show()
                                        loadAddresses(userEmail, db) { addresses = it }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        )
                    }
                    ProfileScreenType.AddAddress -> {
                        AddEditAddressContent(
                            address = null,
                            onSave = { newAddress ->
                                coroutineScope.launch {
                                    try {
                                        addAddress(userEmail, newAddress, db)
                                        Toast.makeText(context, Strings.addressAdded, Toast.LENGTH_SHORT).show()
                                        loadAddresses(userEmail, db) { addresses = it }
                                        navigateBack()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        )
                    }
                    ProfileScreenType.EditAddress -> {
                        selectedAddressForEdit?.let { address ->
                            AddEditAddressContent(
                                address = address,
                                onSave = { updatedAddress ->
                                    coroutineScope.launch {
                                        try {
                                            updateAddress(userEmail, updatedAddress, db)
                                            Toast.makeText(context, Strings.addressUpdated, Toast.LENGTH_SHORT).show()
                                            loadAddresses(userEmail, db) { addresses = it }
                                            navigateBack()
                                            selectedAddressForEdit = null
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            )
                        }
                    }
                    ProfileScreenType.Help -> {
                        HelpContent(
                            onNavigateToFAQ = { navigateTo(ProfileScreenType.FAQ) },
                            onNavigateToContactSupport = { navigateTo(ProfileScreenType.ContactSupport) },
                            onNavigateToTerms = { navigateTo(ProfileScreenType.TermsAndConditions) },
                            onNavigateToAbout = { navigateTo(ProfileScreenType.AboutApp) }
                        )
                    }
                    ProfileScreenType.FAQ -> FAQContent()
                    ProfileScreenType.ContactSupport -> {
                        ContactSupportContent(
                            userEmail = userEmail,
                            userName = userName
                        )
                    }
                    ProfileScreenType.TermsAndConditions -> TermsContent()
                    ProfileScreenType.AboutApp -> AboutContent()
                    ProfileScreenType.MyReviews -> {
                        MyReviewsContent(
                            userEmail = userEmail,
                            userName = userName,
                            onEditReview = { review ->
                                selectedReviewForEdit = review
                                navigateTo(ProfileScreenType.EditReview)
                            },
                            onNavigateToDish = onNavigateToDish
                        )
                    }
                    ProfileScreenType.EditReview -> {
                        selectedReviewForEdit?.let { review ->
                            EditReviewContent(
                                review = review,
                                onSave = { rating, comment ->
                                    coroutineScope.launch {
                                        try {
                                            updateReviewInFirebase(review.id, rating, comment)
                                            Toast.makeText(context, Strings.reviewUpdated, Toast.LENGTH_SHORT).show()
                                            navigateBack()
                                            selectedReviewForEdit = null
                                        } catch (e: Exception) {
                                            Toast.makeText(context, Strings.errorUpdating, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onDelete = {
                                    coroutineScope.launch {
                                        try {
                                            deleteReviewFromFirebase(review.id)
                                            Toast.makeText(context, Strings.reviewDeleted, Toast.LENGTH_SHORT).show()
                                            navigateBack()
                                            selectedReviewForEdit = null
                                        } catch (e: Exception) {
                                            Toast.makeText(context, Strings.errorDeleting, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onBack = {
                                    navigateBack()
                                    selectedReviewForEdit = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== FIRESTORE FUNCTIONS ====================

private fun loadAddresses(email: String, db: FirebaseFirestore, onResult: (List<Address>) -> Unit) {
    db.collection("users").document(email).collection("address").get()
        .addOnSuccessListener { documents ->
            val addresses = documents.mapNotNull { doc ->
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
                } catch (e: Exception) { null }
            }.sortedByDescending { it.isDefault }
            onResult(addresses)
        }
        .addOnFailureListener { onResult(emptyList()) }
}

private suspend fun setDefaultAddress(email: String, addressId: String, db: FirebaseFirestore) {
    val snapshot = db.collection("users").document(email).collection("address")
        .whereEqualTo("isDefault", true).get().await()
    for (doc in snapshot.documents) {
        doc.reference.update("isDefault", false).await()
    }
    db.collection("users").document(email).collection("address").document(addressId)
        .update("isDefault", true).await()
}

private suspend fun addAddress(email: String, address: Address, db: FirebaseFirestore) {
    if (address.isDefault) {
        val snapshot = db.collection("users").document(email).collection("address")
            .whereEqualTo("isDefault", true).get().await()
        for (doc in snapshot.documents) {
            doc.reference.update("isDefault", false).await()
        }
    }
    val data = hashMapOf(
        "address" to address.address,
        "apartment" to address.apartment,
        "entrance" to address.entrance,
        "floor" to address.floor,
        "intercom" to address.intercom,
        "isDefault" to address.isDefault,
        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
    )
    db.collection("users").document(email).collection("address").add(data).await()
}

private suspend fun updateAddress(email: String, address: Address, db: FirebaseFirestore) {
    if (address.isDefault) {
        val snapshot = db.collection("users").document(email).collection("address")
            .whereEqualTo("isDefault", true).get().await()
        for (doc in snapshot.documents) {
            if (doc.id != address.id) {
                doc.reference.update("isDefault", false).await()
            }
        }
    }
    val data = hashMapOf(
        "address" to address.address,
        "apartment" to address.apartment,
        "entrance" to address.entrance,
        "floor" to address.floor,
        "intercom" to address.intercom,
        "isDefault" to address.isDefault
    )
    db.collection("users").document(email).collection("address").document(address.id).set(data).await()
}

private suspend fun deleteAddress(email: String, addressId: String, db: FirebaseFirestore) {
    db.collection("users").document(email).collection("address").document(addressId).delete().await()
}

private suspend fun saveUser(email: String, name: String, phone: String, db: FirebaseFirestore) {
    val data = hashMapOf(
        "email" to email,
        "name" to name,
        "phone" to phone,
        "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
    )
    db.collection("users").document(email).set(data, com.google.firebase.firestore.SetOptions.merge()).await()
}

private suspend fun loadUserReviews(userEmail: String): List<Review> {
    val db = FirebaseFirestore.getInstance()
    return try {
        val snapshot = db.collection("reviews")
            .whereEqualTo("userEmail", userEmail)
            .get()
            .await()

        val reviews = snapshot.documents.mapNotNull { doc: com.google.firebase.firestore.DocumentSnapshot ->
            try {
                Review(
                    id = doc.id,
                    userName = doc.getString("userName") ?: "",
                    userEmail = doc.getString("userEmail") ?: "",
                    rating = doc.getDouble("rating") ?: 0.0,
                    comment = doc.getString("comment") ?: "",
                    date = doc.getString("date") ?: "",
                    dishId = doc.getString("dishId") ?: "",
                    dishName = doc.getString("dishName") ?: "",
                    restaurantId = doc.getString("restaurantId") ?: ""
                )
            } catch (e: Exception) {
                null
            }
        }

        reviews.sortedByDescending { review: Review ->
            try {
                val parts = review.date.split(" ")
                val dateParts = parts[0].split(".")
                val timeParts = if (parts.size > 1) parts[1].split(":") else listOf("0", "0")

                val day = dateParts[0].toIntOrNull() ?: 1
                val month = dateParts[1].toIntOrNull() ?: 1
                val year = dateParts[2].toIntOrNull() ?: 2024
                val hour = timeParts[0].toIntOrNull() ?: 0
                val minute = timeParts[1].toIntOrNull() ?: 0

                year * 100000000L + month * 1000000L + day * 10000L + hour * 100L + minute
            } catch (e: Exception) {
                0L
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

private suspend fun updateReviewInFirebase(reviewId: String, rating: Float, comment: String) {
    val db = FirebaseFirestore.getInstance()
    val currentDate = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())

    val updates = hashMapOf<String, Any>(
        "rating" to rating.toDouble(),
        "comment" to comment,
        "date" to currentDate
    )

    db.collection("reviews").document(reviewId).update(updates).await()

    val reviewDoc = db.collection("reviews").document(reviewId).get().await()
    val dishId = reviewDoc.getString("dishId") ?: return
    updateDishRating(dishId)
}

private suspend fun deleteReviewFromFirebase(reviewId: String) {
    val db = FirebaseFirestore.getInstance()

    val reviewDoc = db.collection("reviews").document(reviewId).get().await()
    val dishId = reviewDoc.getString("dishId") ?: ""

    db.collection("reviews").document(reviewId).delete().await()

    if (dishId.isNotBlank()) {
        db.collection("dishes").document(dishId)
            .update("reviewsIds", com.google.firebase.firestore.FieldValue.arrayRemove(reviewId))
            .await()
        updateDishRating(dishId)
    }
}

private suspend fun updateDishRating(dishId: String) {
    val db = FirebaseFirestore.getInstance()

    try {
        val dishDoc = db.collection("dishes").document(dishId).get().await()
        val reviewsIds = dishDoc.get("reviewsIds") as? List<String> ?: emptyList()

        if (reviewsIds.isNotEmpty()) {
            var totalRating = 0.0
            var count = 0

            for (reviewId in reviewsIds) {
                try {
                    val reviewDoc = db.collection("reviews").document(reviewId).get().await()
                    val rating = reviewDoc.getDouble("rating") ?: 0.0
                    totalRating += rating
                    count++
                } catch (e: Exception) {
                    // Пропускаем удаленные отзывы
                }
            }

            val avgRating = if (count > 0) totalRating / count else 0.0
            db.collection("dishes").document(dishId).update(
                mapOf(
                    "ratingAverage" to avgRating,
                    "ratingCount" to count
                )
            ).await()
        } else {
            db.collection("dishes").document(dishId).update(
                mapOf(
                    "ratingAverage" to 0.0,
                    "ratingCount" to 0
                )
            ).await()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// ==================== SCREEN CONTENTS ====================

@Composable
fun MainProfileContent(
    userName: String,
    userEmail: String,
    userPhone: String,
    addresses: List<Address>,
    onNavigateToOrders: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAddresses: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToReviews: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ProfileHeader(userName = userName, userEmail = userEmail, userPhone = userPhone)
        Spacer(modifier = Modifier.height(8.dp))

        ProfileMenuItem(
            icon = Icons.Outlined.ShoppingBag,
            title = Strings.myOrders,
            subtitle = Strings.orderHistory,
            onClick = onNavigateToOrders
        )
        ProfileMenuItem(
            icon = Icons.Outlined.RateReview,
            title = Strings.myReviews,
            subtitle = Strings.yourReviews,
            onClick = onNavigateToReviews
        )
        ProfileMenuItem(
            icon = Icons.Outlined.LocationOn,
            title = Strings.deliveryAddresses,
            subtitle = when {
                addresses.isEmpty() -> Strings.addAddressForQuickOrder.take(20) + "..."
                addresses.size == 1 -> Strings.reviews_one
                addresses.size in 2..4 -> "${addresses.size} ${Strings.reviews_few}"
                else -> "${addresses.size} ${Strings.reviews_many}"
            },
            onClick = onNavigateToAddresses
        )
        ProfileMenuItem(
            icon = Icons.Outlined.Payment,
            title = Strings.paymentMethods,
            subtitle = Strings.cardsCash,
            onClick = { }
        )
        ProfileMenuItem(
            icon = Icons.Outlined.Help,
            title = Strings.help,
            subtitle = Strings.faqAndSupport,
            onClick = onNavigateToHelp
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "FastBite v1.0.0",
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun ProfileHeader(userName: String, userEmail: String, userPhone: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(70.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (userName.isNotBlank()) userName.take(1).uppercase() else userEmail.take(1).uppercase(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (userName.isNotBlank()) userName else Strings.user,
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
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    badge: String? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (badge != null) {
                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                        Text(text = badge, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun SettingsContent(
    userName: String,
    userPhone: String,
    userEmail: String,
    notificationsEnabled: Boolean,
    emailEnabled: Boolean,
    currentLanguage: Language,
    onNotificationsChange: (Boolean) -> Unit,
    onEmailChange: (Boolean) -> Unit,
    onLanguageClick: () -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    var showLogoutConfirm by remember { mutableStateOf(false) }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text(Strings.logoutConfirm, fontWeight = FontWeight.Bold) },
            text = { Text(Strings.logoutConfirmMessage, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = { showLogoutConfirm = false; onLogout() }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text(Strings.exit)
                }
            },
            dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text(Strings.cancel) } },
            shape = RoundedCornerShape(28.dp)
        )
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        SettingsItem(
            icon = Icons.Outlined.Person,
            title = Strings.editProfile,
            subtitle = if (userName.isNotBlank() || userPhone.isNotBlank()) "$userName • $userPhone" else Strings.fillInformation,
            onClick = onEditProfile
        )
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
        SettingsSwitchItem(
            icon = Icons.Outlined.Notifications,
            title = Strings.notifications,
            subtitle = Strings.receiveOrderNotifications,
            checked = notificationsEnabled,
            onCheckedChange = onNotificationsChange
        )
        SettingsSwitchItem(
            icon = Icons.Outlined.Email,
            title = Strings.emailNewsletter,
            subtitle = Strings.receiveNewsAndOffers,
            checked = emailEnabled,
            onCheckedChange = onEmailChange
        )
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
        SettingsSelectItem(
            icon = Icons.Outlined.Language,
            title = Strings.language_setting,
            value = currentLanguage.displayName,
            onClick = onLanguageClick
        )
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
        SettingsItem(
            icon = Icons.Outlined.Logout,
            title = Strings.logout,
            subtitle = Strings.endSession,
            onClick = { showLogoutConfirm = true },
            isDestructive = true
        )
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit, isDestructive: Boolean = false) {
    val contentColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), color = Color.Transparent, shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = contentColor)
                    Text(text = subtitle, fontSize = 13.sp, color = if (isDestructive) contentColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = if (isDestructive) contentColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun SettingsSwitchItem(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary))
    }
}

@Composable
fun SettingsSelectItem(icon: ImageVector, title: String, value: String, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), color = Color.Transparent, shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 8.dp))
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun EditProfileContent(userName: String, userPhone: String, userEmail: String, onSave: (String, String) -> Unit) {
    var name by remember { mutableStateOf(userName) }
    var phone by remember { mutableStateOf(userPhone) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(Strings.name) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Outlined.Person, null, tint = MaterialTheme.colorScheme.primary) }
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(Strings.phone) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Outlined.Phone, null, tint = MaterialTheme.colorScheme.primary) }
            )
            OutlinedTextField(
                value = userEmail,
                onValueChange = {},
                label = { Text(Strings.email) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                enabled = false,
                leadingIcon = { Icon(Icons.Outlined.Email, null, tint = MaterialTheme.colorScheme.primary) }
            )
        }
        Button(
            onClick = { onSave(name, phone) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(Strings.save)
        }
    }
}

@Composable
fun AddressesContent(
    addresses: List<Address>,
    onAddAddress: () -> Unit,
    onEditAddress: (Address) -> Unit,
    onDeleteAddress: (Address) -> Unit,
    onSetDefaultAddress: (Address) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = Strings.savedAddresses,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (addresses.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.LocationOn, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Text(Strings.noAddresses, fontSize = 16.sp, color = MaterialTheme.colorScheme.outline)
                    Text(Strings.addAddressForQuickOrder, fontSize = 14.sp, color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
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
        Button(onClick = onAddAddress, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Outlined.Add, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(Strings.addAddressButton)
        }
    }
}

@Composable
fun AddressCard(address: Address, onEdit: () -> Unit, onDelete: () -> Unit, onSetDefault: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Text(text = address.address, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Outlined.Edit, Strings.edit, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Outlined.Delete, Strings.delete, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
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
                modifier = Modifier.fillMaxWidth().padding(start = 36.dp, top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (address.isDefault) {
                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                        Text(Strings.defaultAddress, fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimary)
                    }
                } else {
                    TextButton(onClick = onSetDefault, modifier = Modifier.height(32.dp)) {
                        Text(Strings.setAsDefault, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditAddressContent(address: Address?, onSave: (Address) -> Unit) {
    var addressText by remember { mutableStateOf(address?.address ?: "") }
    var apartment by remember { mutableStateOf(address?.apartment ?: "") }
    var entrance by remember { mutableStateOf(address?.entrance ?: "") }
    var floor by remember { mutableStateOf(address?.floor ?: "") }
    var intercom by remember { mutableStateOf(address?.intercom ?: "") }
    var isDefault by remember { mutableStateOf(address?.isDefault ?: false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = if (address == null) Strings.newAddress else Strings.editAddress,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            OutlinedTextField(
                value = addressText,
                onValueChange = { addressText = it },
                label = { Text(Strings.addressRequired) },
                placeholder = { Text(Strings.addressPlaceholder) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                isError = addressText.isBlank(),
                leadingIcon = { Icon(Icons.Outlined.LocationOn, null, tint = MaterialTheme.colorScheme.primary) }
            )
            if (addressText.isBlank()) {
                Text(
                    text = Strings.addressRequiredError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            OutlinedTextField(
                value = apartment,
                onValueChange = { apartment = it },
                label = { Text(Strings.apartment) },
                placeholder = { Text(Strings.optional) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            OutlinedTextField(
                value = entrance,
                onValueChange = { entrance = it },
                label = { Text(Strings.entrance) },
                placeholder = { Text(Strings.optional) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            OutlinedTextField(
                value = floor,
                onValueChange = { floor = it },
                label = { Text(Strings.floor) },
                placeholder = { Text(Strings.optional) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            OutlinedTextField(
                value = intercom,
                onValueChange = { intercom = it },
                label = { Text(Strings.intercom) },
                placeholder = { Text(Strings.optional) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(Strings.makeDefaultAddress, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Switch(
                    checked = isDefault,
                    onCheckedChange = { isDefault = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                onSave(Address(
                    id = address?.id ?: System.currentTimeMillis().toString(),
                    address = addressText,
                    apartment = apartment,
                    entrance = entrance,
                    floor = floor,
                    intercom = intercom,
                    isDefault = isDefault
                ))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = addressText.isNotBlank()
        ) {
            Text(if (address == null) Strings.save else Strings.update)
        }
    }
}

// ==================== HELP SCREENS ====================

@Composable
fun HelpContent(
    onNavigateToFAQ: () -> Unit,
    onNavigateToContactSupport: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text(
            text = Strings.howCanWeHelp,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = Strings.chooseSection,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        HelpCard(
            icon = Icons.Outlined.QuestionAnswer,
            title = Strings.faq,
            description = Strings.faqDescription,
            color = MaterialTheme.colorScheme.primary,
            onClick = onNavigateToFAQ
        )
        Spacer(modifier = Modifier.height(12.dp))
        HelpCard(
            icon = Icons.Outlined.SupportAgent,
            title = Strings.contactSupport,
            description = Strings.contactSupportDescription,
            color = MaterialTheme.colorScheme.tertiary,
            onClick = onNavigateToContactSupport
        )
        Spacer(modifier = Modifier.height(12.dp))
        HelpCard(
            icon = Icons.Outlined.Description,
            title = Strings.termsAndConditions,
            description = Strings.termsDescription,
            color = MaterialTheme.colorScheme.secondary,
            onClick = onNavigateToTerms
        )
        Spacer(modifier = Modifier.height(12.dp))
        HelpCard(
            icon = Icons.Outlined.Info,
            title = Strings.aboutApp,
            description = Strings.aboutDescription,
            color = MaterialTheme.colorScheme.outline,
            onClick = onNavigateToAbout
        )

        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = Strings.contactInformation,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                ContactInfoRow(icon = Icons.Outlined.Email, text = "support@fastbite.com")
                ContactInfoRow(icon = Icons.Outlined.Phone, text = "+7 (999) 123-45-67")
                ContactInfoRow(
                    icon = Icons.Outlined.Schedule,
                    text = "${Strings.daily} 10:00 - 22:00"
                )
            }
        }
    }
}

@Composable
fun HelpCard(icon: ImageVector, title: String, description: String, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(50.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ContactInfoRow(icon: ImageVector, text: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun FAQContent() {
    val faqItems = listOf(
        FAQItem(
            "Как сделать заказ?",
            "Выберите ресторан, добавьте блюда в корзину, укажите адрес доставки и выберите способ оплаты.",
            "Тапсырысты қалай жасауға болады?",
            "Мейрамхананы таңдаңыз, себетке тағамдарды қосыңыз, жеткізу мекенжайын көрсетіңіз және төлем тәсілін таңдаңыз."
        ),
        FAQItem(
            "Сколько времени занимает доставка?",
            "Среднее время доставки составляет 30-60 минут.",
            "Жеткізу қанша уақытты алады?",
            "Орташа жеткізу уақыты 30-60 минутты құрайды."
        ),
        FAQItem(
            "Как оплатить заказ?",
            "Наличными курьеру, банковской картой при получении или онлайн.",
            "Тапсырысты қалай төлеуге болады?",
            "Курьерге қолма-қол ақшамен, алған кезде банк картасымен немесе онлайн."
        ),
        FAQItem(
            "Можно ли изменить или отменить заказ?",
            "Да, до того, как ресторан начал его готовить.",
            "Тапсырысты өзгертуге немесе болдырмауға бола ма?",
            "Иә, мейрамхана оны дайындауды бастағанға дейін."
        ),
        FAQItem(
            "Что делать, если заказ не привезли вовремя?",
            "Свяжитесь с поддержкой через чат или по телефону.",
            "Тапсырыс уақытында жеткізілмесе не істеу керек?",
            "Чат арқылы немесе телефон арқылы қолдау қызметіне хабарласыңыз."
        ),
        FAQItem(
            "Как оставить отзыв о блюде?",
            "После получения заказа в разделе 'Мои заказы'.",
            "Тағам туралы пікірді қалай қалдыруға болады?",
            "Тапсырысты алғаннан кейін 'Менің тапсырыстарым' бөлімінде."
        ),
        FAQItem(
            "Безопасно ли платить онлайн?",
            "Да, все платежи защищены шифрованием.",
            "Онлайн төлеу қауіпсіз бе?",
            "Иә, барлық төлемдер шифрлаумен қорғалған."
        ),
        FAQItem(
            "Как изменить личные данные?",
            "Профиль → Настройки → Редактировать профиль.",
            "Жеке деректерді қалай өзгертуге болады?",
            "Профиль → Баптаулар → Профильді өңдеу."
        )
    )

    var expandedIndex by remember { mutableStateOf(-1) }
    val isKazakh = Strings.getLanguage() == Language.KAZAKH

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(faqItems.size) { index ->
            FAQCard(
                item = faqItems[index],
                isExpanded = expandedIndex == index,
                isKazakh = isKazakh,
                onExpandChange = { expandedIndex = if (expandedIndex == index) -1 else index }
            )
        }
    }
}

@Composable
fun FAQCard(item: FAQItem, isExpanded: Boolean, isKazakh: Boolean, onExpandChange: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onExpandChange),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isKazakh) item.questionKz else item.question,
                    fontSize = 16.sp,
                    fontWeight = if (isExpanded) FontWeight.Bold else FontWeight.Medium,
                    color = if (isExpanded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    null,
                    tint = if (isExpanded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isKazakh) item.answerKz else item.answer,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun ContactSupportContent(userEmail: String, userName: String) {
    val context = LocalContext.current
    var message by remember { mutableStateOf("") }
    var selectedTopic by remember { mutableStateOf(SupportTopic.ORDER) }
    var showTopicDialog by remember { mutableStateOf(false) }
    val isKazakh = Strings.getLanguage() == Language.KAZAKH

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = Strings.writeToUs,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = Strings.describeProblem,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (userName.isNotBlank()) userName else userEmail,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = Strings.topic,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth().clickable { showTopicDialog = true },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isKazakh) selectedTopic.titleKz else selectedTopic.title,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(Icons.Default.ArrowDropDown, Strings.selectTopic, tint = MaterialTheme.colorScheme.primary)
            }
        }

        if (showTopicDialog) {
            AlertDialog(
                onDismissRequest = { showTopicDialog = false },
                title = { Text(Strings.selectTopic, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        SupportTopic.values().forEach { topic ->
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    selectedTopic = topic
                                    showTopicDialog = false
                                },
                                color = if (selectedTopic == topic) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            ) {
                                Text(
                                    text = if (isKazakh) topic.titleKz else topic.title,
                                    modifier = Modifier.padding(16.dp),
                                    color = if (selectedTopic == topic) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showTopicDialog = false }) { Text(Strings.cancel) } },
                shape = RoundedCornerShape(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text(Strings.message) },
            placeholder = { Text(Strings.describeProblemDetailed) },
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(16.dp),
            minLines = 8
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (message.isNotBlank()) {
                    Toast.makeText(context, "${Strings.messageSent} ${Strings.weWillReply} $userEmail", Toast.LENGTH_LONG).show()
                    message = ""
                } else {
                    Toast.makeText(context, Strings.pleaseWriteMessage, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = message.isNotBlank()
        ) {
            Icon(Icons.Outlined.Send, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(Strings.send)
        }
    }
}

@Composable
fun TermsContent() {
    val isKazakh = Strings.getLanguage() == Language.KAZAKH

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            TermsSection(
                title = if (isKazakh) "1. Жалпы ережелер" else "1. Общие положения",
                icon = Icons.Outlined.Info,
                content = if (isKazakh)
                    listOf(
                        "1.1. FastBite қосымшасын пайдалана отырып, сіз осы Ережелермен келісесіз.",
                        "1.2. FastBite серіктес мейрамханалардан тағамға тапсырыс беру платформасын ұсынады.",
                        "1.3. Біз ережелерді кез келген уақытта өзгерту құқығын сақтаймыз."
                    ) else
                    listOf(
                        "1.1. Используя приложение FastBite, вы соглашаетесь с настоящими Правилами.",
                        "1.2. FastBite предоставляет платформу для заказа еды из ресторанов-партнеров.",
                        "1.3. Мы оставляем за собой право изменять правила в любое время."
                    )
            )
        }
        item {
            TermsSection(
                title = if (isKazakh) "2. Тіркеу және аккаунт" else "2. Регистрация и аккаунт",
                icon = Icons.Outlined.Person,
                content = if (isKazakh)
                    listOf(
                        "2.1. Тапсырысты рәсімдеу үшін тіркелу қажет.",
                        "2.2. Сіз өзіңіздің тіркелгі деректеріңіздің сақталуына жауаптысыз."
                    ) else
                    listOf(
                        "2.1. Для оформления заказа необходима регистрация.",
                        "2.2. Вы несете ответственность за сохранность своих учетных данных."
                    )
            )
        }
        item {
            TermsSection(
                title = if (isKazakh) "3. Тапсырысты рәсімдеу" else "3. Оформление заказа",
                icon = Icons.Outlined.ShoppingCart,
                content = if (isKazakh)
                    listOf(
                        "3.1. Тапсырысты рәсімдей отырып, сіз ақпараттың дұрыстығын растайсыз.",
                        "3.2. Қосымшадағы бағалар мейрамханадағы бағалардан өзгеше болуы мүмкін."
                    ) else
                    listOf(
                        "3.1. Оформляя заказ, вы подтверждаете правильность информации.",
                        "3.2. Цены в приложении могут отличаться от цен в ресторане."
                    )
            )
        }
        item {
            TermsSection(
                title = if (isKazakh) "4. Төлем" else "4. Оплата",
                icon = Icons.Outlined.Payment,
                content = if (isKazakh)
                    listOf(
                        "4.1. Төлем тәсілдері: қолма-қол ақша, карталар, Apple Pay, Google Pay.",
                        "4.2. Қаражатты қайтару 3-10 жұмыс күні ішінде жүзеге асырылады."
                    ) else
                    listOf(
                        "4.1. Доступны способы оплаты: наличные, карты, Apple Pay, Google Pay.",
                        "4.2. Возврат средств осуществляется в течение 3-10 рабочих дней."
                    )
            )
        }
        item {
            TermsSection(
                title = if (isKazakh) "5. Жеткізу" else "5. Доставка",
                icon = Icons.Outlined.DeliveryDining,
                content = if (isKazakh)
                    listOf(
                        "5.1. Жеткізу көрсетілген мекенжай бойынша жүзеге асырылады.",
                        "5.2. Тапсырыстың ең төменгі сомасы мейрамханаға байланысты."
                    ) else
                    listOf(
                        "5.1. Доставка осуществляется по указанному адресу.",
                        "5.2. Минимальная сумма заказа зависит от ресторана."
                    )
            )
        }
        item {
            TermsSection(
                title = if (isKazakh) "6. Байланыс ақпараты" else "6. Контактная информация",
                icon = Icons.Outlined.SupportAgent,
                content = listOf("Email: support@fastbite.com", "Телефон: +7 (999) 123-45-67")
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = if (isKazakh) "Соңғы жаңарту: 15 наурыз 2024 ж." else "Последнее обновление: 15 марта 2024 г.",
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TermsSection(title: String, icon: ImageVector, content: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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

@Composable
fun AboutContent() {
    val isKazakh = Strings.getLanguage() == Language.KAZAKH

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "FB",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "FastBite",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "${Strings.version} 1.0.0 (Build 100)",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        AboutSection(
            title = Strings.aboutUs,
            icon = Icons.Outlined.Info,
            content = Strings.aboutContent
        )
        AboutSection(
            title = Strings.ourMission,
            icon = Icons.Outlined.EmojiObjects,
            content = Strings.missionContent
        )
        AboutSection(
            title = Strings.contacts,
            icon = Icons.Outlined.Email,
            content = if (isKazakh)
                "Email: dev@fastbite.com\nСайт: www.fastbite.com" else
                "Email: dev@fastbite.com\nСайт: www.fastbite.com"
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = Strings.allRightsReserved,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun AboutSection(title: String, icon: ImageVector, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Text(text = content, fontSize = 14.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ==================== REVIEWS SCREENS ====================

@Composable
fun MyReviewsContent(
    userEmail: String,
    userName: String,
    onEditReview: (Review) -> Unit,
    onNavigateToDish: (String) -> Unit = {}
) {
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var reviewToDelete by remember { mutableStateOf<Review?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isKazakh = Strings.getLanguage() == Language.KAZAKH

    LaunchedEffect(userEmail) {
        isLoading = true
        reviews = loadUserReviews(userEmail)
        isLoading = false
    }

    fun refreshReviews() {
        coroutineScope.launch {
            isLoading = true
            reviews = loadUserReviews(userEmail)
            isLoading = false
        }
    }

    if (showDeleteDialog && reviewToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; reviewToDelete = null },
            title = { Text(Strings.deleteReviewTitle, fontWeight = FontWeight.Bold) },
            text = { Text(Strings.deleteReviewConfirm) },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                reviewToDelete?.let { review ->
                                    deleteReviewFromFirebase(review.id)
                                    Toast.makeText(context, Strings.reviewDeleted, Toast.LENGTH_SHORT).show()
                                    refreshReviews()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, Strings.errorDeleting, Toast.LENGTH_SHORT).show()
                            } finally {
                                showDeleteDialog = false
                                reviewToDelete = null
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text(Strings.delete) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; reviewToDelete = null }) { Text(Strings.cancel) }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = when {
                    isLoading -> Strings.loadingReviews
                    reviews.isEmpty() -> Strings.noReviews
                    reviews.size == 1 -> Strings.reviews_one
                    reviews.size in 2..4 -> "${reviews.size} ${Strings.reviews_few}"
                    else -> "${reviews.size} ${Strings.reviews_many}"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (reviews.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Outlined.RateReview,
                        null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        Strings.noReviews,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        Strings.yourReviewsWillAppear,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reviews, key = { it.id }) { review ->
                    MyReviewCard(
                        review = review,
                        onEdit = { onEditReview(review) },
                        onDelete = {
                            reviewToDelete = review
                            showDeleteDialog = true
                        },
                        onNavigateToDish = onNavigateToDish
                    )
                }
            }
        }
    }
}

@Composable
fun MyReviewCard(
    review: Review,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onNavigateToDish: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (review.dishId.isNotBlank()) {
                    onNavigateToDish(review.dishId)
                }
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.dishName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Outlined.Edit,
                            Strings.edit,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Outlined.Delete,
                            Strings.delete,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RatingStarsVisual(rating = review.rating, starSize = 18.dp)
                Text(
                    text = "%.1f".format(review.rating),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFFC107)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (review.comment.isNotBlank()) {
                Text(
                    text = review.comment,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.date,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun EditReviewContent(
    review: Review,
    onSave: (Float, String) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    var rating by remember { mutableStateOf(review.rating.toFloat()) }
    var comment by remember { mutableStateOf(review.comment) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val isKazakh = Strings.getLanguage() == Language.KAZAKH

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, Strings.back)
            }
            Text(
                Strings.editReview,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = review.dishName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${Strings.date}: ${review.date}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = Strings.yourRating,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) { index ->
                    IconButton(
                        onClick = { rating = (index + 1).toFloat() },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            if (index < rating) Icons.Filled.Star else Icons.Outlined.Star,
                            "${index + 1} ${if (isKazakh) "жұлдыз" else "звезд"}",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Text(
                text = when (rating.toInt()) {
                    1 -> Strings.veryBad
                    2 -> Strings.bad
                    3 -> Strings.normal
                    4 -> Strings.good
                    5 -> Strings.excellent
                    else -> ""
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFFFC107),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = Strings.yourComment,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                placeholder = { Text(Strings.shareImpressions) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(Strings.cancel)
                }

                Button(
                    onClick = { onSave(rating, comment) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = rating > 0
                ) {
                    Text(Strings.save)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Outlined.Delete, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(Strings.deleteReviewTitle)
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(Strings.deleteReviewTitle, fontWeight = FontWeight.Bold) },
            text = { Text("${Strings.deleteReviewConfirm} ${Strings.deleteReviewPermanently}") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(Strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(Strings.cancel)
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}