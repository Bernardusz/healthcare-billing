package io.github.bernardusz.healthcare_billing.patient.dto

import io.github.bernardusz.healthcare_billing.patient.Patient
import java.time.LocalDate

data class PatientSaveRequest(
    val patientNumber: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val insuranceProvider: String,
    val policyNumber: String
)

fun PatientSaveRequest.toEntity(): Patient = Patient(
    patientNumber = patientNumber,
    firstName = firstName,
    lastName = lastName,
    dateOfBirth = dateOfBirth,
    insuranceProvider = insuranceProvider,
    policyNumber = policyNumber
)