package com.sunil.dhwarehouse.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sunil.dhwarehouse.RoomDB.AccountMaster
import com.sunil.dhwarehouse.RoomDB.ItemMaster
import com.sunil.dhwarehouse.databinding.ItemCategroyRowBinding
import com.sunil.dhwarehouse.databinding.ItemProductRowBinding

class ItemProductAdapter(
    var context: Activity,
    private var itemMasterList: MutableList<ItemMaster>,
    private var query: String = ""
) :
    RecyclerView.Adapter<ItemProductAdapter.MyItemProductHolder>() {
    class MyItemProductHolder(private var binding: ItemProductRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(itemMaster: ItemMaster) {
            binding.txtItemProductName.text = itemMaster.item_name
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyItemProductHolder {
        val binding: ItemProductRowBinding =
            ItemProductRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyItemProductHolder(binding)
    }

    override fun getItemCount(): Int = itemMasterList.size

    override fun onBindViewHolder(holder: MyItemProductHolder, position: Int) {
        holder.bind(itemMasterList[position])
    }

    fun updateData(filteredList: MutableList<ItemMaster>, query: String) {
        this.itemMasterList = filteredList
        this.query = query
        notifyDataSetChanged()
    }

}