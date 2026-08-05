package com.hazardev.fpc_back

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import com.hazardev.fpc_back.shared.config.JwtProperties

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(JwtProperties::class)
class FpcBackApplication

fun main(args: Array<String>) {
	runApplication<FpcBackApplication>(*args)
}
