package com.example.usermgmt.web

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

/**
 * Translates service-layer and validation failures into RFC 7807 ProblemDetail
 * responses. Scoped to the REST controllers so it doesn't interfere with Vaadin.
 */
@RestControllerAdvice(basePackages = ["com.example.usermgmt.web"])
class RestExceptionHandler {

    /** Business-rule violations (duplicate email, self-deletion, …). */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(ex: IllegalArgumentException): ProblemDetail =
        problem(HttpStatus.BAD_REQUEST, ex.message ?: "Invalid request")

    /** Missing entities. */
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ProblemDetail =
        problem(HttpStatus.NOT_FOUND, ex.message ?: "Resource not found")

    /** Bean-validation failures on @Valid request bodies. */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ProblemDetail {
        val detail = ex.bindingResult.fieldErrors.joinToString("; ") {
            "${it.field}: ${it.defaultMessage}"
        }
        return problem(HttpStatus.BAD_REQUEST, detail.ifEmpty { "Validation failed" })
    }

    private fun problem(status: HttpStatus, detail: String): ProblemDetail =
        ProblemDetail.forStatusAndDetail(status, detail).apply {
            setProperty("timestamp", Instant.now())
        }
}
