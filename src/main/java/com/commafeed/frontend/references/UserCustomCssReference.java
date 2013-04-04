package com.commafeed.frontend.references;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

@SuppressWarnings("serial")
public abstract class UserCustomCssReference extends ResourceReference {

	public UserCustomCssReference() {
		super(UserCustomCssReference.class, "custom.css");
	}

	@Override
	public IResource getResource() {
		return new AbstractResource() {
			@Override
			protected ResourceResponse newResourceResponse(Attributes attributes) {
				ResourceResponse resourceResponse = new ResourceResponse();
				resourceResponse.setContentType("text/css");
				resourceResponse.setTextEncoding("UTF-8");
				resourceResponse.setWriteCallback(new WriteCallback() {
					@Override
					public void writeData(Attributes attributes)
							throws IOException {
						attributes.getResponse().write(
								StringUtils.trimToEmpty(getCss()));
					}
				});
				return resourceResponse;
			}
		};
	}

	protected abstract String getCss();
}
