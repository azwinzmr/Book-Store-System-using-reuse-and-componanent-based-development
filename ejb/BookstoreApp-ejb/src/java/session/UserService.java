package session;

import entities.User;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.mindrot.jbcrypt.BCrypt; // Import the jBCrypt library

@Stateless
public class UserService {

    @PersistenceContext(unitName = "BookstoreApp-ejbPU")
    private EntityManager em;

    /**
     * Creates a new user in the database.
     * This method assumes Container-Managed Transactions (CMT).
     * The container will automatically begin and commit/rollback a transaction
     * for this method.
     *
     * @param user The User entity to be persisted.
     * @return The persisted User entity.
     * @throws RuntimeException if an error occurs during user creation.
     */
    public User createUser(User user) {
        try {
            // HASH THE PASSWORD BEFORE PERSISTING
            // Generate a salt and hash the plain text password
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
            user.setPassword(hashedPassword); // Store the hashed password

            em.persist(user);
            return user;
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error creating user: " + e.getMessage());
        }
    }

    /**
     * Authenticates a user based on username and password.
     *
     * @param username The username to authenticate.
     * @param plainTextPassword The plain text password provided by the user.
     * @return The authenticated User entity if credentials are valid, null otherwise.
     */
    public User authenticate(String username, String plainTextPassword) {
        try {
            // Retrieve the user by username
            User user = findByUsername(username);

            if (user != null) {
                // VERIFY THE PROVIDED PLAIN TEXT PASSWORD AGAINST THE HASHED PASSWORD
                // BCrypt.checkpw handles salting and hashing the plainTextPassword internally
                // then compares it to the stored hash.
                if (BCrypt.checkpw(plainTextPassword, user.getPassword())) {
                    return user; // Passwords match
                }
            }
            return null; // User not found or passwords do not match
        } catch (NoResultException e) {
            // No user found with the given username
            return null;
        } catch (Exception e) {
            System.err.println("Error during authentication: " + e.getMessage());
            e.printStackTrace();
            return null; // Treat as invalid credentials for security
        }
    }

    /**
     * Finds a user by their username.
     *
     * @param username The username to search for.
     * @return The User entity if found, null otherwise.
     */
    public User findByUsername(String username) {
        try {
            TypedQuery<User> query = em.createNamedQuery("User.findByUsername", User.class);
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null; // User not found
        } catch (Exception e) {
            System.err.println("Error finding user by username: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Finds a user by their ID.
     *
     * @param id The ID of the user.
     * @return The User entity if found, null otherwise.
     */
    public User findById(Long id) {
        try {
            return em.find(User.class, id);
        } catch (Exception e) {
            System.err.println("Error finding user by ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves all users from the database.
     *
     * @return A list of all User entities.
     */
    public List<User> getAllUsers() {
        try {
            TypedQuery<User> query = em.createNamedQuery("User.findAll", User.class);
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error retrieving all users: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error retrieving all users: " + e.getMessage());
        }
    }

    /**
     * Updates an existing user in the database.
     * If the password in the user object has been changed (e.g., from a user profile update form),
     * it should also be hashed before merging.
     *
     * @param user The detached User entity with updated information.
     * @return The merged (managed) User entity.
     * @throws RuntimeException if an error occurs during user update.
     */
    public User updateUser(User user) {
        try {
            // If the password field in the 'user' object is meant to be updated
            // with a new plain text password, you would hash it here.
            // Example:
            // if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) { // Check if it's not already hashed
            //    user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
            // }
            return em.merge(user);
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error updating user: " + e.getMessage());
        }
    }

    /**
     * Deletes a user from the database by their ID.
     *
     * @param userId The ID of the user to delete.
     * @throws RuntimeException if an error occurs during user deletion.
     */
    public void deleteUser(Long userId) {
        try {
            User user = em.find(User.class, userId);
            if (user != null) {
                em.remove(user);
            }
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error deleting user: " + e.getMessage());
        }
    }

    /**
     * Checks if a username already exists in the database.
     *
     * @param username The username to check.
     * @return true if the username exists, false otherwise.
     */
    public boolean usernameExists(String username) {
        try {
            return findByUsername(username) != null;
        } catch (Exception e) {
            System.err.println("Error checking if username exists: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if an email address already exists in the database.
     *
     * @param email The email address to check.
     * @return true if the email exists, false otherwise.
     */
    public boolean emailExists(String email) {
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class);
            query.setParameter("email", email);
            return query.getSingleResult() > 0;
        } catch (Exception e) {
            System.err.println("Error checking if email exists: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
