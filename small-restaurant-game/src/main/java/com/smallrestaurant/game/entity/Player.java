package com.smallrestaurant.game.entity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "t_player")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "player_id")
    private Long playerId;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(nullable = false)
    private String passwordHash;
    private String nickname;
    @Column(columnDefinition = "int default 1")
    private int level = 1;
    @Column(columnDefinition = "int default 100")
    private int balance = 100;
    @Column(columnDefinition = "int default 0")
    private int exp = 0;
    @Column(precision = 10, scale = 2, columnDefinition = "decimal(10,2) default 0.00")
    private BigDecimal redPacket = BigDecimal.ZERO;
    @Column(columnDefinition = "int default 0")
    private int likeCount = 0;
    @Column(columnDefinition = "int default 0")
    private int totalGuests = 0;
    @Column(columnDefinition = "boolean default false")
    private boolean nextDoubleReward = false;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    public Player() {}
    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getBalance() { return balance; }
    public void setBalance(int balance) { this.balance = balance; }
    public int getExp() { return exp; }
    public void setExp(int exp) { this.exp = exp; }
    public BigDecimal getRedPacket() { return redPacket; }
    public void setRedPacket(BigDecimal redPacket) { this.redPacket = redPacket; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public int getTotalGuests() { return totalGuests; }
    public void setTotalGuests(int totalGuests) { this.totalGuests = totalGuests; }
    public boolean isNextDoubleReward() { return nextDoubleReward; }
    public void setNextDoubleReward(boolean nextDoubleReward) { this.nextDoubleReward = nextDoubleReward; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
