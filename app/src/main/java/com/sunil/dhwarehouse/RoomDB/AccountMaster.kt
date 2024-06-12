package com.sunil.dhwarehouse.RoomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account_master")
data class AccountMaster(
    @PrimaryKey(autoGenerate = true) var id : Int = 0,
    var sales_man : String="",
    var account_name : String="",
    var addess : String="",
    var mobile_no : String="",
    var opening_balance : Double = 0.0,
    var account_type : String="",
    var party_type : String="",
    var day : String="",
)
