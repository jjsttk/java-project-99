package hexlet.code.app.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.format.DateTimeFormatter;

/**
 * Configuration class for customizing Jackson's JSON serialization and deserialization.
 * This configuration ensures that null values are excluded from serialization,
 * supports handling of {@link org.openapitools.jackson.nullable.JsonNullable},
 * and defines a default date format for {@link java.time.LocalDate}.
 */
@Configuration
public class JacksonConfig {

    /**
     * Configures and provides a {@link Jackson2ObjectMapperBuilder} bean.
     * <p>
     * This builder:
     * <ul>
     *     <li>Excludes {@code null} values from JSON output.</li>
     *     <li>Registers support for {@link org.openapitools.jackson.nullable.JsonNullable}.</li>
     *     <li>Sets the default date format for {@link java.time.LocalDate} to {@code yyyy-MM-dd}.</li>
     * </ul>
     *
     * @return A configured instance of {@link Jackson2ObjectMapperBuilder}.
     */
    @Bean
    Jackson2ObjectMapperBuilder objectMapperBuilder() {
        var builder = new Jackson2ObjectMapperBuilder();
        builder.serializationInclusion(JsonInclude.Include.NON_NULL)
                .modulesToInstall(new JsonNullableModule());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        builder.serializers(new LocalDateSerializer(formatter));

        return builder;
    }
}
