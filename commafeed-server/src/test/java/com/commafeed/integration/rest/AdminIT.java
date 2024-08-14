package com.commafeed.integration.rest;

import java.util.List;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.commafeed.backend.model.User;
import com.commafeed.frontend.model.UserModel;
import com.commafeed.frontend.model.request.IDRequest;
import com.commafeed.integration.BaseIT;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import jakarta.ws.rs.core.MediaType;

@QuarkusTest
class AdminIT extends BaseIT {

	@BeforeEach
	void setup() {
		RestAssured.authentication = RestAssured.preemptive().basic("admin", "admin");
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
			RestAssured.given()
					.body(user)
					.contentType(MediaType.APPLICATION_JSON)
					.post("rest/admin/user/save")
					.then()
					.statusCode(HttpStatus.SC_OK);
		}

		private void modifyUser() {
			List<UserModel> existingUsers = getAllUsers();
			UserModel user = existingUsers.stream()
					.filter(u -> u.getName().equals("test"))
					.findFirst()
					.orElseThrow(() -> new NullPointerException("User not found"));
			user.setEmail("new-email@provider.com");
			RestAssured.given()
					.body(user)
					.contentType(MediaType.APPLICATION_JSON)
					.post("rest/admin/user/save")
					.then()
					.statusCode(HttpStatus.SC_OK);
		}

		private void deleteUser() {
			List<UserModel> existingUsers = getAllUsers();
			UserModel user = existingUsers.stream()
					.filter(u -> u.getName().equals("test"))
					.findFirst()
					.orElseThrow(() -> new NullPointerException("User not found"));

			IDRequest req = new IDRequest();
			req.setId(user.getId());
			RestAssured.given()
					.body(req)
					.contentType(MediaType.APPLICATION_JSON)
					.post("rest/admin/user/delete")
					.then()
					.statusCode(HttpStatus.SC_OK);
		}

		private List<UserModel> getAllUsers() {
			return List.of(
					RestAssured.given().get("rest/admin/user/getAll").then().statusCode(HttpStatus.SC_OK).extract().as(UserModel[].class));
		}
	}

}
