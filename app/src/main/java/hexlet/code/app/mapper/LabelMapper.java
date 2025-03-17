package hexlet.code.app.mapper;

import hexlet.code.app.dto.task.label.LabelDTO;
import hexlet.code.app.dto.task.label.LabelCreateDTO;
import hexlet.code.app.dto.task.label.LabelUpdateDTO;
import hexlet.code.app.model.Label;
import org.mapstruct.*;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class LabelMapper implements BaseMapper<Label, LabelDTO,
        LabelCreateDTO, LabelUpdateDTO>{

    protected Long labelToId(Label label) {
        return label == null ? null : label.getId();
    }

    protected Label toEntity(Long id) {
        if (id == null) {
            return null;
        }
        Label label = new Label();
        label.setId(id);
        return label;
    }

    protected List<Label> mapJsonNullableToLabels(JsonNullable<Set<Long>> ids) {
        if (ids != null && ids.isPresent()) {
            return ids.get().stream()
                    .map(this::toEntity)
                    .collect(Collectors.toList());
        }
        return null;
    }

}
