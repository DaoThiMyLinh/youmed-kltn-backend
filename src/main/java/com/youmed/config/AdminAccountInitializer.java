package com.youmed.config;

import com.youmed.entity.User;
import com.youmed.enums.Role;
import com.youmed.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String adminEmail = "admin@gmail.com";
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("System Administrator")
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Default Admin account created successfully! Email: {}, Password: {}", adminEmail, "admin123");
        } else {
            log.info("Default Admin account already exists.");
        }
    }
}
