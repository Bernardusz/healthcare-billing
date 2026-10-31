package io.github.bernardusz.healthcare_billing.claim.dto

import io.github.bernardusz.healthcare_billing.claim.Claim
import java.math.BigDecimal

data class ClaimSaveRequest(
    val claimNumber: String,
    val patientId: Long,
    val icdCode: String,
    val cptCode: String,
    val amount: BigDecimal,
)

fun ClaimSaveRequest.toDomain(billerId: Long): Claim = Claim(
    claimNumber = claimNumber,
    patientId = patientId,
    billerId = billerId,
    icdCode = icdCode,
    cptCode = cptCode,
    amount = amount,
    flaggedForAudit = false
)
