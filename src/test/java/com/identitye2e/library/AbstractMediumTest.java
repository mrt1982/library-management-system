package com.identitye2e.library;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.identitye2e.library.application.LibraryApplication;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {LibraryApplication.class})
public abstract class AbstractMediumTest {
    protected ObjectMapper om = new ObjectMapper();
    @LocalServerPort private int serverPort;

    @BeforeEach
    public void setUp() {
        RestAssured.port = serverPort;
        RestAssured.basePath = "/library-api";
    }
}
