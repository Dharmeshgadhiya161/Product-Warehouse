package com.sunil.dhwarehouse.Activity

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.sunil.dhwarehouse.R
import com.sunil.dhwarehouse.RoomDB.AccountMaster
import com.sunil.dhwarehouse.RoomDB.MasterDatabase
import com.sunil.dhwarehouse.databinding.ActivityAccountMasterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountMasterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountMasterBinding
    private lateinit var aryAccount1: MutableList<AccountMaster>
    private var tag = "AccountMasterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAccountMasterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        aryAccount1 = ArrayList()

        lifecycleScope.launch(Dispatchers.IO) {

            val accountDao = MasterDatabase.getDatabase(this@AccountMasterActivity).accountDao()

            aryAccount1 = accountDao.getAccountMaster()

            withContext(Dispatchers.Main) {
                for (item in aryAccount1) {
                    Log.e(tag, "onCreate: ${item.account_name}")
                }
                Log.e(tag, "onCreate: ${aryAccount1.size}")

            }
        }
    }

}