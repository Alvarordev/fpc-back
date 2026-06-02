package com.hazardev.fpc_back.patient.infrastructure

import com.hazardev.fpc_back.patient.domain.LlmRateLimitGuard
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface LlmRateLimitGuardRepository : JpaRepository<LlmRateLimitGuard, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM LlmRateLimitGuard g WHERE g.provider = :provider")
    fun findByProviderWithLock(@Param("provider") provider: String): LlmRateLimitGuard?
}
