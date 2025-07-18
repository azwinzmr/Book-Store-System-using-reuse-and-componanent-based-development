package controllers;

import entities.User;
import session.UserService;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named
@RequestScoped
public class RegistrationController implements Serializable {
    
    @EJB
    private UserService userService;
    
    private String username;
    private String password; // This will hold the plain text password from the UI
    private String confirmPassword;
    private String email;
    
    /**
     * Handles new user registration.
     * Validates input, checks for existing username/email, creates the user,
     * and redirects to the login page upon success.
     *
     * @return Navigation outcome string.
     */
    public String register() {
        try {
            // Validate form input (client-side validation)
            if (!validateForm()) {
                System.err.println("Registration failed: Form validation errors.");
                return null; // Stay on current page if validation fails
            }
            
            // Check if username already exists in the database
            if (userService.usernameExists(username)) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registration failed", "Username already exists. Please choose a different one."));
                System.err.println("Registration failed for username '" + username + "': Username already exists.");
                return null;
            }
            
            // Check if email already exists in the database
            if (userService.emailExists(email)) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registration failed", "Email already exists. Please use a different email."));
                System.err.println("Registration failed for email '" + email + "': Email already exists.");
                return null;
            }
            
            // Create new user (password will be hashed by UserService)
            User newUser = new User(username, password, email, "USER"); // "USER" is the default role
            userService.createUser(newUser); // UserService handles password hashing
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Registration successful", 
                "Account created successfully. Please login with your new credentials."));
            System.out.println("User '" + username + "' registered successfully.");
            
            // Clear form fields after successful registration
            clearForm();
            
            // Redirect to login page
            return "login?faces-redirect=true";
            
        } catch (Exception e) {
            // Log the exception for server-side debugging
            System.err.println("An unexpected error occurred during registration for username '" + username + "': " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registration failed", 
                "An unexpected error occurred during registration. Please try again later."));
            return null;
        } finally {
            // Always clear sensitive password fields from memory
            this.password = null;
            this.confirmPassword = null;
        }
    }
    
    /**
     * Performs server-side validation for registration form fields.
     * Displays FacesMessages for any validation errors.
     *
     * @return true if all fields are valid, false otherwise.
     */
    private boolean validateForm() {
        boolean valid = true;
        
        System.out.println("Validation: Username: '" + username + "', Email: '" + email + "', Password length: " + (password != null ? password.length() : "null"));

        // Check if all required fields are filled
        if (username == null || username.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Username is required"));
            System.err.println("Validation Failed: Username is required.");
            valid = false;
        }
        
        if (password == null || password.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Password is required"));
            System.err.println("Validation Failed: Password is required.");
            valid = false;
        }
        
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Confirm Password is required"));
            System.err.println("Validation Failed: Confirm Password is required.");
            valid = false;
        }
        
        if (email == null || email.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Email is required"));
            System.err.println("Validation Failed: Email is required.");
            valid = false;
        }
        
        // Check if passwords match
        if (password != null && confirmPassword != null && !password.equals(confirmPassword)) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Passwords do not match"));
            System.err.println("Validation Failed: Passwords do not match.");
            valid = false;
        }
        
        // Check username length
        if (username != null && username.length() < 3) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Username must be at least 3 characters long"));
            System.err.println("Validation Failed: Username length < 3. Current: " + username.length());
            valid = false;
        }
        
        // Check password length
        if (password != null && password.length() < 6) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Password must be at least 6 characters long"));
            System.err.println("Validation Failed: Password length < 6. Current: " + password.length());
            valid = false;
        }
        
        // Basic email validation (more robust regex could be used)
        if (email != null && !email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Please enter a valid email address"));
            System.err.println("Validation Failed: Invalid email format. Email: " + email);
            valid = false;
        }
        
        // Future Enhancement: Add stronger password complexity requirements (e.g., mix of cases, numbers, symbols)
        
        System.out.println("Validation: Final result - " + valid);
        return valid;
    }
    
    /**
     * Clears the input fields of the registration form.
     */
    private void clearForm() {
        username = null;
        password = null;
        confirmPassword = null;
        email = null;
    }
    
    // Navigation methods
    public String goToLogin() {
        return "login?faces-redirect=true";
    }
    
    public String goToHome() {
        return "index?faces-redirect=true";
    }
    
    // Getters and Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
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
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}
