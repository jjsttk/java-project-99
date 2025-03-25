package hexlet.code.app.service.task;

import hexlet.code.app.component.specification.TaskSpecification;
import hexlet.code.app.dto.task.TaskCreateDTO;
import hexlet.code.app.dto.task.TaskDTO;
import hexlet.code.app.dto.task.TaskParamsDTO;
import hexlet.code.app.dto.task.TaskUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskMapper;
import hexlet.code.app.model.Task;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.service.BaseService;
import hexlet.code.app.utils.ExceptionMessage;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public final class TaskService implements BaseService {

    private final TaskRepository taskRepository;
    private final TaskMapper mapper;
    private final TaskSpecification taskSpecification;


    public List<TaskDTO> getAll(TaskParamsDTO params) {
        Specification<Task> specification = taskSpecification.build(params);
        List<Task> filteredEntities = taskRepository.findAll(specification);
        return filteredEntities.stream()
                .map(mapper::map)
                .toList();
    }

    public TaskDTO getById(Long id) {
        var entity = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ExceptionMessage.entityNotFoundMessage(Task.class, id)));
        return mapper.map(entity);
    }

    public TaskDTO create(TaskCreateDTO createDTO) {
        var entity = mapper.map(createDTO);
        var saved = taskRepository.save(entity);
        return mapper.map(entity);
    }

    public TaskDTO update(TaskUpdateDTO updateDTO, Long id) {
        var entity = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ExceptionMessage.entityNotFoundMessage(Task.class, id)));
        mapper.update(updateDTO, entity);
        taskRepository.save(entity);
        return mapper.map(entity);
    }

    public Long totalCount() {
        return taskRepository.count();
    }

    public void delete(Long id) {
        taskRepository.deleteById(id);
    }
}
