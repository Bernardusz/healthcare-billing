package io.github.bernardusz.healthcare_billing.audit.service

import io.github.bernardusz.healthcare_billing.audit.Audit
import io.github.bernardusz.healthcare_billing.audit.repository.AuditRepository
import io.github.bernardusz.healthcare_billing.audit.AuditRuleEngine
import io.github.bernardusz.healthcare_billing.audit.RuleResult
import io.github.bernardusz.healthcare_billing.audit.dto.AuditAction
import io.github.bernardusz.healthcare_billing.audit.dto.AuditResponse
import io.github.bernardusz.healthcare_billing.audit.dto.AuditSaveRequest
import io.github.bernardusz.healthcare_billing.audit.dto.AuditSummaryResponse
import io.github.bernardusz.healthcare_billing.audit.dto.toResponse
import io.github.bernardusz.healthcare_billing.claim.Claim
import io.github.bernardusz.healthcare_billing.claim.repository.ClaimRepository
import io.github.bernardusz.healthcare_billing.claim.ClaimStatus
import io.github.bernardusz.healthcare_billing.claim.dto.ClaimListResponse
import io.github.bernardusz.healthcare_billing.claim.dto.toListResponse
import io.github.bernardusz.healthcare_billing.shared.exception.exceptions.missing.ClaimNotFoundException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class AuditService(
    private val auditRepository: AuditRepository,
    private val claimRepository: ClaimRepository,
    private val auditRuleEngine: AuditRuleEngine,
) {
    @Transactional(readOnly = true)
    fun findByStatus(
        status: ClaimStatus?,
    ): Flow<ClaimListResponse> {
        val status: String? = status?.name
        val claims: Flow<Claim> = claimRepository
           .findByStatus(status)

        return claims.map {
            claim -> claim.toListResponse()
        }
    }

    @Transactional
    suspend fun runComplianceChecks(
        auditorId: Long
    ): AuditSummaryResponse {
        var flaggedCount = 0
        val pendingClaims = claimRepository.findAllByStatus(ClaimStatus.PENDING.name).toList()
        val auditLogs: MutableList<Audit> = mutableListOf()
        val claimUpdates: MutableList<Claim> = mutableListOf()
        for (claim in pendingClaims) {
            when (val result = auditRuleEngine.evaluateClaim(claim)) {
                is RuleResult.Flagged -> {
                    val updatedClaim: Claim = claim.copy(
                        status = ClaimStatus.FLAGGED.name,
                        flaggedForAudit = true,
                        updatedAt = Instant.now()
                    )
                    claimUpdates.add(updatedClaim)

                    auditLogs.add(
                        Audit(
                            auditorId = auditorId,
                            claimId = claim.id ?: throw IllegalStateException("Claim ID is null"),
                            action = "FLAGGED_AUTOMATED",
                            notes = "System automatically flagged claim during compliance run",
                            flaggedReason = result.reason
                        )
                    )

                    flaggedCount++
                }
                is RuleResult.Passed -> {
                    // Immediately approve and pass the auditor id
                    // Default to id one (First admin) if there's none
                    val updatedClaim: Claim = claim.copy(
                        status = ClaimStatus.APPROVED.name,
                        flaggedForAudit = false,
                        updatedAt = Instant.now()
                    )
                    claimUpdates.add(updatedClaim)

                    val auditLog: Audit = Audit(
                        auditorId = auditorId,
                        claimId = claim.id ?: throw IllegalStateException("Claim ID is null"),
                        action = "Running automatic audit",
                        notes = "Automatic audit",
                        flaggedReason = "Not flagged for breach - Automatic audit passed",
                    )

                    auditLogs.add(auditLog)
                }
            }
        }

        claimRepository.updateAllInBatch(
            claimUpdates
        ).collect()

        auditRepository.saveAllInBatch(
            auditLogs
        ).collect()

        return AuditSummaryResponse(
            pendingClaims.size.toLong(),
            flaggedCount.toLong()
        )
    }

    @Transactional
    suspend fun resolveAudit(
        claimId: Long,
        request: AuditSaveRequest,
        auditorId: Long
    ) {
        val claim = claimRepository.findById(claimId)
            ?: throw ClaimNotFoundException("Claim with ID $claimId not found")

        val (newStatus, flagged) = when (request.action) {
            AuditAction.APPROVE -> ClaimStatus.APPROVED.name to false
            AuditAction.REJECT -> ClaimStatus.REJECTED.name to false
            AuditAction.INVESTIGATE -> ClaimStatus.UNDER_AUDIT.name to true
        }

        val updatedClaim = claim.copy(
            status = newStatus,
            flaggedForAudit = flagged,
            updatedAt = Instant.now()
        )

        val log = Audit(
            claimId = claimId,
            auditorId = auditorId,
            action = "${request.action.name}_MANUAL",
            notes = request.notes,
            flaggedReason = request.flaggedReason,
            createdAt = Instant.now()
        )

        claimRepository.save(updatedClaim)
        auditRepository.save(log)
    }

    @Transactional(readOnly = true)
    fun getAllAuditLogs(
        claimId: Long
    ): Flow<AuditResponse> {
        val audits = auditRepository.findAllByClaimId(
            claimId = claimId
        )

        return audits.map {
            audit -> audit.toResponse()
        }
    }


}