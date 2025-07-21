package com.commafeed.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;

import com.commafeed.CommaFeedConfiguration;

import io.quarkus.annotation.processor.Outputs;
import io.quarkus.annotation.processor.documentation.config.model.AbstractConfigItem;
import io.quarkus.annotation.processor.documentation.config.model.ConfigProperty;
import io.quarkus.annotation.processor.documentation.config.model.ConfigRoot;
import io.quarkus.annotation.processor.documentation.config.model.ConfigSection;
import io.quarkus.annotation.processor.documentation.config.model.JavadocElements;
import io.quarkus.annotation.processor.documentation.config.model.ResolvedModel;
import io.quarkus.annotation.processor.documentation.config.util.JacksonMappers;

/**
 * This class generates an application.properties file with all the properties from {@link CommaFeedConfiguration}.
 *
 * This is useful for people who want to be able to configure CommaFeed without having to look at the code or the documentation, or for
 * distribution packages that want to provide a default configuration file.
 *
 **/
public class CommaFeedPropertiesGenerator {

	private final List<String> lines = new ArrayList<>();

	public static void main(String[] args) throws Exception {
		Path targetPath = Paths.get(args[0]);

		Path modelPath = targetPath.resolve(Outputs.QUARKUS_CONFIG_DOC_MODEL);
		Path javadocPath = targetPath.resolve(Outputs.QUARKUS_CONFIG_DOC_JAVADOC);
		Path outputPath = targetPath.resolve("quarkus-generated-doc").resolve("application.properties");

		try (InputStream model = Files.newInputStream(modelPath);
				InputStream javadoc = Files.newInputStream(javadocPath);
				OutputStream output = Files.newOutputStream(outputPath)) {
			new CommaFeedPropertiesGenerator().generate(model, javadoc, output);
		}
	}

	void generate(InputStream model, InputStream javadoc, OutputStream output) throws IOException {
		ResolvedModel resolvedModel = JacksonMappers.yamlObjectReader().readValue(model, ResolvedModel.class);
		JavadocElements javadocElements = JacksonMappers.yamlObjectReader().readValue(javadoc, JavadocElements.class);

		for (ConfigRoot configRoot : resolvedModel.getConfigRoots()) {
			for (AbstractConfigItem item : configRoot.getItems()) {
				handleAbstractConfigItem(item, javadocElements);
			}
		}

		IOUtils.write(String.join("\n", lines), output, StandardCharsets.UTF_8);
	}

	private void handleAbstractConfigItem(AbstractConfigItem item, JavadocElements javadocElements) {
		if (item.isSection()) {
			handleSection((ConfigSection) item, javadocElements);
		} else {
			handleProperty((ConfigProperty) item, javadocElements);
		}
	}

	private void handleSection(ConfigSection section, JavadocElements javadocElements) {
		for (AbstractConfigItem item : section.getItems()) {
			handleAbstractConfigItem(item, javadocElements);
		}
	}

	private void handleProperty(ConfigProperty property, JavadocElements javadocElements) {
		String key = property.getPath().property();
		String description = javadocElements.elements()
				.get(property.getSourceType() + "." + property.getSourceElementName())
				.description()
				.replace("\n", "\n# ");
		String defaultValue = Optional.ofNullable(property.getDefaultValue()).orElse("").toLowerCase();

		lines.add("# " + description);
		lines.add(key + "=" + defaultValue);
		lines.add("");
	}
}
