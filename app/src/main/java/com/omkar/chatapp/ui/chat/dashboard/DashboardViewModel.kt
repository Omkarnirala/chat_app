package com.omkar.chatapp.ui.chat.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.omkar.chatapp.ui.signin.signup.UserFirestore

class DashboardViewModel: ViewModel() {
    private val repository = DashboardRepository()

    val allUsers: LiveData<List<UserFirestore>> = repository.getAllUsers()

    // Existing signIn and updateProfile methods
}