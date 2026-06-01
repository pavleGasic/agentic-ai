package com.master.incidentmanagementserver.config;

import com.master.incidentmanagementserver.entity.Role;
import com.master.incidentmanagementserver.entity.User;
import com.master.incidentmanagementserver.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Value("${incident.system.password:}")
    private String systemPassword;

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("alice").isEmpty()) {
                User alice = new User();
                alice.setUsername("alice");
                alice.setPassword(passwordEncoder.encode("password123"));
                alice.setRole(Role.CUSTOMER);
                userRepository.save(alice);
                log.info("Seeded user: alice (CUSTOMER)");
            }
            if (userRepository.findByUsername("bob").isEmpty()) {
                User bob = new User();
                bob.setUsername("bob");
                bob.setPassword(passwordEncoder.encode("devpass456"));
                bob.setRole(Role.DEVELOPER);
                userRepository.save(bob);
                log.info("Seeded user: bob (DEVELOPER)");
            }
            if (userRepository.findByUsername("system").isEmpty()) {
                String password = (systemPassword != null && !systemPassword.isBlank())
                        ? systemPassword
                        : java.util.UUID.randomUUID().toString();
                User system = new User();
                system.setUsername("system");
                system.setPassword(passwordEncoder.encode(password));
                system.setRole(Role.SYSTEM);
                userRepository.save(system);
                log.info("Seeded user: system (SYSTEM)");
            }
        };
    }
}
