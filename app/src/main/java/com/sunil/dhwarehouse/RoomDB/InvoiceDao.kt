package com.sunil.dhwarehouse.RoomDB

import androidx.room.Insert
import androidx.room.Query

interface InvoiceDao {
    @Insert
    suspend fun insert(invoiceMaster: InvoiceMaster)

    @Query("SELECT * FROM invoice_master")
    suspend fun getInvoiceMaster():List<InvoiceMaster>

    @Query("DELETE FROM invoice_master WHERE id = :itemId")
    suspend fun deleteById(itemId: Int)

    @Query("DELETE FROM invoice_master")
    suspend fun deleteAllInvoiceItem()
}