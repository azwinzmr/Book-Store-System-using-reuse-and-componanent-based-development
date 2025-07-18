package session;

import entities.User;
import entities.Genre;
import entities.Book;
import java.math.BigDecimal;
// import javax.annotation.PostConstruct; // DISABLED - commented out
import javax.ejb.EJB;
import javax.ejb.Singleton;
// import javax.ejb.Startup;  // DISABLED - commented out to prevent automatic initialization

@Singleton
// @Startup  // DISABLED - commented out to prevent automatic execution
public class DataInitializationService {

    @EJB
    private UserService userService;

    @EJB
    private GenreService genreService;

    @EJB
    private BookService bookService;

    // @PostConstruct  // DISABLED - commented out to prevent automatic execution
    /**
     * Initializes sample data (admin user, sample user, genres, and books)
     * into the database if they do not already exist.
     * This method is intended for manual invocation, as @PostConstruct is disabled.
     */
    public void initializeData() {
        System.out.println("Manual data initialization starting...");

        try {
            // Only create admin user if it doesn't exist
            if (!userService.usernameExists("admin")) {
                User admin = new User("admin", "admin123", "admin@bookstore.com", "ADMIN");
                userService.createUser(admin);
                System.out.println("Admin user created successfully");
            } else {
                System.out.println("Admin user already exists");
            }

            // Only create sample user if it doesn't exist
            if (!userService.usernameExists("user1")) {
                User user = new User("user1", "user123", "user1@email.com", "USER");
                userService.createUser(user);
                System.out.println("Sample user created successfully");
            } else {
                System.out.println("Sample user already exists");
            }

            // Check if genres exist, if not create them
            if (genreService.getAllGenres().isEmpty()) {
                System.out.println("Creating genres...");

                Genre fiction = genreService.createGenre(new Genre("Fiction"));
                Genre nonFiction = genreService.createGenre(new Genre("Non-Fiction"));
                Genre sciTech = genreService.createGenre(new Genre("Science & Technology"));
                Genre business = genreService.createGenre(new Genre("Business & Finance"));
                Genre health = genreService.createGenre(new Genre("Health & Wellness"));

                System.out.println("Genres created successfully");

                // Create 15 sample books
                System.out.println("Creating 15 sample books...");

                bookService.createBook(new Book("The Great Adventure", "John Smith", new BigDecimal("25.99"), fiction, 10));
                bookService.createBook(new Book("Learning Java", "Jane Doe", new BigDecimal("45.50"), sciTech, 15));
                bookService.createBook(new Book("Business Strategy", "Mike Johnson", new BigDecimal("35.75"), business, 8));
                bookService.createBook(new Book("Healthy Living Guide", "Sarah Wilson", new BigDecimal("22.00"), health, 12));
                bookService.createBook(new Book("History of Technology", "Robert Brown", new BigDecimal("28.90"), nonFiction, 6));
                bookService.createBook(new Book("Mystery Novel", "Alice Cooper", new BigDecimal("19.99"), fiction, 8));
                bookService.createBook(new Book("Programming Fundamentals", "Bob Wilson", new BigDecimal("52.00"), sciTech, 20));
                bookService.createBook(new Book("Marketing Essentials", "Carol Davis", new BigDecimal("41.25"), business, 7));
                bookService.createBook(new Book("Fitness Guide", "David Lee", new BigDecimal("24.50"), health, 15));
                bookService.createBook(new Book("World History", "Emma Stone", new BigDecimal("33.75"), nonFiction, 9));
                // Add 5 more books to make it 15
                bookService.createBook(new Book("Advanced Java Programming", "Tech Expert", new BigDecimal("65.00"), sciTech, 5));
                bookService.createBook(new Book("Leadership Principles", "Business Guru", new BigDecimal("39.99"), business, 12));
                bookService.createBook(new Book("Cooking Basics", "Chef Master", new BigDecimal("29.50"), health, 18));
                bookService.createBook(new Book("Space Exploration", "Science Writer", new BigDecimal("44.75"), nonFiction, 7));
                bookService.createBook(new Book("Romance in Paris", "Love Author", new BigDecimal("21.99"), fiction, 14));

                System.out.println("15 sample books created successfully");
            } else {
                System.out.println("Genres and books already exist - found " + genreService.getAllGenres().size() + " genres and " + bookService.getAllBooks().size() + " books");
            }

            System.out.println("Manual data initialization completed");

        } catch (Exception e) {
            System.err.println("Error during data initialization: " + e.getMessage());
            e.printStackTrace(); // Print full stack trace for detailed debugging
            // Re-throwing as a RuntimeException to indicate initialization failure
            throw new RuntimeException("Data initialization failed: " + e.getMessage(), e);
        }
    }

    /**
     * Manual method to call initialization if needed.
     * Delegates to the core initializeData() method.
     */
    public void manualInitialize() {
        initializeData();
    }
}
