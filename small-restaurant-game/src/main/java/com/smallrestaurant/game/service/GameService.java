package com.smallrestaurant.game.service;
import com.smallrestaurant.game.entity.Dish;
import com.smallrestaurant.game.entity.GuestType;
import com.smallrestaurant.game.entity.Player;
import com.smallrestaurant.game.entity.Stove;
import com.smallrestaurant.game.model.TableState;
import com.smallrestaurant.game.repository.DishRepository;
import com.smallrestaurant.game.repository.GuestTypeRepository;
import com.smallrestaurant.game.repository.PlayerRepository;
import com.smallrestaurant.game.repository.StoveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Service
public class GameService {
    @Autowired
    private StoveRepository stoveRepository;
    @Autowired
    private DishRepository dishRepository;
    @Autowired
    private GuestTypeRepository guestTypeRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private TableService tableService;
    @Autowired
    @Lazy
    private TaskService taskService;
    private final Map<Long, CookingTask> cookingCache = new ConcurrentHashMap<>();
    public Map<String, Object> guestSit(Long playerId, Long tableId, String dishName) {
        TableState tableState = tableService.getTableState(playerId, tableId);
        if (tableState == null) {
            throw new RuntimeException("餐桌不存在");
        }
        if (!tableState.isUnlocked()) {
            throw new RuntimeException("餐桌未解锁");
        }
        if (!"empty".equals(tableState.getStatus())) {
            throw new RuntimeException("餐桌非空闲状态");
        }
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("玩家不存在"));
        List<GuestType> availableGuests = guestTypeRepository.findByUnlockLevelLessThanEqual(player.getLevel());
        if (availableGuests.isEmpty()) {
            throw new RuntimeException("没有可用的客人类型");
        }
        GuestType guestType = availableGuests.get((int) (Math.random() * availableGuests.size()));
        Dish dish = dishRepository.findById(guestType.getDishId())
                .orElseThrow(() -> new RuntimeException("菜品不存在"));
        tableState.setStatus("waiting");
        tableState.setGuestName(guestType.getGuestName());
        tableState.setRequiredDish(dish.getName());
        tableService.updateTableState(playerId, tableId, tableState);
        Map<String, Object> result = new HashMap<>();
        result.put("tableId", tableId);
        result.put("guestName", guestType.getGuestName());
        result.put("guestTypeId", guestType.getGuestTypeId());
        result.put("avatar", guestType.getAvatar());
        result.put("needDish", dish.getName());
        result.put("needDishId", dish.getId());
        result.put("status", "waiting");
        return result;
    }

    public Map<String, Object> inviteBatchGuests(Long playerId, int count) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("玩家不存在"));
        List<GuestType> availableGuests = guestTypeRepository.findByUnlockLevelLessThanEqual(player.getLevel());
        if (availableGuests.isEmpty()) {
            throw new RuntimeException("没有可用的客人类型");
        }
        List<TableState> allTables = tableService.getPlayerTables(playerId);
        List<TableState> emptyTables = new ArrayList<>();
        for (TableState t : allTables) {
            if (t.isUnlocked() && "empty".equals(t.getStatus())) {
                emptyTables.add(t);
            }
        }
        if (emptyTables.isEmpty()) {
            throw new RuntimeException("没有空闲餐桌");
        }
        int inviteCount = Math.min(count, emptyTables.size());
        List<Map<String, Object>> guests = new ArrayList<>();
        for (int i = 0; i < inviteCount; i++) {
            TableState table = emptyTables.get(i);
            GuestType guestType = availableGuests.get((int) (Math.random() * availableGuests.size()));
            Dish dish = dishRepository.findById(guestType.getDishId())
                    .orElseThrow(() -> new RuntimeException("菜品不存在"));
            table.setStatus("waiting");
            table.setGuestName(guestType.getGuestName());
            table.setRequiredDish(dish.getName());
            tableService.updateTableState(playerId, table.getTableId(), table);
            Map<String, Object> guestInfo = new HashMap<>();
            guestInfo.put("tableId", table.getTableId());
            guestInfo.put("guestName", guestType.getGuestName());
            guestInfo.put("avatar", guestType.getAvatar());
            guestInfo.put("needDish", dish.getName());
            guestInfo.put("needDishId", dish.getId());
            guests.add(guestInfo);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("invitedCount", inviteCount);
        result.put("guests", guests);
        return result;
    }
    public Map<String, Object> accelerateCook(Long playerId, Long stoveId) {
        CookingTask task = cookingCache.get(stoveId);
        if (task == null) {
            throw new RuntimeException("灶台上没有正在烹饪的菜品");
        }
        synchronized (task) {
            if (!task.getPlayerId().equals(playerId)) {
                throw new RuntimeException("灶台不属于该玩家");
            }
            long elapsed = ChronoUnit.SECONDS.between(task.getStartTime(), LocalDateTime.now());
            if (elapsed >= task.getCookTimeSeconds()) {
                throw new RuntimeException("菜品已烹饪完成，无需加速");
            }
            task.setStartTime(LocalDateTime.now().minusSeconds(task.getCookTimeSeconds()));
            Map<String, Object> result = new HashMap<>();
            result.put("stoveId", stoveId);
            result.put("dishId", task.getDishId());
            result.put("dishName", task.getDishName());
            result.put("status", "done");
            return result;
        }
    }
    @Transactional
    public Map<String, Object> settle(Long playerId, Long tableId) {
        TableState tableState = tableService.getTableState(playerId, tableId);
        if (tableState == null) {
            throw new RuntimeException("餐桌不存在");
        }
        if (!"dining".equals(tableState.getStatus())) {
            throw new RuntimeException("餐桌上没有正在用餐的客人");
        }
        Long servingDishId = tableState.getServingDishId();
        if (servingDishId == null) {
            throw new RuntimeException("无法获取用餐菜品信息");
        }
        Dish dish = dishRepository.findById(servingDishId)
                .orElseThrow(() -> new RuntimeException("菜品信息不存在"));
        Player player = playerRepository.findWithLockById(playerId)
                .orElseThrow(() -> new RuntimeException("玩家不存在"));
        int income = dish.getBasePrice();
        int expGain = 10;
        boolean doubleReward = player.isNextDoubleReward();
        if (doubleReward) {
            income *= 2;
            expGain *= 2;
            player.setNextDoubleReward(false);
        }
        player.setBalance(player.getBalance() + income);
        player.setTotalGuests(player.getTotalGuests() + 1);
        player.setExp(player.getExp() + expGain);
        int levelUpExp = player.getLevel() * 100;
        boolean leveledUp = false;
        while (player.getExp() >= levelUpExp) {
            player.setExp(player.getExp() - levelUpExp);
            player.setLevel(player.getLevel() + 1);
            leveledUp = true;
            levelUpExp = player.getLevel() * 100;
        }
        BigDecimal redPacketReward = BigDecimal.ZERO;
        boolean gotRedPacket = Math.random() < 0.3;
        if (gotRedPacket) {
            double randomVal = 0.1 + Math.random() * 4.9;
            redPacketReward = BigDecimal.valueOf(randomVal).setScale(2, RoundingMode.HALF_UP);
            player.setRedPacket(player.getRedPacket().add(redPacketReward));
        }
        playerRepository.save(player);
        taskService.updateProgress(playerId, 1, 1);
        String guestName = tableState.getGuestName();
        tableState.setStatus("empty");
        tableState.setGuestName(null);
        tableState.setPatience(null);
        tableState.setRequiredDish(null);
        tableState.setServingDishId(null);
        tableService.updateTableState(playerId, tableId, tableState);
        Map<String, Object> result = new HashMap<>();
        result.put("tableId", tableId);
        result.put("guestName", guestName);
        result.put("dishName", dish.getName());
        result.put("income", income);
        result.put("expGain", expGain);
        result.put("balance", player.getBalance());
        result.put("totalGuests", player.getTotalGuests());
        result.put("level", player.getLevel());
        result.put("exp", player.getExp());
        result.put("leveledUp", leveledUp);
        result.put("gotRedPacket", gotRedPacket);
        result.put("redPacketReward", redPacketReward);
        result.put("redPacketTotal", player.getRedPacket());
        result.put("tableStatus", "empty");
        result.put("doubleReward", doubleReward);
        return result;
    }
    public Map<String, Object> serve(Long playerId, Long tableId, Long stoveId) {
        CookingTask task = cookingCache.get(stoveId);
        if (task == null) {
            throw new RuntimeException("灶台上没有正在烹饪的菜品");
        }
        if (!task.getPlayerId().equals(playerId)) {
            throw new RuntimeException("灶台不属于该玩家");
        }
        long elapsed = ChronoUnit.SECONDS.between(task.getStartTime(), LocalDateTime.now());
        if (elapsed < task.getCookTimeSeconds()) {
            throw new RuntimeException("菜品尚未烹饪完成，还需等待" + (task.getCookTimeSeconds() - elapsed) + "秒");
        }
        TableState tableState = tableService.getTableState(playerId, tableId);
        if (tableState == null) {
            throw new RuntimeException("餐桌不存在");
        }
        if (!tableState.isUnlocked()) {
            throw new RuntimeException("餐桌未解锁");
        }
        if (!"waiting".equals(tableState.getStatus())) {
            throw new RuntimeException("餐桌上没有等待上菜的客人");
        }
        if (tableState.getRequiredDish() != null && !tableState.getRequiredDish().equals(task.getDishName())) {
            throw new RuntimeException("客人需要的是「" + tableState.getRequiredDish() + "」，不是「" + task.getDishName() + "」");
        }
        cookingCache.remove(stoveId);
        tableState.setStatus("dining");
        tableState.setRequiredDish(null);
        tableState.setServingDishId(task.getDishId());
        tableService.updateTableState(playerId, tableId, tableState);
        Map<String, Object> result = new HashMap<>();
        result.put("tableId", tableId);
        result.put("stoveId", stoveId);
        result.put("dishId", task.getDishId());
        result.put("dishName", task.getDishName());
        result.put("guestName", tableState.getGuestName());
        result.put("tableStatus", "dining");
        return result;
    }
    public List<Map<String, Object>> getPlayerStoveStatus(Long playerId) {
        List<Stove> stoves = stoveRepository.findByPlayerIdOrderByStoveIndex(playerId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Stove stove : stoves) {
            Map<String, Object> item = new HashMap<>();
            item.put("stoveId", stove.getStoveId());
            item.put("stoveIndex", stove.getStoveIndex());
            item.put("isUnlocked", stove.isUnlocked());
            CookingTask task = cookingCache.get(stove.getStoveId());
            if (task != null) {
                long elapsed = ChronoUnit.SECONDS.between(task.getStartTime(), LocalDateTime.now());
                long remaining = task.getCookTimeSeconds() - elapsed;
                if (remaining <= 0) {
                    item.put("status", "done");
                    item.put("dishId", task.getDishId());
                    item.put("dishName", task.getDishName());
                    item.put("remainingSeconds", 0);
                } else {
                    item.put("status", "cooking");
                    item.put("dishId", task.getDishId());
                    item.put("dishName", task.getDishName());
                    item.put("remainingSeconds", remaining);
                }
            } else {
                item.put("status", stove.isUnlocked() ? "idle" : "locked");
                item.put("dishId", null);
                item.put("dishName", null);
                item.put("remainingSeconds", 0);
            }
            result.add(item);
        }
        return result;
    }
    public Map<String, Object> cook(Long playerId, Long stoveId, Long dishId) {
        Stove stove = stoveRepository.findById(stoveId)
                .orElseThrow(() -> new RuntimeException("灶台不存在"));
        if (!stove.getPlayerId().equals(playerId)) {
            throw new RuntimeException("灶台不属于该玩家");
        }
        if (!stove.isUnlocked()) {
            throw new RuntimeException("灶台未解锁");
        }
        if (cookingCache.containsKey(stoveId)) {
            throw new RuntimeException("灶台正在烹饪中");
        }
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new RuntimeException("菜品不存在"));
        LocalDateTime startTime = LocalDateTime.now();
        CookingTask task = new CookingTask(playerId, stoveId, dishId, dish.getName(), dish.getCookTimeSeconds(), startTime);
        cookingCache.put(stoveId, task);
        Map<String, Object> result = new ConcurrentHashMap<>();
        result.put("stoveId", stoveId);
        result.put("dishId", dishId);
        result.put("dishName", dish.getName());
        result.put("cookTimeSeconds", dish.getCookTimeSeconds());
        result.put("startTime", startTime);
        return result;
    }
    public CookingTask getCookingTask(Long stoveId) {
        return cookingCache.get(stoveId);
    }
    public void removeCookingTask(Long stoveId) {
        cookingCache.remove(stoveId);
    }
    public static class CookingTask {
        private Long playerId;
        private Long stoveId;
        private Long dishId;
        private String dishName;
        private int cookTimeSeconds;
        private LocalDateTime startTime;
        public CookingTask(Long playerId, Long stoveId, Long dishId, String dishName, int cookTimeSeconds, LocalDateTime startTime) {
            this.playerId = playerId;
            this.stoveId = stoveId;
            this.dishId = dishId;
            this.dishName = dishName;
            this.cookTimeSeconds = cookTimeSeconds;
            this.startTime = startTime;
        }
        public Long getPlayerId() { return playerId; }
        public Long getStoveId() { return stoveId; }
        public Long getDishId() { return dishId; }
        public String getDishName() { return dishName; }
        public int getCookTimeSeconds() { return cookTimeSeconds; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    }
}
