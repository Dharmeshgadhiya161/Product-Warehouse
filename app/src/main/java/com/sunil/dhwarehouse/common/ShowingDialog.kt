package com.sunil.dhwarehouse.common

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Html
import com.sunil.dhwarehouse.R


class ShowingDialog(mContext: Context, private var message: String?) : Dialog(mContext) {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_loading_dialog)

        when {
            message != null ->
                findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.tvMessage).text = Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY)
        }
    }
}
