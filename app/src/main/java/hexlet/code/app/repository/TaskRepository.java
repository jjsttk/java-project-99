package hexlet.code.app.repository;

import hexlet.code.app.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    boolean existsByAssigneeId(Long userId);
    boolean existsByTaskStatusId(Long taskStatusId);
}
