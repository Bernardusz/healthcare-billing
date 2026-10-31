package io.github.bernardusz.healthcare_billing.claim

import io.github.bernardusz.healthcare_billing.claim.dto.ClaimSaveRequest
import io.github.bernardusz.healthcare_billing.claim.dto.ClaimDetailResponse
import io.github.bernardusz.healthcare_billing.claim.dto.ClaimListResponse
import io.github.bernardusz.healthcare_billing.claim.dto.ClaimStatusUpdateRequest
import io.github.bernardusz.healthcare_billing.user.dto.UserSecurity
import kotlinx.coroutines.flow.Flow
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/v1/claims")
class ClaimController(
    private val claimService: ClaimService
) {
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_BILLER')")
    @PostMapping
    suspend fun createClaim(
        @RequestBody request: ClaimSaveRequest,
        @AuthenticationPrincipal user: UserSecurity
    ): ResponseEntity<Void> {
        val claimId: Long = claimService.createClaim(
            request,
            user.id
        )
        return ResponseEntity.created(
            URI.create("/api/v1/claims/$claimId")
        ).build()
    }

    @GetMapping
    fun searchUsers(
        @RequestParam(required = false) identifier: String?
    ): Flow<ClaimListResponse> {
        return if (identifier.isNullOrBlank()) {
            claimService.getClaims()
        } else {
            claimService.searchPatientByIdentifier(identifier)
        }
    }

    @GetMapping("/{id}")
    suspend fun getClaimById(
        @PathVariable id: Long
    ): ClaimDetailResponse {
        return claimService.getClaimById(id)
    }

    @PutMapping("/{id}")
    suspend fun updateClaimById(
        @PathVariable id: Long,
        @RequestBody request: ClaimSaveRequest,
    ): ClaimDetailResponse {
        return claimService.updateClaimById(
            id,
            claimUpdateRequest = request)
    }

    @PutMapping("/status/{id}")
    suspend fun updateClaimStatus(
        @PathVariable id: Long,
        @RequestBody request: ClaimStatusUpdateRequest
    ): ClaimDetailResponse {
        return claimService.updateClaimStatusById(
            id, request
        )
    }

    @DeleteMapping("/{id}")
    suspend fun deleteClaimById(
        @PathVariable id: Long
    ) {
        claimService.deleteClaimById(id)
    }

}