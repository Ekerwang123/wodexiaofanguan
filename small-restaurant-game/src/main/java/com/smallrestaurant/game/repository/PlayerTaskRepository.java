package com.smallrestaurant.game.repository;
import com.smallrestaurant.game.entity.PlayerTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface PlayerTaskRepository extends JpaRepository<PlayerTask, Long> {
    List<PlayerTask> findByPlayerId(Long playerId);
    Optional<PlayerTask> findByPlayerIdAndTaskId(Long playerId, Long taskId);
}
