package io.github.bernardusz.healthcare_billing.claim.dto

import io.github.bernardusz.healthcare_billing.claim.Claim
import io.github.bernardusz.healthcare_billing.claim.ClaimStatus
import java.math.BigDecimal
import java.time.Instant

data class ClaimDetailResponse(
    val id: Long,
    val claimNumber: String,
    val patientId: Long,
    val billerId: Long,
    val icdCode: String,
    val cptCode: String,
    val status: ClaimStatus,
    val amount: BigDecimal,
    val flaggedForAudit: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)

fun Claim.toDetailResponse(): ClaimDetailResponse = ClaimDetailResponse(
    id = id ?: throw RuntimeException("Claim ID is missing"),
    claimNumber = claimNumber,
    patientId = patientId,
    billerId = billerId,
    icdCode = icdCode,
    cptCode = cptCode,
    status = ClaimStatus.valueOf(status),
    amount = amount,
    flaggedForAudit = flaggedForAudit,
    createdAt = createdAt,
    updatedAt = updatedAt
)