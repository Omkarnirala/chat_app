package com.omkar.chatapp.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.resources.Compatibility.Api18Impl.setAutoCancel
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
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

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private var context: Context? = null

    companion object {
        private val mTag = "FirebaseMessageService"
        private const val CHANNEL_ID = "Chat Messages"
        const val GROUP_KEY_WORK_EMAIL = "com.android.example.WORK_EMAIL"
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
        log(mTag, "notification: ${remoteMessage.notification}")

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

//            showNotification(userDetails.displayName, messages, userDetails.profileImageUrl.toString(), userDetails)

/*            showNewNotification(
                context,
                userDetails.displayName,
                messages,
                userDetails.profileImageUrl.toString(),
                userDetails
            )*/

            sendNoti()

        }

    }

    private fun sendNoti() {
        val SUMMARY_ID = 0
        val GROUP_KEY_WORK_EMAIL = "com.android.example.WORK_EMAIL"

        val newMessageNotification1 = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle("emailObject1.getSummary()")
            .setContentText("You will not believe...")
            .setStyle(NotificationCompat.InboxStyle()
                .addLine("Alex Faarborg Check this out")
                .addLine("Jeff Chang Launch Party")
                .setBigContentTitle("2 new messages")
                .setSummaryText("janedoe@example.com"))
            .setGroup(GROUP_KEY_WORK_EMAIL)
            .build()

        val newMessageNotification2 = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle("emailObject2.getSummary()")
            .setContentText("Please join us to celebrate the...")
            .setStyle(NotificationCompat.InboxStyle()
                .addLine("Alex Faarborg Check this out")
                .addLine("Jeff Chang Launch Party")
                .setBigContentTitle("2 new messages")
                .setSummaryText("janedoe@example.com"))
            .setGroup(GROUP_KEY_WORK_EMAIL)
            .build()

        val summaryNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("emailObject.getSummary()")
            // Set content text to support devices running API level < 24.
            .setContentText("Two new messages")
            .setSmallIcon(R.drawable.app_logo)
            // Specify which group this notification belongs to.
            .setGroup(GROUP_KEY_WORK_EMAIL)
            // Set this notification as the summary for the group.
            .setGroupSummary(true)
            .build()

        NotificationManagerCompat.from(this).apply {
            if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(1, newMessageNotification1)
            notify(2, newMessageNotification2)
            notify(SUMMARY_ID, summaryNotification)
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

    private fun showNewNotification(
        context: Context?,
        userName: String?,
        messages: List<Message>,
        avatarUrl: String,
        userDetails: UserDetailsModel,
    ) {
        log("FirebaseMessagingService", "Title: $userName")
        log("FirebaseMessagingService", "Message: $messages")

        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val contentTitle = if (messages.size == 1) {
            "New message from $userName"
        } else {
            "${messages.size} new messages from $userName"
        }

        val messagingStyle = NotificationCompat.InboxStyle()
        messages.forEach {
            messagingStyle.addLine(it.text)
        }

        val soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + this.packageName + "/" + R.raw.notification)
        val audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build()

        val bundle = Bundle().apply {
            putParcelable("user", userDetails)
        }
        val pendingIntent = NavDeepLinkBuilder(applicationContext).setComponentName(MainActivity::class.java).setGraph(R.navigation.main_navigation)
            .setDestination(R.id.messageFragment).setArguments(bundle).createPendingIntent()

        Glide.with(context).asBitmap().load(avatarUrl).into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.app_logo)
                    .setContentTitle(contentTitle)
                    .setLargeIcon(resource)
                    .setStyle(messagingStyle)
                    .setAutoCancel(true)
                    .setGroup(GROUP_KEY_WORK_EMAIL)
                    .setContentIntent(pendingIntent)

                notificationBuilder.setGroupSummary(true)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(CHANNEL_ID, userName, NotificationManager.IMPORTANCE_HIGH).apply {
                        enableLights(true)
                        enableVibration(true)
                        setSound(soundUri, audioAttributes)
                    }
                    notificationManager.createNotificationChannel(channel)
                } else {
                    notificationBuilder.setSound(soundUri)
                }

                notificationManager.notify(1000, notificationBuilder.build())
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                log("FirebaseMessagingService", "onLoadCleared: $placeholder")
            }
        })
    }

    private fun showNotification(userName: String?, message: List<Message>, value: String, userDetails: UserDetailsModel) {
        log("FirebaseMessagingService", "Title: $userName")
        log("FirebaseMessagingService", "Message: $message")

        val bundle = Bundle().apply {
            putParcelable("user", userDetails)
        }

        val pendingIntent = NavDeepLinkBuilder(applicationContext).setComponentName(MainActivity::class.java).setGraph(R.navigation.main_navigation)
            .setDestination(R.id.messageFragment).setArguments(bundle).createPendingIntent()

        val channelId = CHANNEL_ID
        val soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + this.packageName + "/" + R.raw.notification)
        val audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build()

        val user = Person.Builder()
            .setUri(value)
            .setName(userName)
            .build()

        val contentTitle = if (message.size == 1) {
            "New Message from $userName"
        } else {
            "${message.size} new message from $userName"
        }

        val messagingStyle = NotificationCompat.InboxStyle()
        message.forEach {
            messagingStyle.addLine(it.text)
        }

        var notificationBuilder: NotificationCompat.Builder
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        Glide.with(context!!).asBitmap().load(value).into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                notificationBuilder = NotificationCompat.Builder(context!!, channelId)
                    .setSmallIcon(R.drawable.app_logo)
                    .setContentTitle(contentTitle)
                    .setLargeIcon(resource)
                    .setStyle(
                        messagingStyle
                    )
                    .setAutoCancel(true)
                    .setGroup(GROUP_KEY_WORK_EMAIL)
                    .setGroupSummary(true)
                    .setContentIntent(pendingIntent)

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            val channel = NotificationChannel(channelId, userName, NotificationManager.IMPORTANCE_HIGH)
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
                        log(mTag, "channelId: $channelId, title: $userName")
                    } else {
                        notificationBuilder.setSound(soundUri)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                notificationManager.notify(1000, notificationBuilder.build())
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                log(mTag, "onLoadCleared: $placeholder")
            }

        })

    }

}
