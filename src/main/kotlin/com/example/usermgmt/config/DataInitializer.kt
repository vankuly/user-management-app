package com.example.usermgmt.config

import com.example.usermgmt.domain.Role
import com.example.usermgmt.domain.User
import com.example.usermgmt.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Ensures the two default accounts (admin / user) always exist with the
 * correct BCrypt-hashed passwords, even if the seed migration runs first.
 *
 * This is idempotent – safe to run on every startup.
 */
@Component
class DataInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun run(args: ApplicationArguments) {
        ensureUser("Admin",     "admin@example.com", "admin123", Role.ADMIN)
        ensureUser("John User", "user@example.com",  "user123",  Role.USER)
    }

    private fun ensureUser(name: String, email: String, rawPassword: String, role: Role) {
        if (!userRepository.existsByEmail(email)) {
            userRepository.save(
                User(
                    name     = name,
                    email    = email,
                    password = passwordEncoder.encode(rawPassword),
                    role     = role,
                )
            )
            log.info("Created default {} account: {}", role, email)
        }
    }
}
