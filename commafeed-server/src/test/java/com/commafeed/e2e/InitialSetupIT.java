package com.commafeed.e2e;

import org.junit.jupiter.api.Test;

import com.commafeed.TestConstants;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;

import io.quarkiverse.playwright.InjectPlaywright;
import io.quarkiverse.playwright.WithPlaywright;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithPlaywright
class InitialSetupIT {

	@InjectPlaywright
	private BrowserContext context;

	@Test
	void createAdminAccount() {
		Page page = context.newPage();
		page.navigate("http://localhost:8085");

		page.getByPlaceholder("Admin User Name").fill(TestConstants.ADMIN_USERNAME);
		page.getByPlaceholder("Password").fill(TestConstants.ADMIN_PASSWORD);
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create Admin Account")).click();

		PlaywrightAssertions.assertThat(page).hasURL("http://localhost:8085/#/app/category/all");
	}
}
