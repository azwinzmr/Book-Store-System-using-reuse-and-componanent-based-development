package controllers;

import entities.Book;
import entities.Genre;
import entities.Order;
import entities.User;
import session.BookService;
import session.GenreService;
import session.OrderService;
import session.UserService;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@ViewScoped
public class AdminController implements Serializable {
    
    @EJB
    private BookService bookService;
    
    @EJB
    private GenreService genreService;
    
    @EJB
    private OrderService orderService;
    
    @EJB
    private UserService userService;
    
    @Inject
    private LoginController loginController;
    
    private List<Book> books;
    private List<Genre> genres;
    private List<Order> orders;
    private List<User> users;
    private Book selectedBook;
    private Genre newGenre;
    
    // Dashboard statistics
    private long totalBooks;
    private long totalUsers;
    private long totalOrders;
    private BigDecimal totalRevenue;
    private long adminUsersCount;
    private long regularUsersCount;
    
    @PostConstruct
    public void init() {
        newGenre = new Genre();
        // Basic security check: only load dashboard data if the user is an admin.
        // For production, ensure more robust container-managed security on admin pages.
        if (loginController != null && loginController.isAdmin()) {
            loadDashboardData();
        } else {
            // Optionally, redirect to an error page or home if not authorized
            System.err.println("AdminController init: Unauthorized access attempt.");
            // Example: FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "/index?faces-redirect=true");
        }
    }
    
    /**
     * Loads all data and calculates statistics for the admin dashboard.
     */
    public void loadDashboardData() {
        try {
            loadBooks();
            loadGenres();
            loadAllOrders(); // Using loadAllOrders for consistency
            loadUsers();
            calculateStatistics();
            System.out.println("Dashboard data loaded - Books: " + totalBooks + ", Users: " + totalUsers + ", Orders: " + totalOrders);
        } catch (Exception e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to load dashboard data: " + e.getMessage()));
        }
    }
    
    /**
     * Loads all books from the BookService.
     */
    public void loadBooks() {
        try {
            books = bookService.getAllBooks();
            totalBooks = books != null ? books.size() : 0;
            // debugBooks(); // Uncomment for debugging if needed
            System.out.println("AdminController: Loaded " + totalBooks + " books");
        } catch (Exception e) {
            System.err.println("Error loading books in AdminController: " + e.getMessage());
            e.printStackTrace();
            books = new ArrayList<>();
            totalBooks = 0;
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to load books: " + e.getMessage()));
        }
    }
    
    // Debug method for books - keep commented out or remove for production
    /*
    public void debugBooks() {
        System.out.println("=== DEBUG BOOKS ===");
        System.out.println("AdminController books list size: " + (books != null ? books.size() : "null"));
        if (books != null) {
            for (Book book : books) {
                System.out.println("Book: " + book.getTitle() + " by " + book.getAuthor() + ", Stock: " + book.getStockQuantity());
            }
        }
        System.out.println("==================");
    }
    */
    
    /**
     * Loads all genres from the GenreService.
     */
    public void loadGenres() {
        try {
            genres = genreService.getAllGenres();
            System.out.println("AdminController: Loaded " + (genres != null ? genres.size() : 0) + " genres");
        } catch (Exception e) {
            System.err.println("Error loading genres in AdminController: " + e.getMessage());
            e.printStackTrace();
            genres = new ArrayList<>();
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to load genres: " + e.getMessage()));
        }
    }
    
    /**
     * Loads all orders from the OrderService (for Admin view).
     */
    public void loadAllOrders() {
        try {
            orders = orderService.getAllOrders();
            totalOrders = orders != null ? orders.size() : 0;
            System.out.println("AdminController: Loaded all " + totalOrders + " orders");
        } catch (Exception e) {
            System.err.println("Error loading all orders in AdminController: " + e.getMessage());
            e.printStackTrace();
            orders = new ArrayList<>();
            totalOrders = 0;
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to load orders: " + e.getMessage()));
        }
    }
    
    /**
     * Loads all users from the UserService.
     */
    public void loadUsers() {
        try {
            users = userService.getAllUsers();
            totalUsers = users != null ? users.size() : 0;
            calculateUserStatistics(); // Calculate user role counts
            // debugUsers(); // Uncomment for debugging if needed
            System.out.println("AdminController: Loaded " + totalUsers + " users");
        } catch (Exception e) {
            System.err.println("Error loading users in AdminController: " + e.getMessage());
            e.printStackTrace();
            users = new ArrayList<>();
            totalUsers = 0;
            adminUsersCount = 0;
            regularUsersCount = 0;
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to load users: " + e.getMessage()));
        }
    }
    
