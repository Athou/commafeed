package com.commafeed.e2e;

import com.commafeed.TestConstants;
import com.commafeed.frontend.model.request.InitialSetupRequest;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.GetByRoleOptions;
import com.microsoft.playwright.options.AriaRole;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PlaywrightTestUtils {

	public static void initialSetup() {
		InitialSetupRequest req = new InitialSetupRequest();
		req.setName(TestConstants.ADMIN_USERNAME);
		req.setPassword(TestConstants.ADMIN_PASSWORD);

		RestAssured.given().body(req).contentType(ContentType.JSON).post("rest/user/initialSetup").then().statusCode(200);
	}

	public static void login(Page page) {
		login(page, TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
	}

	public static void login(Page page, String username, String password) {
		page.getByPlaceholder("User Name or E-mail").fill(username);
		page.getByPlaceholder("Password").fill(password);
		page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Log in")).click();
	}

	public static void register(Page page, String username, String email, String password) {
		page.getByPlaceholder("E-mail address").fill(email);
		page.getByPlaceholder("User Name").fill(username);
		page.getByPlaceholder("Password").fill(password);
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign up")).click();
	}

}
