package com.commafeed.e2e;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

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
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;

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
						.withBody(IOUtils.toString(getClass().getResource("/feed/rss.xml"), StandardCharsets.UTF_8))
						.withDelay(TimeUnit.MILLISECONDS, 100));
	}

	@Test
	void scenario() {
		// login
		page.navigate("http://localhost:" + EXT.getLocalPort());
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Log in")).click();
		PlaywrightTestUtils.login(page);

		Locator header = page.getByRole(AriaRole.BANNER);
		Locator sidebar = page.getByRole(AriaRole.NAVIGATION);
		Locator main = page.getByRole(AriaRole.MAIN);

		PlaywrightAssertions.assertThat(main.getByText("You don't have any subscriptions yet.")).hasCount(1);

		// subscribe
		header.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Subscribe")).click();
		main.getByText("Feed URL *").fill("http://localhost:" + this.mockServerClient.getPort());
		main.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Next")).click();
		main.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Subscribe").setExact(true)).click();

		// click on subscription
		sidebar.getByText(Pattern.compile("CommaFeed test feed\\d+")).click();

		// we have two unread entries
		PlaywrightAssertions.assertThat(main.locator(".mantine-Paper-root")).hasCount(2);

		// click on first entry
		main.getByText("Item 1").click();
		PlaywrightAssertions.assertThat(main.getByText("Item 1 description")).hasCount(1);
		PlaywrightAssertions.assertThat(main.getByText("Item 2 description")).hasCount(0);

		// click on subscription
		sidebar.getByText(Pattern.compile("CommaFeed test feed\\d+")).click();

		// only one unread entry now
		PlaywrightAssertions.assertThat(main.locator(".mantine-Paper-root")).hasCount(1);

		// click on second entry
		main.getByText("Item 2").click();
		PlaywrightAssertions.assertThat(main.getByText("Item 1 description")).hasCount(0);
		PlaywrightAssertions.assertThat(main.getByText("Item 2 description")).hasCount(1);
	}

}
