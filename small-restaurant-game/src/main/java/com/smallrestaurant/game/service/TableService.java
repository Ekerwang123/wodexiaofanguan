package com.smallrestaurant.game.service;

import com.smallrestaurant.game.entity.Player;
import com.smallrestaurant.game.entity.RestaurantTable;
import com.smallrestaurant.game.model.TableState;
import com.smallrestaurant.game.repository.PlayerRepository;
import com.smallrestaurant.game.repository.RestaurantTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class TableService {

    @Autowired
    private RestaurantTableRepository restaurantTableRepository;
    @Autowired
    private PlayerRepository playerRepository;
    private final Map<Long, Map<Long, TableState>> tableStateCache = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> tableLocks = new ConcurrentHashMap<>();

    private String lockKey(Long playerId, Long tableId) {
        return playerId + ":" + tableId;
    }

    public ReentrantLock getTableLock(Long playerId, Long tableId) {
        return tableLocks.computeIfAbsent(lockKey(playerId, tableId), k -> new ReentrantLock());
    }

    public List<TableState> getPlayerTables(Long playerId) {
        List<RestaurantTable> tables = restaurantTableRepository.findByPlayerIdOrderByPositionX(playerId);
        tableStateCache.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        Map<Long, TableState> states = tableStateCache.get(playerId);
        List<TableState> result = new ArrayList<>();
        for (RestaurantTable table : tables) {
            TableState state = states.computeIfAbsent(table.getTableId(),
                    id -> new TableState(id, table.getPositionX(), table.getPositionY(), table.isUnlocked()));
            ReentrantLock lock = getTableLock(playerId, table.getTableId());
            lock.lock();
            try {
                state.setUnlocked(table.isUnlocked());
                if (!state.isUnlocked()) {
                    state.setStatus("locked");
                    state.setGuestName(null);
                    state.setPatience(null);
                    state.setRequiredDish(null);
                }
            } finally {
                lock.unlock();
            }
            result.add(state);
        }
        return result;
    }

    public TableState getTableState(Long playerId, Long tableId) {
        Map<Long, TableState> states = tableStateCache.get(playerId);
        if (states != null) {
            return states.get(tableId);
        }
        return null;
    }

    public void updateTableState(Long playerId, Long tableId, TableState state) {
        tableStateCache.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        tableStateCache.get(playerId).put(tableId, state);
    }

    @Transactional
    public Map<String, Object> unlockTable(Long playerId, int tableIndex) {
        if (tableIndex < 1 || tableIndex > 9) {
            throw new RuntimeException("餐桌编号必须在1-9之间");
        }
        int row = (tableIndex - 1) / 3 + 1;
        int col = (tableIndex - 1) % 3 + 1;
        if (row == 1) {
            throw new RuntimeException("第1行餐桌开局已解锁，无需额外解锁");
        }
        int price = row == 2 ? 1000 : 2000;
        RestaurantTable table = restaurantTableRepository
                .findByPlayerIdAndPositionXAndPositionY(playerId, row, col)
                .orElseThrow(() -> new RuntimeException("餐桌不存在，请先初始化餐桌数据"));
        if (table.isUnlocked()) {
            throw new RuntimeException("餐桌已解锁");
        }
        Player player = playerRepository.findWithLockById(playerId)
                .orElseThrow(() -> new RuntimeException("玩家不存在"));
        if (player.getBalance() < price) {
            throw new RuntimeException("余额不足，解锁该餐桌需要 " + price + "，当前余额 " + player.getBalance());
        }
        player.setBalance(player.getBalance() - price);
        playerRepository.save(player);
        table.setUnlocked(true);
        restaurantTableRepository.save(table);
        ReentrantLock lock = getTableLock(playerId, table.getTableId());
        lock.lock();
        try {
            Map<Long, TableState> states = tableStateCache.get(playerId);
            if (states != null && states.containsKey(table.getTableId())) {
                TableState state = states.get(table.getTableId());
                state.setUnlocked(true);
                state.setStatus("empty");
            }
        } finally {
            lock.unlock();
        }
        Map<String, Object> result = new HashMap<>();
        result.put("tableId", table.getTableId());
        result.put("tableIndex", tableIndex);
        result.put("positionX", row);
        result.put("positionY", col);
        result.put("price", price);
        result.put("balance", player.getBalance());
        result.put("unlocked", true);
        return result;
    }
}
