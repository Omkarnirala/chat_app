package com.omkar.chatapp.ui.signin.signup

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import com.omkar.chatapp.LauncherViewModel
import com.omkar.chatapp.R
import com.omkar.chatapp.databinding.FragmentSignupBinding
import com.omkar.chatapp.ui.chat.profile.ProfileFragment
import com.omkar.chatapp.ui.chat.profile.UpdateUserProfileViewModel
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
import com.omkar.chatapp.utils.log
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SignupFragment : BaseFragment() {

    companion object {
        private const val PROFILE_REQUEST = 1
        private const val REQUEST_CAMERA_PERMISSION = 101
        private const val mTag = "SignupFragment"
    }

    private var _binding: FragmentSignupBinding? = null
    private val b get() = _binding!!

    private lateinit var authViewModel: AuthViewModel
    private val launcherViewModel: LauncherViewModel by activityViewModels()
    private lateinit var viewModel: UpdateUserProfileViewModel
    private var isInternetAvailable = false
    private var snackbar: Snackbar? = null
    private var photoURI: Uri? = null
    private var uploadPhotoURI: Uri? = null
    private var currentPhotoFile: String? = null

    private val takeSelfieLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val resultCode = result.resultCode
        uploadPhotoURI = result.data?.data
        when (resultCode) {
            Activity.RESULT_OK -> {
                Glide.with(b.ivProfile.context).load(uploadPhotoURI).apply(RequestOptions().circleCrop()).into(b.ivProfile)
                log(mTag, "registerForActivityResult uploadPhotoURI: $uploadPhotoURI")
            }

            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(requireContext(), ImagePicker.getError(result.data), Toast.LENGTH_SHORT).show()
            }

            else -> {
                Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
            viewModel = ViewModelProvider(this)[UpdateUserProfileViewModel::class.java]

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

            authViewModel.getValidation().observe(viewLifecycleOwner) {
                it?.let { validationStatus ->
                    b.signUpButton.isEnabled = validationStatus
                }
            }

            authViewModel.signUpResult.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is SignUpResult.Success -> {
                        val user = result.user
                        if (user != null) {
                            // Handle successful sign-up, and use user if needed
                            timberLog(mTag, "getViewModelData: ${result.user}")

                            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    result.user.let {
                                        uploadImageToFirebase(context, it, task.result)
                                    }
                                } else {
                                    toasty(requireContext(), "Unable to Signup")
                                }
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

            b.ivProfile.setOnClickListener {
                // Request camera and storage permissions if not granted
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.CAMERA),
                        REQUEST_CAMERA_PERMISSION
                    )
                } else {
                    // If permission granted, dispatch take picture intent
                    dispatchTakePictureIntent(PROFILE_REQUEST, context)
                }
            }

            b.cnfPasswordEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) b.signUpButton.performClick()
                true
            }

            b.userNameEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    s?.let { charSequence ->
                        authViewModel.setSignUpValidation(
                            b.userNameEditText.text?.toString(),
                            b.emailEditText.text?.toString(),
                            b.passwordEditText.text?.toString(),
                            b.cnfPasswordEditText.text.toString()
                        )

                        if (charSequence.toString().isEmpty()) {
                            b.userNameInputLayout.showError(context, R.string.username)
                        } else {
                            b.userNameInputLayout.error = null
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })

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

                        authViewModel.setSignUpValidation(
                            b.userNameEditText.text?.toString(),
                            b.emailEditText.text?.toString(),
                            b.passwordEditText.text?.toString(),
                            b.cnfPasswordEditText.text.toString()
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
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    s?.let { charSequence ->
                        authViewModel.setSignUpValidation(
                            b.userNameEditText.text?.toString(),
                            b.emailEditText.text?.toString(),
                            b.passwordEditText.text?.toString(),
                            b.cnfPasswordEditText.text.toString()
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

            b.cnfPasswordEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    s?.let { charSequence ->
                        authViewModel.setSignUpValidation(
                            b.userNameEditText.text?.toString(),
                            b.emailEditText.text?.toString(),
                            b.passwordEditText.text?.toString(),
                            b.cnfPasswordEditText.text.toString()
                        )

                        if (charSequence.toString().equals(b.passwordEditText.text.toString(), false)) {
                            b.cnfPasswordInputLayout.error = null
                        } else {
                            b.cnfPasswordInputLayout.showError(context, R.string.password_not_match)

                        }

                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })

            b.signUpButton.setOnClickListener {

                if (isInternetAvailable) {
                    if (b.emailEditText.text?.isNotEmpty() == true && b.passwordEditText.text?.isNotEmpty() == true && b.cnfPasswordEditText.text?.isNotEmpty()
                        == true
                    ) {
                        hideKeyboard(requireActivity())
                        b.signUpButton.showMaterialProgressBar(R.string.please_wait)
                        authViewModel.signUp(
                            b.emailEditText.text.toString().trim(),
                            b.passwordEditText.text.toString().trim()
                        )
                    } else {
                        hideKeyboard(requireActivity())
                        toasty(context, "Fields are Empty")
                    }
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

    private fun uploadImageToFirebase(context: Context, firebaseUser: FirebaseUser, token: String) {
        viewModel.uploadImage(
            uploadPhotoURI,
            onSuccess = { url ->
                b.signUpButton.hideMaterialProgressBar(context.getString(R.string.sign_up))
                currentPhotoFile = url

                val user = UserDetailsModel(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = firebaseUser.displayName ?: b.userNameEditText.text.toString(),
                    profileImageUrl = url,
                    isOnline = true,
                    token = token
                )
                authViewModel.addUserToFirestore(user)
                setBooleanData(context, IS_LOGIN, true)
                setStringData(context, USER_ID, user.uid)
                setStringData(context, USER_EMAIL, user.email)
                loginMethod(cxt)
            },
            onFailure = { exception ->
                toasty(requireContext(), "Unable to upload image $exception")
            }
        )
    }

    private fun dispatchTakePictureIntent(selfieRequest: Int, context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                    val photoFile: File? = try {
                        createImageFile(selfieRequest, context)
                    } catch (ex: IOException) {
                        null
                    }
                    photoFile?.also {
                        photoURI = FileProvider.getUriForFile(
                            requireContext(),
                            requireActivity().packageName,
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        ImagePicker.with(requireActivity())
                            .compress(1024)
                            .cameraOnly()
                            .maxResultSize(
                                1080,
                                1080
                            )  //Final image resolution will be less than 1080 x 1080(Optional)
                            .createIntent { intent ->
                                takeSelfieLauncher.launch(intent)
                            }
                    }
                    log(mTag, "photoURI: $photoURI")
                }
            }
        }
    }

    // Function to create a unique image file
    private fun createImageFile(requestType: Int, context: Context): File {
        // Create an image file name
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US)
        val formattedDate = dateFormat.format(Date())
        val imageFileName = when (requestType) {
            PROFILE_REQUEST -> "JPEG_${formattedDate}_selfie_"
            else -> "JPEG_${formattedDate}_"
        }
        // Use external cache directory to avoid needing to request external storage permission
        val storageDir: File? = context.externalCacheDir
        return File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}