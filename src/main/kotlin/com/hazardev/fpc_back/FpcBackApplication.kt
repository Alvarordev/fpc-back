package com.hazardev.fpc_back

import com.hazardev.fpc_back.patient.application.PatientSummaryProperties
import com.hazardev.fpc_back.shared.config.CorsProperties
import com.hazardev.fpc_back.shared.config.JwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(JwtProperties::class, CorsProperties::class, PatientSummaryProperties::class)
class FpcBackApplication

fun main(args: Array<String>) {
	runApplication<FpcBackApplication>(*args)
}
