package com.sunil.dhwarehouse.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sunil.dhwarehouse.roomDB.InvoiceMaster
import com.sunil.dhwarehouse.databinding.InvoiceBilItemRowBinding

class InvoiceBilAdapter(
    private val context: Context,
    private val invoiceMastersList: List<InvoiceMaster>

) :
    RecyclerView.Adapter<InvoiceBilAdapter.MyInvoiceBillHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyInvoiceBillHolder {
        val binding: InvoiceBilItemRowBinding =
            InvoiceBilItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyInvoiceBillHolder(binding)
    }

    override fun getItemCount(): Int = invoiceMastersList.size

    override fun onBindViewHolder(holder: MyInvoiceBillHolder, position: Int) {
        val invoiceMaster = invoiceMastersList[position]

        holder.binding.txtInvoiceName.text=invoiceMaster.account_name
        holder.binding.txtInvoiceAddress.text=invoiceMaster.address
        holder.binding.txtInvoicePhone.text=invoiceMaster.mobile_no
//        holder.binding.txtInvoiceTotalQty.text=invoiceMaster.account_name
//        holder.binding.txtInvoiceTotalAmount.text=invoiceMaster.account_name
    }

    class MyInvoiceBillHolder(public val binding: InvoiceBilItemRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}