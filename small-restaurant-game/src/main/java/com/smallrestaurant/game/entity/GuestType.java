package com.smallrestaurant.game.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "t_guest_type")
public class GuestType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guest_type_id")
    private Long guestTypeId;

    @Column(name = "guest_name", nullable = false, length = 50)
    private String guestName;

    @Column(name = "dish_id", nullable = false)
    private Long dishId;

    @Column(name = "avatar", length = 100)
    private String avatar;

    @Column(name = "unlock_level", columnDefinition = "int default 1")
    private int unlockLevel = 1;

    public GuestType() {}

    public Long getGuestTypeId() { return guestTypeId; }
    public void setGuestTypeId(Long guestTypeId) { this.guestTypeId = guestTypeId; }
    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    public Long getDishId() { return dishId; }
    public void setDishId(Long dishId) { this.dishId = dishId; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public int getUnlockLevel() { return unlockLevel; }
    public void setUnlockLevel(int unlockLevel) { this.unlockLevel = unlockLevel; }
}
