# Database Migration Guide

## Overview
This guide documents the database migration strategies implemented to resolve compilation and startup errors when working with existing PostgreSQL data.

## Migration Issues Resolved

### 1. Non-null Constraint Violations

#### Problem
When adding new non-null columns to existing tables with data, PostgreSQL throws errors:
```
ERROR: column "first_name" of relation "users" contains null values
ERROR: column "phone_verified" of relation "users" contains null values
ERROR: column "updated_at" of relation "users" contains null values
```

#### Solution
Modified entity definitions to be migration-safe:

**User.java Changes:**
```java
// Before: nullable = false (causes migration errors)
@Column(nullable = false, length = 50)
private String firstName;

// After: nullable = true with default values
@Column(length = 50)
@Builder.Default
private String firstName = "";
```

#### Fields Made Nullable for Migration:
- `firstName` - Can be empty string, will be populated by users
- `lastName` - Can be empty string, will be populated by users  
- `phoneVerified` - Defaults to false for existing users
- `updatedAt` - Will be set by @UpdateTimestamp on first update

### 2. Floating Point Column Definitions

#### Problem
```
ERROR: scale has no meaning for SQL floating point types
```

#### Solution
Removed scale annotations from Double fields:

**Address.java Changes:**
```java
// Before: Invalid for Double types
@Column(precision = 10, scale = 8)
private Double latitude;

// After: Simple column definition
@Column
private Double latitude;
```

### 3. Data Population Strategy

For existing users without required fields:

#### Option A: Default Values (Current Implementation)
```sql
-- Existing users will have empty strings for names
-- Frontend should prompt for completion on first login
UPDATE users SET first_name = '' WHERE first_name IS NULL;
UPDATE users SET last_name = '' WHERE last_name IS NULL;
```

#### Option B: Data Migration Script (Alternative)
```sql
-- More sophisticated approach for production
UPDATE users 
SET first_name = COALESCE(SPLIT_PART(full_name, ' ', 1), 'User'),
    last_name = COALESCE(SPLIT_PART(full_name, ' ', 2), '')
WHERE first_name IS NULL OR last_name IS NULL;
```

## Production Migration Plan

### Phase 1: Backup
```bash
pg_dump tiffindb > backup_$(date +%Y%m%d_%H%M%S).sql
```

### Phase 2: Apply Schema Changes
```sql
-- Add nullable columns first
ALTER TABLE users ADD COLUMN IF NOT EXISTS first_name VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_name VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
```

### Phase 3: Data Population
```sql
-- Set default values for existing data
UPDATE users SET 
    first_name = COALESCE(first_name, ''),
    last_name = COALESCE(last_name, ''),
    phone_verified = COALESCE(phone_verified, FALSE),
    updated_at = COALESCE(updated_at, NOW())
WHERE first_name IS NULL OR last_name IS NULL 
   OR phone_verified IS NULL OR updated_at IS NULL;
```

### Phase 4: Application Deployment
Deploy the updated application with migration-safe entity definitions.

### Phase 5: Data Cleanup (Optional)
After confirming stable operation, optionally add constraints:
```sql
-- Only if you want to enforce non-null constraints later
-- ALTER TABLE users ALTER COLUMN first_name SET NOT NULL;
-- ALTER TABLE users ALTER COLUMN last_name SET NOT NULL;
```

## Best Practices for Future Migrations

### 1. Always Use Migration-Safe Defaults
```java
// Good: Provides default for existing data
@Column(length = 50)
@Builder.Default
private String newField = "default_value";

// Avoid: Will break existing data
@Column(nullable = false, length = 50)
private String newField;
```

### 2. Handle Existing Data
- Plan for existing null values
- Provide meaningful defaults
- Consider data transformation scripts

### 3. Test Migration Process
```bash
# Test on development database first
mvn spring-boot:run -Dspring.profiles.active=dev
```

### 4. Monitor Application Logs
Watch for migration warnings during startup:
```
WARN o.h.t.s.i.ExceptionHandlerLoggedImpl - GenerationTarget encountered exception
```

## Configuration Properties

### Development (Permissive)
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # Allows schema evolution
    show-sql: true      # Shows generated SQL
```

### Production (Strict)
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate # No automatic schema changes
    show-sql: false     # Don't log SQL in production
```

## Rollback Strategy

If migration issues occur:

### 1. Stop Application
```bash
# Kill Spring Boot process
pkill -f "spring-boot:run"
```

### 2. Restore Database
```bash
# Restore from backup
psql tiffindb < backup_20251107_161500.sql
```

### 3. Revert Code Changes
```bash
# If needed, revert entity changes
git checkout HEAD~1 -- src/main/java/com/tiffin/user/model/User.java
```

## Verification Commands

### Check Schema Status
```sql
-- Verify column existence
SELECT column_name, is_nullable, column_default 
FROM information_schema.columns 
WHERE table_name = 'users' 
ORDER BY ordinal_position;
```

### Check Data Integrity
```sql
-- Count null values
SELECT 
  COUNT(*) as total_users,
  COUNT(first_name) as has_first_name,
  COUNT(last_name) as has_last_name,
  COUNT(CASE WHEN phone_verified = true THEN 1 END) as phone_verified_count
FROM users;
```

---

**Note**: This migration strategy prioritizes application startup stability while maintaining data integrity. The nullable column approach ensures existing users can continue using the system while new data validation rules apply to new records.