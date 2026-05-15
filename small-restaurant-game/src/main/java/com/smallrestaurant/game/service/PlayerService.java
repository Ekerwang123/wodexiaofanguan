package com.smallrestaurant.game.service;
import com.smallrestaurant.game.entity.Player;
import com.smallrestaurant.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
@Service
public class PlayerService {
    @Autowired
    private PlayerRepository playerRepository;
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
        player.setRedPacket(0);
        player.setLikeCount(0);
        player.setTotalGuests(0);
        return playerRepository.save(player);
    }
    public Player getPlayerById(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("玩家不存在"));
    }
}
