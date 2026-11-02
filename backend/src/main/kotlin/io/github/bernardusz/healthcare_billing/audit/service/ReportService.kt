package io.github.bernardusz.healthcare_billing.audit.service

import io.github.bernardusz.healthcare_billing.claim.repository.ClaimRepository
import io.github.bernardusz.healthcare_billing.shared.exception.exceptions.missing.ClaimNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import java.io.ByteArrayOutputStream
import org.openpdf.text.Document
import org.openpdf.text.Element
import org.openpdf.text.FontFactory
import org.openpdf.text.Paragraph
import org.openpdf.text.Phrase
import org.openpdf.text.pdf.PdfPCell
import org.openpdf.text.pdf.PdfPTable
import org.openpdf.text.pdf.PdfWriter
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import java.awt.Color
import java.time.Instant
import org.springframework.stereotype.Service

@Service
class ReportService(
    private val claimRepository: ClaimRepository
) {

    suspend fun generateClaimsExcelReport(): Resource = withContext(Dispatchers.IO) {
        // 2. Create streaming workbook (keeps last 100 rows in memory, flushes rest to disk)
        val workbook = SXSSFWorkbook(100)
        val sheet = workbook.createSheet("Audited Claims")

        // 3. Create Header Row
        val headerRow = sheet.createRow(0)
        val headers = arrayOf(
            "ID",
            "Claim Number",
            "Patient ID",
            "Biller ID",
            "ICD Code",
            "CPT Code",
            "Amount",
            "Status",
            "Flagged"
        )
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).setCellValue(header)
        }

        var rowIndex = 1
        claimRepository.findAll().collect { claim ->
            val row = sheet.createRow(rowIndex++)
            row.createCell(0).setCellValue(claim.id?.toDouble() ?: 0.0)
            row.createCell(1).setCellValue(claim.claimNumber)
            row.createCell(2).setCellValue(claim.patientId.toDouble())
            row.createCell(3).setCellValue(claim.billerId.toDouble())
            row.createCell(4).setCellValue(claim.icdCode)
            row.createCell(5).setCellValue(claim.cptCode)
            row.createCell(6).setCellValue(claim.amount.toDouble())
            row.createCell(7).setCellValue(claim.status)
            row.createCell(8).setCellValue(claim.flaggedForAudit)
        }

        // 5. Write to OutputStream and clean up temporary memory buffer
        val outputStream = ByteArrayOutputStream()
        workbook.write(outputStream)
        workbook.dispose() // Cleans up temporary disk files created by SXSSF
        workbook.close()

        ByteArrayResource(outputStream.toByteArray())
    }

    suspend fun generateAuditPdfCertificate(claimId: Long): ByteArray = withContext(Dispatchers.IO) {
        val claim = claimRepository.findById(claimId)
            ?: throw ClaimNotFoundException("Claim with ID $claimId not found")

        val document = Document()
        val out = ByteArrayOutputStream()

        PdfWriter.getInstance(document, out)
        document.open()

        // Fonts
        val titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f, Color.BLUE)
        val headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f, Color.BLACK)
        val bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10f, Color.DARK_GRAY)

        // Title Header
        val title = Paragraph("Healthcare Billing Audit Certificate", titleFont)
        title.alignment = Element.ALIGN_CENTER
        document.add(title)

        val timestamp = Paragraph("Generated on: ${Instant.now()}\n\n", bodyFont)
        timestamp.alignment = Element.ALIGN_CENTER
        document.add(timestamp)

        // Details Table
        val table = PdfPTable(2)
        table.widthPercentage = 100f

        fun addRow(label: String, value: String) {
            val cellLabel = PdfPCell(Phrase(label, headerFont))
            cellLabel.backgroundColor = Color.LIGHT_GRAY
            val cellValue = PdfPCell(Phrase(value, bodyFont))
            table.addCell(cellLabel)
            table.addCell(cellValue)
        }

        addRow("Claim Number", claim.claimNumber)
        addRow("Patient ID", claim.patientId.toString())
        addRow("Biller ID", claim.billerId.toString())
        addRow("ICD Diagnosis Code", claim.icdCode)
        addRow("CPT Procedure Code", claim.cptCode)
        addRow("Billed Amount", "$${claim.amount}")
        addRow("Compliance Status", claim.status)
        addRow("Audit Flag Active", claim.flaggedForAudit.toString())

        document.add(table)
        document.close()

        out.toByteArray()
    }
}