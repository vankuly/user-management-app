package com.example.usermgmt.web

import com.example.usermgmt.domain.AuditLogDto
import com.example.usermgmt.service.AuditLogService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.annotation.security.RolesAllowed
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/audit-logs")
@Tag(name = "Audit Logs", description = "Read-only audit trail (admin only)")
@RolesAllowed("ADMIN")
class AuditLogController(private val auditLogService: AuditLogService) {

    @GetMapping
    @Operation(summary = "List all audit log entries")
    fun list(
        @ParameterObject @PageableDefault(size = 25, sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: Pageable,
    ): PagedResponse<AuditLogDto> =
        PagedResponse.of(auditLogService.findAll(pageable))

    @GetMapping("/user/{userId}")
    @Operation(summary = "List audit log entries for a specific target user")
    fun listForUser(
        @PathVariable userId: Long,
        @ParameterObject @PageableDefault(size = 25, sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: Pageable,
    ): PagedResponse<AuditLogDto> =
        PagedResponse.of(auditLogService.findByUserId(userId, pageable))
}
