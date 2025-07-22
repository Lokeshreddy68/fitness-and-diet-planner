package com.fitnessplanner.service;

import com.fitnessplanner.model.Role;
import com.fitnessplanner.model.User;
import com.fitnessplanner.repository.RoleRepository;
import com.fitnessplanner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Define role names as constants for consistency
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";


    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerNewUser(String username, String email, String rawPassword) throws Exception {
        if (userRepository.existsByUsername(username)) {
            throw new Exception("Username already exists: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new Exception("Email already exists: " + email);
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEnabled(true);

        // Assign default role (e.g., ROLE_USER)
        Role userRole = roleRepository.findByName(ROLE_USER)
                .orElseGet(() -> {
                    Role newRole = new Role(ROLE_USER);
                    return roleRepository.save(newRole);
                });
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Method to ensure roles exist in the database, called on application startup perhaps
    @Transactional
    public void ensureRolesExist() {
        if (roleRepository.findByName(ROLE_USER).isEmpty()) {
            roleRepository.save(new Role(ROLE_USER));
        }
        if (roleRepository.findByName(ROLE_ADMIN).isEmpty()) {
            roleRepository.save(new Role(ROLE_ADMIN));
        }
    }
}
