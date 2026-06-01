package com.example.usermgmt.ui

import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.login.LoginForm
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.auth.AnonymousAllowed

@Route("login")
@PageTitle("Login | User Management")
@AnonymousAllowed
open class LoginView : VerticalLayout(), BeforeEnterObserver {

    private val loginForm = LoginForm()

    init {
        addClassName("login-view")
        setSizeFull()
        justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        alignItems        = FlexComponent.Alignment.CENTER

        loginForm.action = "login"

        add(loginForm)
    }

    override fun beforeEnter(event: BeforeEnterEvent) {
        if (event.location.queryParameters.parameters.containsKey("error")) {
            loginForm.isError = true
        }
    }
}
