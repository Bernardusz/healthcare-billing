package io.github.bernardusz.healthcare_billing.claim

import io.github.bernardusz.healthcare_billing.claim.dto.ClaimDetailResponse
import io.github.bernardusz.healthcare_billing.claim.dto.ClaimListResponse
import io.github.bernardusz.healthcare_billing.claim.dto.ClaimSaveRequest
import io.github.bernardusz.healthcare_billing.claim.dto.ClaimStatusUpdateRequest
import io.github.bernardusz.healthcare_billing.claim.dto.toDetailResponse
import io.github.bernardusz.healthcare_billing.claim.dto.toDomain
import io.github.bernardusz.healthcare_billing.claim.dto.toListResponse
import io.github.bernardusz.healthcare_billing.claim.repository.ClaimRepository
import io.github.bernardusz.healthcare_billing.shared.exception.exceptions.missing.ClaimNotFoundException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.String

@Service
class ClaimService(
    private val claimRepository: ClaimRepository
) {
    @Transactional
    suspend fun createClaim(
        claimSaveRequest: ClaimSaveRequest,
        billerId: Long
    ): Long {
        val claim = claimRepository.save(
            claimSaveRequest.toDomain(
                billerId
            )
        )

        return claim.id
            ?: throw IllegalStateException("Claim with ID ${claim.id} was not created")
    }

    @Transactional(readOnly = true)
    fun getClaims(): Flow<ClaimListResponse>{
        val claims = claimRepository.findAll()

        return claims.map {
            claim -> claim.toListResponse()
        }
    }

    @Transactional(readOnly = true)
    fun searchPatientByIdentifier(
        identifier: String
    ): Flow<ClaimListResponse>{
        val claims: Flow<Claim> = claimRepository.searchByIdentifier(
            searchTerm = identifier,
        )

        return claims.map {
            claim -> claim.toListResponse()
        }
    }

    @Transactional(readOnly = true)
    suspend fun getClaimById(claimId: Long): ClaimDetailResponse{
        val claim = claimRepository.findById(claimId)
            ?:throw ClaimNotFoundException(message = "Claim with ID $claimId was not found")

        return claim.toDetailResponse()
    }

    @Transactional
    suspend fun updateClaimById(
        claimId: Long,
        claimUpdateRequest: ClaimSaveRequest
    ): ClaimDetailResponse{
        val claim: Claim = claimRepository.findById(claimId)
            ?: throw ClaimNotFoundException(message = "Claim with ID $claimId was not found")

        val updatedClaim = claim.copy(
            claimNumber = claimUpdateRequest.claimNumber,
            patientId = claimUpdateRequest.patientId,
            icdCode = claimUpdateRequest.icdCode,
            cptCode = claimUpdateRequest.cptCode,
        )

        return claimRepository.save(
            updatedClaim
        ).toDetailResponse()
    }

    @Transactional
    suspend fun updateClaimStatusById(
        claimId: Long,
        claimStatusUpdateRequest: ClaimStatusUpdateRequest
    ): ClaimDetailResponse {
        val claim = claimRepository.findById(claimId)
            ?: throw ClaimNotFoundException(message = "Claim with ID $claimId was not found")

        val updatedClaim = claim.copy(
            status = claimStatusUpdateRequest.status.name,
            flaggedForAudit = claimStatusUpdateRequest.flaggedForAudit
        )

        return claimRepository.save(
            updatedClaim
        ).toDetailResponse()
    }

    @Transactional
    suspend fun deleteClaimById(claimId: Long){
        claimRepository.deleteById(claimId)
    }
}