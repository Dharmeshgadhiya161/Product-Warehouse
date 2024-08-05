package com.sunil.dhwarehouse.common

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.StorageReference
import com.sunil.dhwarehouse.RoomDB.AccountMaster
import com.sunil.dhwarehouse.RoomDB.ItemMaster
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File

class ExcelFileHandler(private val context: Context,private val sharedPrefManager: SharedPrefManager) {

    companion object {
        private const val TAG = "ExcelFileHandler"
    }
    suspend fun readExcelAccountMasterFile(uri: Uri): ArrayList<AccountMaster> {
        return withContext(Dispatchers.IO) {
            Log.d("Excel Data", "Account Master Start1")

            var aryAccount: ArrayList<AccountMaster> = ArrayList()

            val inputSteam = context.contentResolver.openInputStream(uri)
            val workbook = XSSFWorkbook(inputSteam)
            val sheet = workbook.getSheetAt(0)
            val iterator = sheet.iterator()

            var isFirst: Boolean = true
            while (iterator.hasNext()) {

                var accountMaster: AccountMaster = AccountMaster()
                val row = iterator.next()
                val cellIterator = row.cellIterator()

                if (isFirst) {
                    isFirst = false
                } else {
                    var index: Int = 0
                    while (cellIterator.hasNext()) {
                        val cell = cellIterator.next()
                        if (cell.cellType != CellType.BLANK && cell.cellType != CellType.FORMULA) {
                            if (index == 0)
                                accountMaster.sales_man = cell.stringCellValue
                            else if (index == 1) {
                                if (cell.cellType == CellType.STRING) {
                                    accountMaster.account_name = cell.stringCellValue
                                }
                            } else if (index == 2)
                                accountMaster.addess = cell.stringCellValue
                            else if (index == 3)
                                accountMaster.mobile_no = cell.stringCellValue
                            else if (index == 4)
                                accountMaster.opening_balance = cell.numericCellValue
                            else if (index == 5)
                                accountMaster.account_type = cell.stringCellValue
                            else if (index == 6)
                                accountMaster.party_type = cell.stringCellValue
                            else if (index == 7)
                                accountMaster.day = cell.stringCellValue

                            index++
                        }
                    }

                    if (sharedPrefManager.getUserName == accountMaster.sales_man) {
                        aryAccount.add(accountMaster)
                        Log.e("readExcel", "readExcelAccountMasterFile: ${aryAccount.size}")
                    }
                }
            }
            workbook.close()
            inputSteam?.close()

            return@withContext aryAccount
        }
    }
    suspend fun readExcelItemMasterFile(uri: Uri): ArrayList<ItemMaster> {
        return withContext(Dispatchers.IO) {
            Log.d(TAG, "Item Master Start1")

            var aryItems: ArrayList<ItemMaster> = ArrayList<ItemMaster>()

            val inputSteam = context.contentResolver.openInputStream(uri)
            val workbook = XSSFWorkbook(inputSteam)
            //val workbook = WorkbookFactory.create(inputSteam)
            val sheet = workbook.getSheetAt(0)
            val iterator = sheet.iterator()

            var isFirst: Boolean = true
            while (iterator.hasNext()) {

                var itemMaster: ItemMaster = ItemMaster()
                val row = iterator.next()
                val cellIterator = row.cellIterator()

                if (isFirst) {
                    isFirst = false
                } else {

                    var index: Int = 0
                    while (cellIterator.hasNext()) {
                        val cell = cellIterator.next()

                        if (cell.cellType != CellType.BLANK && cell.cellType != CellType.FORMULA) {

                            if (index == 0)
                                itemMaster.category = cell.stringCellValue
                            else if (index == 1) {
                                itemMaster.company_name = cell.stringCellValue
                            } else if (index == 2)
                                itemMaster.brand = cell.stringCellValue
                            else if (index == 3) {
                                if (cell.cellType == CellType.STRING) {
                                    itemMaster.item_name = cell.stringCellValue
                                }
                            } else if (index == 4)
                                itemMaster.mrp = cell.numericCellValue
                            else if (index == 5)
                                itemMaster.purchase_rate = cell.numericCellValue
                            else if (index == 6)
                                itemMaster.sale_rate = cell.numericCellValue
                            else if (index == 7)
                                itemMaster.margin = cell.numericCellValue
                            else if (index == 8)
                                itemMaster.stock_qty = cell.numericCellValue
                            else if (index == 9)
                                itemMaster.stock_amount = cell.numericCellValue
                            else if (index == 10)
                                itemMaster.edtxt_qty = cell.numericCellValue
                            else if (index == 11)
                                itemMaster.edtxt_free = cell.numericCellValue
                            else if (index == 12)
                                itemMaster.edtxt_scm = cell.numericCellValue
                            else if (index == 13)
                                itemMaster.txt_net_rate = cell.numericCellValue
                            else if (index == 14)
                                itemMaster.txt_subTotal = cell.numericCellValue
                            else if (index == 15)
                                itemMaster.old_margin = cell.numericCellValue

                            else if (index == 16)
                                itemMaster.isMarginCustom = false

                            index++
                        }
                    }

                    //TODO Copy margin to old_margin after setting all values
                    itemMaster.old_margin = itemMaster.margin

                    aryItems.add(itemMaster)

                }
            }
            workbook.close()
            inputSteam?.close()

            aryItems
        }
    }
}
