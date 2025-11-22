package com.tiffin.config;

import com.tiffin.menu.model.Dish;
import com.tiffin.menu.repository.DishRepository;
import com.tiffin.user.model.Role;
import com.tiffin.user.model.User;
import com.tiffin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SampleDataInitializer {

    @Bean
    @SuppressWarnings("null")
    CommandLineRunner initSampleData(DishRepository dishRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Initialize dishes
            if (dishRepository.count() == 0) {
                log.info("Creating sample dishes...");
                
                // Breakfast items
                dishRepository.save(new Dish(null, "Masala Dosa", "Crispy crepe with spicy potato filling", 120.0, "/images/masala-dosa.jpg",
                        true, true, true, true, Dish.Category.BREAKFAST, true, true, 320, 8.5, 45.0, 12.0));
                        
                dishRepository.save(new Dish(null, "Upma", "Semolina porridge with vegetables", 80.0, "/images/upma.jpg",
                        true, true, false, true, Dish.Category.BREAKFAST, true, false, 250, 6.0, 35.0, 8.0));
                        
                dishRepository.save(new Dish(null, "Idli Sambhar", "Steamed rice cakes with lentil soup", 90.0, "/images/idli-sambhar.jpg",
                        true, true, true, true, Dish.Category.BREAKFAST, true, true, 180, 8.0, 25.0, 4.0));

                // Lunch items
                dishRepository.save(new Dish(null, "Dal Rice Combo", "Yellow lentils with steamed rice and pickle", 150.0, "/images/dal-rice.jpg",
                        true, true, true, true, Dish.Category.LUNCH, true, true, 400, 15.0, 60.0, 8.0));
                        
                dishRepository.save(new Dish(null, "Rajma Chawal", "Kidney beans curry with basmati rice", 180.0, "/images/rajma-chawal.jpg",
                        true, true, true, true, Dish.Category.LUNCH, true, false, 450, 18.0, 65.0, 10.0));
                        
                dishRepository.save(new Dish(null, "Paneer Butter Masala", "Cottage cheese in rich tomato gravy with naan", 220.0, "/images/paneer-butter-masala.jpg",
                        true, false, false, true, Dish.Category.LUNCH, true, true, 520, 20.0, 35.0, 25.0));

                // Dinner items
                dishRepository.save(new Dish(null, "Chole Bhature", "Spicy chickpeas with fried bread", 160.0, "/images/chole-bhature.jpg",
                        true, true, false, true, Dish.Category.DINNER, true, false, 480, 16.0, 55.0, 18.0));
                        
                dishRepository.save(new Dish(null, "Aloo Gobi", "Cauliflower and potato curry with roti", 140.0, "/images/aloo-gobi.jpg",
                        true, true, true, true, Dish.Category.DINNER, true, false, 320, 8.0, 45.0, 12.0));
                        
                dishRepository.save(new Dish(null, "Mixed Dal Tadka", "Five lentil curry with jeera rice", 130.0, "/images/dal-tadka.jpg",
                        true, true, true, true, Dish.Category.DINNER, true, true, 380, 18.0, 55.0, 8.0));

                // Snacks
                dishRepository.save(new Dish(null, "Samosa", "Crispy pastry with spiced potato filling (2 pieces)", 60.0, "/images/samosa.jpg",
                        true, true, false, true, Dish.Category.SNACK, true, true, 240, 6.0, 28.0, 12.0));
                        
                dishRepository.save(new Dish(null, "Dhokla", "Steamed gram flour cake (4 pieces)", 70.0, "/images/dhokla.jpg",
                        true, true, true, true, Dish.Category.SNACK, true, false, 180, 8.0, 25.0, 5.0));
                        
                dishRepository.save(new Dish(null, "Pav Bhaji", "Mixed vegetable curry with bread rolls", 140.0, "/images/pav-bhaji.jpg",
                        true, true, false, true, Dish.Category.SNACK, true, true, 420, 12.0, 50.0, 15.0));
                
                log.info("Sample dishes created successfully!");
            } else {
                log.info("Sample dishes already exist, skipping dish initialization.");
            }

            // Initialize users
            if (userRepository.count() == 0) {
                log.info("Creating sample users...");

                // Create regular users
                userRepository.save(User.builder()
                        .email("john.customer@example.com")
                        .password(passwordEncoder.encode("password123"))
                        .firstName("John")
                        .lastName("Customer")
                        .phoneNumber("9876543210")
                        .role(Role.USER)
                        .active(true)
                        .emailVerified(true)
                        .phoneVerified(true)
                        .preferredLanguage("en")
                        .createdAt(LocalDateTime.now().minusDays(30))
                        .lastLoginAt(LocalDateTime.now().minusHours(2))
                        .build());

                userRepository.save(User.builder()
                        .email("priya.premium@example.com")
                        .password(passwordEncoder.encode("password123"))
                        .firstName("Priya")
                        .lastName("Premium")
                        .phoneNumber("9876543211")
                        .role(Role.PREMIUM_USER)
                        .active(true)
                        .emailVerified(true)
                        .phoneVerified(true)
                        .preferredLanguage("hi")
                        .createdAt(LocalDateTime.now().minusDays(15))
                        .lastLoginAt(LocalDateTime.now().minusMinutes(30))
                        .build());

                // Create admin
                userRepository.save(User.builder()
                        .email("admin@tiffin.app")
                        .password(passwordEncoder.encode("admin123"))
                        .firstName("Admin")
                        .lastName("User")
                        .phoneNumber("9876543212")
                        .role(Role.ADMIN)
                        .active(true)
                        .emailVerified(true)
                        .phoneVerified(true)
                        .preferredLanguage("en")
                        .createdAt(LocalDateTime.now().minusDays(60))
                        .lastLoginAt(LocalDateTime.now().minusMinutes(10))
                        .build());

                // Create super admin
                userRepository.save(User.builder()
                        .email("superadmin@tiffin.app")
                        .password(passwordEncoder.encode("superadmin123"))
                        .firstName("Super")
                        .lastName("Admin")
                        .phoneNumber("9876543213")
                        .role(Role.SUPER_ADMIN)
                        .active(true)
                        .emailVerified(true)
                        .phoneVerified(true)
                        .preferredLanguage("en")
                        .createdAt(LocalDateTime.now().minusDays(90))
                        .lastLoginAt(LocalDateTime.now().minusMinutes(5))
                        .build());

                // Create delivery person
                userRepository.save(User.builder()
                        .email("delivery@tiffin.app")
                        .password(passwordEncoder.encode("delivery123"))
                        .firstName("Delivery")
                        .lastName("Person")
                        .phoneNumber("9876543214")
                        .role(Role.DELIVERY_PERSON)
                        .active(true)
                        .emailVerified(true)
                        .phoneVerified(true)
                        .preferredLanguage("te")
                        .createdAt(LocalDateTime.now().minusDays(45))
                        .lastLoginAt(LocalDateTime.now().minusHours(1))
                        .build());

                // Create restaurant partner
                userRepository.save(User.builder()
                        .email("partner@tiffin.app")
                        .password(passwordEncoder.encode("partner123"))
                        .firstName("Restaurant")
                        .lastName("Partner")
                        .phoneNumber("9876543215")
                        .role(Role.RESTAURANT_PARTNER)
                        .active(true)
                        .emailVerified(true)
                        .phoneVerified(true)
                        .preferredLanguage("en")
                        .createdAt(LocalDateTime.now().minusDays(20))
                        .lastLoginAt(LocalDateTime.now().minusHours(3))
                        .build());

                // Create test user with simple phone number for testing
                userRepository.save(User.builder()
                        .email("test@tiffin.app")
                        .password(passwordEncoder.encode("test123"))
                        .firstName("Test")
                        .lastName("User")
                        .phoneNumber("9999999999")
                        .role(Role.USER)
                        .active(true)
                        .emailVerified(true)
                        .phoneVerified(true)
                        .preferredLanguage("en")
                        .createdAt(LocalDateTime.now().minusDays(1))
                        .lastLoginAt(LocalDateTime.now().minusMinutes(15))
                        .build());

                log.info("Sample users created successfully!");
                log.info("Test accounts created:");
                log.info("  Regular User: john.customer@example.com / password123");
                log.info("  Premium User: priya.premium@example.com / password123");
                log.info("  Admin: admin@tiffin.app / admin123");
                log.info("  Super Admin: superadmin@tiffin.app / superadmin123");
                log.info("  Delivery: delivery@tiffin.app / delivery123");
                log.info("  Partner: partner@tiffin.app / partner123");
                log.info("  Test User: test@tiffin.app / test123 (Phone: 9999999999)");

            } else {
                log.info("Sample users already exist, skipping user initialization.");
            }
        };
    }
}