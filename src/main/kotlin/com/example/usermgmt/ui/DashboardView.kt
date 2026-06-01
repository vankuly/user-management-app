package com.example.usermgmt.ui

import com.example.usermgmt.domain.UserDto
import com.example.usermgmt.domain.UserFilter
import com.example.usermgmt.service.UserService
import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.provider.SortDirection
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.annotation.security.RolesAllowed
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.core.context.SecurityContextHolder
@Route("", layout = MainLayout::class)
@PageTitle("Dashboard | User Management")
@RolesAllowed("USER", "ADMIN")
open class DashboardView(private val userService: UserService) : VerticalLayout() {

    private val nameFilter  = TextField()
    private val emailFilter = TextField()
    private val pageSizeSelect = Select<Int>()
    private val grid        = Grid<UserDto>()

    private val prevButton  = Button(VaadinIcon.ANGLE_LEFT.create())
    private val nextButton  = Button(VaadinIcon.ANGLE_RIGHT.create())
    private val pageInfo    = Span()

    // ── Pagination state ───────────────────────────────────────────────────────
    private var currentPage = 0
    private var pageSize     = 25
    private var sort: Sort   = Sort.by(Sort.Direction.ASC, "name")
    private var totalPages   = 1

    private val isAdmin: Boolean
        get() = SecurityContextHolder.getContext().authentication
            ?.authorities?.any { it.authority == "ROLE_ADMIN" } == true

    init {
        setSizeFull()
        isPadding = true
        isSpacing = true

        buildToolbar()
        buildGrid()
        buildPager()
        loadPage()
    }

    // ── Toolbar ──────────────────────────────────────────────────────────────

    private fun buildToolbar() {
        val toolbar = HorizontalLayout().apply {
            defaultVerticalComponentAlignment = FlexComponent.Alignment.END
            setWidthFull()
            isPadding = false
            isSpacing = true
        }

        nameFilter.apply {
            label                = "Name"
            placeholder          = "Filter by name…"
            isClearButtonVisible = true
            prefixComponent      = VaadinIcon.SEARCH.create()
            addValueChangeListener { refresh() }
        }
        emailFilter.apply {
            label                = "Email"
            placeholder          = "Filter by email…"
            isClearButtonVisible = true
            prefixComponent      = VaadinIcon.ENVELOPE.create()
            addValueChangeListener { refresh() }
        }

        pageSizeSelect.apply {
            label = "Rows per page"
            setItems(10, 25, 50, 100)
            value = pageSize
            addValueChangeListener {
                pageSize    = it.value
                currentPage = 0
                loadPage()
            }
        }

        toolbar.add(nameFilter, emailFilter, pageSizeSelect)

        val spacer = Div().apply { style.set("flex-grow", "1") }
        toolbar.add(spacer)

        if (isAdmin) {
            val createBtn = Button("Create User", VaadinIcon.PLUS.create()).apply {
                addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                addClickListener { openCreateDialog() }
            }
            toolbar.add(createBtn)
        }

        add(toolbar)
    }

    // ── Grid ─────────────────────────────────────────────────────────────────

