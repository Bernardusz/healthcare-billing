package io.github.bernardusz.healthcare_billing.audit.dto

data class AuditSummaryResponse(
    val totalClaimsChecked: Long,
    val totalFlaggedClaims: Long
)