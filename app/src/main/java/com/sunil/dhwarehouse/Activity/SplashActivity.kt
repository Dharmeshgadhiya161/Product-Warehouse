package com.sunil.dhwarehouse.Activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sunil.dhwarehouse.MainActivity
import com.sunil.dhwarehouse.R
import com.sunil.dhwarehouse.common.SharedPrefManager

class SplashActivity : AppCompatActivity() {
    private lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        sharedPrefManager = SharedPrefManager(this@SplashActivity)


        Handler(Looper.myLooper()!!).postDelayed({
            if (!sharedPrefManager.getFirstShow) {
                startActivity(Intent(this@SplashActivity, LoginUserActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }

//            startActivity(Intent(this@SplashActivity, FirebaseMainActivity::class.java))
//            finish()

        }, 3000)

    }
}