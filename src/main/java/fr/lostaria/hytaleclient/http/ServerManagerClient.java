package fr.lostaria.hytaleclient.http;

import fr.lostaria.hytaleclient.model.ServerStatus;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static fr.lostaria.hytaleclient.utils.HttpUtils.stripTrailingSlash;

public class ServerManagerClient {

    public record Response(int statusCode, String body) {
        public boolean ok() {
            return statusCode >= 200 && statusCode < 300;
        }
    }

    private final HttpClient client;
    private final String baseUrl;

    public ServerManagerClient(String serverManagerUrl) {
        this.baseUrl = stripTrailingSlash(serverManagerUrl);
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public Response updateStatus(String serverId, ServerStatus status, String jwt) throws Exception {
        URI uri = URI.create(baseUrl + "/servers/" + URLEncoder.encode(serverId, StandardCharsets.UTF_8) + "/status");
        String json = "{\"status\":\"" + status.name() + "\"}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + jwt)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        return new Response(res.statusCode(), res.body());
    }
}
