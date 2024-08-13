package com.sunil.dhwarehouse.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UtilsFile {
    companion object {


        const val fileExcelAccount = "account master.xlsx"
        const val localSaveAccountFileName = "account_master.xlsx"
        const val fileExcelItem = "stock leger  margine.xlsx"
        const val localSaveItemFileName = "stock_leger_margine.xlsx"

        private const val TAG = "Utils"

        var isChangeValues: Boolean = false
        var isFinishInvoice: Boolean = false
        var isFinishInvoiceBil: Boolean = false
    }

    fun roundValues(roundValue: Double): Double {
        // Rounding to two decimal places using BigDecimal
        return BigDecimal(roundValue).setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    public fun getLocalFilePath(context: Context, fileName: String): File {
        val directory = File(context.filesDir, "my_excel_files")//folderName
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return File(directory, fileName)
    }

    fun deleteLocalFile(file: File, expectedFileName: String) {
        if (file.exists()) {
            if (file.name == expectedFileName) {
                if (file.delete()) {
                    Log.d(TAG, "File deleted successfully: ${file.absolutePath}")
                } else {
                    Log.e(TAG, "Failed to delete file: ${file.absolutePath}")
                }
            } else {
                Log.e(
                    TAG,
                    "File name does not match expected file name: ${file.name} vs $expectedFileName"
                )
            }
        } else {
            Log.e(TAG, "File does not exist: ${file.absolutePath}")
        }
    }


    fun getFormattedDateTime(): Pair<String, String> {
        val calendar = Calendar.getInstance()
        val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val formattedDate = dateFormatter.format(calendar.time)
        val formattedTime = timeFormatter.format(calendar.time)

        return Pair(formattedDate, formattedTime)
    }



}