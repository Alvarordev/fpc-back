package com.hazardev.fpc_back

import com.hazardev.fpc_back.shared.config.CorsProperties
import com.hazardev.fpc_back.shared.config.JwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties::class, CorsProperties::class)
class FpcBackApplication

fun main(args: Array<String>) {
	runApplication<FpcBackApplication>(*args)
}
