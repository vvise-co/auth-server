package com.vvise.demo.controller;

import com.vvise.demo.dto.UserDto;
import com.vvise.demo.entity.User;
import com.vvise.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> ResponseEntity.ok(UserDto.fromEntity(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> makeAdmin(@PathVariable Long id) {
        User user = userService.addAdminRole(id);
        return ResponseEntity.ok(UserDto.fromEntity(user));
    }

    @DeleteMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> removeAdmin(@PathVariable Long id) {
        User user = userService.removeAdminRole(id);
        return ResponseEntity.ok(UserDto.fromEntity(user));
    }
}
