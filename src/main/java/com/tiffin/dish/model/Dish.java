package com.tiffin.dish.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "dishes")
public class Dish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    private String imageUrl;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "dish_tags", joinColumns = @JoinColumn(name = "dish_id"))
    @Column(name = "tag")
    private Set<String> tags;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "dish_dietary_tags", joinColumns = @JoinColumn(name = "dish_id"))
    @Column(name = "dietary_tag")
    private Set<String> dietaryTags;
    
    @Column
    private String cuisine;
    
    @Column
    @lombok.Builder.Default
    private boolean available = true;
    private Integer calories;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DietType type;
    
    private Double rating;
    private Integer reviewCount;
    private Integer preparationTime;
    private Integer spicyLevel;
    
    // Add missing getter for price
    public BigDecimal getPrice() {
        return price;
    }
}
