package io.github.bernardusz.healthcare_billing

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class TemplateKotlinApplication

fun main(args: Array<String>) {
	runApplication<TemplateKotlinApplication>(*args)
}
