package com.omkar.chatapp.ui.chat.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.search.SearchBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import com.omkar.chatapp.R
import com.omkar.chatapp.databinding.FragmentDashboardBinding
import com.omkar.chatapp.ui.chat.dashboard.alluser.UsersAdapter
import com.omkar.chatapp.ui.chat.dashboard.user.ChatAdapter
import com.omkar.chatapp.ui.chat.messaging.ChatRoomModel
import com.omkar.chatapp.ui.signin.signup.UserDetailsModel
import com.omkar.chatapp.utils.BaseFragment
import com.omkar.chatapp.utils.FIREBASE_MESSAGING_TOKEN
import com.omkar.chatapp.utils.FirebaseEvent
import com.omkar.chatapp.utils.FirebaseUtil
import com.omkar.chatapp.utils.FirebaseUtil.notificationBuilders
import com.omkar.chatapp.utils.USER_EMAIL
import com.omkar.chatapp.utils.USER_NAME
import com.omkar.chatapp.utils.getStringData
import com.omkar.chatapp.utils.log
import com.omkar.chatapp.utils.requestNotificationPermission
import com.omkar.chatapp.utils.setStringData
import com.omkar.chatapp.utils.showInternetError

class DashboardFragment : BaseFragment(), UsersAdapter.UserCallback, ChatAdapter.UserCallback {

    private val mTag = "DashboardFragment"

    private var _binding: FragmentDashboardBinding? = null
    private val b get() = _binding!!
    private var usersAdapter: UsersAdapter? = null
    private var chatAdapter: ChatAdapter? = null
    private var snackbar: Snackbar? = null
    private val auth = FirebaseAuth.getInstance()
    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setUpChatViewData()
        viewListener()
        getNotificationPermission()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }
    }

    private fun getNotificationPermission() {

        notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // You can check the result here to see if the user changed the notification settings.
            // However, there's no direct result data regarding the change in notification settings.
        }

        // Call this where appropriate to request notification permissions.
        requestNotificationPermission(requireActivity(), notificationPermissionLauncher)
    }

    private fun initView() {
        cxt?.let { context ->
            snackbar = showInternetError(context, b.rootLayout)

            firebaseAnalytics?.logEvent(FirebaseEvent.DASHBOARD_HOME) {
                param(USER_EMAIL, getStringData(context, USER_EMAIL, ""))
                param(FirebaseAnalytics.Param.SCREEN_NAME, mTag)
            }

            notificationBuilders.clear()
            log(mTag, "FirebaseUtil.notificationBuilders.size: ${notificationBuilders.size}")

            FirebaseUtil.getCurrentProfilePicStorageRef().downloadUrl.addOnCompleteListener { uri ->
                if (uri.isSuccessful) {
                    log(mTag, "initView: ${uri.result}")
                    Glide.with(b.toolbar.profileImage.context).load(uri.result ?: R.drawable.ic_profile).apply(RequestOptions().circleCrop())
                        .into(b.toolbar.profileImage)
                }
            }

            FirebaseUtil.currentUserName(auth.uid).let {
                setStringData(context, USER_NAME, it)
            }

            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    setStringData(context, FIREBASE_MESSAGING_TOKEN, task.result)
                    log(mTag, "FirebaseMessaging Token: ${task}")
                    log(mTag, "FirebaseMessaging Token Result: ${task.result}")
                }
            }

            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    FirebaseUtil.updateUserToken(auth.currentUser?.uid, task.result)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        usersAdapter?.stopListening()
    }

    private fun setUpChatViewData() {
        cxt?.let { context ->

            b.buttonCurrentChat.setTextColor(ContextCompat.getColor(requireContext(), R.color.bg_home))
            b.buttonAllUsers.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            b.viewCurrentChat.visibility = View.VISIBLE
            b.viewDivideAllUsers.visibility = View.INVISIBLE

            val query = FirebaseUtil.allChatroomCollectionReference()
                .whereArrayContains("userIds", auth.currentUser?.uid.toString())
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)

            val option = FirestoreRecyclerOptions.Builder<ChatRoomModel>()
                .setQuery(query, ChatRoomModel::class.java)
                .setLifecycleOwner(this)
                .build()

            log(mTag, "setUpChatViewData: ${option.snapshots}}")

            chatAdapter = ChatAdapter(option, this)
            b.rvUserList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = chatAdapter
            }

            chatAdapter?.startListening()
            chatAdapter?.registerAdapterDataObserver(object : AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    chatAdapter?.notifyDataSetChanged()
                    log(mTag, "onItemRangeInserted: $itemCount")
                }
            })
        }
    }

    private fun viewListener() {
        cxt?.let {

            b.toolbar.profileImage.setOnClickListener {
                findNavController().navigate(R.id.action_dashboardFragment_to_profileFragment)
            }

            b.buttonCurrentChat.setOnClickListener {
                setUpChatViewData()
            }

            b.buttonAllUsers.setOnClickListener {
                setUpAllUserViewData()
            }

//            b.searchUser.

        }
    }

    private fun setUpAllUserViewData() {
        b.buttonCurrentChat.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        b.buttonAllUsers.setTextColor(ContextCompat.getColor(requireContext(), R.color.bg_home))
        b.viewCurrentChat.visibility = View.INVISIBLE
        b.viewDivideAllUsers.visibility = View.VISIBLE

        val query = FirebaseUtil.getAllUserDetails().whereNotEqualTo("uid", auth.currentUser?.uid)
        val option = FirestoreRecyclerOptions.Builder<UserDetailsModel>()
            .setQuery(query, UserDetailsModel::class.java)
            .setLifecycleOwner(this)
            .build()
        log(mTag, "setUpAllUserViewData: ${option.snapshots.filter { it.uid != auth.currentUser?.uid }}")

        usersAdapter = UsersAdapter(option, this)
        b.rvUserList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = usersAdapter
        }

        usersAdapter?.startListening()
        usersAdapter?.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                usersAdapter?.notifyDataSetChanged()
                log(mTag, "onItemRangeInserted: $itemCount")
            }
        })
    }

    override fun onUserClickedCallBack(position: Int, user: UserDetailsModel?) {
        val bundle = Bundle().apply {
            putParcelable("user", user)
        }
        findNavController().navigate(R.id.action_dashboardFragment_to_messageFragment, bundle)
    }
}