    /**
     * Calculates the count of admin and regular users.
     */
    private void calculateUserStatistics() {
        if (users != null) {
            adminUsersCount = users.stream()
                .filter(user -> "ADMIN".equals(user.getRole()))
                .count();
            regularUsersCount = users.stream()
                .filter(user -> "USER".equals(user.getRole()))
                .count();
        } else {
            adminUsersCount = 0;
            regularUsersCount = 0;
        }
    }
    
    // Debug method for users - keep commented out or remove for production
    /*
    public void debugUsers() {
        try {
            List<User> allUsers = userService.getAllUsers();
            System.out.println("=== ALL USERS IN DATABASE ===");
            System.out.println("Total users: " + allUsers.size());
            for (User user : allUsers) {
                // WARNING: Printing passwords (even hashed ones) is generally not recommended in logs.
                // This is for development debugging only.
                System.out.println("User: " + user.getUsername() + ", Hashed Password: " + user.getPassword() + ", Role: " + user.getRole());
            }
            System.out.println("==============================");
        } catch (Exception e) {
            System.err.println("Error debugging users: " + e.getMessage());
            e.printStackTrace();
        }
    }
    */
    
    /**
     * Manually creates an admin user if one does not already exist.
     * This is a utility method for initial setup or recovery.
     */
    public void createAdminManually() {
        try {
            System.out.println("Manually creating admin user...");
            if (!userService.usernameExists("admin")) {
                // The password "admin123" will be hashed by UserService before persistence.
                User admin = new User("admin", "admin123", "admin@bookstore.com", "ADMIN");
                userService.createUser(admin);
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Admin user created"));
                System.out.println("Admin user created manually");
            } else {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Admin user already exists"));
                System.out.println("Admin user already exists");
            }
            loadUsers(); // Refresh users list after potential creation
        } catch (Exception e) {
            System.err.println("Error creating admin manually: " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to create admin: " + e.getMessage()));
        }
    }
    
    /**
     * Calculates total revenue from completed orders.
     */
    private void calculateStatistics() {
        totalRevenue = BigDecimal.ZERO;
        if (orders != null) {
            for (Order order : orders) {
                if ("COMPLETED".equals(order.getStatus())) {
                    totalRevenue = totalRevenue.add(order.getTotalAmount());
                }
            }
        }
    }
    
    /**
     * Updates the price of a specific book.
     *
     * @param book The book entity to update.
     * @param newPrice The new price for the book.
     */
    public void updateBookPrice(Book book, BigDecimal newPrice) {
        try {
            if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Price must be greater than 0"));
                return;
            }
            
            bookService.updateBookPrice(book.getId(), newPrice);
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", 
                "Price updated for '" + book.getTitle() + "'"));
            
