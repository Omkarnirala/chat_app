package com.omkar.chatapp.utils

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.provider.FontRequest
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import androidx.fragment.app.Fragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.omkar.chatapp.R
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.googlecompat.GoogleCompatEmojiProvider

open class BaseFragment : Fragment() {

    var firebaseAnalytics: FirebaseAnalytics? = null
    private var firebaseCrashlytics: FirebaseCrashlytics? = null


    var cxt: Context? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        cxt = context
        firebaseAnalytics = Firebase.analytics
        firebaseCrashlytics = FirebaseCrashlytics.getInstance()
        firebaseCrashlytics?.setCrashlyticsCollectionEnabled(true)
        firebaseCrashlytics?.setUserId(getStringData(context, USER_ID, ""))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EmojiManager.install(
            GoogleCompatEmojiProvider(
                EmojiCompat.init(
                    FontRequestEmojiCompatConfig(
                        requireContext(),
                        FontRequest(
                            "com.google.android.gms.fonts",
                            "com.google.android.gms",
                            "Noto Color Emoji Compat",
                            R.array.com_google_android_gms_fonts_certs,
                        )
                    ).setReplaceAll(true)
                )
            )
        )
    }

}