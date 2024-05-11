package com.omkar.chatapp.ui.chat.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpdateUserProfileViewModel() : ViewModel() {

    private val userProfileRepository = UpdateUserProfileRepository()
    private val _updateResult = MutableLiveData<Boolean>()
    val updateResult: LiveData<Boolean>
        get() = _updateResult


    fun updateUserProfile(
        displayName: String?,
        profileImageUrl: String?,
        status: String?,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val updates = mutableMapOf<String, Any>()
            displayName?.let { updates["displayName"] = it }
            profileImageUrl?.let { updates["profileImageUrl"] = it }
            status?.let { updates["status"] = it }

            userProfileRepository.updateUserProfile(updates) { status ->
                _updateResult.postValue(status)
            }
        }
    }

    fun uploadImage(
        imageUri: Uri?,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            userProfileRepository.uploadImage(imageUri, onSuccess, onFailure)
        }
    }
}