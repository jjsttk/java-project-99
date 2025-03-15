package hexlet.code.app.service.task.status;

import hexlet.code.app.dto.task.status.TaskStatusCreateDTO;
import hexlet.code.app.dto.task.status.TaskStatusDTO;
import hexlet.code.app.dto.task.status.TaskStatusUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskStatusMapper;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.service.BaseService;
import hexlet.code.app.utils.ExceptionMessage;
import org.springframework.stereotype.Service;



@Service
public final class TaskStatusService extends BaseService<TaskStatus, TaskStatusDTO,
        TaskStatusCreateDTO, TaskStatusUpdateDTO> {

    private final TaskStatusRepository repository;
    private final TaskRepository taskRepository;
    private final TaskStatusMapper mapper;

    public TaskStatusService(TaskStatusRepository taskStatusRepository, TaskRepository taskRepo,
                             TaskStatusMapper taskStatusMapper) {
        super(taskStatusRepository, taskStatusMapper, TaskStatus.class);
        this.repository = taskStatusRepository;
        this.taskRepository = taskRepo;

        this.mapper = taskStatusMapper;
    }


    @Override
    public void delete(Long id) {
        var hasTasks = taskRepository.existsByTaskStatusId(id);
        if (hasTasks) {
            throw new IllegalStateException(
                    "Cannot delete task status with id = " + id
                            + " , because it is used in at least one task.");
        }
        repository.deleteById(id);
    }


    public TaskStatusDTO getBySlug(String slug) {
        var mbTaskStatus = repository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ExceptionMessage.entityNotFoundMessage(TaskStatus.class, slug)));
        return mapper.mapToDTO(mbTaskStatus);
    }
}
