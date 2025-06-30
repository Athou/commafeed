package com.commafeed.e2e;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.GetByRoleOptions;
import com.microsoft.playwright.options.AriaRole;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PlaywrightTestUtils {

	public static void login(Page page) {
		login(page, "admin", "admin");
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
