package com.smallrestaurant.game.model;
public class TableState {
    private Long tableId;
    private int positionX;
    private int positionY;
    private boolean unlocked;
    private String status;
    private String guestName;
    private Integer patience;
    private String requiredDish;
    public TableState() {}
    public TableState(Long tableId, int positionX, int positionY, boolean unlocked) {
        this.tableId = tableId;
        this.positionX = positionX;
        this.positionY = positionY;
        this.unlocked = unlocked;
        this.status = unlocked ? "empty" : "locked";
    }
    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }
    public int getPositionX() { return positionX; }
    public void setPositionX(int positionX) { this.positionX = positionX; }
    public int getPositionY() { return positionY; }
    public void setPositionY(int positionY) { this.positionY = positionY; }
    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    public Integer getPatience() { return patience; }
    public void setPatience(Integer patience) { this.patience = patience; }
    public String getRequiredDish() { return requiredDish; }
    public void setRequiredDish(String requiredDish) { this.requiredDish = requiredDish; }
}
