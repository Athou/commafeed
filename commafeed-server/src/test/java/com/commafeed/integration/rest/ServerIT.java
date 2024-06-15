package com.commafeed.integration.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.commafeed.frontend.model.ServerInfo;
import com.commafeed.integration.BaseIT;

public class ServerIT extends BaseIT {

	@Test
	void getServerInfos() {
		ServerInfo serverInfos = getClient().target(getApiBaseUrl() + "server/get").request().get(ServerInfo.class);
		Assertions.assertTrue(serverInfos.isAllowRegistrations());
		Assertions.assertTrue(serverInfos.isSmtpEnabled());
		Assertions.assertTrue(serverInfos.isDemoAccountEnabled());
		Assertions.assertTrue(serverInfos.isWebsocketEnabled());
		Assertions.assertEquals(900000, serverInfos.getWebsocketPingInterval());
		Assertions.assertEquals(10000, serverInfos.getTreeReloadInterval());

	}
}
