package com.commafeed.e2e;

import org.junit.jupiter.api.Test;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;

import io.quarkiverse.playwright.InjectPlaywright;
import io.quarkiverse.playwright.WithPlaywright;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithPlaywright
class DocumentationIT {

	@InjectPlaywright
	private BrowserContext context;

	@Test
	void documentationAvailable() {
		Page page = context.newPage();
		page.navigate("http://localhost:8085/#/api");
		PlaywrightAssertions.assertThat(page.getByText("Download OpenAPI specification:")).isVisible();
	}

}
