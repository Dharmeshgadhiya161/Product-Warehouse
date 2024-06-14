package com.sunil.dhwarehouse.Activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sunil.dhwarehouse.MainActivity
import com.sunil.dhwarehouse.R
import com.sunil.dhwarehouse.common.SharedPrefManager
import com.sunil.dhwarehouse.databinding.ActivityLoginUserBinding

class LoginUserActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginUserBinding
    lateinit var sharedPrefManager: SharedPrefManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginUserBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        sharedPrefManager = SharedPrefManager(this@LoginUserActivity)

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this@LoginUserActivity, MainActivity::class.java))
            finish()
            sharedPrefManager.getUserName="VK"
            sharedPrefManager.getFirstShow = true
        }

    }
}