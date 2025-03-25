package hexlet.code.service.task.label;

import hexlet.code.dto.task.label.LabelCreateDTO;
import hexlet.code.dto.task.label.LabelDTO;
import hexlet.code.dto.task.label.LabelUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.service.BaseService;
import hexlet.code.utils.ExceptionMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public final class LabelService implements BaseService {
    private TaskRepository taskRepository;
    private LabelMapper mapper;
    private final LabelRepository labelRepository;

    public List<LabelDTO> getAll() {
        return labelRepository.findAll().stream()
                .map(mapper::map)
                .toList();
    }

    public LabelDTO getById(Long id) {
        var entity = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ExceptionMessage.entityNotFoundMessage(Label.class, id)));
        return mapper.map(entity);
    }

    public LabelDTO create(LabelCreateDTO createDTO) {
        var entity = mapper.map(createDTO);
        var saved = labelRepository.save(entity);
        return mapper.map(entity);
    }

    public LabelDTO update(LabelUpdateDTO updateDTO, Long id) {
        var entity = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ExceptionMessage.entityNotFoundMessage(Label.class, id)));
        mapper.update(updateDTO, entity);
        labelRepository.save(entity);
        return mapper.map(entity);
    }

    public Long totalCount() {
        return labelRepository.count();
    }

    public void delete(Long id) {
        var hasTasks = taskRepository.existsByLabelsId(id);
        if (hasTasks) {
            throw new IllegalStateException(
                    "Cannot delete label with id = " + id
                            + " , because it is used in at least one task.");
        }
        labelRepository.deleteById(id);
    }
}
