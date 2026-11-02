package io.github.bernardusz.healthcare_billing.audit.repository

import io.github.bernardusz.healthcare_billing.audit.Audit
import kotlinx.coroutines.flow.Flow

interface CustomAuditRepository {
    fun saveAllInBatch(audits: List<Audit>): Flow<Long>
}