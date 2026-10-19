package io.github.bernardusz.`healthcare-billing`.auth.service

import io.github.bernardusz.`healthcare-billing`.user.dto.UserSecurity
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey


@Service
class JwtService (
    @Value("\${jwt.secret}") private val secret: String,

    @Value("\${jwt.access-token-expiration-ms}")
    private val accessTokenExpiration: Long,
){
    private fun getSigningKey(): SecretKey {
        return Keys.hmacShaKeyFor(
            Decoders.BASE64.decode(secret)
        )
    }

    fun extractAllClaims(token: String): Claims? {
        return try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: JwtException){
            null
        } catch (e: IllegalArgumentException) {
            null
        }

    }

    fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T? {
        val claims = extractAllClaims(token) ?: return null
        return claimsResolver(claims)
    }

    fun buildToken(
        extraClaims: Map<String, Any>,
        user: UserSecurity
    ): String {
        return Jwts.builder()
            .subject(user.getId().toString())
            .claims(extraClaims)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + accessTokenExpiration))
            .signWith(getSigningKey(), Jwts.SIG.HS512)
            .compact()
    }

    fun generateToken(user: UserSecurity): String {
        return buildToken(mapOf(
            "email" to user.getEmail(),
            "username" to user.username,
            "role" to user.getRole()
        ), user
        )
    }

    fun extractSubjectId(token: String): String? {
        return extractClaim(token, Claims::getSubject)
    }

    fun extractExpiration(token: String): Date? {
        return extractClaim(token, Claims::getExpiration)
    }

    fun isTokenExpired(token: String): Boolean {
        val expiration = extractExpiration(token) ?: return true
        return expiration.before(Date())
    }

    fun validateToken(token: String, user: UserSecurity): Boolean {
        val extractedUserId = extractSubjectId(token)
        return extractedUserId == user.getId().toString() && !isTokenExpired(token)
    }

    fun getAccessTokenExpiration(): Long {
        return accessTokenExpiration
    }
}