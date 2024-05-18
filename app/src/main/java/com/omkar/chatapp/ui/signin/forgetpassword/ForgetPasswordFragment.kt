package com.omkar.chatapp.ui.signin.forgetpassword

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.omkar.chatapp.ui.signin.signin.AuthRepository
import com.omkar.chatapp.ui.signin.signin.AuthViewModel
import com.omkar.chatapp.ui.signin.signin.AuthViewModelFactory
import com.omkar.chatapp.ui.signin.signin.ResetPasswordResult
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.omkar.chatapp.LauncherViewModel
import com.omkar.chatapp.R
import com.omkar.chatapp.databinding.FragmentForgetPasswordBinding
import com.omkar.chatapp.utils.BaseFragment
import com.omkar.chatapp.utils.FirebaseEvent
import com.omkar.chatapp.utils.USER_EMAIL
import com.omkar.chatapp.utils.USER_ID
import com.omkar.chatapp.utils.getStringData
import com.omkar.chatapp.utils.hideMaterialProgressBar
import com.omkar.chatapp.utils.isValidEmailAddress
import com.omkar.chatapp.utils.progress.attachTextChangeAnimator
import com.omkar.chatapp.utils.progress.bindProgressButton
import com.omkar.chatapp.utils.showCustomAlertDialog
import com.omkar.chatapp.utils.showError
import com.omkar.chatapp.utils.showInternetError
import com.omkar.chatapp.utils.showMaterialDefaultProgressBar
import com.omkar.chatapp.utils.timberLog

class ForgetPasswordFragment : BaseFragment() {

    private val mTag = "ForgetPasswordFragment"

    private var isInternetAvailable = false
    private var _binding: FragmentForgetPasswordBinding? = null
    private val b get() = _binding!!

    private var snackbar: Snackbar? = null
    private lateinit var authViewModel: AuthViewModel
    private val launcherViewModel: LauncherViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentForgetPasswordBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        getViewModelData()
        viewListener()
    }

    private fun initView() {

        val repository = AuthRepository()
        val viewModelFactory = AuthViewModelFactory(repository)
        authViewModel = ViewModelProvider(this, viewModelFactory)[AuthViewModel::class.java]

        snackbar = showInternetError(cxt, b.root)

        b.buttonReset.apply {
            isEnabled = false
            bindProgressButton(this)
            attachTextChangeAnimator()
        }

        firebaseAnalytics?.logEvent(FirebaseEvent.FORGET_PASSWORD) {
            param(USER_EMAIL, getStringData(cxt, USER_ID, ""))
            param(FirebaseAnalytics.Param.SCREEN_NAME, mTag)
        }
    }

    private fun getViewModelData() {
        cxt?.let {context ->
            launcherViewModel.getInternetStatus().observe(viewLifecycleOwner) { status ->
                status?.let { isInternetAvailable ->

                    timberLog(mTag, "isInternetAvailable = $isInternetAvailable")

                    this.isInternetAvailable = isInternetAvailable

                    snackbar?.let {
                        if (isInternetAvailable) {
                            timberLog(mTag, getString(R.string.snackbar_dismiss))
                            it.dismiss()
                        } else {
                            timberLog(mTag, getString(R.string.snackbar_show))
                            it.show()
                        }
                    }
                }
            }

            authViewModel.getEmailAddress().observe(viewLifecycleOwner) {
                it?.let {
                    if (it.isNotEmpty()) {
                        b.tietEmail.setText(it)
                    }
                }
            }

            authViewModel.resetPasswordResult.observe(viewLifecycleOwner) { result ->
                b.buttonReset.hideMaterialProgressBar(context, R.string.reset)
                when (result) {
                    is ResetPasswordResult.Success -> {
                        // Handle successful password reset
                        timberLog(mTag, "$result")
                        showCustomAlertDialog(context, R.string.reset_link_on_regestered_email, R.drawable.ic_success) {
                            try {
                                Navigation.findNavController(b.root).navigateUp()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    is ResetPasswordResult.Failure -> {
                        // Handle password reset failure
                        showCustomAlertDialog(context, R.string.failed_to_send_reset_link, R.drawable.ic_failed) {
                            try {
                                Navigation.findNavController(b.root).navigateUp()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun viewListener() {
        cxt?.let {context ->
            b.ivBack.setOnClickListener {
                try {
                    Navigation.findNavController(b.root).navigateUp()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            b.toolbarTitle.setText(R.string.forget_password)

            b.tietEmail.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    s?.let { charSequence ->

                        if (charSequence.toString().isEmpty()) {
                            b.tilEmail.showError(context, R.string.enter_email_address)
                            b.buttonReset.isEnabled = false
                        } else {
                            if (isValidEmailAddress(charSequence.toString())) {
                                b.tilEmail.error = null
                                b.buttonReset.isEnabled = true
                            } else {
                                b.tilEmail.showError(context, R.string.enter_email_address_valid)
                                b.buttonReset.isEnabled = false
                            }
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }
            })

            b.buttonReset.setOnClickListener {

                if (isInternetAvailable) {
                    //call web service
                    b.buttonReset.showMaterialDefaultProgressBar()
                    authViewModel.resetPassword(b.tietEmail.text.toString())

                } else {
                    //showDialog(context, R.string.not_connected_to_internet)
                    showCustomAlertDialog(cxt, R.string.not_connected_to_internet, R.drawable.ic_server_connection1) {

                    }
                }

            }

        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}