package com.omkar.chatapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.omkar.chatapp.R
import com.omkar.chatapp.ui.signin.signup.UserDetailsModel

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private var bodyString = ""
    private var context: Context? = null

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        context = applicationContext

        log(mTag, "onMessageReceived : ${remoteMessage.data}")

        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            // Handle message within 10 seconds
            handleNow()
        }
        log(mTag, "notification: ${remoteMessage.notification}")

        remoteMessage.data.let { data ->
            val bodyDetails = data["body"]
            val titleData = data["title"]
            val priorityData = data["priority"]
            val imageURIData = data["imageURI"]
            val receiverData = data["receiverData"]
            log(
                mTag,
                "bodyDetails: $bodyDetails\n" +
                        "titleData: $titleData\n" +
                        "priorityData: $priorityData\n" +
                        "imageURIData: $imageURIData\n" +
                        "receiverData: $receiverData\n"
            )
//            val userDetails = mapToUserDetailsModel(receiverData)



        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            showNotification(it.title, it.body, remoteMessage.data.getValue("imageURI"))
        }


    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token);
    }

    private fun sendRegistrationToServer(token: String) {
        log(mTag, "SendRegistrationTokenToServer: $token")
    }

    private fun handleNow() {
        log(mTag, "Short lived task is done.")
    }

    private fun showNotification(title: String?, message: String?, value: String) {
        log("FirebaseMessagingService", "Title: $title")
        log("FirebaseMessagingService", "Message: $message")

        val channelId = CHANNEL_ID
        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.app_logo)
        val soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + this.packageName + "/" + R.raw.notification)
        val audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build()

        val user = Person.Builder()
            .setUri(value)
            .setName(title)
            .build()

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.app_logo)
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.MessagingStyle(user)
                    .addMessage(message, 45L, user)
            )
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    val channel = NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_HIGH)
                    channel.enableLights(true)
                    channel.enableVibration(true)
                    channel.setSound(soundUri, audioAttributes)
                    notificationManager.createNotificationChannel(channel)
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                log(mTag, "channelId: $channelId, title: $title")

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        notificationManager.notify(1000, notificationBuilder.build())
    }
    private fun mapToUserDetailsModel(receiverData: UserDetailsModel): UserDetailsModel {
        // Return a new instance of UserDetailsModel with data from receiverData
        return UserDetailsModel(
            uid = receiverData.uid,
            email = receiverData.email,
            displayName = receiverData.displayName,
            profileImageUrl = receiverData.profileImageUrl,
            status = receiverData.status,
            lastMessage = receiverData.lastMessage,
            lastOnlineTime = receiverData.lastOnlineTime,
            isOnline = receiverData.isOnline,
            token = receiverData.token
        )
    }
    companion object {
        private val mTag = "FirebaseMessageService"
        private const val CHANNEL_ID = "Chat Messages"
    }
}
