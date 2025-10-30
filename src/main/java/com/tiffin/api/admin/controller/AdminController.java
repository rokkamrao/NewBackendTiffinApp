package com.tiffin.api.admin.controller;

import com.tiffin.api.admin.dto.*;
import com.tiffin.api.admin.service.AdminService;
import com.tiffin.api.auth.security.RequireRole;
import com.tiffin.api.order.service.OrderService;
import com.tiffin.api.order.dto.OrderDto;
import com.tiffin.api.order.model.Order;
import com.tiffin.api.order.model.OrderStatus;
import com.tiffin.api.order.model.PaymentMethod;
import com.tiffin.api.order.repository.OrderRepository;
import com.tiffin.api.dish.repository.DishRepository;
import com.tiffin.api.user.repository.UserRepository;
import com.tiffin.api.user.model.User;
import com.tiffin.api.subscription.repository.SubscriptionRepository;
import com.tiffin.api.subscription.model.Subscription;
import com.tiffin.api.subscription.model.SubscriptionStatus;
import com.tiffin.api.subscription.model.SubscriptionPlan;
import com.tiffin.api.user.model.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final DishRepository dishRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDto> getDashboardStats() {
        log.info("Fetching admin dashboard stats");
        
        try {
            // Calculate real stats from database
            long totalUsers = userRepository.count();
            
            // Get active subscriptions
            long activeSubscriptions = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
            
            // Calculate today's revenue
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            
            BigDecimal todayRevenue = orderRepository.findTotalRevenueByDateRange(startOfDay, endOfDay);
            if (todayRevenue == null) {
                todayRevenue = BigDecimal.ZERO;
            }
            
            // Count pending orders
            long pendingOrders = orderRepository.countByStatusIn(
                List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PREPARING)
            );
            
            // Count deliveries in progress
            long deliveriesInProgress = orderRepository.countByStatus(OrderStatus.OUT_FOR_DELIVERY);
            
            // Get total dishes
            long totalDishes = dishRepository.count();
            
            // Determine system health based on pending orders
            String systemHealth = "healthy";
            if (pendingOrders > 100) {
                systemHealth = "critical";
            } else if (pendingOrders > 50) {
                systemHealth = "warning";
            }
            
            AdminStatsDto stats = AdminStatsDto.builder()
                .totalUsers(totalUsers)
                .activeSubscriptions(activeSubscriptions)
                .todayRevenue(todayRevenue.doubleValue())
                .pendingOrders(pendingOrders)
                .deliveriesInProgress(deliveriesInProgress)
                .feedbackAlerts(0L) // TODO: Implement feedback system
                .lowStockItems(0L) // TODO: Implement inventory system
                .systemHealth(systemHealth)
                .totalDishes(totalDishes)
                .build();
            
            log.info("Dashboard stats calculated: {} users, {} subscriptions, ₹{} revenue", 
                     totalUsers, activeSubscriptions, todayRevenue);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error fetching admin stats", e);
            
            // Return default stats on error
            AdminStatsDto defaultStats = AdminStatsDto.builder()
                .totalUsers(0L)
                .activeSubscriptions(0L)
                .todayRevenue(0.0)
                .pendingOrders(0L)
                .deliveriesInProgress(0L)
                .feedbackAlerts(0L)
                .lowStockItems(0L)
                .systemHealth("critical")
                .totalDishes(0L)
                .build();
            
            return ResponseEntity.ok(defaultStats);
        }
    }

    @GetMapping("/metrics/realtime")
    public ResponseEntity<Map<String, Object>> getRealtimeMetrics() {
        log.info("Fetching realtime metrics");
        
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Calculate real-time metrics from database
            
            // Active users - users with recent activity (this would need a user activity table)
            // For now, use active delivery users as a proxy
            long activeDeliveryUsers = userRepository.countByRole(Role.DELIVERY_USER);
            
            // Orders per hour - calculate from recent orders
            LocalDateTime hourAgo = LocalDateTime.now().minusHours(1);
            LocalDateTime now = LocalDateTime.now();
            
            // Count orders in the last hour
            // We'll need to add this query method
            List<Order> recentOrders = orderRepository.findByCreatedAtBetween(hourAgo, now);
            int ordersPerHour = recentOrders.size();
            
            // Calculate average delivery time from completed orders
            List<Order> deliveredOrders = orderRepository.findByStatusAndDeliveryTimeIsNotNull(OrderStatus.DELIVERED);
            double averageDeliveryTime = 30.0; // Default fallback
            
            if (!deliveredOrders.isEmpty()) {
                double totalMinutes = deliveredOrders.stream()
                    .filter(order -> order.getDeliveryTime() != null && order.getOrderTime() != null)
                    .mapToDouble(order -> {
                        LocalDateTime orderTime = order.getOrderTime();
                        LocalDateTime deliveryTime = order.getDeliveryTime();
                        return java.time.Duration.between(orderTime, deliveryTime).toMinutes();
                    })
                    .average()
                    .orElse(30.0);
                averageDeliveryTime = Math.round(totalMinutes);
            }
            
            // Today's revenue
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            BigDecimal todayRevenue = orderRepository.findTotalRevenueByDateRange(startOfDay, endOfDay);
            if (todayRevenue == null) {
                todayRevenue = BigDecimal.ZERO;
            }
            
            // Popular dishes - would need a more complex query for actual popularity
            // For now, return mock data
            Object[] popularDishes = {
                Map.of("name", "Dal Tadka", "orders", 45),
                Map.of("name", "Paneer Butter Masala", "orders", 38),
                Map.of("name", "Chicken Biryani", "orders", 32)
            };
            
            metrics.put("activeUsers", Math.max(activeDeliveryUsers * 10, 50)); // Estimate based on delivery users
            metrics.put("ordersPerHour", ordersPerHour);
            metrics.put("averageDeliveryTime", (int) averageDeliveryTime);
            metrics.put("customerSatisfaction", 92); // TODO: Calculate from reviews
            metrics.put("kitchenUtilization", Math.min(85 + (ordersPerHour * 2), 100)); // Estimate based on orders
            metrics.put("deliveryEfficiency", Math.max(90 - (ordersPerHour / 5), 70)); // Inverse relation to load
            metrics.put("orderAccuracy", 96); // TODO: Calculate from order fulfillment
            metrics.put("revenueToday", todayRevenue.doubleValue());
            metrics.put("popularDishes", popularDishes);
            
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            log.error("Error fetching realtime metrics", e);
            
            // Return default metrics on error
            metrics.put("activeUsers", 0);
            metrics.put("ordersPerHour", 0);
            metrics.put("averageDeliveryTime", 0);
            metrics.put("customerSatisfaction", 0);
            metrics.put("kitchenUtilization", 0);
            metrics.put("deliveryEfficiency", 0);
            metrics.put("orderAccuracy", 0);
            metrics.put("revenueToday", 0);
            metrics.put("popularDishes", new Object[]{});
            
            return ResponseEntity.ok(metrics);
        }
    }
    
    @GetMapping("/orders")
    public ResponseEntity<Page<Object>> getAdminOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String hub,
            @RequestParam(required = false) String priority,
            Pageable pageable) {
        
        log.info("Fetching admin orders with filters - status: {}, hub: {}, priority: {}", 
                 status, hub, priority);
        
        try {
            // For now, delegate to the regular order service
            // In a real implementation, this would have admin-specific logic
            Page<OrderDto> orders = orderService.getAllOrders(pageable);
            return ResponseEntity.ok(orders.map(order -> (Object) order));
            
        } catch (Exception e) {
            log.error("Error fetching admin orders", e);
            return ResponseEntity.ok(Page.empty());
        }
    }
    
    @GetMapping("/orders/{id}")
    public ResponseEntity<Object> getOrderById(@PathVariable Long id) {
        log.info("Fetching order details for ID: {}", id);
        
        try {
            // Delegate to order service for detailed order information
            Object order = orderService.getOrderById(id);
            return ResponseEntity.ok(order);
            
        } catch (Exception e) {
            log.error("Error fetching order details for ID: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<Object> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String notes) {
        
        log.info("Admin updating order {} status to {}", id, status);
        
        try {
            // Update order status using the order service
            Object updatedOrder = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(updatedOrder);
            
        } catch (Exception e) {
            log.error("Error updating order status", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to update order status"));
        }
    }
    
    @PostMapping("/sample-data")
    public ResponseEntity<Map<String, String>> createSampleData() {
        log.info("Creating sample data for testing");
        
        try {
            // Create sample users if they don't exist
            createSampleUsers();
            
            // Create sample orders
            createSampleOrders();
            
            // Create sample subscriptions
            createSampleSubscriptions();
            
            return ResponseEntity.ok(Map.of("message", "Sample data created successfully"));
            
        } catch (Exception e) {
            log.error("Error creating sample data", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to create sample data: " + e.getMessage()));
        }
    }
    
    private void createSampleUsers() {
        // Create sample customers if they don't exist
        for (int i = 1; i <= 5; i++) {
            String phone = "912345678" + String.format("%02d", i);
            if (!userRepository.existsByPhone(phone)) {
                User customer = User.builder()
                    .name("Customer " + i)
                    .phone(phone)
                    .email("customer" + i + "@example.com")
                    .password("password") // In real app, this would be encoded
                    .role(Role.CUSTOMER)
                    .isActive(true)
                    .build();
                userRepository.save(customer);
                log.info("Created sample customer: {}", customer.getName());
            }
        }
    }
    
    private void createSampleOrders() {
        // Get sample customers
        List<User> customers = userRepository.findByRole(Role.CUSTOMER, Pageable.unpaged()).getContent();
        if (customers.isEmpty()) {
            log.warn("No customers found to create sample orders");
            return;
        }
        
        // Create sample orders with different statuses
        OrderStatus[] statuses = {OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PREPARING, 
                                 OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED};
        
        for (int i = 0; i < 10; i++) {
            User customer = customers.get(i % customers.size());
            OrderStatus status = statuses[i % statuses.length];
            
            Order order = Order.builder()
                .user(customer)
                .totalAmount(BigDecimal.valueOf(250 + (i * 50)))
                .status(status)
                .orderTime(LocalDateTime.now().minusHours(i))
                .createdAt(LocalDateTime.now().minusHours(i))
                .paymentMethod(PaymentMethod.UPI)
                .paymentStatus("PAID")
                .specialInstructions("Sample order " + (i + 1))
                .build();
            
            orderRepository.save(order);
            log.info("Created sample order {} with status {}", order.getId(), status);
        }
    }
    
    private void createSampleSubscriptions() {
        // Get sample customers
        List<User> customers = userRepository.findByRole(Role.CUSTOMER, Pageable.unpaged()).getContent();
        if (customers.isEmpty()) {
            return;
        }
        
        // Create sample subscriptions
        for (int i = 0; i < 3; i++) {
            User customer = customers.get(i % customers.size());
            
            // Check if customer already has a subscription
            if (subscriptionRepository.findByUserAndStatus(customer, SubscriptionStatus.ACTIVE).isPresent()) {
                continue;
            }
            
            Subscription subscription = Subscription.builder()
                .user(customer)
                .planType(SubscriptionPlan.MONTHLY)
                .startDate(LocalDateTime.now().minusDays(i * 5))
                .endDate(LocalDateTime.now().plusDays(30 - (i * 5)))
                .mealsPerDay(2)
                .mealsRemaining(20 - (i * 2))
                .planPrice(BigDecimal.valueOf(1500 + (i * 500)))
                .isActive(true)
                .status(SubscriptionStatus.ACTIVE)
                .build();
                
            subscriptionRepository.save(subscription);
            log.info("Created sample subscription for customer {}", customer.getName());
        }
    }
    
    // ============= USER MANAGEMENT ENDPOINTS =============
    
    @RequireRole(Role.ADMIN)
    @PostMapping("/delivery-users")
    public ResponseEntity<UserManagementDto> createDeliveryUser(@Valid @RequestBody CreateDeliveryUserRequest request) {
        log.info("🚚 Admin creating delivery user: {}", request.getPhone());
        try {
            UserManagementDto deliveryUser = adminService.createDeliveryUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(deliveryUser);
        } catch (Exception e) {
            log.error("❌ Error creating delivery user", e);
            throw e;
        }
    }
    
    @RequireRole(Role.ADMIN)
    @PostMapping("/admin-users")
    public ResponseEntity<UserManagementDto> createAdminUser(@Valid @RequestBody CreateAdminUserRequest request) {
        log.info("👑 Admin creating admin user: {}", request.getPhone());
        try {
            UserManagementDto adminUser = adminService.createAdminUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(adminUser);
        } catch (Exception e) {
            log.error("❌ Error creating admin user", e);
            throw e;
        }
    }
    
    @RequireRole(Role.ADMIN)
    @GetMapping("/delivery-users")
    public ResponseEntity<Page<UserManagementDto>> getAllDeliveryUsers(Pageable pageable) {
        log.info("📋 Fetching all delivery users");
        Page<UserManagementDto> deliveryUsers = adminService.getAllDeliveryUsers(pageable);
        return ResponseEntity.ok(deliveryUsers);
    }
    
    @RequireRole(Role.ADMIN)
    @GetMapping("/delivery-users/active")
    public ResponseEntity<List<UserManagementDto>> getActiveDeliveryUsers() {
        log.info("📋 Fetching active delivery users");
        List<UserManagementDto> activeDeliveryUsers = adminService.getActiveDeliveryUsers();
        return ResponseEntity.ok(activeDeliveryUsers);
    }
    
    @RequireRole(Role.ADMIN)
    @GetMapping("/admin-users")
    public ResponseEntity<Page<UserManagementDto>> getAllAdminUsers(Pageable pageable) {
        log.info("📋 Fetching all admin users");
        Page<UserManagementDto> adminUsers = adminService.getAllAdminUsers(pageable);
        return ResponseEntity.ok(adminUsers);
    }
    
    @RequireRole(Role.ADMIN)
    @GetMapping("/customers")
    public ResponseEntity<Page<UserManagementDto>> getAllCustomers(Pageable pageable) {
        log.info("📋 Fetching all customers");
        Page<UserManagementDto> customers = adminService.getAllCustomers(pageable);
        return ResponseEntity.ok(customers);
    }
    
    @RequireRole(Role.ADMIN)
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserManagementDto> getUserById(@PathVariable Long userId) {
        log.info("🔍 Fetching user by ID: {}", userId);
        UserManagementDto user = adminService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
    
    @RequireRole(Role.ADMIN)
    @PatchMapping("/users/{userId}/toggle-activation")
    public ResponseEntity<UserManagementDto> toggleUserActivation(@PathVariable Long userId) {
        log.info("🔄 Toggling activation for user ID: {}", userId);
        UserManagementDto user = adminService.toggleUserActivation(userId);
        return ResponseEntity.ok(user);
    }
    
    @RequireRole(Role.ADMIN)
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
        log.info("🗑️ Deleting user ID: {}", userId);
        adminService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
}