package com.smallrestaurant.game.controller;
import com.smallrestaurant.game.service.AdService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/api/ad")
public class AdController {
    @Autowired
    private AdService adService;
    @PostMapping("/watch")
    public ResponseEntity<?> watchAd(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            Long playerId = (Long) request.getAttribute("playerId");
            String adId = body.get("adId").toString();
            Map<String, Object> data = adService.verifyAndReward(playerId, adId);
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
