package com.sunil.dhwarehouse.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sunil.dhwarehouse.R
import com.sunil.dhwarehouse.ReviewOderItemAdapter
import com.sunil.dhwarehouse.RoomDB.ItemDao
import com.sunil.dhwarehouse.RoomDB.ItemMaster
import com.sunil.dhwarehouse.RoomDB.MasterDatabase
import com.sunil.dhwarehouse.common.UtilsFile
import com.sunil.dhwarehouse.databinding.ActivityReviewOderItemBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReviewOderItemActivity : AppCompatActivity() {

    lateinit var binding: ActivityReviewOderItemBinding
    private lateinit var itemMasterList: MutableList<ItemMaster>
    private lateinit var itemDao: ItemDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityReviewOderItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        itemMasterList = ArrayList()
        lifecycleScope.launch(Dispatchers.IO) {

            itemDao = MasterDatabase.getDatabase(this@ReviewOderItemActivity).itemDao()
            itemMasterList = itemDao.getItemMaster().toMutableList()

            withContext(Dispatchers.Main) {
                val selectItemList = itemMasterList.filter { it.edtxt_qty > 0.0 }.toMutableList()
                if (selectItemList.isNotEmpty()) {
                    val reviewOderItemAdapter =
                        ReviewOderItemAdapter(
                            this@ReviewOderItemActivity,
                            selectItemList,
                            "",
                            itemDao
                        )
                    binding.rvReviewOrderAccount.layoutManager =
                        LinearLayoutManager(this@ReviewOderItemActivity)
                    binding.rvReviewOrderAccount.adapter = reviewOderItemAdapter
                } else {
                    binding.layoutNoData.constNoDataLay.visibility = View.VISIBLE
                }

            }
        }

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        UtilsFile.isChangeValues = true

    }

}