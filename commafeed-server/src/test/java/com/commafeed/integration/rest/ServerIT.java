package com.commafeed.integration.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.commafeed.frontend.model.ServerInfo;
import com.commafeed.integration.BaseIT;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
class ServerIT extends BaseIT {

	@Test
	void getServerInfos() {
		ServerInfo serverInfos = RestAssured.given().get("/rest/server/get").then().statusCode(200).extract().as(ServerInfo.class);
		Assertions.assertTrue(serverInfos.isAllowRegistrations());
		Assertions.assertTrue(serverInfos.isSmtpEnabled());
		Assertions.assertTrue(serverInfos.isDemoAccountEnabled());
		Assertions.assertTrue(serverInfos.isWebsocketEnabled());
		Assertions.assertEquals(900000, serverInfos.getWebsocketPingInterval());
		Assertions.assertEquals(30000, serverInfos.getTreeReloadInterval());
		Assertions.assertEquals(60000, serverInfos.getForceRefreshCooldownDuration());
		Assertions.assertEquals(4, serverInfos.getMinimumPasswordLength());

	}
}
