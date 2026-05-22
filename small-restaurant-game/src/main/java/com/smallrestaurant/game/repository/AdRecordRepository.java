package com.smallrestaurant.game.repository;
import com.smallrestaurant.game.entity.AdRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface AdRecordRepository extends JpaRepository<AdRecord, String> {
}
