package io.github.bernardusz.healthcare_billing.claim.dto

import io.github.bernardusz.healthcare_billing.claim.ClaimStatus

data class ClaimStatusUpdateRequest (
    val status: ClaimStatus = ClaimStatus.PENDING,
    val flaggedForAudit: Boolean,
)