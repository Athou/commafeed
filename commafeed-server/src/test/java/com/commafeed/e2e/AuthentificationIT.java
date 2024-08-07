package com.commafeed.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class AuthentificationIT {

	private final Playwright playwright = Playwright.create();
	private final Browser browser = playwright.chromium().launch();

	private Page page;

	@BeforeEach
	void init() {
		page = browser.newContext().newPage();
	}

	@AfterEach
	void cleanup() {
		playwright.close();
	}

	@Test
	void loginFail() {
		page.navigate(getLoginPageUrl());
		page.getByPlaceholder("User Name or E-mail").fill("admin");
		page.getByPlaceholder("Password").fill("wrong_password");
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Log in")).click();
		PlaywrightAssertions.assertThat(page.getByRole(AriaRole.ALERT)).containsText("wrong username or password");
	}

	@Test
	void loginSuccess() {
		page.navigate(getLoginPageUrl());
		PlaywrightTestUtils.login(page);
		PlaywrightAssertions.assertThat(page).hasURL("http://localhost:8085/#/app/category/all");
	}

	@Test
	void registerFailPasswordTooSimple() {
		page.navigate(getLoginPageUrl());
		page.getByText("Sign up!").click();
		page.getByPlaceholder("User Name").fill("user");
		page.getByPlaceholder("E-mail address").fill("user@domain.com");
		page.getByPlaceholder("Password").fill("pass");
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign up")).click();

		Locator alert = page.getByRole(AriaRole.ALERT);
		PlaywrightAssertions.assertThat(alert).containsText("Password must be 8 or more characters in length.");
		PlaywrightAssertions.assertThat(alert).containsText("Password must contain 1 or more uppercase characters.");
		PlaywrightAssertions.assertThat(alert).containsText("Password must contain 1 or more digit characters.");
		PlaywrightAssertions.assertThat(alert).containsText("Password must contain 1 or more special characters.");
	}

	@Test
	void registerSuccess() {
		page.navigate(getLoginPageUrl());
		page.getByText("Sign up!").click();
		page.getByPlaceholder("User Name").fill("user");
		page.getByPlaceholder("E-mail address").fill("user@domain.com");
		page.getByPlaceholder("Password").fill("MyPassword1!");
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign up")).click();
		PlaywrightAssertions.assertThat(page).hasURL("http://localhost:8085/#/app/category/all");
	}

	private String getLoginPageUrl() {
		return "http://localhost:8085/#/login";
	}
}
