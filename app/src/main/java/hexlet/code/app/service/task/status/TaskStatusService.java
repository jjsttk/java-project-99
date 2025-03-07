package hexlet.code.app.service.task.status;


import hexlet.code.app.dto.task.status.TaskStatusCreateDTO;
import hexlet.code.app.dto.task.status.TaskStatusDTO;
import hexlet.code.app.dto.task.status.TaskStatusUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskStatusMapper;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.utils.ExceptionMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TaskStatusService {
    private final TaskStatusRepository repository;
    private final TaskStatusMapper mapper;

    public List<TaskStatusDTO> getAll() {
        var models = repository.findAll();
        var listDTO = models.stream()
                .map(mapper::map)
                .toList();
        return listDTO;
    }

    public TaskStatusDTO getTaskStatusById(Long id) {
        var mbModel = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ExceptionMessage.taskStatusNotFoundMessage(id)));
        var dto = mapper.map(mbModel);
        return dto;
    }

    public TaskStatusDTO create(TaskStatusCreateDTO createDTO) {
        var user = mapper.map(createDTO);
        repository.save(user);
        var dto = mapper.map(user);
        return dto;
    }

    public TaskStatusDTO update(TaskStatusUpdateDTO updateDTO, Long maybeModelId) {
        var mbModel = repository.findById(maybeModelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ExceptionMessage.taskStatusNotFoundMessage(maybeModelId)));
        mapper.update(updateDTO, mbModel);
        repository.save(mbModel);

        var dto = mapper.map(repository.save(mbModel));
        return dto;
    }

    public Long getTotalCount() {
        return repository.count();
    }

    public void delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
        } else {
            throw new ResourceNotFoundException(ExceptionMessage.taskStatusNotFoundMessage(id));
        }
    }

}
