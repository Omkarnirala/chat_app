package com.omkar.chatapp

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.omkar.chatapp.utils.FIREBASE_INSTANCE_ID
import com.omkar.chatapp.utils.FirebaseEvent
import com.omkar.chatapp.utils.log
import com.omkar.chatapp.utils.setStringData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.omkar.chatapp.databinding.ActivityLauncherBinding
import com.omkar.chatapp.utils.BaseAppCompatActivity
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import timber.log.Timber

class LauncherActivity : BaseAppCompatActivity() {
    private val mTag = "LauncherActivity"
    private val launcherViewModel: LauncherViewModel by viewModels()

    private var connectivityManager: ConnectivityManager? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var b: ActivityLauncherBinding

    // Define and register the network callback
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            log(mTag, "Internet available")
            launcherViewModel.setInternetStatus(true)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            log(mTag, "Internet not available")
            launcherViewModel.setInternetStatus(false)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(b.root)
        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder().build()
        connectivityManager?.registerNetworkCallback(networkRequest, networkCallback)

        initView()
        setupAdvertisingInfo()
        setupFirebase()

    }

    private fun initView() {

        supportActionBar?.hide()

        firebaseAnalytics = Firebase.analytics
        firebaseAnalytics.logEvent(FirebaseEvent.LAUNCHER_ACTIVITY) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, mTag)
        }

        val formatStrategy: FormatStrategy = PrettyFormatStrategy.newBuilder()
            .showThreadInfo(true)
            .methodCount(1)
            .methodOffset(5)
            .tag("")
            .build()

        Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))

        Timber.plant(object : Timber.DebugTree(){
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                super.log(priority, tag, message, t)
                Logger.log(priority, "-$tag", message, t)
            }
        })
    }

    private fun setupAdvertisingInfo() {
        launcherViewModel.setGoogleAdvId(applicationContext)
    }

    private fun setupFirebase() {

        log(mTag, "existing id = ${FirebaseInstallations.getInstance().id}")

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                log(mTag, "Fetching FCM registration token failed = ${task.exception}")
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            log(mTag, "Fetching New FCM registration token = $token")

            setStringData(applicationContext, FIREBASE_INSTANCE_ID, token)

        })
    }

    override fun onPause() {
        super.onPause()
        connectivityManager?.unregisterNetworkCallback(networkCallback)
    }

    /**
    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
     */
    override fun onResume() {
        super.onResume()
        launcherViewModel.setInternetStatus(false)
        connectivityManager?.registerNetworkCallback(NetworkRequest.Builder().build(), networkCallback)
    }

}