package peps.peps_back.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import peps.peps_back.items.User;
import peps.peps_back.repositories.UserRepository;
import java.sql.Timestamp;

@Component
public class AdminInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminInitializer.class);

    @Value("${ADMIN_USERNAME:admin}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (adminPassword == null || adminPassword.isEmpty()) {
            LOGGER.warn("🚨 ADMIN_PASSWORD environment variable is not set. Default admin might not be created or updated.");
            return;
        }

        LOGGER.info("Verifying default admin user existence...");

        User existingAdmin = userRepository.findByLogin(adminUsername).orElse(null);

        if (existingAdmin == null) {
            User admin = new User();
            admin.setLogin(adminUsername);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setRole("admin");
            admin.setPermission("admin");
            admin.setEnabled(true);

            userRepository.save(admin);
            LOGGER.info("✅ Default admin user '{}' created successfully.", adminUsername);
        } else {
            // Update the password to match the environment variable if needed
            if (!passwordEncoder.matches(adminPassword, existingAdmin.getPasswordHash())) {
               existingAdmin.setPasswordHash(passwordEncoder.encode(adminPassword));
               userRepository.save(existingAdmin);
               LOGGER.info("🔄 Default admin user '{}' password updated to match current environment configuration.", adminUsername);
            } else {
                LOGGER.info("✅ Default admin user '{}' already exists with matching credentials.", adminUsername);
            }
        }
    }
}
