package controllers;

import entities.User;
import session.UserService;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named
@SessionScoped
public class LoginController implements Serializable {
    
    @EJB
    private UserService userService;
    
    private String username;
    private String password; // This will hold the plain text password from the UI
    private User currentUser;
    
    /**
     * Handles user login attempt.
     * Authenticates the user using UserService and redirects based on role.
     *
     * @return Navigation outcome string.
     */
    public String login() {
        try {
            // userService.authenticate now handles hashing and comparison internally.
            // Pass the plain text password from the UI directly.
            User user = userService.authenticate(username, password);
            if (user != null) {
                currentUser = user;
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Login successful", "Welcome " + user.getUsername()));
                
                // Redirect based on role
                if (user.isAdmin()) {
                    System.out.println("Login successful for admin: " + user.getUsername());
                    return "admin/dashboard?faces-redirect=true";
                } else {
                    System.out.println("Login successful for user: " + user.getUsername());
                    return "user/dashboard?faces-redirect=true";
                }
            } else {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login failed", "Invalid username or password"));
                System.err.println("Login failed for username: " + username + " - Invalid credentials.");
                return null;
            }
        } catch (Exception e) {
            // Log the exception for server-side debugging
            System.err.println("An error occurred during login for username " + username + ": " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "An unexpected error occurred during login."));
            return null;
        } finally {
            // It's good practice to clear the password field after an attempt
            // regardless of success or failure for security reasons.
            this.password = null; 
        }
    }
    
    /**
     * Handles user logout.
     * Clears session data and invalidates the current HTTP session.
     *
     * @return Navigation outcome string to redirect to the home page.
     */
    public String logout() {
        System.out.println("User " + (currentUser != null ? currentUser.getUsername() : "unknown") + " logging out.");
        currentUser = null;
        username = null;
        password = null; // Ensure password field is cleared

        // Invalidate session
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Logout successful", "You have been logged out"));
        
        // MODIFIED LINE: Explicitly use the full path to index.xhtml for redirect
        return "/index.xhtml?faces-redirect=true"; 
    }
    
    /**
     * Checks if a user is currently logged in.
     * @return true if a user is logged in, false otherwise.
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Checks if the current logged-in user has the ADMIN role.
     * @return true if the current user is an admin, false otherwise.
     */
    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
    
    /**
     * Checks if the current logged-in user has the USER role (non-admin).
     * @return true if the current user is a regular user, false otherwise.
     */
    public boolean isUser() {
        return currentUser != null && !currentUser.isAdmin();
    }
    
    // Navigation methods
    public String goToRegister() {
        return "register?faces-redirect=true";
    }
    
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
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
    
    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }
    
    public String getCurrentUserRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }
}
