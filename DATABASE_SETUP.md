# Database Configuration Guide

## Current Setup
The application now supports multiple database configurations for different environments:

### 🚀 Quick Start (Current Default)
- **Database**: H2 In-Memory 
- **Profile**: `local`
- **Access**: No setup required, starts immediately
- **H2 Console**: http://localhost:8081/api/h2-console
  - JDBC URL: `jdbc:h2:mem:tiffin_local`
  - Username: `sa`
  - Password: (empty)

### 🐘 PostgreSQL Setup (Optional)

#### Step 1: Install PostgreSQL
```bash
# Windows (using Chocolatey)
choco install postgresql

# Or download from: https://www.postgresql.org/download/
```

#### Step 2: Create Local Database
```sql
-- Connect to PostgreSQL as superuser
psql -U postgres

-- Create database and user
CREATE DATABASE tiffin_local;
CREATE USER tiffin_user WITH PASSWORD 'tiffin123';
GRANT ALL PRIVILEGES ON DATABASE tiffin_local TO tiffin_user;
```

#### Step 3: Update Configuration
Edit `application-local.properties` and uncomment PostgreSQL section:

```properties
# Comment out H2 configuration
# spring.datasource.url=jdbc:h2:mem:tiffin_local
# spring.datasource.driver-class-name=org.h2.Driver
# spring.datasource.username=sa
# spring.datasource.password=
# spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# Uncomment PostgreSQL configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/tiffin_local
spring.datasource.username=tiffin_user
spring.datasource.password=tiffin123
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### 🔄 Profile Management

#### Available Profiles:
- `local` - Local development (H2 or PostgreSQL)
- `dev` - Development environment (PostgreSQL)
- `prod` - Production environment
- `simple` - Simple testing (H2)

#### Switch Profiles:
```bash
# Use local profile (default)
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Use development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Use simple profile (H2 with console enabled)
mvn spring-boot:run -Dspring-boot.run.profiles=simple
```

### 🔧 Configuration Files:
- `application.properties` - Main configuration with active profile
- `application-local.properties` - Local development settings
- `application-dev.yml` - Development environment
- `application-simple.properties` - Simple H2 setup
- `application-prod.yml` - Production settings

### 🌐 Frontend Integration:
The backend is configured to work with your Angular frontend at:
- `http://localhost:4200` (main)
- `http://localhost:3000` (alternative)

### 🔐 Test Accounts Available:
- **Regular User**: `john.customer@example.com` / `password123`
- **Premium User**: `priya.premium@example.com` / `password123`
- **Admin**: `admin@tiffin.app` / `admin123`
- **Test User**: `test@tiffin.app` / `test123` (Phone: `9999999999`)

### 💳 Payment Testing:
- **Razorpay Test Key**: `rzp_test_RXfBZEH79up8IS`
- **Test Card**: 4111 1111 1111 1111
- **Test UPI**: success@razorpay

---

**Recommendation**: Start with H2 (current setup) for quick development and testing. Switch to PostgreSQL later when you need data persistence between restarts.