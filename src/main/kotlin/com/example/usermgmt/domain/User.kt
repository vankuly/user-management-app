package com.example.usermgmt.domain

import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant

enum class Role { USER, ADMIN }

@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_users_email", columnList = "email"),
        Index(name = "idx_users_name",  columnList = "name"),
    ]
)
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var password: String,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "user_role")
    var role: Role = Role.USER,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
) {
    /** Convenience DTO projection – keeps UI layer free of JPA entities. */
    fun toDto() = UserDto(id, name, email, role, createdAt, updatedAt)
}

data class UserDto(
    val id: Long,
    val name: String,
    val email: String,
    val role: Role,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class CreateUserRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Must be a valid email address")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, message = "Password must be at least 6 characters")
    val password: String,

    @field:NotNull(message = "Role is required")
    val role: Role,
)

data class UpdateUserRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Must be a valid email address")
    val email: String,

    /** Null means "leave the password unchanged". */
    @field:Size(min = 6, message = "Password must be at least 6 characters")
    val password: String?,

    @field:NotNull(message = "Role is required")
    val role: Role,
)

data class UserFilter(
    val name: String? = null,
    val email: String? = null,
)
