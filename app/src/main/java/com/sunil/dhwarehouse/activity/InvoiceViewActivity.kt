package com.sunil.dhwarehouse.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sunil.dhwarehouse.BuildConfig
import com.sunil.dhwarehouse.MainActivity
import com.sunil.dhwarehouse.R
import com.sunil.dhwarehouse.roomDB.InvoiceMaster
import com.sunil.dhwarehouse.roomDB.ItemMaster
import com.sunil.dhwarehouse.roomDB.MasterDatabase
import com.sunil.dhwarehouse.adapter.InvoiceViewAdapter
import com.sunil.dhwarehouse.common.UtilsFile
import com.sunil.dhwarehouse.common.writeGroupedInvoicesToPdf
import com.sunil.dhwarehouse.common.writeInvoicesToCsv
import com.sunil.dhwarehouse.databinding.ActivityInvoiceViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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
    private var timeSecond = ""
    private var receivePayment:Double=0.0
    private lateinit var invoiceListBilAct: MutableList<InvoiceMaster>
    private var isInvoiceBilActivity: Boolean = false
    private lateinit var itemMasterList: MutableList<ItemMaster>
    private lateinit var invoiceColor: String
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

        isInvoiceBilActivity = intent.getBooleanExtra("isInvoiceBilActivity", false)
        itemMasterList = ArrayList()
        qtyItemDataUpdate()
        if (isInvoiceBilActivity) {
            invoiceListBilAct = (intent.getParcelableArrayListExtra<InvoiceMaster>("invoice_list")
                ?: emptyList()).toMutableList()
            Log.e("TAG", "isInvoiceBilActivity-->: ${invoiceListBilAct.size}")
            fetchDataInvoiceBilActivity()
        } else {

            getUserName = intent.getStringExtra("getUserName").toString()
            medicalName = intent.getStringExtra("MedicalName").toString()
            medicalAddress = intent.getStringExtra("MedicalAddress").toString()
            mobileNo = intent.getStringExtra("MobileNo").toString()
            date = intent.getStringExtra("Date").toString()
            time = intent.getStringExtra("Time").toString()
            timeSecond = intent.getStringExtra("TimeSecond").toString()
            receivePayment = intent.getDoubleExtra("receivePayment",0.0)

            println("Date: $date")
            println("Time: $time")
            fetchAndUpdateData()

            binding.txtUsername.text = medicalName
            binding.txtDateTime.text = (date + " " + time)
            binding.txtMedicalAddress.text = medicalAddress
            binding.txtMedicalPhone.text = mobileNo
            binding.edtPayReceive.text=receivePayment.toString()
        }
        binding.ivCSVShare.setOnClickListener {
            if (isInvoiceBilActivity) {
                saveCsvFile(invoiceListBilAct, false, medicalName, "_invoice.csv")
            } else {
                saveCsvFile(invoicesList, false, medicalName, "_invoice.csv")
            }
        }

        binding.ivPDFShare.setOnClickListener {
            if (isInvoiceBilActivity) {
                saveCsvFile(invoiceListBilAct, true, medicalName, "_invoice.pdf")
            } else {
                saveCsvFile(invoicesList, true, medicalName, "_invoice.pdf")
            }
        }

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        binding.ivHome.setOnClickListener {
            onBackPressed()
        }

        binding.ivFileDownload.setOnClickListener {
            //  startActivity(Intent(this@InvoiceViewActivity, InvoiceBilActivity::class.java))
            //  overridePendingTransition(R.anim.enter_animation, R.anim.exit_animation)
            // finish()

//                if (isInvoiceBilActivity){
//                    deleteLastItem(invoiceListBilAct)
//                }else{
//                    deleteLastItem(invoicesList)
//                }
        }
    }

    private fun fetchAndUpdateData() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // Fetch data from the database
                val fetchedInvoices =
                    MasterDatabase.getDatabase(this@InvoiceViewActivity).invoiceDao()
                val invoices = fetchedInvoices.getInvoiceMaster().toMutableList()