            loadBooks(); // Refresh book list
        } catch (Exception e) {
            System.err.println("Error updating book price: " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to update price: " + e.getMessage()));
        }
    }
    
    /**
     * Updates the stock quantity of a specific book.
     *
     * @param book The book entity to update.
     * @param newStock The new stock quantity for the book.
     */
    public void updateBookStock(Book book, Integer newStock) {
        try {
            if (newStock == null || newStock < 0) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Stock must be 0 or greater"));
                return;
            }
            
            bookService.updateBookStock(book.getId(), newStock);
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", 
                "Stock updated for '" + book.getTitle() + "'"));
            
            loadBooks(); // Refresh book list
        } catch (Exception e) {
            System.err.println("Error updating book stock: " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to update stock: " + e.getMessage()));
        }
    }
    
    /**
     * Deletes a book from the database.
     *
     * @param book The book entity to delete.
     */
    public void deleteBook(Book book) {
        try {
            bookService.deleteBook(book.getId());
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", 
                "Book '" + book.getTitle() + "' deleted successfully"));
            
            loadBooks(); // Refresh book list
        } catch (Exception e) {
            System.err.println("Error deleting book: " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to delete book: " + e.getMessage()));
        }
    }
    
    /**
     * Adds a new genre to the database.
     */
    public void addGenre() {
        try {
            if (newGenre.getGenreName() == null || newGenre.getGenreName().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Genre name is required"));
                return;
            }
            
            if (genreService.genreExists(newGenre.getGenreName())) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Genre already exists"));
                return;
            }
            
            genreService.createGenre(newGenre);
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", 
                "Genre '" + newGenre.getGenreName() + "' added successfully"));
            
            newGenre = new Genre(); // Reset for new input
            loadGenres(); // Refresh genre list
        } catch (Exception e) {
            System.err.println("Error adding genre: " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to add genre: " + e.getMessage()));
        }
    }
    
    /**
     * Updates the status of a specific order.
     *
     * @param order The order entity to update.
     * @param newStatus The new status string for the order.
     */
    public void updateOrderStatus(Order order, String newStatus) {
        try {
            orderService.updateOrderStatus(order.getId(), newStatus);
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", 
                "Order status updated to: " + newStatus));
            
            loadAllOrders(); // Refresh order list
        } catch (Exception e) {
            System.err.println("Error updating order status: " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to update order status: " + e.getMessage()));
        }
    }
    
    /**
     * Deletes a user from the database.
     * Prevents deletion of admin users.
     *
     * @param user The user entity to delete.
     */
    public void deleteUser(User user) {
        try {
            if (user.isAdmin()) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Cannot delete admin user"));
                return;
            }
            
            userService.deleteUser(user.getId());
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", 
                "User '" + user.getUsername() + "' deleted successfully"));
            
            loadUsers(); // Refresh user list
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to delete user: " + e.getMessage()));
        }
    }
    
    // Navigation method
    public String goToDashboard() {
        return "dashboard?faces-redirect=true";
    }
    
    // User utility methods for JSF EL
    public long getAdminUsersCount() {
        return adminUsersCount;
    }
    
    public long getRegularUsersCount() {
        return regularUsersCount;
    }
    
    public List<User> getAdminUsers() {
        if (users == null) return new ArrayList<>();
        return users.stream()
            .filter(user -> "ADMIN".equals(user.getRole()))
            .collect(Collectors.toList());
    }
    
    public List<User> getRegularUsers() {
        if (users == null) return new ArrayList<>();
        return users.stream()
            .filter(user -> "USER".equals(user.getRole()))
            .collect(Collectors.toList());
    }
    
    public boolean isUsersEmpty() {
        return users == null || users.isEmpty();
    }
    
    public String getFormattedUserCount() {
        return "Total Users: " + totalUsers + 
               " (Admins: " + adminUsersCount + 
               ", Regular: " + regularUsersCount + ")";
    }
    
    public boolean hasUsers() {
        return users != null && !users.isEmpty();
    }
    
    public boolean hasBooks() {
        return books != null && !books.isEmpty();
    }
    
    public boolean hasOrders() {
        return orders != null && !orders.isEmpty();
    }
    
    public boolean hasGenres() {
        return genres != null && !genres.isEmpty();
    }
    
    public boolean getHasUsers() {
        return users != null && !users.isEmpty();
    }
    
    public boolean isHasUsers() {
        return getHasUsers();
    }
    
    /**
     * Gets a list of pending orders.
     * @return List of pending Order entities.
     */
    public List<Order> getPendingOrders() {
        if (orders == null) return new ArrayList<>();
        return orders.stream()
            .filter(order -> "PENDING".equals(order.getStatus()))
            .collect(Collectors.toList());
    }
    
    /**
     * Gets a list of completed orders.
     * @return List of completed Order entities.
     */
    public List<Order> getCompletedOrders() {
        if (orders == null) return new ArrayList<>();
        return orders.stream()
            .filter(order -> "COMPLETED".equals(order.getStatus()))
            .collect(Collectors.toList());
    }
    
    /**
     * Gets the count of pending orders.
     * @return The number of pending orders.
     */
    public long getPendingOrdersCount() {
        if (orders == null) return 0;
        return orders.stream()
            .filter(order -> "PENDING".equals(order.getStatus()))
            .count();
    }
    
    /**
     * Gets the count of completed orders.
     * @return The number of completed orders.
     */
    public long getCompletedOrdersCount() {
        if (orders == null) return 0;
        return orders.stream()
            .filter(order -> "COMPLETED".equals(order.getStatus()))
            .count();
    }
    
    // Getters and Setters
    public List<Book> getBooks() {
        return books;
    }
    
    public void setBooks(List<Book> books) {
        this.books = books;
    }
    
    public List<Genre> getGenres() {
        return genres;
    }
    
    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }
    
    public List<Order> getOrders() {
        return orders;
    }
    
    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
    
    public List<User> getUsers() {
        return users;
    }
    
    public void setUsers(List<User> users) {
        this.users = users;
    }
    
    public Book getSelectedBook() {
        return selectedBook;
    }
    
    public void setSelectedBook(Book selectedBook) {
        this.selectedBook = selectedBook;
    }
    
    public Genre getNewGenre() {
        return newGenre;
    }
    
    public void setNewGenre(Genre newGenre) {
        this.newGenre = newGenre;
    }
    
    public long getTotalBooks() {
        return totalBooks;
    }
    
    public long getTotalUsers() {
        return totalUsers;
    }
    
    public long getTotalOrders() {
        return totalOrders;
    }
    
    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
}
