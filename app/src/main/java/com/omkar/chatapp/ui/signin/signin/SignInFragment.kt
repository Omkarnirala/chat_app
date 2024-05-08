package com.omkar.chatapp.ui.signin.signin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.omkar.chatapp.LauncherViewModel
import com.omkar.chatapp.R
import com.omkar.chatapp.databinding.FragmentSigninBinding
import com.omkar.chatapp.utils.BaseFragment
import com.omkar.chatapp.utils.FirebaseEvent
import com.omkar.chatapp.utils.IS_LOGIN
import com.omkar.chatapp.utils.USER_EMAIL
import com.omkar.chatapp.utils.USER_ID
import com.omkar.chatapp.utils.hideKeyboard
import com.omkar.chatapp.utils.hideMaterialProgressBar
import com.omkar.chatapp.utils.isValidEmailAddress
import com.omkar.chatapp.utils.loginMethod
import com.omkar.chatapp.utils.progress.attachTextChangeAnimator
import com.omkar.chatapp.utils.progress.bindProgressButton
import com.omkar.chatapp.utils.setBooleanData
import com.omkar.chatapp.utils.setStringData
import com.omkar.chatapp.utils.showCustomAlertDialog
import com.omkar.chatapp.utils.showError
import com.omkar.chatapp.utils.showInternetError
import com.omkar.chatapp.utils.showMaterialProgressBar
import com.omkar.chatapp.utils.timberLog
import com.omkar.chatapp.utils.toasty

class SignInFragment : BaseFragment() {

    private val mTag = "SignInFragment"

    private var _binding: FragmentSigninBinding? = null
    private val b get() = _binding!!
    private lateinit var authViewModel: AuthViewModel
    private val launcherViewModel: LauncherViewModel by activityViewModels()
    private var isInternetAvailable = false
    private var snackbar: Snackbar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSigninBinding.inflate(inflater, container, false)
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
            val repository = AuthRepository()
            val viewModelFactory = AuthViewModelFactory(repository)
            authViewModel = ViewModelProvider(this, viewModelFactory)[AuthViewModel::class.java]

            snackbar = showInternetError(context, b.rootLayout)

            b.buttonLogin.apply {
                isEnabled = false
                bindProgressButton(this)
                attachTextChangeAnimator()
            }

            firebaseAnalytics?.logEvent(FirebaseEvent.LOGIN) {
                param(FirebaseAnalytics.Param.SCREEN_NAME, mTag)
            }
        }

    }

    private fun getViewModelData() {
        cxt?.let { context ->

            launcherViewModel.getInternetStatus().observe(viewLifecycleOwner) { status ->
                status?.let { isInternetAvailable ->

                    timberLog(mTag, "isInternetAvailable = $isInternetAvailable")

                    this.isInternetAvailable = isInternetAvailable

                    snackbar?.let {
                        if (isInternetAvailable) {
                            timberLog(mTag, "snackbar dismiss")
                            it.dismiss()
                        } else {
                            timberLog(mTag, "snackbar show")
                            it.show()
                        }
                    }
                }
            }

            authViewModel.getValidation().observe(viewLifecycleOwner) {
                it?.let { validationStatus ->
                    b.buttonLogin.isEnabled = validationStatus
                }
            }

            authViewModel.signInResult.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is SignInResult.Success -> {
                        // Handle successful sign-in
                        timberLog(mTag, "getViewModelData: ${result.authResult}")
                        b.buttonLogin.hideMaterialProgressBar(context.getString(R.string.login))
                        result.authResult?.let {
                            setBooleanData(context, IS_LOGIN, true)
                            setStringData(context, USER_ID, it.user?.uid)
                            setStringData(context, USER_EMAIL, it.user?.email)
                            loginMethod(cxt)
                        }
                        toasty(requireContext(), "Sign-in Successfully")
                    }

                    is SignInResult.Failure -> {
                        // Handle sign-in failure
                        b.buttonLogin.hideMaterialProgressBar(context.getString(R.string.login))
                        toasty(requireContext(), "${result.exception?.message}")
                    }
                }
            }
        }
    }

    private fun viewListener() {
        cxt?.let { context ->

            b.tvMerchantForgotPassword.setOnClickListener {
                authViewModel.setEmailAddress(b.tietMerchantEmail.text?.toString())
                findNavController().navigate(SignInFragmentDirections.actionSignInFragmentToForgetPasswordFragment())
            }

            b.tvSignUp.setOnClickListener {
                findNavController().navigate(SignInFragmentDirections.actionSignInFragmentToSignupFragment())
            }

            b.tietMerchantPassword.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) b.buttonLogin.performClick()
                true
            }

            b.tietMerchantEmail.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    s?.let { charSequence ->

                        authViewModel.setValidation(
                            b.tietMerchantEmail.text?.toString(),
                            b.tietMerchantPassword.text?.toString()
                        )

                        if (charSequence.toString().isEmpty()) {
                            b.tilMerchantEmail.showError(context, R.string.enter_first_name)
                        } else {
                            if (isValidEmailAddress(charSequence.toString())) {
                                b.tilMerchantEmail.error = null
                            } else {
                                b.tilMerchantEmail.showError(
                                    context,
                                    R.string.enter_email_address_valid
                                )
                            }
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }
            })

            b.tietMerchantPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    s?.let { charSequence ->
                        authViewModel.setValidation(
                            b.tietMerchantEmail.text?.toString(),
                            b.tietMerchantPassword.text?.toString()
                        )

                        if (charSequence.toString().isEmpty()) {
                            b.tilMerchantPassword.showError(context, R.string.enter_password)
                        } else {
                            b.tilMerchantPassword.error = null
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                }

            })

            b.buttonLogin.setOnClickListener {
                if (isInternetAvailable) {
                    hideKeyboard(requireActivity())
                    b.buttonLogin.showMaterialProgressBar(R.string.please_wait)
                    authViewModel.signIn(
                        b.tietMerchantEmail.text.toString(),
                        b.tietMerchantPassword.text.toString()
                    )
                } else{
                    showCustomAlertDialog(context, R.string.not_connected_to_internet, R.drawable.ic_server_connection1) {}
                }

            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}


