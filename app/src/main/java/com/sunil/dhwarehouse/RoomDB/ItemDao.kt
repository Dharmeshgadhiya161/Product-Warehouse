package com.sunil.dhwarehouse.RoomDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ItemDao {

    @Insert
    suspend fun insert(item: ItemMaster)

    @Query("SELECT * FROM item_master")
    suspend fun getItemMaster() : List<ItemMaster>

    @Query("DELETE FROM item_master")
    suspend fun deleteAllItems()
}