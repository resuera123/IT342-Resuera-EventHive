package edu.cit.resuera.eventhive.shared.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Note: We avoid hitting the live Supabase API in unit tests. These tests
 * focus on input validation and the safe-no-op behavior of deleteByPublicUrl.
 *
 * The actual upload code path is covered by integration tests against a real
 * Supabase bucket during regression — see Test Plan section 5.
 */
@ExtendWith(MockitoExtension.class)
class SupabaseStorageServiceTest {

    private SupabaseStorageService service;

    @BeforeEach
    void setUp() {
        service = new SupabaseStorageService();
        // Inject test config values via reflection (matches @Value injection)
        ReflectionTestUtils.setField(service, "supabaseUrl", "https://test.supabase.co");
        ReflectionTestUtils.setField(service, "supabaseServiceKey", "fake-key");
        ReflectionTestUtils.setField(service, "eventsBucket", "event-images");
        ReflectionTestUtils.setField(service, "profilesBucket", "profile-pics");
    }

    @Test
    @DisplayName("deleteByPublicUrl ignores null URL silently")
    void deleteByPublicUrl_nullUrl_isSafe() {
        assertThatCode(() -> service.deleteByPublicUrl(null)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("deleteByPublicUrl ignores blank URL silently")
    void deleteByPublicUrl_blankUrl_isSafe() {
        assertThatCode(() -> service.deleteByPublicUrl("")).doesNotThrowAnyException();
        assertThatCode(() -> service.deleteByPublicUrl("   ")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("deleteByPublicUrl ignores legacy /uploads/... paths from before the migration")
    void deleteByPublicUrl_legacyPath_isSafe() {
        // These URLs predate Supabase Storage and should not trigger HTTP calls
        assertThatCode(() ->
                service.deleteByPublicUrl("/uploads/events/123_pic.jpg")
        ).doesNotThrowAnyException();

        assertThatCode(() ->
                service.deleteByPublicUrl("/uploads/profiles/45_avatar.png")
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("deleteByPublicUrl ignores URLs that don't match the configured Supabase host")
    void deleteByPublicUrl_externalUrl_isSafe() {
        // URL points to a different host - should be ignored
        assertThatCode(() ->
                service.deleteByPublicUrl("https://other.supabase.co/storage/v1/object/public/x/y.jpg")
        ).doesNotThrowAnyException();

        assertThatCode(() ->
                service.deleteByPublicUrl("https://random.cdn.example.com/some/image.jpg")
        ).doesNotThrowAnyException();
    }
}