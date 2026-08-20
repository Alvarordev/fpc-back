package com.hazardev.fpc_back.contact.infrastructure

import com.hazardev.fpc_back.contact.domain.ContactServiceReferral
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ContactServiceReferralRepository : JpaRepository<ContactServiceReferral, UUID> {

    fun findByContactId(contactId: UUID): ContactServiceReferral?

    fun findByContactIdIn(contactIds: Collection<UUID>): List<ContactServiceReferral>
}
