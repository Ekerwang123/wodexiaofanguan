package com.smallrestaurant.game.repository;
import com.smallrestaurant.game.entity.WithdrawRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface WithdrawRecordRepository extends JpaRepository<WithdrawRecord, Long> {
    List<WithdrawRecord> findByPlayerIdOrderByCreateTimeDesc(Long playerId);
}
