package org.example.service;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import lombok.extern.log4j.Log4j2;
import org.example.model.ApiResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Log4j2
public class ApiServiceImpl implements ApiService {

    private final HttpClient client = HttpClient.newHttpClient();

    public ApiResponse getData(String url) {
        return getData(url, Map.of(), LocalDateTime.now().plusDays(30));
    }

    public ApiResponse getData(String url, Map<String, String> headers) {
        return getData(url, headers, LocalDateTime.now().plusDays(30));
    }

    @Override
    public ApiResponse getData(String url, Map<String, String> headers, LocalDateTime evictionTime) {

        RetryPolicy<ApiResponse> retryPolicy = RetryPolicy.<ApiResponse>builder()
                .handle(IOException.class)
                .withDelay(Duration.ofSeconds(2))
                .withMaxRetries(3)
                .withBackoff(Duration.ofSeconds(1), Duration.ofSeconds(4))
                .onRetry(e -> System.out.println(
                        "Retry attempt #" + e.getAttemptCount() + " due to: " + e.getLastException().getMessage()))
                .build();

        try {
            return Failsafe.with(retryPolicy).get(() -> {
                if (isValidUrl(url)) {
                    System.out.println("[ERROR] Invalid URL: " + url);
                    return null;
                }

                HttpRequest.Builder builder = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET();

                // Check if User-Agent exists (case-insensitive)
                boolean hasUserAgent = headers != null &&
                        headers.keySet().stream().anyMatch(h -> h.equalsIgnoreCase("User-Agent"));

                // Add user-provided headers
                if (headers != null && !headers.isEmpty()) {
                    headers.forEach(builder::header);
                }

                // Add User-Agent only if missing
                if (!hasUserAgent) {
                    builder.header("User-Agent", "Java-HttpClient");
                }

                HttpRequest request = builder.build();

                // Send request as bytes
                HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

                int status = response.statusCode();

                // Detect Content-Type
                String contentType = response.headers().firstValue("Content-Type")
                        .orElse("application/octet-stream");

                byte[] body = response.body();

                // Retry if body empty
                if (body == null || body.length == 0) {
                    throw new IOException("Empty body");
                }

                log.info("Fetched {} ({} bytes) [{}]", url, body.length, contentType);

                // Convert body → ApiResponse (text/image/binary)
                return convertToApiResponse(contentType, body);
            });

        } catch (IllegalArgumentException e) {
            // Happens if the URL is invalid (malformed)
            log.error("Invalid URL: {}", url, e);
            return null;

        } catch (Exception e) {
            // Generic fallback
            log.error("An unexpected error occurred while making the request to: {}", url, e);
            return null;
        }
    }

    private ApiResponse convertToApiResponse(String contentType, byte[] body) {
        try {
            if (contentType.contains("application/json") ||
                    contentType.contains("text/") ||
                    contentType.contains("xml") ||
                    contentType.contains("html")) {

                return ApiResponse.text(new String(body));
            }

            if (contentType.startsWith("image/")) {
                var img = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(body));
                return ApiResponse.image(img);
            }

            // Unknown → treat as binary (PDF, ZIP, anything)
            return ApiResponse.binary(body);

        } catch (Exception e) {
            log.error("Failed to convert response: {}", e.getMessage());
            return ApiResponse.binary(body);
        }
    }

    public static boolean isValidUrl(String url) {
        try {
            URI uri = new URI(url);
            return uri.getScheme() == null
                    || (!uri.getScheme().equalsIgnoreCase("http") && !uri.getScheme().equalsIgnoreCase("https"));
        } catch (URISyntaxException e) {
            return true;
        }
    }

}
