package io.github.bernardusz.healthcare_billing.patient

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.time.LocalDate

@Table("patients")
data class Patient (
    @Id val id: Long? = null,
    val patientNumber: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val insuranceProvider: String,
    val policyNumber: String,
    val createdAt: Instant = Instant.now(),
)