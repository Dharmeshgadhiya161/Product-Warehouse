package com.sunil.dhwarehouse.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.sunil.dhwarehouse.RoomDB.AccountMaster
import com.sunil.dhwarehouse.databinding.AccountItemRowBinding

class AccountDataAdapter(private val userList: MutableList<AccountMaster>) :
    RecyclerView.Adapter<AccountDataAdapter.MyAccountHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAccountHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: AccountItemRowBinding =
            AccountItemRowBinding.inflate(layoutInflater, parent, false)
        return MyAccountHolder(binding)
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: MyAccountHolder, position: Int) {
        holder.bind(userList[position])
    }

    class MyAccountHolder(private val binding: AccountItemRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: AccountMaster) {
            binding.txtAccountName.text=user.account_name
            binding.txtAccountAddress.text=user.addess
            binding.txtAccountPhone.text=user.mobile_no
        }
    }
}