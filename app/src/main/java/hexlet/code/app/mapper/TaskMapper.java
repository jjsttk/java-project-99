package hexlet.code.app.mapper;

import hexlet.code.app.dto.task.TaskCreateDTO;
import hexlet.code.app.dto.task.TaskDTO;
import hexlet.code.app.dto.task.TaskUpdateDTO;
import hexlet.code.app.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * A mapper for converting between {@link Task}, {@link TaskDTO}, {@link TaskCreateDTO},
 * and {@link TaskUpdateDTO}.
 * <p>
 * This class provides methods for mapping task-related DTOs to task entities and vice versa.
 * It also maps task properties like labels, assignee, and status between the corresponding objects.
 * </p>
 */
@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class, LabelMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class TaskMapper implements BaseMapper<Task, TaskDTO,
        TaskCreateDTO, TaskUpdateDTO> {

    /**
     * Maps a {@link Task} entity to a {@link TaskDTO}.
     * <p>
     * This method maps task properties, including labels, assignee, and status,
     * to the corresponding DTO fields.
     * </p>
     *
     * @param task the {@link Task} entity to map.
     * @return the corresponding {@link TaskDTO}.
     */
    @Override
    @Mapping(source = "labels", target = "taskLabelIds")
    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(source = "name", target = "title")
    @Mapping(source = "description", target = "content")
    @Mapping(source = "taskStatus.slug", target = "status")
    public abstract TaskDTO mapToDTO(Task task);

    /**
     * Maps a {@link TaskCreateDTO} to a {@link Task} entity.
     * <p>
     * This method maps the fields from the create DTO to the task entity,
     * including task labels, assignee, and status.
     * </p>
     *
     * @param createDTO the {@link TaskCreateDTO} to map.
     * @return the corresponding {@link Task} entity.
     */
    @Override
    @Mapping(source = "title", target = "name")
    @Mapping(source = "content", target = "description")
    @Mapping(source = "status", target = "taskStatus")
    @Mapping(source = "assigneeId", target = "assignee")
    @Mapping(source = "taskLabelIds", target = "labels")
    public abstract Task mapToEntity(TaskCreateDTO createDTO);

    /**
     * Updates an existing {@link Task} entity using data from a {@link TaskUpdateDTO}.
     * <p>
     * This method updates the task entity with new data, including task labels,
     * assignee, and status.
     * </p>
     *
     * @param updateDTO the {@link TaskUpdateDTO} containing the updated data.
     * @param entity    the {@link Task} entity to update.
     */
    @Override
    @Mapping(source = "title", target = "name")
    @Mapping(source = "content", target = "description")
    @Mapping(source = "status", target = "taskStatus")
    @Mapping(source = "assigneeId", target = "assignee")
    @Mapping(source = "taskLabelIds", target = "labels")
    public abstract void update(TaskUpdateDTO updateDTO, @MappingTarget Task entity);

}

