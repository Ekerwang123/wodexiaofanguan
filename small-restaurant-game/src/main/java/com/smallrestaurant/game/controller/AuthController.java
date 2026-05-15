package com.smallrestaurant.game.controller;
import com.smallrestaurant.game.entity.User;
import com.smallrestaurant.game.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService userService;
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String nickname = request.get("nickname");
            User user = userService.register(username, password, nickname);
            Map<String, Object> result = new HashMap<>();
            result.put("id", user.getId());
            result.put("username", user.getUsername());
            result.put("nickname", user.getNickname());
            result.put("balance", user.getBalance());
            result.put("redPacket", user.getRedPacket());
            result.put("likeCount", user.getLikeCount());
            result.put("totalGuests", user.getTotalGuests());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            User user = userService.login(username, password);
            Map<String, Object> result = new HashMap<>();
            result.put("id", user.getId());
            result.put("username", user.getUsername());
            result.put("nickname", user.getNickname());
            result.put("balance", user.getBalance());
            result.put("redPacket", user.getRedPacket());
            result.put("likeCount", user.getLikeCount());
            result.put("totalGuests", user.getTotalGuests());
            result.put("exp", user.getExp());
            result.put("level", user.getLevel());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
