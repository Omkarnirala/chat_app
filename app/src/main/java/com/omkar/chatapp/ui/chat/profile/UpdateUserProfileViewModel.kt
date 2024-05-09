package com.omkar.chatapp.ui.chat.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

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
        val updates = mutableMapOf<String, Any>()

        displayName?.let { updates["displayName"] = it }
        profileImageUrl?.let { updates["profileImageUrl"] = it }
        status?.let { updates["status"] = it }

        userProfileRepository.updateUserProfile(updates) { status ->
            _updateResult.postValue(status)
        }
    }

    fun uploadImage(
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        userProfileRepository.uploadImage(imageUri, onSuccess, onFailure)
    }
}