package com.smallrestaurant.game.repository;
import com.smallrestaurant.game.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    List<RestaurantTable> findByPlayerIdOrderByPositionX(Long playerId);
    Optional<RestaurantTable> findByPlayerIdAndPositionXAndPositionY(Long playerId, int positionX, int positionY);
}
