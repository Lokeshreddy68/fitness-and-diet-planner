package com.fitnessplanner.controller;

import com.fitnessplanner.dto.UserRegistrationDto;
import com.fitnessplanner.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
// import javax.validation.Valid; // If using JSR 303 validation
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginForm(Model model, String error, String logout) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been logged out successfully.");
        }
        return "login"; // Renders login.html
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userDto", new UserRegistrationDto());
        return "register"; // Renders register.html
    }

    @PostMapping("/register")
    public String processRegistration(
            @ModelAttribute("userDto") /*@Valid*/ UserRegistrationDto userDto, // Uncomment @Valid if using JSR 303
            BindingResult bindingResult, // For JSR 303 validation errors
            Model model,
            RedirectAttributes redirectAttributes) {

        // Manual validation for now, can be replaced/augmented with @Valid
        if (userDto.getUsername() == null || userDto.getUsername().trim().isEmpty()) {
            bindingResult.rejectValue("username", "userDto.username", "Username is required.");
        } else if (userDto.getUsername().length() < 3 || userDto.getUsername().length() > 20) {
             bindingResult.rejectValue("username", "userDto.username", "Username must be between 3 and 20 characters.");
        }

        if (userDto.getEmail() == null || userDto.getEmail().trim().isEmpty()) {
            bindingResult.rejectValue("email", "userDto.email", "Email is required.");
        } else if (!userDto.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) { // Basic email regex
            bindingResult.rejectValue("email", "userDto.email", "Invalid email format.");
        }


        if (userDto.getPassword() == null || userDto.getPassword().isEmpty()) {
            bindingResult.rejectValue("password", "userDto.password", "Password is required.");
        } else if (userDto.getPassword().length() < 6) {
            bindingResult.rejectValue("password", "userDto.password", "Password must be at least 6 characters.");
        }


        if (!userDto.isPasswordConfirmed()) {
            bindingResult.rejectValue("confirmPassword", "userDto.confirmPassword", "Passwords do not match.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("userDto", userDto); // Send DTO back to pre-fill form
            return "register"; // Return to registration page with errors
        }

        try {
            userService.registerNewUser(userDto.getUsername(), userDto.getEmail(), userDto.getPassword());
            redirectAttributes.addFlashAttribute("registrationSuccess", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            // Log the exception e.g. e.printStackTrace();
            model.addAttribute("userDto", userDto); // Send DTO back
            model.addAttribute("registrationError", e.getMessage()); // Display specific error from service
            return "register";
        }
    }

    // A simple home page controller
    @GetMapping({"/", "/home"})
    public String home() {
        return "home"; // Renders home.html
    }
}
