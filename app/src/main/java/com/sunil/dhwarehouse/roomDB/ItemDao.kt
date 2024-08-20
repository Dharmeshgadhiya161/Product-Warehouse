package com.sunil.dhwarehouse.roomDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ItemDao {

    @Insert
    suspend fun insert(item: ItemMaster)

    @Query("SELECT * FROM item_master")
    fun getItemMaster() : List<ItemMaster>

    @Query("DELETE FROM item_master")
    suspend fun deleteAllItems()

    @Update
    suspend fun updateItem(item: ItemMaster)

}