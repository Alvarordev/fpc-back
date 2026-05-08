package com.hazardev.fpc_back.healthcenter.application.dto

import com.hazardev.fpc_back.shared.domain.PeruDepartment

data class UpdateHealthCenterRequest(
    val name: String? = null,
    val department: PeruDepartment? = null
)
