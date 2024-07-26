package com.sunil.dhwarehouse.model

import androidx.lifecycle.LiveData
import com.sunil.dhwarehouse.RoomDB.ItemDao
import com.sunil.dhwarehouse.RoomDB.ItemMaster

class MyRepository(private val myDao: ItemDao) {
    fun getAllItems(): List<ItemMaster> {
        return myDao.getItemMaster()
    }

    suspend fun updateItem(item: ItemMaster) {
        myDao.updateItem(item)
    }
}