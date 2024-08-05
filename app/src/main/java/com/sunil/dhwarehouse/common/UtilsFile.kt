package com.sunil.dhwarehouse.common

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UtilsFile {
    companion object {
        const val fileExcelAccount= "account master.xlsx"
        const val localSaveAccountFileName = "account_master.xlsx"
        const val fileExcelItem = "stock leger  margine.xlsx"
        const val localSaveItemFileName = "stock_leger_margine.xlsx"

        private const val TAG = "Utils"

        var isChangeValues:Boolean= false

    }
   public  fun getLocalFilePath(context: Context, fileName: String): File {
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


    private fun getFormattedDateTime(format: String): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    // Usage
    val formattedDateTime = getFormattedDateTime("dd-MM-yyyy HH:mm:ss")

}