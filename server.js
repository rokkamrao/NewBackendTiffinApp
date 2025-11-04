const express = require('express');
const cors = require('cors');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { v4: uuidv4 } = require('uuid');

const app = express();
const port = 8080;

// Middleware
app.use(cors());
app.use(express.json());

// Request logging middleware
app.use((req, res, next) => {
  console.log(`🌐 ${req.method} ${req.path} - ${new Date().toLocaleTimeString()}`);
  next();
});

// JWT Secret
const JWT_SECRET = 'your-secret-key-change-in-production';

// In-memory data store (replace with database in production)
let users = [
  {
    id: 1,
    email: 'user@test.com',
    name: 'Test User',
    phone: '+1234567890',
    password: bcrypt.hashSync('password', 10),
    role: 'CUSTOMER',
    isActive: true
  },
  {
    id: 2,
    email: 'admin@tiffin.com',
    name: 'Admin User',
    phone: '+1234567891',
    password: bcrypt.hashSync('password', 10),
    role: 'ADMIN',
    isActive: true
  },
  {
    id: 3,
    email: 'john@delivery.com',
    name: 'John Delivery',
    phone: '+1234567892',
    password: bcrypt.hashSync('password', 10),
    role: 'DELIVERY_PARTNER',
    isActive: true
  }
];

let dishes = [
  {
    id: 1,
    name: 'Chicken Biryani',
    description: 'Aromatic basmati rice with tender chicken pieces',
    price: 15.99,
    category: 'MAIN_COURSE',
    isVegetarian: false,
    isAvailable: true,
    imageUrl: '/assets/images/dishes/chicken-biryani.jpg',
    preparationTime: 25,
    spiceLevel: 'MEDIUM'
  },
  {
    id: 2,
    name: 'Vegetable Curry',
    description: 'Mixed vegetables in rich curry sauce',
    price: 12.99,
    category: 'MAIN_COURSE',
    isVegetarian: true,
    isAvailable: true,
    imageUrl: '/assets/images/dishes/veg-curry.jpg',
    preparationTime: 20,
    spiceLevel: 'MILD'
  },
  {
    id: 3,
    name: 'Dal Tadka',
    description: 'Yellow lentils with tempered spices',
    price: 8.99,
    category: 'MAIN_COURSE',
    isVegetarian: true,
    isAvailable: true,
    imageUrl: '/assets/images/dishes/dal-tadka.jpg',
    preparationTime: 15,
    spiceLevel: 'MILD'
  },
  {
    id: 4,
    name: 'Masala Dosa',
    description: 'Crispy rice crepe with spiced potato filling',
    price: 10.99,
    category: 'BREAKFAST',
    isVegetarian: true,
    isAvailable: true,
    imageUrl: '/assets/images/dishes/masala-dosa.jpg',
    preparationTime: 15,
    spiceLevel: 'MEDIUM'
  }
];

let orders = [];
let otpStore = new Map();
let deliveryPartners = [];
let notifications = [];

// Helper function to generate JWT token
const generateToken = (user) => {
  return jwt.sign(
    { 
      id: user.id, 
      email: user.email, 
      role: user.role,
      name: user.name,
      phone: user.phone 
    },
    JWT_SECRET,
    { expiresIn: '24h' }
  );
};

// Auth middleware
const authenticateToken = (req, res, next) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) {
    return res.status(401).json({ error: 'Access token required' });
  }

  jwt.verify(token, JWT_SECRET, (err, user) => {
    if (err) {
      return res.status(403).json({ error: 'Invalid token' });
    }
    req.user = user;
    next();
  });
};

// Role-based authorization middleware
const requireRole = (...roles) => {
  return (req, res, next) => {
    if (!req.user || !roles.includes(req.user.role)) {
      return res.status(403).json({ error: 'Insufficient permissions' });
    }
    next();
  };
};

// ==================== AUTH ENDPOINTS ====================
app.post('/api/auth/send-otp', (req, res) => {
  try {
    const { phone } = req.body;
    console.log(`📱 OTP requested for phone: ${phone}`);
    
    if (!phone) {
      return res.status(400).json({ success: false, message: 'Phone number is required' });
    }

    // Generate OTP (for demo, using static OTP)
    const otp = '123456';
    
    // Store OTP (expires in 5 minutes)
    otpStore.set(phone, {
      otp,
      expiresAt: Date.now() + 5 * 60 * 1000
    });
    
    console.log(`✅ OTP ${otp} sent to ${phone}`);
    res.json({ 
      success: true, 
      message: 'OTP sent successfully',
      otp: otp // In production, don't return OTP in response
    });
  } catch (error) {
    console.error('Send OTP error:', error);
    res.status(500).json({ success: false, message: 'Failed to send OTP' });
  }
});

