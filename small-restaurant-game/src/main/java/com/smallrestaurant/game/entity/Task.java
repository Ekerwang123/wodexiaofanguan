package com.smallrestaurant.game.entity;
import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;
import java.math.BigDecimal;
@Entity
@Table(name = "t_task")
@DynamicUpdate
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long taskId;
    @Column(name = "task_type", nullable = false)
    private int taskType;
    @Column(name = "task_desc", nullable = false)
    private String taskDesc;
    @Column(name = "target_count", nullable = false)
    private int targetCount;
    @Column(name = "reward_type", nullable = false)
    private int rewardType;
    @Column(name = "reward_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal rewardAmount;
    public Task() {}
    public Task(int taskType, String taskDesc, int targetCount, int rewardType, BigDecimal rewardAmount) {
        this.taskType = taskType;
        this.taskDesc = taskDesc;
        this.targetCount = targetCount;
        this.rewardType = rewardType;
        this.rewardAmount = rewardAmount;
    }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public int getTaskType() { return taskType; }
    public void setTaskType(int taskType) { this.taskType = taskType; }
    public String getTaskDesc() { return taskDesc; }
    public void setTaskDesc(String taskDesc) { this.taskDesc = taskDesc; }
    public int getTargetCount() { return targetCount; }
    public void setTargetCount(int targetCount) { this.targetCount = targetCount; }
    public int getRewardType() { return rewardType; }
    public void setRewardType(int rewardType) { this.rewardType = rewardType; }
    public BigDecimal getRewardAmount() { return rewardAmount; }
    public void setRewardAmount(BigDecimal rewardAmount) { this.rewardAmount = rewardAmount; }
}
