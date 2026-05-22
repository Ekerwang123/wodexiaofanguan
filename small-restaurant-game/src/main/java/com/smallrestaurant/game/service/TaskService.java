package com.smallrestaurant.game.service;
import com.smallrestaurant.game.entity.Player;
import com.smallrestaurant.game.entity.PlayerTask;
import com.smallrestaurant.game.entity.Task;
import com.smallrestaurant.game.repository.PlayerRepository;
import com.smallrestaurant.game.repository.PlayerTaskRepository;
import com.smallrestaurant.game.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private PlayerTaskRepository playerTaskRepository;
    @Autowired
    private PlayerRepository playerRepository;

    @Transactional
    public void initPlayerTasks(Long playerId) {
        List<Task> allTasks = taskRepository.findAll();
        for (Task task : allTasks) {
            if (!playerTaskRepository.findByPlayerIdAndTaskId(playerId, task.getTaskId()).isPresent()) {
                playerTaskRepository.save(new PlayerTask(playerId, task.getTaskId()));
            }
        }
    }

    @Transactional
    public void updateProgress(Long playerId, int taskType, int increment) {
        List<Task> tasks = taskRepository.findByTaskType(taskType);
        for (Task task : tasks) {
            PlayerTask pt = playerTaskRepository.findByPlayerIdAndTaskId(playerId, task.getTaskId())
                    .orElse(null);
            if (pt == null || pt.isCompleted()) {
                continue;
            }
            pt.setCurrentCount(pt.getCurrentCount() + increment);
            if (pt.getCurrentCount() >= task.getTargetCount()) {
                pt.setCurrentCount(task.getTargetCount());
                pt.setCompleted(true);
            }
            playerTaskRepository.save(pt);
        }
    }

    @Transactional
    public Map<String, Object> claimReward(Long playerId, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        PlayerTask pt = playerTaskRepository.findByPlayerIdAndTaskId(playerId, taskId)
                .orElseThrow(() -> new RuntimeException("玩家任务不存在"));
        if (!pt.isCompleted()) {
            throw new RuntimeException("任务尚未完成");
        }
        if (pt.isClaimed()) {
            throw new RuntimeException("奖励已领取");
        }
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("玩家不存在"));
        if (task.getRewardType() == 1) {
            player.setRedPacket(player.getRedPacket().add(task.getRewardAmount()));
        } else if (task.getRewardType() == 2) {
            player.setBalance(player.getBalance() + task.getRewardAmount().intValue());
        }
        playerRepository.save(player);
        pt.setClaimed(true);
        playerTaskRepository.save(pt);
        Map<String, Object> result = new HashMap<>();
        result.put("taskId", taskId);
        result.put("rewardType", task.getRewardType());
        result.put("rewardAmount", task.getRewardAmount());
        result.put("balance", player.getBalance());
        result.put("redPacket", player.getRedPacket());
        return result;
    }

    public List<Map<String, Object>> getPlayerTaskList(Long playerId) {
        List<PlayerTask> playerTasks = playerTaskRepository.findByPlayerId(playerId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (PlayerTask pt : playerTasks) {
            Task task = taskRepository.findById(pt.getTaskId()).orElse(null);
            if (task == null) continue;
            Map<String, Object> item = new HashMap<>();
            item.put("taskId", task.getTaskId());
            item.put("taskType", task.getTaskType());
            item.put("taskDesc", task.getTaskDesc());
            item.put("targetCount", task.getTargetCount());
            item.put("currentCount", pt.getCurrentCount());
            item.put("completed", pt.isCompleted());
            item.put("claimed", pt.isClaimed());
            item.put("rewardType", task.getRewardType());
            item.put("rewardAmount", task.getRewardAmount());
            result.add(item);
        }
        return result;
    }
}
