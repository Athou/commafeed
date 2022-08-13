package com.commafeed.e2e;

import com.microsoft.playwright.Page;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PlaywrightTestUtils {

	public static void login(Page page) {
		page.locator("[placeholder='User Name or E-mail']").fill("admin");
		page.locator("[placeholder='Password']").fill("admin");
		page.locator("button:has-text('Log in')").click();
	}

}
