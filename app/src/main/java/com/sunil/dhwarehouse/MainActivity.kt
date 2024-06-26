package com.sunil.dhwarehouse

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    private lateinit var dayWiseAryAccount1: MutableList<AccountMaster>
    lateinit var aryNewListAccount: MutableList<AccountMaster>
    private lateinit var showingDialog: ShowingDialog
    private lateinit var accountDataAdapter: AccountDataAdapter

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
        dayWiseAryAccount1 = ArrayList()
        aryNewListAccount = ArrayList()
        setDataLoad("All")
        setTabSelect()

        binding.searchView.setOnQueryTextListener { query ->
            filterAccounts(query)
        }
    }

    private fun setTabSelect() {
        binding.txtAll.setOnClickListener {

            binding.txtAll.background = getDrawable(R.drawable.bg_select_rectangle)
            binding.txtMon.background = getDrawable(R.drawable.bg_un_select_rectangle)
            binding.txtTue.background = getDrawable(R.drawable.bg_un_select_rectangle)
            binding.txtWed.background = getDrawable(R.drawable.bg_un_select_rectangle)
            binding.txtThu.background = getDrawable(R.drawable.bg_un_select_rectangle)
            binding.txtFri.background = getDrawable(R.drawable.bg_un_select_rectangle)
            binding.txtSat.background = getDrawable(R.drawable.bg_un_select_rectangle)
            binding.txtSun.background = getDrawable(R.drawable.bg_un_select_rectangle)

            binding.txtAll.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.txtMon.setTextColor(ContextCompat.getColor(this, R.color.sub_text))
            binding.txtTue.setTextColor(ContextCompat.getColor(this, R.color.sub_text))
            binding.txtWed.setTextColor(ContextCompat.getColor(this, R.color.sub_text))
            binding.txtThu.setTextColor(ContextCompat.getColor(this, R.color.sub_text))
            binding.txtFri.setTextColor(ContextCompat.getColor(this, R.color.sub_text))
            binding.txtSat.setTextColor(ContextCompat.getColor(this, R.color.sub_text))
            binding.txtSun.setTextColor(ContextCompat.getColor(this, R.color.sub_text))

            setDataLoad("All")
        }

        binding.txtMon.setOnClickListener {

            binding.txtAll.background = getDrawable(R.drawable.bg_un_select_rectangle)
            binding.txtMon.background = getDrawable(R.drawable.bg_select_rectangle)
            binding.txtTue.background = getDrawable(R.drawable.bg_un_select_rectangle)
            binding.txtWed.background = getDrawable(R.drawable.bg_un_select_rectangle)
            binding.txtThu.background = getDrawable(R.drawable.bg_un_select_rectangle)
            binding.txtFri.background = getDrawable(R.drawable.bg_un_select_rectangle)
            binding.txtSat.background = getDrawable(R.drawable.bg_un_select_rectangle)
            binding.txtSun.background = getDrawable(R.drawable.bg_un_select_rectangle)

            binding.txtAll.setTextColor(ContextCompat.getColor(this, R.color.sub_text))
            binding.txtMon.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.txtTue.setTextColor(ContextCompat.getColor(this, R.color.sub_text))
            binding.txtWed.setTextColor(ContextCompat.getColor(this, R.color.sub_text))
            binding.txtThu.setTextColor(ContextCompat.getColor(this, R.color.sub_text))
            binding.txtFri.setTextColor(ContextCompat.getColor(this, R.color.sub_text))
            binding.txtSat.setTextColor(ContextCompat.getColor(this, R.color.sub_text))
            binding.txtSun.setTextColor(ContextCompat.getColor(this, R.color.sub_text))

            setDataLoad("MONDAY")
        }

        binding.txtSun.setOnClickListener {

            binding.txtAll.background = getDrawable(R.drawable.bg_un_select_rectangle)
            binding.txtMon.background = getDrawable(R.drawable.bg_un_select_rectangle)
            binding.txtTue.background = getDrawable(R.drawable.bg_un_select_rectangle)
            binding.txtWed.background = getDrawable(R.drawable.bg_un_select_rectangle)
            binding.txtThu.background = getDrawable(R.drawable.bg_un_select_rectangle)
            binding.txtFri.background = getDrawable(R.drawable.bg_un_select_rectangle)
            binding.txtSat.background = getDrawable(R.drawable.bg_un_select_rectangle)
            binding.txtSun.background = getDrawable(R.drawable.bg_select_rectangle)

            binding.txtAll.setTextColor(ContextCompat.getColor(this, R.color.sub_text))
            binding.txtMon.setTextColor(ContextCompat.getColor(this, R.color.sub_text))
            binding.txtTue.setTextColor(ContextCompat.getColor(this, R.color.sub_text))
            binding.txtWed.setTextColor(ContextCompat.getColor(this, R.color.sub_text))
            binding.txtThu.setTextColor(ContextCompat.getColor(this, R.color.sub_text))
            binding.txtFri.setTextColor(ContextCompat.getColor(this, R.color.sub_text))
            binding.txtSat.setTextColor(ContextCompat.getColor(this, R.color.sub_text))
            binding.txtSun.setTextColor(ContextCompat.getColor(this, R.color.white))

            setDataLoad("SUNDAY")
        }
    }

    private lateinit var filteredList: MutableList<AccountMaster>
    private fun filterAccounts(query: String) {
        filteredList = dayWiseAryAccount1.filter {
            it.account_name.contains(query, ignoreCase = true) || it.mobile_no.contains(
                query, ignoreCase = true
            )
        }.toMutableList()
        accountDataAdapter.updateData(filteredList, query)
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
//                    lifecycleScope.launch(Dispatchers.IO) {
//                        Thread.sleep(300)
//
//                        runOnUiThread {
//                            dismissDialog()
//                        }
//                    }
                    binding.rvAllAccount.visibility = View.VISIBLE
                    binding.layoutNoData.constNoDataLay.visibility = View.GONE
                    accountDataAdapter = AccountDataAdapter(this@MainActivity,dayWiseAryAccount1)
                    binding.rvAllAccount.layoutManager = LinearLayoutManager(this@MainActivity)
                    binding.rvAllAccount.adapter = accountDataAdapter
                } else {
                    binding.rvAllAccount.visibility = View.GONE
                    binding.layoutNoData.constNoDataLay.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setProgressShowDialog(context: Activity, msg: String) {
        showingDialog = ShowingDialog(context, msg)
        showingDialog.setCanceledOnTouchOutside(false)
        showingDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun showDialog() {
        //showingDialog.show()
    }

    private fun dismissDialog() {
//        if (showingDialog.isShowing) {
//            showingDialog.cancel()
//        }
    }
}