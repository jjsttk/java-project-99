package hexlet.code.app.util.exception;

public class ExceptionMessage {

    public static String userNotFoundMessage(Long id) {
        return "User with id %d not found".formatted(id);
    }
}
