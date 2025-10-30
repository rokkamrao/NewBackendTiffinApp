package com.tiffin.api.config;

import com.tiffin.api.dish.model.Dish;
import com.tiffin.api.dish.model.DietType;
import com.tiffin.api.dish.repository.DishRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final DishRepository dishRepository;
    
    @Override
    public void run(String... args) {
        try {
            long count = dishRepository.count();
            log.info("📊 Current dish count in database: {}", count);
            
            if (count == 0) {
                log.info("🍽️ Initializing sample dishes...");
                initializeDishes();
                log.info("✅ Sample dishes added successfully! Total: {}", dishRepository.count());
            } else {
                log.info("📊 Database already contains {} dishes - skipping initialization", count);
            }
        } catch (Exception e) {
            log.error("❌ Failed to initialize dishes: {}", e.getMessage(), e);
        }
    }
    
    private void initializeDishes() {
        // Vegetarian Dishes
        dishRepository.save(Dish.builder()
                .name("Paneer Butter Masala")
                .description("Creamy tomato gravy with soft paneer cubes")
                .price(new BigDecimal("150.00"))
                .type(DietType.VEG)
                .cuisine("North Indian")
                .tags(Set.of("Popular", "Healthy"))
                .dietaryTags(Set.of("Protein-rich", "Vegetarian"))
                .calories(320)
                .preparationTime(25)
                .spicyLevel(2)
                .rating(4.5)
                .reviewCount(245)
                .available(true)
                .build());
        
        dishRepository.save(Dish.builder()
                .name("Dal Tadka")
                .description("Yellow lentils tempered with spices")
                .price(new BigDecimal("100.00"))
                .type(DietType.VEG)
                .cuisine("North Indian")
                .tags(Set.of("Healthy", "Low Cal"))
                .dietaryTags(Set.of("High-protein", "Vegan"))
                .calories(180)
                .preparationTime(30)
                .spicyLevel(1)
                .rating(4.3)
                .reviewCount(189)
                .available(true)
                .build());
        
        dishRepository.save(Dish.builder()
                .name("Palak Paneer")
                .description("Spinach curry with cottage cheese")
                .price(new BigDecimal("140.00"))
                .type(DietType.VEG)
                .cuisine("North Indian")
                .tags(Set.of("Healthy"))
                .dietaryTags(Set.of("Iron-rich", "Protein-rich"))
                .calories(250)
                .preparationTime(25)
                .spicyLevel(1)
                .rating(4.4)
                .reviewCount(156)
                .available(true)
                .build());
        
        dishRepository.save(Dish.builder()
                .name("Chole Bhature")
                .description("Spicy chickpea curry with fried bread")
                .price(new BigDecimal("120.00"))
                .type(DietType.VEG)
                .cuisine("North Indian")
                .tags(Set.of("Popular", "Spicy"))
                .dietaryTags(Set.of("High-protein", "Vegan"))
                .calories(450)
                .preparationTime(35)
                .spicyLevel(3)
                .rating(4.6)
                .reviewCount(298)
                .available(true)
                .build());
        
        dishRepository.save(Dish.builder()
                .name("Veg Biryani")
                .description("Aromatic rice with mixed vegetables")
                .price(new BigDecimal("130.00"))
                .type(DietType.VEG)
                .cuisine("Hyderabadi")
                .tags(Set.of("Popular"))
                .dietaryTags(Set.of("Vegan"))
                .calories(380)
                .preparationTime(40)
                .spicyLevel(2)
                .rating(4.5)
                .reviewCount(312)
                .available(true)
                .build());
        
        dishRepository.save(Dish.builder()
                .name("Masala Dosa")
                .description("Crispy rice crepe with spiced potato filling")
                .price(new BigDecimal("90.00"))
                .type(DietType.VEG)
                .cuisine("South Indian")
                .tags(Set.of("Popular", "Low Cal"))
                .dietaryTags(Set.of("Vegan", "Gluten-free"))
                .calories(220)
                .preparationTime(20)
                .spicyLevel(2)
                .rating(4.7)
                .reviewCount(421)
                .available(true)
                .build());
        
        dishRepository.save(Dish.builder()
                .name("Aloo Gobi")
                .description("Potato and cauliflower dry curry")
                .price(new BigDecimal("110.00"))
                .type(DietType.VEG)
                .cuisine("North Indian")
                .tags(Set.of("Healthy", "Low Cal"))
                .dietaryTags(Set.of("Vegan", "Low-calorie"))
                .calories(160)
                .preparationTime(25)
                .spicyLevel(2)
                .rating(4.2)
                .reviewCount(134)
                .available(true)
                .build());
        
        // Non-Vegetarian Dishes
        dishRepository.save(Dish.builder()
                .name("Butter Chicken")
                .description("Tender chicken in rich tomato cream sauce")
                .price(new BigDecimal("180.00"))
                .type(DietType.NON_VEG)
                .cuisine("North Indian")
                .tags(Set.of("Popular"))
                .dietaryTags(Set.of("High-protein"))
                .calories(420)
                .preparationTime(35)
                .spicyLevel(2)
                .rating(4.8)
                .reviewCount(567)
                .available(true)
                .build());
        
        dishRepository.save(Dish.builder()
                .name("Chicken Biryani")
                .description("Fragrant basmati rice with marinated chicken")
                .price(new BigDecimal("170.00"))
                .type(DietType.NON_VEG)
                .cuisine("Hyderabadi")
                .tags(Set.of("Popular", "Spicy"))
                .dietaryTags(Set.of("High-protein"))
                .calories(480)
                .preparationTime(45)
                .spicyLevel(3)
                .rating(4.7)
                .reviewCount(623)
                .available(true)
                .build());
        
        dishRepository.save(Dish.builder()
                .name("Fish Curry")
                .description("Coastal style fish in coconut gravy")
                .price(new BigDecimal("190.00"))
                .type(DietType.NON_VEG)
                .cuisine("South Indian")
                .tags(Set.of("Healthy", "Spicy"))
                .dietaryTags(Set.of("Omega-3", "Low-carb"))
                .calories(280)
                .preparationTime(30)
                .spicyLevel(3)
                .rating(4.5)
                .reviewCount(178)
                .available(true)
                .build());
        
        dishRepository.save(Dish.builder()
                .name("Mutton Rogan Josh")
                .description("Aromatic lamb curry with Kashmiri spices")
                .price(new BigDecimal("220.00"))
                .type(DietType.NON_VEG)
                .cuisine("Kashmiri")
                .tags(Set.of("Spicy"))
                .dietaryTags(Set.of("High-protein"))
                .calories(520)
                .preparationTime(50)
                .spicyLevel(3)
                .rating(4.6)
                .reviewCount(234)
                .available(true)
                .build());
        
        dishRepository.save(Dish.builder()
                .name("Tandoori Chicken")
                .description("Marinated chicken grilled in clay oven")
                .price(new BigDecimal("160.00"))
                .type(DietType.NON_VEG)
                .cuisine("North Indian")
                .tags(Set.of("Healthy", "Popular"))
                .dietaryTags(Set.of("High-protein", "Low-carb"))
                .calories(310)
                .preparationTime(40)
                .spicyLevel(2)
                .rating(4.6)
                .reviewCount(389)
                .available(true)
                .build());
        
        dishRepository.save(Dish.builder()
                .name("Chicken Tikka Masala")
                .description("Grilled chicken in creamy tomato sauce")
                .price(new BigDecimal("175.00"))
                .type(DietType.NON_VEG)
                .cuisine("North Indian")
                .tags(Set.of("Popular"))
                .dietaryTags(Set.of("High-protein"))
                .calories(390)
                .preparationTime(35)
                .spicyLevel(2)
                .rating(4.7)
                .reviewCount(445)
                .available(true)
                .build());
        
        dishRepository.save(Dish.builder()
                .name("Prawn Masala")
                .description("Juicy prawns in spicy onion-tomato gravy")
                .price(new BigDecimal("210.00"))
                .type(DietType.NON_VEG)
                .cuisine("Coastal")
                .tags(Set.of("Spicy"))
                .dietaryTags(Set.of("High-protein", "Low-carb"))
                .calories(260)
                .preparationTime(25)
                .spicyLevel(3)
                .rating(4.5)
                .reviewCount(167)
                .available(true)
                .build());
        
        dishRepository.save(Dish.builder()
                .name("Egg Curry")
                .description("Boiled eggs in rich onion-tomato gravy")
                .price(new BigDecimal("110.00"))
                .type(DietType.NON_VEG)
                .cuisine("North Indian")
                .tags(Set.of("Healthy"))
                .dietaryTags(Set.of("High-protein"))
                .calories(240)
                .preparationTime(20)
                .spicyLevel(2)
                .rating(4.3)
                .reviewCount(201)
                .available(true)
                .build());
    }
}
