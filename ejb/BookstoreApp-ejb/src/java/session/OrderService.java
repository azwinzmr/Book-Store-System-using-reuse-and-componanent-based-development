/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package session;

import entities.Book;
import entities.Order;
import entities.OrderItem;
import entities.User;
import java.math.BigDecimal;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

@Stateless
public class OrderService {
    
    @PersistenceContext(unitName = "BookstoreApp-ejbPU")
    private EntityManager em;
    
    @EJB
    private BookService bookService;
    
    // Create new order
    public Order createOrder(Order order) {
        try {
            em.persist(order);
            em.flush();
            return order;
        } catch (Exception e) {
            throw new RuntimeException("Error creating order: " + e.getMessage());
        }
    }
    
    // Find order by ID
    public Order findById(Long id) {
        return em.find(Order.class, id);
    }
    
    // Get all orders
    public List<Order> getAllOrders() {
        TypedQuery<Order> query = em.createNamedQuery("Order.findAll", Order.class);
        return query.getResultList();
    }
    
    // Get orders by user
    public List<Order> getOrdersByUser(Long userId) {
        TypedQuery<Order> query = em.createNamedQuery("Order.findByUser", Order.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }
    
    // Get orders by status
    public List<Order> getOrdersByStatus(String status) {
        TypedQuery<Order> query = em.createNamedQuery("Order.findByStatus", Order.class);
        query.setParameter("status", status);
        return query.getResultList();
    }
    
    // Update order
    public Order updateOrder(Order order) {
        try {
            return em.merge(order);
        } catch (Exception e) {
            throw new RuntimeException("Error updating order: " + e.getMessage());
        }
    }
    
    // Update order status
    public Order updateOrderStatus(Long orderId, String status) {
        try {
            Order order = findById(orderId);
            if (order != null) {
                order.setStatus(status);
                return em.merge(order);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error updating order status: " + e.getMessage());
        }
    }
    
    // Delete order
    public void deleteOrder(Long orderId) {
        try {
            Order order = em.find(Order.class, orderId);
            if (order != null) {
                em.remove(order);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error deleting order: " + e.getMessage());
        }
    }
    
    // Process order (complete purchase)
    public Order processOrder(User user, List<OrderItem> items) {
        try {
            // Create new order
            Order order = new Order(user);
            
            // Add items and validate stock
            BigDecimal total = BigDecimal.ZERO;
            for (OrderItem item : items) {
                // Check if book is available
                if (!bookService.isBookAvailable(item.getBook().getId(), item.getQuantity())) {
                    throw new RuntimeException("Book '" + item.getBook().getTitle() + "' is not available in requested quantity");
                }
                
                // Set the order reference
                item.setOrder(order);
                
                // Calculate item price
                BigDecimal itemTotal = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
                total = total.add(itemTotal);
                
                // Decrease stock
                bookService.decreaseStock(item.getBook().getId(), item.getQuantity());
            }
            
            // Set order details
            order.setOrderItems(items);
            order.setTotalAmount(total);
            order.setStatus("COMPLETED");
            
            // Persist order (cascade will save order items)
            em.persist(order);
            em.flush();
            
            return order;
        } catch (Exception e) {
            throw new RuntimeException("Error processing order: " + e.getMessage());
        }
    }
    
    // Create order item
    public OrderItem createOrderItem(Book book, int quantity) {
        OrderItem item = new OrderItem();
        item.setBook(book);
        item.setQuantity(quantity);
        item.setPrice(book.getPrice());
        return item;
    }
    
    // Get user's order history
    public List<Order> getUserOrderHistory(Long userId) {
        return getOrdersByUser(userId);
    }
}