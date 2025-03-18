package hexlet.code.app.service.task.label;

import hexlet.code.app.dto.task.label.LabelCreateDTO;
import hexlet.code.app.dto.task.label.LabelDTO;
import hexlet.code.app.dto.task.label.LabelUpdateDTO;
import hexlet.code.app.mapper.LabelMapper;
import hexlet.code.app.model.Label;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.service.BaseService;
import org.springframework.stereotype.Service;

@Service
public final class LabelService extends BaseService<Label, LabelDTO, LabelCreateDTO, LabelUpdateDTO> {
    private final LabelRepository repository;

    public LabelService(LabelRepository labelRepository, LabelMapper labelMapper) {
        super(labelRepository, labelMapper, Label.class);
        this.repository = labelRepository;

    }

    @Override
    public void delete(Long id) {
        var hasTasks = repository.existsByTasksId(id);
        if (hasTasks) {
            throw new IllegalStateException(
                    "Cannot delete label with id = " + id
                            + " , because it is used in at least one task.");
        }
        repository.deleteById(id);
    }
}
