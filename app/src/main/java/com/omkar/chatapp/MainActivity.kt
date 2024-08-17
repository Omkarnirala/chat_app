package com.omkar.chatapp

import android.Manifest
import android.app.Application
import android.os.Bundle
import com.elvishew.xlog.XLog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.omkar.chatapp.utils.BaseAppCompatActivity
import com.omkar.chatapp.utils.FirebaseUtil
import com.omkar.chatapp.utils.log
import com.omkar.chatapp.utils.timberLog
import com.permissionx.guolindev.PermissionX
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.event.CallEndListener
import com.zegocloud.uikit.prebuilt.call.event.ErrorEventsListener
import com.zegocloud.uikit.prebuilt.call.event.SignalPluginConnectListener
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallInvitationData
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoUIKitPrebuiltCallConfigProvider
import com.zegocloud.uikit.service.express.IExpressEngineEventHandler
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason
import org.json.JSONObject
import timber.log.Timber

class MainActivity : BaseAppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val mTAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        XLog.init()
        offlineUsePermission()
        FirebaseUtil.currentUserDetails()
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val document = it.result
                    if (document != null) {
                        initCallInviteService(document.getString("uid").toString(), document.getString("displayName").toString())
                    }
                }
            }
            .addOnFailureListener {exception ->
                log(mTAG, "currentUserName Error getting document: $exception")
            }
    }

    private fun offlineUsePermission() {
        PermissionX.init(this).permissions(Manifest.permission.SYSTEM_ALERT_WINDOW)
            .onExplainRequestReason { scope, deniedList ->
                val message =
                    "We need your consent for the following permissions in order to use the offline call function properly"
                scope.showRequestReasonDialog(deniedList, message, "Allow", "Deny")
            }.request { _, _, _ -> }
    }

    private fun initCallInviteService(uid: String, uName: String) {

        val application: Application = application

        //Old
//        val appID: Long = 1602751084
//        val appSign = "98aed2b4e01ff9affd18ae5aa05f658293d4116b1a6238939dbeb02238f487d3"

        //New
        val appID: Long = 1043619169
        val appSign = "2729b9d1ae42270273a5b74dcd01b1e5fedf70ce5a77df4b6cd60b2b617830eb"

        val userID: String = uid
        val userName: String = uName
        timberLog("MainActivity", "UserName: $userName")
        val callInvitationConfig = ZegoUIKitPrebuiltCallInvitationConfig().apply {
            provider = ZegoUIKitPrebuiltCallConfigProvider { invitationData -> getConfig(invitationData) }
        }

        ZegoUIKitPrebuiltCallService.events.errorEventsListener =
            ErrorEventsListener { errorCode, message -> Timber.d("onError() called with: errorCode = [$errorCode], message = [$message]") }

        ZegoUIKitPrebuiltCallService.events.invitationEvents.pluginConnectListener =
            SignalPluginConnectListener { state, event, extendedData -> Timber.d("onSignalPluginConnectionStateChanged() called with: state = [$state], event = [$event], extendedData = [$extendedData]") }

        ZegoUIKitPrebuiltCallService.init(
            application,
            appID,
            appSign,
            userID,
            userName,
            callInvitationConfig
        )

        ZegoUIKitPrebuiltCallService.events.callEvents.callEndListener =
            CallEndListener { callEndReason, jsonObject -> Timber.d("onCallEnd() called with: callEndReason = [$callEndReason], jsonObject = [$jsonObject]") }

        ZegoUIKitPrebuiltCallService.events.callEvents.setExpressEngineEventHandler(object :
            IExpressEngineEventHandler() {
            override fun onRoomStateChanged(
                roomID: String,
                reason: ZegoRoomStateChangedReason,
                errorCode: Int,
                extendedData: JSONObject,
            ) {
                Timber.d("onRoomStateChanged() called with: roomID = [$roomID], reason = [$reason], errorCode = [$errorCode], extendedData = [$extendedData]")
            }
        })
    }

    private fun getConfig(invitationData: ZegoCallInvitationData): ZegoUIKitPrebuiltCallConfig {
        val isVideoCall = invitationData.type == ZegoInvitationType.VIDEO_CALL.value
        val isGroupCall = invitationData.invitees.size > 1
        return when {
            isVideoCall && isGroupCall -> ZegoUIKitPrebuiltCallConfig.groupVideoCall()
            !isVideoCall && isGroupCall -> ZegoUIKitPrebuiltCallConfig.groupVoiceCall()
            !isVideoCall -> ZegoUIKitPrebuiltCallConfig.oneOnOneVoiceCall()
            else -> ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall()
        }
    }

    override fun onPause() {
        super.onPause()
        FirebaseUtil.updateUserStatus(auth.currentUser?.uid, false)
    }

    override fun onResume() {
        super.onResume()
        FirebaseUtil.updateUserStatus(auth.currentUser?.uid, true)
    }

    override fun onDestroy() {
        super.onDestroy()
        ZegoUIKitPrebuiltCallService.endCall()
        ZegoUIKitPrebuiltCallService.unInit()
        FirebaseUtil.updateUserStatus(auth.currentUser?.uid, false)
    }
}