package io.github.bernardusz.healthcare_billing.claim.repository

import io.github.bernardusz.healthcare_billing.claim.Claim
import io.github.bernardusz.healthcare_billing.claim.ClaimStatus
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ClaimRepository: CoroutineCrudRepository<Claim, Long>, CustomClaimRepository {

    @Query("""
        SELECT * FROM medical_claims
        WHERE claim_number ILIKE CONCAT('%', :searchTerm, '%') 
           OR icd_code ILIKE CONCAT('%', :searchTerm, '%')
           OR cpt_code ILIKE CONCAT('%', :searchTerm, '%')
    """)
    fun searchByIdentifier(searchTerm: String): Flow<Claim>

    @Query("""
        SELECT * FROM medical_claims
        WHERE flagged_for_audit = TRUE
            AND (:status IS NULL OR status = :status)
    """)
    fun findByStatus(
        status: String?
    ): Flow<Claim>

    fun findAllByPatientId(id: Long): Flow<Claim>

    fun findAllByStatus(status: String): Flow<Claim>
}