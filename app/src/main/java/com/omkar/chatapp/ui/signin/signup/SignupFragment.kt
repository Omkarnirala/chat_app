package com.omkar.chatapp.ui.signin.signup

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.messaging.FirebaseMessaging
import com.omkar.chatapp.LauncherViewModel
import com.omkar.chatapp.R
import com.omkar.chatapp.databinding.FragmentSignupBinding
import com.omkar.chatapp.ui.signin.signin.AuthRepository
import com.omkar.chatapp.ui.signin.signin.AuthViewModel
import com.omkar.chatapp.ui.signin.signin.AuthViewModelFactory
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

class SignupFragment : BaseFragment() {

    private val mTag = "SignupFragment"

    private var _binding: FragmentSignupBinding? = null
    private val b get() = _binding!!

    private lateinit var authViewModel: AuthViewModel
    private val launcherViewModel: LauncherViewModel by activityViewModels()
    private var isInternetAvailable = false
    private var snackbar: Snackbar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        getViewModelData()
        viewListener()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }
    }

    private fun initView() {

        cxt?.let { context ->
            val repository = AuthRepository()
            val viewModelFactory = AuthViewModelFactory(repository)
            authViewModel = ViewModelProvider(this, viewModelFactory)[AuthViewModel::class.java]

            snackbar = showInternetError(context, b.rootLayout)

            b.signUpButton.apply {
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

            authViewModel.signUpResult.observe(viewLifecycleOwner) { result ->
                b.signUpButton.hideMaterialProgressBar(context.getString(R.string.sign_up))
                when (result) {
                    is SignUpResult.Success -> {
                        val user = result.user
                        if (user != null) {
                            // Handle successful sign-up, and use user if needed
                            timberLog(mTag, "getViewModelData: ${result.user}")

                            result.user.let {
                                val user = UserDetailsModel(
                                    uid = it.uid,
                                    email = it.email,
                                    displayName = it.displayName,
                                    profileImageUrl = it.photoUrl?.toString(),
                                    isOnline = true,
                                    token = FirebaseMessaging.getInstance().token.result
                                )
                                authViewModel.addUserToFirestore(user)
                                setBooleanData(context, IS_LOGIN, true)
                                setStringData(context, USER_ID, user.uid)
                                setStringData(context, USER_EMAIL, user.email)
                                loginMethod(cxt)
                            }
                        } else {
                            // Handle failure case where user is null
                            toasty(requireContext(), "Unable to Signup")
                        }
                    }

                    is SignUpResult.Failure -> {
                        // Handle sign-up failure
                        val exception = result.exception
                        Toast.makeText(
                            context,
                            "Sign-up failed: ${exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> {}
                }
            }

        }

    }

    private fun viewListener() {
        cxt?.let { context ->
            b.ivBack.setOnClickListener {
                findNavController().navigateUp()
            }

            b.passwordEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) b.signUpButton.performClick()
                true
            }

            b.emailEditText.addTextChangedListener(object : TextWatcher {
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
                            b.emailEditText.text?.toString(),
                            b.passwordEditText.text?.toString()
                        )

                        if (charSequence.toString().isEmpty()) {
                            b.emailInputLayout.showError(context, R.string.enter_first_name)
                        } else {
                            if (isValidEmailAddress(charSequence.toString())) {
                                b.emailInputLayout.error = null
                            } else {
                                b.emailInputLayout.showError(
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

            b.passwordEditText.addTextChangedListener(object : TextWatcher {
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
                            b.passwordEditText.text?.toString(),
                            b.passwordEditText.text?.toString()
                        )

                        if (charSequence.toString().isEmpty()) {
                            b.passwordInputLayout.showError(context, R.string.enter_password)
                        } else {
                            b.passwordInputLayout.error = null
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                }

            })

            b.cnfPasswordEditText.addTextChangedListener {
                object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        s?.let { charSequence ->
                            /*if (charSequence.toString().isEmpty()) {
                                b.cnfPasswordInputLayout.showError(context, R.string.enter_password)
                            } else {
                                b.cnfPasswordInputLayout.error = null
                            }*/

                            if (b.passwordEditText.text?.equals(charSequence.toString()) != true) {
                                b.cnfPasswordInputLayout.showError(context, R.string.enter_password)
                            } else {
                                b.cnfPasswordInputLayout.error = null
                            }

                        }
                    }

                    override fun afterTextChanged(s: Editable?) {

                    }

                }
            }

            b.signUpButton.setOnClickListener {
                if (isInternetAvailable) {
                    hideKeyboard(requireActivity())
                    b.signUpButton.showMaterialProgressBar(R.string.please_wait)
                    authViewModel.signUp(
                        b.emailEditText.text.toString(),
                        b.passwordEditText.text.toString()
                    )
                } else {
                    showCustomAlertDialog(
                        context,
                        R.string.not_connected_to_internet,
                        R.drawable.ic_server_connection1
                    ) {}
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }


}