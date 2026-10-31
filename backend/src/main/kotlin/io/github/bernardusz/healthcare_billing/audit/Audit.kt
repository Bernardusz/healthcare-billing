package io.github.bernardusz.healthcare_billing.audit

import org.springframework.data.annotation.Id

data class Audit(
    @Id val id: Long? = null,
)