app.post('/api/auth/verify-otp', (req, res) => {
  try {
    const { phone, otp } = req.body;
    console.log(`🔐 OTP verification for phone: ${phone}, OTP: ${otp}`);
    
    if (!phone || !otp) {
      return res.status(400).json({ success: false, message: 'Phone and OTP are required' });
    }

    const storedOtpData = otpStore.get(phone);
    
    if (!storedOtpData) {
      return res.status(400).json({ success: false, message: 'OTP not found or expired' });
    }
    
    if (Date.now() > storedOtpData.expiresAt) {
      otpStore.delete(phone);
      return res.status(400).json({ success: false, message: 'OTP expired' });
    }
    
    if (storedOtpData.otp !== otp) {
      return res.status(400).json({ success: false, message: 'Invalid OTP' });
    }
    
    // OTP verified, clear it
    otpStore.delete(phone);
    
    // Check if user exists
    let user = users.find(u => u.phone === phone);
    let isNewUser = false;
    
    if (!user) {
      // Create new user
      isNewUser = true;
      user = {
        id: users.length + 1,
        phone,
        name: `User ${users.length + 1}`,
        email: '',
        role: 'CUSTOMER',
        isActive: true,
        password: bcrypt.hashSync('defaultPassword', 10)
      };
      users.push(user);
      console.log(`👤 New user created: ${user.name}`);
    }
    
    const token = generateToken(user);
    
    console.log(`✅ Login successful for ${user.name}`);
    res.json({
      success: true,
      token,
      phone: user.phone,
      name: user.name,
      isNewUser
    });
  } catch (error) {
    console.error('Verify OTP error:', error);
    res.status(500).json({ success: false, message: 'OTP verification failed' });
  }
});

app.post('/api/auth/login', (req, res) => {
  try {
    const { email, password } = req.body;
    console.log(`🔐 Login attempt for email: ${email}`);
    
    const user = users.find(u => u.email === email);
    
    if (!user || !bcrypt.compareSync(password, user.password)) {
      return res.status(401).json({ success: false, message: 'Invalid credentials' });
    }
    
    if (!user.isActive) {
      return res.status(401).json({ success: false, message: 'Account deactivated' });
    }
    
    const token = generateToken(user);
    
    console.log(`✅ Login successful for ${user.name} (${user.role})`);
    res.json({
      success: true,
      token,
      user: {
        id: user.id,
        name: user.name,
        email: user.email,
        phone: user.phone,
        role: user.role
      }
    });
  } catch (error) {
    console.error('Login error:', error);
    res.status(500).json({ success: false, message: 'Login failed' });
  }
});

app.post('/api/auth/signup', (req, res) => {
  try {
    const { name, email, phone, password } = req.body;
    console.log(`📝 Signup attempt for email: ${email}`);
    
    if (users.find(u => u.email === email || u.phone === phone)) {
      return res.status(400).json({ success: false, message: 'User already exists' });
    }
    
    const user = {
      id: users.length + 1,
      name,
      email,
      phone,
      password: bcrypt.hashSync(password, 10),
      role: 'CUSTOMER',
      isActive: true
    };
    
    users.push(user);
    const token = generateToken(user);
    
    console.log(`✅ Signup successful for ${user.name}`);
    res.json({
      success: true,
      token,
      user: {
        id: user.id,
        name: user.name,
        email: user.email,
        phone: user.phone,
        role: user.role
      }
    });
  } catch (error) {
    console.error('Signup error:', error);
    res.status(500).json({ success: false, message: 'Signup failed' });
  }
});

// ==================== MENU ENDPOINTS ====================
app.get('/api/menu/dishes', (req, res) => {
  try {
    console.log(`🍽️ Fetching all dishes`);
    const availableDishes = dishes.filter(d => d.isAvailable);
    console.log(`✅ Returning ${availableDishes.length} dishes`);
    res.json(availableDishes);
  } catch (error) {
    console.error('Get dishes error:', error);
    res.status(500).json({ error: 'Failed to fetch dishes' });
  }
});

