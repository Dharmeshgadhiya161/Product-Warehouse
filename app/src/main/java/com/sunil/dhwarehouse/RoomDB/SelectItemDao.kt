package com.sunil.dhwarehouse.RoomDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface SelectItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ItemMaster)

    @Query("SELECT * FROM item_master")
    fun getItemMaster() : List<ItemMaster>

    @Query("DELETE FROM item_master")
    suspend fun deleteAllItems()

//    @Query("DELETE FROM item_master WHERE id = :itemId")
//    suspend fun deleteById(itemId: Int)
    @Update
    suspend fun updateItem(item: ItemMaster)

}