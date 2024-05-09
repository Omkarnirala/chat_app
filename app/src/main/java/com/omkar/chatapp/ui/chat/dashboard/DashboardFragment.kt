package com.omkar.chatapp.ui.chat.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.omkar.chatapp.R
import com.omkar.chatapp.databinding.FragmentDashboardBinding
import com.omkar.chatapp.ui.signin.signup.UserFirestore
import com.omkar.chatapp.utils.BaseFragment
import com.omkar.chatapp.utils.FirebaseEvent
import com.omkar.chatapp.utils.USER_EMAIL
import com.omkar.chatapp.utils.getStringData
import com.omkar.chatapp.utils.log
import com.omkar.chatapp.utils.showInternetError
import com.omkar.chatapp.utils.toasty

class DashboardFragment : BaseFragment() , UsersAdapter.UserCallback {

    private val mTag = "DashboardFragment"

    private var _binding: FragmentDashboardBinding? = null
    private val b get() = _binding!!
    private lateinit var dashboardViewModel: DashboardViewModel
    private var usersAdapter: UsersAdapter? = null
    private var snackbar: Snackbar? = null

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
        cxt?.let {context ->
            snackbar = showInternetError(context, b.rootLayout)

            firebaseAnalytics?.logEvent(FirebaseEvent.DASHBOARD_HOME) {
                param(USER_EMAIL, getStringData(context, USER_EMAIL, ""))
                param(FirebaseAnalytics.Param.SCREEN_NAME, mTag)
            }

            dashboardViewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
            usersAdapter = UsersAdapter(arrayListOf(), this)
            b.rvUserList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = usersAdapter
            }

            dashboardViewModel.getCurrentUser().observe(viewLifecycleOwner){userData ->
                log(mTag, "Image URL: ${userData.profileImageUrl}")
                Glide.with(b.toolbar.profileImage.context).load(userData.profileImageUrl)
                    .into(b.toolbar.profileImage)
            }

            // Set user as online when the activity is created
            dashboardViewModel.setUserOnlineStatus(true)
        }
    }

    override fun onStop() {
        super.onStop()

        // Set user as offline when the activity is created
        dashboardViewModel.setUserOnlineStatus(false)
    }

    override fun onResume() {
        super.onResume()

        // Set user as online when the activity is created
        dashboardViewModel.setUserOnlineStatus(true)
    }

    private fun getViewModelData() {
        cxt?.let {context ->
            dashboardViewModel.allUsers.observe(viewLifecycleOwner) {userList ->
                usersAdapter?.updateList(userList)
            }

        }
    }

    private fun viewListener() {
        cxt?.let {
            b.toolbar.profileImage.setOnClickListener {
                findNavController().navigate(R.id.action_dashboardFragment_to_profileFragment)
            }
        }
    }

    override fun onUserClickedCallBack(position: Int, user: UserFirestore) {
        toasty(requireContext(), "User Clicked: ${user.email}")
    }


}