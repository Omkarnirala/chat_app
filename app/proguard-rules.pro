# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class **.zego.** { *; }
-keep class android.support.** { *; }

-dontwarn com.heytap.msp.push.HeytapPushManager
-dontwarn com.heytap.msp.push.callback.ICallBackResultService
-dontwarn com.heytap.msp.push.mode.DataMessage
-dontwarn com.heytap.msp.push.service.DataMessageCallbackService
-dontwarn com.huawei.hms.aaid.HmsInstanceId
-dontwarn com.huawei.hms.common.ApiException
-dontwarn com.huawei.hms.push.HmsMessageService
-dontwarn com.huawei.hms.push.RemoteMessage$Notification
-dontwarn com.huawei.hms.push.RemoteMessage
-dontwarn com.vivo.push.IPushActionListener
-dontwarn com.vivo.push.PushClient
-dontwarn com.vivo.push.PushConfig$Builder
-dontwarn com.vivo.push.PushConfig
-dontwarn com.vivo.push.listener.IPushQueryActionListener
-dontwarn com.vivo.push.model.UPSNotificationMessage
-dontwarn com.vivo.push.model.UnvarnishedMessage
-dontwarn com.vivo.push.sdk.OpenClientPushMessageReceiver
-dontwarn com.vivo.push.util.VivoPushException
-dontwarn com.xiaomi.mipush.sdk.MiPushClient
-dontwarn com.xiaomi.mipush.sdk.MiPushCommandMessage
-dontwarn com.xiaomi.mipush.sdk.MiPushMessage
-dontwarn com.xiaomi.mipush.sdk.PushMessageReceiver

# This will strip `Log.v`, `Log.d`, and `Log.i` statements and will leave `Log.w` and `Log.e` statements intact.
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# R8 full mode strips generic signatures from return types if not kept.
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

#######################################################################################################
################################################# GSON ################################################
#######################################################################################################

# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

#######################################################################################################
################################################# Firebase Crashlytics ################################
#######################################################################################################
# for Firebase Crashlytics
# Get deobfuscated crash reports with the Firebase Crashlytics SDK
# https://firebase.google.com/docs/crashlytics/get-deobfuscated-reports?platform=android
-keepattributes SourceFile,LineNumberTable        # Keep file names and line numbers.
-keep public class * extends java.lang.Exception  # Optional: Keep custom exceptions.

#######################################################################################################
##################################### FragmentContainerView ###########################################
#######################################################################################################

-keep class * extends androidx.fragment.app.Fragment{}
-keep class * extends androidx.fragment.app.FragmentContainerView{}
#######################################################################################################
####################### Didn't find class "android.view.x" on path: DexPathList #######################
#######################################################################################################
-keep class * extends android.view.LayoutInflater
-keep class * extends android.view.View
-keep class * extends android.view.ViewGroup

#######################################################################################################
##################################### CoordinatorLayout ###########################################
#######################################################################################################
-keep public class * extends androidx.coordinatorlayout.widget.CoordinatorLayout$Behavior {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>();
}
# Make sure we keep annotations for CoordinatorLayout's DefaultBehavior and ViewPager's DecorView
-keepattributes *Annotation*

# Gson uses generic type information stored in a class file when working with
# fields. Proguard removes such information by default, keep it.
-keepattributes Signature

# This is also needed for R8 in compat mode since multiple
# optimizations will remove the generic signature such as class
# merging and argument removal. See:
# https://r8.googlesource.com/r8/+/refs/heads/main/compatibility-faq.md#troubleshooting-gson-gson
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Optional. For using GSON @Expose annotation
-keepattributes AnnotationDefault,RuntimeVisibleAnnotations

# Application classes that will be serialized/deserialized over Gson
-keep class com.myapplication.model.api.** { *; }