package com.hazardev.fpc_back.user.api

import com.hazardev.fpc_back.user.application.AuthService
import com.hazardev.fpc_back.user.application.dto.LoginRequest
import com.hazardev.fpc_back.user.application.dto.RefreshRequest
import com.hazardev.fpc_back.user.application.dto.TokenResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): TokenResponse {
        return authService.login(request.email, request.password)
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody request: RefreshRequest): TokenResponse {
        return authService.refresh(request.refreshToken)
    }
}