    private fun buildGrid() {
        grid.apply {
            setSizeFull()
            isAllRowsVisible = false

            addColumn(UserDto::name).apply {
                setHeader("Name")
                setSortProperty("name")
                isResizable = true
            }
            addColumn(UserDto::email).apply {
                setHeader("Email")
                setSortProperty("email")
                isResizable = true
            }
            addColumn { it.role.name }.apply {
                setHeader("Role")
                isResizable = true
            }
            addColumn { Formatters.dateTime(it.createdAt) }.apply {
                setHeader("Created At")
                setSortProperty("createdAt")
                isResizable = true
            }
            addColumn { Formatters.dateTime(it.updatedAt) }.apply {
                setHeader("Updated At")
                setSortProperty("updatedAt")
                isResizable = true
            }

            if (isAdmin) {
                addComponentColumn { user ->
                    HorizontalLayout().apply {
                        isSpacing = false
                        add(
                            Button(VaadinIcon.EDIT.create()).apply {
                                addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL)
                                addClickListener { openEditDialog(user) }
                            },
                            Button(VaadinIcon.TRASH.create()).apply {
                                addThemeVariants(
                                    ButtonVariant.LUMO_TERTIARY,
                                    ButtonVariant.LUMO_ERROR,
                                    ButtonVariant.LUMO_SMALL,
                                )
                                addClickListener { openDeleteConfirm(user) }
                            },
                        )
                    }
                }.apply {
                    setHeader("Actions")
                    isResizable = false
                    flexGrow    = 0
                    setWidth("120px")
                }
            }

            // Server-side sorting: re-query page 0 whenever the sort changes.
            addSortListener { event ->
                val orders = event.sortOrder.flatMap { gridOrder ->
                    gridOrder.sorted.getSortOrder(gridOrder.direction).toList()
                        .map { qso ->
                            val dir = if (qso.direction == SortDirection.ASCENDING) Sort.Direction.ASC
                                      else Sort.Direction.DESC
                            Sort.Order(dir, qso.sorted)
                        }
                }
                sort = if (orders.isEmpty()) Sort.by(Sort.Direction.ASC, "name")
                       else Sort.by(orders)
                currentPage = 0
                loadPage()
            }
        }
        add(grid)
        grid.element.style.set("flex-grow", "1")
    }

    // ── Pager ────────────────────────────────────────────────────────────────

    private fun buildPager() {
        prevButton.apply {
            addThemeVariants(ButtonVariant.LUMO_TERTIARY)
            addClickListener {
                if (currentPage > 0) {
                    currentPage--
                    loadPage()
                }
            }
        }
        nextButton.apply {
            addThemeVariants(ButtonVariant.LUMO_TERTIARY)
            addClickListener {
                if (currentPage < totalPages - 1) {
                    currentPage++
                    loadPage()
                }
            }
        }

        val pager = HorizontalLayout(prevButton, pageInfo, nextButton).apply {
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
            isSpacing = true
            setWidthFull()
            justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        }
        add(pager)
    }

    // ── Data loading ───────────────────────────────────────────────────────────

    private fun currentFilter() = UserFilter(
        name  = nameFilter.value.takeIf { it.isNotBlank() },
        email = emailFilter.value.takeIf { it.isNotBlank() },
    )

    private fun loadPage() {
        val filter = currentFilter()
        val page   = userService.findAll(filter, PageRequest.of(currentPage, pageSize, sort))

        totalPages  = page.totalPages.coerceAtLeast(1)
        currentPage = currentPage.coerceIn(0, totalPages - 1)

        grid.setItems(page.content)

        pageInfo.text     = "Page ${currentPage + 1} of $totalPages  (${page.totalElements} total)"
        prevButton.isEnabled = currentPage > 0
        nextButton.isEnabled = currentPage < totalPages - 1
    }

    /** Resets to the first page and reloads — used by filters and mutations. */
    private fun refresh() {
        currentPage = 0
        loadPage()
    }

    // ── Dialogs ──────────────────────────────────────────────────────────────

    private fun openCreateDialog() {
        UserDialog(null, userService) { refresh() }.open()
    }

    private fun openEditDialog(user: UserDto) {
        UserDialog(user, userService) { refresh() }.open()
    }

    private fun openDeleteConfirm(user: UserDto) {
        ConfirmDialog(
            title     = "Delete user?",
            message   = "Delete '${user.name}' (${user.email})? This cannot be undone.",
            onConfirm = {
                runCatching {
                    val principal = SecurityContextHolder.getContext().authentication.name
                    userService.delete(user.id, principal)
                    showSuccess("User '${user.name}' deleted")
                    refresh()
                }.onFailure { showError(it.message ?: "Delete failed") }
            },
        ).open()
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun showSuccess(msg: String) =
        Notification.show(msg, 3_000, Notification.Position.BOTTOM_END)
            .also { it.addThemeVariants(NotificationVariant.LUMO_SUCCESS) }

    private fun showError(msg: String) =
        Notification.show(msg, 5_000, Notification.Position.BOTTOM_END)
            .also { it.addThemeVariants(NotificationVariant.LUMO_ERROR) }
}
