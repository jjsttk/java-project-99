package hexlet.code.service.task;

import hexlet.code.component.specification.TaskSpecification;
import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskParamsDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.repository.TaskRepository;
import hexlet.code.service.BaseService;
import hexlet.code.utils.ExceptionMessage;
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
