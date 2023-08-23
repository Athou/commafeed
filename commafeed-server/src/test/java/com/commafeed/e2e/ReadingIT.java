package com.commafeed.e2e;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import com.commafeed.CommaFeedDropwizardAppExtension;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Locator.WaitForOptions;
import com.microsoft.playwright.assertions.PlaywrightAssertions;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

@ExtendWith(DropwizardExtensionsSupport.class)
@ExtendWith(MockServerExtension.class)
class ReadingIT extends PlaywrightTestBase {

	private static final CommaFeedDropwizardAppExtension EXT = new CommaFeedDropwizardAppExtension();

	private MockServerClient mockServerClient;

	@BeforeEach
	void init(MockServerClient mockServerClient) throws IOException {
		this.mockServerClient = mockServerClient;
		this.mockServerClient.when(HttpRequest.request().withMethod("GET"))
				.respond(HttpResponse.response()
						.withBody(IOUtils.toString(getClass().getResource("/feed/rss.xml"), StandardCharsets.UTF_8)));
	}

	@Test
	void scenario() {
		// login
		page.navigate("http://localhost:" + EXT.getLocalPort());
		page.locator("button:has-text('Log in')").click();
		PlaywrightTestUtils.login(page);
		PlaywrightAssertions.assertThat(page.locator("text=You don't have any subscriptions yet.")).hasCount(1);

		// subscribe
		page.locator("[aria-label='Subscribe']").click();
		page.locator("text=Feed URL *").fill("http://localhost:" + this.mockServerClient.getPort());
		page.locator("button:has-text('Next')").click();
		page.locator("button:has-text('Subscribe')").nth(2).click();

		// subscription has two unread entries
		Locator treeNode = page.locator("nav >> text=CommaFeed test feed2");
		treeNode.waitFor(new WaitForOptions().setTimeout(30000));
		PlaywrightAssertions.assertThat(treeNode).hasCount(1);

		// click on subscription
		treeNode.click();
		Locator entries = page.locator("main >> .mantine-Paper-root");
		PlaywrightAssertions.assertThat(entries).hasCount(2);

		// click on first entry
		page.locator("text='Item 1'").click();
		PlaywrightAssertions.assertThat(page.locator("text=Item 1 description")).hasCount(1);
		PlaywrightAssertions.assertThat(page.locator("text=Item 2 description")).hasCount(0);
		// only one unread entry now
		PlaywrightAssertions.assertThat(page.locator("nav >> text=CommaFeed test feed1")).hasCount(1);

		// click on second entry
		page.locator("text=Item 2").click();
		PlaywrightAssertions.assertThat(page.locator("text=Item 1 description")).hasCount(0);
		PlaywrightAssertions.assertThat(page.locator("text=Item 2 description")).hasCount(1);
	}

}
