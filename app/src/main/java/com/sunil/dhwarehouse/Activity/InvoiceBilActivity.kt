package com.sunil.dhwarehouse.Activity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sunil.dhwarehouse.R
import com.sunil.dhwarehouse.RoomDB.InvoiceMaster
import com.sunil.dhwarehouse.RoomDB.MasterDatabase
import com.sunil.dhwarehouse.adapter.InvoiceViewAdapter
import com.sunil.dhwarehouse.databinding.ActivityInvoiceBilBinding
import com.sunil.dhwarehouse.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InvoiceBilActivity : AppCompatActivity() {
   private lateinit var binding : ActivityInvoiceBilBinding
    private val invoices = mutableListOf<InvoiceMaster>()
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

    }

    private fun fetchAndUpdateData() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // Fetch data from the database
                val fetchedInvoices =
                    MasterDatabase.getDatabase(this@InvoiceBilActivity).invoiceDao()

                withContext(Dispatchers.Main) {
                    invoices.clear()
                    invoices.addAll(fetchedInvoices.getInvoiceMaster())

//                    invoiceViewAdapter = InvoiceViewAdapter(this@InvoiceBilActivity, invoices)
//                    binding.rvInvoice.adapter = invoiceViewAdapter
                }
            } catch (e: Exception) {
                //   Log.e(TAG, "Error fetching invoices: ${e.message}")
            }
        }
    }
}