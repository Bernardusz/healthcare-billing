package io.github.bernardusz.healthcare_billing.audit

import io.github.bernardusz.healthcare_billing.audit.dto.AuditResponse
import io.github.bernardusz.healthcare_billing.audit.dto.AuditSaveRequest
import io.github.bernardusz.healthcare_billing.audit.dto.AuditSummaryResponse
import io.github.bernardusz.healthcare_billing.audit.service.AuditService
import io.github.bernardusz.healthcare_billing.audit.service.ReportService
import io.github.bernardusz.healthcare_billing.claim.ClaimStatus
import io.github.bernardusz.healthcare_billing.claim.dto.ClaimListResponse
import io.github.bernardusz.healthcare_billing.user.dto.UserSecurity
import kotlinx.coroutines.flow.Flow
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/audit")
@PreAuthorize("hasAnyRole('ROLE_AUDITOR', 'ROLE_ADMIN')")
class AuditController(
    private val auditService: AuditService,
    private val reportService: ReportService
) {

    // 1. Fetch current audit queue
    @GetMapping("/flagged")
    fun getFlaggedClaims(
        @RequestParam(required = false) status: ClaimStatus?,
    ): Flow<ClaimListResponse> {
        return auditService.findByStatus(status)
    }

    // 2. Trigger automated compliance check across pending claims
    @PostMapping("/run-rules")
    suspend fun triggerComplianceEngine(
        @AuthenticationPrincipal userSecurity: UserSecurity,
    ): ResponseEntity<AuditSummaryResponse> {
        val summary: AuditSummaryResponse = auditService.
        runComplianceChecks(
            userSecurity.id
        )
        return ResponseEntity.ok(summary)
    }

    // 3. Resolve audit (Approve/Reject + Log entry)
    @PostMapping("/claims/{claimId}/resolution")
    suspend fun resolveClaimAudit(
        @PathVariable claimId: Long,
        @RequestBody request: AuditSaveRequest,
        @AuthenticationPrincipal auditor: UserSecurity
    ): ResponseEntity<Void> {
        auditService.resolveAudit(claimId, request, auditor.id)
        return ResponseEntity.noContent().build()
    }

    // 4. Get historical audit trail for a claim
    @GetMapping("/claims/{claimId}/logs")
    fun getClaimAuditLogs(
        @PathVariable claimId: Long
    ): Flow<AuditResponse> {
        return auditService.getAllAuditLogs(claimId)
    }

    @GetMapping("/excel")
    suspend fun exportExcelReport(): ResponseEntity<Resource> {
        val resource = reportService.generateClaimsExcelReport()

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=claims_audit_report.xlsx")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(resource)
    }

    @GetMapping("/pdf/{claimId}")
    suspend fun exportPdfCertificate(
        @PathVariable claimId: Long
    ): ResponseEntity<ByteArray> {
        val pdfBytes = reportService.generateAuditPdfCertificate(claimId)

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=audit_certificate_$claimId.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfBytes)
    }
}