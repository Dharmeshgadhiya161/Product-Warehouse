package com.sunil.dhwarehouse

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sunil.dhwarehouse.Activity.InvoiceBilActivity
import com.sunil.dhwarehouse.RoomDB.AccountMaster
import com.sunil.dhwarehouse.RoomDB.ItemMaster
import com.sunil.dhwarehouse.RoomDB.MasterDatabase
import com.sunil.dhwarehouse.adapter.AccountDataAdapter
import com.sunil.dhwarehouse.common.DialogUtil
import com.sunil.dhwarehouse.common.ExcelFileHandler
import com.sunil.dhwarehouse.common.NetworkUtil
import com.sunil.dhwarehouse.common.SharedPrefManager
import com.sunil.dhwarehouse.common.ShowingDialog
import com.sunil.dhwarehouse.common.UtilsFile
import com.sunil.dhwarehouse.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var aryAccount1: MutableList<AccountMaster>
    private lateinit var dayWiseAryAccount1: MutableList<AccountMaster>
    private lateinit var aryNewListAccount: MutableList<AccountMaster>
    private lateinit var showingDialog: ShowingDialog
    lateinit var accountDataAdapter: AccountDataAdapter
    private lateinit var filteredListMain: MutableList<AccountMaster>
    private lateinit var itemMasterList: MutableList<ItemMaster>

    lateinit var dialog: Dialog
    private lateinit var storageReference: StorageReference

    var TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        sharedPrefManager = SharedPrefManager(this@MainActivity)

        storageReference = FirebaseStorage.getInstance().reference
        dialog = Dialog(this)
        setProgressShowDialog(this, "Loading.. Excel File!")

        aryAccount1 = ArrayList()
        dayWiseAryAccount1 = ArrayList()
        aryNewListAccount = ArrayList()
        filteredListMain = ArrayList()
        itemMasterList = ArrayList()
        setDataLoad("All")
        itemDataListClear()
        setTabSelect()

        binding.txtUsername.text = sharedPrefManager.getUserName


        binding.searchView.setOnQueryTextListener { query ->
            filterAccounts(query)
        }

        binding.ivExcelFile.setOnClickListener {
            showAddExcelFileDialog(dialog)
        }

        binding.ivInvoiceClick.setOnClickListener {
            startActivity(Intent(this@MainActivity, InvoiceBilActivity::class.java))
            overridePendingTransition(R.anim.enter_animation, R.anim.exit_animation)
        }
    }


    private fun updateSelection(selectedDay: String) {
        val days = listOf(
            binding.txtAll to "All",
            binding.txtMon to "MONDAY",
            binding.txtTue to "TUESDAY",
            binding.txtWed to "WEDNESDAY",
            binding.txtThu to "THURSDAY",
            binding.txtFri to "FRIDAY",
            binding.txtSat to "SATURDAY",
            binding.txtSun to "SUNDAY"
        )

        for ((textView, day) in days) {
            if (day == selectedDay) {
                textView.background = getDrawable(R.drawable.bg_select_rectangle)
                textView.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                textView.background = getDrawable(R.drawable.bg_un_select_rectangle)
                textView.setTextColor(ContextCompat.getColor(this, R.color.sub_text))
            }
        }

        setDataLoad(selectedDay)
    }


    private fun setTabSelect() {
        binding.txtAll.setOnClickListener {
            updateSelection("All")
        }

        binding.txtMon.setOnClickListener {
            updateSelection("MONDAY")
        }

        binding.txtTue.setOnClickListener {
            updateSelection("TUESDAY")
        }

        binding.txtWed.setOnClickListener {
            updateSelection("WEDNESDAY")
        }

        binding.txtThu.setOnClickListener {
            updateSelection("THURSDAY")
        }

        binding.txtFri.setOnClickListener {
            updateSelection("FRIDAY")
        }

        binding.txtSat.setOnClickListener {
            updateSelection("SATURDAY")
        }

        binding.txtSun.setOnClickListener {
            updateSelection("SUNDAY")
        }
    }


    private fun filterAccounts(query: String) {
        filteredListMain = dayWiseAryAccount1.filter {
            it.account_name.contains(query, ignoreCase = true) || it.mobile_no.contains(
                query, ignoreCase = true
            )
        }.toMutableList()
        accountDataAdapter.updateData(filteredListMain, query)
    }

    private fun setDataLoad(dayName: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val accountDao = MasterDatabase.getDatabase(this@MainActivity).accountDao()
            aryAccount1 = accountDao.getAccountMaster()

            if (dayName == "All") {
                dayWiseAryAccount1.clear()
                for (item in aryAccount1) {
                    dayWiseAryAccount1.add(item)
                }
            } else {
                dayWiseAryAccount1.clear()
                for (item in aryAccount1) {
                    if (item.day == dayName) {
                        dayWiseAryAccount1.add(item)
                    }
                }
            }

            withContext(Dispatchers.Main) {
                if (dayWiseAryAccount1.size != 0) {
                    binding.rvAllAccount.visibility = View.VISIBLE
                    binding.layoutNoData.constNoDataLay.visibility = View.GONE
                    accountDataAdapter = AccountDataAdapter(this@MainActivity, dayWiseAryAccount1)
                    binding.rvAllAccount.layoutManager = LinearLayoutManager(this@MainActivity)
                    binding.rvAllAccount.adapter = accountDataAdapter
                    if (showingDialog.isShowing) {
                        showingDialog.dismiss()
                    }
                } else {
                    binding.rvAllAccount.visibility = View.GONE
                    binding.layoutNoData.constNoDataLay.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun itemDataListClear() {
        lifecycleScope.launch(Dispatchers.IO) {

            val accountDao1 = MasterDatabase.getDatabase(this@MainActivity).itemDao()
            itemMasterList = accountDao1.getItemMaster().toMutableList()

            withContext(Dispatchers.Main) {
                if (itemMasterList.size > 0) {
                    for (item in itemMasterList) {
                        GlobalScope.launch(Dispatchers.IO) {
                            item.edtxt_qty = 0.0
                            item.edtxt_free = 0.0
                            item.edtxt_scm = 0.0
                            item.txt_net_rate = 0.0
                            item.txt_subTotal = 0.0
                            item.margin = item.old_margin
                            accountDao1.updateItem(item)
                        }
                    }
                }
            }
        }
    }


    private fun setProgressShowDialog(context: Activity, msg: String) {
        showingDialog = ShowingDialog(context, msg)
        showingDialog.setCanceledOnTouchOutside(false)
        showingDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }


    private fun showAddExcelFileDialog(dialog: Dialog) {

        dialog.setContentView(R.layout.dialog_change_excel)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()

        dialog.findViewById<TextView>(R.id.btnAccountExcel).setOnClickListener {
            if (NetworkUtil.isInternetAvailable(this)) {
                downloadAccountMasterFile()
            } else {
                DialogUtil.showNoInternetDialog(this)
            }
        }

        dialog.findViewById<TextView>(R.id.btnItemExcel).setOnClickListener {
            if (NetworkUtil.isInternetAvailable(this)) {
                downloadItemMasterFile()
            } else {
                DialogUtil.showNoInternetDialog(this)
            }
        }

    }


    private fun downloadAccountMasterFile() {
        val fileReference = storageReference.child(UtilsFile.fileExcelAccount)
        val localFile = UtilsFile().getLocalFilePath(this, UtilsFile.localSaveAccountFileName)
        Log.d(TAG, "Starting download from: ${fileReference.path}")

        fileReference.getFile(localFile).addOnSuccessListener {
            Log.d(TAG, "File downloaded successfully: ${localFile.absolutePath}")

            showingDialog.show()

            if (localFile.length() == 0L) {
                Log.e(TAG, "Downloaded file is empty.")
                return@addOnSuccessListener
            }
            val uri = Uri.fromFile(localFile)

            CoroutineScope(Dispatchers.IO).launch {
                val data = ExcelFileHandler(
                    this@MainActivity,
                    sharedPrefManager
                ).readExcelAccountMasterFile(uri)
                Log.e(TAG, "reading Excel file{${data.size}}")
                if (data.size != 0) {
                    val accountDao =
                        MasterDatabase.getDatabase(this@MainActivity).accountDao()

                    accountDao.deleteAllAccounts()
                    aryAccount1.clear()
                    data.forEach { row ->
                        accountDao.insert(row)
                    }
                    dayWiseAryAccount1.clear()
                    aryAccount1 = accountDao.getAccountMaster()
                    Log.d(TAG, "Account Master come to add : " + data.size)
                    Log.d(TAG, "Account Master come to add aryAccount1 : " + aryAccount1.size)


                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                    lifecycleScope.launch(Dispatchers.IO) {
                        Thread.sleep(300)
                        setDataLoad("All")
                        UtilsFile().deleteLocalFile(localFile, UtilsFile.localSaveAccountFileName)
                    }

                }
            }

        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error downloading file", exception)
            Toast.makeText(this, "Does not have permission downloading file", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun downloadItemMasterFile() {
        val fileReference = storageReference.child(UtilsFile.fileExcelItem)
        val localFile = UtilsFile().getLocalFilePath(this, UtilsFile.localSaveItemFileName)
        Log.d(TAG, "Starting download from: ${fileReference.path}")

        fileReference.getFile(localFile).addOnSuccessListener {
            Log.d(TAG, "File downloaded successfully: ${localFile.absolutePath}")
            if (dialog.isShowing) {
                dialog.dismiss()
            }
            showingDialog.show()
            // Check if the file is not empty
            if (localFile.length() == 0L) {
                Log.e(TAG, "Downloaded file is empty.")
                return@addOnSuccessListener
            }
            val uri = Uri.fromFile(localFile)

            CoroutineScope(Dispatchers.IO).launch {
                var itemDao = MasterDatabase.getDatabase(this@MainActivity).itemDao()
                val data = ExcelFileHandler(
                    this@MainActivity,
                    sharedPrefManager
                ).readExcelItemMasterFile(uri)
                Log.d(TAG, "Item Master come to add : " + data.size)
                itemDao.deleteAllItems()
                data.forEach { row ->
                    itemDao.insert(row)
                }
                Thread.sleep(300)
                UtilsFile().deleteLocalFile(localFile, UtilsFile.localSaveItemFileName)
                Log.d(TAG, "Item Master data added.")
                showingDialog.dismiss()
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this@MainActivity, "Successfully Update!", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error downloading file", exception)
        }
    }
}