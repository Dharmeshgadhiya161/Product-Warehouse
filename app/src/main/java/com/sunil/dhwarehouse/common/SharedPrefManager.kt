package com.sunil.dhwarehouse.common

import android.content.Context

class SharedPrefManager(private var mContext:Context) {

    val sharedPref = mContext.getSharedPreferences("AppDataSave", Context.MODE_PRIVATE)
    private val editor = sharedPref.edit()

    private val userName = "UserName"
    private val isFirstShow = "FirstShow"
    private val isExcelDialogShow = "ExcelDialogShow"
    private val isReadAccountExcel = "ReadAccountExcelShow"
    private val isReadItemExcel = "ReadItemExcelShow"


    var getUserName: String?
        get() = sharedPref.getString(userName, "")
        set(setUserName) = storeString(userName, setUserName)

    var getFirstShow:Boolean
        get()=sharedPref.getBoolean(isFirstShow,false)
    set(setFirstShow) {
        editor.putBoolean(isFirstShow, setFirstShow)
        editor.apply()
    }

    var isExcelFileShowing:Boolean
        get() = sharedPref.getBoolean(isExcelDialogShow,false)
        set(setExcelFile) {
            editor.putBoolean(isExcelDialogShow,setExcelFile)
            editor.apply()
        }

    var isAccountExcelRead:Boolean
        get() = sharedPref.getBoolean(isReadAccountExcel,false)
        set(setAccountExcelRead) {
            editor.putBoolean(isReadAccountExcel,setAccountExcelRead)
            editor.apply()
        }

    var isItemExcelRead:Boolean
        get() = sharedPref.getBoolean(isReadItemExcel,false)
        set(setItemExcelRead) {
            editor.putBoolean(isReadItemExcel,setItemExcelRead)
            editor.apply()
        }

    private fun storeString(key: String, value: String?) {
        editor.run {
            putString(key, value)
            apply()
        }
    }
}