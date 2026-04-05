package io.MHWilds.mhwbuilder.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class H2SeedInitializer {

    private static final String SEED_DB_CLASSPATH = "/db/seed/mhwbuilder.mv.db";
    private static final Path RUNTIME_DIR = Path.of("./runtime-data");
    private static final Path RUNTIME_DB_FILE = RUNTIME_DIR.resolve("mhwbuilder.mv.db");

    private H2SeedInitializer() {
    }

    public static void initialize() {
        try {
            createRuntimeDirectoryIfNeeded();
            copySeedDbIfMissing();
        } catch (IOException e) {
            throw new IllegalStateException("초기 H2 DB 파일 준비 중 오류가 발생했습니다.", e);
        }
    }

    private static void createRuntimeDirectoryIfNeeded() throws IOException {
        if (Files.notExists(RUNTIME_DIR)) {
            Files.createDirectories(RUNTIME_DIR);
        }
    }

    private static void copySeedDbIfMissing() throws IOException {
        if (Files.exists(RUNTIME_DB_FILE)) {
            return;
        }

        try (InputStream inputStream = H2SeedInitializer.class.getResourceAsStream(SEED_DB_CLASSPATH)) {
            if (inputStream == null) {
                throw new IllegalStateException("seed DB 파일을 찾을 수 없습니다: " + SEED_DB_CLASSPATH);
            }

            Files.copy(inputStream, RUNTIME_DB_FILE, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}