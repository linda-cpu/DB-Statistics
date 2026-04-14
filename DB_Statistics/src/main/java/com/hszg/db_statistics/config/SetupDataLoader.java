package com.hszg.db_statistics.config;

import com.hszg.db_statistics.entity.AppUser;
import com.hszg.db_statistics.entity.Privilege;
import com.hszg.db_statistics.entity.Role;
import com.hszg.db_statistics.repository.AppUserRepository;
import com.hszg.db_statistics.repository.PrivilegeRepository;
import com.hszg.db_statistics.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    boolean alreadySetup = false;
    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppUserRepository userRepository;


    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Starting setup of initial data...");
        if(alreadySetup) return;

        Privilege readStats = createPrivilegeIfNotFound("READ_STATISTICS");
        Privilege manageUsers = createPrivilegeIfNotFound("MANAGE_USERS");
        Privilege writeAnnotations = createPrivilegeIfNotFound("WRITE_ANNOTATIONS");
        Privilege initWrite = createPrivilegeIfNotFound("INIT_WRITE");
        Privilege readStation = createPrivilegeIfNotFound("READ_STATION");
        Privilege writeStation = createPrivilegeIfNotFound("WRITE_STATION");
        Privilege readData = createPrivilegeIfNotFound("READ_DATA");
        Privilege readDelayReason = createPrivilegeIfNotFound("READ_DELAY_REASON");
        Privilege manageFavorites = createPrivilegeIfNotFound("MANAGE_FAVORITES");

        Role adminRole = createRoleIfNotFound("ADMIN", Arrays.asList(readStats, manageUsers, writeAnnotations, initWrite,  readStation, writeStation, readData, readDelayReason, manageFavorites));
        createRoleIfNotFound("USER", Arrays.asList(readStats, readData, readStation, writeStation, readDelayReason, manageFavorites));

        if (userRepository.findByUsername("admin").isEmpty()) {

            var adminUser = new AppUser();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setRole(adminRole);

            userRepository.save(adminUser);
            log.info("Default Admin account created.");
        } else {
            log.info("Default Admin account already exists.");
        }

        log.info("Initial data setup complete.");
        alreadySetup = true;
    }

    @Transactional
    Privilege createPrivilegeIfNotFound(String name) {
        return privilegeRepository.findByName(name)
                .orElseGet(() -> privilegeRepository.save(new Privilege(name)));
    }

    @Transactional
    Role createRoleIfNotFound(String name, Collection<Privilege> privileges) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role role = new Role(name);
                    role.setPrivileges(privileges);
                    return roleRepository.save(role);
                });
    }
}
