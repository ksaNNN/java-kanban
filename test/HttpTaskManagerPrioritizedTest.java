import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpTaskManagerPrioritizedTest {
    private TaskManager manager;
    private HttpTaskServer taskServer;

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        taskServer.start();
    }

    @AfterEach
    void shutDown() {
        taskServer.stop();
    }

    @Test
    void getPrioritizedReturns200AndOrderedTasks() throws IOException, InterruptedException {
        manager.createTask(new Task("Задача поздняя", "Описание",
                Duration.ofMinutes(30), LocalDateTime.now().plusHours(2)));
        manager.createTask(new Task("Задача ранняя", "Описание",
                Duration.ofMinutes(30), LocalDateTime.now()));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().indexOf("ранняя") < response.body().indexOf("поздняя"));
    }
}