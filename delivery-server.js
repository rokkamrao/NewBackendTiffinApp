const express = require('express');
const cors = require('cors');
const app = express();
const port = 8081;

// Middleware
app.use(cors());
app.use(express.json());

// Mock database
let deliveryPartners = [
  {
    id: 1,
    email: 'john@delivery.com',
    name: 'John Doe',
    phone: '+1234567890',
    isOnline: true,
    currentLocation: { lat: 40.7128, lng: -74.0060 },
    earnings: { total: 1250.50, today: 85.75 },
    rating: 4.8,
    completedDeliveries: 156,
    vehicleType: 'BIKE'
  }
];

let deliveries = [
  {
    id: 1,
    orderId: 'ORD001',
    customerName: 'Alice Smith',
    pickupAddress: '123 Restaurant St, New York',
    deliveryAddress: '456 Customer Ave, New York',
    customerPhone: '+1987654321',
    items: [
      { name: 'Chicken Biryani', quantity: 2 },
      { name: 'Raita', quantity: 1 }
    ],
    amount: 25.99,
    status: 'ASSIGNED',
    estimatedDeliveryTime: new Date(Date.now() + 30 * 60 * 1000).toISOString(),
    specialInstructions: 'Ring the doorbell twice',
    assignedPartnerId: 1
  },
  {
    id: 2,
    orderId: 'ORD002',
    customerName: 'Bob Johnson',
    pickupAddress: '789 Food Corner, New York',
    deliveryAddress: '321 Home St, New York',
    customerPhone: '+1555666777',
    items: [
      { name: 'Masala Dosa', quantity: 1 },
      { name: 'Filter Coffee', quantity: 2 }
    ],
    amount: 18.50,
    status: 'READY_FOR_PICKUP',
    estimatedDeliveryTime: new Date(Date.now() + 45 * 60 * 1000).toISOString(),
    specialInstructions: 'Leave at the door',
    assignedPartnerId: 1
  }
];

// Authentication endpoints
app.post('/api/delivery/auth/login', (req, res) => {
  const { email, password } = req.body;
  
  const partner = deliveryPartners.find(p => p.email === email);
  if (partner && password === 'password') {
    res.json({
      success: true,
      token: 'delivery_token_' + partner.id,
      partner: partner
    });
  } else {
    res.status(401).json({
      success: false,
      message: 'Invalid credentials'
    });
  }
});

// Delivery partner endpoints
app.get('/api/delivery/profile', (req, res) => {
  const partner = deliveryPartners[0]; // Mock logged in partner
  res.json(partner);
});

app.put('/api/delivery/status', (req, res) => {
  const { isOnline } = req.body;
  deliveryPartners[0].isOnline = isOnline;
  res.json({ success: true, isOnline });
});

app.put('/api/delivery/location', (req, res) => {
  const { lat, lng } = req.body;
  deliveryPartners[0].currentLocation = { lat, lng };
  res.json({ success: true, location: { lat, lng } });
});

// Delivery management endpoints
app.get('/api/delivery/orders', (req, res) => {
  const partnerId = 1; // Mock logged in partner
  const partnerDeliveries = deliveries.filter(d => d.assignedPartnerId === partnerId);
  res.json(partnerDeliveries);
});

app.get('/api/delivery/orders/:id', (req, res) => {
  const deliveryId = parseInt(req.params.id);
  const delivery = deliveries.find(d => d.id === deliveryId);
  
  if (delivery) {
    res.json(delivery);
  } else {
    res.status(404).json({ message: 'Delivery not found' });
  }
});

app.put('/api/delivery/orders/:id/accept', (req, res) => {
  const deliveryId = parseInt(req.params.id);
  const delivery = deliveries.find(d => d.id === deliveryId);
  
  if (delivery) {
    delivery.status = 'ACCEPTED';
    res.json({ success: true, delivery });
  } else {
    res.status(404).json({ message: 'Delivery not found' });
  }
});

app.put('/api/delivery/orders/:id/status', (req, res) => {
  const deliveryId = parseInt(req.params.id);
  const { status } = req.body;
  const delivery = deliveries.find(d => d.id === deliveryId);
  
  if (delivery) {
    delivery.status = status;
    
    // Update earnings when delivery is completed
    if (status === 'DELIVERED') {
      deliveryPartners[0].earnings.today += delivery.amount * 0.15; // 15% commission
      deliveryPartners[0].earnings.total += delivery.amount * 0.15;
      deliveryPartners[0].completedDeliveries += 1;
    }
    
    res.json({ success: true, delivery });
  } else {
    res.status(404).json({ message: 'Delivery not found' });
  }
});

// Dashboard stats
app.get('/api/delivery/stats', (req, res) => {
  const partner = deliveryPartners[0];
  const todayDeliveries = deliveries.filter(d => 
    d.assignedPartnerId === partner.id && 
    d.status === 'DELIVERED'
  ).length;
  
  res.json({
    totalEarnings: partner.earnings.total,
    todayEarnings: partner.earnings.today,
    totalDeliveries: partner.completedDeliveries,
    todayDeliveries: todayDeliveries,
    rating: partner.rating,
    activeOrders: deliveries.filter(d => 
      d.assignedPartnerId === partner.id && 
      ['ASSIGNED', 'ACCEPTED', 'PICKED_UP'].includes(d.status)
    ).length
  });
});

// Health check
app.get('/health', (req, res) => {
  res.json({ status: 'OK', service: 'Delivery API', port: port });
});

app.listen(port, () => {
  console.log(`🚚 Delivery API Server running on http://localhost:${port}`);
  console.log(`📊 Dashboard stats: http://localhost:${port}/api/delivery/stats`);
  console.log(`🔐 Test login: john@delivery.com / password`);
});