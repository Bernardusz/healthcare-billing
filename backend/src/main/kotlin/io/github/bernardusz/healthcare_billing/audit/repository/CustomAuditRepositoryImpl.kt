package io.github.bernardusz.healthcare_billing.audit.repository

import io.github.bernardusz.healthcare_billing.audit.Audit
import io.github.bernardusz.healthcare_billing.shared.utils.bindNullable
import io.r2dbc.spi.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.r2dbc.core.DatabaseClient
import reactor.core.publisher.Flux

class CustomAuditRepositoryImpl(
    private val databaseClient: DatabaseClient
) : CustomAuditRepository {
    override fun saveAllInBatch(audits: List<Audit>): Flow<Long> {
        if (audits.isEmpty()) return Flux.empty<Long>().asFlow()

        return databaseClient.inConnectionMany { connection ->
            val statement = connection.createStatement(
                """
                    INSERT INTO audit_logs (
                        claim_id,
                        auditor_id,
                        action,
                        notes,
                        flagged_reason
                    ) 
                    VALUES ($1, $2, $3, $4, $5)
                """.trimIndent()
            )

            audits.forEachIndexed { index, audit ->
                statement.bind(0, audit.claimId)
                    .bind(1, audit.auditorId)
                    .bind(2, audit.action)
                    .bindNullable(3, audit.notes, String::class.java)
                    .bindNullable(4, audit.flaggedReason, String::class.java)

                // Add the current set of parameters to the batch if not the last item
                if (index < audits.size - 1) {
                    statement.add()
                }
            }

            Flux.from(statement.execute())
                .flatMap { result: Result -> result.rowsUpdated }
        }.asFlow() // Converts Reactor Flux to Kotlin Flow
    }
}
