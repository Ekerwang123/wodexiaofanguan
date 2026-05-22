package com.smallrestaurant.game.service;
import com.smallrestaurant.game.entity.Player;
import com.smallrestaurant.game.entity.Stove;
import com.smallrestaurant.game.repository.PlayerRepository;
import com.smallrestaurant.game.repository.StoveRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class StoveService {

    private static final Logger log = LoggerFactory.getLogger(StoveService.class);

    @Autowired
    private StoveRepository stoveRepository;
    @Autowired
    private PlayerRepository playerRepository;
    private static final int UNLOCK_PRICE = 1000;
    public List<Stove> getPlayerStoves(Long playerId) {
        return stoveRepository.findByPlayerIdOrderByStoveIndex(playerId);
    }
    @Transactional
    public Map<String, Object> unlockStove(Long playerId, int stoveIndex) {
        if (stoveIndex < 1 || stoveIndex > 4) {
            throw new RuntimeException("灶台编号必须在1-4之间");
        }
        Stove stove = stoveRepository.findByPlayerIdAndStoveIndex(playerId, stoveIndex)
                .orElseThrow(() -> new RuntimeException("灶台不存在"));
        if (stove.isUnlocked()) {
            throw new RuntimeException("灶台已解锁");
        }
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("玩家不存在"));
        if (player.getBalance() < UNLOCK_PRICE) {
            throw new RuntimeException("余额不足，解锁灶台需要 " + UNLOCK_PRICE + "，当前余额 " + player.getBalance());
        }
        player.setBalance(player.getBalance() - UNLOCK_PRICE);
        playerRepository.save(player);
        stove.setUnlocked(true);
        stoveRepository.save(stove);
        Map<String, Object> result = new HashMap<>();
        result.put("stoveId", stove.getStoveId());
        result.put("playerId", stove.getPlayerId());
        result.put("stoveIndex", stove.getStoveIndex());
        result.put("isUnlocked", true);
        result.put("price", UNLOCK_PRICE);
        result.put("balance", player.getBalance());
        return result;
    }
}
