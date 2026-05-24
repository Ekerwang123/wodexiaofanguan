package com.smallrestaurant.game.repository;

import com.smallrestaurant.game.entity.AdRecord;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdRecordRepository extends JpaRepository<AdRecord, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AdRecord a WHERE a.adId = :adId")
    AdRecord findWithLockByAdId(@Param("adId") String adId);
}
