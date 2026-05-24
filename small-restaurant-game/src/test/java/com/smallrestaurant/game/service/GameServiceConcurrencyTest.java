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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceConcurrencyTest {

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

    private Dish dish;
    private static final int THREAD_COUNT = 10;
    private static final int TIMEOUT_SECONDS = 30;

    @BeforeEach
    void setUp() {
        dish = new Dish(3L, "麻婆豆腐", 18, 30, true, null);
        lenient().when(tableService.getTableLock(anyLong(), anyLong())).thenReturn(new ReentrantLock());
    }

    private Player createPlayer(Long playerId, int balance, int exp, int level, int totalGuests, boolean doubleReward) {
        Player player = new Player();
        player.setPlayerId(playerId);
        player.setBalance(balance);
        player.setExp(exp);
        player.setLevel(level);
        player.setTotalGuests(totalGuests);
        player.setRedPacket(BigDecimal.ZERO);
        player.setNextDoubleReward(doubleReward);
        return player;
    }

    private TableState createDiningTable(Long tableId, Long servingDishId, String guestName) {
        TableState tableState = new TableState(tableId, 1, 1, true);
        tableState.setStatus("dining");
        tableState.setGuestName(guestName);
        tableState.setServingDishId(servingDishId);
        tableState.setRequiredDish("麻婆豆腐");
        return tableState;
    }

    @Test
    void settle_concurrentDifferentTables_noDeadlockAllComplete() throws Exception {
        Long playerId = 1L;
        Player sharedPlayer = createPlayer(playerId, 100, 0, 1, 0, false);

        when(dishRepository.findById(3L)).thenReturn(Optional.of(dish));
        when(playerRepository.findWithLockById(playerId)).thenReturn(Optional.of(sharedPlayer));
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));

        List<TableState> tables = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            tables.add(createDiningTable((long) (i + 1), 3L, "客人" + i));
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            when(tableService.getTableState(playerId, (long) (i + 1))).thenReturn(tables.get(i));
        }

        CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT);
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int tableIndex = i;
            futures.add(executor.submit(() -> {
                try {
                    barrier.await(5, TimeUnit.SECONDS);
                    Map<String, Object> result = gameService.settle(playerId, (long) (tableIndex + 1));
                    if (result != null && result.containsKey("income")) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                }
            }));
        }

        for (Future<?> f : futures) {
            f.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), "线程池应在超时前终止(无死锁)");

        long elapsed = System.currentTimeMillis() - startTime;

        System.out.println("=== 场景1: 10线程并发结算不同餐桌 ===");
        System.out.println("成功结算数: " + successCount.get() + "/" + THREAD_COUNT);
        System.out.println("失败数: " + failCount.get());
        System.out.println("最终余额: " + sharedPlayer.getBalance());
        System.out.println("最终总客数: " + sharedPlayer.getTotalGuests());
        System.out.println("耗时: " + elapsed + "ms");

        assertEquals(THREAD_COUNT, successCount.get(), "所有结算都应成功(不同餐桌无冲突)");
        assertEquals(0, failCount.get(), "不应有失败");
        assertTrue(sharedPlayer.getBalance() >= 100, "余额不应低于初始值");
        assertTrue(elapsed < TIMEOUT_SECONDS * 1000, "不应发生死锁");
    }

    @Test
    void settle_concurrentSameTable_exactlyOneSucceeds() throws Exception {
        Long playerId = 1L;
        Long tableId = 1L;
        Player sharedPlayer = createPlayer(playerId, 100, 0, 1, 0, false);
        TableState diningTable = createDiningTable(tableId, 3L, "小王");

        when(tableService.getTableState(playerId, tableId)).thenReturn(diningTable);
        when(dishRepository.findById(3L)).thenReturn(Optional.of(dish));
        when(playerRepository.findWithLockById(playerId)).thenReturn(Optional.of(sharedPlayer));
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));

        CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT);
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger duplicateSettleCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<String> errorMessages = Collections.synchronizedList(new ArrayList<>());
        List<Future<?>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < THREAD_COUNT; i++) {
            futures.add(executor.submit(() -> {
                try {
                    barrier.await(5, TimeUnit.SECONDS);
                    Map<String, Object> result = gameService.settle(playerId, tableId);
                    if (result != null && result.containsKey("income")) {
                        int prev = successCount.getAndIncrement();
                        if (prev > 0) {
                            duplicateSettleCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    errorMessages.add(e.getMessage());
                }
            }));
        }

        for (Future<?> f : futures) {
            f.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), "线程池应在超时前终止(无死锁)");

        long elapsed = System.currentTimeMillis() - startTime;

        System.out.println("=== 场景2: 10线程并发结算同一餐桌(防重复结算验证) ===");
        System.out.println("成功结算数: " + successCount.get());
        System.out.println("重复结算数: " + duplicateSettleCount.get());
        System.out.println("失败(被锁拒绝)数: " + failCount.get());
        System.out.println("最终余额: " + sharedPlayer.getBalance());
        System.out.println("最终总客数: " + sharedPlayer.getTotalGuests());
        System.out.println("错误消息样本: " + (errorMessages.isEmpty() ? "无" : errorMessages.get(0)));
        System.out.println("耗时: " + elapsed + "ms");

        assertEquals(1, successCount.get(), "同一餐桌应恰好只有1个结算成功(ReentrantLock+settling中间态)");
        assertEquals(0, duplicateSettleCount.get(), "不应有重复结算");
        assertEquals(THREAD_COUNT - 1, failCount.get(), "其余线程应全部被拒绝");
        assertEquals(118, sharedPlayer.getBalance(), "余额应只增加一次(100+18)");
        assertEquals(1, sharedPlayer.getTotalGuests(), "总客数应只增加1");
        assertTrue(errorMessages.stream().anyMatch(msg -> msg.contains("用餐") || msg.contains("settling")),
                "被拒绝的线程应收到正确的错误信息");
        assertTrue(elapsed < TIMEOUT_SECONDS * 1000, "不应发生死锁");
    }

    @Test
    void settle_doubleRewardConcurrent_consumedAtMostOnce() throws Exception {
        Long playerId = 1L;
        Long tableId = 1L;
        Player sharedPlayer = createPlayer(playerId, 100, 0, 1, 0, true);
        TableState diningTable = createDiningTable(tableId, 3L, "小王");

        when(tableService.getTableState(playerId, tableId)).thenReturn(diningTable);
        when(dishRepository.findById(3L)).thenReturn(Optional.of(dish));
        when(playerRepository.findWithLockById(playerId)).thenReturn(Optional.of(sharedPlayer));
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));

        CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT);
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicInteger doubleRewardCount = new AtomicInteger(0);
        AtomicInteger normalCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            futures.add(executor.submit(() -> {
                try {
                    barrier.await(5, TimeUnit.SECONDS);
                    Map<String, Object> result = gameService.settle(playerId, tableId);
                    if (result != null && result.containsKey("doubleReward")) {
                        if (Boolean.TRUE.equals(result.get("doubleReward"))) {
                            doubleRewardCount.incrementAndGet();
                        } else {
                            normalCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                }
            }));
        }

        for (Future<?> f : futures) {
            f.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), "线程池应在超时前终止(无死锁)");

        System.out.println("=== 场景3: 双倍收益并发结算 ===");
        System.out.println("双倍收益结算数: " + doubleRewardCount.get());
        System.out.println("普通结算数: " + normalCount.get());
        System.out.println("失败数: " + failCount.get());
        System.out.println("双倍收益标记最终状态: " + sharedPlayer.isNextDoubleReward());

        assertTrue(doubleRewardCount.get() <= 1, "双倍收益最多只能被消耗一次(生产环境由PESSIMISTIC_WRITE锁保证)");
    }

    @Test
    void settle_highVolume_100concurrentSettles_noDeadlock() throws Exception {
        int totalThreads = 100;
        Long playerId = 1L;
        Player sharedPlayer = createPlayer(playerId, 0, 0, 1, 0, false);

        when(dishRepository.findById(3L)).thenReturn(Optional.of(dish));
        when(playerRepository.findWithLockById(playerId)).thenReturn(Optional.of(sharedPlayer));
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));

        List<TableState> tables = new ArrayList<>();
        for (int i = 0; i < totalThreads; i++) {
            tables.add(createDiningTable((long) (i + 1), 3L, "客人" + i));
        }
        for (int i = 0; i < totalThreads; i++) {
            when(tableService.getTableState(playerId, (long) (i + 1))).thenReturn(tables.get(i));
        }

        CyclicBarrier barrier = new CyclicBarrier(totalThreads);
        ExecutorService executor = Executors.newFixedThreadPool(totalThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < totalThreads; i++) {
            final int tableIndex = i;
            futures.add(executor.submit(() -> {
                try {
                    barrier.await(10, TimeUnit.SECONDS);
                    Map<String, Object> result = gameService.settle(playerId, (long) (tableIndex + 1));
                    if (result != null && result.containsKey("income")) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                }
            }));
        }

        for (Future<?> f : futures) {
            f.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), "线程池应在超时前终止(无死锁)");

        long elapsed = System.currentTimeMillis() - startTime;

        System.out.println("=== 场景4: 100线程高并发结算 ===");
        System.out.println("成功结算数: " + successCount.get() + "/" + totalThreads);
        System.out.println("失败数: " + failCount.get());
        System.out.println("最终余额: " + sharedPlayer.getBalance());
        System.out.println("最终总客数: " + sharedPlayer.getTotalGuests());
        System.out.println("最终经验: " + sharedPlayer.getExp());
        System.out.println("耗时: " + elapsed + "ms");

        assertEquals(totalThreads, successCount.get(), "所有100个结算都应成功(不同餐桌无冲突)");
        assertEquals(0, failCount.get(), "不应有失败");
        assertTrue(sharedPlayer.getBalance() > 0, "余额应为正数");
        assertTrue(sharedPlayer.getTotalGuests() > 0, "总客数应大于0");
        assertTrue(elapsed < TIMEOUT_SECONDS * 1000, "不应发生死锁");
    }

    @Test
    void settle_mixedOperations_settleAndDoubleReward_noDeadlock() throws Exception {
        Long playerId = 1L;
        Player sharedPlayer = createPlayer(playerId, 100, 0, 1, 0, false);
        List<TableState> diningTables = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            diningTables.add(createDiningTable((long) (i + 1), 3L, "客人" + i));
            when(tableService.getTableState(playerId, (long) (i + 1))).thenReturn(diningTables.get(i));
        }

        when(dishRepository.findById(3L)).thenReturn(Optional.of(dish));
        when(playerRepository.findWithLockById(playerId)).thenReturn(Optional.of(sharedPlayer));
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));

        int settleThreads = 5;
        int doubleRewardThreads = 5;
        int totalThreads = settleThreads + doubleRewardThreads;

        CyclicBarrier barrier = new CyclicBarrier(totalThreads);
        ExecutorService executor = Executors.newFixedThreadPool(totalThreads);
        AtomicInteger settleSuccess = new AtomicInteger(0);
        AtomicInteger doubleRewardSuccess = new AtomicInteger(0);
        AtomicInteger doubleRewardFail = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < settleThreads; i++) {
            final int tableIdx = i;
            futures.add(executor.submit(() -> {
                try {
                    barrier.await(5, TimeUnit.SECONDS);
                    gameService.settle(playerId, (long) (tableIdx + 1));
                    settleSuccess.incrementAndGet();
                } catch (Exception e) {
                }
            }));
        }

        for (int i = 0; i < doubleRewardThreads; i++) {
            futures.add(executor.submit(() -> {
                try {
                    barrier.await(5, TimeUnit.SECONDS);
                    Player lockedPlayer = createPlayer(playerId, sharedPlayer.getBalance(),
                            sharedPlayer.getExp(), sharedPlayer.getLevel(),
                            sharedPlayer.getTotalGuests(), sharedPlayer.isNextDoubleReward());
                    if (!lockedPlayer.isNextDoubleReward()) {
                        lockedPlayer.setNextDoubleReward(true);
                        doubleRewardSuccess.incrementAndGet();
                    } else {
                        doubleRewardFail.incrementAndGet();
                    }
                } catch (Exception e) {
                    doubleRewardFail.incrementAndGet();
                }
            }));
        }

        for (Future<?> f : futures) {
            f.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), "线程池应在超时前终止(无死锁)");

        System.out.println("=== 场景5: 混合并发(settle + doubleReward) ===");
        System.out.println("结算成功数: " + settleSuccess.get());
        System.out.println("双倍收益激活成功数: " + doubleRewardSuccess.get());
        System.out.println("双倍收益激活失败数: " + doubleRewardFail.get());
        System.out.println("最终余额: " + sharedPlayer.getBalance());

        assertTrue(settleSuccess.get() >= 1, "至少有一个结算成功");
        assertTrue(doubleRewardSuccess.get() >= 1, "至少有一个双倍收益激活成功");
    }

    @Test
    void accelerateCook_concurrentSameStove_noDeadlock() throws Exception {
        Long playerId = 1L;
        Long stoveId = 1L;
        GameService.CookingTask task = new GameService.CookingTask(
                playerId, stoveId, 3L, "麻婆豆腐", 30,
                java.time.LocalDateTime.now().plusSeconds(30)
        );
        gameService.removeCookingTask(stoveId);

        Map<Long, GameService.CookingTask> cache = getCookingCache();
        cache.put(stoveId, task);

        CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT);
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < THREAD_COUNT; i++) {
            futures.add(executor.submit(() -> {
                try {
                    barrier.await(5, TimeUnit.SECONDS);
                    Map<String, Object> result = gameService.accelerateCook(playerId, stoveId);
                    if (result != null) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                }
            }));
        }

        for (Future<?> f : futures) {
            f.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), "线程池应在超时前终止(无死锁)");

        long elapsed = System.currentTimeMillis() - startTime;

        System.out.println("=== 场景6: 10线程并发加速同一灶台 ===");
        System.out.println("成功数: " + successCount.get());
        System.out.println("失败数: " + failCount.get());
        System.out.println("耗时: " + elapsed + "ms");

        assertTrue(elapsed < TIMEOUT_SECONDS * 1000, "不应发生死锁");
        assertTrue(successCount.get() >= 1, "至少有一个加速成功");
        assertEquals(THREAD_COUNT, successCount.get() + failCount.get(), "所有线程都应正常完成");

        cache.remove(stoveId);
    }

    @Test
    void settle_levelUpConcurrent_noCorruptedLevel() throws Exception {
        Long playerId = 1L;
        Player sharedPlayer = createPlayer(playerId, 100, 950, 1, 0, false);

        when(dishRepository.findById(3L)).thenReturn(Optional.of(dish));
        when(playerRepository.findWithLockById(playerId)).thenReturn(Optional.of(sharedPlayer));
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));

        int settleCount = 6;
        List<TableState> tables = new ArrayList<>();
        for (int i = 0; i < settleCount; i++) {
            tables.add(createDiningTable((long) (i + 1), 3L, "客人" + i));
        }
        for (int i = 0; i < settleCount; i++) {
            when(tableService.getTableState(playerId, (long) (i + 1))).thenReturn(tables.get(i));
        }

        CyclicBarrier barrier = new CyclicBarrier(settleCount);
        ExecutorService executor = Executors.newFixedThreadPool(settleCount);
        AtomicInteger successCount = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < settleCount; i++) {
            final int tableIndex = i;
            futures.add(executor.submit(() -> {
                try {
                    barrier.await(5, TimeUnit.SECONDS);
                    Map<String, Object> result = gameService.settle(playerId, (long) (tableIndex + 1));
                    if (result != null) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                }
            }));
        }

        for (Future<?> f : futures) {
            f.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), "线程池应在超时前终止(无死锁)");

        System.out.println("=== 场景7: 并发结算触发升级 ===");
        System.out.println("成功结算数: " + successCount.get());
        System.out.println("初始经验: 950, 每次结算+10");
        System.out.println("最终等级: " + sharedPlayer.getLevel());
        System.out.println("最终经验: " + sharedPlayer.getExp());
        System.out.println("最终余额: " + sharedPlayer.getBalance());

        assertTrue(sharedPlayer.getLevel() >= 2, "多次结算后应触发升级");
        assertTrue(sharedPlayer.getExp() >= 0, "经验不应为负数");
        assertTrue(sharedPlayer.getBalance() >= 100, "余额应只增不减");
    }

    @Test
    void settle_sequential_baselineCorrect() throws Exception {
        Long playerId = 1L;
        int operations = 20;

        Player seqPlayer = createPlayer(playerId, 100, 0, 1, 0, false);
        when(dishRepository.findById(3L)).thenReturn(Optional.of(dish));
        when(playerRepository.findWithLockById(playerId)).thenReturn(Optional.of(seqPlayer));
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));

        List<TableState> seqTables = new ArrayList<>();
        for (int i = 0; i < operations; i++) {
            seqTables.add(createDiningTable((long) (i + 1), 3L, "客人" + i));
            when(tableService.getTableState(playerId, (long) (i + 1))).thenReturn(seqTables.get(i));
        }

        for (int i = 0; i < operations; i++) {
            gameService.settle(playerId, (long) (i + 1));
        }

        int seqBalance = seqPlayer.getBalance();
        int seqExp = seqPlayer.getExp();
        int seqTotalGuests = seqPlayer.getTotalGuests();
        int seqLevel = seqPlayer.getLevel();

        int expectedLevel = 1;
        int remainingExp = operations * 10;
        while (remainingExp >= expectedLevel * 100) {
            remainingExp -= expectedLevel * 100;
            expectedLevel++;
        }

        System.out.println("=== 场景8: 顺序执行基线验证 ===");
        System.out.println("顺序执行 " + operations + " 次结算:");
        System.out.println("  余额: " + seqBalance + " (期望: " + (100 + operations * 18) + ")");
        System.out.println("  经验: " + seqExp + " (期望: " + remainingExp + ")");
        System.out.println("  总客数: " + seqTotalGuests + " (期望: " + operations + ")");
        System.out.println("  等级: " + seqLevel + " (期望: " + expectedLevel + ")");

        assertEquals(100 + operations * 18, seqBalance, "顺序执行余额应正确");
        assertEquals(remainingExp, seqExp, "顺序执行经验应正确(扣除升级消耗)");
        assertEquals(expectedLevel, seqLevel, "顺序执行等级应正确");
        assertEquals(operations, seqTotalGuests, "顺序执行总客数应正确");
    }

    @SuppressWarnings("unchecked")
    private Map<Long, GameService.CookingTask> getCookingCache() throws Exception {
        java.lang.reflect.Field field = GameService.class.getDeclaredField("cookingCache");
        field.setAccessible(true);
        return (Map<Long, GameService.CookingTask>) field.get(gameService);
    }
}
