package com.smallrestaurant.game.entity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
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
    public Dish() {}
    public Dish(Long id, String name, int basePrice, int cookTimeSeconds, boolean isUnlockedByDefault, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.basePrice = basePrice;
        this.cookTimeSeconds = cookTimeSeconds;
        this.isUnlockedByDefault = isUnlockedByDefault;
        this.createdAt = createdAt;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getBasePrice() { return basePrice; }
    public void setBasePrice(int basePrice) { this.basePrice = basePrice; }
    public int getCookTimeSeconds() { return cookTimeSeconds; }
    public void setCookTimeSeconds(int cookTimeSeconds) { this.cookTimeSeconds = cookTimeSeconds; }
    public boolean isUnlockedByDefault() { return isUnlockedByDefault; }
    public void setUnlockedByDefault(boolean unlockedByDefault) { isUnlockedByDefault = unlockedByDefault; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
