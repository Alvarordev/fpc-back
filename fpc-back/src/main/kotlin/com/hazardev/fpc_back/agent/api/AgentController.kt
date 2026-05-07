package com.hazardev.fpc_back.agent.api

import com.hazardev.fpc_back.agent.application.AgentService
import com.hazardev.fpc_back.agent.application.dto.AgentResponse
import com.hazardev.fpc_back.agent.application.dto.CreateAgentRequest
import com.hazardev.fpc_back.agent.application.dto.UpdateAgentRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/agents")
class AgentController(
    private val agentService: AgentService
) {

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createAgent(@RequestBody request: CreateAgentRequest): ResponseEntity<AgentResponse> {
        val response = agentService.createAgent(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun listAgents(): List<AgentResponse> {
        return agentService.listAgents()
    }

    @GetMapping("/{id}")
    fun getAgentById(@PathVariable id: UUID): AgentResponse {
        return agentService.getAgentById(id)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateAgent(
        @PathVariable id: UUID,
        @RequestBody request: UpdateAgentRequest
    ): AgentResponse {
        return agentService.updateAgent(id, request)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteAgent(@PathVariable id: UUID): ResponseEntity<Void> {
        agentService.deleteAgent(id)
        return ResponseEntity.noContent().build()
    }
}
