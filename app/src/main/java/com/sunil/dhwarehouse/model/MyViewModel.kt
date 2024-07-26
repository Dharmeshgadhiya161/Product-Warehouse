package com.sunil.dhwarehouse.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunil.dhwarehouse.RoomDB.ItemMaster
import kotlinx.coroutines.launch

class MyViewModel(private val repository: MyRepository) : ViewModel() {
    val allItems: List<ItemMaster> = repository.getAllItems()

    fun updateItem(item: ItemMaster) {
        viewModelScope.launch {
            repository.updateItem(item)
        }
    }
}