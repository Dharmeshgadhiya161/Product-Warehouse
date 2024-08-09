package com.sunil.dhwarehouse.RoomDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface InvoiceDao {
//    @Insert
//    suspend fun insert(invoiceMaster: InvoiceMaster)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(invoices: List<InvoiceMaster>)

    @Query("SELECT * FROM invoice_master")
    suspend fun getInvoiceMaster(): List<InvoiceMaster>

    @Query("DELETE FROM invoice_master WHERE id = :itemId")
    suspend fun deleteById(itemId: Int)

    @Query("DELETE FROM invoice_master")
    suspend fun deleteAllInvoiceItem()
}