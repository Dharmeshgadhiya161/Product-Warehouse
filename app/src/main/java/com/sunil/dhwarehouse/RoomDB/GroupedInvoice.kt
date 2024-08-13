package com.sunil.dhwarehouse.RoomDB

data class GroupedInvoice(
    val accountName: String,
    val date: String,
    val time:String,
    val invoices: List<InvoiceMaster>
)