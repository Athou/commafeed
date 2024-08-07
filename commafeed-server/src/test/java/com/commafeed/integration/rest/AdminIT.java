package com.commafeed.integration.rest;

import java.util.Arrays;
import java.util.List;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.commafeed.backend.model.User;
import com.commafeed.frontend.model.UserModel;
import com.commafeed.frontend.model.request.IDRequest;
import com.commafeed.integration.BaseIT;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.client.Entity;

@QuarkusTest
class AdminIT extends BaseIT {

	@Override
	protected JerseyClientBuilder configureClientBuilder(JerseyClientBuilder base) {
		return base.register(HttpAuthenticationFeature.basic("admin", "admin"));
	}

	@Nested
	class Users {
		@Test
		void saveModifyAndDeleteNewUser() {
			List<UserModel> existingUsers = getAllUsers();

			createUser();
			Assertions.assertEquals(existingUsers.size() + 1, getAllUsers().size());

			modifyUser();
			Assertions.assertEquals(existingUsers.size() + 1, getAllUsers().size());

			deleteUser();
			Assertions.assertEquals(existingUsers.size(), getAllUsers().size());
		}

		private void createUser() {
			User user = new User();
			user.setName("test");
			user.setPassword("test".getBytes());
			user.setEmail("test@test.com");
			getClient().target(getApiBaseUrl() + "admin/user/save").request().post(Entity.json(user), Void.TYPE);
		}

		private void modifyUser() {
			List<UserModel> existingUsers = getAllUsers();
			UserModel user = existingUsers.stream()
					.filter(u -> u.getName().equals("test"))
					.findFirst()
					.orElseThrow(() -> new NullPointerException("User not found"));
			user.setEmail("new-email@provider.com");
			getClient().target(getApiBaseUrl() + "admin/user/save").request().post(Entity.json(user), Void.TYPE);
		}

		private void deleteUser() {
			List<UserModel> existingUsers = getAllUsers();
			UserModel user = existingUsers.stream()
					.filter(u -> u.getName().equals("test"))
					.findFirst()
					.orElseThrow(() -> new NullPointerException("User not found"));

			IDRequest req = new IDRequest();
			req.setId(user.getId());
			getClient().target(getApiBaseUrl() + "admin/user/delete").request().post(Entity.json(req), Void.TYPE);
		}

		private List<UserModel> getAllUsers() {
			return Arrays.asList(getClient().target(getApiBaseUrl() + "admin/user/getAll").request().get(UserModel[].class));
		}
	}

}
