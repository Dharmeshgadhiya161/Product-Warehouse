package com.sunil.dhwarehouse.common

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class HighlightTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    fun setHighlightText(text: String, highlight: String, highlightColor: Int) {
        if (highlight.isEmpty()) {
            setText(text)
            return
        }

        val spannable = SpannableString(text)
        var start = text.lowercase().indexOf(highlight.lowercase())
        while (start >= 0) {
            val end = start + highlight.length
            spannable.setSpan(
                ForegroundColorSpan(highlightColor),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            start = text.lowercase().indexOf(highlight.lowercase(), end)
        }
        setText(spannable)
    }
}