package io.github.bernardusz.healthcare_billing.patient.dto

import io.github.bernardusz.healthcare_billing.patient.Patient
import java.time.Instant
import java.time.LocalDate

data class PatientDetailResponse (
    val id: Long,
    val patientNumber: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val insuranceProvider: String,
    val policyNumber: String,
    val createdAt: Instant = Instant.now(),
)

fun Patient.toDetailResponse(): PatientDetailResponse = PatientDetailResponse(
    id = id ?: throw IllegalStateException("Patient ID is missing"),
    patientNumber = patientNumber,
    firstName = firstName,
    lastName = lastName,
    dateOfBirth = dateOfBirth,
    insuranceProvider = insuranceProvider,
    policyNumber = policyNumber,
    createdAt = createdAt,
)