app.get('/api/menu/dishes/:id', (req, res) => {
  try {
    const dishId = parseInt(req.params.id);
    const dish = dishes.find(d => d.id === dishId);
    
    if (!dish) {
      return res.status(404).json({ error: 'Dish not found' });
    }
    
    console.log(`🍽️ Fetching dish: ${dish.name}`);
    res.json(dish);
  } catch (error) {
    console.error('Get dish error:', error);
    res.status(500).json({ error: 'Failed to fetch dish' });
  }
});

// ==================== ORDER ENDPOINTS ====================
app.get('/api/orders', authenticateToken, (req, res) => {
  try {
    let userOrders;
    
    if (req.user.role === 'ADMIN') {
      userOrders = orders;
    } else if (req.user.role === 'DELIVERY_PARTNER') {
      userOrders = orders.filter(o => o.deliveryPartnerId === req.user.id || o.status === 'CONFIRMED');
    } else {
      userOrders = orders.filter(o => o.userId === req.user.id);
    }
    
    console.log(`📦 Fetching ${userOrders.length} orders for ${req.user.role}`);
    res.json(userOrders);
  } catch (error) {
    console.error('Get orders error:', error);
    res.status(500).json({ error: 'Failed to fetch orders' });
  }
});

app.post('/api/orders', authenticateToken, (req, res) => {
  try {
    const { items, deliveryAddress, paymentMethod, specialInstructions } = req.body;
    
    const order = {
      id: orders.length + 1,
      userId: req.user.id,
      items,
      totalAmount: items.reduce((sum, item) => sum + (item.price * item.quantity), 0),
      deliveryAddress,
      paymentMethod,
      specialInstructions,
      status: 'PENDING',
      paymentStatus: 'PENDING',
      orderTime: new Date().toISOString(),
      deliveryTime: null,
      deliveryPartnerId: null
    };
    
    orders.push(order);
    
    console.log(`📦 Order created: #${order.id} for ${req.user.name}`);
    res.json({ success: true, order });
  } catch (error) {
    console.error('Create order error:', error);
    res.status(500).json({ error: 'Failed to create order' });
  }
});

app.put('/api/orders/:id/status', authenticateToken, (req, res) => {
  try {
    const orderId = parseInt(req.params.id);
    const { status } = req.body;
    
    const order = orders.find(o => o.id === orderId);
    if (!order) {
      return res.status(404).json({ error: 'Order not found' });
    }
    
    // Check permissions
    if (req.user.role === 'CUSTOMER' && order.userId !== req.user.id) {
      return res.status(403).json({ error: 'Not authorized' });
    }
    
    order.status = status;
    
    if (status === 'OUT_FOR_DELIVERY' && req.user.role === 'DELIVERY_PARTNER') {
      order.deliveryPartnerId = req.user.id;
    }
    
    console.log(`📦 Order #${orderId} status updated to ${status}`);
    res.json({ success: true, order });
  } catch (error) {
    console.error('Update order status error:', error);
    res.status(500).json({ error: 'Failed to update order status' });
  }
});

// ==================== ADMIN ENDPOINTS ====================
app.get('/api/admin/stats', authenticateToken, requireRole('ADMIN'), (req, res) => {
  try {
    const stats = {
      totalUsers: users.filter(u => u.role === 'CUSTOMER').length,
      totalOrders: orders.length,
      totalRevenue: orders.reduce((sum, order) => sum + order.totalAmount, 0),
      totalDishes: dishes.length,
      activeDeliveryPartners: users.filter(u => u.role === 'DELIVERY_PARTNER' && u.isActive).length,
      ordersToday: orders.filter(o => {
        const today = new Date().toDateString();
        return new Date(o.orderTime).toDateString() === today;
      }).length
    };
    
    console.log(`📊 Admin stats requested`);
    res.json(stats);
  } catch (error) {
    console.error('Get admin stats error:', error);
    res.status(500).json({ error: 'Failed to get admin stats' });
  }
});

app.get('/api/admin/users', authenticateToken, requireRole('ADMIN'), (req, res) => {
  try {
    const sanitizedUsers = users.map(u => ({
      id: u.id,
      name: u.name,
      email: u.email,
      phone: u.phone,
      role: u.role,
      isActive: u.isActive
    }));
    
    console.log(`👥 Admin fetching all users`);
    res.json(sanitizedUsers);
  } catch (error) {
    console.error('Get users error:', error);
    res.status(500).json({ error: 'Failed to get users' });
  }
});

