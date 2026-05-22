package com.smallrestaurant.game.controller;
import com.smallrestaurant.game.service.TaskService;
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
@RequestMapping("/api/task")
public class TaskController {
    private static final Logger log = LoggerFactory.getLogger(TaskController.class);
    @Autowired
    private TaskService taskService;
    @GetMapping("/list")
    public ResponseEntity<?> getTaskList(HttpServletRequest request) {
        try {
            Long playerId = (Long) request.getAttribute("playerId");
            List<Map<String, Object>> tasks = taskService.getPlayerTaskList(playerId);
            Map<String, Object> data = new HashMap<>();
            data.put("playerId", playerId);
            data.put("tasks", tasks);
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
    @PostMapping("/claim")
    public ResponseEntity<?> claimReward(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            Long playerId = (Long) request.getAttribute("playerId");
            Long taskId = Long.valueOf(body.get("taskId").toString());
            log.info("claimReward: playerId={}, taskId={}", playerId, taskId);
            Map<String, Object> data = taskService.claimReward(playerId, taskId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", data);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.warn("claimReward failed: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
