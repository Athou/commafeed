package com.commafeed.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.assertions.PlaywrightAssertions;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class DocumentationIT {

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
	void documentationAvailable() {
		page.navigate("http://localhost:8085/#/api");
		PlaywrightAssertions.assertThat(page.getByText("Download OpenAPI specification:")).isVisible();
	}

}
