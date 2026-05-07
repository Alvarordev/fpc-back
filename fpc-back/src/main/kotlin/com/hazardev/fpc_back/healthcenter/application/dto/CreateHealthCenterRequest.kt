package com.hazardev.fpc_back.healthcenter.application.dto

import com.hazardev.fpc_back.shared.domain.PeruDepartment

data class CreateHealthCenterRequest(
    val name: String,
    val department: PeruDepartment
)
