package com.sunil.dhwarehouse.Activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.sunil.dhwarehouse.MainActivity
import com.sunil.dhwarehouse.R
import com.sunil.dhwarehouse.RoomDB.ItemMaster
import com.sunil.dhwarehouse.RoomDB.MasterDatabase
import com.sunil.dhwarehouse.common.SharedPrefManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var itemMasterList: MutableList<ItemMaster>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        sharedPrefManager = SharedPrefManager(this@SplashActivity)
        itemMasterList = ArrayList()


        Handler(Looper.myLooper()!!).postDelayed({


            if (!sharedPrefManager.getFirstShow) {
                startActivity(Intent(this@SplashActivity, LoginUserActivity::class.java))
                finish()
            } else {
                lifecycleScope.launch(Dispatchers.IO) {

                    val accountDao = MasterDatabase.getDatabase(this@SplashActivity).itemDao()
                    itemMasterList = accountDao.getItemMaster().toMutableList()
                    if (itemMasterList.size > 0) {
                        for (item in itemMasterList) {
                            GlobalScope.launch(Dispatchers.IO) {
                                item.edtxt_qty = 0.0
                                item.edtxt_free = 0.0
                                item.edtxt_scm = 0.0
                                item.txt_net_rate = 0.0
                                item.txt_subTotal = 0.0
                                item.stock_qty = item.old_stockQty
                                item.margin = item.old_margin
                                accountDao.updateItem(item)

                            }
                        }
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        finish()
                    }
                }
            }
        }, 3000)

    }
}