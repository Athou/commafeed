package com.commafeed.frontend.pages;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.commafeed.backend.security.Role;
import com.commafeed.frontend.references.angular.AngularReference;
import com.commafeed.frontend.references.angular.AngularResourceReference;
import com.commafeed.frontend.references.angular.AngularSanitizeReference;
import com.commafeed.frontend.references.angularui.AngularUIReference;
import com.commafeed.frontend.references.angularuibootstrap.AngularUIBootstrapReference;
import com.commafeed.frontend.references.csstreeview.CssTreeViewReference;
import com.commafeed.frontend.references.nginfinitescroll.NGInfiniteScrollReference;
import com.commafeed.frontend.references.ngupload.NGUploadReference;
import com.commafeed.frontend.references.select2.Select2Reference;
import com.commafeed.frontend.references.spinjs.SpinJSReference;

@SuppressWarnings("serial")
@AuthorizeInstantiation(Role.USER)
public class HomePage extends BasePage {

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		AngularReference.renderHead(response);
		AngularResourceReference.renderHead(response);
		AngularSanitizeReference.renderHead(response);
		AngularUIReference.renderHead(response);
		AngularUIBootstrapReference.renderHead(response);
		NGUploadReference.renderHead(response);
		NGInfiniteScrollReference.renderHead(response);
		Select2Reference.renderHead(response);
		SpinJSReference.renderHead(response);

		CssTreeViewReference.renderHead(response);

		response.render(JavaScriptHeaderItem.forUrl("js/main.js"));
		response.render(JavaScriptHeaderItem.forUrl("js/controllers.js"));
		response.render(JavaScriptHeaderItem.forUrl("js/directives.js"));
		response.render(JavaScriptHeaderItem.forUrl("js/services.js"));

		response.render(CssHeaderItem.forUrl("css/app.css"));
	}
}
