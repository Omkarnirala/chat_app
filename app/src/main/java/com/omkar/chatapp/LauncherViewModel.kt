package com.omkar.chatapp

import android.content.Context
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omkar.chatapp.utils.GOOGLE_ADVERTISING_ID
import com.omkar.chatapp.utils.log
import com.omkar.chatapp.utils.setStringData
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class LauncherViewModel : ViewModel() {

    private val mTag = "LauncherViewModel"

    /**
     * Internet validation
     */
    private val internetStatus = MutableLiveData<Boolean>()

    fun setInternetStatus(status: Boolean) {
        internetStatus.postValue(status)
    }

    fun getInternetStatus(): MutableLiveData<Boolean> {
        return internetStatus
    }

    /**
     * Google advertising Id
     */

    fun setGoogleAdvId(cxt: Context?) {
        cxt?.let { context ->
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
                        log(mTag, "setGoogleAdvId = ${adInfo.id}")
                        setStringData(context, GOOGLE_ADVERTISING_ID, adInfo.id)
                    } catch (e: IOException) {
                        log(mTag, "setGoogleAdvId: IOException = ${e.message}")
                    } catch (e: IllegalStateException) {
                        log(mTag, "setGoogleAdvId: IllegalStateException = ${e.message}")
                    } catch (e: GooglePlayServicesNotAvailableException) {
                        log(mTag, "setGoogleAdvId: GooglePlayServicesNotAvailableException = ${e.message}")

                        if (Build.MANUFACTURER == "Clover" && BuildConfig.DEBUG) {
                            val dummyId = Build.BRAND.plus(Build.MODEL).plus(Build.ID).plus(Build.FINGERPRINT)
                            log(mTag, "setGoogleAdvId: dummyId = $dummyId")
                            setStringData(context, GOOGLE_ADVERTISING_ID, dummyId)
                        }

                    } catch (e: GooglePlayServicesRepairableException) {
                        log(mTag, "setGoogleAdvId: GooglePlayServicesRepairableException = ${e.message}")
                    } catch (throwable: Throwable) {
                        log(mTag, "setGoogleAdvId: Throwable = ${throwable.message}")
                    }
                }
            }
        }
    }

}