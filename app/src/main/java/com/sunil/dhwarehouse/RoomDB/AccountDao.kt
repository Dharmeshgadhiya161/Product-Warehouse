package com.sunil.dhwarehouse.RoomDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AccountDao {

    @Insert
    suspend fun insert(acount : AccountMaster)

    @Query("SELECT * from account_master")
    suspend fun getAccountMaster() : List<AccountMaster>

    @Query("DELETE FROM account_master")
    suspend fun deleteAllAccounts()
}