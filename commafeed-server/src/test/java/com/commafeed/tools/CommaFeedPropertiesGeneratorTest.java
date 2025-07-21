package com.commafeed.tools;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.io.Resources;

class CommaFeedPropertiesGeneratorTest {

	@Test
	void testGenerate() throws Exception {
		InputStream model = getClass().getResourceAsStream("/properties/quarkus-config-model.yaml");
		InputStream javadoc = getClass().getResourceAsStream("/properties/quarkus-config-javadoc.yaml");
		URL output = getClass().getResource("/properties/output.properties");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		new CommaFeedPropertiesGenerator().generate(model, javadoc, baos);

		Assertions.assertLinesMatch(Resources.readLines(output, StandardCharsets.UTF_8).stream(),
				baos.toString(StandardCharsets.UTF_8).lines());
	}

}