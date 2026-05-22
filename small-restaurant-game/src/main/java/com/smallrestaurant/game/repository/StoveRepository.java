package com.smallrestaurant.game.repository;
import com.smallrestaurant.game.entity.Stove;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface StoveRepository extends JpaRepository<Stove, Long> {
    List<Stove> findByPlayerIdOrderByStoveIndex(Long playerId);
    Optional<Stove> findByPlayerIdAndStoveIndex(Long playerId, int stoveIndex);
}
