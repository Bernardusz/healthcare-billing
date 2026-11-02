package io.github.bernardusz.healthcare_billing.audit.dto

import io.github.bernardusz.healthcare_billing.audit.Audit

data class AuditSaveRequest(
    val claimId: Long,
    val action: AuditAction,
    val notes: String?,
    val flaggedReason: String?,
)

enum class AuditAction {
    APPROVE,
    REJECT,
    INVESTIGATE
}

fun AuditSaveRequest.toDomain(auditorId: Long): Audit = Audit(
    claimId = claimId,
    action = action.name,
    notes = notes,
    flaggedReason = flaggedReason,
    auditorId = auditorId
)