package com.smallrestaurant.game.controller;
import com.smallrestaurant.game.entity.Dish;
import com.smallrestaurant.game.entity.GuestType;
import com.smallrestaurant.game.entity.Player;
import com.smallrestaurant.game.repository.DishRepository;
import com.smallrestaurant.game.repository.GuestTypeRepository;
import com.smallrestaurant.game.repository.PlayerRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/guest")
public class GuestController {
    @Autowired
    private GuestTypeRepository guestTypeRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private DishRepository dishRepository;
    @GetMapping("/list")
    public ResponseEntity<?> getGuestList(HttpServletRequest request) {
        try {
            Long playerId = (Long) request.getAttribute("playerId");
            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new RuntimeException("玩家不存在"));
            List<GuestType> allGuests = guestTypeRepository.findAll();
            List<Map<String, Object>> guestList = new ArrayList<>();
            for (GuestType gt : allGuests) {
                boolean unlocked = gt.getUnlockLevel() <= player.getLevel();
                Map<String, Object> item = new HashMap<>();
                item.put("guestTypeId", gt.getGuestTypeId());
                item.put("guestName", gt.getGuestName());
                item.put("avatar", gt.getAvatar());
                item.put("unlockLevel", gt.getUnlockLevel());
                item.put("unlocked", unlocked);
                if (unlocked) {
                    Dish dish = dishRepository.findById(gt.getDishId()).orElse(null);
                    item.put("dishId", gt.getDishId());
                    item.put("dishName", dish != null ? dish.getName() : null);
                } else {
                    item.put("dishId", null);
                    item.put("dishName", null);
                }
                guestList.add(item);
            }
            Map<String, Object> data = new HashMap<>();
            data.put("playerId", playerId);
            data.put("playerLevel", player.getLevel());
            data.put("guests", guestList);
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
