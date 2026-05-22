package com.autoatelier.util;

import com.autoatelier.config.SupabaseConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class HttpUtil {

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 600;

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    public static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private static HttpResponse<String> sendWithRetry(HttpRequest request) throws Exception {
        Exception lastEx = null;
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (java.io.IOException e) {
                lastEx = e;
                if (attempt < MAX_RETRIES - 1) {
                    Thread.sleep(RETRY_DELAY_MS * (attempt + 1));
                }
            }
        }
        throw lastEx;
    }

    public static HttpResponse<String> get(String path, String query, String token) throws Exception {
        String url = SupabaseConfig.REST_URL + path + (query != null ? "?" + query : "");
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(20))
                .GET();
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return sendWithRetry(builder.build());
    }

    public static HttpResponse<String> post(String path, String body, String token) throws Exception {
        String url = SupabaseConfig.REST_URL + path;
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Prefer", "return=representation")
                .timeout(Duration.ofSeconds(20))
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return sendWithRetry(builder.build());
    }

    public static HttpResponse<String> patch(String path, String query, String body, String token) throws Exception {
        String url = SupabaseConfig.REST_URL + path + (query != null ? "?" + query : "");
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Prefer", "return=representation")
                .timeout(Duration.ofSeconds(20))
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body));
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return sendWithRetry(builder.build());
    }

    public static HttpResponse<String> delete(String path, String query, String token) throws Exception {
        String url = SupabaseConfig.REST_URL + path + (query != null ? "?" + query : "");
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(20))
                .DELETE();
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return sendWithRetry(builder.build());
    }

    public static HttpResponse<String> authPost(String endpoint, String body) throws Exception {
        String url = SupabaseConfig.AUTH_URL + endpoint;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(20))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return sendWithRetry(request);
    }

    public static HttpResponse<String> authPatch(String endpoint, String body, String token) throws Exception {
        String url = SupabaseConfig.AUTH_URL + endpoint;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(20))
                .method("PUT", HttpRequest.BodyPublishers.ofString(body))
                .build();
        return sendWithRetry(request);
    }

    public static HttpResponse<String> uploadFile(String bucket, String filePath,
                                                   byte[] data, String mimeType,
                                                   String token) throws Exception {
        String url = SupabaseConfig.STORAGE_URL + "/object/" + bucket + "/" + filePath;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", mimeType)
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofByteArray(data))
                .build();
        return sendWithRetry(request);
    }

    public static String getPublicUrl(String bucket, String filePath) {
        return SupabaseConfig.STORAGE_URL + "/object/public/" + bucket + "/" + filePath;
    }

    public static String toJson(Map<String, Object> map) throws Exception {
        return MAPPER.writeValueAsString(map);
    }

    public static boolean isSuccess(int status) {
        return status >= 200 && status < 300;
    }
}
