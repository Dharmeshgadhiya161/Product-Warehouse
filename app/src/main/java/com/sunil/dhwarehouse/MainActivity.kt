package com.sunil.dhwarehouse

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sunil.dhwarehouse.RoomDB.AccountMaster
import com.sunil.dhwarehouse.RoomDB.MasterDatabase
import com.sunil.dhwarehouse.adapter.AccountDataAdapter
import com.sunil.dhwarehouse.common.SharedPrefManager
import com.sunil.dhwarehouse.common.ShowingDialog
import com.sunil.dhwarehouse.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var aryAccount1: MutableList<AccountMaster>
    lateinit var aryNewListAccount: MutableList<AccountMaster>
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
        aryNewListAccount = ArrayList()
        setProgressShowDialog(this, "Load Data..!")
        setDataLoad()

//        binding.txtMon.setOnClickListener {
//
//            binding.txtAll.background = getDrawable(R.drawable.bg_un_select_rectangle)
//            binding.txtMon.background = getDrawable(R.drawable.bg_select_rectangle)
//            binding.txtTue.background = getDrawable(R.drawable.bg_un_select_rectangle)
//            binding.txtWed.background = getDrawable(R.drawable.bg_un_select_rectangle)
//            binding.txtThu.background = getDrawable(R.drawable.bg_un_select_rectangle)
//            binding.txtFri.background = getDrawable(R.drawable.bg_un_select_rectangle)
//            binding.txtSat.background = getDrawable(R.drawable.bg_un_select_rectangle)
//            binding.txtSun.background = getDrawable(R.drawable.bg_un_select_rectangle)
//
//
//            lifecycleScope.launch(Dispatchers.IO) {
//                val accountDao = MasterDatabase.getDatabase(this@MainActivity).accountDao()
//                aryAccount1 = accountDao.getAccountMaster()
//                withContext(Dispatchers.Main) {
//                    for (item in aryAccount1) {
//                        if (item.day == "MONDAY") {
//                            aryAccount1.add(item)
//                            accountDataAdapter.updateData(aryAccount1)
//                        }
//                    }
//
//                    Log.e("Main", "onCreate: ${aryAccount1.size}")
//                    Log.e("Main", "onCreate: ${aryAccount1.size}")
//                }
//            }
//        }


       binding.searchView.setOnQueryTextListener { query ->
            filterAccounts(query)
        }


//        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                Log.d("MainActivity", "Query text submit: $query")
//                query?.let { accountDataAdapter.filter(it) } // Assuming your adapter has a filter method
//                return false
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                Log.d("MainActivity", "Query text change: $newText")
//                newText?.let { accountDataAdapter.filter(it) } // Assuming your adapter has a filter method
//                return false
//            }
//        })

    }
    private lateinit var filteredList: MutableList<AccountMaster>
    private fun filterAccounts(query: String) {
        filteredList = aryAccount1.filter {
            it.account_name.contains(query, ignoreCase = true)|| it.mobile_no.contains(query, ignoreCase = true)
        }.toMutableList()

        accountDataAdapter.updateData(filteredList,query)
    }
    private fun setDataLoad() {
        lifecycleScope.launch(Dispatchers.IO) {
            val accountDao = MasterDatabase.getDatabase(this@MainActivity).accountDao()
            aryAccount1 = accountDao.getAccountMaster()
            binding.rvAllAccount.layoutManager = LinearLayoutManager(this@MainActivity)
            withContext(Dispatchers.Main) {
                accountDataAdapter = AccountDataAdapter(aryAccount1)
                binding.rvAllAccount.adapter = accountDataAdapter
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