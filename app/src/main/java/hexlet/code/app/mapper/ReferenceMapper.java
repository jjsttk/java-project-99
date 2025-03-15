package hexlet.code.app.mapper;

import hexlet.code.app.model.BaseEntity;
import jakarta.persistence.EntityManager;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.TargetType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A utility class for handling reference entity mappings.
 * <p>
 * This class provides methods for converting an ID or a slug to an entity instance using the {@link EntityManager}.
 * It can convert IDs to entities of a given class type and also allows retrieval of an entity using its slug.
 * </p>
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ReferenceMapper {

    @Autowired
    private EntityManager entityManager;

    /**
     * Converts an ID to an entity of the specified type using the {@link EntityManager}.
     *
     * @param id          the ID of the entity to retrieve.
     * @param entityClass the class type of the entity to return.
     * @param <T>         the type of the entity, which must extend {@link BaseEntity}.
     * @return the entity corresponding to the given ID, or {@code null} if the ID is {@code null}.
     */
    public <T extends BaseEntity> T toEntity(Long id, @TargetType Class<T> entityClass) {
        return id != null ? entityManager.find(entityClass, id) : null;
    }

    /**
     * Converts a slug to an entity of the specified type using the {@link EntityManager}.
     *
     * @param slug        the slug of the entity to retrieve.
     * @param entityClass the class type of the entity to return.
     * @param <T>         the type of the entity, which must extend {@link BaseEntity}.
     * @return the entity corresponding to the given slug,
     * or {@code null} if the slug is {@code null} or no entity is found.
     */
    public <T extends BaseEntity> T toEntity(String slug, @TargetType Class<T> entityClass) {
        return slug != null ? entityManager.createQuery(
                        "SELECT e FROM " + entityClass.getSimpleName()
                                + " e WHERE e.slug = :slug", entityClass)
                .setParameter("slug", slug)
                .getResultStream()
                .findFirst()
                .orElse(null) : null;
    }
}
