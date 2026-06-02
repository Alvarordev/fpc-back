package com.hazardev.fpc_back.patient.infrastructure

import com.hazardev.fpc_back.patient.domain.PatientSummary
import com.hazardev.fpc_back.patient.domain.PatientSummaryStatus
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

@Repository
interface PatientSummaryRepository : JpaRepository<PatientSummary, UUID> {

    @Query(
        """
        SELECT ps.patientId
        FROM PatientSummary ps
        WHERE (
            ps.status IN :eligibleStatuses AND (ps.nextAttemptAt IS NULL OR ps.nextAttemptAt <= :now)
        ) OR (
            ps.status = :processingStatus AND ps.processingStartedAt IS NOT NULL AND ps.processingStartedAt <= :staleBefore
        )
        ORDER BY COALESCE(ps.nextAttemptAt, ps.updatedAt, ps.createdAt)
        """
    )
    fun findCandidatePatientIds(
        @Param("eligibleStatuses") eligibleStatuses: Collection<PatientSummaryStatus>,
        @Param("processingStatus") processingStatus: PatientSummaryStatus,
        @Param("now") now: LocalDateTime,
        @Param("staleBefore") staleBefore: LocalDateTime,
        pageable: Pageable
    ): List<UUID>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ps FROM PatientSummary ps WHERE ps.patientId = :patientId")
    fun findByPatientIdWithLock(@Param("patientId") patientId: UUID): Optional<PatientSummary>
}
