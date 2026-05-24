package com.smallrestaurant.game.service;
import com.smallrestaurant.game.entity.Dish;
import com.smallrestaurant.game.entity.Player;
import com.smallrestaurant.game.model.TableState;
import com.smallrestaurant.game.repository.DishRepository;
import com.smallrestaurant.game.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class GameServiceSettleTest {
    @Mock
    private TableService tableService;
    @Mock
    private DishRepository dishRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private TaskService taskService;
    @InjectMocks
    private GameService gameService;
    private TableState diningTable;
    private Dish dish;
    private Player player;
    private ReentrantLock tableLock;
    @BeforeEach
    void setUp() {
        diningTable = new TableState(1L, 1, 1, true);
        diningTable.setStatus("dining");
        diningTable.setGuestName("小王");
        diningTable.setServingDishId(3L);
        diningTable.setRequiredDish("麻婆豆腐");
        dish = new Dish(3L, "麻婆豆腐", 18, 30, true, null);
        player = new Player();
        player.setPlayerId(1L);
        player.setBalance(100);
        player.setExp(0);
        player.setLevel(1);
        player.setTotalGuests(0);
        player.setRedPacket(BigDecimal.ZERO);
        player.setNextDoubleReward(false);
        tableLock = new ReentrantLock();
        lenient().when(tableService.getTableLock(anyLong(), anyLong())).thenReturn(tableLock);
    }
    @Test
    void settle_normalIncome() {
        when(tableService.getTableState(1L, 1L)).thenReturn(diningTable);
        when(dishRepository.findById(3L)).thenReturn(Optional.of(dish));
        when(playerRepository.findWithLockById(1L)).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));
        Map<String, Object> result = gameService.settle(1L, 1L);
        assertEquals(18, result.get("income"));
        assertEquals(false, result.get("doubleReward"));
        assertEquals("empty", result.get("tableStatus"));
        assertEquals(118, player.getBalance());
        assertEquals(1, player.getTotalGuests());
        assertEquals(10, player.getExp());
        verify(playerRepository).save(player);
    }
    @Test
    void settle_doubleReward_incomeDoubled() {
        player.setNextDoubleReward(true);
        when(tableService.getTableState(1L, 1L)).thenReturn(diningTable);
        when(dishRepository.findById(3L)).thenReturn(Optional.of(dish));
        when(playerRepository.findWithLockById(1L)).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));
        Map<String, Object> result = gameService.settle(1L, 1L);
        assertEquals(36, result.get("income"));
        assertEquals(true, result.get("doubleReward"));
        assertEquals(136, player.getBalance());
        assertEquals(20, player.getExp());
        assertFalse(player.isNextDoubleReward());
    }
    @Test
    void settle_doubleReward_consumedAfterSettle() {
        player.setNextDoubleReward(true);
        when(tableService.getTableState(1L, 1L)).thenReturn(diningTable);
        when(dishRepository.findById(3L)).thenReturn(Optional.of(dish));
        when(playerRepository.findWithLockById(1L)).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));
        gameService.settle(1L, 1L);
        assertFalse(player.isNextDoubleReward());
    }
    @Test
    void settle_tableNotDining_throwsException() {
        diningTable.setStatus("waiting");
        when(tableService.getTableState(1L, 1L)).thenReturn(diningTable);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> gameService.settle(1L, 1L));
        assertEquals("餐桌上没有正在用餐的客人", ex.getMessage());
    }
    @Test
    void settle_tableNotFound_throwsException() {
        when(tableService.getTableState(1L, 999L)).thenReturn(null);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> gameService.settle(1L, 999L));
        assertEquals("餐桌不存在", ex.getMessage());
    }
    @Test
    void settle_servingDishIdNull_throwsException() {
        diningTable.setServingDishId(null);
        when(tableService.getTableState(1L, 1L)).thenReturn(diningTable);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> gameService.settle(1L, 1L));
        assertEquals("无法获取用餐菜品信息", ex.getMessage());
    }
    @Test
    void settle_dishNotFound_throwsException() {
        when(tableService.getTableState(1L, 1L)).thenReturn(diningTable);
        when(dishRepository.findById(3L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> gameService.settle(1L, 1L));
        assertEquals("菜品信息不存在", ex.getMessage());
    }
    @Test
    void settle_playerNotFound_throwsException() {
        when(tableService.getTableState(1L, 1L)).thenReturn(diningTable);
        when(dishRepository.findById(3L)).thenReturn(Optional.of(dish));
        when(playerRepository.findWithLockById(1L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> gameService.settle(1L, 1L));
        assertEquals("玩家不存在", ex.getMessage());
    }
    @Test
    void settle_levelUp() {
        player.setExp(95);
        when(tableService.getTableState(1L, 1L)).thenReturn(diningTable);
        when(dishRepository.findById(3L)).thenReturn(Optional.of(dish));
        when(playerRepository.findWithLockById(1L)).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));
        Map<String, Object> result = gameService.settle(1L, 1L);
        assertEquals(true, result.get("leveledUp"));
        assertEquals(2, player.getLevel());
        assertEquals(5, player.getExp());
    }
    @Test
    void settle_tableStateResetToEmpty() {
        when(tableService.getTableState(1L, 1L)).thenReturn(diningTable);
        when(dishRepository.findById(3L)).thenReturn(Optional.of(dish));
        when(playerRepository.findWithLockById(1L)).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));
        gameService.settle(1L, 1L);
        verify(tableService).updateTableState(eq(1L), eq(1L), argThat(ts ->
                "empty".equals(ts.getStatus()) &&
                ts.getGuestName() == null &&
                ts.getRequiredDish() == null &&
                ts.getServingDishId() == null
        ));
    }
    @Test
    void settle_taskProgressUpdated() {
        when(tableService.getTableState(1L, 1L)).thenReturn(diningTable);
        when(dishRepository.findById(3L)).thenReturn(Optional.of(dish));
        when(playerRepository.findWithLockById(1L)).thenReturn(Optional.of(player));
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));
        gameService.settle(1L, 1L);
        verify(taskService).updateProgress(1L, 1, 1);
    }
}
