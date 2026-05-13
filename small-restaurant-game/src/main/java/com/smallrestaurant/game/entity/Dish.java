package com.smallrestaurant.game.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dishes")
public class Dish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private int basePrice;
    private int cookTimeSeconds;
    @Column(columnDefinition = "boolean default false")
    private boolean isUnlockedByDefault = false;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
