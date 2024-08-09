package com.sunil.dhwarehouse.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sunil.dhwarehouse.R
import com.sunil.dhwarehouse.RoomDB.InvoiceMaster
import com.sunil.dhwarehouse.databinding.FooterItemRowBinding
import com.sunil.dhwarehouse.databinding.InvoiceOrderItemViewBinding

class InvoiceViewAdapter(
    private val context: Context,
    private val invoicesList: MutableList<InvoiceMaster>,
    private val totalItems: Int,
    private val totalAmount: Double
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_VIEW_TYPE_FOOTER = 1
    private val ITEM_VIEW_TYPE_ITEM = 0

    override fun getItemViewType(position: Int): Int {
        return if (position == invoicesList.size) ITEM_VIEW_TYPE_FOOTER else ITEM_VIEW_TYPE_ITEM
    }
    class InvoiceViewHolder(var binding: InvoiceOrderItemViewBinding) :
        RecyclerView.ViewHolder(binding.root)

    class FooterViewHolder (var bindingFooter: FooterItemRowBinding) :
        RecyclerView.ViewHolder(bindingFooter.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_VIEW_TYPE_FOOTER) {
            val bindingFooter: FooterItemRowBinding =
                FooterItemRowBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            FooterViewHolder(bindingFooter)
        }else {
            val binding: InvoiceOrderItemViewBinding =
                InvoiceOrderItemViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

            InvoiceViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is InvoiceViewHolder) {

            val invoiceMaster: InvoiceMaster = invoicesList[position]
            Log.e("ReviewOderItemActivity", "onBindViewHolder:${invoicesList.size} ")
            // Set different background colors for odd and even items
            if (position % 2 == 0) {
                holder.binding.linerBottom.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.col_table_line_1
                    )
                )
            } else {
                holder.binding.linerBottom.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.col_table_line_2
                    )
                )
            }

            holder.binding.txtItemProductName.text =
                invoiceMaster.productItemName
            holder.binding.txtItemProductName.setSelected(true)
            holder.binding.txtQty.text =
                invoiceMaster.qty.toString()
            holder.binding.txtQtyFree.text =
                invoiceMaster.free.toString()
            holder.binding.txtScm.text =
                invoiceMaster.scm.toString()
            holder.binding.txtSaleRate.text =invoiceMaster.rate.toString()
            holder.binding.txtSubTotal.text =invoiceMaster.subTotal.toString()
        } else if (holder is FooterViewHolder) {

            holder.bindingFooter.txtTotalItem.text = "$totalItems"
            holder.bindingFooter.txtTotalRs.text = "${context.getString(R.string.rs)} $totalAmount"
        }
    }


//    override fun getItemCount(): Int = invoicesList.size

    override fun getItemCount(): Int {
        return invoicesList.size + 1 // Add 1 for the footer
    }


}


