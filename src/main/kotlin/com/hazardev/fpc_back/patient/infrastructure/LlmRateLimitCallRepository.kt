package com.hazardev.fpc_back.patient.infrastructure

import com.hazardev.fpc_back.patient.domain.LlmRateLimitCall
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface LlmRateLimitCallRepository : JpaRepository<LlmRateLimitCall, Long> {

    @Modifying
    @Query("DELETE FROM LlmRateLimitCall c WHERE c.provider = :provider AND c.reservedAt < :threshold")
    fun deleteExpired(@Param("provider") provider: String, @Param("threshold") threshold: LocalDateTime): Int

    fun countByProviderAndReservedAtGreaterThanEqual(provider: String, threshold: LocalDateTime): Long

    fun findTopByProviderAndReservedAtGreaterThanEqualOrderByReservedAtAsc(
        provider: String,
        threshold: LocalDateTime
    ): LlmRateLimitCall?
}
