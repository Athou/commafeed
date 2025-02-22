package com.commafeed.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
		new CommaFeedPropertiesGenerator().generate(args);
	}

	private void generate(String[] args) throws IOException {
		Path targetPath = Paths.get(args[0]);

		ResolvedModel resolvedModel = JacksonMappers.yamlObjectReader()
				.readValue(targetPath.resolve(Outputs.QUARKUS_CONFIG_DOC_MODEL).toFile(), ResolvedModel.class);
		JavadocElements javadocElements = JacksonMappers.yamlObjectReader()
				.readValue(targetPath.resolve(Outputs.QUARKUS_CONFIG_DOC_JAVADOC).toFile(), JavadocElements.class);

		for (ConfigRoot configRoot : resolvedModel.getConfigRoots()) {
			for (AbstractConfigItem item : configRoot.getItems()) {
				handleAbstractConfigItem(item, javadocElements);
			}
		}

		Files.writeString(targetPath.resolve("quarkus-generated-doc").resolve("application.properties"), String.join("\n", lines));
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
