# Compilation Troubleshooting Guide

## Overview
This document provides solutions for common Java compilation errors encountered in the Spring Boot application.

## Resolved Issues

### 1. DTO Class Separation Errors

#### Error Message
```
[ERROR] class NewsletterSubscriptionRequest is public, should be declared in a file named NewsletterSubscriptionRequest.java
[ERROR] duplicate class: com.tiffin.newsletter.dto.NewsletterSubscriptionRequest
```

#### Root Cause
Multiple public classes in a single Java file violates Java naming conventions.

#### Solution
Split combined DTO files into separate files:

**Before**: `NewsletterDTOs.java` (Combined file)
```java
public class NewsletterSubscriptionRequest { ... }
public class NewsletterSubscriptionResponse { ... }
public class NewsletterRequest { ... }
public class NewsletterResponse { ... }
```

**After**: Separate files
- `NewsletterSubscriptionRequest.java`
- `NewsletterSubscriptionResponse.java` 
- `NewsletterRequest.java`
- `NewsletterResponse.java`

#### Implementation Steps
1. Create individual DTO files with proper package declarations
2. Remove the combined DTO file
3. Update imports in dependent classes if needed

### 2. HQL Query Syntax Errors

#### Error Message
```
[ERROR] Select item at position 1 in select list has no alias (aliases are required in CTEs and in subqueries occurring in from clause)
```

#### Root Cause
Invalid subquery syntax in JPA @Query annotation.

#### Solution
Rewrite query using proper JPA syntax:

**Before**: Invalid subquery alias
```java
@Query("SELECT COUNT(*) FROM (SELECT la.sessionId FROM LandingAnalytics la WHERE la.eventType = 'PAGE_VIEW' GROUP BY la.sessionId HAVING COUNT(la) = 1) as singlePageSessions")
```

**After**: Proper JPA query
```java
@Query("SELECT COUNT(DISTINCT la.sessionId) FROM LandingAnalytics la WHERE la.eventType = 'PAGE_VIEW' " +
       "AND la.sessionId IN (SELECT la2.sessionId FROM LandingAnalytics la2 WHERE la2.eventType = 'PAGE_VIEW' " +
       "GROUP BY la2.sessionId HAVING COUNT(la2) = 1)")
```

### 3. Missing Bean Dependencies

#### Error Message
```
[ERROR] No qualifying bean of type 'org.springframework.security.crypto.password.PasswordEncoder' available
```

#### Root Cause
UserService constructor requires PasswordEncoder bean that wasn't configured.

#### Solution
Create comprehensive SecurityConfig with required beans:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Configuration...
    }
}
```

### 4. Column Definition Errors

#### Error Message
```
[ERROR] scale has no meaning for SQL floating point types
```

#### Root Cause
Using `scale` parameter with Double/Float column types is invalid.

#### Solution
Remove precision/scale from floating-point columns:

**Before**: Invalid for Double
```java
@Column(precision = 10, scale = 8)
private Double latitude;
```

**After**: Simple column definition
```java
@Column
private Double latitude;
```

## Prevention Strategies

### 1. Code Organization
- **One public class per file**: Follow Java naming conventions
- **Proper package structure**: Organize related classes together
- **Clear naming**: Use descriptive class and file names

### 2. Query Validation
- **Test HQL queries**: Validate syntax before deployment
- **Use proper aliases**: Follow JPA query conventions
- **Avoid subquery aliases**: Use standard JPA syntax patterns

### 3. Dependency Management
- **Configure required beans**: Ensure all dependencies are available
- **Use @Configuration classes**: Organize bean definitions properly
- **Document dependencies**: Clear JavaDoc for complex dependencies

### 4. Database Design
- **Know column types**: Understand SQL type limitations
- **Migration planning**: Design for existing data compatibility
- **Test schemas**: Validate definitions before production

## Debugging Tools

### 1. Maven Compilation
```bash
# Verbose compilation output
mvn compile -X

# Skip tests for faster compilation
mvn compile -DskipTests

# Clean and compile
mvn clean compile
```

### 2. IDE Assistance
```bash
# IntelliJ IDEA: Build > Rebuild Project
# Eclipse: Project > Clean > Build
# VS Code: Java: Rebuild Projects
```

### 3. Application Testing
```bash
# Test Spring Boot startup
mvn spring-boot:run

# Check specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Quality Assurance

### 1. Pre-commit Checks
```bash
# Compile before commit
mvn compile

# Run tests
mvn test

# Check formatting
mvn spotless:check
```

### 2. CI/CD Pipeline
```yaml
# .github/workflows/build.yml
- name: Compile
  run: mvn compile -DskipTests

- name: Test
  run: mvn test

- name: Package
  run: mvn package -DskipTests
```

### 3. Code Review Checklist
- [ ] Single public class per file
- [ ] Proper package declarations
- [ ] Valid JPA query syntax
- [ ] Required beans configured
- [ ] Appropriate column definitions
- [ ] Migration-safe changes

## Common Patterns

### 1. DTO Structure
```java
// Good: Single responsibility
public class UserRequest {
    @NotBlank
    private String email;
    // ... getters/setters
}

// Avoid: Multiple classes in one file
public class UserDTOs {
    public static class UserRequest { ... }
    public static class UserResponse { ... }
}
```

### 2. Repository Queries
```java
// Good: Standard JPA syntax
@Query("SELECT u FROM User u WHERE u.active = true")
List<User> findActiveUsers();

// Good: Proper subquery
@Query("SELECT u FROM User u WHERE u.id IN (SELECT o.userId FROM Order o WHERE o.status = 'COMPLETED')")
List<User> findUsersWithCompletedOrders();
```

### 3. Configuration Beans
```java
// Good: Clear configuration
@Configuration
public class DatabaseConfig {
    
    @Bean
    @Primary
    public DataSource primaryDataSource() {
        // Configuration
    }
}
```

## Emergency Fixes

### 1. Quick Class Separation
```bash
# Script to split DTO files
#!/bin/bash
# extract_classes.sh <combined_file> <output_dir>
```

### 2. Disable Problematic Features
```yaml
# application.yml - temporary workaround
spring:
  jpa:
    hibernate:
      ddl-auto: none  # Skip schema validation
```

### 3. Rollback Strategy
```bash
# Revert to last working state
git checkout HEAD~1 -- problematic_file.java
mvn clean compile
```

---

**Best Practice**: Always test compilation after significant changes and maintain clean, well-organized code structure to prevent these issues.