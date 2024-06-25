package com.sunil.dhwarehouse.Activity

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.sunil.dhwarehouse.R
import com.sunil.dhwarehouse.RoomDB.ItemMaster
import com.sunil.dhwarehouse.RoomDB.MasterDatabase
import com.sunil.dhwarehouse.databinding.ActivityItemProductBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ItemProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityItemProductBinding
    private lateinit var itemMasterList: MutableList<ItemMaster>
    private var tag = "ItemProductActivity"
    private var medicalName = String


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
        val medicalName = intent.getStringExtra("MedicalName")
        Log.d("SecondActivity", "Received MedicalName: $medicalName")

        binding.txtMedicalName.text = medicalName

        itemMasterList = ArrayList()

        lifecycleScope.launch(Dispatchers.IO) {

            val accountDao = MasterDatabase.getDatabase(this@ItemProductActivity).itemDao()

            itemMasterList = accountDao.getItemMaster().toMutableList()

            withContext(Dispatchers.Main) {
                for (item in itemMasterList) {
                    Log.e(tag, "onCreate: ${item.item_name}")
                }
                Log.e(tag, "onCreate: ${itemMasterList.size}")

            }
        }
    }

}