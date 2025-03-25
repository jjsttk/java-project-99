package hexlet.code.mapper;

import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
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
public abstract class UserMapper implements BaseMapper {

    @Autowired
    private PasswordEncoder passwordEncoder;

    public abstract UserDTO map(User model);

    @Mapping(target = "password", source = "password", qualifiedByName = "encodePassword")
    public abstract User map(UserCreateDTO createDTO);

    @Mapping(target = "password", source = "password", qualifiedByName = "encodePassword")
    public abstract void update(UserUpdateDTO updateDTO, @MappingTarget User user);

    /**
     * Encodes the given plain text password using the configured {@link PasswordEncoder}.
     * <p>
     * This method is used to ensure that passwords are securely stored in an encoded format.
     * </p>
     *
     * @param password the plain text password to encode.
     * @return the encoded password.
     */
    @Named("encodePassword")
    protected String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}
