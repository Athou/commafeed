package com.commafeed.e2e;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.commafeed.CommaFeedDropwizardAppExtension;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.PlaywrightAssertions;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

@ExtendWith(DropwizardExtensionsSupport.class)
class AuthentificationIT extends PlaywrightTestBase {

	private static final CommaFeedDropwizardAppExtension EXT = new CommaFeedDropwizardAppExtension();

	@Test
	void loginFail() {
		page.navigate(getLoginPageUrl());
		page.locator("[placeholder='User Name or E-mail']").fill("admin");
		page.locator("[placeholder='Password']").fill("wrong_password");
		page.locator("button:has-text('Log in')").click();
		PlaywrightAssertions.assertThat(page.locator("div[role='alert']")).containsText("wrong username or password");
	}

	@Test
	void loginSuccess() {
		page.navigate(getLoginPageUrl());
		PlaywrightTestUtils.login(page);
		PlaywrightAssertions.assertThat(page).hasURL("http://localhost:" + EXT.getLocalPort() + "/#/app/category/all");
	}

	@Test
	void registerFailPasswordTooSimple() {
		page.navigate(getLoginPageUrl());
		page.locator("text=Sign up!").click();
		page.locator("[placeholder='User Name']").fill("user");
		page.locator("[placeholder='E-mail address']").fill("user@domain.com");
		page.locator("[placeholder='Password']").fill("pass");
		page.locator("button:has-text('Sign up')").click();

		Locator alert = page.locator("div[role='alert']");
		PlaywrightAssertions.assertThat(alert).containsText("Password must be 8 or more characters in length.");
		PlaywrightAssertions.assertThat(alert).containsText("Password must contain 1 or more uppercase characters.");
		PlaywrightAssertions.assertThat(alert).containsText("Password must contain 1 or more digit characters.");
		PlaywrightAssertions.assertThat(alert).containsText("Password must contain 1 or more special characters.");
	}

	@Test
	void registerSuccess() {
		page.navigate(getLoginPageUrl());
		page.locator("text=Sign up!").click();
		page.locator("[placeholder='User Name']").fill("user");
		page.locator("[placeholder='E-mail address']").fill("user@domain.com");
		page.locator("[placeholder='Password']").fill("MyPassword1!");
		page.locator("button:has-text('Sign up')").click();
		PlaywrightAssertions.assertThat(page).hasURL("http://localhost:" + EXT.getLocalPort() + "/#/app/category/all");
	}

	private String getLoginPageUrl() {
		return "http://localhost:" + EXT.getLocalPort() + "/#/login";
	}
}
