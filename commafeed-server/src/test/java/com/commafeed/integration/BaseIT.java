package com.commafeed.integration;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.HttpHeaders;

import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import com.commafeed.TestConstants;
import com.commafeed.frontend.model.Category;
import com.commafeed.frontend.model.Entries;
import com.commafeed.frontend.model.Subscription;
import com.commafeed.frontend.model.request.AddCategoryRequest;
import com.commafeed.frontend.model.request.InitialSetupRequest;
import com.commafeed.frontend.model.request.SubscribeRequest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.Getter;

@Getter
public abstract class BaseIT {

	private static final HttpRequest FEED_REQUEST = HttpRequest.request().withMethod("GET").withPath("/");

	@Getter
	private MockServerClient mockServerClient;
	private Client client;
	private String feedUrl;
	private String baseUrl;
	private String apiBaseUrl;
	private String webSocketUrl;

	@BeforeEach
	void init() throws IOException {
		this.mockServerClient = ClientAndServer.startClientAndServer(0);

		this.feedUrl = "http://localhost:" + mockServerClient.getPort() + "/";
		this.baseUrl = "http://localhost:8085/";
		this.apiBaseUrl = this.baseUrl + "rest/";
		this.webSocketUrl = "ws://localhost:8085/ws";

		URL resource = Objects.requireNonNull(getClass().getResource("/feed/rss.xml"));
		this.mockServerClient.when(FEED_REQUEST)
				.respond(HttpResponse.response().withBody(IOUtils.toString(resource, StandardCharsets.UTF_8)));
	}

	@AfterEach
	void cleanup() {
		if (this.mockServerClient != null) {
			this.mockServerClient.close();
		}

		if (this.client != null) {
			this.client.close();
		}
	}

	protected void feedNowReturnsMoreEntries() throws IOException {
		mockServerClient.clear(FEED_REQUEST);

		URL resource = Objects.requireNonNull(getClass().getResource("/feed/rss_2.xml"));
		mockServerClient.when(FEED_REQUEST).respond(HttpResponse.response().withBody(IOUtils.toString(resource, StandardCharsets.UTF_8)));
	}

	protected void initialSetup(String userName, String password) {
		InitialSetupRequest req = new InitialSetupRequest();
		req.setName(userName);
		req.setPassword(password);
		req.setEmail(userName + "@commafeed.com");

		RestAssured.given().body(req).contentType(ContentType.JSON).post("rest/user/initialSetup").then().statusCode(200);
	}

	protected List<HttpCookie> login() {
		List<Header> setCookieHeaders = RestAssured.given()
				.auth()
				.none()
				.formParams("j_username", TestConstants.ADMIN_USERNAME, "j_password", TestConstants.ADMIN_PASSWORD)
				.post("j_security_check")
				.then()
				.statusCode(HttpStatus.SC_OK)
				.extract()
				.headers()
				.getList(HttpHeaders.SET_COOKIE);
		return setCookieHeaders.stream().flatMap(h -> HttpCookie.parse(h.getValue()).stream()).toList();
	}

	protected String createCategory(String name) {
		AddCategoryRequest addCategoryRequest = new AddCategoryRequest();
		addCategoryRequest.setName(name);
		return RestAssured.given()
				.body(addCategoryRequest)
				.contentType(ContentType.JSON)
				.post("rest/category/add")
				.then()
				.extract()
				.as(String.class);
	}

	protected Category getRootCategory() {
		return RestAssured.given().get("rest/category/get").then().statusCode(HttpStatus.SC_OK).extract().as(Category.class);
	}

	protected Long subscribe(String feedUrl) {
		return subscribe(feedUrl, null);
	}

	protected Long subscribe(String feedUrl, String categoryId) {
		return subscribe(feedUrl, categoryId, false);
	}

	protected Long subscribe(String feedUrl, String categoryId, boolean pushNotificationsEnabled) {
		SubscribeRequest subscribeRequest = new SubscribeRequest();
		subscribeRequest.setUrl(feedUrl);
		subscribeRequest.setTitle("my title for this feed");
		subscribeRequest.setCategoryId(categoryId);
		subscribeRequest.setPushNotificationsEnabled(pushNotificationsEnabled);
		return RestAssured.given()
				.body(subscribeRequest)
				.contentType(ContentType.JSON)
				.post("rest/feed/subscribe")
				.then()
				.statusCode(HttpStatus.SC_OK)
				.extract()
				.as(Long.class);
	}

	protected Long subscribeAndWaitForEntries(String feedUrl) {
		return subscribeAndWaitForEntries(feedUrl, null);
	}

	protected Long subscribeAndWaitForEntries(String feedUrl, String categoryId) {
		Long subscriptionId = subscribe(feedUrl, categoryId);
		Awaitility.await().atMost(Duration.ofSeconds(15)).until(() -> getFeedEntries(subscriptionId), e -> e.getEntries().size() == 2);
		return subscriptionId;
	}

	protected Subscription getSubscription(Long subscriptionId) {
		return RestAssured.given()
				.get("rest/feed/get/{id}", subscriptionId)
				.then()
				.statusCode(HttpStatus.SC_OK)
				.extract()
				.as(Subscription.class);
	}

	protected Entries getFeedEntries(long subscriptionId) {
		return RestAssured.given()
				.get("rest/feed/entries?id={id}&readType=all", subscriptionId)
				.then()
				.statusCode(HttpStatus.SC_OK)
				.extract()
				.as(Entries.class);
	}

	protected Entries getCategoryEntries(String categoryId) {
		return RestAssured.given()
				.get("rest/category/entries?id={id}&readType=all", categoryId)
				.then()
				.statusCode(HttpStatus.SC_OK)
				.extract()
				.as(Entries.class);
	}

	protected Entries getCategoryEntries(String categoryId, String keywords) {
		return RestAssured.given()
				.get("rest/category/entries?id={id}&readType=all&keywords={keywords}", categoryId, keywords)
				.then()
				.statusCode(HttpStatus.SC_OK)
				.extract()
				.as(Entries.class);
	}

	protected Entries getTaggedEntries(String tag) {
		return RestAssured.given()
				.get("rest/category/entries?id=all&readType=all&tag={tag}", tag)
				.then()
				.statusCode(HttpStatus.SC_OK)
				.extract()
				.as(Entries.class);
	}

	protected int forceRefreshAllFeeds() {
		return RestAssured.given().get("rest/feed/refreshAll").then().extract().statusCode();
	}
}
