package com.gsir.tools.network;

import org.springframework.http.HttpStatus;
import com.gsir.common.error.GsirException;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class SecureHttpClient {

    private final HttpClient httpClient;
    private static final int MAX_RESPONSE_SIZE = 1024 * 1024 * 5; // 5 MB
    private static final int MAX_REDIRECTS = 3;
    private final CircuitBreaker circuitBreaker;

    public SecureHttpClient() {
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.circuitBreaker = new CircuitBreaker(5, Duration.ofSeconds(30)); // 5 failures, 30s open
    }

    public String get(String url) {
        return circuitBreaker.execute(() -> executeGetWithRetries(url));
    }
    
    private String executeGetWithRetries(String url) {
        int maxRetries = 3;
        int attempt = 0;
        while (true) {
            try {
                return executeGet(url);
            } catch (Exception e) {
                attempt++;
                if (attempt >= maxRetries || !isTransientError(e)) {
                    if (e instanceof RuntimeException) throw (RuntimeException) e;
                    throw new RuntimeException("HTTP request failed: " + e.getMessage(), e);
                }
                try {
                    Thread.sleep(1000L * attempt); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry backoff", ie);
                }
            }
        }
    }
    
    private boolean isTransientError(Exception e) {
        if (e instanceof java.net.http.HttpTimeoutException) return true;
        if (e instanceof java.io.IOException) return true;
        return false;
    }

    private String executeGet(String url) {
        try {
            URI currentUri = URI.create(url);
            int redirectCount = 0;

            while (true) {
                validateTarget(currentUri);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(currentUri)
                        .timeout(Duration.ofSeconds(15))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                int statusCode = response.statusCode();
                if (statusCode == 200) {
                    if (response.body().length() > MAX_RESPONSE_SIZE) {
                        throw new GsirException("SECURITY_VIOLATION", "Response size exceeds maximum allowed limit", HttpStatus.FORBIDDEN);
                    }
                    return response.body();
                } else if (statusCode >= 300 && statusCode < 400) {
                    redirectCount++;
                    if (redirectCount > MAX_REDIRECTS) {
                        throw new GsirException("SECURITY_VIOLATION", "Too many redirects", HttpStatus.FORBIDDEN);
                    }
                    String location = response.headers().firstValue("Location")
                            .orElseThrow(() -> new GsirException("SECURITY_VIOLATION", "Redirect location missing", HttpStatus.FORBIDDEN));
                    currentUri = currentUri.resolve(location);
                } else if (statusCode == 429 || statusCode >= 500) {
                    throw new java.io.IOException("Transient server error: " + statusCode);
                } else {
                    throw new RuntimeException("Unexpected status code: " + statusCode);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Request interrupted", e);
        } catch (Exception e) {
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new GsirException("SECURITY_VIOLATION", "Failed to execute secure request", HttpStatus.FORBIDDEN);
        }
    }

    private void validateTarget(URI uri) {
        String scheme = uri.getScheme();
        if (!"https".equalsIgnoreCase(scheme)) {
            throw new GsirException("SECURITY_VIOLATION", "Only HTTPS is allowed. Blocked scheme: " + scheme, HttpStatus.FORBIDDEN);
        }
        String host = uri.getHost();
        if (host == null || isInternal(host)) {
            throw new GsirException("SECURITY_VIOLATION", "local/private network is forbidden: " + host, HttpStatus.FORBIDDEN);
        }
    }

    private boolean isInternal(String host) {
        return host.equals("localhost") || host.equals("127.0.0.1") ||
               host.equals("::1") || host.startsWith("10.") ||
               host.startsWith("192.168.") || host.matches("^172\\.(1[6-9]|2[0-9]|3[0-1])\\..*") ||
               host.equals("169.254.169.254") || host.equals("0.0.0.0") ||
               host.endsWith(".internal") || host.endsWith(".local");
    }
}
