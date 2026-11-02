package io.github.bernardusz.healthcare_billing.audit.repository

import io.github.bernardusz.healthcare_billing.audit.Audit
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface AuditRepository : CoroutineCrudRepository<Audit, Long>, CustomAuditRepository {
    fun findAllByClaimId(claimId: Long): Flow<Audit>
}