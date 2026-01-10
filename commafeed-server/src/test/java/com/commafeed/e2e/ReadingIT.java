package com.commafeed.e2e;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import com.commafeed.TestConstants;
import com.commafeed.frontend.model.Entries;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;

import io.quarkiverse.playwright.InjectPlaywright;
import io.quarkiverse.playwright.WithPlaywright;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
@WithPlaywright
class ReadingIT {

	@InjectPlaywright
	private BrowserContext context;

	private MockServerClient mockServerClient;

	@BeforeEach
	void init() throws IOException {
		this.mockServerClient = ClientAndServer.startClientAndServer(0);
		this.mockServerClient.when(HttpRequest.request().withMethod("GET"))
				.respond(HttpResponse.response()
						.withBody(IOUtils.toString(getClass().getResource("/feed/rss.xml"), StandardCharsets.UTF_8))
						.withDelay(TimeUnit.MILLISECONDS, 100));

		PlaywrightTestUtils.initialSetup();
		RestAssured.authentication = RestAssured.preemptive().basic(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
	}

	@AfterEach
	void cleanup() {
		RestAssured.reset();
	}

	@Test
	void scenario() {
		Page page = context.newPage();

		// login
		page.navigate("http://localhost:8085");
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
		PlaywrightAssertions.assertThat(main.getByRole(AriaRole.ARTICLE)).hasCount(2);

		// click on first entry
		main.getByText("Item 1").click();
		PlaywrightAssertions.assertThat(main.getByText("Item 1 description")).hasCount(1);
		PlaywrightAssertions.assertThat(main.getByText("Item 2 description")).hasCount(0);

		// wait for the entry to be marked as read since the UI is updated immediately while the entry is marked as read in the background
		Awaitility.await()
				.atMost(15, TimeUnit.SECONDS)
				.until(() -> RestAssured.given()
						.get("rest/category/entries?id=all&readType=unread")
						.then()
						.statusCode(HttpStatus.SC_OK)
						.extract()
						.as(Entries.class), e -> e.getEntries().size() == 1);

		// click on subscription
		sidebar.getByText(Pattern.compile("CommaFeed test feed\\d*")).click();

		// only one unread entry now
		PlaywrightAssertions.assertThat(main.getByRole(AriaRole.ARTICLE)).hasCount(1);

		// click on second entry
		main.getByText("Item 2").click();
		PlaywrightAssertions.assertThat(main.getByText("Item 1 description")).hasCount(0);
		PlaywrightAssertions.assertThat(main.getByText("Item 2 description")).hasCount(1);
	}

}
