package com.omkar.chatapp.utils

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.omkar.chatapp.R
import com.omkar.chatapp.databinding.DialogMessageBinding

fun showCustomAlertDialog(cxt: Context?, message: Int, icon: Int, function: () -> Unit) {

    cxt?.let { context ->
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setCancelable(false)

        val binding: DialogMessageBinding =
            DialogMessageBinding.inflate(LayoutInflater.from(context))
        builder.setView(binding.root)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_bg)
        binding.ivIcon.setImageDrawable(ContextCompat.getDrawable(context, icon))
        binding.tvMessage.text = context.getString(message)
        binding.buttonOk.setOnClickListener {
            dialog.dismiss()
            function()
        }
        dialog.show()
    }
}