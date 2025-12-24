package com.vvise.demo.service;

import com.vvise.demo.dto.UserDto;
import com.vvise.demo.entity.Role;
import com.vvise.demo.entity.User;
import com.vvise.demo.repository.RoleRepository;
import com.vvise.demo.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @PostConstruct
    public void initRoles() {
        if (!roleRepository.existsByName(Role.ROLE_USER)) {
            roleRepository.save(Role.builder().name(Role.ROLE_USER).build());
            log.info("Created ROLE_USER");
        }
        if (!roleRepository.existsByName(Role.ROLE_ADMIN)) {
            roleRepository.save(Role.builder().name(Role.ROLE_ADMIN).build());
            log.info("Created ROLE_ADMIN");
        }
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByProviderAndProviderId(User.AuthProvider provider, String providerId) {
        return userRepository.findByProviderAndProviderId(provider, providerId);
    }

    @Transactional
    public User createOrUpdateOAuth2User(String email, String name, String imageUrl,
                                          User.AuthProvider provider, String providerId) {
        Optional<User> existingUser = findByProviderAndProviderId(provider, providerId);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setName(name);
            user.setImageUrl(imageUrl);
            user.setEmail(email);
            return userRepository.save(user);
        }

        Role userRole = roleRepository.findByName(Role.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User newUser = User.builder()
                .email(email)
                .name(name)
                .imageUrl(imageUrl)
                .provider(provider)
                .providerId(providerId)
                .roles(Set.of(userRole))
                .build();

        return userRepository.save(newUser);
    }

    @Transactional
    public User addAdminRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role adminRole = roleRepository.findByName(Role.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Admin role not found"));

        user.getRoles().add(adminRole);
        return userRepository.save(user);
    }

    @Transactional
    public User removeAdminRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.getRoles().removeIf(role -> role.getName().equals(Role.ROLE_ADMIN));
        return userRepository.save(user);
    }

    public UserDto toDto(User user) {
        return UserDto.fromEntity(user);
    }
}
