package com.commafeed.integration.rest;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Entity;

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
		ApplicationSettings settings = getClient().target(getApiBaseUrl() + "admin/settings").request().get(ApplicationSettings.class);
		Assertions.assertTrue(settings.getAllowRegistrations());
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
			getClient().target(getApiBaseUrl() + "admin/user/save").request().post(Entity.json(user), Void.TYPE);

			List<UserModel> newUsers = getAllUsers();
			Assertions.assertEquals(existingUsers.size() + 1, newUsers.size());

			UserModel newUser = newUsers.stream()
					.filter(u -> u.getName().equals("test"))
					.findFirst()
					.orElseThrow(() -> new NullPointerException("User not found"));
			user.setId(newUser.getId());

			IDRequest req = new IDRequest();
			req.setId(user.getId());
			getClient().target(getApiBaseUrl() + "admin/user/delete").request().post(Entity.json(req), Void.TYPE);
			Assertions.assertEquals(existingUsers.size(), getAllUsers().size());
		}

		@Test
		void editExistingUser() {
			List<UserModel> existingUsers = getAllUsers();
			UserModel user = existingUsers.stream()
					.filter(u -> u.getName().equals("admin"))
					.findFirst()
					.orElseThrow(() -> new NullPointerException("User not found"));
			user.setEmail("new-email@provider.com");

			getClient().target(getApiBaseUrl() + "admin/user/save").request().post(Entity.json(user), Void.TYPE);
			Assertions.assertEquals(existingUsers.size(), getAllUsers().size());
		}

		private List<UserModel> getAllUsers() {
			return Arrays.asList(getClient().target(getApiBaseUrl() + "admin/user/getAll").request().get(UserModel[].class));
		}
	}

}
