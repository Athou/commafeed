package com.commafeed.backend.service.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.manticore.h2.H2MigrationTool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class H2MigrationService {

	public void migrateIfNeeded(Path path, String user, String password) {
		if (Files.notExists(path)) {
			return;
		}

		int format;
		try {
			format = getH2FileFormat(path);
		} catch (IOException e) {
			throw new RuntimeException("could not detect H2 format", e);
		}

		if (format == 2) {
			try {
				migrate(path, user, password, "2.1.214", "2.2.224");
			} catch (Exception e) {
				throw new RuntimeException("could not migrate H2 to format 3", e);
			}
		}
	}

	public int getH2FileFormat(Path path) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(path)) {
			String headers = reader.readLine();

			return Stream.of(headers.split(","))
					.filter(h -> h.startsWith("format:"))
					.map(h -> h.split(":")[1])
					.map(Integer::parseInt)
					.findFirst()
					.orElseThrow(() -> new RuntimeException("could not find format in H2 file headers"));
		}
	}

	private void migrate(Path path, String user, String password, String fromVersion, String toVersion) throws Exception {
		log.info("migrating H2 database at {} from format {} to format {}", path, fromVersion, toVersion);

		Path scriptPath = path.resolveSibling("script-" + System.currentTimeMillis() + ".sql");
		Path newVersionPath = path.resolveSibling(path.getFileName() + "." + getPatchVersion(toVersion) + ".mv.db");
		Path oldVersionBackupPath = path.resolveSibling(path.getFileName() + "." + getPatchVersion(fromVersion) + ".backup");

		Files.deleteIfExists(scriptPath);
		Files.deleteIfExists(newVersionPath);
		Files.deleteIfExists(oldVersionBackupPath);

		H2MigrationTool.readDriverRecords();
		new H2MigrationTool().migrate(fromVersion, toVersion, path.toAbsolutePath().toString(), user, password,
				scriptPath.toAbsolutePath().toString(), "", "", false, false, "");
		if (!Files.exists(newVersionPath)) {
			throw new RuntimeException("H2 migration failed, new version file not found");
		}

		Files.move(path, oldVersionBackupPath);
		Files.move(newVersionPath, path);
		Files.delete(oldVersionBackupPath);
		Files.delete(scriptPath);

		log.info("migrated H2 database from format {} to format {}", fromVersion, toVersion);
	}

	private String getPatchVersion(String version) {
		return StringUtils.substringAfterLast(version, ".");
	}

}
