package com.sunil.dhwarehouse.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sunil.dhwarehouse.R
import com.sunil.dhwarehouse.roomDB.ClickItemCategory
import com.sunil.dhwarehouse.roomDB.ItemDao
import com.sunil.dhwarehouse.roomDB.ItemMaster
import com.sunil.dhwarehouse.roomDB.MasterDatabase
import com.sunil.dhwarehouse.adapter.ItemCategoryAdapter
import com.sunil.dhwarehouse.adapter.ItemProductAdapter
import com.sunil.dhwarehouse.common.UtilsFile
import com.sunil.dhwarehouse.databinding.ActivityItemProductBinding
import com.sunil.dhwarehouse.databinding.DialogBackItemBinding
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
    private var getUserName = ""
    private var medicalAddress = ""
    private var mobileNo = ""
    private var medicalName = ""
    private var itemCategoryName = ""
    private var all = "ALL"
    private lateinit var accountDao: ItemDao
    private lateinit var selectItemList: MutableList<ItemMaster>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
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
//        selectItemList = ArrayList()
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
                    //  itemCategoryList1.add(all)
                    itemCategoryList1.add(item.category)

                }
                for (item in itemCategoryList1) {
                    //TODO item in same Name item category remove list
                    if (item !in uniqueList) {
                        uniqueList.add(item)
                        println("List after item removing duplicates: $item")
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
                    filterAndSortList(uniqueList),
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
            intent.putExtra("getUserName", getUserName)
            intent.putExtra("MedicalName", medicalName)
            intent.putExtra("MedicalAddress", medicalAddress)
            intent.putExtra("MobileNo", mobileNo)
            startActivity(intent)
            overridePendingTransition(R.anim.enter_animation, R.anim.exit_animation)
        }

        binding.cardClick.setOnClickListener {
            binding.cardClick.setCardBackgroundColor(getColor(R.color.colorAccent))
            binding.txtItemCategory.setTextColor(getColor(R.color.white))

            productWiseItemList.clear()
            for (item in itemMasterList) {
                productWiseItemList.add(item)
            }

            itemCategoryAdapter.updateRefreshSelectPos(-1)
            itemProductAdapter.updateData(productWiseItemList, "")

        }
//
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

                binding.cardClick.setCardBackgroundColor(getColor(R.color.white))
                binding.txtItemCategory.setTextColor(getColor(R.color.black))


                productWiseItemList.add(item)
                println("categorySelect: ${productWiseItemList.size}")
            }
        }
        itemProductAdapter.updateData(productWiseItemList, "")
    }

    override fun onBackPressed() {
        if (UtilsFile.isQtyBack) {
            showDeleteDialog()
        } else {
            super.onBackPressed()
            itemDataClear()
        }

    }

    private fun itemDataClear() {
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
                    itemCategoryAdapter.updateRefreshSelectPos(-1)
                    binding.cardClick.setCardBackgroundColor(getColor(R.color.colorAccent))
                    binding.txtItemCategory.setTextColor(getColor(R.color.white))
                    itemProductAdapter.updateData(productWiseItemList, "")
                    itemProductAdapter.updateTotalData(itemMasterList)
                    UtilsFile.isChangeValues = false
                }
            }
        }


    }

    private fun filterAndSortList(inputList: MutableList<String>): MutableList<String> {
        val uniqueList = inputList.toSet().toMutableList()
        uniqueList.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it })
        return uniqueList
    }

    private fun showDeleteDialog(
    ) {
        val dialog = Dialog(this@ItemProductActivity)
        val binding: DialogBackItemBinding =
            DialogBackItemBinding.inflate(LayoutInflater.from(this@ItemProductActivity))
        dialog.setContentView(binding.getRoot())
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()

        binding.ivClose.setOnClickListener { dialog.dismiss() }
        binding.btnYes.setOnClickListener {
            finish()
            UtilsFile.isQtyBack = false
            itemDataClear()
            dialog.dismiss()
        }
        binding.btnNo.setOnClickListener {
            dialog.dismiss()
        }

    }

}

