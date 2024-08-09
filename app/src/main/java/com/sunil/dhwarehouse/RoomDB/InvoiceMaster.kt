package com.sunil.dhwarehouse.RoomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoice_master")
data class InvoiceMaster(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var salesName: String = "",
    var account_name: String = "",
    var address: String = "",
    var mobile_no: String = "",
    var date: String = "",
    var time: String = "",
    var productItemName: String = "",
    var qty: Double = 0.0,
    var free: Double = 0.0,
    var scm: Double = 0.0,
    var rate: Double = 0.0,
    var subTotal: Double = 0.0,

)
