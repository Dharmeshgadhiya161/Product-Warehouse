package com.sunil.dhwarehouse.RoomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "item_master")
data class ItemMaster(
    @PrimaryKey(autoGenerate = true) var id : Int = 0,
    var category : String ="",
    var company_name : String = "",
    var brand : String = "",
    var item_name : String = "",
    //var unit : String = "",

    var mrp : Double = 0.0,
    var purchase_rate : Double = 0.0,
    var sale_rate : Double = 0.0,
    var stock_qty : Double = 0.0,
    var stock_amount : Double = 0.0,

//    var company_margin : Double = 0.0,
//    var inner_pcs : Double = 0.0,
//    var case_pcs : Double = 0.0,
//    var item_Status : String = "",
//    var dump_day : Double = 0.0,
)
