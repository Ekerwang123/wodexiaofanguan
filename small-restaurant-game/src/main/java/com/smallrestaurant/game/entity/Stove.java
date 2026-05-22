package com.smallrestaurant.game.entity;
import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
@Entity
@Table(name = "t_stove")
@DynamicUpdate
public class Stove {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stove_id")
    private Long stoveId;
    @Column(nullable = false)
    private Long playerId;
    @Column(nullable = false)
    private int stoveIndex;
    @Column(name = "is_unlocked", columnDefinition = "boolean default false")
    private boolean unlocked = false;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    public Stove() {}
    public Stove(Long playerId, int stoveIndex, boolean unlocked) {
        this.playerId = playerId;
        this.stoveIndex = stoveIndex;
        this.unlocked = unlocked;
    }
    public Long getStoveId() { return stoveId; }
    public void setStoveId(Long stoveId) { this.stoveId = stoveId; }
    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }
    public int getStoveIndex() { return stoveIndex; }
    public void setStoveIndex(int stoveIndex) { this.stoveIndex = stoveIndex; }
    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
