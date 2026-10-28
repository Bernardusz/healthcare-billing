package io.github.bernardusz.healthcare_billing.patient

import io.github.bernardusz.healthcare_billing.patient.dto.PatientDetailResponse
import io.github.bernardusz.healthcare_billing.patient.dto.PatientInformationResponse
import io.github.bernardusz.healthcare_billing.patient.dto.PatientSaveRequest
import kotlinx.coroutines.flow.Flow
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/patients")
@PreAuthorize("hasAnyRole('ADMIN', 'BILLER')")
class PatientController(
    val patientService: PatientService
) {
    @PostMapping
    suspend fun createPatient(
        patientSaveRequest: PatientSaveRequest
    ): Long {
        return patientService.createPatient(patientSaveRequest)
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLER', 'AUDITOR')")
    fun getPatients(
        @RequestParam(required = false) identifier: String?
    ): Flow<PatientInformationResponse> {
        return if (identifier.isNullOrEmpty()) {
            patientService.getPatients()
        }
        else {
            patientService.searchAllByIdentifier(identifier)
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLER', 'AUDITOR')")
    suspend fun getPatientById(@PathVariable id: Long): PatientDetailResponse {
        return patientService.getPatientById(id)
    }

    @PutMapping("/{id}")
    suspend fun updatePatient(
        @PathVariable id: Long,
        @RequestBody patient: PatientSaveRequest
    ): PatientDetailResponse {
        return patientService.updatePatientById(
            id = id, patient = patient
        )
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    suspend fun deletePatient(@PathVariable id: Long) {
        return patientService.deletePatientById(id)
    }
}