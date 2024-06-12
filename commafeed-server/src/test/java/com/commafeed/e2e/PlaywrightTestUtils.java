package com.commafeed.e2e;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.GetByRoleOptions;
import com.microsoft.playwright.options.AriaRole;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PlaywrightTestUtils {

	public static void login(Page page) {
		page.getByPlaceholder("User Name or E-mail").fill("admin");
		page.getByPlaceholder("Password").fill("admin");
		page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Log in")).click();
	}

}
