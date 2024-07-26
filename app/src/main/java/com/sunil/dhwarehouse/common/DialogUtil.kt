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
/*holder.binding.edtAddQty.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Update the data source when EditText value changes
                if (position != RecyclerView.NO_POSITION) {
                    edtQtyNumber = (s.toString().toDoubleOrNull() ?: 0.0).toString()
                    //itemMasterList[position].stock_qty = - itemMasterList[position].stock_qty - edtQtyNumber.toDouble()
                    holder.binding.txtCountQty.text = edtQtyNumber
                 //   notifyItemChanged(position) // Notify change to refresh the view
                }


            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })*/