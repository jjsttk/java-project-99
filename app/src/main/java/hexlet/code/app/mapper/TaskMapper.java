package hexlet.code.app.mapper;

import hexlet.code.app.dto.task.TaskCreateDTO;
import hexlet.code.app.dto.task.TaskDTO;
import hexlet.code.app.dto.task.TaskUpdateDTO;
import hexlet.code.app.model.Label;
import hexlet.code.app.model.Task;
import org.mapstruct.*;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class, LabelMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class TaskMapper implements BaseMapper<Task, TaskDTO,
        TaskCreateDTO, TaskUpdateDTO> {

    @Override
    @Mapping(source = "labels", target = "taskLabelIds")
    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(source = "name", target = "title")
    @Mapping(source = "description", target = "content")
    @Mapping(source = "taskStatus.slug", target = "status")
    public abstract TaskDTO mapToDTO(Task task);

    @Override
    @Mapping(source = "title", target = "name")
    @Mapping(source = "content", target = "description")
    @Mapping(source = "status", target = "taskStatus")
    @Mapping(source = "assigneeId", target = "assignee")
    @Mapping(source = "taskLabelIds", target = "labels")
    public abstract Task mapToEntity(TaskCreateDTO createDTO);

    @Override
    @Mapping(source = "title", target = "name")
    @Mapping(source = "content", target = "description")
    @Mapping(source = "status", target = "taskStatus")
    @Mapping(source = "assigneeId", target = "assignee")
    @Mapping(source = "taskLabelIds", target = "labels")
    public abstract void update(TaskUpdateDTO updateDTO, @MappingTarget Task entity);
}
