package io.github.bernardusz.healthcare_billing.patient.dto

import io.github.bernardusz.healthcare_billing.patient.Patient

data class PatientInformationResponse (
    val id: Long,
    val patientNumber: String,
    val firstName: String,
    val lastName: String,
)

fun Patient.toListResponse(): PatientInformationResponse = PatientInformationResponse(
    id = id ?: throw IllegalStateException("Patient ID is missing"),
    patientNumber = patientNumber,
    firstName = firstName,
    lastName = lastName,
)