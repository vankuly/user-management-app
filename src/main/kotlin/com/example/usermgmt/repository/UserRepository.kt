package com.example.usermgmt.repository

import com.example.usermgmt.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {

    fun findByEmail(email: String): Optional<User>

    fun existsByEmail(email: String): Boolean

    fun existsByEmailAndIdNot(email: String, id: Long): Boolean

    @Query(
        """
        SELECT u FROM User u
        WHERE (:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')))
        AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:email AS string), '%')))
        """
    )
    fun findByFilter(
        @Param("name")  name:  String?,
        @Param("email") email: String?,
        pageable: Pageable,
    ): Page<User>

    @Query(
        """
        SELECT COUNT(u) FROM User u
        WHERE (:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')))
        AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:email AS string), '%')))
        """
    )
    fun countByFilter(
        @Param("name")  name:  String?,
        @Param("email") email: String?,
    ): Long
}
