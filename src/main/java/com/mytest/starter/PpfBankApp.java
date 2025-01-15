package com.mytest.starter;

import com.mytest.api.RestApi;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.core.json.JsonObject;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Třída PpfBankApp představuje vstupní bod aplikace PPF Banka.
 * Třída obsahuje metody pro spuštění a zastavení aplikace..
 */
public class PpfBankApp extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(PpfBankApp.class);

    /**
     * Metoda pro spuštění aplikace.
     * Metoda načte konfiguraci z konfiguračního souboru a spustí server HTTP.
     * Pokud se konfigurace nepodaří načíst, metoda vypíše chybovou hlášku.
     * Pokud se nepodaří spustit server HTTP, metoda rovněž vypíše chybovou hlášku.
     *
     */
    @Override
    public void start() throws Exception {
        Promise<Void> configPromise = Promise.promise();

        ConfigStoreOptions fileStore = new ConfigStoreOptions()
                .setType("file")
                .setConfig(new JsonObject().put("path", "config.json"));

        ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(fileStore);
        ConfigRetriever retriever = ConfigRetriever.create(vertx, options);

        retriever.getConfig(ar -> {
            if (ar.succeeded()) {
                JsonObject config = ar.result();
                vertx.sharedData().getLocalMap("app-config").put("dbConfig", config.getJsonObject("db"));
                vertx.sharedData().getLocalMap("app-config").put("httpConfig", config.getJsonObject("http"));
                logger.info("Konfigurace byla úspěšně načtena");
                configPromise.complete();
            } else {
                logger.error("Nepodařilo se načíst konfiguraci. Chyba: " + ar.cause().getMessage());
                configPromise.fail(ar.cause());
            }
        });
        configPromise.future().onComplete(ar -> {
            if (ar.succeeded()) {
                JsonObject httpConfig = (JsonObject) vertx.sharedData().getLocalMap("app-config").get("httpConfig");
                int localServerPort = httpConfig.getInteger("port", 8080);
                Router router = new RestApi(vertx).createRouter();

                vertx.createHttpServer().requestHandler(router).listen(localServerPort, http -> {
                    if (http.succeeded()) {
                        logger.info("Server HTTP byl spuštěn na portu " + localServerPort);
                    } else {
                        logger.error("Nepodařilo se spustit server HTTP. Chyba: " + http.cause().getMessage());
                    }
                });
            } else {
                logger.error("Konfigurace nebyla správně uložena, server HTTP nebude spuštěn.");
            }
        });
    }

    /**
     * Metoda pro ukončení aplikace.
     * Metoda zavře instanci třídy Vertx.
     * Pokud se nepodaří instanci třídy Vertx zavřít, metoda vypíše chybovou hlášku.
     *
     */
    @Override
    public void stop() throws Exception {
        vertx.close(ar -> {
            if (ar.succeeded()) {
                logger.info("Aplikace byla úspěšně ukončena.");
            } else {
                logger.error("Chyba při ukončování aplikace: " + ar.cause().getMessage());
            }
        });
    }
}
