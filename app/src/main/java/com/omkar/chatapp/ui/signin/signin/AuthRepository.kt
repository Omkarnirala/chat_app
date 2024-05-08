package com.omkar.chatapp.ui.signin.signin

import com.google.android.gms.tasks.Task
import com.omkar.chatapp.ui.signin.signup.SignUpResult
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.omkar.chatapp.ui.signin.signup.UserFirestore
import com.omkar.chatapp.utils.timberLog

class AuthRepository {

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

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

    fun addUserToFirestore(user: UserFirestore): Task<Void> {
        return db.collection("users").document(user.uid).set(user)
    }

    fun updateUserProfile(uid: String, updates: Map<String, Any>): Task<Void> {
        return db.collection("users").document(uid).update(updates)
    }


}