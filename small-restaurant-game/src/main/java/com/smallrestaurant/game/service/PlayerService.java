package com.smallrestaurant.game.service;
import com.smallrestaurant.game.entity.Player;
import com.smallrestaurant.game.entity.RestaurantTable;
import com.smallrestaurant.game.entity.Stove;
import com.smallrestaurant.game.entity.WithdrawRecord;
import com.smallrestaurant.game.repository.PlayerRepository;
import com.smallrestaurant.game.repository.RestaurantTableRepository;
import com.smallrestaurant.game.repository.StoveRepository;
import com.smallrestaurant.game.repository.WithdrawRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
@Service
public class PlayerService {
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private StoveRepository stoveRepository;
    @Autowired
    private RestaurantTableRepository restaurantTableRepository;
    @Autowired
    @Lazy
    private TaskService taskService;
    @Autowired
    private WithdrawRecordRepository withdrawRecordRepository;
    @Transactional
    public Player loginOrRegister(String username, String password, String nickname) {
        Optional<Player> existing = playerRepository.findByUsername(username);
        if (existing.isPresent()) {
            Player player = existing.get();
            if (!player.getPasswordHash().equals(password)) {
                throw new RuntimeException("密码错误");
            }
            return player;
        }
        Player player = new Player();
        player.setUsername(username);
        player.setPasswordHash(password);
        player.setNickname(nickname != null ? nickname : username);
        player.setLevel(1);
        player.setBalance(100);
        player.setRedPacket(BigDecimal.ZERO);
        player.setLikeCount(0);
        player.setTotalGuests(0);
        player = playerRepository.save(player);
        restaurantTableRepository.save(new RestaurantTable(player.getPlayerId(), 1, 1, true));
        restaurantTableRepository.save(new RestaurantTable(player.getPlayerId(), 1, 2, true));
        restaurantTableRepository.save(new RestaurantTable(player.getPlayerId(), 1, 3, true));
        stoveRepository.save(new Stove(player.getPlayerId(), 1, true));
        stoveRepository.save(new Stove(player.getPlayerId(), 2, false));
        stoveRepository.save(new Stove(player.getPlayerId(), 3, false));
        stoveRepository.save(new Stove(player.getPlayerId(), 4, false));
        taskService.initPlayerTasks(player.getPlayerId());
        return player;
    }
    public Player getPlayerById(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("玩家不存在"));
    }

    @Transactional
    public void enableDoubleReward(Long playerId) {
        Player player = playerRepository.findWithLockById(playerId)
                .orElseThrow(() -> new RuntimeException("玩家不存在"));
        if (player.isNextDoubleReward()) {
            throw new RuntimeException("双倍收益已激活，无需重复激活");
        }
        player.setNextDoubleReward(true);
        playerRepository.save(player);
    }

    private static final BigDecimal MIN_WITHDRAW = new BigDecimal("1");

    @Transactional
    public Map<String, Object> withdrawRedPacket(Long playerId, BigDecimal amount) {
        if (amount.compareTo(MIN_WITHDRAW) < 0) {
            throw new RuntimeException("单次提现最低1元");
        }
        Player player = playerRepository.findWithLockById(playerId)
                .orElseThrow(() -> new RuntimeException("玩家不存在"));
        if (amount.compareTo(player.getRedPacket()) > 0) {
            throw new RuntimeException("红包余额不足，当前余额 " + player.getRedPacket());
        }
        player.setRedPacket(player.getRedPacket().subtract(amount));
        playerRepository.save(player);
        withdrawRecordRepository.save(new WithdrawRecord(playerId, amount, 1));
        Map<String, Object> result = new HashMap<>();
        result.put("newRedPacket", player.getRedPacket());
        result.put("withdrawAmount", amount);
        return result;
    }
}
