package hexlet.code.app.service;

import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.BaseMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import hexlet.code.app.utils.ExceptionMessage;

/**
 * Abstract service class providing common CRUD operations.
 *
 * @param <T>         The entity type
 * @param <DTO>       The DTO type
 * @param <CreateDTO> The DTO type used for creation
 * @param <UpdateDTO> The DTO type used for updates
 */
@AllArgsConstructor
public abstract class BaseService<T, DTO, CreateDTO, UpdateDTO> {
    private final JpaRepository<T, Long> repository;
    private final BaseMapper<T, DTO, CreateDTO, UpdateDTO> mapper;
    private final Class<T> entityClass;

    /**
     * Retrieves all entities and maps them to DTOs.
     *
     * @return List of DTOs representing all entities.
     */
    public List<DTO> getAll() {
        return repository.findAll().stream()
                .map(mapper::mapToDTO)
                .toList();
    }

    /**
     * Retrieves an entity by its ID and maps it to a DTO.
     *
     * @param id The ID of the entity to retrieve.
     * @return The corresponding DTO.
     * @throws ResourceNotFoundException if the entity is not found.
     */
    public DTO getById(Long id) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ExceptionMessage.entityNotFoundMessage(entityClass, id)));
        return mapper.mapToDTO(entity);
    }

    /**
     * Creates a new entity from the provided DTO.
     *
     * @param createDTO The DTO containing the data for the new entity.
     * @return The DTO representing the created entity.
     */
    public DTO create(CreateDTO createDTO) {
        var entity = mapper.mapToEntity(createDTO);
        repository.save(entity);
        return mapper.mapToDTO(entity);
    }

    /**
     * Updates an existing entity with data from the provided DTO.
     *
     * @param updateDTO The DTO containing the updated data.
     * @param id        The ID of the entity to update.
     * @return The updated DTO.
     * @throws ResourceNotFoundException if the entity is not found.
     */
    public DTO update(UpdateDTO updateDTO, Long id) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ExceptionMessage.entityNotFoundMessage(entityClass, id)));
        mapper.update(updateDTO, entity);
        repository.save(entity);
        return mapper.mapToDTO(entity);
    }

    /**
     * Deletes an entity by its ID.
     *
     * @param id The ID of the entity to delete.
     * @throws ResourceNotFoundException if the entity does not exist.
     */
    public void delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
        } else {
            throw new ResourceNotFoundException(
                    ExceptionMessage.entityNotFoundMessage(entityClass, id));
        }
    }

    /**
     * Returns the total count of entities in the repository.
     *
     * @return The total number of entities.
     */
    public Long totalCount() {
        return repository.count();
    }
}
