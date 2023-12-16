package com.commafeed.integration.rest;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.commafeed.CommaFeedConfiguration.ApplicationSettings;
import com.commafeed.backend.model.User;
import com.commafeed.frontend.model.UserModel;
import com.commafeed.frontend.model.request.IDRequest;
import com.commafeed.integration.BaseIT;

class AdminIT extends BaseIT {

	@Test
	void getApplicationSettings() {
		try (Response response = getClient().target(getApiBaseUrl() + "admin/settings").request().get()) {
			ApplicationSettings settings = response.readEntity(ApplicationSettings.class);
			Assertions.assertTrue(settings.getAllowRegistrations());
		}
	}

	@Nested
	class Users {
		@Test
		void saveThenDeleteNewUser() {
			List<UserModel> existingUsers = getAllUsers();

			User user = new User();
			user.setName("test");
			user.setPassword("test".getBytes());
			user.setEmail("test@test.com");

			try (Response response = getClient().target(getApiBaseUrl() + "admin/user/save").request().post(Entity.json(user))) {
				Assertions.assertEquals(HttpStatus.OK_200, response.getStatus());

				List<UserModel> newUsers = getAllUsers();
				Assertions.assertEquals(existingUsers.size() + 1, newUsers.size());

				UserModel newUser = newUsers.stream().filter(u -> u.getName().equals("test")).findFirst().get();
				user.setId(newUser.getId());
			}

			IDRequest req = new IDRequest();
			req.setId(user.getId());
			try (Response response = getClient().target(getApiBaseUrl() + "admin/user/delete").request().post(Entity.json(req))) {
				Assertions.assertEquals(HttpStatus.OK_200, response.getStatus());

				List<UserModel> newUsers = getAllUsers();
				Assertions.assertEquals(existingUsers.size(), newUsers.size());
			}
		}

		@Test
		void editExistingUser() {
			List<UserModel> existingUsers = getAllUsers();
			UserModel user = existingUsers.stream().filter(u -> u.getName().equals("admin")).findFirst().get();
			user.setEmail("new-email@provider.com");

			try (Response response = getClient().target(getApiBaseUrl() + "admin/user/save").request().post(Entity.json(user))) {
				Assertions.assertEquals(HttpStatus.OK_200, response.getStatus());

				List<UserModel> newUsers = getAllUsers();
				Assertions.assertEquals(existingUsers.size(), newUsers.size());
			}
		}

		private List<UserModel> getAllUsers() {
			try (Response response = getClient().target(getApiBaseUrl() + "admin/user/getAll").request().get()) {
				return Arrays.asList(response.readEntity(UserModel[].class));
			}
		}
	}

}
