package com.omkar.chatapp.ui.chat.dashboard

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
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.omkar.chatapp.R
import com.omkar.chatapp.databinding.FragmentDashboardBinding
import com.omkar.chatapp.ui.signin.signup.UserDetailsModel
import com.omkar.chatapp.utils.BaseFragment
import com.omkar.chatapp.utils.FirebaseEvent
import com.omkar.chatapp.utils.FirebaseUtil
import com.omkar.chatapp.utils.USER_EMAIL
import com.omkar.chatapp.utils.USER_NAME
import com.omkar.chatapp.utils.getStringData
import com.omkar.chatapp.utils.log
import com.omkar.chatapp.utils.setStringData
import com.omkar.chatapp.utils.showInternetError

class DashboardFragment : BaseFragment(), UsersAdapter.UserCallback {

    private val mTag = "DashboardFragment"

    private var _binding: FragmentDashboardBinding? = null
    private val b get() = _binding!!
    private var usersAdapter: UsersAdapter? = null
    private var snackbar: Snackbar? = null
    private val auth = FirebaseAuth.getInstance()

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
        getViewModelData()
        viewListener()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }
    }

    private fun initView() {
        cxt?.let { context ->
            snackbar = showInternetError(context, b.rootLayout)

            firebaseAnalytics?.logEvent(FirebaseEvent.DASHBOARD_HOME) {
                param(USER_EMAIL, getStringData(context, USER_EMAIL, ""))
                param(FirebaseAnalytics.Param.SCREEN_NAME, mTag)
            }

            FirebaseUtil.getCurrentProfilePicStorageRef().downloadUrl.addOnCompleteListener { uri ->
                if (uri.isSuccessful) {
                    log(mTag, "initView: ${uri.result}")
                    Glide.with(b.toolbar.profileImage.context).load(uri.result).apply(RequestOptions().circleCrop()).into(b.toolbar.profileImage)
                }
            }

            FirebaseUtil.currentUserName(auth.uid).let {
                setStringData(context, USER_NAME, it)
            }

        }
    }

    override fun onStop() {
        super.onStop()
        usersAdapter?.stopListening()
    }


    private fun getViewModelData() {
        cxt?.let { context ->

            val query = FirebaseUtil.getAllUserDetails().whereNotEqualTo("uid", auth.currentUser?.uid)
            val option = FirestoreRecyclerOptions.Builder<UserDetailsModel>()
                .setQuery(query, UserDetailsModel::class.java)
                .setLifecycleOwner(this)
                .build()
            log(mTag, "setupMessagingRecyclerView: ${option.snapshots.filter { it.uid != auth.currentUser?.uid }}")

            usersAdapter = UsersAdapter(option, this)
            b.rvUserList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = usersAdapter
            }

            usersAdapter?.startListening()
            usersAdapter?.registerAdapterDataObserver(object : AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    b.rvUserList.scrollToPosition(0)
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
        }
    }

    override fun onUserClickedCallBack(position: Int, user: UserDetailsModel?) {
        val bundle = Bundle().apply {
            putParcelable("user", user)
        }
        findNavController().navigate(R.id.action_dashboardFragment_to_messageFragment, bundle)
    }

}