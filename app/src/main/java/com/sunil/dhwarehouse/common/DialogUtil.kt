package com.sunil.dhwarehouse.common

import android.app.AlertDialog
import android.content.Context

object DialogUtil {
    fun showNoInternetDialog(context: Context) {
        AlertDialog.Builder(context).apply {
            setTitle("No Internet Connection")
            setMessage("Please check your internet connection and try again.")
            setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            setCancelable(false)
            create()
            show()
        }
    }
}