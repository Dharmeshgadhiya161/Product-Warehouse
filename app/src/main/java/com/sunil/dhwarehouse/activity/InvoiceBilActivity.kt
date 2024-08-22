package com.sunil.dhwarehouse.activity

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sunil.dhwarehouse.R
import com.sunil.dhwarehouse.roomDB.GroupedInvoice
import com.sunil.dhwarehouse.roomDB.MasterDatabase
import com.sunil.dhwarehouse.adapter.GroupedInvoiceAdapter
import com.sunil.dhwarehouse.databinding.ActivityInvoiceBilBinding
import com.sunil.dhwarehouse.databinding.DialogAllDeleteItemBinding
import com.sunil.dhwarehouse.databinding.DialogDeleteItemBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class InvoiceBilActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInvoiceBilBinding
    private val groupedInvoiceList = mutableListOf<GroupedInvoice>() // Move here

    private lateinit var invoiceBilAdapter: GroupedInvoiceAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityInvoiceBilBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        fetchAndUpdateData()

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun fetchAndUpdateData() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val fetchedInvoices =
                    MasterDatabase.getDatabase(this@InvoiceBilActivity).invoiceDao()
                val invoices = fetchedInvoices.getInvoiceMaster().toMutableList()


                // Group invoices by accountName, date, and time
                val groupedInvoices = invoices.groupBy {
                    Triple(it.account_name, it.date, it.time)
                }

                // Create a list of GroupedInvoice objects
                groupedInvoiceList.clear() // Clear existing data if any
                groupedInvoiceList.addAll(groupedInvoices.map { (key, invoiceGroup) ->
                    val (accountName, date, time) = key
                    GroupedInvoice(
                        accountName = accountName,
                        date = date,
                        time = time,
                        invoices = invoiceGroup
                    )
                })
                // Ensure date and time are in a sortable format
                val sortedGroupedInvoiceList = groupedInvoiceList.sortedWith(
                    compareByDescending<GroupedInvoice> {
                        parseDate(it.date)
                    }.thenByDescending {
                        parseTime(it.time)
                    }
                )
                // Update the RecyclerView with the grouped data
                withContext(Dispatchers.Main) {
                    updateRecyclerView()

                    invoiceBilAdapter = GroupedInvoiceAdapter(
                        this@InvoiceBilActivity,
                        sortedGroupedInvoiceList.toMutableList()
                    ) { position ->
                        handleDelete(position)
                    }
                    binding.rvInvoice.layoutManager = LinearLayoutManager(this@InvoiceBilActivity)
                    binding.rvInvoice.adapter = invoiceBilAdapter
                }
            } catch (e: Exception) {
                Log.e("TAG", "Error fetching invoices: ${e.message}")
            }
        }

        binding.ivDeleteExcelFile.setOnClickListener {
            if (groupedInvoiceList.size != 0) {
                showAllDeleteDialog()
            }
        }

    }

    private fun handleAllDelete() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val database = MasterDatabase.getDatabase(this@InvoiceBilActivity)
                //     val invoiceToDelete = groupedInvoiceList[position]
                database.invoiceDao().deleteAllInvoiceItem()

                withContext(Dispatchers.Main) {
                    invoiceBilAdapter.removeItemAll()

                    Toast.makeText(
                        this@InvoiceBilActivity,
                        "All Item deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateRecyclerView()
                }
            } catch (e: Exception) {
                Log.e("TAG", "Error deleting invoice: ${e.message}")
            }
        }
    }

    private fun handleDelete(position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val database = MasterDatabase.getDatabase(this@InvoiceBilActivity)
                val invoiceToDelete = groupedInvoiceList[position]
                database.invoiceDao().deleteInvoices(invoiceToDelete.invoices)

                withContext(Dispatchers.Main) {
                    invoiceBilAdapter.removeItem(position)

                    Toast.makeText(
                        this@InvoiceBilActivity,
                        "Item deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateRecyclerView()
                }
            } catch (e: Exception) {
                Log.e("TAG", "Error deleting invoice: ${e.message}")
            }
        }
    }

    private fun updateRecyclerView() {
        if (groupedInvoiceList.size == 0) {
            binding.rvInvoice.visibility = View.GONE
            binding.layoutNoData.constNoDataLay.visibility =
                View.VISIBLE
        } else {
            binding.rvInvoice.visibility = View.VISIBLE
            binding.layoutNoData.constNoDataLay.visibility = View.GONE
        }
    }

    private fun parseDate(date: String): LocalDate {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    }

    private fun parseTime(time: String): LocalTime {
        return LocalTime.parse(time, DateTimeFormatter.ofPattern("hh:mm a"))
    }


    private fun showAllDeleteDialog() {

        var dialog = Dialog(this@InvoiceBilActivity)
        val binding: DialogAllDeleteItemBinding =
            DialogAllDeleteItemBinding.inflate(LayoutInflater.from(this))
        dialog.setContentView(binding.getRoot())
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()

        binding.btnYes.setOnClickListener {
            handleAllDelete()
            dialog.dismiss()
        }
        binding.btnNo.setOnClickListener {
            dialog.dismiss()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()

        finish()
    }
}