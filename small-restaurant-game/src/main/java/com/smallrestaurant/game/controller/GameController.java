package com.smallrestaurant.game.controller;
import com.smallrestaurant.game.model.TableState;
import com.smallrestaurant.game.service.AdService;
import com.smallrestaurant.game.service.GameService;
import com.smallrestaurant.game.service.PlayerService;
import com.smallrestaurant.game.service.TableService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/game")
public class GameController {
    @Autowired
    private TableService tableService;
    @Autowired
    private GameService gameService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private AdService adService;
    @GetMapping("/tables")
    public ResponseEntity<?> getTables(HttpServletRequest request) {
        try {
            Long playerId = (Long) request.getAttribute("playerId");
            List<TableState> tables = tableService.getPlayerTables(playerId);
            List<Map<String, Object>> tableList = new ArrayList<>();
            for (TableState t : tables) {
                Map<String, Object> item = new HashMap<>();
                item.put("tableId", t.getTableId());
                item.put("positionX", t.getPositionX());
                item.put("positionY", t.getPositionY());
                item.put("unlocked", t.isUnlocked());
                item.put("status", t.getStatus());
                item.put("guestName", t.getGuestName());
                item.put("patience", t.getPatience());
                item.put("needDish", t.getRequiredDish());
                item.put("servingDishId", t.getServingDishId());
                tableList.add(item);
            }
            Map<String, Object> data = new HashMap<>();
            data.put("playerId", playerId);
            data.put("tables", tableList);
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
    @GetMapping("/stoves")
    public ResponseEntity<?> getStoves(HttpServletRequest request) {
        try {
            Long playerId = (Long) request.getAttribute("playerId");
            List<Map<String, Object>> stoves = gameService.getPlayerStoveStatus(playerId);
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
    @PostMapping("/cook")
    public ResponseEntity<?> cook(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            Long playerId = (Long) request.getAttribute("playerId");
            Long stoveId = Long.valueOf(body.get("stoveId").toString());
            Long dishId = Long.valueOf(body.get("dishId").toString());
            Map<String, Object> data = gameService.cook(playerId, stoveId, dishId);
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
    @PostMapping("/serve")
    public ResponseEntity<?> serve(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            Long playerId = (Long) request.getAttribute("playerId");
            Long tableId = Long.valueOf(body.get("tableId").toString());
            Long stoveId = Long.valueOf(body.get("stoveId").toString());
            Map<String, Object> data = gameService.serve(playerId, tableId, stoveId);
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
    @PostMapping("/settle")
    public ResponseEntity<?> settle(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            Long playerId = (Long) request.getAttribute("playerId");
            Long tableId = Long.valueOf(body.get("tableId").toString());
            Map<String, Object> data = gameService.settle(playerId, tableId);
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
    @PostMapping("/guest/sit")
    public ResponseEntity<?> guestSit(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            Long playerId = (Long) request.getAttribute("playerId");
            Long tableId = Long.valueOf(body.get("tableId").toString());
            String dishName = body.get("dishName") != null ? body.get("dishName").toString() : null;
            Map<String, Object> data = gameService.guestSit(playerId, tableId, dishName);
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
    @PostMapping("/double-reward")
    public ResponseEntity<?> enableDoubleReward(HttpServletRequest request) {
        try {
            Long playerId = (Long) request.getAttribute("playerId");
            playerService.getPlayerById(playerId);
            com.smallrestaurant.game.entity.AdRecord record = adService.createAdRecord(playerId, "double", 1, null);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("adId", record.getAdId());
            result.put("message", "请观看广告后领取双倍收益奖励");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
    @PostMapping("/cook-accelerate")
    public ResponseEntity<?> accelerateCook(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            Long playerId = (Long) request.getAttribute("playerId");
            Long stoveId = Long.valueOf(body.get("stoveId").toString());
            gameService.getCookingTask(stoveId);
            com.smallrestaurant.game.entity.AdRecord record = adService.createAdRecord(playerId, "accelerate", 1, stoveId.toString());
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("adId", record.getAdId());
            result.put("message", "请观看广告后领取加速烹饪奖励");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
    @PostMapping("/invite-batch")
    public ResponseEntity<?> inviteBatchGuests(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            Long playerId = (Long) request.getAttribute("playerId");
            int count = body.containsKey("count") ? Integer.parseInt(body.get("count").toString()) : 12;
            com.smallrestaurant.game.entity.AdRecord record = adService.createAdRecord(playerId, "batch", count, null);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("adId", record.getAdId());
            result.put("message", "请观看广告后领取一键揽客奖励");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
    @PostMapping("/table/unlock")
    public ResponseEntity<?> unlockTable(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            Long playerId = (Long) request.getAttribute("playerId");
            int tableIndex = Integer.parseInt(body.get("tableIndex").toString());
            Map<String, Object> data = tableService.unlockTable(playerId, tableIndex);
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