app.put('/api/admin/users/:id/toggle-status', authenticateToken, requireRole('ADMIN'), (req, res) => {
  try {
    const userId = parseInt(req.params.id);
    const user = users.find(u => u.id === userId);
    
    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }
    
    user.isActive = !user.isActive;
    
    console.log(`👤 User ${user.name} status toggled to ${user.isActive ? 'active' : 'inactive'}`);
    res.json({ success: true, user: { id: user.id, isActive: user.isActive } });
  } catch (error) {
    console.error('Toggle user status error:', error);
    res.status(500).json({ error: 'Failed to toggle user status' });
  }
});

// ==================== DELIVERY PARTNER ENDPOINTS ====================
app.post('/api/delivery/auth/login', (req, res) => {
  try {
    const { email, password } = req.body;
    console.log(`🚚 Delivery partner login attempt: ${email}`);
    
    const user = users.find(u => u.email === email && u.role === 'DELIVERY_PARTNER');
    
    if (!user || !bcrypt.compareSync(password, user.password)) {
      return res.status(401).json({ success: false, message: 'Invalid credentials' });
    }
    
    if (!user.isActive) {
      return res.status(401).json({ success: false, message: 'Account deactivated' });
    }
    
    const token = generateToken(user);
    
    console.log(`✅ Delivery partner login successful: ${user.name}`);
    res.json({
      success: true,
      token,
      partner: {
        id: user.id,
        name: user.name,
        email: user.email,
        phone: user.phone
      }
    });
  } catch (error) {
    console.error('Delivery login error:', error);
    res.status(500).json({ success: false, message: 'Login failed' });
  }
});

app.get('/api/delivery/orders', authenticateToken, requireRole('DELIVERY_PARTNER'), (req, res) => {
  try {
    const availableOrders = orders.filter(o => 
      o.status === 'CONFIRMED' || 
      (o.deliveryPartnerId === req.user.id && ['OUT_FOR_DELIVERY', 'DELIVERED'].includes(o.status))
    );
    
    console.log(`🚚 Delivery partner ${req.user.name} fetching orders`);
    res.json(availableOrders);
  } catch (error) {
    console.error('Get delivery orders error:', error);
    res.status(500).json({ error: 'Failed to fetch delivery orders' });
  }
});

app.get('/api/delivery/stats', authenticateToken, requireRole('DELIVERY_PARTNER'), (req, res) => {
  try {
    const partnerOrders = orders.filter(o => o.deliveryPartnerId === req.user.id);
    const todayOrders = partnerOrders.filter(o => {
      const today = new Date().toDateString();
      return new Date(o.orderTime).toDateString() === today;
    });
    
    const stats = {
      totalDeliveries: partnerOrders.filter(o => o.status === 'DELIVERED').length,
      totalEarnings: partnerOrders.filter(o => o.status === 'DELIVERED').length * 50, // ₹50 per delivery
      todayDeliveries: todayOrders.filter(o => o.status === 'DELIVERED').length,
      todayEarnings: todayOrders.filter(o => o.status === 'DELIVERED').length * 50,
      pendingOrders: partnerOrders.filter(o => o.status === 'OUT_FOR_DELIVERY').length,
      rating: 4.5 + Math.random() * 0.5 // Mock rating
    };
    
    console.log(`📊 Delivery partner stats for ${req.user.name}`);
    res.json(stats);
  } catch (error) {
    console.error('Get delivery stats error:', error);
    res.status(500).json({ error: 'Failed to get delivery stats' });
  }
});

app.get('/api/delivery/profile', authenticateToken, requireRole('DELIVERY_PARTNER'), (req, res) => {
  try {
    const user = users.find(u => u.id === req.user.id);
    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }
    
    const profile = {
      id: user.id,
      name: user.name,
      email: user.email,
      phone: user.phone,
      isActive: user.isActive,
      vehicleNumber: 'MH01AB1234', // Mock data
      licenseNumber: 'DL123456789', // Mock data
      rating: 4.5 + Math.random() * 0.5,
      totalDeliveries: orders.filter(o => o.deliveryPartnerId === user.id && o.status === 'DELIVERED').length
    };
    
    console.log(`👤 Delivery partner profile for ${user.name}`);
    res.json(profile);
  } catch (error) {
    console.error('Get delivery profile error:', error);
    res.status(500).json({ error: 'Failed to get delivery profile' });
  }
});

