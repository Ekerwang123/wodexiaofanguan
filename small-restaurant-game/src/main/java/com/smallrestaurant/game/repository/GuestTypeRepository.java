package com.smallrestaurant.game.repository;

import com.smallrestaurant.game.entity.GuestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GuestTypeRepository extends JpaRepository<GuestType, Long> {
    List<GuestType> findByUnlockLevelLessThanEqual(int level);
}
