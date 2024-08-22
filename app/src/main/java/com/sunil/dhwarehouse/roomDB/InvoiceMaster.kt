package com.sunil.dhwarehouse.roomDB

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Entity(tableName = "invoice_master")
@Parcelize
data class InvoiceMaster(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var no: Int = 1,
    var salesName: String = "",
    var account_name: String = "",
    var address: String = "",
    var mobile_no: String = "",
    var date: String = "",
    var time: String = "",
    var timeSecond: String = "",
    var productItemName: String = "",
    var mrp: Double = 0.0,
    var qty: Int = 0,
    var free: Int = 0,
    var scm: Double = 0.0,
    var rate: Double = 0.0,
    var amount: Double = 0.0,
    var receiveAmount: Double = 0.0

) : Parcelable
