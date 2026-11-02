package io.github.bernardusz.healthcare_billing.audit

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("audit_logs")
data class Audit(
    @Id val id: Long? = null,
    val claimId: Long,
    val auditorId: Long,
    val action: String,
    val notes: String?,
    val flaggedReason: String?,
    val createdAt: Instant = Instant.now(),
)
