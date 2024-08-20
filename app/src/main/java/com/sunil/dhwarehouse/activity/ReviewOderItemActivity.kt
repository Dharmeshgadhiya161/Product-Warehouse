package com.sunil.dhwarehouse.activity

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import com.sunil.dhwarehouse.adapter.ReviewOderItemAdapter
import com.sunil.dhwarehouse.roomDB.ItemDao
import com.sunil.dhwarehouse.roomDB.ItemMaster
import com.sunil.dhwarehouse.roomDB.MasterDatabase
import com.sunil.dhwarehouse.common.ShowingDialog
import com.sunil.dhwarehouse.common.UtilsFile
import com.sunil.dhwarehouse.databinding.ActivityReviewOderItemBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReviewOderItemActivity : AppCompatActivity() {

    lateinit var binding: ActivityReviewOderItemBinding
    private lateinit var itemMasterList: MutableList<ItemMaster>
    private lateinit var itemDao: ItemDao
    private var getUserName = ""
    private var medicalAddress = ""
    private var mobileNo = ""
    private var medicalName = ""
    private lateinit var selectItemList: MutableList<ItemMaster>
    private lateinit var reviewOderItemAdapter: ReviewOderItemAdapter
    private lateinit var showingDialog: ShowingDialog
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
        getUserName = intent.getStringExtra("getUserName").toString()
        medicalName = intent.getStringExtra("MedicalName").toString()
        medicalAddress = intent.getStringExtra("MedicalAddress").toString()
        mobileNo = intent.getStringExtra("MobileNo").toString()


        itemMasterList = ArrayList()
        selectItemList = ArrayList()
        setProgressShowDialog(this@ReviewOderItemActivity, "Loading..Order Processing!")

        lifecycleScope.launch(Dispatchers.IO) {

            itemDao = MasterDatabase.getDatabase(this@ReviewOderItemActivity).itemDao()
            itemMasterList = itemDao.getItemMaster().toMutableList()

            var invoiceDao = MasterDatabase.getDatabase(this@ReviewOderItemActivity).invoiceDao()

            withContext(Dispatchers.Main) {
                selectItemList = itemMasterList.filter { it.edtxt_qty > 0.0 }.toMutableList()

                Log.e("ReviewOderItemActivity", "onCreate:${selectItemList.size} ")

                if (selectItemList.isNotEmpty()) {
                    reviewOderItemAdapter =
                        ReviewOderItemAdapter(
                            this@ReviewOderItemActivity,
                            selectItemList,
                            "",
                            itemDao,
                            invoiceDao,
                            binding.txtSubtotalRS,
                            binding.txtTotalItem,
                            binding.btnClickRequestOrder,
                            getUserName,
                            medicalName,
                            medicalAddress,
                            mobileNo,
                            showingDialog
                        )
                    binding.rvReviewOrderAccount.layoutManager =
                        LinearLayoutManager(this@ReviewOderItemActivity)
                    binding.rvReviewOrderAccount.adapter = reviewOderItemAdapter
                } else {
                    binding.layoutNoData.constNoDataLay.visibility = View.VISIBLE
                }

            }
        }



        binding.txtMedicalName.text = medicalName
        binding.txtMedicalAddress.text = medicalAddress
        binding.txtMedicalPhone.text = mobileNo

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }


    }

    private fun setProgressShowDialog(context: Activity, msg: String) {
        showingDialog = ShowingDialog(context, msg)
        showingDialog.setCanceledOnTouchOutside(false)
        showingDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onBackPressed() {
        super.onBackPressed()
        UtilsFile.isChangeValues = true

    }

}