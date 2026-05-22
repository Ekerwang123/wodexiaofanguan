package com.smallrestaurant.game.controller;
import com.smallrestaurant.game.service.PlayerService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/api/red-packet")
public class WithdrawController {
    private static final Logger log = LoggerFactory.getLogger(WithdrawController.class);
    @Autowired
    private PlayerService playerService;
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            Long playerId = (Long) request.getAttribute("playerId");
            BigDecimal amount = new BigDecimal(body.get("amount").toString());
            log.info("withdraw: playerId={}, amount={}", playerId, amount);
            Map<String, Object> data = playerService.withdrawRedPacket(playerId, amount);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "提现成功");
            result.put("data", data);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.warn("withdraw failed: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
