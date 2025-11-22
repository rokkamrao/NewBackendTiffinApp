package com.tiffin.subscription.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Daily, Weekly, Monthly
    private String description;
    private double price;
    private int durationDays;
    private int totalMeals;

    // Discount percentage (e.g. 10 for 10%)
    private double discountPercentage;
}
