package com.omkar.chatapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.omkar.chatapp.MainActivity
import com.omkar.chatapp.R
import com.omkar.chatapp.ui.chat.messaging.Message
import com.omkar.chatapp.ui.signin.signup.UserDetailsModel
import com.omkar.chatapp.utils.FirebaseUtil.notificationBuilders

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private var context: Context? = null

    companion object {
        private const val mTag = "FirebaseMessageService"
        private const val CHANNEL_ID = "Chat Messages"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        context = applicationContext

        log(mTag, "onMessageReceived : ${remoteMessage.data}")

        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            // Handle message within 10 seconds
            handleNow()
        }

        remoteMessage.data.let { data ->
            val messageBodyData = data["body"]
            val titleData = data["title"]
            val priorityData = data["priority"]
            val receiverData = data["receiverData"]
            log(
                mTag,
                "bodyDetails: $messageBodyData\n" +
                        "titleData: $titleData\n" +
                        "priorityData: $priorityData\n" +
                        "receiverData: $receiverData\n"
            )

            val gson = Gson()
            val userDetails = gson.fromJson(receiverData, UserDetailsModel::class.java)
            val messageType = object : TypeToken<List<Message>>() {}.type
            val messages: List<Message> = gson.fromJson(messageBodyData, messageType)

            log(mTag, "userDetails of Receiver Data: ${userDetails.status}")

            showNotification(messages, userDetails)
        }
    }

    private fun showNotification(messages: List<Message>, userDetails: UserDetailsModel) {

        log(mTag, "Title: ${userDetails.displayName}")
        log(mTag, "Message: $messages")

        val soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + this.packageName + "/" + R.raw.notification)
        val audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build()

        val bundle = Bundle().apply {
            putParcelable("user", userDetails)
        }
        val pendingIntent = NavDeepLinkBuilder(applicationContext).setComponentName(MainActivity::class.java).setGraph(R.navigation.main_navigation)
            .setDestination(R.id.messageFragment).setArguments(bundle).createPendingIntent()

        Glide.with(applicationContext).asBitmap().load(userDetails.profileImageUrl ?: R.drawable.ic_profile).into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {

                messages.forEach { message ->

                    val contentTitle = if (messages.size == 1) {
                        "New message from ${message.person}"
                    } else {
                        "${messages.size} new messages from ${message.person}"
                    }
                    log(mTag, "message.text: ${message.text}")
                    log(mTag, "contentTitle: $contentTitle")

                    val messagingStyle = NotificationCompat.InboxStyle()
                    messages.forEach {
                        messagingStyle.addLine(it.text)
                        messagingStyle.setBigContentTitle(contentTitle)
                        messagingStyle.setSummaryText(message.person)
                    }

                    val builder = notificationBuilders.getOrPut(message.person) {
                        NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                            .setSmallIcon(R.drawable.app_logo)
                            .setLargeIcon(resource)
                            .setSound(soundUri)
                            .setStyle(
                                NotificationCompat.InboxStyle()
                            )
                            .setAutoCancel(true)
                            .setContentIntent(pendingIntent)
                    }

                    builder.setContentText(messages.last().text)
                        .setContentTitle(contentTitle)
                        .setStyle(messagingStyle)

                    notificationBuilders[message.person] = builder

                }

                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            val channel = NotificationChannel(CHANNEL_ID, userDetails.displayName, NotificationManager.IMPORTANCE_HIGH)
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
                        log(mTag, "channelId: $CHANNEL_ID, title: ${userDetails.displayName}")

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                notificationBuilders.forEach { (person, builder) ->
                    log(mTag, "notificationBuilders.notify ID: ${person.hashCode()}")
                    log(mTag, "notificationBuilders.notify Builder: $builder")
                    notificationManager.notify(person.hashCode(), builder.build())
                }

            }

            override fun onLoadCleared(placeholder: Drawable?) {
                log(mTag, "onLoadCleared: $placeholder")
            }

        })
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

}
