package com.commafeed.backend.service.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class H2MigrationServiceTest {

	@TempDir
	private Path root;

	@Test
	void testMigrateIfNeeded() throws IOException {
		Path path = root.resolve("database.mv.db");
		Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/h2-migration/database-v2.1.214.mv.db")), path);

		H2MigrationService service = new H2MigrationService();
		Assertions.assertEquals(2, service.getH2FileFormat(path));

		service.migrateIfNeeded(path, "sa", "sa");
		Assertions.assertEquals(3, service.getH2FileFormat(path));
	}

}