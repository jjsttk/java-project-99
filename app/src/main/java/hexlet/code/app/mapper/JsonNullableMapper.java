package hexlet.code.app.mapper;

import org.mapstruct.Condition;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.openapitools.jackson.nullable.JsonNullable;

/**
 * A mapper utility for handling {@link JsonNullable} values.
 * <p>
 * This class provides methods to wrap and unwrap nullable values,
 * as well as a condition checker for presence.
 * </p>
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class JsonNullableMapper {

    /**
     * Wraps a given value in a {@link JsonNullable} container.
     * <p>
     * This method is used to wrap a non-null value into a nullable container.
     * </p>
     *
     * @param entity the value to be wrapped.
     * @param <T>    the type of the value.
     * @return a {@link JsonNullable} containing the given value.
     */
    public <T> JsonNullable<T> wrap(T entity) {
        return JsonNullable.of(entity);
    }

    /**
     * Unwraps a {@link JsonNullable} value, returning {@code null} if the container is empty.
     * <p>
     * This method is used to retrieve the value from a nullable container.
     * If the value is not present, it returns {@code null}.
     * </p>
     *
     * @param jsonNullable the nullable container.
     * @param <T>          the type of the value.
     * @return the unwrapped value, or {@code null} if not present.
     */
    public <T> T unwrap(JsonNullable<T> jsonNullable) {
        return jsonNullable == null ? null : jsonNullable.orElse(null);
    }

    /**
     * Checks if a {@link JsonNullable} value is present.
     * <p>
     * This method is used to check whether the nullable container contains a value.
     * </p>
     *
     * @param nullable the nullable container.
     * @param <T>      the type of the value.
     * @return {@code true} if the value is present, {@code false} otherwise.
     */
    @Condition
    public <T> boolean isPresent(JsonNullable<T> nullable) {
        return nullable != null && nullable.isPresent();
    }
}
