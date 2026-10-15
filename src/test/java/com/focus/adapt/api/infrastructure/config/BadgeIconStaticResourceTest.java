package com.focus.adapt.api.infrastructure.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.focus.adapt.api.AbstractIntegrationTest;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class BadgeIconStaticResourceTest extends AbstractIntegrationTest {

    @TempDir
    static Path tempDir;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("app.badge-icons.path", () -> tempDir.toAbsolutePath().toString());
    }

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void setUp() throws IOException {
        Path sampleIcon = tempDir.resolve("study-task-10.png");
        byte[] pngHeader = new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
        Files.write(sampleIcon, pngHeader);
    }

    @Test
    void shouldServeBadgeIconPubliclyWithCacheControl() throws Exception {
        mockMvc.perform(get("/badge-icons/study-task-10.png"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.IMAGE_PNG_VALUE))
                .andExpect(header().string("Cache-Control", containsString("max-age=604800")))
                .andExpect(header().string("Cache-Control", containsString("public")));
    }

    @Test
    void shouldReturnNotFoundForNonExistentIcon() throws Exception {
        mockMvc.perform(get("/badge-icons/non-existent.png"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldPreventPathTraversal() throws Exception {
        mockMvc.perform(get("/badge-icons/../secret.txt"))
                .andExpect(status().isBadRequest());
    }
}
