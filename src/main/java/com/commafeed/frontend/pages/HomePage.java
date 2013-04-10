package com.commafeed.frontend.pages;

import javax.inject.Inject;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.commafeed.backend.dao.UserSettingsService;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.model.UserSettings;
import com.commafeed.frontend.CommaFeedSession;
import com.commafeed.frontend.SecurityCheck;
import com.commafeed.frontend.references.UserCustomCssReference;
import com.commafeed.frontend.references.angular.AngularReference;
import com.commafeed.frontend.references.angular.AngularResourceReference;
import com.commafeed.frontend.references.angular.AngularSanitizeReference;
import com.commafeed.frontend.references.angularui.AngularUIReference;
import com.commafeed.frontend.references.angularuibootstrap.AngularUIBootstrapReference;
import com.commafeed.frontend.references.angularuistate.AngularUIStateReference;
import com.commafeed.frontend.references.codemirror.CodeMirrorCssReference;
import com.commafeed.frontend.references.mousetrap.MouseTrapReference;
import com.commafeed.frontend.references.nggrid.NGGridReference;
import com.commafeed.frontend.references.nginfinitescroll.NGInfiniteScrollReference;
import com.commafeed.frontend.references.ngupload.NGUploadReference;
import com.commafeed.frontend.references.select2.Select2Reference;
import com.commafeed.frontend.references.spinjs.SpinJSReference;

import de.agilecoders.wicket.markup.html.bootstrap.extensions.icon.OpenWebIconsCssReference;

@SuppressWarnings("serial")
@SecurityCheck(Role.USER)
public class HomePage extends BasePage {

	@Inject
	UserSettingsService settingsService;

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		AngularReference.renderHead(response);
		AngularResourceReference.renderHead(response);
		AngularSanitizeReference.renderHead(response);
		AngularUIReference.renderHead(response);
		AngularUIBootstrapReference.renderHead(response);
		AngularUIStateReference.renderHead(response);
		NGUploadReference.renderHead(response);
		NGInfiniteScrollReference.renderHead(response);
		Select2Reference.renderHead(response);
		SpinJSReference.renderHead(response);
		MouseTrapReference.renderHead(response);
		NGGridReference.renderHead(response);
		CodeMirrorCssReference.renderHead(response);

		response.render(CssHeaderItem.forReference(OpenWebIconsCssReference
				.instance()));

		response.render(JavaScriptHeaderItem.forUrl("js/main.js"));
		response.render(JavaScriptHeaderItem.forUrl("js/controllers.js"));
		response.render(JavaScriptHeaderItem.forUrl("js/directives.js"));
		response.render(JavaScriptHeaderItem.forUrl("js/services.js"));

		response.render(CssHeaderItem.forUrl("css/app.css"));

		response.render(CssHeaderItem.forReference(
				new UserCustomCssReference() {
					@Override
					protected String getCss() {
						UserSettings settings = settingsService
								.findByUser(CommaFeedSession.get().getUser());
						return settings == null ? null : settings
								.getCustomCss();
					}
				}, new PageParameters().add("_t", System.currentTimeMillis()),
				null));
	}
}
