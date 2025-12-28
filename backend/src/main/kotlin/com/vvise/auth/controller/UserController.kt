package com.vvise.auth.controller

import com.vvise.auth.dto.UserDto
import com.vvise.auth.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllUsers(): ResponseEntity<List<UserDto>> {
        val users = userService.findAll()
        return ResponseEntity.ok(users.map { UserDto.fromEntity(it) })
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    fun getUser(@PathVariable id: Long): ResponseEntity<UserDto> {
        val user = userService.findById(id)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(UserDto.fromEntity(user))
    }

    @PostMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    fun makeAdmin(@PathVariable id: Long): ResponseEntity<UserDto> {
        val user = userService.addAdminRole(id)
        return ResponseEntity.ok(UserDto.fromEntity(user))
    }

    @DeleteMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    fun removeAdmin(@PathVariable id: Long): ResponseEntity<UserDto> {
        val user = userService.removeAdminRole(id)
        return ResponseEntity.ok(UserDto.fromEntity(user))
    }
}
