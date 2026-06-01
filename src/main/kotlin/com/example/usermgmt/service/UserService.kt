package com.example.usermgmt.service

import com.example.usermgmt.domain.*
import com.example.usermgmt.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val auditLogService: AuditLogService,
) {

    @Transactional(readOnly = true)
    fun findAll(filter: UserFilter, pageable: Pageable): Page<UserDto> =
        userRepository
            .findByFilter(filter.name?.ifBlank { null }, filter.email?.ifBlank { null }, pageable)
            .map { it.toDto() }

    @Transactional(readOnly = true)
    fun count(filter: UserFilter): Long =
        userRepository.countByFilter(filter.name?.ifBlank { null }, filter.email?.ifBlank { null })

    @Transactional(readOnly = true)
    fun findById(id: Long): UserDto =
        userRepository.findById(id)
            .map { it.toDto() }
            .orElseThrow { NoSuchElementException("User $id not found") }

    fun create(request: CreateUserRequest, performedBy: String): UserDto {
        require(!userRepository.existsByEmail(request.email)) {
            "Email '${request.email}' is already in use"
        }
        val user = User(
            name     = request.name.trim(),
            email    = request.email.trim().lowercase(),
            password = passwordEncoder.encode(request.password),
            role     = request.role,
        )
        val saved = userRepository.save(user)
        auditLogService.log(AuditAction.CREATE, saved, performedBy, "User created")
        return saved.toDto()
    }

    fun update(id: Long, request: UpdateUserRequest, performedBy: String): UserDto {
        val user = userRepository.findById(id)
            .orElseThrow { NoSuchElementException("User $id not found") }

        require(!userRepository.existsByEmailAndIdNot(request.email.trim().lowercase(), id)) {
            "Email '${request.email}' is already taken by another user"
        }

        val changes = buildList {
            if (user.name != request.name.trim())  add("name: ${user.name} → ${request.name}")
            if (user.email != request.email.trim().lowercase()) add("email: ${user.email} → ${request.email}")
            if (user.role != request.role)          add("role: ${user.role} → ${request.role}")
            if (request.password != null)           add("password changed")
        }.joinToString(", ")

        user.name      = request.name.trim()
        user.email     = request.email.trim().lowercase()
        user.role      = request.role
        user.updatedAt = Instant.now()
        if (request.password != null) {
            user.password = passwordEncoder.encode(request.password)
        }

        val saved = userRepository.save(user)
        auditLogService.log(AuditAction.UPDATE, saved, performedBy, changes.ifEmpty { "No changes" })
        return saved.toDto()
    }

    fun delete(id: Long, performedBy: String) {
        val user = userRepository.findById(id)
            .orElseThrow { NoSuchElementException("User $id not found") }
        require(!user.email.equals(performedBy, ignoreCase = true)) {
            "You cannot delete your own account"
        }
        auditLogService.log(AuditAction.DELETE, user, performedBy, "User deleted")
        userRepository.delete(user)
    }
}
