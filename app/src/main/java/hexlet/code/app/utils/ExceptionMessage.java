package hexlet.code.app.utils;

public class ExceptionMessage {

    public static String userNotFoundMessage(Long id) {
        return "User with id %d not found".formatted(id);
    }

    public static String taskStatusNotFoundMessage(Long id) {
        return "Task status with id %d not found".formatted(id);
    }
}
