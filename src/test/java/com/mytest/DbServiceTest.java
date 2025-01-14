package com.mytest;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import com.mytest.db.DbService;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Třída DbServiceTest představuje testovací třídu pro testování třídy DbService.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension.class)
public class DbServiceTest {
    private DbService dbService;

    @BeforeAll
    void setUp(Vertx vertx, VertxTestContext testContext) {
        dbService = new DbService(vertx);
        testContext.completeNow();
    }

    @Test
    void testReadResource(VertxTestContext testContext) {
        String result = dbService.readResourceStr(getClass().getClassLoader().getResource("mocktext.txt"));
        assertTrue(result.contains("This is just a test. =)"));
        testContext.completeNow();
    }
}
