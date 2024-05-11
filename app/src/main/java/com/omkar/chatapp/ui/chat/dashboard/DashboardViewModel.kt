package com.omkar.chatapp.ui.chat.dashboard

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omkar.chatapp.ui.signin.signup.UserDetailsModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    private val repository = DashboardRepository()

    fun getCurrentUser(): MutableLiveData<UserDetailsModel?> {
        return repository.getUserData()
    }

    fun setUserOnlineStatus(isOnline: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setUserOnlineStatus(isOnline)
        }
    }
}