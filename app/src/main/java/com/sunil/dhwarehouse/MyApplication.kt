package com.sunil.dhwarehouse

import android.app.Application
import android.app.ActivityManager
import android.content.Context
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this@MyApplication)
    }

}