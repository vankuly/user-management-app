package com.example.usermgmt.service

import com.example.usermgmt.domain.*
import com.example.usermgmt.repository.UserRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.time.Instant
import java.util.Optional

@ExtendWith(MockKExtension::class)
class UserServiceTest {

    @MockK lateinit var userRepository: UserRepository
    @MockK lateinit var auditLogService: AuditLogService

    private val passwordEncoder = BCryptPasswordEncoder()
    private lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        userService = UserService(userRepository, passwordEncoder, auditLogService)
    }

    private fun sampleUser(id: Long = 1L) = User(
        id        = id,
        name      = "Alice",
        email     = "alice@example.com",
        password  = passwordEncoder.encode("secret"),
        role      = Role.USER,
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )

    // ── findAll ──────────────────────────────────────────────────────────────

    @Test
    fun `findAll returns mapped DTOs`() {
        val page = PageImpl(listOf(sampleUser()))
        every { userRepository.findByFilter(null, null, any()) } returns page

        val result = userService.findAll(UserFilter(), PageRequest.of(0, 10))

        assertEquals(1, result.totalElements)
        assertEquals("alice@example.com", result.content[0].email)
    }

    @Test
    fun `findAll with name filter passes non-blank value`() {
        val page = PageImpl(listOf(sampleUser()))
        every { userRepository.findByFilter("ali", null, any()) } returns page

        userService.findAll(UserFilter(name = "ali"), PageRequest.of(0, 10))

        verify { userRepository.findByFilter("ali", null, any()) }
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    fun `create persists new user and records audit log`() {
        val request = CreateUserRequest("Bob", "bob@example.com", "pw123", Role.USER)
        val saved   = sampleUser(2L).also { it.name = "Bob"; it.email = "bob@example.com" }

        every { userRepository.existsByEmail("bob@example.com") } returns false
        every { userRepository.save(any()) }                       returns saved
        every { auditLogService.log(any(), any(), any(), any()) }  just Runs

        val dto = userService.create(request, "admin@example.com")

        assertEquals("Bob", dto.name)
        verify(exactly = 1) { auditLogService.log(AuditAction.CREATE, any(), "admin@example.com", any()) }
    }

    @Test
    fun `create throws when email already exists`() {
        every { userRepository.existsByEmail("alice@example.com") } returns true

        val ex = assertThrows<IllegalArgumentException> {
            userService.create(
                CreateUserRequest("Alice", "alice@example.com", "pw", Role.USER),
                "admin",
            )
        }
        assertTrue(ex.message!!.contains("already in use"))
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    fun `update changes fields and records audit`() {
        val existing = sampleUser(1L)
        val request  = UpdateUserRequest("Alice B", "aliceb@example.com", null, Role.ADMIN)

        every { userRepository.findById(1L) }                          returns Optional.of(existing)
        every { userRepository.existsByEmailAndIdNot("aliceb@example.com", 1L) } returns false
        every { userRepository.save(any()) }                           returns existing
        every { auditLogService.log(any(), any(), any(), any()) }      just Runs

        userService.update(1L, request, "admin@example.com")

        assertEquals("Alice B",             existing.name)
        assertEquals("aliceb@example.com",  existing.email)
        assertEquals(Role.ADMIN,             existing.role)
        verify { auditLogService.log(AuditAction.UPDATE, any(), "admin@example.com", any()) }
    }

    @Test
    fun `update hashes new password when provided`() {
        val existing = sampleUser(1L)
        val oldHash  = existing.password
        val request  = UpdateUserRequest("Alice", "alice@example.com", "newPw!", Role.USER)

        every { userRepository.findById(1L) }                                    returns Optional.of(existing)
        every { userRepository.existsByEmailAndIdNot("alice@example.com", 1L) }  returns false
        every { userRepository.save(any()) }                                      returns existing
        every { auditLogService.log(any(), any(), any(), any()) }                 just Runs

        userService.update(1L, request, "admin")

        assertNotEquals(oldHash, existing.password)
        assertTrue(passwordEncoder.matches("newPw!", existing.password))
    }

    // ── delete ───────────────────────────────────────────────────────────────

    @Test
    fun `delete removes user and records audit`() {
        val user = sampleUser(1L)

        every { userRepository.findById(1L) }                     returns Optional.of(user)
        every { userRepository.delete(user) }                     just Runs
        every { auditLogService.log(any(), any(), any(), any()) } just Runs

        userService.delete(1L, "admin@example.com")

        verify { userRepository.delete(user) }
        verify { auditLogService.log(AuditAction.DELETE, user, "admin@example.com", any()) }
    }

    @Test
    fun `delete throws when user not found`() {
        every { userRepository.findById(99L) } returns Optional.empty()

        assertThrows<NoSuchElementException> {
            userService.delete(99L, "admin")
        }
    }
}
