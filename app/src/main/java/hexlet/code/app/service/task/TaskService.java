package hexlet.code.app.service.task;

import hexlet.code.app.dto.task.TaskCreateDTO;
import hexlet.code.app.dto.task.TaskDTO;
import hexlet.code.app.dto.task.TaskUpdateDTO;
import hexlet.code.app.mapper.TaskMapper;
import hexlet.code.app.model.Task;
import hexlet.code.app.service.BaseService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public final class TaskService extends BaseService<Task, TaskDTO, TaskCreateDTO, TaskUpdateDTO> {

    public TaskService(JpaRepository<Task, Long> repository, TaskMapper mapper) {
        super(repository, mapper, Task.class);
    }
}
