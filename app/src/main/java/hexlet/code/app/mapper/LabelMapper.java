package hexlet.code.app.mapper;

import hexlet.code.app.dto.task.label.LabelDTO;
import hexlet.code.app.dto.task.label.LabelCreateDTO;
import hexlet.code.app.dto.task.label.LabelUpdateDTO;
import hexlet.code.app.model.Label;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

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
public abstract class LabelMapper implements BaseMapper<Label, LabelDTO,
        LabelCreateDTO, LabelUpdateDTO> {

    @Autowired
    private ReferenceMapper referenceMapper;

    /**
     * Converts a {@link JsonNullable} set of task label IDs into a list of {@link Label} entities.
     * <p>
     * This method retrieves each {@link Label} corresponding to the given IDs from the database.
     * </p>
     *
     * @param taskLabelIds the {@link JsonNullable} set of task label IDs to convert.
     * @return a list of {@link Label} entities corresponding to the given IDs,
     * or {@code null} if the input is null or empty.
     */
    protected final List<Label> convertJsonNullableToLabelList(JsonNullable<Set<Long>> taskLabelIds) {
        return referenceMapper.mapJsonNullableSetToEntityList(taskLabelIds, Label.class);
    }

}
