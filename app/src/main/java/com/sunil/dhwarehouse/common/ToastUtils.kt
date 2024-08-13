package com.sunil.dhwarehouse.common

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.sunil.dhwarehouse.R

object ToastUtils {

    fun showCustomToast(context: Context, message: String, duration: Int) {
        // Get the LayoutInflater from the context
        val inflater = LayoutInflater.from(context)

        // Inflate the custom layout
        val layout = inflater.inflate(R.layout.custom_toast, null)

        // Get the TextView from the custom layout and set the text
        val textView = layout.findViewById<TextView>(R.id.toastMessage)
        textView.text = message

        // Create the Toast and set its view to the custom layout
        val toast = Toast(context)
        toast.duration = duration
        toast.view = layout

        // Show the Toast
        toast.show()
    }
}