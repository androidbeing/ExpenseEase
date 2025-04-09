package com.dolphin.expenseease.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import com.dolphin.expenseease.R
import com.dolphin.expenseease.data.model.Alert
import com.dolphin.expenseease.listeners.OnClickAlertListener

object DialogUtils {

    fun showAlertDialog(context: Context, alert: Alert, listener: OnClickAlertListener) {
        val alertDialog = LayoutInflater.from(context).inflate(R.layout.layout_confirm_alert, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(alertDialog)
        builder.setCancelable(false)
        alertDialog.findViewById<TextView>(R.id.txtTitle).text = alert.title
        alertDialog.findViewById<TextView>(R.id.txtMsg).text = alert.message
        val okBtn = alertDialog.findViewById<AppCompatButton>(R.id.btnOkay)
        val noBtn = alertDialog.findViewById<AppCompatButton>(R.id.btnNo)
        okBtn.text = alert.positiveButtonText
        noBtn.text = alert.negativeButtonText

        val dialog: AlertDialog = builder.create()
        okBtn.setOnClickListener {
            listener.onAcknowledge(true)
            dialog.dismiss()
        }

        noBtn.setOnClickListener {
            listener.onAcknowledge(false)
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }
}