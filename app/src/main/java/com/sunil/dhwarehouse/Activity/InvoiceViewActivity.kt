package com.sunil.dhwarehouse.Activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.sunil.dhwarehouse.BuildConfig
import com.sunil.dhwarehouse.MainActivity
import com.sunil.dhwarehouse.R
import com.sunil.dhwarehouse.RoomDB.InvoiceMaster
import com.sunil.dhwarehouse.RoomDB.MasterDatabase
import com.sunil.dhwarehouse.adapter.InvoiceViewAdapter
import com.sunil.dhwarehouse.common.UtilsFile
import com.sunil.dhwarehouse.databinding.ActivityInvoiceViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode

class InvoiceViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInvoiceViewBinding
    private val invoicesList = mutableListOf<InvoiceMaster>()
    private lateinit var invoiceViewAdapter: InvoiceViewAdapter
    private val REQUEST_WRITE_STORAGE = 112
    private var getUserName = ""
    private var medicalAddress = ""
    private var mobileNo = ""
    private var medicalName = ""
    var date = ""
    var time = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityInvoiceViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Request storage permissions
        //requestStoragePermissions()

        getUserName = intent.getStringExtra("getUserName").toString()
        medicalName = intent.getStringExtra("MedicalName").toString()
        medicalAddress = intent.getStringExtra("MedicalAddress").toString()
        mobileNo = intent.getStringExtra("MobileNo").toString()
        date = intent.getStringExtra("Date").toString()
        time = intent.getStringExtra("Time").toString()

        println("Date: $date")
        println("Time: $time")
        fetchAndUpdateData()

        binding.txtUsername.text = medicalName
        binding.txtDateTime.text = (date + " " + time)
        binding.txtMedicalAddress.text = medicalAddress
        binding.txtMedicalPhone.text = mobileNo

        binding.ivShare.setOnClickListener {
            saveCsvFile(false)
        }

        binding.ivWAShare.setOnClickListener {
            saveCsvFile(true)
        }

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        binding.ivFileDownload.setOnClickListener {
          //  startActivity(Intent(this@InvoiceViewActivity, InvoiceBilActivity::class.java))
          //  overridePendingTransition(R.anim.enter_animation, R.anim.exit_animation)
           // finish()
        }
    }

    private fun fetchAndUpdateData() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // Fetch data from the database
                val fetchedInvoices =
                    MasterDatabase.getDatabase(this@InvoiceViewActivity).invoiceDao()
                val invoices = fetchedInvoices.getInvoiceMaster().toMutableList()

                // Initialize invoicesList
                //invoicesList = mutableListOf<InvoiceMaster>()

                // Filter invoices based on account_name
                invoicesList.clear()
                for (item in invoices) {
                    if (item.account_name == medicalName && item.date == date && item.time == time) {
                        invoicesList.add(item)
                    }
                }

                // Update the adapter with the filtered list
                withContext(Dispatchers.Main) {
                    // Calculate totals
                    val totalItems = invoicesList.size
                    val totalAmount = invoicesList.sumOf { it.subTotal }
                    invoiceViewAdapter = InvoiceViewAdapter(
                        this@InvoiceViewActivity,
                        invoicesList,
                        totalItems,
                        UtilsFile().roundValues(totalAmount)
                    )
                    binding.rvInvoice.layoutManager = LinearLayoutManager(this@InvoiceViewActivity)
                    binding.rvInvoice.adapter = invoiceViewAdapter
                }
            } catch (e: Exception) {
                Log.e("TAG", "Error fetching invoices: ${e.message}")
            }
        }
    }

    private fun saveCsvFile(isWAShare: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // val invoices = getInvoicesFromDatabase()
                if (invoicesList.isNotEmpty()) {
                    val filePath = getCsvFilePath("invoices.csv")
                    val isSuccess = writeInvoicesToCsv(invoicesList, filePath)

                    withContext(Dispatchers.Main) {
                        if (isSuccess) {
                            notifyMediaScanner(filePath)
                            Log.e("saveCsvFile", "saveCsvFile: $filePath")
                            if (!isWAShare) {
                                shareFile(this@InvoiceViewActivity, filePath)
                            } else {
                               shareFileToWhatsApp(filePath)
                            }
                        } else {
                            Toast.makeText(
                                this@InvoiceViewActivity,
                                "Failed to save CSV file",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@InvoiceViewActivity,
                            "No invoices to export",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("saveCsvFile", "saveCsvFile:  ${e.message}")
                    Toast.makeText(
                        this@InvoiceViewActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                e.printStackTrace()
            }
        }
    }

    private fun getCsvFilePath(fileName: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use scoped storage on Android 10 and above
            val directory = getExternalFilesDir(null)
            File(directory, fileName).absolutePath
        } else {
            // Use external storage directory on Android 9 and below
            val directory = File(Environment.getExternalStorageDirectory(), "Invoices")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            File(directory, fileName).absolutePath
        }
    }

//    private fun getCsvFilePath(fileName: String): String {
//        val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//        val customFolder = File(downloadsDirectory, "MyAppFolder")
//
//        if (!customFolder.exists()) {
//            customFolder.mkdirs()
//        }
//
//        return File(customFolder, fileName).absolutePath
//    }


    private fun notifyMediaScanner(filePath: String) {
        val file = File(filePath)
        val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.provider", file)
        } else {
            Uri.fromFile(file)
        }

        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
    }

    fun shareFile(context: Context, filePath: String) {
        val file = File(filePath)
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, "$packageName.provider", file)
        } else {
            Uri.fromFile(file)
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share CSV File"))
    }

    fun shareFileToWhatsApp(filePath: String) {
        val file = File(filePath)
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(this@InvoiceViewActivity, "$packageName.provider", file)
        } else {
            Uri.fromFile(file)
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.whatsapp")
        }

        // Check if WhatsApp is installed
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this@InvoiceViewActivity, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun writeInvoicesToCsv(invoices: List<InvoiceMaster>, filePath: String): Boolean {
        return try {
            val file = File(filePath)
            file.bufferedWriter().use { out ->
                out.write("ID,Sales Name,Account Name,Address,Mobile No,Date,Time,Product Item Name,Qty,Free,SCM,Rate,SubTotal\n")
                for (invoice in invoices) {
                    out.write(
                        "${invoice.id},${invoice.salesName},${invoice.account_name},${invoice.address},${invoice.mobile_no},${invoice.date},${invoice.time}," +
                                "${invoice.productItemName},${invoice.qty},${invoice.free},${invoice.scm},${invoice.rate}," +
                                "${invoice.subTotal}\n"
                    )
                }
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private suspend fun getInvoicesFromDatabase(): List<InvoiceMaster> {
        val invoiceDao = MasterDatabase.getDatabase(this@InvoiceViewActivity).invoiceDao()
        return invoiceDao.getInvoiceMaster() // or use any appropriate method to fetch data
    }


    private fun requestStoragePermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE_STORAGE
            )
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@InvoiceViewActivity, MainActivity::class.java))
        UtilsFile.isFinishInvoice = true
        finish()
    }
}


