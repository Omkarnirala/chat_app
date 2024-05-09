package com.omkar.chatapp.ui.chat.profile

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.omkar.chatapp.R
import com.omkar.chatapp.databinding.FragmentProfileBinding
import com.omkar.chatapp.utils.BaseFragment
import com.omkar.chatapp.utils.FirebaseEvent
import com.omkar.chatapp.utils.USER_EMAIL
import com.omkar.chatapp.utils.getStringData
import com.omkar.chatapp.utils.hideKeyboard
import com.omkar.chatapp.utils.hideMaterialProgressBar
import com.omkar.chatapp.utils.showInternetError
import com.omkar.chatapp.utils.showMaterialProgressBar
import com.omkar.chatapp.utils.toasty
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileFragment : BaseFragment() {


    companion object {
        private const val PROFILE_REQUEST = 1
        private const val REQUEST_CAMERA_PERMISSION = 101
        private const val mTag = "ProfileFragment"
    }

    private var _binding: FragmentProfileBinding? = null
    private val b get() = _binding!!
    private lateinit var viewModel: UpdateUserProfileViewModel
    private var snackbar: Snackbar? = null
    private var photoURI: Uri? = null
    private var currentPhotoFile: String? = null

    private val takeSelfieLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val resultCode = result.resultCode
            val data = result.data
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val fileUri = data?.data!!
//                b.includeUpload.progressDemSelfie.visibility = View.VISIBLE
//                b.includeUpload.ivDemoSelfie.visibility = View.GONE
                    uploadImageToFirebase(PROFILE_REQUEST, fileUri)
                }

                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT)
                        .show()
                }

                else -> {
                    Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun uploadImageToFirebase(selfieRequest: Int, fileUri: Uri) {
        viewModel.uploadImage(fileUri,
            onSuccess = { url ->
                currentPhotoFile = url
                toasty(requireContext(), "Done")
            },
            onFailure = { exception ->
                toasty(requireContext(), "Unable to upload image $exception")
            }
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
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

        snackbar = showInternetError(context, b.rootLayout)

        firebaseAnalytics?.logEvent(FirebaseEvent.PROFILE_FRAGMENT) {
            param(USER_EMAIL, getStringData(context, USER_EMAIL, ""))
            param(FirebaseAnalytics.Param.SCREEN_NAME, mTag)
        }

        viewModel = ViewModelProvider(this)[UpdateUserProfileViewModel::class.java]

    }

    private fun getViewModelData() {
        cxt?.let { context ->
            // Observing update results
            viewModel.updateResult.observe(viewLifecycleOwner) { success ->
                b.buttonSave.hideMaterialProgressBar(context.getString(R.string.save))
                if (success) {
                    toasty(requireContext(), "Profile Updated Successfully")
                    findNavController().navigateUp()
                } else {
                    toasty(requireContext(), "Failed to Update Profile")
                }
            }
        }
    }

    private fun viewListener() {
        cxt?.let { context ->
            b.toolbar.tvTitle.text = getString(R.string.profile)
            b.toolbar.backButton.setOnClickListener {
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


            b.buttonSave.setOnClickListener {
                hideKeyboard(requireActivity())
                b.buttonSave.showMaterialProgressBar(R.string.please_wait)
                viewModel.updateUserProfile(
                    b.tietUserName.text.toString(),
                    currentPhotoFile,
                    b.tietAboutUser.text.toString()
                )
            }
        }
    }

    private fun dispatchTakePictureIntent(selfieRequest: Int, context: Context) {
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


}