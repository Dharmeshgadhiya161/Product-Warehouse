package com.sunil.dhwarehouse.Activity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sunil.dhwarehouse.R
import com.sunil.dhwarehouse.databinding.ActivityInvoiceBilBinding
import com.sunil.dhwarehouse.databinding.ActivityMainBinding

class InvoiceBilActivity : AppCompatActivity() {
   private lateinit var binding : ActivityInvoiceBilBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityInvoiceBilBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.layoutNoData.constNoDataLay.visibility = View.VISIBLE
    }
}