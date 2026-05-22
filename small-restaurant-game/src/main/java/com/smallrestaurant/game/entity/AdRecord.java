package com.smallrestaurant.game.entity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
@Entity
@Table(name = "t_ad_record")
public class AdRecord {
    @Id
    @Column(length = 64)
    private String adId;
    @Column(name = "player_id", nullable = false)
    private Long playerId;
    @Column(name = "ad_type", nullable = false, length = 20)
    private String adType;
    @Column(name = "reward_amount", nullable = false, columnDefinition = "int default 0")
    private int rewardAmount = 0;
    @Column(nullable = false, columnDefinition = "int default 0")
    private int status = 0;
    @Column(length = 500)
    private String params;
    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
    public AdRecord() {}
    public String getAdId() { return adId; }
    public void setAdId(String adId) { this.adId = adId; }
    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }
    public String getAdType() { return adType; }
    public void setAdType(String adType) { this.adType = adType; }
    public int getRewardAmount() { return rewardAmount; }
    public void setRewardAmount(int rewardAmount) { this.rewardAmount = rewardAmount; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getParams() { return params; }
    public void setParams(String params) { this.params = params; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