// ==================== PAYMENT ENDPOINTS ====================
app.post('/api/payments', authenticateToken, (req, res) => {
  try {
    const { amount, currency = 'INR', paymentMethod = 'RAZORPAY' } = req.body;
    
    const paymentOrder = {
      id: `order_${Date.now()}`,
      amount: amount * 100, // Convert to paise
      currency,
      status: 'created',
      paymentMethod
    };
    
    console.log(`💳 Payment order created: ${paymentOrder.id} for ₹${amount}`);
    res.json(paymentOrder);
  } catch (error) {
    console.error('Create payment order error:', error);
    res.status(500).json({ error: 'Failed to create payment order' });
  }
});

app.post('/api/payments/create-order', authenticateToken, (req, res) => {
  try {
    const { amount, currency = 'INR' } = req.body;
    
    const paymentOrder = {
      id: `order_${Date.now()}`,
      amount: amount * 100, // Convert to paise
      currency,
      status: 'created'
    };
    
    console.log(`💳 Payment order created: ${paymentOrder.id} for ₹${amount}`);
    res.json(paymentOrder);
  } catch (error) {
    console.error('Create payment order error:', error);
    res.status(500).json({ error: 'Failed to create payment order' });
  }
});

app.post('/api/payments/verify', authenticateToken, (req, res) => {
  try {
    const { paymentId, orderId, signature } = req.body;
    
    // Mock verification (in production, verify with payment gateway)
    const isValid = paymentId && orderId && signature;
    
    if (isValid) {
      // Update order payment status
      const order = orders.find(o => o.id.toString() === orderId.split('_')[1]);
      if (order) {
        order.paymentStatus = 'COMPLETED';
        order.status = 'CONFIRMED';
      }
      
      console.log(`✅ Payment verified for order: ${orderId}`);
      res.json({ success: true, message: 'Payment verified successfully' });
    } else {
      console.log(`❌ Payment verification failed for order: ${orderId}`);
      res.status(400).json({ success: false, message: 'Payment verification failed' });
    }
  } catch (error) {
    console.error('Verify payment error:', error);
    res.status(500).json({ error: 'Failed to verify payment' });
  }
});

// ==================== NOTIFICATION ENDPOINTS ====================
app.get('/api/notifications', authenticateToken, (req, res) => {
  try {
    const userNotifications = notifications.filter(n => 
      n.userId === req.user.id || 
      (req.user.role === 'ADMIN' && n.type === 'ADMIN') ||
      (req.user.role === 'DELIVERY_PARTNER' && n.type === 'DELIVERY')
    );
    
    console.log(`🔔 Fetching ${userNotifications.length} notifications`);
    res.json(userNotifications);
  } catch (error) {
    console.error('Get notifications error:', error);
    res.status(500).json({ error: 'Failed to fetch notifications' });
  }
});

// ==================== HEALTH CHECK ====================
app.get('/health', (req, res) => {
  res.json({ 
    status: 'OK', 
    timestamp: new Date().toISOString(),
    uptime: process.uptime(),
    environment: process.env.NODE_ENV || 'development'
  });
});

// Start server
app.listen(port, () => {
  console.log('🍛 Tiffin App API Server running on http://localhost:' + port);
  console.log('📊 Health check: http://localhost:' + port + '/health');
  console.log('🔐 Test credentials:');
  console.log('   - Customer: user@test.com / password');
  console.log('   - Admin: admin@tiffin.com / password');
  console.log('   - Delivery: john@delivery.com / password');
  console.log('📋 Available endpoints:');
  console.log('   - Authentication: /api/auth/*');
  console.log('   - Menu: /api/menu/*');
  console.log('   - Orders: /api/orders/*');
  console.log('   - Admin: /api/admin/*');
  console.log('   - Delivery: /api/delivery/*');
  console.log('   - Payments: /api/payments/*');
  console.log('   - Notifications: /api/notifications/*');
});

// Graceful shutdown
process.on('SIGTERM', () => {
  console.log('🛑 Server shutting down gracefully...');
  process.exit(0);
});

process.on('SIGINT', () => {
  console.log('🛑 Server shutting down gracefully...');
  process.exit(0);
});