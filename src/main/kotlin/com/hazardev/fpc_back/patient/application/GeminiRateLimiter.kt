package com.hazardev.fpc_back.patient.application

import com.hazardev.fpc_back.patient.domain.LlmRateLimitCall
import com.hazardev.fpc_back.patient.domain.LlmRateLimitGuard
import com.hazardev.fpc_back.patient.infrastructure.LlmRateLimitCallRepository
import com.hazardev.fpc_back.patient.infrastructure.LlmRateLimitGuardRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

data class RateLimitReservation(
    val acquired: Boolean,
    val nextAvailableAt: LocalDateTime? = null
)

@Service
class GeminiRateLimiter(
    private val guardRepository: LlmRateLimitGuardRepository,
    private val callRepository: LlmRateLimitCallRepository,
    private val properties: PatientSummaryProperties
) {
    private val provider = "GEMINI"

    @Transactional
    fun tryAcquire(now: LocalDateTime = LocalDateTime.now()): RateLimitReservation {
        ensureGuardRow()
        guardRepository.findByProviderWithLock(provider)

        val threshold = now.minusMinutes(1)
        callRepository.deleteExpired(provider, threshold)
        val activeCalls = callRepository.countByProviderAndReservedAtGreaterThanEqual(provider, threshold)
        if (activeCalls < properties.rateLimitPerMinute) {
            callRepository.save(LlmRateLimitCall(provider = provider))
            return RateLimitReservation(acquired = true)
        }

        val oldestCall = callRepository.findTopByProviderAndReservedAtGreaterThanEqualOrderByReservedAtAsc(provider, threshold)
        return RateLimitReservation(
            acquired = false,
            nextAvailableAt = oldestCall?.reservedAt?.plusMinutes(1) ?: now.plusMinutes(1)
        )
    }

    private fun ensureGuardRow() {
        if (!guardRepository.existsById(provider)) {
            try {
            guardRepository.save(LlmRateLimitGuard(provider = provider))
            } catch (_: DataIntegrityViolationException) {
                // Another node created the singleton row concurrently.
            }
        }
    }
}
