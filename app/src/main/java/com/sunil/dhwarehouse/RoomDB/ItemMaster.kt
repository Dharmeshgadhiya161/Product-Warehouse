package com.sunil.dhwarehouse.RoomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "item_master")
data class ItemMaster(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var category: String = "",
    var company_name: String = "",
    var brand: String = "",
    var item_name: String = "",
    var mrp: Double = 0.0,
    var purchase_rate: Double = 0.0,
    var sale_rate: Double = 0.0,
    var margin: Double = 0.0,
    var stock_qty: Double = 0.0,
    var stock_amount: Double = 0.0,
    var edtxt_qty: Double = 0.0,
    var edtxt_free: Double = 0.0,
    var edtxt_scm: Double = 0.0,
    var txt_net_rate: Double = 0.0,
    var txt_subTotal: Double = 0.0,
    var old_margin: Double = 0.0,
    var old_stockQty: Double = 0.0
)
