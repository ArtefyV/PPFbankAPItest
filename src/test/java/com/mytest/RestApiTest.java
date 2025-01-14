package com.mytest;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import com.mytest.api.RestApi;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Třída RestApiTest představuje testovací třídu pro testování třídy RestApi.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension.class)
public class RestApiTest {

    @BeforeAll
    void setUp(Vertx vertx, VertxTestContext testContext) {
        Router router = new RestApi(vertx).createRouter();
        vertx.createHttpServer().requestHandler(router).listen(8080, testContext.succeeding(id -> testContext.completeNow()));
    }

    @Test
    void testHiEndpoint(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(8080, "localhost", "/hi").send(testContext.succeeding(response -> {
            assertEquals(200, response.statusCode());
            assertEquals("PPF Bank vás srdečně vítá!", response.bodyAsString());
            testContext.completeNow();
        }));
    }
}
