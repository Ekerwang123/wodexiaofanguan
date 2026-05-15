package com.smallrestaurant.game;
import com.smallrestaurant.game.entity.Player;
import com.smallrestaurant.game.model.TableState;
import com.smallrestaurant.game.service.PlayerService;
import com.smallrestaurant.game.service.TableService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;
@Configuration
public class TestDataRunner {
    @Bean
    CommandLineRunner testApis(PlayerService playerService, TableService tableService) {
        return args -> {
            System.out.println("===== 自动测试开始 =====");
            System.out.println("\n--- 测试 /api/player/info ---");
            try {
                Player player = playerService.loginOrRegister("autotest", "123456", "AutoTest");
                System.out.println("playerId: " + player.getPlayerId());
                System.out.println("nickname: " + player.getNickname());
                System.out.println("level: " + player.getLevel());
                System.out.println("balance: " + player.getBalance());
                System.out.println("redPacket: " + player.getRedPacket());
                System.out.println("likeCount: " + player.getLikeCount());
                System.out.println("totalGuests: " + player.getTotalGuests());
            } catch (Exception e) {
                System.out.println("玩家接口异常: " + e.getMessage());
            }
            System.out.println("\n--- 测试 /api/game/tables ---");
            try {
                List<TableState> tables = tableService.getPlayerTables(2L);
                System.out.println("餐桌数量: " + tables.size());
                for (TableState t : tables) {
                    System.out.println("  餐桌#" + t.getTableId()
                            + " 位置(" + t.getPositionX() + "," + t.getPositionY() + ")"
                            + " 解锁=" + t.isUnlocked()
                            + " 状态=" + t.getStatus()
                            + " 客人=" + t.getGuestName()
                            + " 需求=" + t.getRequiredDish());
                }
            } catch (Exception e) {
                System.out.println("餐桌接口异常: " + e.getMessage());
            }
            System.out.println("\n===== 自动测试结束 =====");
        };
    }
}
