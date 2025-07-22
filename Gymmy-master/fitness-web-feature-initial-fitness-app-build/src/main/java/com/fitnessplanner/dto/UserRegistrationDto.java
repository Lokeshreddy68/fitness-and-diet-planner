package com.fitnessplanner.dto;

// Using javax.validation if available, or Spring's validation
// import javax.validation.constraints.Email;
// import javax.validation.constraints.NotEmpty;
// import javax.validation.constraints.Size;

public class UserRegistrationDto {

    // @NotEmpty(message = "Username cannot be empty")
    // @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    // @NotEmpty(message = "Email cannot be empty")
    // @Email(message = "Email should be valid")
    private String email;

    // @NotEmpty(message = "Password cannot be empty")
    // @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String confirmPassword; // For password confirmation on the form

    // Constructors
    public UserRegistrationDto() {
    }

    public UserRegistrationDto(String username, String email, String password, String confirmPassword) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    // Basic validation (can be enhanced with annotations if validation starter is used)
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(confirmPassword);
    }
}
