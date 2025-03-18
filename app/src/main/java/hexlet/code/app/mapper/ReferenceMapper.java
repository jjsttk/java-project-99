package hexlet.code.app.mapper;

import hexlet.code.app.model.BaseEntity;
import jakarta.persistence.EntityManager;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.TargetType;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A utility class for handling reference entity mappings.
 * <p>
 * This class provides methods for converting an ID
 * or a slug to an entity instance using the {@link EntityManager}.
 * It can convert IDs to entities of a given class type
 * and also allows retrieval of an entity using its slug.
 * </p>
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {JsonNullableMapper.class}
)
public abstract class ReferenceMapper {

    @Autowired
    private EntityManager entityManager;

    /**
     * Converts an ID to an entity of the specified type using the {@link EntityManager}.
     * <p>
     * This method retrieves an entity from the database based on the provided ID.
     * </p>
     *
     * @param id          the ID of the entity to retrieve.
     * @param entityClass the class type of the entity to return.
     * @param <T>         the type of the entity, which must extend {@link BaseEntity}.
     * @return the entity corresponding to the given ID, or {@code null} if the ID is {@code null}.
     */
    protected <T extends BaseEntity> T toEntity(Long id, @TargetType Class<T> entityClass) {
        return id != null ? entityManager.find(entityClass, id) : null;
    }

    /**
     * Converts a list of entities into a list of their corresponding IDs.
     * <p>
     * This method retrieves the identifier of each entity using the {@link EntityManager}.
     * </p>
     *
     * @param entities the list of entities to convert.
     * @param <T>      the type of the entities, which must extend {@link BaseEntity}.
     * @return a list of entity IDs, or {@code null} if the input list is {@code null}.
     */
    protected <T extends BaseEntity> List<Long> toEntityIdList(List<T> entities) {
        return entities == null ? null : entities.stream()
                .map(entity -> (Long) entityManager.getEntityManagerFactory()
                        .getPersistenceUnitUtil()
                        .getIdentifier(entity))
                .toList();
    }

    /**
     * Converts a {@link JsonNullable} set of entity IDs into a list of entity instances.
     * <p>
     * If the {@code idsSet} is null or not present, the method returns {@code null}.
     * Otherwise, it retrieves each entity corresponding to the given IDs.
     * </p>
     *
     * @param idsSet      the {@link JsonNullable} set of entity IDs to convert.
     * @param entityClass the class type of the entities to return.
     * @param <T>         the type of the entity, which must extend {@link BaseEntity}.
     * @return a list of entities corresponding to the given IDs,
     * or {@code null} if the input is null or empty.
     */
    protected <T extends BaseEntity> List<T> mapJsonNullableSetToEntityList(JsonNullable<Set<Long>> idsSet,
                                                                            @TargetType Class<T> entityClass) {
        return (idsSet == null || !idsSet.isPresent()) ? null : idsSet.get().stream()
                .map(id -> toEntity(id, entityClass))
                .filter(Objects::nonNull)
                .toList();
    }
}

