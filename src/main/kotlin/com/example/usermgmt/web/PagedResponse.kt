package com.example.usermgmt.web

import org.springframework.data.domain.Page

/** Stable, framework-agnostic pagination envelope for API responses. */
data class PagedResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean,
) {
    companion object {
        fun <T> of(page: Page<T>) = PagedResponse(
            content       = page.content,
            page          = page.number,
            size          = page.size,
            totalElements = page.totalElements,
            totalPages    = page.totalPages,
            first         = page.isFirst,
            last          = page.isLast,
        )
    }
}
