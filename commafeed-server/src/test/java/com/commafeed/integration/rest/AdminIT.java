package com.commafeed.integration.rest;

import java.util.List;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.commafeed.TestConstants;
import com.commafeed.frontend.model.UserModel;
import com.commafeed.frontend.model.request.AdminSaveUserRequest;
import com.commafeed.frontend.model.request.IDRequest;
import com.commafeed.integration.BaseIT;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@QuarkusTest
class AdminIT extends BaseIT {

	@BeforeEach
	void setup() {
		initialSetup(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
		RestAssured.authentication = RestAssured.preemptive().basic(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
	}

	@AfterEach
	void cleanup() {
		RestAssured.reset();
	}

	@Nested
	class Users {
		@Test
		void saveModifyAndDeleteNewUser() {
			List<UserModel> existingUsers = getAllUsers();

			long userId = createUser();
			Assertions.assertEquals(existingUsers.size() + 1, getAllUsers().size());

			UserModel user = getUser(userId);
			Assertions.assertEquals("test", user.getName());

			modifyUser(user);
			Assertions.assertEquals(existingUsers.size() + 1, getAllUsers().size());

			deleteUser();
			Assertions.assertEquals(existingUsers.size(), getAllUsers().size());
		}

		private long createUser() {
			AdminSaveUserRequest user = new AdminSaveUserRequest();
			user.setName("test");
			user.setPassword("Test1234!");
			user.setEmail("test@test.com");
			user.setEnabled(true);
			String response = RestAssured.given()
					.body(user)
					.contentType(ContentType.JSON)
					.post("rest/admin/user/save")
					.then()
					.statusCode(HttpStatus.SC_OK)
					.extract()
					.asString();
			return Long.parseLong(response);
		}

		private UserModel getUser(long userId) {
			return RestAssured.given()
					.get("rest/admin/user/get/{id}", userId)
					.then()
					.statusCode(HttpStatus.SC_OK)
					.extract()
					.as(UserModel.class);
		}

		private void modifyUser(UserModel user) {
			user.setEmail("new-email@provider.com");
			RestAssured.given().body(user).contentType(ContentType.JSON).post("rest/admin/user/save").then().statusCode(HttpStatus.SC_OK);
		}

		private void deleteUser() {
			List<UserModel> existingUsers = getAllUsers();
			UserModel user = existingUsers.stream()
					.filter(u -> u.getName().equals("test"))
					.findFirst()
					.orElseThrow(() -> new NullPointerException("User not found"));

			IDRequest req = new IDRequest();
			req.setId(user.getId());
			RestAssured.given().body(req).contentType(ContentType.JSON).post("rest/admin/user/delete").then().statusCode(HttpStatus.SC_OK);
		}

		private List<UserModel> getAllUsers() {
			return List.of(
					RestAssured.given().get("rest/admin/user/getAll").then().statusCode(HttpStatus.SC_OK).extract().as(UserModel[].class));
		}
	}

}
