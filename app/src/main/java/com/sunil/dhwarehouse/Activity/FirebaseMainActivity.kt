package com.sunil.dhwarehouse.Activity

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sunil.dhwarehouse.R
import com.sunil.dhwarehouse.RoomDB.AccountMaster
import com.sunil.dhwarehouse.RoomDB.MasterDatabase
import com.sunil.dhwarehouse.common.SharedPrefManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class FirebaseMainActivity : AppCompatActivity() {

    private lateinit var storageReference: StorageReference
    private lateinit var deleteLocalFile: File
    lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var aryAccount1: MutableList<AccountMaster>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fire_base_main)

        sharedPrefManager = SharedPrefManager(this@FirebaseMainActivity)
        storageReference = FirebaseStorage.getInstance().reference
        aryAccount1 = ArrayList()

        sharedPrefManager.getUserName = "VK"
        findViewById<Button>(R.id.readButton).setOnClickListener {
            downloadAndReadExcelFile()
        }
    }

    private fun downloadAndReadExcelFile() {
        val fileReference = storageReference.child("account master.xlsx")
        //val fileReference = storageReference.child("stock reg.xlsx")
        val localFile = getLocalFilePath("account_master.xlsx")
        deleteLocalFile = localFile
        Log.d("MainActivity", "Starting download from: ${fileReference.path}")

        fileReference.getFile(localFile).addOnSuccessListener {
            Log.d("MainActivity", "File downloaded successfully: ${localFile.absolutePath}")

            // Check if the file is not empty
            if (localFile.length() == 0L) {
                Log.e("MainActivity", "Downloaded file is empty.")
                return@addOnSuccessListener
            }
            val uri = Uri.fromFile(localFile)

            CoroutineScope(Dispatchers.IO).launch {
                val data = readExcelAccountMasterFile(uri)
                Log.e("MainActivity", "reading Excel file{${data.size}}")
                if (data.size != 0) {

                    val accountDao =
                        MasterDatabase.getDatabase(this@FirebaseMainActivity).accountDao()

                    accountDao.deleteAllAccounts()
                    data.forEach { row ->
                        accountDao.insert(row)
                    }
                    aryAccount1 = accountDao.getAccountMaster()
                    Log.d("Master RoomDB", "Account Master come to add : " + data.size)
                    Log.d(
                        "Master RoomDB aryAccount1",
                        "Account Master come to add aryAccount1 : " + aryAccount1.size
                    )
                    sharedPrefManager.isAccountExcelRead = true
                    //dismissDialog(this@LoginUserActivity)
                    // Delete the file after reading
                  //  deleteLocalFile(localFile, "account_master.xlsx")
                }
            }

        }.addOnFailureListener { exception ->
            Log.e("MainActivity", "Error downloading file", exception)
        }
    }


    private suspend fun readExcelAccountMasterFile(uri: Uri): ArrayList<AccountMaster> {
        return withContext(Dispatchers.IO) {
            Log.d("Excel Data", "Account Master Start1")

            var aryAccount: ArrayList<AccountMaster> = ArrayList<AccountMaster>()

            val inputSteam = contentResolver.openInputStream(uri)
            val workbook = XSSFWorkbook(inputSteam)
            val sheet = workbook.getSheetAt(0)

            val iterator = sheet.iterator()

            var isFirst = true
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
                    Log.d("MainActivity", "File deleted successfully: ${file.absolutePath}")
                } else {
                    Log.e("MainActivity", "Failed to delete file: ${file.absolutePath}")
                }
            } else {
                Log.e(
                    "MainActivity",
                    "File name does not match expected file name: ${file.name} vs $expectedFileName"
                )
            }
        } else {
            Log.e("MainActivity", "File does not exist: ${file.absolutePath}")
        }
    }


}
