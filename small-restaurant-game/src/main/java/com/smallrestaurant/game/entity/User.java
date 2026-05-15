package com.smallrestaurant.game.entity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(nullable = false)
    private String passwordHash;
    private String nickname;
    @Column(columnDefinition = "bigint default 0")
    private Long balance = 0L;
    @Column(columnDefinition = "decimal(10,2) default 0.00")
    private BigDecimal redPacket = BigDecimal.ZERO;
    @Column(columnDefinition = "int default 0")
    private Integer likeCount = 0;
    @Column(columnDefinition = "int default 0")
    private Integer totalGuests = 0;
    @Column(columnDefinition = "int default 0")
    private int exp = 0;
    @Column(columnDefinition = "int default 1")
    private int level = 1;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    public User() {}
    public User(Long id, String username, String passwordHash, String nickname, Long balance, BigDecimal redPacket, Integer likeCount, Integer totalGuests, int exp, int level, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.balance = balance;
        this.redPacket = redPacket;
        this.likeCount = likeCount;
        this.totalGuests = totalGuests;
        this.exp = exp;
        this.level = level;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public Long getBalance() { return balance; }
    public void setBalance(Long balance) { this.balance = balance; }
    public BigDecimal getRedPacket() { return redPacket; }
    public void setRedPacket(BigDecimal redPacket) { this.redPacket = redPacket; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getTotalGuests() { return totalGuests; }
    public void setTotalGuests(Integer totalGuests) { this.totalGuests = totalGuests; }
    public int getExp() { return exp; }
    public void setExp(int exp) { this.exp = exp; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
