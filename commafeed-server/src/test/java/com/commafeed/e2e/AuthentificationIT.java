package com.commafeed.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.commafeed.TestConstants;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;

import io.quarkiverse.playwright.InjectPlaywright;
import io.quarkiverse.playwright.WithPlaywright;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithPlaywright
class AuthentificationIT {

	@InjectPlaywright
	private BrowserContext context;

	@BeforeEach
	void setup() {
		PlaywrightTestUtils.initialSetup();
	}

	@AfterEach
	void cleanup() {
		context.clearCookies();
	}

	@Test
	void loginFail() {
		Page page = context.newPage();
		page.navigate(getLoginPageUrl());
		PlaywrightTestUtils.login(page, TestConstants.ADMIN_USERNAME, "wrong_password");
		PlaywrightAssertions.assertThat(page.getByRole(AriaRole.ALERT)).containsText("wrong username or password");
	}

	@Test
	void loginSuccess() {
		Page page = context.newPage();
		page.navigate(getLoginPageUrl());
		PlaywrightTestUtils.login(page);
		PlaywrightAssertions.assertThat(page).hasURL("http://localhost:8085/#/app/category/all");
	}

	@Test
	void registerFailPasswordTooSimple() {
		Page page = context.newPage();
		page.navigate(getLoginPageUrl());
		page.getByText("Sign up!").click();
		PlaywrightTestUtils.register(page, "user", "user@domain.com", "pass");

		Locator alert = page.getByRole(AriaRole.ALERT);
		PlaywrightAssertions.assertThat(alert).containsText("Password must be 8 or more characters in length.");
		PlaywrightAssertions.assertThat(alert).containsText("Password must contain 1 or more uppercase characters.");
		PlaywrightAssertions.assertThat(alert).containsText("Password must contain 1 or more digit characters.");
		PlaywrightAssertions.assertThat(alert).containsText("Password must contain 1 or more special characters.");
	}

	@Test
	void registerSuccess() {
		Page page = context.newPage();
		page.navigate(getLoginPageUrl());
		page.getByText("Sign up!").click();
		PlaywrightTestUtils.register(page, "user", "user@domain.com", "MyPassword1!");
		PlaywrightAssertions.assertThat(page).hasURL("http://localhost:8085/#/app/category/all");
	}

	private String getLoginPageUrl() {
		return "http://localhost:8085/#/login";
	}
}