//                val fetchedInvoicesBil =
//                    MasterDatabase.getDatabase(this@InvoiceViewActivity).invoiceBilDao()
//                val invoicesBil = fetchedInvoicesBil.getInvoiceMaster().toMutableList()

                // Filter invoices based on account_name
                invoicesList.clear()
                // var invoiceBil: InvoiceBilMaster
                var sNo = 1
                //  val invoicesToInsert = mutableListOf<InvoiceBilMaster>()
                for (item in invoices) {
                    if (item.account_name == medicalName && item.date == date && item.timeSecond == timeSecond) {
                        item.no = sNo++
                        GlobalScope.launch(Dispatchers.IO) {
                            fetchedInvoices.updateItem(item)
                        }
                        invoicesList.add(item)
                    }
                }

                // Update the adapter with the filtered list
                withContext(Dispatchers.Main) {
                    // Calculate totals
                    val totalItems = invoicesList.sumOf { it.qty }
                    val totalFreeQty = invoicesList.sumOf { it.free }
                    val totalSch = invoicesList.sumOf { it.scm }
                    val totalAmount = invoicesList.sumOf { it.amount }
                    invoiceViewAdapter = InvoiceViewAdapter(
                        this@InvoiceViewActivity,
                        invoicesList,
                        totalItems,
                        UtilsFile().roundValues(totalSch),
                        UtilsFile().roundValues(totalAmount), totalFreeQty
                    )
                    binding.rvInvoice.layoutManager = LinearLayoutManager(this@InvoiceViewActivity)
                    binding.rvInvoice.adapter = invoiceViewAdapter
                }
            } catch (e: Exception) {
                Log.e("TAG", "Error fetching invoices: ${e.message}")
            }
        }
    }


    private fun fetchDataInvoiceBilActivity() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    for (item in invoiceListBilAct) {
                        medicalName = item.account_name
                        binding.txtUsername.text = item.account_name
                        binding.txtDateTime.text = (item.date + " " + item.time)
                        binding.txtMedicalAddress.text = item.address
                        binding.txtMedicalPhone.text = item.mobile_no
                        binding.edtPayReceive.text = item.receiveAmount.toString()
                    }

                    // Calculate totals
                    val totalItems = invoiceListBilAct.sumOf { it.qty }
                    val totalFreeQty = invoiceListBilAct.sumOf { it.free }
                    val totalSch = invoiceListBilAct.sumOf { it.scm }
                    val totalAmount = invoiceListBilAct.sumOf { it.amount }
                    invoiceViewAdapter = InvoiceViewAdapter(
                        this@InvoiceViewActivity,
                        invoiceListBilAct,
                        totalItems,
                        UtilsFile().roundValues(totalSch),
                        UtilsFile().roundValues(totalAmount),
                        totalFreeQty
                    )
                    binding.rvInvoice.layoutManager = LinearLayoutManager(this@InvoiceViewActivity)
                    binding.rvInvoice.adapter = invoiceViewAdapter
                }
            } catch (e: Exception) {
                Log.e("TAG", "Error fetching invoices: ${e.message}")
            }
        }
    }

    private fun saveCsvFile(
        invoicesList: MutableList<InvoiceMaster>,
        isPDFShare: Boolean,
        medicalName: String,
        fileExtension: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var isSuccess: Boolean
                if (invoicesList.isNotEmpty()) {
                    val filePath = getCsvFilePath(medicalName + fileExtension)
                    if (!isPDFShare) {
                        isSuccess = writeInvoicesToCsv(invoicesList, filePath)
                    } else {
                        // Group the invoices by the `no` (invoice number)
                        val groupedInvoices = invoicesList.groupBy { it.no }
                        isSuccess = writeGroupedInvoicesToPdf(
                            this@InvoiceViewActivity,
                            groupedInvoices,
                            filePath,
                            invoicesList
                        )
                    }
                    withContext(Dispatchers.Main) {
                        if (isSuccess) {
                            notifyMediaScanner(filePath)
                            Log.e("saveCsvFile", "saveCsvFile: $filePath")
                            if (!isPDFShare) {
                                shareFile(this@InvoiceViewActivity, filePath)
                            } else {
                                sharePdfFile(filePath)
                            }
                        } else {
                            Toast.makeText(
                                this@InvoiceViewActivity,
                                "Failed to save file",
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


//    private fun writeInvoicesToCsv(invoices: List<InvoiceMaster>, filePath: String): Boolean {
//        return try {
//            val file = File(filePath)
//            file.bufferedWriter().use { out ->
//                out.write("No,Sales Name,Account Name,Address,Mobile No,Date,Time,Product Item Name,Qty,Free,SCM,Rate,SubTotal\n")
//                for (invoice in invoices) {
//                    out.write(
//                        "${invoice.no},${invoice.salesName},${invoice.account_name},${invoice.address},${invoice.mobile_no},${invoice.date},${invoice.time}," +
//                                "${invoice.productItemName},${invoice.qty},${invoice.free},${invoice.scm},${invoice.rate}," +
//                                "${invoice.amount}\n"
//                    )
//                }
//            }
//            true
//        } catch (e: IOException) {
//            e.printStackTrace()
//            false
//        }
//    }

    private fun sharePdfFile(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            // Create a URI for the file using FileProvider
            val uri: Uri = FileProvider.getUriForFile(
                this@InvoiceViewActivity,
                "$packageName.provider", // Replace with your package name
                file
            )

            // Create a sharing intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Start the sharing intent
            startActivity(Intent.createChooser(shareIntent, "Share PDF using"))
        } else {
            // Handle the case where the file does not exist
            Toast.makeText(this@InvoiceViewActivity, "File not found", Toast.LENGTH_SHORT).show()
        }
    }


//    private fun deleteLastItem(invoiceListBilAct: MutableList<InvoiceMaster>) {
//
//
//        if (invoiceListBilAct.isNotEmpty()) {
//            val position = invoiceListBilAct.size - 1 // Get the position of the last item
//            val itemToDelete = invoiceListBilAct[position]
//
//            GlobalScope.launch(Dispatchers.IO) {
//                try {
//                    // Delete the item from the Room database
//                    MasterDatabase.getDatabase(this@InvoiceViewActivity).invoiceDao().deleteInvoicesList(itemToDelete)
//
//                    // Update the UI on the main thread
//                    withContext(Dispatchers.Main) {
//                        invoiceListBilAct.removeAt(position)
//                        invoiceViewAdapter.notifyItemRemoved(position)
//                        invoiceViewAdapter.notifyItemRangeChanged(position, invoiceListBilAct.size)
//
//                        Toast.makeText(this@InvoiceViewActivity, "Last item deleted", Toast.LENGTH_SHORT).show()
//
//
//                    }
//                } catch (e: Exception) {
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(this@InvoiceViewActivity, "Error deleting item: ${e.message}", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        } else {
//            Toast.makeText(this, "No items to delete", Toast.LENGTH_SHORT).show()
//        }
//    }


    override fun onBackPressed() {
        super.onBackPressed()
        if (isInvoiceBilActivity) {
            finish()
        } else {
            startActivity(Intent(this@InvoiceViewActivity, MainActivity::class.java))
            UtilsFile.isFinishInvoice = true
            finish()
        }
    }

    private fun qtyItemDataUpdate() {
        lifecycleScope.launch(Dispatchers.IO) {

            val accountDao1 = MasterDatabase.getDatabase(this@InvoiceViewActivity).itemDao()
            itemMasterList = accountDao1.getItemMaster().toMutableList()

            withContext(Dispatchers.Main) {
                if (itemMasterList.size > 0) {
                    for (item in itemMasterList) {
                        GlobalScope.launch(Dispatchers.IO) {
                            item.stock_qty = item.old_stockQty
                            accountDao1.updateItem(item)
                            //   Log.d("UpdateItem", "Updating item at index $item with stock_qty: ${item.stock_qty}")
                        }
                    }
                }
            }
        }
    }


}


