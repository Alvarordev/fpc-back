package com.hazardev.fpc_back.patient.domain

enum class PatientSummaryStatus {
    PENDING,
    PROCESSING,
    READY,
    RETRY_WAIT,
    FAILED_PERMANENT
}
