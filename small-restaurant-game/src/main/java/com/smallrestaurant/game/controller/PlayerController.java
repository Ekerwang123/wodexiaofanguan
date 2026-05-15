package com.smallrestaurant.game.controller;
import com.smallrestaurant.game.entity.Player;
import com.smallrestaurant.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/api/player")
public class PlayerController {
    @Autowired
    private PlayerService playerService;
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String nickname = request.get("nickname");
            Player player = playerService.loginOrRegister(username, password, nickname);
            Map<String, Object> result = new HashMap<>();
            result.put("playerId", player.getPlayerId());
            result.put("nickname", player.getNickname());
            result.put("level", player.getLevel());
            result.put("balance", player.getBalance());
            result.put("redPacket", player.getRedPacket());
            result.put("likeCount", player.getLikeCount());
            result.put("totalGuests", player.getTotalGuests());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    @GetMapping("/info")
    public ResponseEntity<?> getPlayerInfo(@RequestParam Long playerId) {
        try {
            Player player = playerService.getPlayerById(playerId);
            Map<String, Object> result = new HashMap<>();
            result.put("playerId", player.getPlayerId());
            result.put("nickname", player.getNickname());
            result.put("level", player.getLevel());
            result.put("balance", player.getBalance());
            result.put("redPacket", player.getRedPacket());
            result.put("likeCount", player.getLikeCount());
            result.put("totalGuests", player.getTotalGuests());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
