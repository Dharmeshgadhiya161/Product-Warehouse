package com.sunil.dhwarehouse.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sunil.dhwarehouse.activity.ItemProductActivity
import com.sunil.dhwarehouse.R
import com.sunil.dhwarehouse.roomDB.AccountMaster
import com.sunil.dhwarehouse.databinding.AccountItemRowBinding

class AccountDataAdapter(
    var context: Activity,
    private var masterMutableList: MutableList<AccountMaster>,
    private var query: String = "",
   var userName:String
) :
    RecyclerView.Adapter<AccountDataAdapter.MyAccountHolder>() {
    private var filteredDataList: MutableList<AccountMaster> = masterMutableList.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAccountHolder {
        val binding: AccountItemRowBinding =
            AccountItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyAccountHolder(binding)
    }

    override fun getItemCount(): Int = masterMutableList.size
    fun updateData(filteredListMain: MutableList<AccountMaster>, query: String) {
        masterMutableList = filteredListMain
        this.query = query
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyAccountHolder, position: Int) {
        holder.bind(masterMutableList[position], query, context,userName)
    }

    class MyAccountHolder(private val binding: AccountItemRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(accountMaster: AccountMaster, query1: String, context: Activity, getUserName:String) {
            binding.txtAccountName.setHighlightText(
                accountMaster.account_name,
                query1,
                context.getColor(R.color.colorPrimary)
            ) // Change the highlight color
            binding.txtAccountAddress.setText(accountMaster.addess +"("+accountMaster.day +")")
            binding.txtAccountPhone.setHighlightText(
                accountMaster.mobile_no,
                query1,
                context.getColor(R.color.colorPrimary)
            )

            binding.cardV.setOnClickListener {
                val intent = Intent(context, ItemProductActivity::class.java)
                intent.putExtra("getUserName",getUserName)
                intent.putExtra("MedicalName", accountMaster.account_name)
                intent.putExtra("MedicalAddress", accountMaster.addess)
                intent.putExtra("MobileNo", accountMaster.mobile_no)
                context.startActivity(intent)
                context.overridePendingTransition(R.anim.enter_animation, R.anim.exit_animation)
            }

        }
    }
}