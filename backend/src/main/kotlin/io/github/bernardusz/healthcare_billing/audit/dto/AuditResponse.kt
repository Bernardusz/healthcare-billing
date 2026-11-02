package io.github.bernardusz.healthcare_billing.audit.dto

import io.github.bernardusz.healthcare_billing.audit.Audit
import java.time.Instant

data class AuditResponse(
    val id: Long,
    val claimId: Long,
    val auditorId: Long,
    val action: String,
    val notes: String?,
    val flaggedReason: String?,
    val createdAt: Instant,
)

fun Audit.toResponse(): AuditResponse = AuditResponse(
    id = id ?: throw IllegalStateException("Audit id cannot be null"),
    claimId = claimId,
    auditorId = auditorId,
    action = action,
    notes = notes,
    flaggedReason = flaggedReason,
    createdAt = createdAt,
)
