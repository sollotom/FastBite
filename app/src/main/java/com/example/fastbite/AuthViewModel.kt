package com.example.fastbite

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {
    private val _email = MutableStateFlow("")
    private val _password = MutableStateFlow("")
    private val _message = MutableStateFlow("")

    val email = _email.asStateFlow()
    val password = _password.asStateFlow()
    val message = _message.asStateFlow()

    fun updateEmail(value: String) {
        _email.value = value
    }

    fun updatePassword(value: String) {
        _password.value = value
    }

    fun register() {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _message.value = "Введите email и пароль"
            return
        }
        _message.value = "Регистрация успешна!"
        // Здесь можно добавить сохранение данных (Firebase / сервер Node.js)
    }

    fun login() {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _message.value = "Введите email и пароль"
            return
        }
        // Здесь можно добавить проверку логина (Firebase / API)
        _message.value = "Вход выполнен успешно!"
    }
}
