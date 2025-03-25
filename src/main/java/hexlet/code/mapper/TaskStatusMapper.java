package hexlet.code.mapper;


import hexlet.code.dto.task.status.TaskStatusCreateDTO;
import hexlet.code.dto.task.status.TaskStatusDTO;
import hexlet.code.dto.task.status.TaskStatusUpdateDTO;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

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
public abstract class TaskStatusMapper implements BaseMapper {
    @Autowired
    private TaskStatusRepository taskStatusRepository;

    public abstract TaskStatus map(TaskStatusCreateDTO createDTO);
    public abstract TaskStatusDTO map(TaskStatus model);
    public abstract void update(TaskStatusUpdateDTO updateDTO, @MappingTarget TaskStatus model);

    /**
     * Maps a task status slug to the corresponding {@link TaskStatus} entity.
     * <p>
     * This method fetches the task status from the repository based on the provided slug.
     * If no matching entity is found, it returns {@code null}.
     * </p>
     *
     * @param slug The unique identifier (slug) of the task status.
     * @return The corresponding {@link TaskStatus} entity or {@code null} if not found.
     */
    protected TaskStatus mapSlugToEntity(String slug) {
        return slug == null ? null : taskStatusRepository.findBySlug(slug)
                .orElse(null);
    }
}
