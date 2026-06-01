package com.example.usermgmt.web

import com.example.usermgmt.domain.CreateUserRequest
import com.example.usermgmt.domain.UpdateUserRequest
import com.example.usermgmt.domain.UserDto
import com.example.usermgmt.domain.UserFilter
import com.example.usermgmt.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.annotation.security.RolesAllowed
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.security.Principal

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User CRUD operations")
class UserController(private val userService: UserService) {

    @GetMapping
    @RolesAllowed("USER", "ADMIN")
    @Operation(summary = "List users", description = "Paginated, optionally filtered by name and/or email.")
    fun list(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) email: String?,
        @ParameterObject @PageableDefault(size = 25, sort = ["name"]) pageable: Pageable,
    ): PagedResponse<UserDto> =
        PagedResponse.of(userService.findAll(UserFilter(name, email), pageable))

    @GetMapping("/{id}")
    @RolesAllowed("USER", "ADMIN")
    @Operation(summary = "Get a single user by id")
    fun get(@PathVariable id: Long): UserDto = userService.findById(id)

    @PostMapping
    @RolesAllowed("ADMIN")
    @Operation(summary = "Create a user")
    fun create(
        @Valid @RequestBody request: CreateUserRequest,
        principal: Principal,
    ): ResponseEntity<UserDto> {
        val created = userService.create(request, principal.name)
        return ResponseEntity.created(URI.create("/api/users/${created.id}")).body(created)
    }

    @PutMapping("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Update a user", description = "Omit or null the password field to keep the current one.")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateUserRequest,
        principal: Principal,
    ): UserDto = userService.update(id, request, principal.name)

    @DeleteMapping("/{id}")
    @RolesAllowed("ADMIN")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a user", description = "You cannot delete the account you are authenticated as.")
    fun delete(@PathVariable id: Long, principal: Principal) {
        userService.delete(id, principal.name)
    }
}
