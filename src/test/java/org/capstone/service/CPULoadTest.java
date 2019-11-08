package org.capstone.service;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class CPULoadTest {

    @Test
    public void testCreateLoadEndpoint() {
            given()
                    .when().get("/create-load")
                    .then()
                    .statusCode(200);
    }

}