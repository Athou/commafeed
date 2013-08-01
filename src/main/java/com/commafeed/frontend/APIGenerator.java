package com.commafeed.frontend;

import java.io.File;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.commafeed.frontend.model.Entries;
import com.commafeed.frontend.model.request.MarkRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.core.Documentation;
import com.wordnik.swagger.core.DocumentationEndPoint;
import com.wordnik.swagger.core.SwaggerSpec;
import com.wordnik.swagger.core.util.TypeUtil;
import com.wordnik.swagger.jaxrs.HelpApi;
import com.wordnik.swagger.jaxrs.JaxrsApiReader;

@SupportedAnnotationTypes("com.wordnik.swagger.annotations.Api")
@SupportedOptions("outputDirectory")
public class APIGenerator extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		try {
			return processInternal(annotations, roundEnv);
		} catch (Exception e) {
			e.printStackTrace();
			processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage());
		}
		return false;
	}

	private boolean processInternal(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws Exception {
		JaxrsApiReader.setFormatString("");
		TypeUtil.addAllowablePackage(Entries.class.getPackage().getName());
		TypeUtil.addAllowablePackage(MarkRequest.class.getPackage().getName());

		String apiVersion = "1.0";
		String swaggerVersion = SwaggerSpec.version();
		String basePath = "../rest";

		Documentation doc = new Documentation();
		for (Element element : roundEnv.getElementsAnnotatedWith(Api.class)) {
			TypeElement type = (TypeElement) element;
			String fqn = type.getQualifiedName().toString();
			Class<?> resource = Class.forName(fqn);

			Api api = resource.getAnnotation(Api.class);
			String apiPath = api.value();

			Documentation apiDoc = JaxrsApiReader.read(resource, apiVersion, swaggerVersion, basePath, apiPath);
			apiDoc = new HelpApi(null).filterDocs(apiDoc, null, null, null, null);

			apiDoc.setSwaggerVersion(swaggerVersion);
			apiDoc.setApiVersion(apiVersion);
			write(apiDoc.getResourcePath(), apiDoc, element);

			doc.addApi(new DocumentationEndPoint(api.value(), api.description()));

		}
		doc.setSwaggerVersion(swaggerVersion);
		doc.setApiVersion(apiVersion);

		write(doc.getResourcePath(), doc, null);

		return true;
	}

	private void write(String resourcePath, Object doc, Element element) throws Exception {
		String fileName = StringUtils.defaultString(resourcePath, "resources");
		fileName = StringUtils.removeStart(fileName, "/");

		FileObject resource = null;
		try {
			resource = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "", fileName, element);
		} catch (Exception e) {
			// already processed
		}
		if (resource != null) {
			FileUtils.writeStringToFile(new File(resource.toUri()), new ObjectMapper().writeValueAsString(doc));
		}

	}
}
