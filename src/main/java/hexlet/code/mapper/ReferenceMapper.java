package hexlet.code.mapper;

import hexlet.code.model.BaseEntity;
import jakarta.persistence.EntityManager;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.TargetType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * A utility class for handling reference entity mappings.
 * <p>
 * This class provides methods for converting entity identifiers (IDs)
 * into entity instances using the {@link EntityManager}. It also allows
 * conversion of entity collections into ID lists.
 * </p>
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public abstract class ReferenceMapper {

    @Autowired
    private EntityManager entityManager;

    /**
     * Converts an ID to an entity of the specified type using the {@link EntityManager}.
     * <p>
     * If the given ID is {@code null}, this method returns {@code null}.
     * Otherwise, it retrieves the entity from the database based on the provided ID.
     * </p>
     *
     * @param id          the ID of the entity to retrieve.
     * @param entityClass the class type of the entity to return.
     * @param <T>         the type of the entity, which must extend {@link BaseEntity}.
     * @return the entity corresponding to the given ID, or {@code null} if the ID is {@code null}.
     */
    protected <T extends BaseEntity> T toEntity(Long id, @TargetType Class<T> entityClass) {
        return id == null ? null : entityManager.find(entityClass, id);
    }

    /**
     * Converts a set of entity IDs to a set of entity instances of the specified type.
     * <p>
     * This method retrieves each entity from the database using its ID.
     * If the input set is {@code null}, an empty set is returned.
     * </p>
     *
     * @param identifiers a set of entity IDs to convert.
     * @param entityClass the class type of the entity.
     * @param <T>         the type of the entity, which must extend {@link BaseEntity}.
     * @return a set of entity instances corresponding to the given IDs.
     */
    protected <T extends BaseEntity> Set<T> mapSetLongToSetEntity(Set<Long> identifiers,
                                                                  @TargetType Class<T> entityClass) {
        return (identifiers == null) ? Collections.emptySet() : identifiers.stream()
                .map(id -> toEntity(id, entityClass))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * Retrieves the ID of the given entity using the {@link EntityManager}.
     * <p>
     * If the entity is {@code null}, this method returns {@code null}.
     * </p>
     *
     * @param entity the entity instance whose ID is to be retrieved.
     * @param <T>    the type of the entity, which must extend {@link BaseEntity}.
     * @return the ID of the entity, or {@code null} if the entity is {@code null}.
     */
    protected <T extends BaseEntity> Long getEntityId(T entity) {
        return entity == null ? null : (Long) entityManager.getEntityManagerFactory()
                .getPersistenceUnitUtil().getIdentifier(entity);
    }
}

