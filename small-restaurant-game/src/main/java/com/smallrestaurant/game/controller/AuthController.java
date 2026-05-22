package com.smallrestaurant.game.controller;
import com.smallrestaurant.game.entity.Player;
import com.smallrestaurant.game.service.PlayerService;
import com.smallrestaurant.game.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/api/player")
public class AuthController {
    @Autowired
    private PlayerService playerService;
    @Autowired
    private JwtUtil jwtUtil;
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String nickname = request.get("nickname");
            Player player = playerService.loginOrRegister(username, password, nickname);
            String token = jwtUtil.generateToken(player.getPlayerId());
            Map<String, Object> data = new HashMap<>();
            data.put("playerId", player.getPlayerId());
            data.put("nickname", player.getNickname());
            data.put("level", player.getLevel());
            data.put("balance", player.getBalance());
            data.put("token", token);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", data);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
