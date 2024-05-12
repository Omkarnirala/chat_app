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
import com.omkar.chatapp.R
import com.omkar.chatapp.databinding.FragmentMessageBinding
import com.omkar.chatapp.ui.signin.signup.UserDetailsModel
import com.omkar.chatapp.utils.BaseFragment
import com.omkar.chatapp.utils.FirebaseEvent
import com.omkar.chatapp.utils.FirebaseUtil
import com.omkar.chatapp.utils.USER_EMAIL
import com.omkar.chatapp.utils.getStringData
import com.omkar.chatapp.utils.hideKeyboard
import com.omkar.chatapp.utils.log
import com.omkar.chatapp.utils.showInternetError
import com.omkar.chatapp.utils.showKeyboard
import com.omkar.chatapp.utils.toasty
import com.vanniktech.emoji.EmojiPopup

class MessageFragment : BaseFragment() {

    private var chatroomId: String = ""
    private val mTag = "MessageFragment"

    private var _binding: FragmentMessageBinding? = null
    private val b get() = _binding!!

    private var isInternetAvailable = false
    private var snackbar: Snackbar? = null
    private lateinit var messagesAdapter: MessagesAdapter
    private var receiverData: UserDetailsModel? = null
    private val auth = FirebaseAuth.getInstance()
    private var emojiPopup: EmojiPopup? = null
    private var chatroomModel: ChatRoomModel? = null

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
                b.rvMessage.smoothScrollToPosition(0)
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


        }
    }

    private fun getViewModelData() {
        cxt?.let { context ->

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
                val emoji = b.emojiEditText.text.toString().trim()
                if (b.etMessage.text.toString().trim().isEmpty() && b.emojiEditText.text.toString().trim().isEmpty()) {
                    return@setOnClickListener
                }

                sendMessageToUser(msg)

            }

            b.ivEmoji.setOnClickListener {
                b.etMessage.visibility = View.INVISIBLE
                b.emojiEditText.visibility = View.VISIBLE
                b.ivKeyboard.visibility = View.VISIBLE
                b.ivEmoji.visibility = View.INVISIBLE
                emojiPopup = EmojiPopup(it, b.emojiEditText)
                emojiPopup?.toggle() // Toggles visibility of the Popup.
            }

            b.ivKeyboard.setOnClickListener {
                b.etMessage.visibility = View.VISIBLE
                b.emojiEditText.visibility = View.INVISIBLE
                b.ivKeyboard.visibility = View.INVISIBLE
                b.ivEmoji.visibility = View.VISIBLE
                if (emojiPopup?.isShowing == true) {
                    emojiPopup?.dismiss()
                }
                showKeyboard(requireActivity())
            }
        }
    }

    private fun sendMessageToUser(message: String) {
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
                    b.emojiEditText.text?.clear()
                    sendNotification(message)
                }
            }
    }

    private fun sendNotification(message: String) {
        toasty(requireContext(), "Sending Notification: $message")
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}