package com.example.usermgmt.ui

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Paragraph

/**
 * A lightweight yes/no confirmation dialog.
 */
class ConfirmDialog(
    title: String,
    message: String,
    private val onConfirm: () -> Unit,
) : Dialog() {

    init {
        headerTitle = title
        add(Paragraph(message))

        val confirmBtn = Button("Confirm") {
            onConfirm()
            close()
        }.apply { addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR) }

        val cancelBtn = Button("Cancel") { close() }

        footer.add(cancelBtn, confirmBtn)
    }
}
