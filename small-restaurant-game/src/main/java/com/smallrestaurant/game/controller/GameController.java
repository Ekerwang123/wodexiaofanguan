package com.smallrestaurant.game.controller;
import com.smallrestaurant.game.model.TableState;
import com.smallrestaurant.game.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/game")
public class GameController {
    @Autowired
    private TableService tableService;
    @GetMapping("/tables")
    public ResponseEntity<?> getTables(@RequestParam Long playerId) {
        try {
            List<TableState> tables = tableService.getPlayerTables(playerId);
            Map<String, Object> result = new HashMap<>();
            result.put("playerId", playerId);
            result.put("tables", tables);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
