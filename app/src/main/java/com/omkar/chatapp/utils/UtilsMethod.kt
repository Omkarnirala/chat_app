package com.omkar.chatapp.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.omkar.chatapp.BuildConfig
import com.omkar.chatapp.LauncherActivity
import com.omkar.chatapp.MainActivity
import com.omkar.chatapp.R
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.event.CallEndListener
import com.zegocloud.uikit.prebuilt.call.event.ErrorEventsListener
import com.zegocloud.uikit.prebuilt.call.event.SignalPluginConnectListener
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallInvitationData
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoUIKitPrebuiltCallConfigProvider
import com.zegocloud.uikit.service.express.IExpressEngineEventHandler
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason
import org.json.JSONObject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

fun log(mTag: String, message: String) {
    if (BuildConfig.DEBUG) {
        Log.d(mTag, message)
    }
}

fun timberLog(mTag: String, message: String) {
    if (BuildConfig.DEBUG) {
        Timber.tag(mTag).d(message)
    }
}

fun setStringData(context: Context?, key: String, defaultValue: String?) {
    try {
        context?.let { cxt ->
            val sharedPreferencesEditor =
                cxt.getSharedPreferences(SETTING_NAME, Context.MODE_PRIVATE).edit()
            sharedPreferencesEditor.putString(key, defaultValue)
            sharedPreferencesEditor.apply()
        } ?: run {
            log("setStringData", "context is null")
        }
    } catch (e: Exception) {
        log("setStringData", "(setStringData) catch error = ${e.message}")
    }
}

fun getStringData(context: Context?, key: String, defaultValue: String): String {
    context?.let { cxt ->
        return cxt.getSharedPreferences(SETTING_NAME, 0).getString(key, defaultValue)!!
    } ?: run {
        return defaultValue
    }
}

fun setIntData(context: Context?, key: String, defaultValue: Int) {
    try {
        context?.let { cxt ->
            val sharedPreferencesEditor =
                cxt.getSharedPreferences(SETTING_NAME, Context.MODE_PRIVATE).edit()
            sharedPreferencesEditor.putInt(key, defaultValue)
            sharedPreferencesEditor.apply()
        } ?: run {
            log("setStringData", "context is null")
        }
    } catch (e: Exception) {
        log("setStringData", "(setStringData) catch error = ${e.message}")
    }
}


fun getIntData(context: Context?, key: String, defaultValue: Int): Int {
    context?.let { cxt ->
        return cxt.getSharedPreferences(SETTING_NAME, 0).getInt(key, defaultValue)
    } ?: run {
        return defaultValue
    }
}

fun setBooleanData(context: Context?, key: String, value: Boolean) {
    try {
        context?.let { cxt ->
            val sharedPreferencesEditor = cxt.getSharedPreferences(SETTING_NAME, 0).edit()
            sharedPreferencesEditor.putBoolean(key, value)
            sharedPreferencesEditor.apply()
        } ?: run {
            log("Utils", "context is null")
        }
    } catch (e: Exception) {
        log("setBooleanData", "(setBooleanData) catch error = ${e.message}")
    }
}

fun getBooleanData(context: Context?, key: String, value: Boolean = false): Boolean {
    context?.let { cxt ->
        return cxt.getSharedPreferences(SETTING_NAME, 0).getBoolean(key, value)
    } ?: run {
        return false
    }
}

