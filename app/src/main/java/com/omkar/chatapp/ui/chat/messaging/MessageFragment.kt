package com.omkar.chatapp.ui.chat.messaging

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.gson.Gson
import com.omkar.chatapp.R
import com.omkar.chatapp.databinding.FragmentMessageBinding
import com.omkar.chatapp.ui.signin.signup.UserDetailsModel
import com.omkar.chatapp.utils.BaseFragment
import com.omkar.chatapp.utils.FirebaseEvent
import com.omkar.chatapp.utils.FirebaseUtil
import com.omkar.chatapp.utils.USER_EMAIL
import com.omkar.chatapp.utils.getStringData
import com.omkar.chatapp.utils.log
import com.omkar.chatapp.utils.showInternetError
import com.omkar.chatapp.utils.toasty
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MessageFragment : BaseFragment() {

    private var chatroomId: String = ""
    private val mTag = "MessageFragment"

    private var _binding: FragmentMessageBinding? = null
    private val b get() = _binding!!

    private var isInternetAvailable = false
    private var snackbar: Snackbar? = null
    private lateinit var messagesAdapter: MessagesAdapter
    private var receiverData: UserDetailsModel? = null
    private var currentData: UserDetailsModel? = null
    private val auth = FirebaseAuth.getInstance()
//    private var emojiPopup: EmojiPopup? = null
    private var chatroomModel: ChatRoomModel? = null
    private val messages = mutableListOf<Message>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        getOrCreateMessagingRoomModel()
        setupMessagingRecyclerView()
        getViewModelData()
        viewListener()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }
    }

    private fun getOrCreateMessagingRoomModel() {
        FirebaseUtil.getChatroomReference(chatroomId = chatroomId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    log(mTag, "getOrCreateMessagingRoomModel: ${task.result}")
                    chatroomModel = task.result.toObject(ChatRoomModel::class.java)
                    if (chatroomModel == null) {
                        chatroomModel = ChatRoomModel(
                            chatroomId = chatroomId,
                            userIds = listOf(FirebaseUtil.currentUserId(), receiverData?.uid),
                            lastMessageTimestamp = Timestamp.now(),
                            lastMessageSenderId = "",
                            lastMessage = "",

                            )
                        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel!!)
                    }
                } else {
                    log(mTag, "getOrCreateMessagingRoomModel: ${task.exception}")
                }
            }
    }

    private fun setupMessagingRecyclerView() {
        val query = FirebaseUtil.getChatroomMessageReference(chatroomId).orderBy("timeStamps", Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<ChatMessageModel>().setQuery(query, ChatMessageModel::class.java).build()

        log(mTag, "setupMessagingRecyclerView: ${options.snapshots}")
        messagesAdapter = MessagesAdapter(options)

        val manager = LinearLayoutManager(requireContext())
        manager.reverseLayout = true
        b.rvMessage.apply {
            layoutManager = manager
            adapter = messagesAdapter
            smoothScrollToPosition(0)
        }

        messagesAdapter.startListening()
        messagesAdapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                log(mTag, "onItemRangeInserted: $itemCount")
                try {
                    b.rvMessage.smoothScrollToPosition(0)
                } catch (e: Exception) {
                    log(mTag, "Exception: $e")
                }
            }

        })
    }

    private fun initView() {
        cxt?.let { context ->
            snackbar = showInternetError(context, b.rootLayout)

            firebaseAnalytics?.logEvent(FirebaseEvent.MESSAGING_FRAGMENT) {
                param(USER_EMAIL, getStringData(context, USER_EMAIL, ""))
                param(FirebaseAnalytics.Param.SCREEN_NAME, mTag)
            }

            // Retrieve the user data from arguments Bundle
            receiverData = arguments?.getParcelable("user")
            log(mTag, "Receiver Data: $receiverData")
            chatroomId = FirebaseUtil.getChatroomId(FirebaseUtil.currentUserId()!!, receiverData?.uid.toString())

            FirebaseUtil.currentUserDetails()
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val document = it.result
                        if (document != null) {
                            currentData = it.result.toObject()
                            log(mTag, "currentData: $currentData")
                        }
                    }
                }
                .addOnFailureListener {exception ->
                    log(mTag, "currentUserName Error getting document: $exception")
                }

        }
    }

    private fun getViewModelData() {
        cxt?.let { context ->
            setAudioCall(receiverData?.uid.toString())
            setVideoCall(receiverData?.uid.toString())
        }
    }

    private fun viewListener() {
        cxt?.let {

            b.ivBack.setOnClickListener {
                findNavController().navigateUp()
            }

            FirebaseUtil.getOtherProfilePicStorageRef(receiverData?.email.toString()).downloadUrl.addOnCompleteListener { uri ->
                if (uri.isSuccessful) {
                    Glide.with(b.ivUserImage.context).load(uri.result).apply(RequestOptions().circleCrop()).into(b.ivUserImage)
                }
            }

            FirebaseUtil.getOtherUserOnlineStatus(receiverData?.uid)
                .addSnapshotListener { value, error ->
                    try {
                        if (value != null) {
                            val isOnline = value.getBoolean("isOnline")
                            if (isOnline == true) {
                                b.statusIndicator.setImageResource(R.drawable.online_status)
                            } else {
                                b.statusIndicator.setImageResource(R.drawable.offline_status)
                            }
                        } else {
                            log(mTag, "viewListener: $error")
                        }
                    } catch (e: Exception) {
                        log(mTag, "viewListener: $e")
                    }

                }

            b.userName.text = receiverData?.displayName

            b.ivSend.setOnClickListener {

                val msg = b.etMessage.text.toString().trim()
//                val emoji = b.emojiEditText.text.toString().trim()
                if (b.etMessage.text.toString().trim().isEmpty() /*&& b.emojiEditText.text.toString().trim().isEmpty()*/) {
                    return@setOnClickListener
                }

                sendMessageToUser(msg, receiverData?.token, receiverData?.isOnline)

            }
        }
    }

    private fun sendMessageToUser(message: String, token: String?, online: Boolean?) {
        val chatRoomModel = ChatRoomModel(
            chatroomId = chatroomId,
            userIds = listOf(FirebaseUtil.currentUserId(), receiverData?.uid),
            lastMessageTimestamp = Timestamp.now(),
            lastMessageSenderId = auth.currentUser?.uid.toString(),
            lastMessage = message
        )

        FirebaseUtil.getChatroomReference(chatroomId).set(chatRoomModel)

        val chatMessageModel = ChatMessageModel(
            message = message,
            senderID = auth.currentUser?.uid.toString(),
            timeStamps = Timestamp.now()
        )

        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    b.etMessage.text?.clear()
                    FirebaseUtil.getOtherUserOnlineStatus(receiverData?.uid)
                        .get()
                        .addOnCompleteListener { value ->
                            try {
                                val isOnline = value.result.getBoolean("isOnline")
                                if (isOnline != true) {
                                    messages.add(Message(message, System.currentTimeMillis(), currentData?.displayName.toString()))
                                    sendNotification(messages, token, currentData?.displayName)
                                } else {
                                    messages.clear()
                                }
                            } catch (e: Exception) {
                                log(mTag, "viewListener: $e")
                            }
                        }
                }
            }
    }


    private fun setAudioCall(targetUserId: String) {

        val newVoiceCall = b.ivAudioCall
        newVoiceCall.setIsVideoCall(false)

        newVoiceCall.resourceID = "zego_data"

        val split = targetUserId.split(",")
        val users = ArrayList<ZegoUIKitUser>()
        for (userID in split) {
            println("userID=$userID")
            val userName = receiverData?.displayName
            users.add(ZegoUIKitUser(userID, userName))
        }
        newVoiceCall.setInvitees(users)
    }

    private fun setVideoCall(targetUserId: String) {

        val newVideoCall = b.ivVideoCall
        newVideoCall.setIsVideoCall(true)

        //for notification sound
        newVideoCall.resourceID = "zego_data"

        newVideoCall.setOnClickListener { view ->
            val split = targetUserId.split(",")
            val users = ArrayList<ZegoUIKitUser>()
            for (userID in split) {
                println("userID=$userID")
                val userName = receiverData?.displayName
                users.add(ZegoUIKitUser(userID, userName))
            }
            newVideoCall.setInvitees(users)
        }
    }

    private fun sendNotification(fcmMessage: MutableList<Message>, fcmToken: String?, userName: String?) {
        toasty(requireContext(), "Sending Notification: $fcmMessage")
        CoroutineScope(Dispatchers.IO).launch {
            // Executes the network request on a background thread
            try {
                val url = URL("https://fcm.googleapis.com/fcm/send")
                val conn = url.openConnection() as HttpsURLConnection
                // Create the JSON payload
                val gson = Gson()
                val json = gson.toJson(currentData)
                val jsonMessages = gson.toJson(fcmMessage)
                log(mTag, "fcmMessage: $jsonMessages")
                log(mTag, json)
                conn.apply {
                    readTimeout = 10000
                    connectTimeout = 15000
                    requestMethod = "POST"
                    doInput = true
                    doOutput = true

                    // Set headers
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty(
                        "Authorization",
                        "key=AAAANOcCEhE:APA91bFgDmHMfrToJ-cdxuyzbC6JS7FRpBghMqzyfMYmadJHNuBDMzOmi78UlCS5tSOQgIZtEsJzPVGZht3qSP8419AuMDYZVsAGyqoPTqVXLhSsS2B3ULCOM2coQvKTjRwuwgFJmiqA"
                    ) // Replace YOUR_SERVER_KEY with your actual FCM server key

                    val jsonPayload = """
                {
                    "to": "$fcmToken",
                    "data": {
                        "body": $jsonMessages,
                        "title": "$userName",
                        "priority": "high",
                        "receiverData":${json}
                    }
                }
                """.trimIndent()
                    log(mTag, "jsonPayload: $jsonPayload")
                    // Send FCM message content.
                    outputStream.use { os ->
                        os.write(jsonPayload.toByteArray(charset("UTF-8")))
                    }

                    // Read FCM server response.
                    val responseCode = responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val response = inputStream.bufferedReader().use { it.readText() }
                        log(mTag, "Response: $response")
                        println("Response: $response")
                    } else {
                        log(mTag, "FCM request failed with response code $responseCode")
//                        println("FCM request failed with response code $responseCode")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}