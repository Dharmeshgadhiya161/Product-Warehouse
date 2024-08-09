package com.sunil.dhwarehouse.adapter

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sunil.dhwarehouse.R
import com.sunil.dhwarehouse.RoomDB.ItemDao
import com.sunil.dhwarehouse.RoomDB.ItemMaster
import com.sunil.dhwarehouse.databinding.ItemProductRowBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

class ItemProductAdapter(
    var context: Activity,
    private var itemMasterList: MutableList<ItemMaster>,
    private var query: String = "",
    private var accountDao: ItemDao,
    private var btnRequestOrder: RelativeLayout,
    private var recyclerView: RecyclerView,
    var txtSubtotalRS: TextView,
    var txtTotalItem: TextView,
    private var btnClickRequestOrder: TextView,
   private var totalDataitemMasterList1: MutableList<ItemMaster>
) : RecyclerView.Adapter<ItemProductAdapter.ViewHolder>() {
    private var TAG = "ItemProductAdapter"
    var subTotalResult = " "
    var netSaleRate = 0.0
    var freeQty = 0.0
    var saleRate = 0.0
    var scmRs = 0.0
    var selectItemList: MutableList<ItemMaster> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemProductRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(
            binding,
            MyCustomEditTextListener(),
            FreeQtyEditTextListener(),
            ScmRsEditTextListener(),
            CustomMarginEditTextListener()
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemMaster: ItemMaster = itemMasterList[position]

        holder.binding.txtItemProductName.setHighlightText(
            itemMaster.item_name, query, context.getColor(R.color.colorPrimary)
        )

        holder.binding.txtProductMRP.text =
            "${context.getString(R.string.rs)}" + itemMaster.mrp.toString()

        holder.binding.txtTotalQty.text = itemMaster.stock_qty.toInt().toString()

        holder.myCustomEditTextListener.updatePosition(
            holder.adapterPosition, holder.binding, itemMaster
        )

        holder.freeQtyEditTextListener.updatePosition(
            holder.adapterPosition, holder.binding, itemMaster
        )

        holder.scmRsEditTextListener.updatePosition(
            holder.adapterPosition, holder.binding, itemMaster
        )

        holder.customMarginEditTextListener.updatePosition(
            holder.adapterPosition, holder.binding, itemMaster
        )

        // Check if the value is 0.0 or the EditText is empty
        // scroll time update data
        if (itemMasterList[holder.adapterPosition].edtxt_qty == 0.0) {
            holder.binding.edtAddQty.setText("")
            holder.binding.edtAddQtyFree.setText("")
            holder.binding.edtAddScm.setText("")

            holder.binding.txtNetSale.text = ""
            holder.binding.txtCountQty.text = ""
            holder.binding.txtProductSubTotal.text = ""

            holder.binding.ivClearProduct.visibility = View.GONE
            holder.binding.linerTotal.visibility = View.GONE

        } else {
            holder.binding.edtAddQty.setText(
                itemMasterList[holder.adapterPosition].edtxt_qty.toInt().toString()
            )

            if (itemMasterList[holder.adapterPosition].edtxt_free.toInt() != 0) {
                holder.binding.edtAddQtyFree.setText(
                    itemMasterList[holder.adapterPosition].edtxt_free.toInt().toString()
                )
            } else {
                holder.binding.edtAddQtyFree.setText("")
            }

            if (itemMasterList[holder.adapterPosition].edtxt_scm.toInt() != 0) {
                holder.binding.edtAddScm.setText(
                    itemMasterList[holder.adapterPosition].edtxt_scm.toInt().toString()
                )
            } else {
                holder.binding.edtAddScm.setText("")
            }

            selectItemListData()

            holder.binding.edtAddQtyFree.enabled()
            holder.binding.edtAddScm.enabled()

        }
        holder.binding.edtMargin.setText(
            roundValues(itemMasterList[holder.adapterPosition].margin).toString()
        )

        if (itemMasterList[holder.adapterPosition].edtxt_scm > 0.0) {
            holder.binding.edtAddQtyFree.disable()
            holder.binding.edtAddScm.enabled()
        } else if (itemMasterList[holder.adapterPosition].edtxt_free > 0.0) {
            holder.binding.edtAddScm.disable()
            holder.binding.edtAddQtyFree.enabled()
        } else {
            holder.binding.edtAddQtyFree.enabled()
            holder.binding.edtAddScm.enabled()
        }


        holder.binding.txtProductSubTotal.text =
            itemMasterList[holder.adapterPosition].txt_subTotal.toString()


        holder.binding.ivClearProduct.setOnClickListener {
            holder.binding.txtTotalQty.text = itemMaster.stock_qty.toInt().toString()
            itemMasterList[holder.adapterPosition].apply {
                edtxt_qty = 0.0
                edtxt_free = 0.0
                edtxt_scm = 0.0
                txt_net_rate = 0.0
                txt_subTotal = 0.0
            }

            /*===================================================================*/
            /*TODO  this code use to custom change margin values after clear data old margin set */
            GlobalScope.launch(Dispatchers.IO) {
                accountDao.updateItem(item = itemMaster.copy(margin = itemMasterList[position].old_margin))
            }
            itemMasterList[position].margin = itemMasterList[position].old_margin
            holder.binding.edtMargin.setText(roundValues(itemMasterList[holder.adapterPosition].margin).toString())

            /*========================================================================*/

            holder.binding.edtAddQty.text.clear()
            holder.binding.edtAddQtyFree.text.clear()
            holder.binding.edtAddScm.text.clear()

            holder.binding.txtNetSale.text = 0.0.toString()
            holder.binding.txtCountQty.text = 0.toInt().toString()
            holder.binding.txtProductSubTotal.text = 0.0.toString()

            holder.binding.edtAddQtyFree.enabled()
            holder.binding.edtAddScm.enabled()

            btnRequestOrder.visibility = if (selectItemList.size == 0) View.GONE else View.VISIBLE
        }
    }


    override fun getItemCount(): Int {
        return itemMasterList.size
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        holder.enableTextWatcher()
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        holder.disableTextWatcher()
    }

    class ViewHolder(
        val binding: ItemProductRowBinding,
        var myCustomEditTextListener: MyCustomEditTextListener,
        var freeQtyEditTextListener: FreeQtyEditTextListener,
        var scmRsEditTextListener: ScmRsEditTextListener,
        var customMarginEditTextListener: CustomMarginEditTextListener
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.edtAddQty.addTextChangedListener(myCustomEditTextListener)
            binding.edtAddQtyFree.addTextChangedListener(freeQtyEditTextListener)
            binding.edtAddScm.addTextChangedListener(scmRsEditTextListener)
            binding.edtMargin.addTextChangedListener(customMarginEditTextListener)
        }

        fun enableTextWatcher() {
            binding.edtAddQty.addTextChangedListener(myCustomEditTextListener)
            binding.edtAddQtyFree.addTextChangedListener(freeQtyEditTextListener)
            binding.edtAddScm.addTextChangedListener(scmRsEditTextListener)
            binding.edtMargin.addTextChangedListener(customMarginEditTextListener)
        }

        fun disableTextWatcher() {
            binding.edtAddQty.removeTextChangedListener(myCustomEditTextListener)
            binding.edtAddQtyFree.removeTextChangedListener(freeQtyEditTextListener)
            binding.edtAddScm.removeTextChangedListener(scmRsEditTextListener)
            binding.edtMargin.removeTextChangedListener(customMarginEditTextListener)
        }


    }

    inner class MyCustomEditTextListener : TextWatcher {
        private var position = 0
        private lateinit var binding: ItemProductRowBinding
        private lateinit var itemMaster: ItemMaster
        fun updatePosition(
            position: Int, binding1: ItemProductRowBinding, itemMaster1: ItemMaster
        ) {
            this.position = position
            this.binding = binding1
            this.itemMaster = itemMaster1
        }

        override fun beforeTextChanged(
            charSequence: CharSequence, start: Int, count: Int, after: Int
        ) {
        }

        override fun onTextChanged(
            charSequence: CharSequence, start: Int, before: Int, count: Int
        ) {
            val input = charSequence.toString()
            if (input.isNotEmpty() && input.isNotBlank() && input != ".") {
                itemMasterList[position].edtxt_qty = input.toDouble()

                //todo Free Qty values hoy tare and editText ma qty change after total Qty - Free Qty
                if (itemMasterList[position].edtxt_free > 0.0) {
                    freeQty = itemMaster.stock_qty - itemMaster.edtxt_free - input.toDouble()
                    binding.txtTotalQty.text = freeQty.toString()

                    Log.e(TAG, "onTextChanged freeQtyQty NetSaleRate:<freeQty> $freeQty")

                    updateQtyItem(input)
                    //TODO this code use already scm values edit box add and qty values add this code use
                } else if (itemMasterList[position].edtxt_scm > 0.0) {
                    binding.txtCountQty.text = input
                    binding.txtTotalQty.text =
                        (itemMaster.stock_qty - itemMasterList[position].edtxt_qty).toString()

                    saleRate = roundValues(calculateNetSaleRate(itemMaster.mrp, itemMaster.margin))
                    scmRs = (saleRate * itemMasterList[position].edtxt_scm) / 100
                    netSaleRate = saleRate - scmRs
                    itemMasterList[position].txt_net_rate = roundValues(netSaleRate)
                    binding.txtNetSale.text = roundValues(netSaleRate).toString()
                    subTotalResult = calculateSubTotalRound(
                        netSaleRate,
                        itemMasterList[position].edtxt_qty
                    ).toString()
                    binding.txtProductSubTotal.text = subTotalResult

                    itemMasterList[position].txt_subTotal = subTotalResult.toDouble()

                    binding.edtAddQtyFree.disable()


                    GlobalScope.launch(Dispatchers.IO) {
                        accountDao.updateItem(item = itemMaster.copy(edtxt_qty = input.toDouble()))
                        accountDao.updateItem(item = itemMaster.copy(txt_net_rate = roundValues(netSaleRate)))
                        accountDao.updateItem(item = itemMaster.copy(txt_subTotal = subTotalResult.toDouble()))
                    }

                    binding.ivClearProduct.visibility = View.VISIBLE
                    binding.linerTotal.visibility = View.VISIBLE
                    btnRequestOrder.visibility = View.VISIBLE
                } else {
                    binding.txtTotalQty.text =
                        (itemMaster.stock_qty - itemMasterList[position].edtxt_qty).toString()
                    Log.e(TAG, "onTextChanged Qty NetSaleRate:<Qty>")
                    updateQtyItem(input)
                }

                selectItemListData()
            } else {
                //TODO use to if free edit text data hoy ne and qty data 0 hoy tare total qty data update
                if (itemMasterList[position].edtxt_free > 0.0) {
                    freeQty = itemMaster.stock_qty - itemMaster.edtxt_free
                    binding.txtTotalQty.text = freeQty.toInt().toString()

                    GlobalScope.launch(Dispatchers.IO) {
                        accountDao.updateItem(item = itemMaster.copy(edtxt_qty = 0.0))
                        accountDao.updateItem(item = itemMaster.copy(txt_net_rate = 0.0))
                        accountDao.updateItem(item = itemMaster.copy(txt_subTotal = 0.0))
                    }

                    binding.ivClearProduct.visibility = View.GONE
                    binding.linerTotal.visibility = View.GONE

                    itemMasterList[position].edtxt_qty = 0.0
                    itemMasterList[position].txt_net_rate = 0.0
                    itemMasterList[position].txt_subTotal = 0.0

                    selectItemList.remove(itemMaster)
                } else {

                    GlobalScope.launch(Dispatchers.IO) {
                        accountDao.updateItem(item = itemMaster.copy(edtxt_qty = 0.0))
                        accountDao.updateItem(item = itemMaster.copy(txt_net_rate = 0.0))
                        accountDao.updateItem(item = itemMaster.copy(txt_subTotal = 0.0))
                    }
                    binding.txtTotalQty.text = itemMaster.stock_qty.toString()
                    binding.ivClearProduct.visibility = View.GONE
                    binding.linerTotal.visibility = View.GONE

                    itemMasterList[position].edtxt_qty = 0.0
                    itemMasterList[position].txt_net_rate = 0.0
                    itemMasterList[position].txt_subTotal = 0.0

                    selectItemList.remove(itemMaster)
                }

                selectItemListData()

            }
        }

        private fun updateQtyItem(input: String) {
            netSaleRate = calculateNetSaleRate(itemMaster.mrp, itemMaster.margin)
            itemMasterList[position].txt_net_rate = netSaleRate
            binding.txtNetSale.text = netSaleRate.toString()
            binding.txtCountQty.text = input

            subTotalResult = calculateSubTotalRound(
                netSaleRate,
                itemMasterList[position].edtxt_qty
            ).toString()
            binding.txtProductSubTotal.text = subTotalResult
            itemMasterList[position].txt_subTotal = subTotalResult.toDouble()


            GlobalScope.launch(Dispatchers.IO) {
                accountDao.updateItem(item = itemMaster.copy(edtxt_qty = input.toDouble()))
                accountDao.updateItem(item = itemMaster.copy(txt_net_rate = itemMasterList[position].txt_net_rate))
                accountDao.updateItem(item = itemMaster.copy(txt_subTotal = itemMasterList[position].txt_subTotal))
            }

            Log.e(TAG, "onTextChanged Qty NetSaleRate:<Qty> $netSaleRate")

            binding.ivClearProduct.visibility = View.VISIBLE
            binding.linerTotal.visibility = View.VISIBLE
            btnRequestOrder.visibility = View.VISIBLE
        }

        override fun afterTextChanged(editable: Editable) {

        }

    }

    private fun selectItemListData() {
        selectItemList = totalDataitemMasterList1.filter { it.edtxt_qty > 0.0 }
            .toMutableList() as ArrayList<ItemMaster>
//        for (item in selectItemList) {
//            Log.e(TAG, "selectItemList--> $item ")
//        }
        val totalSubTotal = selectItemList.sumOf { it.txt_subTotal }
        txtSubtotalRS.text =
            "${context.getString(R.string.rs)}" + roundValues(totalSubTotal).toString()

        txtTotalItem.text = selectItemList.size.toString() + " Item"

        btnRequestOrder.visibility = if (selectItemList.size == 0) View.GONE else View.VISIBLE


    }

    inner class FreeQtyEditTextListener : TextWatcher {
        private var position = 0
        private lateinit var binding: ItemProductRowBinding
        private lateinit var itemMaster: ItemMaster
        fun updatePosition(
            position: Int, binding1: ItemProductRowBinding, itemMaster1: ItemMaster
        ) {
            this.position = position
            this.binding = binding1
            this.itemMaster = itemMaster1
        }

        override fun beforeTextChanged(
            charSequence: CharSequence, start: Int, count: Int, after: Int
        ) {
        }

        override fun onTextChanged(
            charSequence: CharSequence,
            start: Int,
            before: Int,
            count: Int
        ) {
            val input = charSequence.toString()
            if (input.isNotEmpty() && input.isNotBlank() && input != ".") {
                itemMasterList[position].edtxt_free = input.toDouble()
                freeQty = itemMaster.stock_qty - itemMaster.edtxt_qty - input.toDouble()
                binding.txtTotalQty.text = freeQty.toString()

                binding.edtAddScm.disable()

                GlobalScope.launch(Dispatchers.IO) {
                    accountDao.updateItem(item = itemMaster.copy(edtxt_free = itemMasterList[position].edtxt_free))
                    accountDao.updateItem(item = itemMaster.copy(edtxt_scm = 0.0))
                }


            } else {
                binding.edtAddScm.enabled()
                //TODO  free Data 0/empty kari tare.jo qty edit text data hoy tare total qty data update
                if (itemMasterList[position].edtxt_qty > 0.0) {
                    binding.txtTotalQty.text =
                        (itemMaster.stock_qty - itemMasterList[position].edtxt_qty).toString()
                    itemMasterList[position].edtxt_free = 0.0
                } else {
                    binding.txtTotalQty.text = itemMaster.stock_qty.toString()
                    itemMasterList[position].edtxt_free = 0.0

                    GlobalScope.launch(Dispatchers.IO) {
                        accountDao.updateItem(item = itemMaster.copy(edtxt_free = 0.0))
                    }
                }
            }
        }

        override fun afterTextChanged(editable: Editable) {
        }

    }

    inner class ScmRsEditTextListener : TextWatcher {
        private var position = 0
        private lateinit var binding: ItemProductRowBinding
        private lateinit var itemMaster: ItemMaster
        fun updatePosition(
            position: Int, binding1: ItemProductRowBinding, itemMaster1: ItemMaster
        ) {
            this.position = position
            this.binding = binding1
            this.itemMaster = itemMaster1
        }

        override fun beforeTextChanged(
            charSequence: CharSequence, start: Int, count: Int, after: Int
        ) {
        }

        override fun onTextChanged(
            charSequence: CharSequence, start: Int, before: Int, count: Int
        ) {
            try {
                val input = charSequence.toString()
                if (input.isNotEmpty() && input.isNotBlank() && input != ".") {
                    //TODO mrp/marin = saleRate
                    //TODO saleRate * scm(%)/100 = scmRs
                    // TODO netSales = saleRate - scmRs
                    var ediScmValue = input.toDouble()

                    itemMasterList[position].edtxt_scm = ediScmValue
                    saleRate = roundValues(calculateNetSaleRate(itemMaster.mrp, itemMaster.margin))
                    scmRs = (saleRate * ediScmValue) / 100
                    netSaleRate = saleRate - scmRs
                    itemMasterList[position].txt_net_rate = roundValues(netSaleRate)
                    binding.txtNetSale.text = roundValues(netSaleRate).toString()
                    subTotalResult = calculateSubTotalRound(
                        netSaleRate,
                        itemMasterList[position].edtxt_qty
                    ).toString()
                    binding.txtProductSubTotal.text = subTotalResult
                    itemMasterList[position].txt_subTotal = subTotalResult.toDouble()

                    binding.edtAddQtyFree.disable()
                    Log.e(TAG, "onTextChanged ScmRs NetSaleRate:<ScmRs> $netSaleRate")

                    GlobalScope.launch(Dispatchers.IO) {
                        accountDao.updateItem(item = itemMaster.copy(edtxt_scm = ediScmValue))
                        accountDao.updateItem(item = itemMaster.copy(edtxt_free = 0.0))
                        accountDao.updateItem(item = itemMaster.copy(txt_net_rate = roundValues(netSaleRate)))
                        accountDao.updateItem(item = itemMaster.copy(txt_subTotal = subTotalResult.toDouble()))
                    }
                    selectItemListData()

                    /*TODO this is use custom margin add after use this code*/
                    if (itemMasterList[position].edtxt_scm != 0.0
                        && itemMasterList[position].margin != itemMasterList[position].old_margin
                    ) {
                        netSaleRate = customMarginCalculateResult(
                            itemMasterList[position].mrp,
                            itemMasterList[position].margin,
                            itemMasterList[position].edtxt_scm
                        ).toDouble()

                        itemMasterList[position].txt_net_rate = netSaleRate
                        Log.e(TAG, "onTextChanged ediMargin NetSaleRate:<ma> $netSaleRate")
                        binding.txtNetSale.text = roundValues(netSaleRate).toString()
                        subTotalResult = calculateSubTotalRound(
                            netSaleRate,
                            itemMasterList[position].edtxt_qty
                        ).toString()
                        binding.txtProductSubTotal.text = subTotalResult
                        itemMasterList[position].txt_subTotal = subTotalResult.toDouble()

                        GlobalScope.launch(Dispatchers.IO) {
                            accountDao.updateItem(item = itemMaster.copy(margin = itemMasterList[position].margin))
                            accountDao.updateItem(item = itemMaster.copy(txt_net_rate = roundValues(netSaleRate)))
                            accountDao.updateItem(item = itemMaster.copy(txt_subTotal = subTotalResult.toDouble()))
                        }

                        selectItemListData()
                    }


                } else {
                    itemMasterList[position].edtxt_scm = 0.0
                    binding.edtAddQtyFree.enabled()
                    /*ToDO this scm 0 hoy to only qty data
                     * edit text Qty  */

                    netSaleRate = calculateNetSaleRate(itemMaster.mrp, itemMaster.margin)
                    itemMasterList[position].txt_net_rate = netSaleRate
                    binding.txtNetSale.text = netSaleRate.toString()
                    binding.txtCountQty.text = itemMasterList[position].edtxt_qty.toString()

                    subTotalResult = calculateSubTotalRound(
                        netSaleRate,
                        itemMasterList[position].edtxt_qty
                    ).toString()
                    binding.txtProductSubTotal.text = subTotalResult
                    itemMasterList[position].txt_subTotal = subTotalResult.toDouble()

                    GlobalScope.launch(Dispatchers.IO) {
                        accountDao.updateItem(item = itemMaster.copy(edtxt_scm = 0.0))
                        accountDao.updateItem(item = itemMaster.copy(edtxt_qty = itemMasterList[position].edtxt_qty))
                        accountDao.updateItem(item = itemMaster.copy(txt_net_rate = netSaleRate))
                        accountDao.updateItem(item = itemMaster.copy(txt_subTotal = subTotalResult.toDouble()))
                    }

                    selectItemListData()

                }
            } catch (e: NumberFormatException) {
                Log.d(TAG, "catch onTextChanged:${e.message}")
            }
        }

        override fun afterTextChanged(editable: Editable) {
        }
    }

    inner class CustomMarginEditTextListener : TextWatcher {
        private var position = 0
        private lateinit var binding: ItemProductRowBinding
        lateinit var itemMaster: ItemMaster
        fun updatePosition(
            position: Int, binding1: ItemProductRowBinding, itemMaster1: ItemMaster
        ) {
            this.position = position
            this.binding = binding1
            this.itemMaster = itemMaster1
        }

        override fun beforeTextChanged(
            charSequence: CharSequence, start: Int, count: Int, after: Int
        ) {
        }

        override fun onTextChanged(
            charSequence: CharSequence, start: Int, before: Int, count: Int
        ) {
            try {
                val input = charSequence.toString()
                if (input.isNotEmpty() && input.isNotBlank() && input != "." && input != "0") {
                    var ediMargin = input.toDouble()

                    if (itemMasterList[position].edtxt_scm != 0.0 && ediMargin != itemMasterList[position].old_margin) {

                        itemMasterList[position].margin = ediMargin

                        netSaleRate = customMarginCalculateResult(
                            itemMasterList[position].mrp,
                            itemMasterList[position].margin,
                            itemMasterList[position].edtxt_scm
                        ).toDouble()

                        itemMasterList[position].txt_net_rate = netSaleRate
                        Log.e(TAG, "onTextChanged ediMargin NetSaleRate:<ma> $netSaleRate")
                        binding.txtNetSale.text = roundValues(netSaleRate).toString()
                        subTotalResult = calculateSubTotalRound(
                            netSaleRate,
                            itemMasterList[position].edtxt_qty
                        ).toString()
                        binding.txtProductSubTotal.text = subTotalResult
                        itemMasterList[position].txt_subTotal = subTotalResult.toDouble()

                        GlobalScope.launch(Dispatchers.IO) {
                            accountDao.updateItem(item = itemMaster.copy(margin = ediMargin))
                            accountDao.updateItem(item = itemMaster.copy(txt_net_rate = netSaleRate))
                            accountDao.updateItem(item = itemMaster.copy(txt_subTotal = subTotalResult.toDouble()))
                        }

                        selectItemListData()
                    }else{

                    }

                } else {
                    if (input.isEmpty() || input == "0") {
                        GlobalScope.launch(Dispatchers.IO) {
                            accountDao.updateItem(item = itemMaster.copy(margin = itemMasterList[position].old_margin))
                        }
                        itemMasterList[position].margin = itemMasterList[position].old_margin
                        Log.e(
                            TAG,
                            "DO not onTextChanged:-->EMpty -- >margin ${itemMasterList[position].margin}"
                        )
                        selectItemListData()
                    } else {
                        Log.d("TextWatcher", "Text is neither empty nor '0'")
                    }


                }
            } catch (e: NumberFormatException) {
                Log.d(TAG, "catch onTextChanged:${e.message}")
            }
        }

        override fun afterTextChanged(editable: Editable) {
        }
    }


    //todo   mrp/margin = netSaleRate
    private fun calculateNetSaleRate(mrp: Double, margin: Double): Double {
        return roundValues(mrp / margin)
    }

    //TODO subTOTal Item Wise Simple  subTotal = netSalRate * edtQty
    private fun calculateSubTotalRound(netSaleRate: Double, edtQty: Double): Double {
        return roundValues(netSaleRate * edtQty)
    }

    private fun roundValues(roundValue: Double): Double {
        // Rounding to two decimal places using BigDecimal
        return BigDecimal(roundValue).setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    fun customMarginCalculateResult(mrp: Double, margin: Double, edtxtScm: Double): BigDecimal {
//        val result = (192 - ((192 / 1.1) * 20 / 100))
        val result = mrp - ((mrp / margin) * edtxtScm / 100.0)
        return BigDecimal(result).setScale(2, RoundingMode.HALF_UP) // Round to 2 decimal places
    }

    fun updateData(filteredList: MutableList<ItemMaster>, query: String) {
        this.itemMasterList = filteredList
        this.query = query
        notifyDataSetChanged()
    }
    fun updateTotalData(filteredList: MutableList<ItemMaster>) {
        this.totalDataitemMasterList1 = filteredList
        selectItemListData()
    }

    fun EditText.disable() {
        this.isEnabled = false
    }

    fun EditText.enabled() {
        this.isEnabled = true
    }

}
