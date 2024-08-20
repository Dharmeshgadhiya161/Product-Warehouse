package com.sunil.dhwarehouse.common

import android.content.Context
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.UnitValue
import com.sunil.dhwarehouse.roomDB.InvoiceMaster

fun writeGroupedInvoicesToPdf(
    context: Context,
    groupedInvoices: Map<Int, List<InvoiceMaster>>,
    filePath: String,
    invoicesList: MutableList<InvoiceMaster>,

    ): Boolean {
    return try {
        // Initialize PDF writer
        val pdfWriter = PdfWriter(filePath)
        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument)
    // Set margins for the document
        //document.setMargins(36f, 36f, 36f, 36f)  // Left, Right, Top, Bottom margins


        val summaryTableHea = Table(UnitValue.createPercentArray(floatArrayOf(1f)))
        summaryTableHea.useAllAvailableWidth()

        val salesName = invoicesList[0].salesName
        val titleHea = "MAHADEV DISTRIBUTOR"

        val nestedTableHea = Table(UnitValue.createPercentArray(floatArrayOf(1f)))
        nestedTableHea.useAllAvailableWidth()


        nestedTableHea.addCell(
            Cell().add(Paragraph(salesName).setTextAlignment(TextAlignment.LEFT).setBold().setFontSize(20f).setPaddingLeft(4f))
                .setBorder(Border.NO_BORDER)
        )

        nestedTableHea.addCell(
            Cell().add(Paragraph(titleHea).setTextAlignment(TextAlignment.CENTER).setBold().setFontSize(18f))
                .setBorder(Border.NO_BORDER)
        )

        val cellHea = Cell().add(nestedTableHea)
            .setMinHeight(40f)  // Adjust height as needed

        summaryTableHea.addCell(cellHea)
        document.add(summaryTableHea)


        val summaryTable = Table(UnitValue.createPercentArray(floatArrayOf(1f)))
        summaryTable.useAllAvailableWidth()

        val accountName = invoicesList[0].account_name
        val date = "Date:  ${invoicesList[0].date}"

        val nestedTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 1f)))
        nestedTable.useAllAvailableWidth()

        nestedTable.addCell(
            Cell().add(Paragraph(accountName).setTextAlignment(TextAlignment.LEFT).setBold().setFontSize(14f).setPaddingLeft(12f))
                .setBorder(Border.NO_BORDER)
        )
        nestedTable.addCell(
            Cell().add(Paragraph(date).setTextAlignment(TextAlignment.RIGHT).setFontSize(14f).setPaddingRight(12f))
                .setBorder(Border.NO_BORDER)
        )
        val cell = Cell().add(nestedTable)
            .setMinHeight(33f)  // Set the minimum height as needed
        summaryTable.addCell(cell)
        document.add(summaryTable)



        val itemTable = Table(
            UnitValue.createPercentArray(
                floatArrayOf(
                    0.8f,
                    3.9f,
                    1.1f,
                    0.9f,
                    0.9f,
                    1.2f,
                    0.9f,
                    1.9f
                )
            )
        )
        itemTable.useAllAvailableWidth()

        // Add headers for the item details
        val headers = arrayOf("NO", "ITEM NAME", "MRP", "QTY", "FREE", "RATE", "SCH%", "AMOUNT")
        for (header in headers) {
            val cell = Cell().add(Paragraph(header))
                .setBackgroundColor(DeviceRgb(63, 169, 245))
                .setFontColor(DeviceRgb(255, 255, 255))
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
            itemTable.addHeaderCell(cell)
        }
        var totalQTY = 0
        var totalSCH = 0.0
        var totalAmount = 0.0
        for ((invoiceNo, invoices) in groupedInvoices) {
            // Add item details under the same table
            invoices.forEachIndexed { index, invoice ->
                itemTable.addCell(
                    Cell().add(
                        Paragraph((invoice.no).toString())
                            .setTextAlignment(TextAlignment.CENTER)
                            .setFontSize(11f)
                    )
                )
                itemTable.addCell(
                    Cell().add(
                        Paragraph(invoice.productItemName)
                            .setTextAlignment(TextAlignment.LEFT)
                            .setFontSize(12f)
                    )
                )
                itemTable.addCell(
                    Cell().add(
                        Paragraph(invoice.mrp.toString())
                            .setTextAlignment(TextAlignment.CENTER)
                            .setBold()
                            .setFontSize(12f)
                    )
                )
                itemTable.addCell(
                    Cell().add(
                        Paragraph(invoice.qty.toString())
                            .setTextAlignment(TextAlignment.CENTER)
                            .setFontSize(12f)

                    )
                )
                itemTable.addCell(
                    Cell().add(
                        Paragraph(if (invoice.free == 0) "" else invoice.free.toString())
                            .setTextAlignment(TextAlignment.CENTER)
                            .setFontSize(12f)
                            .setBorder(Border.NO_BORDER)
                    )
                )

                itemTable.addCell(
                    Cell().add(
                        Paragraph(invoice.rate.toString())
                            .setTextAlignment(TextAlignment.CENTER)
                            .setFontSize(12f)
                            .setBorder(Border.NO_BORDER)
                    )
                )

                itemTable.addCell(
                    Cell().add(
                        Paragraph(if (invoice.scm == 0.0) "" else invoice.scm.toString())
                            .setTextAlignment(TextAlignment.CENTER)
                            .setFontSize(12f)
                            .setBorder(Border.NO_BORDER)
                    )
                )

                itemTable.addCell(
                    Cell().add(
                        Paragraph(invoice.amount.toString())
                            .setTextAlignment(TextAlignment.CENTER)
                            .setFontSize(12f)
                    )

                )
            }

            // Sum up the amount
            totalQTY += invoices[0].qty
//            totalSCH += UtilsFile().roundValues(invoices[0].scm)
            totalAmount += invoices[0].amount
        }


        // Manually add total row to the item table
        itemTable.addCell(
            Cell(1, 2).add(
                Paragraph("TOTAL").setBold().setTextAlignment(TextAlignment.CENTER)
            )
        )
        itemTable.addCell(
            Cell(1, 2).add(
                Paragraph(totalQTY.toString()).setTextAlignment(
                    TextAlignment.RIGHT
                ).setBold()
            )
        )
        itemTable.addCell(
            Cell(1, 3).add(
                Paragraph(if (totalSCH == 0.0) "" else UtilsFile().roundValues(totalSCH).toString()).setTextAlignment(
                    TextAlignment.RIGHT
                ).setBold()
            )
        )
        itemTable.addCell(
            Cell(1, 6).add(
                Paragraph(UtilsFile().roundValues(totalAmount).toString()).setTextAlignment(
                    TextAlignment.RIGHT
                ).setBold()
            )
        )


        itemTable.addCell(
            Cell(1, 7).add(
                Paragraph("Grand Total").setBold().setTextAlignment(TextAlignment.RIGHT)
            )
        )
        itemTable.addCell(
            Cell().add(
                Paragraph(UtilsFile().roundValues(totalAmount).toString()).setTextAlignment(
                    TextAlignment.RIGHT
                ).setBold()
            )
        )


        // Add the full item table to the document
        document.add(itemTable)

        // Add a spacing between invoices
        document.add(Paragraph("\n"))
        // }

        // Close the document
        document.close()



        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
