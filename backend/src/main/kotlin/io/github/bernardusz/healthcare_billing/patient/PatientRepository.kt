package io.github.bernardusz.healthcare_billing.patient

import io.github.bernardusz.healthcare_billing.user.User
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PatientRepository: CoroutineCrudRepository<Patient, Long> {
    @Query("""
        SELECT * FROM patients
        WHERE patient_number ILIKE CONCAT('%', :searchTerm, '%') 
           OR first_name ILIKE CONCAT('%', :searchTerm, '%')
           OR last_name ILIKE CONCAT('%', :searchTerm, '%')
           OR insurance_provider ILIKE CONCAT('%', :searchTerm, '%')
           OR policy_number ILIKE CONCAT('%', :searchTerm, '%')
    """)
    fun searchByIdentifier(searchTerm: String): Flow<Patient>
}