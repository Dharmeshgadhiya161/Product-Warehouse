package com.sunil.dhwarehouse.Activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sunil.dhwarehouse.R
import com.sunil.dhwarehouse.RoomDB.ClickItemCategory
import com.sunil.dhwarehouse.RoomDB.ItemDao
import com.sunil.dhwarehouse.RoomDB.ItemMaster
import com.sunil.dhwarehouse.RoomDB.MasterDatabase
import com.sunil.dhwarehouse.adapter.ItemCategoryAdapter
import com.sunil.dhwarehouse.adapter.ItemProductAdapter
import com.sunil.dhwarehouse.common.UtilsFile
import com.sunil.dhwarehouse.databinding.ActivityItemProductBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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
    private var getUserName=""
    private var medicalAddress = ""
    private var mobileNo = ""
    private var medicalName = ""
    private var itemCategoryName = ""
    private var all = "ALL"
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var accountDao: ItemDao
    private lateinit var selectItemList: MutableList<ItemMaster>

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

        getUserName = intent.getStringExtra("getUserName").toString()
        medicalName = intent.getStringExtra("MedicalName").toString()
        medicalAddress = intent.getStringExtra("MedicalAddress").toString()
        mobileNo = intent.getStringExtra("MobileNo").toString()



        binding.txtMedicalName.text = medicalName

        itemMasterList = ArrayList()
        productWiseItemList = ArrayList()
        itemCategoryList1 = ArrayList()
        uniqueList = ArrayList()
        selectItemList = ArrayList()
        bottomSheetDialog = BottomSheetDialog(this@ItemProductActivity)
        lifecycleScope.launch(Dispatchers.IO) {

            accountDao = MasterDatabase.getDatabase(this@ItemProductActivity).itemDao()
            itemMasterList = accountDao.getItemMaster().toMutableList()


            /*--------this Code first Time use default 0 position get--------------*/
            itemCategoryName = all
            //itemCategoryName = itemMasterList[0].category
            println("itemCategoryName at position 0:--> $itemCategoryName")
            /*-------------End Code--------------------------------*/


            withContext(Dispatchers.Main) {

                for (item in itemMasterList) {
                    itemCategoryList1.add(all)
                    itemCategoryList1.add(item.category)

                }
                for (item in itemCategoryList1) {
                    //TODO item in same Name item category remove list
                    if (item !in uniqueList) {
                        uniqueList.add(item)
                    }
                }
                // Remove only the last blank string from the list
                if (uniqueList.last().isBlank()) {
                    uniqueList.removeAt(uniqueList.lastIndex)
                }

                println("List after removing duplicates: ${uniqueList.size}")

                binding.rvItemCategory.layoutManager = LinearLayoutManager(
                    this@ItemProductActivity, LinearLayoutManager.HORIZONTAL, false
                )
                itemCategoryAdapter = ItemCategoryAdapter(
                    this@ItemProductActivity,
                    uniqueList,
                    this@ItemProductActivity
                )

                binding.rvItemCategory.adapter = itemCategoryAdapter

                /*----------ProductWiseDataShow-------------------------------------------*/

                productWiseItemList.addAll(itemMasterList)

                itemProductAdapter =
                    ItemProductAdapter(
                        this@ItemProductActivity,
                        productWiseItemList,
                        "",
                        accountDao,
                        binding.btnRequestOrder,
                        binding.rvItemProduct,
                        binding.txtSubtotalRS, binding.txtTotalItem, binding.btnClickRequestOrder,
                        itemMasterList
                    )
                binding.rvItemProduct.layoutManager = LinearLayoutManager(this@ItemProductActivity)
                binding.rvItemProduct.adapter = itemProductAdapter

            }
        }

        binding.searchView.setOnQueryTextListener { query ->
            filterAccounts(query)
        }

        binding.btnClickRequestOrder.setOnClickListener {
            val intent = Intent(this@ItemProductActivity, ReviewOderItemActivity::class.java)
            intent.putExtra("getUserName",getUserName)
            intent.putExtra("MedicalName", medicalName)
            intent.putExtra("MedicalAddress", medicalAddress)
            intent.putExtra("MobileNo", mobileNo)
            startActivity(intent)
            overridePendingTransition(R.anim.enter_animation, R.anim.exit_animation)
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
        productWiseItemList.clear()
        for (item in itemMasterList) {
            if (categorySelect == all) {
                productWiseItemList.add(item)
            } else if (item.category == categorySelect) {
                productWiseItemList.add(item)
                println("categorySelect: ${productWiseItemList.size}")
            }
        }
        itemProductAdapter.updateData(productWiseItemList, "")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (bottomSheetDialog.isShowing) {
            bottomSheetDialog.dismiss()
        }

        if (itemMasterList.size > 0) {
            for (item in itemMasterList) {
                GlobalScope.launch(Dispatchers.IO) {
                    item.edtxt_qty = 0.0
                    item.edtxt_free = 0.0
                    item.edtxt_scm = 0.0
                    item.txt_net_rate = 0.0
                    item.txt_subTotal = 0.0
                    item.margin = item.old_margin
                    accountDao.updateItem(item)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (UtilsFile.isChangeValues) {
            // Refresh the RecyclerView data
            lifecycleScope.launch(Dispatchers.IO) {
                productWiseItemList.clear()
                accountDao = MasterDatabase.getDatabase(this@ItemProductActivity).itemDao()
                itemMasterList = ArrayList()
                itemMasterList = accountDao.getItemMaster().toMutableList()

                productWiseItemList.addAll(itemMasterList)
                withContext(Dispatchers.Main) {
                    //itemCategoryAdapter.updateRefreshSelectPos(0)
                    itemProductAdapter.updateData(productWiseItemList, "")
                    itemProductAdapter.updateTotalData(itemMasterList)
                    UtilsFile.isChangeValues = false

                }

            }
        }

    }


}

