package hexlet.code.app.mapper;

import hexlet.code.app.dto.user.UserCreateDTO;
import hexlet.code.app.dto.user.UserDTO;
import hexlet.code.app.dto.user.UserUpdateDTO;
import hexlet.code.app.model.User;
import org.mapstruct.BeforeMapping;
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
 * A mapper for converting between {@link User}, {@link UserDTO}, {@link UserCreateDTO}, and {@link UserUpdateDTO}.
 * <p>
 * This class provides methods for mapping user-related DTOs
 * to user entities and vice versa.
 * It also handles password encryption.
 */
@Mapper(
        uses = {JsonNullableMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class UserMapper {

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Maps a {@link User} entity to a {@link UserDTO}.
     *
     * @param model the {@link User} entity to map.
     * @return the corresponding {@link UserDTO}.
     */
    public abstract UserDTO map(User model);

    /**
     * Maps a {@link UserCreateDTO} to a {@link User} entity.
     *
     * @param createDTO the {@link UserCreateDTO} to map.
     * @return the corresponding {@link User} entity.
     */
    public abstract User map(UserCreateDTO createDTO);

    /**
     * Updates an existing {@link User} entity using data from a {@link UserUpdateDTO}.
     * If the password is present in the {@link UserUpdateDTO}, it will be encoded.
     *
     * @param updateDTO the {@link UserUpdateDTO} containing the updated data.
     * @param user      the {@link User} entity to update.
     */
    @Mapping(target = "password", qualifiedByName = "encodePasswordIfPresent")
    public abstract void update(UserUpdateDTO updateDTO, @MappingTarget User user);

    /**
     * Encrypts the password from the {@link UserCreateDTO} before mapping it to a {@link User} entity.
     *
     * @param data the {@link UserCreateDTO} containing the password to encrypt.
     */
    @BeforeMapping
    public void encryptPassword(UserCreateDTO data) {
        var password = data.getPassword();
        data.setPassword(passwordEncoder.encode(password));
    }

    /**
     * Encodes the password if it is present in the {@link JsonNullable} wrapper.
     * If the password is not present, it returns {@code null}.
     *
     * @param password the {@link JsonNullable} containing the password to encode.
     * @return the encoded password or {@code null} if the password is not present.
     */
    @Named("encodePasswordIfPresent")
    String encodePasswordIfPresent(JsonNullable<String> password) {
        return (password != null && password.isPresent()) ? passwordEncoder.encode(password.get()) : null;
    }

}
