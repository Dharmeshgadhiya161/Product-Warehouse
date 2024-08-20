package com.sunil.dhwarehouse.common

import com.sunil.dhwarehouse.roomDB.InvoiceMaster
import java.io.File
import java.io.IOException

fun writeInvoicesToCsv(invoices: List<InvoiceMaster>, filePath: String): Boolean {
    return try {
        val file = File(filePath)
        file.bufferedWriter().use { out ->
            // Write the header
            out.write("No,Sales Name,Account Name,Address,Mobile No,Date,Time,Product Item Name,Qty,Free,SCM,Rate,SubTotal\n")
            
            // Write each invoice line by line
            for (invoice in invoices) {
                out.write(
                    "${invoice.no}," +
                    "${escapeCsv(invoice.salesName)}," +
                    "${escapeCsv(invoice.account_name)}," +
                    "${escapeCsv(invoice.address)}," +
                    "${escapeCsv(invoice.mobile_no)}," +
                    "${invoice.date}," +
                    "${invoice.time}," +
                    "${escapeCsv(invoice.productItemName)}," +
                    "${invoice.qty}," +
                    "${invoice.free}," +
                    "${invoice.scm}," +
                    "${invoice.rate}," +
                    "${invoice.amount}\n"
                )
            }
        }
        true
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}

// Helper function to escape CSV fields that contain commas, quotes, or new lines
private fun escapeCsv(field: String?): String {
    return if (field != null && (field.contains(",") || field.contains("\"") || field.contains("\n"))) {
        "\"" + field.replace("\"", "\"\"") + "\""
    } else {
        field ?: ""
    }
}
