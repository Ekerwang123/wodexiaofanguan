package com.smallrestaurant.game.service;
import com.smallrestaurant.game.entity.AdRecord;
import com.smallrestaurant.game.repository.AdRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
@Service
public class AdService {
    @Autowired
    private AdRecordRepository adRecordRepository;
    @Autowired
    private GameService gameService;
    @Autowired
    private PlayerService playerService;
    public AdRecord createAdRecord(Long playerId, String adType, int rewardAmount, String params) {
        String adId = java.util.UUID.randomUUID().toString().replace("-", "");
        AdRecord record = new AdRecord();
        record.setAdId(adId);
        record.setPlayerId(playerId);
        record.setAdType(adType);
        record.setRewardAmount(rewardAmount);
        record.setStatus(0);
        record.setParams(params);
        return adRecordRepository.save(record);
    }
    @Transactional
    public Map<String, Object> verifyAndReward(Long playerId, String adId) {
        AdRecord record = adRecordRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("广告记录不存在"));
        if (!record.getPlayerId().equals(playerId)) {
            throw new RuntimeException("广告记录与玩家不匹配");
        }
        if (record.getStatus() == 1) {
            throw new RuntimeException("奖励已领取，不可重复领取");
        }
        Map<String, Object> rewardData;
        switch (record.getAdType()) {
            case "batch":
                int count = record.getRewardAmount();
                rewardData = gameService.inviteBatchGuests(playerId, count);
                break;
            case "accelerate":
                Long stoveId = Long.valueOf(record.getParams());
                rewardData = gameService.accelerateCook(playerId, stoveId);
                break;
            case "double":
                playerService.enableDoubleReward(playerId);
                rewardData = new HashMap<>();
                rewardData.put("doubleReward", true);
                break;
            default:
                throw new RuntimeException("未知的广告类型: " + record.getAdType());
        }
        record.setStatus(1);
        adRecordRepository.save(record);
        Map<String, Object> result = new HashMap<>();
        result.put("adId", adId);
        result.put("adType", record.getAdType());
        result.put("reward", rewardData);
        return result;
    }
}
