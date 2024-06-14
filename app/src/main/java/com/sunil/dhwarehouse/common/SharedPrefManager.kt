package com.sunil.dhwarehouse.common

import android.content.Context

class SharedPrefManager(private var mContext:Context) {

    val sharedPref = mContext.getSharedPreferences("AppDataSave", Context.MODE_PRIVATE)
    private val editor = sharedPref.edit()

    private val userName = "UserName"
    private val isFirstShow = "FirstShow"


    var getUserName: String?
        get() = sharedPref.getString(userName, "")
        set(setUserName) = storeString(userName, setUserName)

    var getFirstShow:Boolean
        get()=sharedPref.getBoolean(isFirstShow,false)
    set(setFirstShow) {
        editor.putBoolean(isFirstShow, setFirstShow)
        editor.apply()
    }

    private fun storeString(key: String, value: String?) {
        editor.run {
            putString(key, value)
            apply()
        }
    }
}