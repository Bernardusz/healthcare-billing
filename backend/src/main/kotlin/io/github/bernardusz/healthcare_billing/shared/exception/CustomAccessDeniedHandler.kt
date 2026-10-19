package io.github.bernardusz.healthcare_billing.shared.exception

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.Instant

@Component
class CustomAccessDeniedHandler : ServerAccessDeniedHandler {

    override fun handle(
        exchange: ServerWebExchange,
        denied: AccessDeniedException
    ): Mono<Void> {
        val response = exchange.response
        response.statusCode = HttpStatus.FORBIDDEN
        response.headers.contentType = MediaType.APPLICATION_JSON

        val errorJson = """
            {
              "message": "You do not have permission to access this resource.",
              "code": 403,
              "timestamp": "${Instant.now()}"
            }
        """.trimIndent()

        val buffer: DataBuffer = response.bufferFactory().wrap(errorJson.toByteArray(Charsets.UTF_8))
        return response.writeWith(Mono.just(buffer))
    }
}