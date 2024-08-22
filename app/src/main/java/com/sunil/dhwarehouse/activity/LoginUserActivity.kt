package com.sunil.dhwarehouse.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sunil.dhwarehouse.MainActivity
import com.sunil.dhwarehouse.R
import com.sunil.dhwarehouse.roomDB.AccountMaster
import com.sunil.dhwarehouse.roomDB.MasterDatabase
import com.sunil.dhwarehouse.common.DialogUtil
import com.sunil.dhwarehouse.common.ExcelFileHandler
import com.sunil.dhwarehouse.common.NetworkUtil
import com.sunil.dhwarehouse.common.SharedPrefManager
import com.sunil.dhwarehouse.common.ShowingDialog
import com.sunil.dhwarehouse.common.UtilsFile
import com.sunil.dhwarehouse.common.UtilsFile.Companion.fileExcelAccount
import com.sunil.dhwarehouse.common.UtilsFile.Companion.fileExcelItem
import com.sunil.dhwarehouse.common.UtilsFile.Companion.localSaveAccountFileName
import com.sunil.dhwarehouse.common.UtilsFile.Companion.localSaveItemFileName
import com.sunil.dhwarehouse.databinding.ActivityLoginUserBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class LoginUserActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginUserBinding
    private lateinit var sharedPrefManager: SharedPrefManager
    private val storage_permission_code = 1000
    private lateinit var aryAccount1: MutableList<AccountMaster>
    lateinit var dialog: Dialog
    private lateinit var showingDialog: ShowingDialog
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    subscribeToNotifications()
                } else {
                    showPermissionDeniedMessage()
                }
            }

            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            subscribeToNotifications()
        }


        binding.btnLogin.setOnClickListener {
            val inputText = binding.edtUserName.text.toString()
            if (inputText.isEmpty()) {
                binding.filledTextField.error = "Please Enter Your Username cannot be empty"
            } else {
                binding.filledTextField.error = null

                sharedPrefManager.getUserName =
                    binding.filledTextField.editText?.getText().toString()

                if (!sharedPrefManager.isExcelFileShowing) {
                    if (checkPermission()) {
                        if (NetworkUtil.isInternetAvailable(this)) {
                            showDialog(this)
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
//    private fun checkPermission(): Boolean {
//        return ContextCompat.checkSelfPermission(
//            this,
//            android.Manifest.permission.READ_EXTERNAL_STORAGE
//        ) == PackageManager.PERMISSION_GRANTED
//    }
//
//    private fun requestPermission() {
//        ActivityCompat.requestPermissions(
//            this,
//            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), storage_permission_code
//        )
//    }
    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.READ_MEDIA_IMAGES
                ),
                storage_permission_code
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                storage_permission_code
            )
        }
    }
//
//
    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
//
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == storage_permission_code) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                if (NetworkUtil.isInternetAvailable(this)) {
                    showDialog(this)
                    downloadAccountMasterFile()
                } else {
                    DialogUtil.showNoInternetDialog(this)
                }
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun downloadAccountMasterFile() {
        val fileReference = storageReference.child(fileExcelAccount)
        val localFile =
            UtilsFile().getLocalFilePath(this@LoginUserActivity, localSaveAccountFileName)
        Log.d(TAG, "Starting download from: ${fileReference.path}")

        fileReference.getFile(localFile).addOnSuccessListener {
            Log.d(TAG, "File downloaded successfully: ${localFile.absolutePath}")

            // Check if the file is not empty
            if (localFile.length() == 0L) {
                Log.e(TAG, "Downloaded file is empty.")
                return@addOnSuccessListener
            }
            val uri = Uri.fromFile(localFile)

            CoroutineScope(Dispatchers.IO).launch {
                val data = ExcelFileHandler(
                    this@LoginUserActivity,
                    sharedPrefManager
                ).readExcelAccountMasterFile(uri)
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
                    //dismissDialog(this@LoginUserActivity)
                    lifecycleScope.launch(Dispatchers.IO) {
                        Thread.sleep(200)
                        downloadItemMasterFile()
                    }
                    UtilsFile().deleteLocalFile(localFile, localSaveAccountFileName)
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


    private fun downloadItemMasterFile() {
        val fileReference = storageReference.child(fileExcelItem)
        val localFile = UtilsFile().getLocalFilePath(this, localSaveItemFileName)
        Log.d(TAG, "Starting download from: ${fileReference.path}")

        fileReference.getFile(localFile).addOnSuccessListener {
            Log.d(TAG, "File downloaded successfully: ${localFile.absolutePath}")
//            showDialog(this)
            // Check if the file is not empty
            if (localFile.length() == 0L) {
                Log.e(TAG, "Downloaded file is empty.")
                return@addOnSuccessListener
            }
            val uri = Uri.fromFile(localFile)

            CoroutineScope(Dispatchers.IO).launch {
                var itemDao = MasterDatabase.getDatabase(this@LoginUserActivity).itemDao()
                val data = ExcelFileHandler(
                    this@LoginUserActivity,
                    sharedPrefManager
                ).readExcelItemMasterFile(uri)
                Log.d(TAG, "Item Master come to add : " + data.size)
                itemDao.deleteAllItems()
                data.forEach { row ->
                    itemDao.insert(row)
                }
                dismissDialog(this@LoginUserActivity)
                sharedPrefManager.isItemExcelRead = true
                sharedPrefManager.isExcelFileShowing = true
                sharedPrefManager.getFirstShow = true

                UtilsFile().deleteLocalFile(localFile, localSaveItemFileName)

                startActivity(Intent(this@LoginUserActivity, MainActivity::class.java))
                overridePendingTransition(R.anim.enter_animation, R.anim.exit_animation)

                finish()

                Log.d(TAG, "Item Master data added.")


            }

        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error downloading file", exception)
            Toast.makeText(this, "Does not have Item File Name Not Match", Toast.LENGTH_SHORT)
                .show()
        }
    }


//    private fun showAddExcelFileDialog(dialog: Dialog) {
//
//        dialog.setContentView(R.layout.dialog_add_excel_show)
//        dialog.window?.setLayout(
//            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
//        )
//        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        dialog.setCanceledOnTouchOutside(false)
//        dialog.setCancelable(false)
//        dialog.show()
//
//        dialog.findViewById<TextView>(R.id.btnAccountExcel).setOnClickListener {
//            fileType = 1
//            if (checkPermission()) {
//                if (NetworkUtil.isInternetAvailable(this)) {
//                    downloadAccountMasterFile()
//                } else {
//                    DialogUtil.showNoInternetDialog(this)
//                }
//            } else {
//                requestPermission()
//            }
//        }
//        dialog.findViewById<TextView>(R.id.btnItemExcel).setOnClickListener {
//            fileType = 2
//            if (checkPermission()) {
//                downloadItemMasterFile()
//            } else {
//                requestPermission()
//            }
//        }
//
//    }

    private fun setProgressShowDialog(context: Activity, string: String) {
        showingDialog = ShowingDialog(context, string)
        showingDialog.setCanceledOnTouchOutside(false)
        showingDialog.setCancelable(false)
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

    private fun subscribeToNotifications() {
        FirebaseMessaging.getInstance().subscribeToTopic(UtilsFile.firebaseMessagSubscribeToTopic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Subscribed to notifications", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to subscribe to notifications", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showPermissionDeniedMessage() {
        Toast.makeText(
            this,
            "Notification permission denied. You won't receive notifications.",
            Toast.LENGTH_LONG
        ).show()
    }
}