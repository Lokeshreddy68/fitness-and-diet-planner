package com.fitnessplanner;

import com.fitnessplanner.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling; // For scheduled tasks later

@SpringBootApplication
@EnableScheduling // For Smart Plan Suggestions (Module 8)
public class FitnessPlannerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FitnessPlannerApplication.class, args);
    }

    @Bean
    CommandLineRunner run(UserService userService) {
        return args -> {
            userService.ensureRolesExist();
            // You could also create a default admin user here if needed for testing
            // try {
            // userService.registerNewUser("admin", "admin@example.com", "adminpass", UserService.ROLE_ADMIN);
            // } catch (Exception e) {
            // System.out.println("Admin user already exists or error creating: " + e.getMessage());
            // }
        };
    }
}
