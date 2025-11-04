-- Demo Delivery Partner Data for Testing
-- Run this SQL script in your PostgreSQL database to create test data

-- Insert demo delivery partner
INSERT INTO delivery_partners (
    phone, name, email, password_hash, status, vehicle_type, vehicle_number,
    license_number, aadhar_number, rating, total_deliveries, total_earnings,
    is_online, documents_verified, joining_date, created_at, updated_at
) VALUES (
    '9876543210',
    'Rajesh Kumar',
    'rajesh.delivery@tiffin.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- password: delivery123
    'APPROVED',
    'MOTORCYCLE',
    'KA-01-AB-1234',
    'DL12345678',
    '123456789012',
    4.8,
    1250,
    35000.00,
    true,
    true,
    NOW() - INTERVAL '6 months',
    NOW(),
    NOW()
);

-- Insert demo deliveries for the partner
INSERT INTO deliveries (
    order_id, delivery_partner_id, status, pickup_address, pickup_latitude, pickup_longitude,
    delivery_address, delivery_latitude, delivery_longitude, customer_name, customer_phone,
    delivery_instructions, delivery_fee, estimated_delivery_time, delivery_otp,
    distance_km, created_at, updated_at
) VALUES 
(
    1001,
    (SELECT id FROM delivery_partners WHERE phone = '9876543210'),
    'DELIVERED',
    'Tiffin Kitchen, Brigade Road, Bangalore',
    12.9716,
    77.5946,
    'Koramangala 5th Block, Bangalore',
    12.9352,
    77.6245,
    'Priya Sharma',
    '9876543211',
    'Ring the bell twice, Apartment 2B',
    25.00,
    NOW() - INTERVAL '2 hours',
    '123456',
    3.5,
    NOW() - INTERVAL '3 hours',
    NOW() - INTERVAL '2 hours'
),
(
    1002,
    (SELECT id FROM delivery_partners WHERE phone = '9876543210'),
    'IN_TRANSIT',
    'Tiffin Kitchen, Indiranagar, Bangalore',
    12.9784,
    77.6408,
    'Whitefield, Bangalore',
    12.9698,
    77.7499,
    'Amit Patel',
    '9876543212',
    'Call before delivery, Gate code: 1234',
    30.00,
    NOW() + INTERVAL '30 minutes',
    '789012',
    8.2,
    NOW() - INTERVAL '45 minutes',
    NOW()
),
(
    1003,
    (SELECT id FROM delivery_partners WHERE phone = '9876543210'),
    'ASSIGNED',
    'Tiffin Kitchen, JP Nagar, Bangalore',
    12.9082,
    77.5833,
    'Banashankari, Bangalore',
    12.9265,
    77.5787,
    'Sneha Reddy',
    '9876543213',
    'Vegetarian only, no spicy food',
    20.00,
    NOW() + INTERVAL '1 hour',
    '345678',
    4.1,
    NOW(),
    NOW()
);

-- Update partner stats
UPDATE delivery_partners 
SET 
    total_deliveries = (SELECT COUNT(*) FROM deliveries WHERE delivery_partner_id = delivery_partners.id AND status = 'DELIVERED'),
    total_earnings = (SELECT COALESCE(SUM(delivery_fee), 0) FROM deliveries WHERE delivery_partner_id = delivery_partners.id AND status = 'DELIVERED')
WHERE phone = '9876543210';