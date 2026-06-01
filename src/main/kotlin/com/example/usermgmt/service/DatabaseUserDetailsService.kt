package com.example.usermgmt.service

import com.example.usermgmt.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User as SecurityUser
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Spring Security adapter that loads application users for authentication.
 *
 * Kept separate from [UserService] so the CRUD/business logic and the
 * authentication concern can evolve independently. Picked up automatically
 * by VaadinWebSecurity as the [UserDetailsService] bean.
 */
@Service
class DatabaseUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {

    /** Looks up by email (used as the username). */
    @Transactional(readOnly = true)
    override fun loadUserByUsername(username: String): UserDetails =
        userRepository.findByEmail(username)
            .map { user ->
                SecurityUser(
                    user.email,
                    user.password,
                    listOf(SimpleGrantedAuthority("ROLE_${user.role.name}")),
                )
            }
            .orElseThrow { UsernameNotFoundException("No user with email: $username") }
}
