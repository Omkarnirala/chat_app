package com.omkar.chatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.omkar.chatapp.utils.BaseAppCompatActivity
import com.omkar.chatapp.utils.FirebaseUtil

class MainActivity : BaseAppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Set user as online when the activity is created
//        FirebaseUtil.updateUserStatus(auth.currentUser?.uid, true)

    }

    override fun onPause() {
        super.onPause()
        FirebaseUtil.updateUserStatus(auth.currentUser?.uid, false)
    }

    override fun onResume() {
        super.onResume()
        FirebaseUtil.updateUserStatus(auth.currentUser?.uid, true)
    }

    override fun onDestroy() {
        super.onDestroy()
        FirebaseUtil.updateUserStatus(auth.currentUser?.uid, false)
    }
}