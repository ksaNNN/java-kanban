import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;

    public EpicsHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String[] parts = exchange.getRequestURI().getPath().split("/");

            switch (method) {
                case "GET":
                    handleGet(exchange, parts);
                    break;
                case "POST":
                    handlePost(exchange, parts);
                    break;
                case "DELETE":
                    handleDelete(exchange, parts);
                    break;
                default:
                    sendNotFound(exchange, "Некорректный метод запроса");
            }
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (NumberFormatException e) {
            sendNotFound(exchange, "Некорректный идентификатор эпика");
        } catch (Exception e) {
            sendServerError(exchange, "Произошла ошибка при обработке запроса: " + e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange, String[] parts) throws IOException {
        if (parts.length == 2) {
            sendText(exchange, gson.toJson(manager.getAllEpics()), STATUS_OK);
            return;
        }
        if (parts.length == 3) {
            int id = Integer.parseInt(parts[2]);
            Epic epic = manager.getEpicById(id);
            sendText(exchange, gson.toJson(epic), STATUS_OK);
            return;
        }
        if (parts.length == 4 && parts[3].equals("subtasks")) {
            int id = Integer.parseInt(parts[2]);
            manager.getEpicById(id);
            sendText(exchange, gson.toJson(manager.getSubtasksByEpic(id)), STATUS_OK);
            return;
        }
        sendNotFound(exchange, "Некорректный путь запроса");
    }

    private void handlePost(HttpExchange exchange, String[] parts) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Epic parsedEpic = gson.fromJson(body, Epic.class);

        if (parsedEpic.getId() == 0) {
            Epic newEpic = new Epic(parsedEpic.getName(), parsedEpic.getDescription());
            manager.createEpic(newEpic);
        } else {
            manager.updateEpic(parsedEpic);
        }
        sendCreated(exchange);
    }

    private void handleDelete(HttpExchange exchange, String[] parts) throws IOException {
        if (parts.length == 3) {
            int id = Integer.parseInt(parts[2]);
            manager.deleteEpic(id);
        } else {
            manager.deleteAllEpics();
        }
        sendText(exchange, "", STATUS_OK);
    }
}