package com.hazardev.fpc_back.contact.infrastructure

import com.hazardev.fpc_back.contact.domain.Contact
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ContactRepository : JpaRepository<Contact, Long>
