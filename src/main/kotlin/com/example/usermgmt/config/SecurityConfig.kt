package com.example.usermgmt.config

import com.example.usermgmt.ui.LoginView
import com.vaadin.flow.spring.security.VaadinWebSecurity
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
class SecurityConfig : VaadinWebSecurity() {

    companion object {
        private const val B_CRYPT_COST = 10
    }
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder(B_CRYPT_COST)

    override fun configure(http: HttpSecurity) {
        super.configure(http)
        setLoginView(http, LoginView::class.java)
    }
}
