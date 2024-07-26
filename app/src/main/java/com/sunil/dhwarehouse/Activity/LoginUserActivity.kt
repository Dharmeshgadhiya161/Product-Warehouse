package com.sunil.dhwarehouse.Activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sunil.dhwarehouse.MainActivity
import com.sunil.dhwarehouse.R
import com.sunil.dhwarehouse.RoomDB.AccountMaster
import com.sunil.dhwarehouse.RoomDB.ItemMaster
import com.sunil.dhwarehouse.RoomDB.MasterDatabase
import com.sunil.dhwarehouse.common.DialogUtil
import com.sunil.dhwarehouse.common.NetworkUtil
import com.sunil.dhwarehouse.common.SharedPrefManager
import com.sunil.dhwarehouse.common.ShowingDialog
import com.sunil.dhwarehouse.databinding.ActivityLoginUserBinding
import com.sunil.dhwarehouse.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File

class LoginUserActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginUserBinding
    lateinit var sharedPrefManager: SharedPrefManager
    val storage_permission_code = 1000
    val fileRequestCode = 10
    var fileType: Int = 0
    private lateinit var aryAccount1: MutableList<AccountMaster>
    lateinit var dialog: Dialog
    lateinit var showingDialog: ShowingDialog
    private lateinit var storageReference: StorageReference
    private lateinit var deleteLocalFile: File
    var TAG = "LoginUserActivity"
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
        storageReference = FirebaseStorage.getInstance().reference
        sharedPrefManager = SharedPrefManager(this@LoginUserActivity)
        aryAccount1 = ArrayList()
        dialog = Dialog(this)
        setProgressShowDialog(this, "Loading.. Excel File!")

        binding.btnLogin.setOnClickListener {

            val inputText = binding.edtUserName.text.toString()
            if (inputText.isEmpty()) {
                binding.filledTextField.error = "Please Enter Your Username cannot be empty"
            } else {
                binding.filledTextField.error = null // Clear the error

                sharedPrefManager.getUserName =
                    binding.filledTextField.editText?.getText().toString()

                if (!sharedPrefManager.isExcelFileShowing) {

                    if (checkPermission()) {
                        if (NetworkUtil.isInternetAvailable(this)) {
                            downloadAccountMasterFile()
                        } else {
                            DialogUtil.showNoInternetDialog(this)
                        }
                    } else {
                        requestPermission()
                    }
                    //showAddExcelFileDialog(dialog)
                }
            }
        }

    }

    /*====Excel File Code=========*/
    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), storage_permission_code
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == storage_permission_code) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, proceed with file selection
                //  openFilePicker()
                if (NetworkUtil.isInternetAvailable(this)) {
                    downloadAccountMasterFile()
                } else {
                    DialogUtil.showNoInternetDialog(this)
                }
            } else {
                // Permission denied
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun downloadAccountMasterFile() {
        val fileReference = storageReference.child("account master.xlsx")
        val localFile = getLocalFilePath("account_master.xlsx")
        deleteLocalFile = localFile
        Log.d(TAG, "Starting download from: ${fileReference.path}")

        fileReference.getFile(localFile).addOnSuccessListener {
            Log.d(TAG, "File downloaded successfully: ${localFile.absolutePath}")
            showDialog(this)
            // Check if the file is not empty
            if (localFile.length() == 0L) {
                Log.e(TAG, "Downloaded file is empty.")
                return@addOnSuccessListener
            }
            val uri = Uri.fromFile(localFile)

            CoroutineScope(Dispatchers.IO).launch {
                val data = readExcelAccountMasterFile(uri)
                Log.e(TAG, "reading Excel file{${data.size}}")
                if (data.size != 0) {

                    val accountDao =
                        MasterDatabase.getDatabase(this@LoginUserActivity).accountDao()

                    accountDao.deleteAllAccounts()
                    data.forEach { row ->
                        accountDao.insert(row)
                    }
                    aryAccount1 = accountDao.getAccountMaster()
                    Log.d(TAG, "Account Master come to add : " + data.size)
                    Log.d(
                        TAG, "Account Master come to add aryAccount1 : " + aryAccount1.size
                    )
                    sharedPrefManager.isAccountExcelRead = true
                    dismissDialog(this@LoginUserActivity)
                    lifecycleScope.launch(Dispatchers.IO) {
                        Thread.sleep(200)
                        downloadItemMasterFile()
                    }
                    // Delete the file after reading
                    //  deleteLocalFile(loca3lFile, "account_master.xlsx")
                } else {
                    Log.e("Master RoomDB", "UserName not mach")

                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                    sharedPrefManager.isAccountExcelRead = false
                    sharedPrefManager.isExcelFileShowing = false
                    sharedPrefManager.getFirstShow = false
                    dismissDialog(this@LoginUserActivity)

                    lifecycleScope.launch(Dispatchers.IO) {
                        Thread.sleep(500)

                        runOnUiThread {
                            binding.filledTextField.error =
                                "Username cannot be match please try again.!"
                        }
                    }
                }
            }

        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error downloading file", exception)
            Toast.makeText(this, "Does not have permission downloading file", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private suspend fun readExcelAccountMasterFile(uri: Uri): ArrayList<AccountMaster> {
        return withContext(Dispatchers.IO) {
            Log.d("Excel Data", "Account Master Start1")

            var aryAccount: ArrayList<AccountMaster> = ArrayList<AccountMaster>()

            val inputSteam = contentResolver.openInputStream(uri)
            val workbook = XSSFWorkbook(inputSteam)
            //val workbook = WorkbookFactory.create(inputSteam)
            val sheet = workbook.getSheetAt(0)
            val iterator = sheet.iterator()

            var isFirst: Boolean = true
            while (iterator.hasNext()) {

                var accountMaster: AccountMaster = AccountMaster()
                val row = iterator.next()
                val cellIterator = row.cellIterator()

                if (isFirst) {
                    isFirst = false
                } else {
                    var index: Int = 0
                    while (cellIterator.hasNext()) {
                        val cell = cellIterator.next()
                        if (cell.cellType != CellType.BLANK && cell.cellType != CellType.FORMULA) {
                            if (index == 0)
                                accountMaster.sales_man = cell.stringCellValue
                            else if (index == 1) {
                                if (cell.cellType == CellType.STRING) {
                                    accountMaster.account_name = cell.stringCellValue
                                }
                            } else if (index == 2)
                                accountMaster.addess = cell.stringCellValue
                            else if (index == 3)
                                accountMaster.mobile_no = cell.stringCellValue
                            else if (index == 4)
                                accountMaster.opening_balance = cell.numericCellValue
                            else if (index == 5)
                                accountMaster.account_type = cell.stringCellValue
                            else if (index == 6)
                                accountMaster.party_type = cell.stringCellValue
                            else if (index == 7)
                                accountMaster.day = cell.stringCellValue

                            index++
                        }
                    }

                    if (sharedPrefManager.getUserName == accountMaster.sales_man) {
                        aryAccount.add(accountMaster)
                        Log.e("readExcel", "readExcelAccountMasterFile: ${aryAccount.size}")
                    }
                }
            }
            workbook.close()
            inputSteam?.close()

            return@withContext aryAccount
        }
    }


    private fun downloadItemMasterFile() {
        val fileReference = storageReference.child("stock leger  margine.xlsx")
        val localFile = getLocalFilePath("stock_leger_margine.xlsx")
        deleteLocalFile = localFile
        Log.d(TAG, "Starting download from: ${fileReference.path}")

        fileReference.getFile(localFile).addOnSuccessListener {
            Log.d(TAG, "File downloaded successfully: ${localFile.absolutePath}")
            showDialog(this)
            // Check if the file is not empty
            if (localFile.length() == 0L) {
                Log.e(TAG, "Downloaded file is empty.")
                return@addOnSuccessListener
            }
            val uri = Uri.fromFile(localFile)

            CoroutineScope(Dispatchers.IO).launch {
                var itemDao = MasterDatabase.getDatabase(this@LoginUserActivity).itemDao()
                val data = readExcelItemMasterFile(uri)
                Log.d(TAG, "Item Master come to add : " + data.size)
                itemDao.deleteAllItems()
                data.forEach { row ->
                    itemDao.insert(row)
                }
                dismissDialog(this@LoginUserActivity)
                sharedPrefManager.isItemExcelRead = true
                sharedPrefManager.isExcelFileShowing = true
                sharedPrefManager.getFirstShow = true

                deleteLocalFile(localFile,"stock_leger_margine.xlsx")

                startActivity(Intent(this@LoginUserActivity, MainActivity::class.java))
                overridePendingTransition(R.anim.enter_animation, R.anim.exit_animation)

                finish()

                Log.d(TAG, "Item Master data added.")


            }

        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error downloading file", exception)
        }
    }


    private suspend fun readExcelItemMasterFile(uri: Uri): ArrayList<ItemMaster> {
        return withContext(Dispatchers.IO) {
            Log.d(TAG, "Item Master Start1")

            var aryItems: ArrayList<ItemMaster> = ArrayList<ItemMaster>()

            val inputSteam = contentResolver.openInputStream(uri)
            val workbook = XSSFWorkbook(inputSteam)
            //val workbook = WorkbookFactory.create(inputSteam)
            val sheet = workbook.getSheetAt(0)
            val iterator = sheet.iterator()

            var isFirst: Boolean = true
            while (iterator.hasNext()) {

                var itemMaster: ItemMaster = ItemMaster()
                val row = iterator.next()
                val cellIterator = row.cellIterator()

                if (isFirst) {
                    isFirst = false
                } else {

                    var index: Int = 0
                    while (cellIterator.hasNext()) {
                        val cell = cellIterator.next()

                        if (cell.cellType != CellType.BLANK && cell.cellType != CellType.FORMULA) {

                            if (index == 0)
                                itemMaster.category = cell.stringCellValue
                            else if (index == 1) {
                                itemMaster.company_name = cell.stringCellValue
                            } else if (index == 2)
                                itemMaster.brand = cell.stringCellValue
                            else if (index == 3) {
                                if (cell.cellType == CellType.STRING) {
                                    itemMaster.item_name = cell.stringCellValue
                                }
                            } else if (index == 4)
                                itemMaster.mrp = cell.numericCellValue
                            else if (index == 5)
                                itemMaster.purchase_rate = cell.numericCellValue
                            else if (index == 6)
                                itemMaster.sale_rate = cell.numericCellValue
                            else if (index == 7)
                                itemMaster.margin = cell.numericCellValue
                            else if (index == 8)
                                itemMaster.stock_qty = cell.numericCellValue
                            else if (index == 9)
                                itemMaster.stock_amount = cell.numericCellValue
                            else if (index == 10)
                                itemMaster.edtxt_qty = cell.numericCellValue
                            else if (index == 11)
                                itemMaster.edtxt_free = cell.numericCellValue
                            else if (index == 12)
                                itemMaster.edtxt_scm = cell.numericCellValue
                            else if (index == 13)
                                itemMaster.txt_subTotal = cell.numericCellValue

                            index++
                        }
                    }
                    aryItems.add(itemMaster)
                }
            }
            workbook.close()
            inputSteam?.close()

            aryItems
        }
    }


    private fun showAddExcelFileDialog(dialog: Dialog) {

        dialog.setContentView(R.layout.dialog_add_excel_show)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()

        dialog.findViewById<TextView>(R.id.btnAccountExcel).setOnClickListener {
            fileType = 1
            if (checkPermission()) {
                if (NetworkUtil.isInternetAvailable(this)) {
                    downloadAccountMasterFile()
                } else {
                    DialogUtil.showNoInternetDialog(this)
                }
            } else {
                requestPermission()
            }
        }
        dialog.findViewById<TextView>(R.id.btnItemExcel).setOnClickListener {
            fileType = 2
            if (checkPermission()) {
                downloadItemMasterFile()
            } else {
                requestPermission()
            }
        }

    }

    private fun setProgressShowDialog(context: Activity, string: String) {
        showingDialog = ShowingDialog(context, string)
        showingDialog.setCanceledOnTouchOutside(false)
        showingDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun showDialog(context: Activity) {
        if (!context.isFinishing) {
            showingDialog.show()
        }
    }

    private fun dismissDialog(context: Activity) {
        if (!context.isFinishing && showingDialog.isShowing) {
            showingDialog.cancel()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissDialog(this)
    }


    private fun getLocalFilePath(fileName: String): File {
        // Get the directory for the app's private files
        val directory = File(filesDir, "my_excel_files")//folderName
        if (!directory.exists()) {
            directory.mkdirs() // Create the directory if it does not exist
        }
        return File(directory, fileName)
    }


    private fun deleteLocalFile(file: File, expectedFileName: String) {
        if (file.exists()) {
            if (file.name == expectedFileName) {
                if (file.delete()) {
                    Log.d(TAG, "File deleted successfully: ${file.absolutePath}")
                } else {
                    Log.e(TAG, "Failed to delete file: ${file.absolutePath}")
                }
            } else {
                Log.e(
                    TAG,
                    "File name does not match expected file name: ${file.name} vs $expectedFileName"
                )
            }
        } else {
            Log.e(TAG, "File does not exist: ${file.absolutePath}")
        }
    }


    //    private fun openFilePicker() {
//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
//            this.addCategory(Intent.CATEGORY_OPENABLE)
//            this.type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
//        }
//        startActivityForResult(intent, fileRequestCode)
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        Log.d("Excel Data", "Come Result")
//        if (requestCode == fileRequestCode && resultCode == Activity.RESULT_OK) {
//            Log.d("Excel Data", "Come Result ok")
//            data?.data?.also { uri ->
//                Log.d("Excel Data", "Start")
//                showDialog(this)
//                CoroutineScope(Dispatchers.IO).launch {
//
//                    if (fileType == 1) {
//
//                        val data = readExcelAccountMasterFile(uri)
//                        if (data.size != 0) {
//
////                            val accountDao =
////                                MasterDatabase.getDatabase(this@LoginUserActivity).accountDao()
////
////                            accountDao.deleteAllAccounts()
////                            data.forEach { row ->
////                                accountDao.insert(row)
////                            }
////                            aryAccount1 = accountDao.getAccountMaster()
////                            Log.d("Master RoomDB", "Account Master come to add : " + data.size)
////                            Log.d(
////                                "Master RoomDB aryAccount1",
////                                "Account Master come to add aryAccount1 : " + aryAccount1.size
////                            )
////                            sharedPrefManager.isAccountExcelRead = true
////                            dismissDialog(this@LoginUserActivity)
//                        } else {
//                            Log.e("Master RoomDB", "UserName not mach")
//
//                            if (dialog.isShowing) {
//                                dialog.dismiss()
//                            }
//                            sharedPrefManager.isAccountExcelRead = false
//                            sharedPrefManager.isExcelFileShowing = false
//                            sharedPrefManager.getFirstShow = false
//                            dismissDialog(this@LoginUserActivity)
//
//                            lifecycleScope.launch(Dispatchers.IO) {
//                                Thread.sleep(500)
//
//                                runOnUiThread {
//                                    binding.filledTextField.error =
//                                        "Username cannot be match please try again.!"
//                                }
//                            }
//                        }
//                    } else {
////                        var itemDao = MasterDatabase.getDatabase(this@LoginUserActivity).itemDao()
////                        val data = readExcelItemMasterFile(uri)
////                        Log.d("Master RoomDB", "Item Master come to add : " + data.size)
////                        itemDao.deleteAllItems()
////                        data.forEach { row ->
////                            itemDao.insert(row)
////                        }
////                        sharedPrefManager.isItemExcelRead = true
////
////                        if (dialog.isShowing) {
////                            dialog.dismiss()
////                            sharedPrefManager.isExcelFileShowing = true
////                            sharedPrefManager.getFirstShow = true
////
////                            startActivity(Intent(this@LoginUserActivity, MainActivity::class.java))
////                            finish()
////                        }
////                        Log.d("Master RoomDB", "Item Master data added.")
////
////                        dismissDialog(this@LoginUserActivity)
//                    }
//                }
//            }
//        }
//    }
}