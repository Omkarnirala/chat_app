package com.omkar.chatapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.NonNull
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.omkar.chatapp.utils.progress.hideProgress
import com.omkar.chatapp.utils.progress.showProgress
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.omkar.chatapp.R
import java.text.SimpleDateFormat
import java.util.Locale


private const val mTag = "UtilsMethodExtension"
private val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
private val outputFormat = SimpleDateFormat("MM/dd/yy", Locale.US)


fun TextInputLayout.showError(context: Context, errorMessage: String) {
    this.error = errorMessage
    this.errorIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_info_error)
}

fun TextInputLayout.showError(context: Context, errorMessage: Int) {
    this.error = context.getString(errorMessage)
    this.errorIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_info_error)
}

fun TextInputLayout.hideError(cxt: Context, defaultEndIcon: Int = R.drawable.ic_transparent) {
    this.error = null
    this.errorIconDrawable = ContextCompat.getDrawable(cxt, defaultEndIcon)
}

fun logLong(tag: String, content: String) {
    val maxLogSize = 1000
    for (i in 0..content.length / maxLogSize) {
        val start = i * maxLogSize
        var end = (i + 1) * maxLogSize
        end = if (end > content.length) content.length else end
        Log.d(tag, content.substring(start, end))
    }
}

fun Button.showMaterialDefaultProgressBar(@ColorRes color: Int) {
    isClickable = false
    showProgress {
        buttonTextRes = R.string.loading
        progressColorRes = color
    }
}

fun View.disableClick() {
    isFocusable = false
    isClickable = false
    isEnabled = false
}

fun View.enableClick() {
    isFocusable = true
    isClickable = true
    isEnabled = true
}

fun Button.showMaterialDefaultProgressBar() {
    isEnabled = false
    showProgress {
        buttonTextRes = R.string.loading
        progressColor = Color.WHITE
    }
}

fun Button.showMaterialEmailProgressBar() {
    isEnabled = false
    showProgress {
        buttonTextRes = R.string.loading
        progressColor = Color.BLACK
    }
}

fun TextView.showMaterialDefaultProgressBar() {
    isEnabled = false
    showProgress {
        buttonTextRes = R.string.loading
        progressColor = Color.WHITE
    }
}

fun Button.showMaterialDefaultProgressBarBlack() {
    isEnabled = false
    showProgress {
        buttonTextRes = R.string.loading
        progressColor = Color.BLACK
    }
}

fun Button.showMaterialDefaultProgressBarRed() {
    isEnabled = false
    showProgress {
        buttonTextRes = R.string.deleting
        progressColor = Color.RED
    }
}

@SuppressLint("ResourceAsColor")
fun Button.showMaterialDefaultProgressBarForApproval() {
    isEnabled = false
    showProgress {
        buttonTextRes = R.string.loading
        progressColor = R.color.color_primary
    }
}

@SuppressLint("ResourceAsColor")
fun Button.showMaterialDefaultProgressBarForSave() {
    isEnabled = false
    showProgress {
        progressColor = R.color.black
    }
}

fun Button.showMaterialProgressBar(@NonNull resourceMessage: Int) {
    isEnabled = false
    showProgress {
        buttonTextRes = resourceMessage
        progressColor = Color.WHITE
    }
}

fun Button.hideMaterialProgressBar(@NonNull context: Context, @NonNull message: Int) {
    isEnabled = true
    hideProgress(context.getString(message))
}

fun Button.hideMaterialProgressBar(@NonNull message: String) {
    isEnabled = true
    hideProgress(message)
}

fun TextView.hideMaterialProgressBar(@NonNull message: Int) {
    isEnabled = true
    hideProgress(message)
}

fun TextView.hideMaterialProgressBar(@NonNull message: String) {
    isEnabled = true
    hideProgress(message)
}

fun Button.hideMaterialProgressBar() {
    isEnabled = true
    hideProgress()
}

fun Boolean.getSwitchStatus(): String {
    return if (this) SWITCH_ON else SWITCH_OFF
}

fun CoordinatorLayout.showInternetError(cxt: Context?): Snackbar? {
    cxt?.let { context ->
        return Snackbar.make(this, R.string.no_internet, Snackbar.LENGTH_INDEFINITE).setActionTextColor(
            ContextCompat.getColor(context, R.color.red))
            .setAction(R.string.check) {
                context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
            }
    }
    return null
}

@SuppressLint("ShowToast")
fun showInternetError(cxt: Context?, coordinatorLayout: CoordinatorLayout): Snackbar? {
    cxt?.let { context ->
        return try {
            Snackbar.make(coordinatorLayout, R.string.no_internet, Snackbar.LENGTH_INDEFINITE).setActionTextColor(
                ContextCompat.getColor(context, R.color.red))
                .setAction(R.string.check) {
                    context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                }
        } catch (e: Throwable) {
            null
        }
    }
    return null
}

@SuppressLint("ShowToast")
fun showInternetError(cxt: Context?, constraintLayout: ConstraintLayout): Snackbar? {
    cxt?.let { context ->
        return try {
            Snackbar.make(constraintLayout, R.string.no_internet, Snackbar.LENGTH_INDEFINITE).setActionTextColor(
                ContextCompat.getColor(context, R.color.red))
                .setAction(R.string.check) {
                    context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                }
        } catch (e: Throwable) {
            null
        }
    }
    return null
}

/**
 * removes all recyclerview item decorations
 *
 */
fun RecyclerView.removeItemDecorations() {
    while (this.itemDecorationCount > 0) {
        this.removeItemDecorationAt(0)
    }
}

fun String?.formatMobile(): String {
    if (this.isNullOrEmpty()) return ""
    if (this.length == 10) {
        return "${this.substring(0, 3)}-${this.substring(3, 6)}-${this.substring(6, this.length)}"
    }
    return this
}

// The multiplication table
var d = arrayOf(
    intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
    intArrayOf(1, 2, 3, 4, 0, 6, 7, 8, 9, 5),
    intArrayOf(2, 3, 4, 0, 1, 7, 8, 9, 5, 6),
    intArrayOf(3, 4, 0, 1, 2, 8, 9, 5, 6, 7),
    intArrayOf(4, 0, 1, 2, 3, 9, 5, 6, 7, 8),
    intArrayOf(5, 9, 8, 7, 6, 0, 4, 3, 2, 1),
    intArrayOf(6, 5, 9, 8, 7, 1, 0, 4, 3, 2),
    intArrayOf(7, 6, 5, 9, 8, 2, 1, 0, 4, 3),
    intArrayOf(8, 7, 6, 5, 9, 3, 2, 1, 0, 4),
    intArrayOf(9, 8, 7, 6, 5, 4, 3, 2, 1, 0)
)

// The permutation table
var p = arrayOf(
    intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
    intArrayOf(1, 5, 7, 6, 2, 8, 3, 0, 9, 4),
    intArrayOf(5, 8, 0, 3, 7, 9, 6, 1, 4, 2),
    intArrayOf(8, 9, 1, 6, 0, 4, 3, 5, 2, 7),
    intArrayOf(9, 4, 5, 3, 1, 2, 6, 8, 7, 0),
    intArrayOf(4, 2, 8, 6, 5, 7, 3, 9, 0, 1),
    intArrayOf(2, 7, 9, 3, 8, 0, 6, 4, 1, 5),
    intArrayOf(7, 0, 4, 6, 9, 1, 3, 2, 5, 8)
)

// The inverse table
var inv = intArrayOf(0, 4, 3, 2, 1, 5, 6, 7, 8, 9)

