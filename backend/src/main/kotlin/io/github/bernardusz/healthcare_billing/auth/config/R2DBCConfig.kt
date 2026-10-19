package io.github.bernardusz.healthcare_billing.auth.config

import io.github.bernardusz.healthcare_billing.user.UserRole
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.PostgresDialect

@WritingConverter
class UserRoleToStringConverter : Converter<UserRole, String> {
    override fun convert(source: UserRole): String = source.name
}

@ReadingConverter
class StringToUserRoleConverter : Converter<String, UserRole> {
    override fun convert(source: String): UserRole {
        return try {
            UserRole.valueOf(source.trim().uppercase())
        } catch (_: IllegalArgumentException) {
            UserRole.ROLE_BILLER
        }
    }
}

@Configuration
class R2DBCConfig {
    @Bean
    fun r2dbcCustomConversions(): R2dbcCustomConversions {
        val converters = listOf(
            StringToUserRoleConverter(),
            UserRoleToStringConverter(),
        )

        return R2dbcCustomConversions.of(
            PostgresDialect.INSTANCE, converters
        )
    }
}