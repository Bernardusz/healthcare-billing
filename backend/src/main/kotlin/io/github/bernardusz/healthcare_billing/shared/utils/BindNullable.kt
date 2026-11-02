package io.github.bernardusz.healthcare_billing.shared.utils

import io.r2dbc.spi.Statement

// Reusable helper extension function
fun Statement.bindNullable(index: Int, value: Any?, type: Class<*>): Statement {
    return if (value != null) {
        this.bind(index, value)
    } else {
        this.bindNull(index, type)
    }
}
