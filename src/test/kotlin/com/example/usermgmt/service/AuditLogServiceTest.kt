package com.example.usermgmt.service

import com.example.usermgmt.domain.*
import com.example.usermgmt.repository.AuditLogRepository
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.time.Instant

@ExtendWith(MockKExtension::class)
class AuditLogServiceTest {

    private val repo    = mockk<AuditLogRepository>()
    private val service = AuditLogService(repo)

    @Test
    fun `log saves a new AuditLog entity`() {
        val user = User(
            id        = 5L,
            name      = "Test",
            email     = "test@example.com",
            password  = "x",
            role      = Role.USER,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )
        val slot = slot<AuditLog>()
        every { repo.save(capture(slot)) } returns AuditLog(
            id              = 1L,
            action          = AuditAction.CREATE,
            targetUserId    = user.id,
            targetUserEmail = user.email,
            performedBy     = "admin",
        )

        service.log(AuditAction.CREATE, user, "admin", "created")

        assertEquals(AuditAction.CREATE,  slot.captured.action)
        assertEquals(user.id,             slot.captured.targetUserId)
        assertEquals(user.email,          slot.captured.targetUserEmail)
        assertEquals("admin",             slot.captured.performedBy)
        assertEquals("created",           slot.captured.details)
    }

    @Test
    fun `findAll delegates to repository with correct pageable`() {
        val pageable = PageRequest.of(0, 20)
        every { repo.findAll(pageable) } returns PageImpl(emptyList())

        val result = service.findAll(pageable)

        assertEquals(0, result.totalElements)
        verify { repo.findAll(pageable) }
    }
}
