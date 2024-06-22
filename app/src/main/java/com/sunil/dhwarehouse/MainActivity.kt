package com.sunil.dhwarehouse

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sunil.dhwarehouse.RoomDB.AccountDao
import com.sunil.dhwarehouse.RoomDB.AccountMaster
import com.sunil.dhwarehouse.RoomDB.ItemDao
import com.sunil.dhwarehouse.RoomDB.ItemMaster
import com.sunil.dhwarehouse.RoomDB.MasterDatabase
import com.sunil.dhwarehouse.adapter.AccountDataAdapter
import com.sunil.dhwarehouse.common.SharedPrefManager
import com.sunil.dhwarehouse.common.ShowingDialog
import com.sunil.dhwarehouse.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPrefManager: SharedPrefManager
    lateinit var aryAccount1: MutableList<AccountMaster>
    private lateinit var showingDialog: ShowingDialog
    private lateinit var accountDataAdapter: AccountDataAdapter

    lateinit var dayName: String
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
        aryAccount1 = ArrayList()

        lifecycleScope.launch(Dispatchers.IO) {
            val accountDao = MasterDatabase.getDatabase(this@MainActivity).accountDao()
            aryAccount1 = accountDao.getAccountMaster()

            withContext(Dispatchers.Main) {
                accountDataAdapter = AccountDataAdapter(aryAccount1)
                binding.rvAllAccount.layoutManager = LinearLayoutManager(this@MainActivity)
                binding.rvAllAccount.adapter = accountDataAdapter

                Log.e("tag", "onCreate: ${aryAccount1.size}")

            }
        }
    }

    fun setProgressShowDialog(context: Activity, string: String) {
        showingDialog = ShowingDialog(context, string)
        showingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        showingDialog.setCanceledOnTouchOutside(false)
        showingDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun showDialog(context: Activity) {
        if (!context.isFinishing) {
            showingDialog.show()
        }
    }

    fun dismissDialog(context: Activity) {
        if (!context.isFinishing && showingDialog.isShowing) {
            showingDialog.cancel()
        }
    }
}