package com.hszg.db_statistics.auth;

import com.hszg.db_statistics.entity.Role;
import com.hszg.db_statistics.repository.AppUserRepository;
import com.hszg.db_statistics.repository.RoleRepository;
import com.hszg.db_statistics.service.JwtService;
import com.hszg.db_statistics.entity.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

        private final AppUserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;
        private final RoleRepository roleRepository;

        public AuthenticationResponse register(AuthenticationRequest request) {
                log.info("Attempting to register new user with username: {}", request.getUsername().toLowerCase());
                if (userRepository.findByUsername(request.getUsername().toLowerCase()).isPresent()) {
                        log.warn("Registration failed: Username '{}' is already taken.", request.getUsername().toLowerCase());
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
                }

                Role userRole = roleRepository.findByName("USER")
                                .orElseThrow(() -> {
                                        log.error("Registration failed: Default role 'USER' not found in database.");
                                        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                        "Default role not set.");
                                });

                AppUser user = AppUser.builder()
                                .username(request.getUsername().toLowerCase())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .role(userRole)
                                .build();

                userRepository.save(user);
                long id = user.getId();

                log.info("Registered user: {} with id {}", user.getUsername(), id);

                String jwtToken = jwtService.generateToken(user);
                log.info("User '{}' registered successfully.", request.getUsername().toLowerCase());
                return AuthenticationResponse.builder()
                                .token(jwtToken)
                                .id(id)
                                .role(user.getRole().getName())
                                .build();
        }

        public AuthenticationResponse authenticate(AuthenticationRequest request) {
                log.info("Attempting to authenticate user with username: {}", request.getUsername().toLowerCase());
                // 1. Spring Security prüft Username & Passwort
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getUsername().toLowerCase(),
                                                request.getPassword()));

                // 2. Wenn wir hier sind, war der Login korrekt. User laden.
                AppUser user = userRepository.findByUsername(request.getUsername().toLowerCase())
                                .orElseThrow(() -> {
                                       log.warn("Login failed: Username '{}' not found in database.", request.getUsername().toLowerCase());
                                    return new UsernameNotFoundException("User not found");
                                });

                log.info("User '{}' authenticated successfully.", request.getUsername().toLowerCase());
                // 3. Token generieren
                String jwtToken = jwtService.generateToken(user);
                return AuthenticationResponse.builder()
                                .token(jwtToken)
                                .id(user.getId())
                                .role(user.getRole().getName())
                                .build();
        }
}