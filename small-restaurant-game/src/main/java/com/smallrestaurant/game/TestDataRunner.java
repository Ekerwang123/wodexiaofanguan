package com.smallrestaurant.game;
import com.smallrestaurant.game.model.TableState;
import com.smallrestaurant.game.service.GameService;
import com.smallrestaurant.game.service.TableService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;
@Configuration
public class TestDataRunner {
    @Bean
    CommandLineRunner testApis(TableService tableService, GameService gameService, Environment env, RestTemplateBuilder restTemplateBuilder) {
        return args -> {
            String port = env.getProperty("local.server.port", "52341");
            String baseUrl = "http://localhost:" + port;
            RestTemplate restTemplate = restTemplateBuilder.build();
            System.out.println("===== 自动测试开始 =====");
            System.out.println("\n--- 测试 POST /api/player/login (新用户注册) ---");
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                String body = "{\"username\":\"testrunner\",\"password\":\"123456\",\"nickname\":\"TestRunner\"}";
                HttpEntity<String> request = new HttpEntity<>(body, headers);
                @SuppressWarnings("unchecked")
                Map<String, Object> result = restTemplate.postForObject(baseUrl + "/api/player/login", request, Map.class);
                System.out.println("success: " + result.get("success"));
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                System.out.println("data.playerId: " + data.get("playerId"));
                System.out.println("data.nickname: " + data.get("nickname"));
                System.out.println("data.level: " + data.get("level"));
                System.out.println("data.balance: " + data.get("balance"));
            } catch (Exception e) {
                System.out.println("新用户注册异常: " + e.getMessage());
            }
            System.out.println("\n--- 测试 POST /api/player/login (已存在用户登录) ---");
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                String body = "{\"username\":\"testrunner\",\"password\":\"123456\"}";
                HttpEntity<String> request = new HttpEntity<>(body, headers);
                @SuppressWarnings("unchecked")
                Map<String, Object> result = restTemplate.postForObject(baseUrl + "/api/player/login", request, Map.class);
                System.out.println("success: " + result.get("success"));
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                System.out.println("data.playerId: " + data.get("playerId"));
                System.out.println("data.nickname: " + data.get("nickname"));
                System.out.println("data.level: " + data.get("level"));
                System.out.println("data.balance: " + data.get("balance"));
            } catch (Exception e) {
                System.out.println("已存在用户登录异常: " + e.getMessage());
            }
            System.out.println("\n--- 测试上菜流程 (模拟) ---");
            try {
                Long playerId = 15L;
                List<TableState> tables = tableService.getPlayerTables(playerId);
                if (!tables.isEmpty()) {
                    TableState table = tables.get(0);
                    table.setStatus("waiting");
                    table.setGuestName("TestGuest");
                    table.setRequiredDish("麻婆豆腐");
                    tableService.updateTableState(playerId, table.getTableId(), table);
                    System.out.println("模拟客人入座: tableId=" + table.getTableId() + " guest=TestGuest requiredDish=麻婆豆腐");
                    System.out.println("上菜测试: 请通过 POST /api/game/serve 手动测试");
                } else {
                    System.out.println("玩家无餐桌数据，跳过上菜测试");
                }
            } catch (Exception e) {
                System.out.println("上菜测试异常: " + e.getMessage());
            }
            System.out.println("\n===== 自动测试结束 =====");
        };
    }
}
