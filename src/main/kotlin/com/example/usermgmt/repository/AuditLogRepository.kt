package com.example.usermgmt.repository

import com.example.usermgmt.domain.AuditLog
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface AuditLogRepository : JpaRepository<AuditLog, Long> {
    fun findByTargetUserId(targetUserId: Long, pageable: Pageable): Page<AuditLog>
}
