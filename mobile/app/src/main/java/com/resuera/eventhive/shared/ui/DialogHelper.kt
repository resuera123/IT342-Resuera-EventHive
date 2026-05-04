package com.resuera.eventhive.ui

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.resuera.eventhive.R

enum class DialogIcon(val bgRes: Int, val emoji: String, val emojiColor: String) {
    SUCCESS(R.drawable.bg_dialog_success, "✓", "#198754"),
    WARNING(R.drawable.bg_dialog_warning, "⚠", "#D97706"),
    DANGER(R.drawable.bg_dialog_danger, "✕", "#DC3545"),
    INFO(R.drawable.bg_dialog_info, "▶", "#0D6EFD")
}

object DialogHelper {

    fun showConfirm(
        context: Context,
        icon: DialogIcon,
        title: String,
        message: String,
        confirmText: String,
        confirmColor: String,
        onConfirm: () -> Unit
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_confirm, null)
        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Set icon
        view.findViewById<View>(R.id.viewIconBg).setBackgroundResource(icon.bgRes)
        val tvEmoji = view.findViewById<TextView>(R.id.tvIconEmoji)
        tvEmoji.text = icon.emoji
        tvEmoji.setTextColor(Color.parseColor(icon.emojiColor))

        view.findViewById<TextView>(R.id.tvDialogTitle).text = title
        view.findViewById<TextView>(R.id.tvDialogMessage).text = message

        val btnConfirm = view.findViewById<Button>(R.id.btnDialogConfirm)
        btnConfirm.text = confirmText
        btnConfirm.setBackgroundColor(Color.parseColor(confirmColor))
        btnConfirm.setTextColor(Color.WHITE)

        btnConfirm.setOnClickListener { onConfirm(); dialog.dismiss() }
        view.findViewById<Button>(R.id.btnDialogCancel).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    fun showSuccess(
        context: Context,
        title: String,
        message: String,
        onDone: (() -> Unit)? = null
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_success, null)
        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        view.findViewById<TextView>(R.id.tvSuccessTitle).text = title
        view.findViewById<TextView>(R.id.tvSuccessMessage).text = message

        view.findViewById<Button>(R.id.btnSuccessOk).setOnClickListener {
            dialog.dismiss()
            onDone?.invoke()
        }

        dialog.show()
    }
}