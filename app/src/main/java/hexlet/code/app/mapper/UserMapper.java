package hexlet.code.app.mapper;

import hexlet.code.app.dto.user.UserCreateDTO;
import hexlet.code.app.dto.user.UserDTO;
import hexlet.code.app.dto.user.UserUpdateDTO;
import hexlet.code.app.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * A mapper for converting between
 * {@link User}, {@link UserDTO},
 * {@link UserCreateDTO}, and {@link UserUpdateDTO}.
 * <p>
 * This class provides methods for mapping user-related DTOs
 * to user entities and vice versa.
 * It also handles password encryption when mapping user data.
 * </p>
 */
@Mapper(
        uses = {JsonNullableMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class UserMapper implements BaseMapper<User, UserDTO,
        UserCreateDTO, UserUpdateDTO> {

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Maps a {@link User} entity to a {@link UserDTO}.
     *
     * @param model the {@link User} entity to map.
     * @return the corresponding {@link UserDTO}.
     */
    @Override
    public abstract UserDTO mapToDTO(User model);

    /**
     * Maps a {@link UserCreateDTO} to a {@link User} entity.
     * <p>
     * This method automatically encodes the user's password before mapping.
     * </p>
     *
     * @param createDTO the {@link UserCreateDTO} to map.
     * @return the corresponding {@link User} entity.
     */
    @Override
    @Mapping(target = "password", qualifiedByName = "encodePassword")
    public abstract User mapToEntity(UserCreateDTO createDTO);

    /**
     * Updates an existing {@link User} entity using data from a {@link UserUpdateDTO}.
     * <p>
     * If the password is present in the {@link UserUpdateDTO},
     * it will be encoded before updating the entity.
     * </p>
     *
     * @param updateDTO the {@link UserUpdateDTO} containing the updated data.
     * @param user      the {@link User} entity to update.
     */
    @Override
    @Mapping(target = "password", qualifiedByName = "encodePassword")
    public abstract void update(UserUpdateDTO updateDTO, @MappingTarget User user);

    /**
     * Encodes the password if it is present in the {@link JsonNullable} wrapper.
     * <p>
     * If the password is not present, this method returns {@code null}.
     * </p>
     *
     * @param password the {@link JsonNullable} containing the password to encode.
     * @return the encoded password or {@code null} if the password is not present.
     */
    @Named("encodePassword")
    protected String encodePassword(JsonNullable<String> password) {
        return (password != null && password.isPresent()) ? passwordEncoder.encode(password.get()) : null;
    }

    /**
     * Encodes the given password using {@link PasswordEncoder}.
     * <p>
     * This method is used when a password string is directly available.
     * </p>
     *
     * @param password the raw password string to encode.
     * @return the encoded password.
     */
    @Named("encodePassword")
    protected String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}
