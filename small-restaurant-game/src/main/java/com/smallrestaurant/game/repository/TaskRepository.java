package com.smallrestaurant.game.repository;
import com.smallrestaurant.game.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByTaskType(int taskType);
}
