package com.sunil.dhwarehouse.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sunil.dhwarehouse.Activity.ItemProductActivity
import com.sunil.dhwarehouse.R
import com.sunil.dhwarehouse.RoomDB.AccountMaster
import com.sunil.dhwarehouse.databinding.AccountItemRowBinding

class AccountDataAdapter(
    var context: Activity,
    private var masterMutableList: MutableList<AccountMaster>,
    private var query: String = ""
) :
    RecyclerView.Adapter<AccountDataAdapter.MyAccountHolder>() {
    //private var filteredDataList: MutableList<AccountMaster> = masterMutableList.toMutableList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAccountHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: AccountItemRowBinding =
            AccountItemRowBinding.inflate(layoutInflater, parent, false)
        return MyAccountHolder(binding)
    }

    override fun getItemCount(): Int = masterMutableList.size
    fun updateData(newAccounts: MutableList<AccountMaster>, query: String) {
        masterMutableList = newAccounts
        this.query = query
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyAccountHolder, position: Int) {
        holder.bind(masterMutableList[position], query, context)
    }

    class MyAccountHolder(private val binding: AccountItemRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(accountMaster: AccountMaster, query1: String, context: Activity) {
            binding.txtAccountName.setHighlightText(
                accountMaster.account_name,
                query1,
                context.getColor(R.color.colorPrimary)
            ) // Change the highlight color
            binding.txtAccountAddress.text = accountMaster.addess
            binding.txtAccountPhone.setHighlightText(
                accountMaster.mobile_no,
                query1,
                context.getColor(R.color.colorPrimary)
            )

            binding.cardV.setOnClickListener {
                val intent = Intent(context, ItemProductActivity::class.java)
                intent.putExtra("MedicalName", accountMaster.account_name)
                intent.putExtra("MedicalAddress", accountMaster.addess)
                context.startActivity(intent)
            }

        }
    }


//    fun filter(text: String) {
//        Log.d("MyAdapter", "Filtering for query: $text")
//        filteredDataList.clear()
//        if (text.isEmpty()) {
//            filteredDataList.addAll(masterMutableList)
//        } else {
//            val lowerCaseQuery = text.lowercase()
//            for (item in masterMutableList) {
//                if (item.account_name.lowercase().contains(lowerCaseQuery)) {
//                    filteredDataList.add(item)
//                }
//            }
//        }
//        Log.d("MyAdapter", "Filtered list size: ${filteredDataList.size}")
//        notifyDataSetChanged()
//    }
}