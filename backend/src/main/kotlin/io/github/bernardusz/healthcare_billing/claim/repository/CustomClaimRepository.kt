package io.github.bernardusz.healthcare_billing.claim.repository

import io.github.bernardusz.healthcare_billing.claim.Claim
import kotlinx.coroutines.flow.Flow

interface CustomClaimRepository {
    fun updateAllInBatch(audits: List<Claim>): Flow<Long>
}