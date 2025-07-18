package controllers;

import entities.Book;
import entities.Order;
import entities.OrderItem;
import entities.User;
import session.BookService;
import session.OrderService;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@SessionScoped
public class OrderController implements Serializable {
    
    @EJB
    private OrderService orderService;
    
    @EJB
    private BookService bookService;
    
    @Inject
    private LoginController loginController;
    
    private Map<Long, Integer> cart; // bookId -> quantity
    private List<Order> userOrders;
    private List<Order> allOrders; // For admin view
    private BigDecimal cartTotal;
    
    @PostConstruct
    public void init() {
        cart = new HashMap<>();
        cartTotal = BigDecimal.ZERO;
        // userOrders and allOrders are loaded on demand by specific methods
    }
    
    /**
     * Adds a specified book to the shopping cart.
     * Performs stock availability checks.
     *
     * @param book The book to add to the cart.
     */
    public void addToCart(Book book) {
        try {
            if (book == null) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Book not found or invalid."));
                System.err.println("addToCart: Attempted to add null book.");
                return;
            }

            Long bookId = book.getId();
            int currentQuantityInCart = cart.getOrDefault(bookId, 0);
            int requestedQuantity = currentQuantityInCart + 1;
            
            // Re-check book availability from service in case stock changed
            Book freshBook = bookService.findById(bookId);
            if (freshBook == null || !bookService.isBookAvailable(bookId, requestedQuantity)) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                    "Cannot add more. Only " + (freshBook != null ? freshBook.getStockQuantity() : "0") + " items available for '" + book.getTitle() + "'"));
                System.err.println("addToCart: Insufficient stock for bookId " + bookId + ". Requested: " + requestedQuantity + ", Available: " + (freshBook != null ? freshBook.getStockQuantity() : "0"));
                return;
            }
            
            cart.put(bookId, requestedQuantity);
            calculateCartTotal();
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", 
                book.getTitle() + " added to cart. Quantity: " + requestedQuantity));
            System.out.println("addToCart: Added book " + book.getTitle() + " (ID: " + bookId + "), new quantity in cart: " + requestedQuantity);
                
        } catch (Exception e) {
            System.err.println("Error adding book to cart: " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to add to cart: " + e.getMessage()));
        }
    }
    
    /**
     * Removes a book from the shopping cart.
     *
     * @param bookId The ID of the book to remove.
     */
    public void removeFromCart(Long bookId) {
        if (bookId == null || !cart.containsKey(bookId)) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Item not in cart."));
            System.out.println("removeFromCart: Attempted to remove non-existent item with ID " + bookId);
            return;
        }

        cart.remove(bookId);
        calculateCartTotal();
        
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Item removed from cart"));
        System.out.println("removeFromCart: Removed item with ID " + bookId);
    }
    
    /**
     * Updates the quantity of a specific book in the cart.
     *
     * @param bookId The ID of the book.
     * @param quantity The new quantity for the book in the cart.
     */
    public void updateCartQuantity(Long bookId, int quantity) {
        try {
            if (bookId == null) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Invalid book ID."));
                System.err.println("updateCartQuantity: Invalid book ID provided (null).");
                return;
            }

            if (quantity <= 0) {
                removeFromCart(bookId); // If quantity is 0 or less, remove item
                System.out.println("updateCartQuantity: Quantity set to 0 or less for book ID " + bookId + ", removed from cart.");
                return;
            }
            
            Book book = bookService.findById(bookId);
            if (book != null) {
                if (quantity <= book.getStockQuantity()) {
                    cart.put(bookId, quantity);
                    calculateCartTotal();
                    FacesContext.getCurrentInstance().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", 
                        "Quantity for '" + book.getTitle() + "' updated to " + quantity));
                    System.out.println("updateCartQuantity: Book ID " + bookId + " quantity updated to " + quantity);
                } else {
                    FacesContext.getCurrentInstance().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                        "Quantity exceeds available stock for '" + book.getTitle() + "'. Max available: " + book.getStockQuantity()));
                    System.err.println("updateCartQuantity: Quantity " + quantity + " exceeds stock " + book.getStockQuantity() + " for book ID " + bookId);
                }
            } else {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Book not found."));
                System.err.println("updateCartQuantity: Book not found for ID " + bookId);
            }
        } catch (Exception e) {
            System.err.println("Error updating cart quantity: " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to update quantity: " + e.getMessage()));
        }
    }
    
    /**
     * Recalculates the total amount of all items in the cart.
     * Iterates through cart items, fetches book details, and sums up subtotals.
     */
    private void calculateCartTotal() {
        cartTotal = BigDecimal.ZERO;
        if (cart.isEmpty()) {
            System.out.println("calculateCartTotal: Cart is empty, total is ZERO.");
            return;
        }

        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            Book book = bookService.findById(entry.getKey());
            if (book != null) {
                BigDecimal itemTotal = book.getPrice().multiply(new BigDecimal(entry.getValue()));
                cartTotal = cartTotal.add(itemTotal);
            } else {
                System.err.println("calculateCartTotal: Book with ID " + entry.getKey() + " not found while calculating total. Skipping this item.");
                // Optionally remove this item from cart if the book no longer exists
                // cart.remove(entry.getKey());
            }
        }
        System.out.println("calculateCartTotal: New cart total is " + cartTotal);
    }
    
    /**
     * Gets a list of CartItem objects for display in the UI.
     * Each CartItem contains book details, quantity, and subtotal.
     *
     * @return A list of CartItem objects.
     */
    public List<CartItem> getCartItems() {
        List<CartItem> cartItems = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            Book book = bookService.findById(entry.getKey());
            if (book != null) {
                CartItem item = new CartItem();
                item.setBook(book);
                item.setQuantity(entry.getValue());
                item.setSubtotal(book.getPrice().multiply(new BigDecimal(entry.getValue())));
                cartItems.add(item);
            } else {
                System.err.println("getCartItems: Book with ID " + entry.getKey() + " not found. Skipping item from display.");
            }
        }
        return cartItems;
    }
    
    /**
     * Processes the current shopping cart as a new order.
     * Requires the user to be logged in and the cart to be non-empty.
     *
     * @return Navigation outcome string.
     */
    public String checkout() {
        try {
            if (!loginController.isLoggedIn()) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please login to checkout"));
                System.err.println("Checkout failed: User not logged in.");
                return "login?faces-redirect=true";
            }
            
            if (cart.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Cart is empty. Please add items before checking out."));
                System.err.println("Checkout failed: Cart is empty.");
                return null;
            }
            
            User currentUser = loginController.getCurrentUser();
            if (currentUser == null) {
                 FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Current user session invalid. Please log in again."));
                 System.err.println("Checkout failed: Current user is null despite isLoggedIn() being true. Session issue?");
                 return "login?faces-redirect=true";
            }

            List<OrderItem> orderItems = new ArrayList<>();
            // Before processing, re-check stock one last time to prevent race conditions during checkout
            for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
                Long bookId = entry.getKey();
                int quantityInCart = entry.getValue();
                Book book = bookService.findById(bookId);

                if (book == null || !bookService.isBookAvailable(bookId, quantityInCart)) {
                    FacesContext.getCurrentInstance().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                        "Stock changed for '" + (book != null ? book.getTitle() : "an item") + "'. Please review your cart."));
                    System.err.println("Checkout failed due to stock change for book ID: " + bookId);
                    return null; // Abort checkout
                }
                // Create OrderItem using the current price from the book
                OrderItem item = new OrderItem();
                item.setBook(book);
                item.setQuantity(quantityInCart);
                item.setPrice(book.getPrice()); // Capture price at time of order
                orderItems.add(item);
            }
            
            // Process the order via OrderService
            Order order = orderService.processOrder(currentUser, orderItems);
            
            // Clear cart upon successful order
            cart.clear();
            cartTotal = BigDecimal.ZERO;
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", 
                "Order placed successfully! Order ID: " + order.getId()));
            System.out.println("Order placed successfully! Order ID: " + order.getId() + " by user " + currentUser.getUsername());
            
            return "/user/orders.xhtml?faces-redirect=true"; // MODIFIED LINE: Redirect to user's order history
            
        } catch (Exception e) {
            System.err.println("Checkout failed: " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Checkout failed: " + e.getMessage()));
            return null;
        }
    }
    
    /**
     * Loads the current logged-in user's order history.
     */
    public void loadUserOrders() {
        try {
            if (loginController.isLoggedIn() && loginController.getCurrentUser() != null) {
                User currentUser = loginController.getCurrentUser();
                userOrders = orderService.getUserOrderHistory(currentUser.getId());
                System.out.println("Loaded " + (userOrders != null ? userOrders.size() : 0) + " orders for user " + currentUser.getUsername());
            } else {
                userOrders = new ArrayList<>(); // Clear orders if not logged in
                System.out.println("loadUserOrders: User not logged in, clearing user orders list.");
            }
        } catch (Exception e) {
            System.err.println("Error loading user orders: " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to load your orders: " + e.getMessage()));
            userOrders = new ArrayList<>(); // Initialize to empty list on error
        }
    }
    
    /**
     * Loads all orders (typically for an Admin function).
     */
    public void loadAllOrders() {
        try {
            allOrders = orderService.getAllOrders();
            System.out.println("Loaded " + (allOrders != null ? allOrders.size() : 0) + " all orders for admin view.");
        } catch (Exception e) {
            System.err.println("Error loading all orders (Admin): " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to load all orders: " + e.getMessage()));
            allOrders = new ArrayList<>(); // Initialize to empty list on error
        }
    }
    
    /**
     * Clears all items from the shopping cart.
     */
    public void clearCart() {
        cart.clear();
        cartTotal = BigDecimal.ZERO;
        
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Cart cleared"));
        System.out.println("Cart cleared.");
    }
    
    /**
     * Gets the total number of items in the cart (sum of quantities).
     * @return The total number of items.
     */
    public int getCartSize() {
        return cart.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    /**
     * Checks if the shopping cart is empty.
     * @return true if the cart is empty, false otherwise.
     */
    public boolean isCartEmpty() {
        return cart.isEmpty();
    }
    
    /**
     * Gets the quantity of a specific book in the cart.
     * @param bookId The ID of the book.
     * @return The quantity of the book in the cart, or 0 if not present.
     */
    public int getQuantityInCart(Long bookId) {
        return cart.getOrDefault(bookId, 0);
    }
    
    /**
     * Inner class to represent an item in the shopping cart for UI display.
     */
    public static class CartItem implements Serializable {
        private Book book;
        private int quantity;
        private BigDecimal subtotal;
        
        // Getters and setters
        public Book getBook() { return book; }
        public void setBook(Book book) { this.book = book; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    }
    
    // Getters and Setters
    public Map<Long, Integer> getCart() {
        return cart;
    }
    
    public void setCart(Map<Long, Integer> cart) {
        this.cart = cart;
    }
    
    public List<Order> getUserOrders() {
        return userOrders;
    }
    
    public void setUserOrders(List<Order> userOrders) {
        this.userOrders = userOrders;
    }
    
    public List<Order> getAllOrders() {
        return allOrders;
    }
    
    public void setAllOrders(List<Order> allOrders) {
        this.allOrders = allOrders;
    }
    
    public BigDecimal getCartTotal() {
        return cartTotal;
    }
    
    public void setCartTotal(BigDecimal cartTotal) {
        this.cartTotal = cartTotal;
    }
}
