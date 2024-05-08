package com.omkar.chatapp.ui.signin.signup

import com.google.firebase.auth.FirebaseUser

sealed class SignUpResult {
    data class Success(val user: FirebaseUser?) : SignUpResult()
    data class Failure(val exception: Exception?) : SignUpResult()
}
