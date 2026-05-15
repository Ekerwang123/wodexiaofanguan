package com.smallrestaurant.game.repository;
import com.smallrestaurant.game.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {
}
