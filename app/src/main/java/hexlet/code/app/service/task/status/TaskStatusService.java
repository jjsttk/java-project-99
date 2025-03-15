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
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@AllArgsConstructor
public final class TaskStatusService implements BaseService {

    private final TaskStatusRepository taskStatusRepository;
    private final TaskRepository taskRepository;
    private final TaskStatusMapper mapper;


    public List<TaskStatusDTO> getAll() {
        return taskStatusRepository.findAll().stream()
                .map(mapper::map)
                .toList();
    }

    public TaskStatusDTO getById(Long id) {
        var entity = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ExceptionMessage.entityNotFoundMessage(TaskStatus.class, id)));
        return mapper.map(entity);
    }

    public TaskStatusDTO create(TaskStatusCreateDTO createDTO) {
        var entity = mapper.map(createDTO);
        var saved = taskStatusRepository.save(entity);
        return mapper.map(entity);
    }

    public TaskStatusDTO update(TaskStatusUpdateDTO updateDTO, Long id) {
        var entity = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ExceptionMessage.entityNotFoundMessage(TaskStatus.class, id)));
        mapper.update(updateDTO, entity);
        taskStatusRepository.save(entity);
        return mapper.map(entity);
    }

    public Long totalCount() {
        return taskStatusRepository.count();
    }

    public void delete(Long id) {
        var hasTasks = taskRepository.existsByTaskStatusId(id);
        if (hasTasks) {
            throw new IllegalStateException(
                    "Cannot delete task status with id = " + id
                            + " , because it is used in at least one task.");
        }
        taskStatusRepository.deleteById(id);
    }


    public TaskStatusDTO getBySlug(String slug) {
        var mbTaskStatus = taskStatusRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ExceptionMessage.entityNotFoundMessage(TaskStatus.class, slug)));
        return mapper.map(mbTaskStatus);
    }
}
