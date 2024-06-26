package com.sunil.dhwarehouse.adapter

import android.app.Activity
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sunil.dhwarehouse.R
import com.sunil.dhwarehouse.RoomDB.ClickItemCategory
import com.sunil.dhwarehouse.databinding.ItemCategroyRowBinding

class ItemCategoryAdapter(
    var context: Activity,
    private var categoryList: MutableList<String>,
    private var clickItemCategory: ClickItemCategory
) :
    RecyclerView.Adapter<ItemCategoryAdapter.MyItemCategoryHolder>() {
    private var selectedItemPosition: Int = 0

    class MyItemCategoryHolder(val binding: ItemCategroyRowBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(categoryName: String) {
            binding.txtItemCategory.text = categoryName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyItemCategoryHolder {
        val binding: ItemCategroyRowBinding =
            ItemCategroyRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyItemCategoryHolder(binding)
    }

    override fun getItemCount(): Int = categoryList.size

    override fun onBindViewHolder(holder: MyItemCategoryHolder, position: Int) {
        holder.bind(categoryList[position])

        if (position == selectedItemPosition) {
            holder.binding.cardClick.setCardBackgroundColor(context.getColor(R.color.colorAccent))
            holder.binding.txtItemCategory.setTextColor(context.getColor(R.color.white))
        }else{
            holder.binding.cardClick.setCardBackgroundColor(context.getColor(R.color.white))
            holder.binding.txtItemCategory.setTextColor(context.getColor(R.color.black))
        }

        holder.binding.cardClick.setOnClickListener {

            val position = holder.adapterPosition
            if (selectedItemPosition != position) {
                val previousItemPosition = selectedItemPosition
                selectedItemPosition = position
                notifyItemChanged(previousItemPosition)
                notifyItemChanged(selectedItemPosition)
            }
            clickItemCategory.onClickItemCat(categoryList[position])
        }
    }
}