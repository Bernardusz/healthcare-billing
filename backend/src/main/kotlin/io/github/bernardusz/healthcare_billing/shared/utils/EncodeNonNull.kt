package io.github.bernardusz.healthcare_billing.shared.utils

import org.springframework.security.crypto.password.PasswordEncoder

fun PasswordEncoder.encodeNonNull(rawPassword: CharSequence): String {
    return requireNotNull(this.encode(rawPassword)) {
        "PasswordEncoder returned null for the provided Password"
    }
}