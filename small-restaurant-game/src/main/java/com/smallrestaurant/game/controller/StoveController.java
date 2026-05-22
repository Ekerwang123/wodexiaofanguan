package com.smallrestaurant.game.controller;
import com.smallrestaurant.game.entity.Stove;
import com.smallrestaurant.game.service.StoveService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/stove")
public class StoveController {
    private static final Logger log = LoggerFactory.getLogger(StoveController.class);
    @Autowired
    private StoveService stoveService;
    @GetMapping("/list")
    public ResponseEntity<?> getStoveList(HttpServletRequest request) {
        try {
            Long playerId = (Long) request.getAttribute("playerId");
            List<Stove> stoves = stoveService.getPlayerStoves(playerId);
            Map<String, Object> data = new HashMap<>();
            data.put("playerId", playerId);
            data.put("stoves", stoves);
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
    @PostMapping("/unlock")
    public ResponseEntity<?> unlockStove(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            Long playerId = (Long) request.getAttribute("playerId");
            int stoveIndex = Integer.parseInt(body.get("stoveIndex").toString());
            log.info("unlockStove: playerId={}, stoveIndex={}", playerId, stoveIndex);
            Map<String, Object> data = stoveService.unlockStove(playerId, stoveIndex);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", data);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.warn("unlockStove failed: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
