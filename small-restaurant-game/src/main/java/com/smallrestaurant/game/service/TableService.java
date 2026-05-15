package com.smallrestaurant.game.service;
import com.smallrestaurant.game.entity.RestaurantTable;
import com.smallrestaurant.game.model.TableState;
import com.smallrestaurant.game.repository.RestaurantTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Service
public class TableService {
    @Autowired
    private RestaurantTableRepository restaurantTableRepository;
    private final Map<Long, Map<Long, TableState>> tableStateCache = new ConcurrentHashMap<>();
    public List<TableState> getPlayerTables(Long userId) {
        List<RestaurantTable> tables = restaurantTableRepository.findByUserId(userId);
        tableStateCache.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        Map<Long, TableState> states = tableStateCache.get(userId);
        List<TableState> result = new ArrayList<>();
        for (RestaurantTable table : tables) {
            TableState state = states.computeIfAbsent(table.getId(),
                    id -> new TableState(id, table.getPositionX(), table.getPositionY(), table.isUnlocked()));
            state.setUnlocked(table.isUnlocked());
            if (!state.isUnlocked()) {
                state.setStatus("locked");
                state.setGuestName(null);
                state.setPatience(null);
                state.setRequiredDish(null);
            }
            result.add(state);
        }
        return result;
    }
    public TableState getTableState(Long userId, Long tableId) {
        Map<Long, TableState> states = tableStateCache.get(userId);
        if (states != null) {
            return states.get(tableId);
        }
        return null;
    }
    public void updateTableState(Long userId, Long tableId, TableState state) {
        tableStateCache.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        tableStateCache.get(userId).put(tableId, state);
    }
}
