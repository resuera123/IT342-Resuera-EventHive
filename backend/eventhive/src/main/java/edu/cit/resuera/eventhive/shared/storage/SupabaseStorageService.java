package edu.cit.resuera.eventhive.shared.storage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Handles file uploads and deletes against Supabase Storage via the REST API.
 *
 * Uses Java's built-in HttpClient (no external dependencies). The service-role
 * key bypasses Row Level Security, so this MUST run server-side only and the
 * key MUST be kept out of source control / git / chat.
 *
 * Public bucket reads work without authentication — once a file is uploaded,
 * any client can fetch it via the returned public URL.
 */
@Service
public class SupabaseStorageService {

    private static final String UPLOAD_PATH_TEMPLATE  = "%s/storage/v1/object/%s/%s";
    private static final String PUBLIC_URL_TEMPLATE   = "%s/storage/v1/object/public/%s/%s";

    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.service-key:}")
    private String supabaseServiceKey;

    @Value("${supabase.bucket.events:event-images}")
    private String eventsBucket;

    @Value("${supabase.bucket.profiles:profile-pics}")
    private String profilesBucket;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    // ─────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────

    /** Upload an event image; returns the public URL of the uploaded file. */
    public String uploadEventImage(MultipartFile file) throws IOException {
        return upload(eventsBucket, file, "events");
    }

    /** Upload a profile picture; returns the public URL of the uploaded file. */
    public String uploadProfilePic(MultipartFile file, Long userId) throws IOException {
        // Profile pics scoped under userId so deletes/updates are deterministic
        String filename = userId + "_" + System.currentTimeMillis() + "_" + sanitize(file.getOriginalFilename());
        return uploadWithFilename(profilesBucket, file, filename);
    }

    /** Delete a previously uploaded file given its public URL. Best-effort. */
    public void deleteByPublicUrl(String publicUrl) {
        if (publicUrl == null || publicUrl.isBlank()) return;
        if (!publicUrl.startsWith(supabaseUrl)) return;  // Not ours

        try {
            // Public URL pattern: {supabaseUrl}/storage/v1/object/public/{bucket}/{path}
            String marker = "/storage/v1/object/public/";
            int idx = publicUrl.indexOf(marker);
            if (idx < 0) return;
            String bucketAndPath = publicUrl.substring(idx + marker.length());
            int slash = bucketAndPath.indexOf('/');
            if (slash < 0) return;
            String bucket = bucketAndPath.substring(0, slash);
            String path = bucketAndPath.substring(slash + 1);

            URI deleteUri = URI.create(String.format(UPLOAD_PATH_TEMPLATE, supabaseUrl, bucket, path));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(deleteUri)
                    .header("Authorization", "Bearer " + supabaseServiceKey)
                    .DELETE()
                    .build();
            httpClient.send(request, BodyHandlers.discarding());
        } catch (Exception ignored) {
            // Best-effort delete; do not fail the user-facing operation
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Internals
    // ─────────────────────────────────────────────────────────────────

    private String upload(String bucket, MultipartFile file, String pathPrefix) throws IOException {
        String safeOriginal = sanitize(file.getOriginalFilename());
        String filename = pathPrefix + "/" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "_" + safeOriginal;
        return uploadWithFilename(bucket, file, filename);
    }

    private String uploadWithFilename(String bucket, MultipartFile file, String objectPath) throws IOException {
        if (supabaseUrl.isBlank() || supabaseServiceKey.isBlank()) {
            throw new IOException("Supabase storage is not configured (missing supabase.url or supabase.service-key)");
        }

        URI uploadUri = URI.create(String.format(UPLOAD_PATH_TEMPLATE, supabaseUrl, bucket, objectPath));

        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uploadUri)
                .header("Authorization", "Bearer " + supabaseServiceKey)
                .header("Content-Type", contentType)
                .header("x-upsert", "true")  // overwrite if same filename
                .POST(BodyPublishers.ofByteArray(file.getBytes()))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("Supabase upload failed: HTTP " + response.statusCode() + " — " + response.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Supabase upload interrupted", e);
        }

        return String.format(PUBLIC_URL_TEMPLATE, supabaseUrl, bucket, objectPath);
    }

    /**
     * Strip path separators and weird characters from filenames before
     * sending to Supabase. Supabase rejects whitespace, slashes, and quotes
     * in object paths.
     */
    private String sanitize(String filename) {
        if (filename == null || filename.isBlank()) return "file";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}