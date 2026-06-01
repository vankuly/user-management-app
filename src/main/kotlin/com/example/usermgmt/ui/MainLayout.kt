package com.example.usermgmt.ui

import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.applayout.DrawerToggle
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.sidenav.SideNav
import com.vaadin.flow.component.sidenav.SideNavItem
import com.vaadin.flow.router.RouterLayout
import com.vaadin.flow.server.VaadinServletRequest
import com.vaadin.flow.server.auth.AnonymousAllowed
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler

@AnonymousAllowed
open class MainLayout : AppLayout(), RouterLayout {

    init {
        val toggle = DrawerToggle()

        val title = H1("User Management").apply {
            style.set("font-size", "var(--lumo-font-size-l)")
                 .set("margin", "0")
        }

        val auth = SecurityContextHolder.getContext().authentication
        val isAdmin = auth?.authorities?.any { it.authority == "ROLE_ADMIN" } == true

        val nav = SideNav().apply {
            addItem(SideNavItem("Dashboard", DashboardView::class.java, VaadinIcon.DASHBOARD.create()))
            if (isAdmin) {
                addItem(SideNavItem("Audit Log", AuditLogView::class.java, VaadinIcon.LIST.create()))
            }
        }

        val principal = auth?.name ?: ""
        val userInfo  = Span("Logged in as $principal").apply {
            style.set("font-size", "var(--lumo-font-size-s)")
                 .set("color",     "var(--lumo-secondary-text-color)")
        }

        val logoutBtn = Button("Logout", VaadinIcon.SIGN_OUT.create()) {
            // Invalidate Spring Security context and HTTP session, then redirect
            val request = VaadinServletRequest.getCurrent()?.httpServletRequest
            if (request != null) {
                SecurityContextLogoutHandler().logout(request, null, auth)
            }
            SecurityContextHolder.clearContext()
            com.vaadin.flow.component.UI.getCurrent().page.setLocation("/login")
        }.apply {
            addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL)
        }

        val header = HorizontalLayout(toggle, title).apply {
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
            expand(title)
            setWidthFull()
        }

        addToNavbar(header)
        addToDrawer(nav, userInfo, logoutBtn)
    }
}
