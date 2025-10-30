# Razorpay Payment Integration Guide

## Setup Instructions

### 1. Add Razorpay Test Credentials

You can configure Razorpay credentials in three ways:

#### Option A: Environment Variables (Recommended for production)
```bash
export RAZORPAY_KEY_ID=rzp_test_your_key_id
export RAZORPAY_KEY_SECRET=your_key_secret
export RAZORPAY_WEBHOOK_SECRET=your_webhook_secret
```

#### Option B: Update application.yml directly (for local testing)
```yaml
app:
  razorpay:
    key-id: rzp_test_your_key_id
    key-secret: your_key_secret
    webhook-secret: your_webhook_secret
```

#### Option C: Use application-local.yml (gitignored)
Create `src/main/resources/application-local.yml`:
```yaml
app:
  razorpay:
    key-id: rzp_test_your_key_id
    key-secret: your_key_secret
    webhook-secret: your_webhook_secret
```

Then run with: `mvn spring-boot:run -Dspring-boot.run.profiles=local`

### 2. Get Razorpay Test Keys

1. Login to [Razorpay Dashboard](https://dashboard.razorpay.com/)
2. Switch to **Test Mode** (toggle in top-left)
3. Go to **Settings** → **API Keys**
4. Click **Generate Test Key**
5. Copy **Key ID** and **Key Secret**

### 3. Setup Webhook (for production)

1. Go to **Settings** → **Webhooks** in Razorpay Dashboard
2. Click **+ Add New Webhook**
3. Enter webhook URL: `https://yourdomain.com/api/payments/webhook`
4. Select events:
   - `payment.authorized`
   - `payment.captured`
   - `payment.failed`
   - `order.paid`
5. Copy the **Webhook Secret**

## API Endpoints

### 1. Create Payment Order

**POST** `/api/payments`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Request Body:**
```json
{
  "orderId": "12345",
  "amount": 500.00,
  "currency": "INR",
  "paymentMethod": "card",
  "paymentMethodDetails": {
    "cardNumber": "4111111111111111",
    "expiryMonth": "12",
    "expiryYear": "2025",
    "cvv": "123",
    "cardHolderName": "John Doe"
  }
}
```

**Response:**
```json
{
  "id": "pay_xxx",
  "orderId": "12345",
  "userId": "9876543210",
  "amount": 500.00,
  "currency": "INR",
  "status": "PENDING",
  "paymentMethod": "card",
  "transactionId": "order_razorpay_xxx",
  "createdAt": "2025-10-25T15:30:00",
  "updatedAt": "2025-10-25T15:30:00"
}
```

### 2. Verify Payment

**POST** `/api/payments/verify`

**Request Body:**
```json
{
  "razorpay_order_id": "order_xxx",
  "razorpay_payment_id": "pay_xxx",
  "razorpay_signature": "signature_xxx"
}
```

**Response:**
```json
{
  "status": "success",
  "message": "Payment verified successfully",
  "payment_id": "pay_xxx"
}
```

### 3. Get User Payments

**GET** `/api/payments`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```json
[
  {
    "id": "pay_1",
    "orderId": "12345",
    "amount": 500.00,
    "status": "SUCCESS",
    "transactionId": "order_razorpay_xxx",
    "createdAt": "2025-10-25T15:30:00"
  }
]
```

### 4. Get Payment by ID

**GET** `/api/payments/{paymentId}`

### 5. Retry Failed Payment

**POST** `/api/payments/{paymentId}/retry`

## Test Cards (Razorpay Test Mode)

### Successful Payment
- **Card Number:** `4111 1111 1111 1111`
- **CVV:** Any 3 digits
- **Expiry:** Any future date
- **Name:** Any name

### Failed Payment (Insufficient Funds)
- **Card Number:** `4000 0000 0000 0002`

### Payment Authentication Required (3D Secure)
- **Card Number:** `5104 0600 0000 0008`

### For UPI Testing
- **UPI ID:** `success@razorpay`

## Frontend Integration Example

### Step 1: Create Order (Backend API Call)
```javascript
const createOrder = async (orderData) => {
  const response = await fetch('/api/payments', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${jwtToken}`
    },
    body: JSON.stringify(orderData)
  });
  return response.json();
};
```

### Step 2: Initialize Razorpay Checkout
```html
<script src="https://checkout.razorpay.com/v1/checkout.js"></script>
```

```javascript
const options = {
  key: "rzp_test_your_key_id", // Your Razorpay Key ID
  amount: payment.amount * 100, // Amount in paise
  currency: payment.currency,
  name: "Tiffin App",
  description: "Order Payment",
  order_id: payment.transactionId, // Razorpay order ID from backend
  handler: function (response) {
    // Verify payment on backend
    verifyPayment(response);
  },
  prefill: {
    name: "Customer Name",
    email: "customer@example.com",
    contact: "9876543210"
  },
  theme: {
    color: "#3399cc"
  }
};

const rzp = new Razorpay(options);
rzp.open();
```

### Step 3: Verify Payment
```javascript
const verifyPayment = async (razorpayResponse) => {
  const response = await fetch('/api/payments/verify', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      razorpay_order_id: razorpayResponse.razorpay_order_id,
      razorpay_payment_id: razorpayResponse.razorpay_payment_id,
      razorpay_signature: razorpayResponse.razorpay_signature
    })
  });
  
  const result = await response.json();
  if (result.status === 'success') {
    // Payment successful
    console.log('Payment verified!');
  }
};
```

## Payment Flow

1. **User initiates payment** → Frontend calls `/api/payments`
2. **Backend creates Razorpay order** → Returns order details with `transactionId` (Razorpay order_id)
3. **Frontend opens Razorpay checkout** → User completes payment
4. **Razorpay callback** → Frontend receives payment details
5. **Verify signature** → Frontend calls `/api/payments/verify`
6. **Backend validates** → Updates payment and order status
7. **Webhook notification** → Razorpay sends webhook to `/api/payments/webhook`

## Testing Webhooks Locally

### Using ngrok:
```bash
# Install ngrok
npm install -g ngrok

# Expose local server
ngrok http 8080

# Use the ngrok URL in Razorpay webhook settings
# Example: https://abc123.ngrok.io/api/payments/webhook
```

## Security Best Practices

1. **Never expose Key Secret** - Keep it server-side only
2. **Always verify signatures** - For both payment callbacks and webhooks
3. **Use HTTPS in production** - Required for webhooks
4. **Validate amounts** - Check amount matches order total
5. **Implement idempotency** - Handle duplicate webhook calls
6. **Log all transactions** - For audit and debugging

## Troubleshooting

### Payment fails with "Invalid key"
- Verify you're using the correct test/live key
- Check if key is properly set in environment variables

### Webhook signature verification fails
- Ensure webhook secret matches Razorpay dashboard
- Check payload is passed as-is (not parsed JSON)

### Amount mismatch errors
- Razorpay uses paise (multiply by 100)
- Verify currency is set correctly

## Additional Features

The implementation includes methods for:
- **Capture Payment**: For authorized payments
- **Refund Payment**: Full or partial refunds
- **Fetch Payment Details**: Get payment status from Razorpay

See `RazorpayPaymentGatewayClient.java` for complete API.

## Support

- [Razorpay Documentation](https://razorpay.com/docs/)
- [Razorpay API Reference](https://razorpay.com/docs/api/)
- [Test Cards & Credentials](https://razorpay.com/docs/payments/payments/test-card-details/)
