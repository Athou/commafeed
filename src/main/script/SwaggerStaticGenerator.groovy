import java.io.File;

import org.apache.commons.io.FileUtils;

import com.commafeed.frontend.model.Entries;
import com.commafeed.frontend.model.request.MarkRequest;
import com.commafeed.frontend.rest.RESTApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.core.Documentation;
import com.wordnik.swagger.core.DocumentationEndPoint;
import com.wordnik.swagger.core.SwaggerSpec;
import com.wordnik.swagger.core.util.TypeUtil;
import com.wordnik.swagger.jaxrs.HelpApi;
import com.wordnik.swagger.jaxrs.JaxrsApiReader;

public class SwaggerStaticGenerator {
	public void generate(String directory) throws Exception {

		JaxrsApiReader.setFormatString("");
		TypeUtil.addAllowablePackage(Entries.class.getPackage().getName());
		TypeUtil.addAllowablePackage(MarkRequest.class.getPackage().getName());

		RESTApplication app = new RESTApplication();

		String apiVersion = "1.0";
		String swaggerVersion = SwaggerSpec.version();
		String basePath = "../rest";

		Documentation doc = new Documentation();
		for (Class<?> resource : app.getClasses()) {
			Api api = resource.getAnnotation(Api.class);
			if (api != null) {
				String apiPath = api.value();
				String apiListingPath = api.value();

				Documentation apiDoc = new HelpApi(null).filterDocs(
						JaxrsApiReader.read(resource, apiVersion, swaggerVersion, basePath, apiPath), null, null, apiListingPath, apiPath);

				apiDoc.setSwaggerVersion(swaggerVersion);
				apiDoc.setApiVersion(apiVersion);
				write(apiDoc, directory);

				doc.addApi(new DocumentationEndPoint(api.value(), api.description()));
			}
		}
		doc.setSwaggerVersion(swaggerVersion);
		doc.setApiVersion(apiVersion);

		write(doc, directory);
	}

	private static void write(Documentation doc, String directory) throws Exception {
		FileUtils.writeStringToFile(new File(directory, doc.getResourcePath() == null ? "resources" : doc.getResourcePath()), new ObjectMapper().writeValueAsString(doc));

	}
}
