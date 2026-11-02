package io.github.bernardusz.healthcare_billing.claim.repository

import io.github.bernardusz.healthcare_billing.claim.Claim
import io.r2dbc.spi.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.r2dbc.core.DatabaseClient
import reactor.core.publisher.Flux

class CustomClaimRepositoryImpl(
    private val databaseClient: DatabaseClient
) : CustomClaimRepository {
    override fun updateAllInBatch(claims: List<Claim>): Flow<Long> {
        if (claims.isEmpty()) return Flux.empty<Long>().asFlow()

        return databaseClient.inConnectionMany { connection ->
            val statement = connection.createStatement(
                """
                    UPDATE medical_claims
                        SET 
                            claim_number = $1,
                            patient_id = $2,
                            biller_id = $3,
                            icd_code = $4,
                            cpt_code = $5,
                            amount = $6,
                            status = $7,
                            flagged_for_audit = $8
                        WHERE id = $9;
                """.trimIndent()
            )

            claims.forEachIndexed { index, claim ->
                statement.bind(0, claim.claimNumber)
                    .bind(1, claim.patientId)
                    .bind(2, claim.billerId)
                    .bind(3, claim.icdCode)
                    .bind(4, claim.cptCode)
                    .bind(5, claim.amount)
                    .bind(6, claim.status)
                    .bind(7, claim.flaggedForAudit)
                    .bind(8, claim.id ?: throw IllegalStateException("Claim ID cannot be null"))
                // Add the current set of parameters to the batch if not the last item
                if (index < claims.size - 1) {
                    statement.add()
                }
            }

            Flux.from(statement.execute())
                .flatMap { result: Result -> result.rowsUpdated }
        }.asFlow() // Converts Reactor Flux to Kotlin Flow
    }
}
