package com.example.usermgmt.ui

import com.example.usermgmt.domain.CreateUserRequest
import com.example.usermgmt.domain.Role
import com.example.usermgmt.domain.UpdateUserRequest
import com.example.usermgmt.domain.UserDto
import com.example.usermgmt.service.UserService
import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.textfield.PasswordField
import com.vaadin.flow.component.textfield.TextField
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Create / Edit dialog for a User.
 * [user] == null → create mode; != null → edit mode.
 */
class UserDialog(
    private val user: UserDto?,
    private val userService: UserService,
    private val onSave: () -> Unit,
) : Dialog() {

    private val nameField     = TextField("Name")
    private val emailField    = TextField("Email")
    private val passwordField = PasswordField(if (user == null) "Password" else "New Password (leave blank to keep)")
    private val roleField     = ComboBox<Role>("Role")

    init {
        isModal      = true
        isDraggable  = true
        width        = "400px"

        headerTitle = if (user == null) "Create User" else "Edit User"

        // Pre-fill in edit mode (role is set after its items are populated, below)
        user?.let {
            nameField.value  = it.name
            emailField.value = it.email
        }

        nameField.apply {
            isRequiredIndicatorVisible = true
            setWidthFull()
        }
        emailField.apply {
            isRequiredIndicatorVisible = true
            setWidthFull()
        }
        passwordField.apply {
            isRequiredIndicatorVisible = user == null
            setWidthFull()
        }
        roleField.apply {
            setItems(*Role.values())
            isAllowCustomValue = false
            isRequiredIndicatorVisible = true
            value = user?.role ?: Role.USER
            setWidthFull()
        }

        add(
            com.vaadin.flow.component.orderedlayout.VerticalLayout(
                nameField, emailField, passwordField, roleField
            ).apply {
                isPadding = false
                isSpacing = true
                setWidthFull()
            }
        )

        val saveBtn = com.vaadin.flow.component.button.Button("Save") { save() }
            .apply { addThemeVariants(ButtonVariant.LUMO_PRIMARY) }
        val cancelBtn = com.vaadin.flow.component.button.Button("Cancel") { close() }

        footer.add(cancelBtn, saveBtn)
    }

    private fun save() {
        if (!validate()) return

        val principal = SecurityContextHolder.getContext().authentication.name

        runCatching {
            if (user == null) {
                userService.create(
                    CreateUserRequest(
                        name     = nameField.value.trim(),
                        email    = emailField.value.trim(),
                        password = passwordField.value,
                        role     = roleField.value,
                    ),
                    principal,
                )
            } else {
                userService.update(
                    user.id,
                    UpdateUserRequest(
                        name     = nameField.value.trim(),
                        email    = emailField.value.trim(),
                        password = passwordField.value.takeIf { it.isNotBlank() },
                        role     = roleField.value,
                    ),
                    principal,
                )
            }
        }.onSuccess {
            Notification.show("User saved", 2_000, Notification.Position.BOTTOM_END)
                .apply { addThemeVariants(NotificationVariant.LUMO_SUCCESS) }
            close()
            onSave()
        }.onFailure { ex ->
            Notification.show(ex.message ?: "Save failed", 5_000, Notification.Position.BOTTOM_END)
                .apply { addThemeVariants(NotificationVariant.LUMO_ERROR) }
        }
    }

    private fun validate(): Boolean {
        var ok = true
        if (nameField.value.isBlank()) {
            nameField.errorMessage = "Name is required"
            nameField.isInvalid    = true
            ok = false
        } else {
            nameField.isInvalid = false
        }
        if (emailField.value.isBlank() || !emailField.value.contains("@")) {
            emailField.errorMessage = "Valid email is required"
            emailField.isInvalid    = true
            ok = false
        } else {
            emailField.isInvalid = false
        }
        if (user == null && passwordField.value.isBlank()) {
            passwordField.errorMessage = "Password is required"
            passwordField.isInvalid    = true
            ok = false
        } else {
            passwordField.isInvalid = false
        }
        if (roleField.value == null) {
            roleField.errorMessage = "Role is required"
            roleField.isInvalid    = true
            ok = false
        } else {
            roleField.isInvalid = false
        }
        return ok
    }
}
