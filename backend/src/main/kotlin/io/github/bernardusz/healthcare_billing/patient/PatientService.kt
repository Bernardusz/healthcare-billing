package io.github.bernardusz.healthcare_billing.patient

import io.github.bernardusz.healthcare_billing.patient.dto.PatientDetailResponse
import io.github.bernardusz.healthcare_billing.patient.dto.PatientInformationResponse
import io.github.bernardusz.healthcare_billing.patient.dto.PatientSaveRequest
import io.github.bernardusz.healthcare_billing.patient.dto.toDetailResponse
import io.github.bernardusz.healthcare_billing.patient.dto.toEntity
import io.github.bernardusz.healthcare_billing.patient.dto.toListResponse
import io.github.bernardusz.healthcare_billing.shared.exception.exceptions.missing.PatientNotFound
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PatientService(
    private val patientRepository: PatientRepository,
) {
    @Transactional
    suspend fun createPatient(patient: PatientSaveRequest): Long {
        val savedPatient = patientRepository.save(
            patient.toEntity()
        )

        return savedPatient.id
            ?: throw IllegalStateException("Database failed to generate user ID")
    }

    @Transactional
    fun getPatients(): Flow<PatientInformationResponse>{
        val userData: Flow<Patient> = patientRepository.findAll()

        return userData.map { user -> user.toListResponse()}
    }

    @Transactional
    fun searchAllByIdentifier(identifier: String): Flow<PatientInformationResponse>{
        val patients: Flow<Patient> = patientRepository.searchByIdentifier(
            searchTerm = identifier,
        )

        return patients.map { patient ->
            patient.toListResponse()
        }
    }

    @Transactional
    suspend fun getPatientById(id: Long): PatientDetailResponse {
        val patientData: Patient = patientRepository.findById(
            id
        ) ?: throw PatientNotFound("Patient with id $id not found")

        return patientData.toDetailResponse()
    }

    @Transactional
    suspend fun updatePatientById(
        id: Long,
        patient: PatientSaveRequest,
    ): PatientDetailResponse {
        val existingPatient: Patient = patientRepository.findById(
            id
        ) ?: throw PatientNotFound("Patient with id $id not found")

        val updatedPatient: Patient = existingPatient.copy(
            patientNumber = patient.patientNumber,
            firstName = patient.firstName,
            lastName = patient.lastName,
            dateOfBirth = patient.dateOfBirth,
            insuranceProvider = patient.insuranceProvider,
            policyNumber = patient.policyNumber
        )

        return patientRepository.save(
            updatedPatient
        ).toDetailResponse()
    }

    @Transactional
    suspend fun deletePatientById(id: Long) {
        patientRepository.deleteById(id)
    }
}