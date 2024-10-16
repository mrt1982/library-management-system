package com.identitye2e.library;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class MetricsMediumTest extends AbstractMediumTest {
    @Test
    void testHealthCheck() {
        given().log().all()
                .contentType(JSON)
                .when()
                .get("/health")
                .then().log().all()
                .assertThat().statusCode(200).contentType(JSON)
                .assertThat().body(notNullValue())
                .assertThat().body("status", equalTo("UP"));
    }

}
