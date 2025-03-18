package hexlet.code.app.mapper;


import hexlet.code.app.dto.task.status.TaskStatusCreateDTO;
import hexlet.code.app.dto.task.status.TaskStatusDTO;
import hexlet.code.app.dto.task.status.TaskStatusUpdateDTO;
import hexlet.code.app.model.TaskStatus;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * A mapper for converting between {@link TaskStatus}, {@link TaskStatusDTO},
 * {@link TaskStatusCreateDTO}, and {@link TaskStatusUpdateDTO}.
 * <p>
 * This class provides methods for mapping task status-related DTOs
 * to task status entities and vice versa.
 * </p>
 */
@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class TaskStatusMapper implements BaseMapper<TaskStatus, TaskStatusDTO,
        TaskStatusCreateDTO, TaskStatusUpdateDTO> {

}
