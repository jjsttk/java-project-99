package hexlet.code.app.utils;

public class ExceptionMessage {
    public static String entityNotFoundMessage(Class<?> entityClass, Long id) {
        return "%s with id %d not found".formatted(entityClass.getSimpleName(), id);
    }

    public static String entityNotFoundMessage(Class<?> entityClass, String slug) {
        return "%s with slug %s not found".formatted(entityClass.getSimpleName(), slug);
    }

}
