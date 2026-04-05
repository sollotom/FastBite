package com.example.fastbite

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {
    private val _email = MutableStateFlow("")
    private val _password = MutableStateFlow("")
    private val _userName = MutableStateFlow("")
    private val _isSeller = MutableStateFlow(false)
    private val _message = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)

    val email = _email.asStateFlow()
    val password = _password.asStateFlow()
    val userName = _userName.asStateFlow()
    val isSeller = _isSeller.asStateFlow()
    val message = _message.asStateFlow()
    val isLoading = _isLoading.asStateFlow()

    fun updateEmail(value: String) { _email.value = value }
    fun updatePassword(value: String) { _password.value = value }
    fun updateUserName(value: String) { _userName.value = value }
    fun updateIsSeller(value: Boolean) { _isSeller.value = value }

    fun clearMessage() { _message.value = "" }
}