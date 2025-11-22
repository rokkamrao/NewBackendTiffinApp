package com.tiffin.subscription.model;

import com.tiffin.user.model.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private Plan plan;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    private int mealsRemaining;

    // Preference for this subscription
    @Enumerated(EnumType.STRING)
    private MealTime mealTime; // LUNCH, DINNER, BOTH

    private boolean hasSmartTiffin;

    public enum Status {
        ACTIVE, PAUSED, EXPIRED, CANCELLED
    }

    public enum MealTime {
        LUNCH, DINNER, BOTH
    }
}
