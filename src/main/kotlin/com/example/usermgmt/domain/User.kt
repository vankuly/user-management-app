package com.example.usermgmt.domain

import jakarta.persistence.*
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
    val name: String,
    val email: String,
    val password: String,
    val role: Role,
)

data class UpdateUserRequest(
    val name: String,
    val email: String,
    /** Null means "leave the password unchanged". */
    val password: String?,
    val role: Role,
)

data class UserFilter(
    val name: String? = null,
    val email: String? = null,
)
