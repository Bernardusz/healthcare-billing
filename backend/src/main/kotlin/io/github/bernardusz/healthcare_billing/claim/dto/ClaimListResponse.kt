package io.github.bernardusz.healthcare_billing.claim.dto

import io.github.bernardusz.healthcare_billing.claim.Claim
import io.github.bernardusz.healthcare_billing.claim.ClaimStatus

data class ClaimListResponse(
    val id: Long,
    val claimNumber: String,
    val patientId: Long,
    val billerId: Long,
    val status: ClaimStatus,
    val flaggedForAudit: Boolean,
)

fun Claim.toListResponse(): ClaimListResponse = ClaimListResponse(
    id = id ?: throw RuntimeException("ClaimListResponse cannot be null"),
    claimNumber = claimNumber,
    patientId = patientId,
    billerId = billerId,
    status = ClaimStatus.valueOf(status),
    flaggedForAudit = flaggedForAudit,
)