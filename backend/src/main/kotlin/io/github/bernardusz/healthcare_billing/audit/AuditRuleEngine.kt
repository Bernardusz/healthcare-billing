package io.github.bernardusz.healthcare_billing.audit

import io.github.bernardusz.healthcare_billing.claim.Claim
import io.github.bernardusz.healthcare_billing.claim.repository.ClaimRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit

sealed class RuleResult {
    object Passed : RuleResult()
    data class Flagged(val reason: String) : RuleResult()
}

@Component
class AuditRuleEngine(
    private val claimRepository: ClaimRepository
) {
    suspend fun evaluateClaim(claim: Claim): RuleResult {
        // Rule 1: High Amount Threshold Check (e.g., CPT 99214 standard max is $300.00)
        if (claim.cptCode == "99214" && claim.amount > BigDecimal("300.00")) {
            return RuleResult.Flagged("Billed amount $${claim.amount} exceeds standard threshold ($300.00) for CPT 99214.")
        }

        // Rule 2: Duplicate Claim within 24 Hours
        val oneDayAgo = Instant.now().minus(24, ChronoUnit.HOURS)
        val duplicates = claimRepository.findAllByPatientId(claim.patientId)
            .toList()
            .filter {
                it.id != claim.id &&
                        it.cptCode == claim.cptCode &&
                        it.icdCode == claim.icdCode &&
                        it.createdAt.isAfter(oneDayAgo)
            }

        if (duplicates.isNotEmpty()) {
            return RuleResult.Flagged("Duplicate claim detected for patient ${claim.patientId} within 24 hours.")
        }

        // Rule 3: Zero or Negative Amount Check
        if (claim.amount <= BigDecimal.ZERO) {
            return RuleResult.Flagged("Claim amount must be greater than 0.")
        }

        return RuleResult.Passed
    }
}