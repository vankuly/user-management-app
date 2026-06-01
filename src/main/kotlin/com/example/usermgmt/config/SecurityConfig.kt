package com.example.usermgmt.config

import com.example.usermgmt.ui.LoginView
import com.vaadin.flow.spring.security.VaadinWebSecurity
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
class SecurityConfig : VaadinWebSecurity() {

    companion object {
        private const val B_CRYPT_COST = 10
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder(B_CRYPT_COST)

    /**
     * Dedicated chain for the REST API. Evaluated before the Vaadin chain
     * (lower @Order) and only matches the `/api` path space. It uses stateless HTTP Basic
     * so the API can be called from Swagger UI or curl without a Vaadin
     * session, and returns 401 instead of redirecting to the login view.
     * Per-endpoint role checks are handled by @RolesAllowed on the controllers.
     */
    @Bean
    @Order(1)
    fun apiFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.securityMatcher("/api/**")
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { it.anyRequest().authenticated() }
            .httpBasic { it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) }
            .exceptionHandling { it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) }
        return http.build()
    }

    /** Leave the OpenAPI spec and the self-hosted Swagger UI publicly reachable. */
    @Bean
    fun openApiWebSecurityCustomizer(): WebSecurityCustomizer = WebSecurityCustomizer { web ->
        web.ignoring().requestMatchers(
            "/v3/api-docs/**",
            "/swagger/**",
        )
    }

    override fun configure(http: HttpSecurity) {
        super.configure(http)
        setLoginView(http, LoginView::class.java)
    }
}
