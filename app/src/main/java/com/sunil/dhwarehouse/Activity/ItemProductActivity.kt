package com.sunil.dhwarehouse.Activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sunil.dhwarehouse.R
import com.sunil.dhwarehouse.RoomDB.AccountMaster
import com.sunil.dhwarehouse.RoomDB.ClickItemCategory
import com.sunil.dhwarehouse.RoomDB.ItemCategoryName
import com.sunil.dhwarehouse.RoomDB.ItemMaster
import com.sunil.dhwarehouse.RoomDB.MasterDatabase
import com.sunil.dhwarehouse.adapter.AccountDataAdapter
import com.sunil.dhwarehouse.adapter.ItemCategoryAdapter
import com.sunil.dhwarehouse.adapter.ItemProductAdapter
import com.sunil.dhwarehouse.databinding.ActivityItemProductBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ItemProductActivity : AppCompatActivity(), ClickItemCategory {
    private lateinit var binding: ActivityItemProductBinding
    private lateinit var itemMasterList: MutableList<ItemMaster>
    private lateinit var productWiseItemList: MutableList<ItemMaster>
    private lateinit var itemCategoryList1: MutableList<String>
    private lateinit var uniqueList: MutableList<String>
    private lateinit var itemCategoryAdapter: ItemCategoryAdapter
    private lateinit var itemProductAdapter: ItemProductAdapter
    private var TAG = "ItemProductActivity"
    private var medicalName = ""
    private var itemCategoryName = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityItemProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        medicalName = intent.getStringExtra("MedicalName").toString()
        Log.d(TAG, "Received MedicalName: $medicalName")

        binding.txtMedicalName.text = medicalName

        itemMasterList = ArrayList()
        productWiseItemList = ArrayList()
        itemCategoryList1 = ArrayList()
        uniqueList = ArrayList()

        lifecycleScope.launch(Dispatchers.IO) {

            val accountDao = MasterDatabase.getDatabase(this@ItemProductActivity).itemDao()
            itemMasterList = accountDao.getItemMaster().toMutableList()

            /*--------this Code first Time use default 0 position get--------------*/
            itemCategoryName = itemMasterList[0].category
            println("itemCategoryName at position 0:--> $itemCategoryName")/*-------------End Code--------------------------------*/
            for (item in itemMasterList) {
                itemCategoryList1.add(item.category)
            }
            for (item in itemCategoryList1) {
                if (item !in uniqueList) {
                    uniqueList.add(item)
                }
            }
            // Remove only the last blank string from the list
            if (uniqueList.last().isBlank()) {
                uniqueList.removeAt(uniqueList.lastIndex)
            }

            println("List after removing duplicates: ${uniqueList.size}")

            withContext(Dispatchers.Main) {
                itemCategoryAdapter = ItemCategoryAdapter(
                    this@ItemProductActivity, uniqueList, this@ItemProductActivity
                )
                binding.rvItemCategory.layoutManager = LinearLayoutManager(
                    this@ItemProductActivity, LinearLayoutManager.HORIZONTAL, false
                )
                binding.rvItemCategory.adapter = itemCategoryAdapter

                /*----------ProductWiseDataShow-------------------------------------------*/

                for (item in itemMasterList) {
                    if (item.category == itemCategoryName) {
                        productWiseItemList.add(item)
                        println("itemCategoryName:--> ${productWiseItemList.size}")
                    }
                }

                itemProductAdapter =
                    ItemProductAdapter(this@ItemProductActivity, productWiseItemList)
                binding.rvItemProduct.layoutManager = LinearLayoutManager(this@ItemProductActivity)
                binding.rvItemProduct.adapter = itemProductAdapter

            }
        }

        binding.searchView.setOnQueryTextListener { query ->
            filterAccounts(query)
        }
    }

    private lateinit var filteredList: MutableList<ItemMaster>
    private fun filterAccounts(query: String) {
        filteredList = productWiseItemList.filter {
            it.item_name.contains(query, ignoreCase = true) || it.category.contains(
                query, ignoreCase = true
            )
        }.toMutableList()
        itemProductAdapter.updateData(filteredList, query)
    }

    override fun onClickItemCat(categorySelect: String) {
        Log.e(TAG, "onClickItemCat:--> $categorySelect")
        productWiseItemList.clear()
        for (item in itemMasterList) {
            if (item.category == categorySelect) {
                productWiseItemList.add(item)
                println("onClickItemCat:--> ${productWiseItemList.size}")
            }
        }
        itemProductAdapter.updateData(productWiseItemList, "")

    }
}

