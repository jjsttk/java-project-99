package hexlet.code.app.mapper;

import hexlet.code.app.dto.task.label.LabelDTO;
import hexlet.code.app.dto.task.label.LabelCreateDTO;
import hexlet.code.app.dto.task.label.LabelUpdateDTO;
import hexlet.code.app.model.Label;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper class responsible for converting between {@link Label} entities and DTOs.
 * <p>
 * This class uses {@link JsonNullableMapper} and {@link ReferenceMapper} to handle conversion
 * between entities and DTOs. It ensures that null values are ignored during the mapping process.
 * </p>
 */
@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class LabelMapper implements BaseMapper {

    public abstract Label map(LabelCreateDTO createDTO);
    public abstract LabelDTO map(Label model);
    public abstract void update(LabelUpdateDTO updateDTO, @MappingTarget Label label);
}
