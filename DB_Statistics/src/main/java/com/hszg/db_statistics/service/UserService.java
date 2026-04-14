package com.hszg.db_statistics.service;

import com.hszg.db_statistics.dto.UpdateUserDto;
import com.hszg.db_statistics.dto.UserDto;
import com.hszg.db_statistics.entity.AppUser;
import com.hszg.db_statistics.repository.AppUserRepository;
import com.hszg.db_statistics.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public Iterable<UserDto> getAllUsers() {
        log.info("Get All Users");
        var users = userRepository.findAll();

        return users.stream().map(appUser -> {
            UserDto dto = new UserDto();
            dto.setId(appUser.getId());
            dto.setUsername(appUser.getUsername().toLowerCase());
            dto.setRole(appUser.getRole().getName());
            return dto;
        }).toList();
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Delete User: {}", id);
        var user = userRepository.findById(id);

        if (user.isEmpty()) {
            log.error("User {} not found for deletion", id);
            throw new RuntimeException("User not found");
        }

        userRepository.deleteById(id);
        log.info("User {} deleted successfully", id);
    }

    public UserDto getUser(Long id) {
        log.info("Get User: {}", id);
        var user = userRepository.findById(id);

        if (user.isEmpty()) {
            log.error("User {} not found", id);
            throw new RuntimeException("User not found");
        }

        var appUser = user.get();

        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setUsername(appUser.getUsername().toLowerCase());
        dto.setRole(appUser.getRole().getName());
        log.info("User {} retrieved successfully", id);
        return dto;
    }

    @Transactional
    public UserDto updateUser(Long id, UpdateUserDto request) {
        log.info("Update User: {}", id);
        var user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User '{}' not found for update", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User '" + id + "' not found");
            });

        // Username
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            if (!request.getUsername().equalsIgnoreCase(user.getUsername()) &&
                    userRepository.findByUsername(request.getUsername().toLowerCase()).isPresent()) {
                log.error("Username '{}' is already taken", request.getUsername().toLowerCase());
                throw new ResponseStatusException(HttpStatus.CONFLICT,"Username '" + request.getUsername().toLowerCase() + "' is already taken");
            }
            user.setUsername(request.getUsername().toLowerCase());
        }

        // Role (with SecurityCheck)
        if (request.getRole() != null && !request.getRole().isBlank()) {

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            assert auth != null;
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> Objects.equals(a.getAuthority(), "MANAGE_USERS") || Objects.equals(a.getAuthority(), "ADMIN"));

            if (!isAdmin) {
                log.error("User '{}' has no permission to change roles", auth.getName());
                throw new AccessDeniedException("Not enough permissions to change roles.");
            }

            String roleName = request.getRole().toUpperCase();
            var dbRole = roleRepository.findByName(roleName)
                    .orElseThrow(() -> {
                        log.error("Role '{}' not found in database", roleName);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Role '" + roleName + "' not found");
                    });

            user.setRole(dbRole);
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        var savedUser = userRepository.save(user);

        UserDto result = new UserDto();
        result.setId(user.getId());
        result.setUsername(savedUser.getUsername().toLowerCase());
        result.setRole(savedUser.getRole().getName());
        log.info("User '{}' updated successfully", id);
        return result;
    }

    public UserDto getUserbyUsername(String username) {
        Optional<AppUser> user = userRepository.findByUsername(username);
        UserDto dto = new UserDto();
        if (user.isPresent()) {
            dto.setId(user.get().getId());
            dto.setUsername(user.get().getUsername().toLowerCase());
            dto.setRole(user.get().getRole().getName());
        }
        else  {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return dto;
    }
}