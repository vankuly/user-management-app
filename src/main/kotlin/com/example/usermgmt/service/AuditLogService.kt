package com.example.usermgmt.service

import com.example.usermgmt.domain.*
import com.example.usermgmt.repository.AuditLogRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class AuditLogService(private val auditLogRepository: AuditLogRepository) {

    /** Saves an audit entry in the same transaction as the triggering operation. */
    @Transactional(propagation = Propagation.MANDATORY)
    fun log(action: AuditAction, user: User, performedBy: String, details: String? = null) {
        auditLogRepository.save(
            AuditLog(
                action          = action,
                targetUserId    = user.id,
                targetUserEmail = user.email,
                performedBy     = performedBy,
                details         = details,
            )
        )
    }

    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable): Page<AuditLogDto> =
        auditLogRepository.findAll(pageable).map { it.toDto() }

    @Transactional(readOnly = true)
    fun findByUserId(userId: Long, pageable: Pageable): Page<AuditLogDto> =
        auditLogRepository.findByTargetUserId(userId, pageable).map { it.toDto() }

    private fun AuditLog.toDto() = AuditLogDto(
        id              = id,
        action          = action,
        targetUserId    = targetUserId,
        targetUserEmail = targetUserEmail,
        performedBy     = performedBy,
        details         = details,
        createdAt       = createdAt,
    )
}
