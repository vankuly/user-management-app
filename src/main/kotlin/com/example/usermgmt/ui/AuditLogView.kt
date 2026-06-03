package com.example.usermgmt.ui

import com.example.usermgmt.domain.AuditLogDto
import com.example.usermgmt.web.AuditLogController
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.data.provider.DataProvider
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.annotation.security.RolesAllowed
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@Route("audit-log", layout = MainLayout::class)
@PageTitle("Audit Log | User Management")
@RolesAllowed("ADMIN")
open class AuditLogView(private val auditLogController: AuditLogController) : VerticalLayout() {

    init {
        setSizeFull()
        isPadding = true

        val grid = Grid<AuditLogDto>()
        grid.setSizeFull()

        grid.addColumn { it.action.name    }.setHeader("Action").setSortProperty("action")
        grid.addColumn { it.targetUserEmail }.setHeader("Target User").isResizable = true
        grid.addColumn { it.performedBy    }.setHeader("Performed By").isResizable = true
        grid.addColumn { it.details ?: "-" }.setHeader("Details").isResizable = true
        grid.addColumn { Formatters.dateTimeSeconds(it.createdAt) }.apply {
            setHeader("Timestamp")
            setSortProperty("createdAt")
            isResizable = true
        }

        val dataProvider = DataProvider.fromCallbacks<AuditLogDto>(
            { query ->
                val sort = query.sortOrders.fold(Sort.unsorted()) { acc, so ->
                    val dir = if (so.direction == com.vaadin.flow.data.provider.SortDirection.ASCENDING)
                        Sort.Direction.ASC else Sort.Direction.DESC
                    acc.and(Sort.by(dir, so.sorted))
                }.let { if (it.isUnsorted) Sort.by(Sort.Direction.DESC, "createdAt") else it }

                val pageable = PageRequest.of(
                    query.offset / query.limit.coerceAtLeast(1),
                    query.limit.coerceAtLeast(1),
                    sort,
                )
                auditLogController.list(pageable).content.stream()
            },
            { auditLogController.list(PageRequest.of(0, 1)).totalElements.toInt() },
        )

        grid.setItems(dataProvider)
        add(grid)
    }
}