fun loginMethod(cxt: Context?) {
    cxt?.let { context ->
        val i = Intent(context, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(i)
    }
}

fun logoutMethod(cxt: Context?) {
    cxt?.let { context ->

        //FirebaseMessaging.getInstance().deleteToken()
        //FirebaseInstallations.getInstance().delete()
        //FirebaseMessaging.getInstance().deleteToken()

        val preferences: SharedPreferences = context.getSharedPreferences(SETTING_NAME, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.clear()
        editor.apply()

//        val notificationManager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager?
//        notificationManager?.cancelAll()

        //clear existing activity, launch new Activity
        val i = Intent(context, LauncherActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(i)
    }
}

fun isValidEmailAddress(email: String): Boolean {
    val pattern: Pattern = Patterns.EMAIL_ADDRESS
    return pattern.matcher(email).matches()
}


fun getEnvironment(): String {
    return "Android ${Build.VERSION.SDK_INT} ${Build.MODEL} ${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})"
}

fun getVersionName(context: Context?): String? {
    return try {
        context?.let { cxt ->
            val pInfo = cxt.packageManager.getPackageInfo(cxt.packageName, 0)
            return pInfo.versionName
        } ?: run {
            return null
        }
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }
}

fun toasty(context: Context?, message: String?) {
    context?.let { cxt ->
        Toast.makeText(cxt, message ?: run { "" }, Toast.LENGTH_SHORT).show()
    }
}

fun toasty(context: Context?, message: Int) {
    context?.let { cxt ->
        Toast.makeText(cxt, cxt.getString(message), Toast.LENGTH_SHORT).show()
    }
}

fun showPlayServiceErrorDialog(context: Context?, message: String, activity: Activity?) {

    context?.let { cxt ->
        activity?.let {
            val builder =
                AlertDialog.Builder(cxt).setCancelable(false).setMessage(message).setPositiveButton(
                    R.string.ok
                ) { _, _ ->
                    //dialog.dismiss()
                    it.finish()
                }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        }
    } ?: run {
        //context is null
    }
}


fun hideKeyboard(activity: Activity) {
    val inputManager: InputMethodManager =
        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    // check if no view has focus:
    val currentFocusedView: View? = activity.currentFocus
    if (currentFocusedView != null) {
        inputManager.hideSoftInputFromWindow(
            currentFocusedView.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN
        )
    }
}

/*Use this for case of:: dialog hide keyboard*/
fun hideDialogKeyboard(context: Context, alertDialog: android.app.AlertDialog?) {
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val currentFocusedView = alertDialog?.currentFocus
    currentFocusedView?.let {
        inputMethodManager.hideSoftInputFromWindow(
            it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
        )
    }
}

//for show keypad
fun showKeyboard(activity: Activity) {
    val inputManager: InputMethodManager =
        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    // check if view has focus:
    val currentFocusedView: View? = activity.currentFocus
    if (currentFocusedView != null) {
        inputManager.showSoftInput(
            currentFocusedView,
            InputMethodManager.SHOW_FORCED,
        )
    }
}

fun isGooglePlayServicesAvailable(activity: Activity, cxt: Context): Boolean {
    val googleApiAvailability = GoogleApiAvailability.getInstance()/*val status = googleApiAvailability.isGooglePlayServicesAvailable(activity)
    if (status != ConnectionResult.SUCCESS) {
        if (googleApiAvailability.isUserResolvableError(status)) {
            googleApiAvailability.getErrorDialog(activity, status*//*error code*//*, 2404*//*request code*//*).show()
        }
        return false
    }*/
    //log(mTag = "UtilsMethod","name = ${Build.MANUFACTURER}")

    return if (Build.MANUFACTURER == "Clover" && BuildConfig.DEBUG) {
        true
    } else {
        when (googleApiAvailability.isGooglePlayServicesAvailable(activity)) {
            ConnectionResult.API_UNAVAILABLE -> {
                showPlayServiceErrorDialog(cxt, "Google Play Service API not available.", activity)
                false
            }

            ConnectionResult.CANCELED -> {
                showPlayServiceErrorDialog(
                    cxt, "Google Play Service connection was canceled.", activity
                )
                false
            }

            ConnectionResult.DEVELOPER_ERROR -> {
                showPlayServiceErrorDialog(cxt, "Google Play Service is misconfigure.", activity)
                false
            }

            ConnectionResult.INTERNAL_ERROR -> {
                showPlayServiceErrorDialog(cxt, "An internal error occurred.", activity)
                false
            }

            ConnectionResult.INTERRUPTED -> {
                showPlayServiceErrorDialog(
                    cxt,
                    "An interrupt occurred while waiting for the connection complete.",
                    activity
                )
                false
            }

            ConnectionResult.INVALID_ACCOUNT -> {
                showPlayServiceErrorDialog(
                    cxt,
                    "Google Play Service attempted to connect to the service with an invalid account name specified.",
                    activity
                )
                false
            }

            ConnectionResult.LICENSE_CHECK_FAILED -> {
                showPlayServiceErrorDialog(
                    cxt, "Google Play Service is not licensed to the user.", activity
                )
                false
            }

            ConnectionResult.NETWORK_ERROR -> {
                showPlayServiceErrorDialog(cxt, "A network error occurred.", activity)
                false
            }

            ConnectionResult.RESOLUTION_REQUIRED -> {
                showPlayServiceErrorDialog(
                    cxt,
                    "Google Play Service completing the connection requires some form of resolution.",
                    activity
                )
                false
            }

            ConnectionResult.RESTRICTED_PROFILE -> {
                showPlayServiceErrorDialog(
                    cxt,
                    "The current user profile is restricted and cannot use authenticated features.",
                    activity
                )
                false
            }

            ConnectionResult.SERVICE_DISABLED -> {
                showPlayServiceErrorDialog(
                    cxt,
                    "The installed version of Google Play services has been disabled on this device.",
                    activity
                )
                false
            }

            ConnectionResult.SERVICE_INVALID -> {
                showPlayServiceErrorDialog(
                    cxt,
                    "The version of the Google Play services installed on this device is not authentic.",
                    activity
                )
                false
            }

            ConnectionResult.SERVICE_MISSING -> {
                showPlayServiceErrorDialog(
                    cxt, "Google Play services is missing on this device.", activity
                )
                false
            }

            ConnectionResult.SERVICE_MISSING_PERMISSION -> {
                showPlayServiceErrorDialog(
                    cxt,
                    "Google Play service doesn't have one or more required permissions.",
                    activity
                )
                false
            }

            ConnectionResult.SERVICE_UPDATING -> {
                showPlayServiceErrorDialog(
                    cxt, "Google Play service is currently being updated on this device.", activity
                )
                false
            }

            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> {
                showPlayServiceErrorDialog(
                    cxt, "The installed version of Google Play services is out of date.", activity
                )
                false
            }

            ConnectionResult.SIGN_IN_FAILED -> {
                showPlayServiceErrorDialog(
                    cxt,
                    "Google Play Service attempted to connect to the service but the user is not signed in.",
                    activity
                )
                false
            }

            ConnectionResult.SIGN_IN_REQUIRED -> {
                showPlayServiceErrorDialog(
                    cxt,
                    "Google Play Service attempted to connect to the service but the user is not signed in.",
                    activity
                )
                false
            }

            ConnectionResult.SUCCESS -> {
                true
            }

            ConnectionResult.TIMEOUT -> {
                showPlayServiceErrorDialog(
                    cxt,
                    "Google Play Service timeout was exceeded while waiting for the connection to complete.",
                    activity
                )
                false
            }

            else -> false
        }
    }

}

fun clearSharedPreference(cxt: Context?) {
    cxt?.let { context ->
        val preferences: SharedPreferences =
            context.getSharedPreferences(SETTING_NAME, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
    }
}

/*
* For a given number generates a Verhoeff digit
*
*/
fun generateVerhoeff(num: String): String {
    var c = 0
    val myArray = stringToReversedIntArray(num)
    for (i in myArray.indices) {
        c = d[c][p[(i + 1) % 8][myArray[i]]]
    }
    return inv[c].toString()
}


/*
 * Validates that an entered number is Verhoeff compliant.
 * NB: Make sure the check digit is the last one.
 */
fun validateVerhoeff(num: String): Boolean {
    var c = 0
    val myArray = stringToReversedIntArray(num)
    for (i in myArray.indices) {
        c = d[c][p[i % 8][myArray[i]]]
    }
    return c == 0
}


/*
 * Converts a string to a reversed integer array.
 */
fun stringToReversedIntArray(num: String): IntArray {
    var myArray = IntArray(num.length)
    for (i in num.indices) {
        myArray[i] = num.substring(i, i + 1).toInt()
    }
    myArray = reverse(myArray)
    return myArray
}

/*
 * Reverses an int array
 */
fun reverse(myArray: IntArray): IntArray {
    val reversed = IntArray(myArray.size)
    for (i in myArray.indices) {
        reversed[i] = myArray[myArray.size - (i + 1)]
    }
    return reversed
}

fun formatTimestampTo12Hour(timestamp: com.google.firebase.Timestamp?): String {
    val inputFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm:ss a 'UTC'XXX", Locale.ENGLISH)
    val date: Date = inputFormat.parse(inputFormat.toString()) ?: throw IllegalArgumentException("Invalid date") // Convert Timestamp to java.util.Date
    val formatter = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
    return formatter.format(date.toString())
}

/*
fun initCallInviteService(requiredApplication: Application, uid: String?, displayName: String?) {
    val application: Application = requiredApplication
    val appID: Long = 1602751084
    val appSign = "98aed2b4e01ff9affd18ae5aa05f658293d4116b1a6238939dbeb02238f487d3"
    val userID: String = uid ?: ""
    val userName: String = displayName ?: ""
    timberLog("UtilsMethod", "initCallInviteService uid: $uid")
    timberLog("UtilsMethod", "initCallInviteService userName: $userName")
    val callInvitationConfig = ZegoUIKitPrebuiltCallInvitationConfig().apply {
        provider = ZegoUIKitPrebuiltCallConfigProvider { invitationData -> getConfig(invitationData) }
    }

    ZegoUIKitPrebuiltCallService.events.errorEventsListener =
        ErrorEventsListener { errorCode, message -> Timber.d("onError() called with: errorCode = [$errorCode], message = [$message]") }

    ZegoUIKitPrebuiltCallService.events.invitationEvents.pluginConnectListener =
        SignalPluginConnectListener { state, event, extendedData -> Timber.d("onSignalPluginConnectionStateChanged() called with: state = [$state], event = [$event], extendedData = [$extendedData]") }

    ZegoUIKitPrebuiltCallService.init(
        application,
        appID,
        appSign,
        userID,
        userName,
        callInvitationConfig
    )

    ZegoUIKitPrebuiltCallService.events.callEvents.callEndListener =
        CallEndListener { callEndReason, jsonObject -> Timber.d("onCallEnd() called with: callEndReason = [$callEndReason], jsonObject = [$jsonObject]") }

    ZegoUIKitPrebuiltCallService.events.callEvents.setExpressEngineEventHandler(object :
        IExpressEngineEventHandler() {
        override fun onRoomStateChanged(
            roomID: String,
            reason: ZegoRoomStateChangedReason,
            errorCode: Int,
            extendedData: JSONObject,
        ) {
            Timber.d("onRoomStateChanged() called with: roomID = [$roomID], reason = [$reason], errorCode = [$errorCode], extendedData = [$extendedData]")
        }
    })
}

private fun getConfig(invitationData: ZegoCallInvitationData): ZegoUIKitPrebuiltCallConfig {
    val isVideoCall = invitationData.type == ZegoInvitationType.VIDEO_CALL.value
    val isGroupCall = invitationData.invitees.size > 1
    return when {
        isVideoCall && isGroupCall -> ZegoUIKitPrebuiltCallConfig.groupVideoCall()
        !isVideoCall && isGroupCall -> ZegoUIKitPrebuiltCallConfig.groupVoiceCall()
        !isVideoCall -> ZegoUIKitPrebuiltCallConfig.oneOnOneVoiceCall()
        else -> ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall()
    }
}

*/


