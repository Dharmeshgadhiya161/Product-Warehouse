package com.sunil.dhwarehouse.roomDB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

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
    @Update
    suspend fun updateItem(item: InvoiceMaster)

    @Delete
    suspend fun deleteInvoices(invoices: List<InvoiceMaster>)

    @Delete
    suspend fun deleteInvoicesList(invoices: InvoiceMaster)
}