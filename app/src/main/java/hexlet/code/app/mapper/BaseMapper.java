package hexlet.code.app.mapper;

import org.mapstruct.MappingTarget;

/**
 * A base interface for mapping between entities and DTOs.
 * <p>
 * This interface defines the standard methods for converting entities to DTOs,
 * creating entities from DTOs, and updating existing entities from update DTOs.
 * </p>
 *
 * @param <T>       the entity type.
 * @param <DTO>     the DTO type.
 * @param <CreateDTO> the DTO type for creation.
 * @param <UpdateDTO> the DTO type for updating.
 */
public interface BaseMapper<T, DTO, CreateDTO, UpdateDTO> {

    /**
     * Maps an entity to a DTO.
     * <p>
     * This method converts the given entity to its corresponding DTO.
     * </p>
     *
     * @param entity the entity to map.
     * @return the mapped DTO.
     */
    DTO mapToDTO(T entity);

    /**
     * Maps a DTO to an entity for creation.
     * <p>
     * This method converts the given create DTO to its corresponding entity.
     * </p>
     *
     * @param createDTO the DTO containing data for entity creation.
     * @return the mapped entity.
     */
    T mapToEntity(CreateDTO createDTO);

    /**
     * Updates an existing entity using data from an update DTO.
     * <p>
     * This method updates the given entity with values from the provided update DTO.
     * </p>
     *
     * @param updateDTO the DTO containing updated data.
     * @param entity    the entity to update.
     */
    void update(UpdateDTO updateDTO, @MappingTarget T entity);
}
