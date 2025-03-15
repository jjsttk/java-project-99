package hexlet.code.app.mapper;

import hexlet.code.app.dto.task.TaskCreateDTO;
import hexlet.code.app.dto.task.TaskDTO;
import hexlet.code.app.dto.task.TaskUpdateDTO;
import hexlet.code.app.model.Label;
import hexlet.code.app.model.Task;
import hexlet.code.app.repository.LabelRepository;
import lombok.extern.log4j.Log4j2;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;

/**
 * A mapper for converting between {@link Task}, {@link TaskDTO}, {@link TaskCreateDTO},
 * and {@link TaskUpdateDTO}.
 * <p>
 * This class provides methods for mapping task-related DTOs to task entities and vice versa.
 * It also maps task properties like labels, assignee, and status between the corresponding objects.
 * </p>
 */
@Log4j2
@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class, TaskStatusMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class TaskMapper implements BaseMapper {

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private ReferenceMapper referenceMapper;

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
    @Mapping(source = "labels", target = "taskLabelIds")
    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(source = "name", target = "title")
    @Mapping(source = "description", target = "content")
    @Mapping(source = "taskStatus.slug", target = "status")
    public abstract TaskDTO map(Task task);

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
    @Mapping(source = "title", target = "name")
    @Mapping(source = "content", target = "description")
    @Mapping(source = "status", target = "taskStatus")
    @Mapping(source = "assigneeId", target = "assignee")
    @Mapping(source = "taskLabelIds", target = "labels")
    public abstract Task map(TaskCreateDTO createDTO);

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
    @Mapping(source = "title", target = "name")
    @Mapping(source = "content", target = "description")
    @Mapping(source = "status", target = "taskStatus")
    @Mapping(source = "assigneeId", target = "assignee")
    @Mapping(source = "taskLabelIds", target = "labels", qualifiedByName = "convertTaskLabelIdsToLabels")
    public abstract void update(TaskUpdateDTO updateDTO, @MappingTarget Task entity);

    /**
     * Sets the labels for a task entity after mapping from {@link TaskCreateDTO}.
     * <p>
     * This method is executed after the primary mapping process to ensure that labels
     * are properly fetched from the database and assigned to the {@link Task} entity.
     * </p>
     *
     * @param createDTO The DTO containing label IDs.
     * @param task      The task entity where the labels will be set.
     */
    @AfterMapping
    protected void setLabels(TaskCreateDTO createDTO, @MappingTarget Task task) {
        if (createDTO.getTaskLabelIds() != null) {
            var labels = labelRepository.findAllById(createDTO.getTaskLabelIds());
            task.setLabels(new HashSet<>((labels)));
        }
    }
    /**
     * Converts a set of task label IDs into a set of {@link Label} entities.
     * <p>
     * This method is used by MapStruct to automatically map sets of label IDs
     * into corresponding label entities using {@link ReferenceMapper}.
     * </p>
     *
     * @param taskLabelIds The set of label IDs to convert.
     * @return The corresponding set of {@link Label} entities.
     */
    @Named("convertTaskLabelIdsToLabels")
    public Set<Label> convertTaskLabelIdsToLabels(Set<Long> taskLabelIds) {
        return referenceMapper.mapSetLongToSetEntity(taskLabelIds, Label.class);
    }



}

