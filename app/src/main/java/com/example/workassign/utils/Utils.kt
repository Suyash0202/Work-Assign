package com.example.workassign.utils

import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.workassign.R

object Utils {
    private var dialogs: AlertDialog? = null

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showProgressDialog(context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.progress_dialoge, null)
        dialogs = AlertDialog.Builder(context).setView(dialogView).setCancelable(false).create()
        dialogs?.show()
    }

    fun hideProgressDialog() {
        dialogs?.dismiss()
        dialogs = null
    }
}