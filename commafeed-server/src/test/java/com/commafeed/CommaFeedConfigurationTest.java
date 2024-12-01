package com.commafeed;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CommaFeedConfigurationTest {

	@Test
	void verifyMarkdownDocIsUpToDate() throws IOException {
		String versionedDocumentationFile = FileUtils.readFileToString(new File("doc/commafeed.md"), StandardCharsets.UTF_8);
		String generatedDocumentationFile = FileUtils.readFileToString(new File("target/quarkus-generated-doc/config/commafeed-server.md"),
				StandardCharsets.UTF_8);

		Assertions.assertLinesMatch(versionedDocumentationFile.lines(), generatedDocumentationFile.lines());
	}

}