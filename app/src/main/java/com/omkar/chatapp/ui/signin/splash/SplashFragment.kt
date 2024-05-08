package com.omkar.chatapp.ui.signin.splash

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.load
import com.omkar.chatapp.LauncherViewModel
import com.omkar.chatapp.R
import com.omkar.chatapp.databinding.FragmentSplashBinding
import com.omkar.chatapp.utils.BaseFragment
import com.omkar.chatapp.utils.IS_LOGIN
import com.omkar.chatapp.utils.getBooleanData
import com.omkar.chatapp.utils.getVersionName
import com.omkar.chatapp.utils.isGooglePlayServicesAvailable
import com.omkar.chatapp.utils.log
import com.omkar.chatapp.utils.loginMethod
import com.omkar.chatapp.utils.showCustomAlertDialog

class SplashFragment : BaseFragment() {

    private val mTag = "SplashFragment"

    private var _binding: FragmentSplashBinding? = null
    private val b get() = _binding!!

    private val launcherViewModel: LauncherViewModel by activityViewModels()
    private var isInternetAvailable = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        animateLogo()
        getViewModelData()

        getVersionName(cxt).let {
            b.tvVersion.text = getString(R.string.version, it)
        }
    }

    private fun animateLogo() {
        try {
            val imageLoader = ImageLoader.Builder(requireContext()).componentRegistry {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder())
                } else {
                    add(GifDecoder())
                }
            }.build()
            b.ivLogo.load(R.drawable.ic_launcher_foreground, imageLoader)
        } catch (e: Throwable) {
            log(mTag, "catch error = ${e.message}")
        }
    }

    private fun getViewModelData() {
        cxt?.let { context ->
            launcherViewModel.getInternetStatus().observe(viewLifecycleOwner) { status ->
                status?.let { isInternetAvailable ->

                    log(mTag, "isInternetAvailable = $isInternetAvailable")

                    this.isInternetAvailable = isInternetAvailable

                    checkForInternet()//viewmodel

                }
            }
        }

    }

    private fun checkForInternet() {
        if (isInternetAvailable) {
            log(mTag, "internet is available")
            checkForLoginStatus()

        } else {
            log(mTag, "internet is unavailable")
            showCustomAlertDialog(cxt, R.string.not_connected_to_internet, R.drawable.no_data) {
                requireActivity().finish()
            }

        }
    }

    private fun checkForLoginStatus() {

        cxt?.let { context ->

            if (isGooglePlayServicesAvailable(requireActivity(), context)) {
                Handler(Looper.getMainLooper()).postDelayed({
                    getBooleanData(context, IS_LOGIN).let { loginStatus ->
                        if (loginStatus) {
                            loginMethod(context)//SplashFragment //checkForLoginStatus
                        } else {
                            //if not logged in
                            try {
                                findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToSignInFragment())
                            } catch (e: Throwable) {
                                log(mTag, "catch error = ${e.message}")
                            }
                        }
                    }
                },1000)

            }
        }

    }

    private fun checkForLoginStatusDemo() {

        cxt?.let { context ->

            if (isGooglePlayServicesAvailable(requireActivity(), context)) {
                Handler(Looper.getMainLooper()).postDelayed({
                    getBooleanData(context, IS_LOGIN).let { loginStatus ->
                        if (loginStatus) {
                            //if already logged in
                            /* val i = Intent(requireContext(), MainActivity::class.java)
                             i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                             startActivity(i)*/
                            loginMethod(context)//SplashFragment //checkForLoginStatusDemo
                        } else {
                            //if not logged in
                            try {
                                findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToSignInFragment())
                            } catch (e: Throwable) {
                                log(mTag, "catch error = ${e.message}")
                            }
                        }
                    }
                }, 2000)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}