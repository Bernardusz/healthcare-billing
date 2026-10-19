package io.github.bernardusz.`healthcare-billing`.auth.scheduler

import io.github.bernardusz.`healthcare-billing`.auth.service.RefreshTokenService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class TokenCleanupScheduler(
    private val refreshTokenService: RefreshTokenService,
) {
    private val logger = LoggerFactory.getLogger(TokenCleanupScheduler::class.java)

    @Scheduled(cron = "0 0 3 * * ?")
    suspend fun cleanupExpiredTokens() {
        logger.info("Starting scheduled token purge task...")

        val purgedCount = refreshTokenService.purgeExpiredTokensOrRevokedTokens()

        logger.info("Scheduled task finished. Successfully purged {} expired or revoked refresh tokens.", purgedCount)
    }
}