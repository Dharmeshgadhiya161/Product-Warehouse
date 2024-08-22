package com.sunil.dhwarehouse.adapter

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.RecyclerView
import com.sunil.dhwarehouse.activity.InvoiceViewActivity
import com.sunil.dhwarehouse.R
import com.sunil.dhwarehouse.roomDB.GroupedInvoice
import com.sunil.dhwarehouse.common.UtilsFile
import com.sunil.dhwarehouse.databinding.DialogDeleteItemBinding
import com.sunil.dhwarehouse.databinding.ItemGroupedInvoiceBinding

class GroupedInvoiceAdapter(
    private val context: Context,
    private val groupedInvoices: MutableList<GroupedInvoice>,
    val onDeleteClickListener: (Int) -> Unit
) : RecyclerView.Adapter<GroupedInvoiceAdapter.GroupedInvoiceViewHolder>() {

    class GroupedInvoiceViewHolder(val binding: ItemGroupedInvoiceBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupedInvoiceViewHolder {
        val binding = ItemGroupedInvoiceBinding.inflate(LayoutInflater.from(context), parent, false)
        return GroupedInvoiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupedInvoiceViewHolder, position: Int) {
        val groupedInvoice = groupedInvoices[position]
        holder.binding.tvSalarName.text = groupedInvoice.invoices[0].salesName
        holder.binding.tvAccountName.text = groupedInvoice.accountName
        holder.binding.tvAccountAdders.text = groupedInvoice.invoices[0].address
        holder.binding.tvDate.text = groupedInvoice.date + " " +  groupedInvoice.time

        holder.binding.tvTotalItem.text = "TOTAL ITEM :- " + groupedInvoice.invoices.size.toString()

        val totalQty = UtilsFile().roundValues(groupedInvoice.invoices.sumOf { it.qty }.toDouble())
        holder.binding.tvTotalItemQty.text ="TOTAL QTY :- "+totalQty.toInt().toString()

        val totalAmount = UtilsFile().roundValues(groupedInvoice.invoices.sumOf { it.amount })
        holder.binding.tvTotalAmount.text ="TOTAL AMOUNT :- "+"${context.getString(R.string.rs)}$totalAmount"

        // Set up the nested RecyclerView
//        holder.binding.rvInvoices.layoutManager = LinearLayoutManager(context)
//        holder.binding.rvInvoices.adapter = InvoiceBilAdapter(context, groupedInvoice.invoices)
        holder.binding.ivDelete.setOnClickListener {
            showDeleteDialog(
                context,
                holder.adapterPosition,
                groupedInvoice.accountName,
                groupedInvoice.date,
                groupedInvoice.time
            )

        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, InvoiceViewActivity::class.java)
            intent.putExtra("isInvoiceBilActivity", true)
            intent.putParcelableArrayListExtra("invoice_list", ArrayList(groupedInvoice.invoices))
            context.startActivity(intent)

        }

    }


    private fun showDeleteDialog(
        context: Context,
        adapterPosition: Int,
        accountName: String,
        date: String,
        time: String
    ) {

        var dialog = Dialog(context)
        val binding: DialogDeleteItemBinding =
            DialogDeleteItemBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.getRoot())
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()

        binding.txtMedicalName.text = accountName
        binding.txtDataTime.text = date + "" + " / " + time
        binding.btnYes.setOnClickListener {
            onDeleteClickListener(adapterPosition)
            dialog.dismiss()
        }
        binding.btnNo.setOnClickListener {
            dialog.dismiss()
        }

    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < groupedInvoices.size) {
            groupedInvoices.removeAt(position)
            notifyItemRemoved(position)
            if (position < groupedInvoices.size) {
                notifyItemRangeChanged(position, groupedInvoices.size - position)
            }
        }
    }

    fun removeItemAll() {
      //  if (position >= 0 && position < groupedInvoices.size) {
            groupedInvoices.clear()
           notifyDataSetChanged()

      //  }
    }

    override fun getItemCount(): Int {
        return groupedInvoices.size
    }
}
