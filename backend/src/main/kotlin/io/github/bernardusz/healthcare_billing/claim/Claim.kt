package io.github.bernardusz.healthcare_billing.claim

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.Instant

enum class ClaimStatus {
    PENDING,
    APPROVED,
    REJECTED,
    FLAGGED,
    UNDER_AUDIT
}

@Table("medical_claims")
data class Claim(
    @Id val id: Long? = null,
    val claimNumber: String,
    val patientId: Long,
    val billerId: Long,
    val icdCode: String,
    val cptCode: String,
    val amount: BigDecimal,
    val status: String = ClaimStatus.PENDING.name,
    val flaggedForAudit: Boolean = false,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)
