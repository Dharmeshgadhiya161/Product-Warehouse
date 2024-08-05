package com.sunil.dhwarehouse.adapter

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.sunil.dhwarehouse.R
import com.sunil.dhwarehouse.RoomDB.ItemDao
import com.sunil.dhwarehouse.RoomDB.ItemMaster
import com.sunil.dhwarehouse.databinding.ItemProductRowBinding
import java.math.BigDecimal
import java.math.RoundingMode

class ItemProductAdapter(
    var context: Activity,
    private var itemMasterList: MutableList<ItemMaster>,
    private var query: String = "",
    private var accountDao: ItemDao,
    private var btnRequestOrder: TextView,
    private val recyclerView: RecyclerView
) : RecyclerView.Adapter<ItemProductAdapter.MyItemProductHolder>() {
    var edtQtyNumber = ""
    var subTotalResult = " "
    private var TAG = "ItemProductAdapter"
    var netSaleRate = 0.0
    var scmRs = 0.0
    var freeQty = 0.0
    var scmPercent = 0.0
    private lateinit var selectItemList: MutableList<ItemMaster>
    init {
        setHasStableIds(true)
    }
    class MyItemProductHolder(var binding: ItemProductRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(itemMaster: ItemMaster, query1: String, context: Activity) {
            binding.txtItemProductName.setHighlightText(
                itemMaster.item_name, query1, context.getColor(R.color.colorPrimary)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyItemProductHolder {
        val binding: ItemProductRowBinding =
            ItemProductRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyItemProductHolder(binding)
    }

    override fun getItemCount(): Int = itemMasterList.size

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    // Override getItemId to return the unique ID of each item
    override fun getItemId(position: Int): Long {
        return itemMasterList[position].id.toLong()
    }
    override fun onBindViewHolder(holder: MyItemProductHolder, position: Int) {

        holder.bind(itemMasterList[position], query, context)
        val itemMaster: ItemMaster = itemMasterList[position]


        holder.binding.txtProductMRP.text =
            "${context.getString(R.string.rs)}" + itemMaster.mrp.toString()
        holder.binding.txtTotalQty.text = itemMaster.stock_qty.toString()
        holder.binding.edtMargin.setText(String.format("%.1f", itemMaster.margin))

        holder.binding.edtAddQty.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                recyclerView.post {
                    recyclerView.smoothScrollToPosition(position)
                }
            }
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    val input = s.toString()
                    if (input.isNotEmpty()) {
                        val current = s.toString().toDoubleOrNull() ?: 0.0
                        btnRequestOrder.visibility = View.VISIBLE
                        holder.binding.ivClearProduct.visibility = View.VISIBLE
                        holder.binding.linerTotal.visibility = View.VISIBLE
                        //  val total = holder.binding.txtTotalQty.text.toString().toDouble()
//                val total = holder.binding.txtTotalQty.text.toString().toDouble()
//                holder.binding.txtTotalQty.text = (total - itemMaster.edtxt_qty).toString()
//                itemMaster.stock_qty = total
                        holder.binding.txtTotalQty.text =
                            (itemMaster.stock_qty - current).toString()
                        itemMaster.edtxt_qty = current

                        var netSaleRateRounded = (itemMaster.mrp / itemMaster.margin).toString()
                        netSaleRate = roundValues(netSaleRateRounded.toDouble())
                        holder.binding.txtNetSale.text = netSaleRate.toString()
                        holder.binding.txtCountQty.text = current.toString()

                        var subTotalRound = (netSaleRate * current).toString()
                        subTotalResult = roundValues(subTotalRound.toDouble()).toString()
                        holder.binding.txtProductSubTotal.text = subTotalResult
                        itemMaster.txt_subTotal = subTotalResult.toDouble()

                        Log.e(TAG, "onBindViewHolder: ${itemMasterList[0].edtxt_qty}")

//                 GlobalScope.launch(Dispatchers.IO) {
//                    accountDao.updateItem(item = itemMaster.copy(txt_subTotal = total))
//                }
                    } else {
                        btnRequestOrder.visibility = View.GONE
                        holder.binding.ivClearProduct.visibility = View.GONE
                        holder.binding.linerTotal.visibility = View.GONE

                        holder.binding.txtTotalQty.text = itemMaster.stock_qty.toString()
                        itemMaster.edtxt_qty = 0.0
                        holder.binding.txtNetSale.text = 0.0.toString()
                        holder.binding.txtCountQty.text = 0.0.toString()
                        holder.binding.txtProductSubTotal.text = 0.0.toString()
                    }
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        val twAddQtyFree = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    val input = s.toString()
                    if (input.isNotEmpty()) {
                        val current = s.toString().toDoubleOrNull() ?: 0.0
                    } else {

                    }
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        val twAddScm = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    val input = s.toString()
                    if (input.isNotEmpty()) {
                        val current = s.toString().toDoubleOrNull() ?: 0.0
                    } else {

                    }
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }


        btnRequestOrder.setOnClickListener {
            Log.e(TAG, "onBindViewHolder:--> ${itemMaster.edtxt_qty}")
            Log.e(TAG, "onBindViewHolder SubTotal:--> ${itemMaster.txt_subTotal}")
        }


        holder.binding.edtAddQty.addTextChangedListener(textWatcher)
        holder.binding.edtAddQty.tag = textWatcher
        holder.binding.edtAddQtyFree.addTextChangedListener(twAddQtyFree)
        holder.binding.edtAddQtyFree.tag = twAddQtyFree
        holder.binding.edtAddScm.addTextChangedListener(twAddScm)
        holder.binding.edtAddScm.tag = twAddScm

        Log.e(TAG, "onBindViewHolder:--> $position  ${itemMaster.id} ${itemMaster.item_name} , ${itemMaster.edtxt_qty}   ${itemMaster.txt_subTotal}")

        holder.binding.ivClearProduct.setOnClickListener {
            holder.binding.txtTotalQty.text = itemMaster.stock_qty.toString()
            itemMaster.edtxt_qty = 0.0

            holder.binding.edtAddQty.text.clear()
            holder.binding.edtAddQtyFree.text.clear()
            holder.binding.edtAddScm.text.clear()

            holder.binding.txtNetSale.text = 0.0.toString()
            holder.binding.txtCountQty.text = 0.0.toString()
            holder.binding.txtProductSubTotal.text = 0.0.toString()
        }

    }

    fun roundValues(roundValue: Double): Double {
        // Rounding to two decimal places using BigDecimal
        return BigDecimal(roundValue).setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    fun updateData(filteredList: MutableList<ItemMaster>, query: String) {
        this.itemMasterList = filteredList
        this.query = query
        notifyDataSetChanged()
    }
}