package com.omkar.chatapp.ui.signin.signin

import com.omkar.chatapp.ui.signin.signup.SignUpResult
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.omkar.chatapp.utils.timberLog

class AuthRepository {

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    fun signIn(email: String, password: String, callback: (Boolean, Exception?, AuthResult?) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback.invoke(true, null, task.result)
                } else {
                    callback.invoke(false, task.exception, null)
                }
            }
    }

    fun resetPassword(email: String, callback: (Boolean, Exception?, Void?) -> Unit) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                timberLog("AuthRepo", "$task")
                if (task.isSuccessful) {
                    callback.invoke(true, null, task.result)
                } else {
                    callback.invoke(false, task.exception, null)
                }
            }
    }

    fun signUp(email: String, password: String, callback: (SignUpResult) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    callback.invoke(SignUpResult.Success(user))
                } else {
                    callback.invoke(SignUpResult.Failure(task.exception))
                }
            }
    }


}