/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

@Entity
@Table(name = "orders")
@NamedQueries({
    @NamedQuery(name = "Order.findAll", query = "SELECT o FROM Order o ORDER BY o.orderDate DESC"),
    @NamedQuery(name = "Order.findByUser", query = "SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.orderDate DESC"),
    @NamedQuery(name = "Order.findByStatus", query = "SELECT o FROM Order o WHERE o.status = :status ORDER BY o.orderDate DESC")
})
public class Order implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "order_date")
    private Date orderDate;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(length = 20)
    private String status = "PENDING";
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> orderItems;
    
    // Constructors
    public Order() {
        this.orderDate = new Date();
        this.orderItems = new ArrayList<>();
        this.totalAmount = BigDecimal.ZERO;
    }
    
    public Order(User user) {
        this();
        this.user = user;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Date getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }
    
    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
    
    // Utility methods
    public void addOrderItem(OrderItem item) {
        if (orderItems == null) {
            orderItems = new ArrayList<>();
        }
        orderItems.add(item);
        item.setOrder(this);
        calculateTotal();
    }
    
    public void removeOrderItem(OrderItem item) {
        if (orderItems != null) {
            orderItems.remove(item);
            calculateTotal();
        }
    }
    
    public void calculateTotal() {
        totalAmount = BigDecimal.ZERO;
        if (orderItems != null) {
            for (OrderItem item : orderItems) {
                totalAmount = totalAmount.add(item.getSubtotal());
            }
        }
    }
    
    public int getTotalItems() {
        if (orderItems == null) {
            return 0;
        }
        return orderItems.stream().mapToInt(OrderItem::getQuantity).sum();
    }
    
    @Override
    public String toString() {
        return "Order{" + "id=" + id + ", user=" + user.getUsername() + ", totalAmount=" + totalAmount + ", status=" + status + '}';
    }
}
