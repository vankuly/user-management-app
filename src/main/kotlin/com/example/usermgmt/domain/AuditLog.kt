package com.example.usermgmt.domain

import jakarta.persistence.*
import java.time.Instant

enum class AuditAction { CREATE, UPDATE, DELETE }

@Entity
@Table(
    name = "audit_logs",
    indexes = [
        Index(name = "idx_audit_target",  columnList = "target_user_id"),
        Index(name = "idx_audit_created", columnList = "created_at"),
    ]
)
class AuditLog(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val action: AuditAction,

    @Column(name = "target_user_id", nullable = false)
    val targetUserId: Long,

    @Column(name = "target_user_email", nullable = false)
    val targetUserEmail: String,

    @Column(name = "performed_by", nullable = false)
    val performedBy: String,

    @Column(columnDefinition = "TEXT")
    val details: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
)

data class AuditLogDto(
    val id: Long,
    val action: AuditAction,
    val targetUserId: Long,
    val targetUserEmail: String,
    val performedBy: String,
    val details: String?,
    val createdAt: Instant,
)
