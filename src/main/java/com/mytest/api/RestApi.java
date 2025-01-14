package com.mytest.api;

import com.mytest.db.DbService;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Třída RestApi představuje REST API aplikace PPF Banka.
 */
public class RestApi {
    private final Vertx vertx;
    private Router restApi;
    private static final String SETUP_ACTION = "setup";
    private static final String DROP_ACTION = "drop";
    private static final String FILL_ACTION = "fill";
    private static final String TEST_ACTION = "test";
    private static final String DEFAULT_CHARSET = "UTF-8";

    private static final String BODY_MISSING = "Chybí tělo požadavku";

    /**
     * Konstruktor třídy RestApi.
     * @param vertX instance třídy Vertx
     */
    public RestApi(Vertx vertX){
        this.vertx = vertX;
    }

    /**
     * Metoda získání hotového routeru.
     * @return router
     */
    public Router build() {
        return restApi;
    }

    /**
     * Metoda pro vytvoření routeru a definici jednotlivých cest.
     * @return instance třídy RestApi
     */
    public RestApi createRouter() {
        restApi = Router.router(vertx);

        restApi.get("/hi").handler(res -> {
            res.response()
                    .putHeader(HttpHeaders.CONTENT_TYPE, "text/plain; charset="+DEFAULT_CHARSET)
                    .end("PPF Bank vás srdečně vítá!");
        });

        restApi.get("/accounts/:accountId/transactions").handler(res -> {
            String accountId = res.request().getParam("accountId");
            if(accountId == null) {
                res.response()
                        .setStatusCode(400)
                        .putHeader(HttpHeaders.CONTENT_TYPE, "text/plain; charset="+DEFAULT_CHARSET)
                        .end("Chybí parametr \"accountId\" v cestě" );
                return;
            }
            String actionResult;
            DbService dbService = new DbService(vertx);
            actionResult = dbService.getTransactionsByAccountNumber(accountId);

            res.response()
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset="+DEFAULT_CHARSET)
                    .end(actionResult);

        });

        restApi.route().handler(BodyHandler.create());

        restApi.post("/db").handler(res -> {
            String actionResult = "";
            JsonObject body = res.body().asJsonObject();
            if(body == null) {
                res.response()
                        .setStatusCode(400)
                        .putHeader(HttpHeaders.CONTENT_TYPE, "text/plain; charset="+DEFAULT_CHARSET)
                        .end(BODY_MISSING);
                return;
            }
            String action = body.getString("action");
            if (action == null) {
                res.response()
                        .setStatusCode(400)
                        .putHeader(HttpHeaders.CONTENT_TYPE, "text/plain; charset="+DEFAULT_CHARSET)
                        .end("Chybí parametr \"action\" v těle požadavku");
                return;
            }

            if(action.equals(SETUP_ACTION) || action.equals(DROP_ACTION) || action.equals(FILL_ACTION) || action.equals(TEST_ACTION)) {

                DbService dbService = new DbService(vertx);

                switch (action) {
                    case SETUP_ACTION:
                        actionResult = dbService.setupDatabase();
                        break;
                    case DROP_ACTION:
                        actionResult = dbService.dropDatabase();
                        break;
                    case FILL_ACTION:
                        actionResult = dbService.fillUpDatabase();
                        break;
                    case TEST_ACTION:
                        actionResult = dbService.testDatabase();
                        break;
                }
            } else {
                res.response()
                        .setStatusCode(400)
                        .putHeader(HttpHeaders.CONTENT_TYPE, "text/plain; charset="+DEFAULT_CHARSET)
                        .end("Neznámá akce: " + action);
                return;
            }

            res.response()
                    .putHeader(HttpHeaders.CONTENT_TYPE, "text/plain; charset="+DEFAULT_CHARSET)
                    .end(String.format("Akce \"%s\" dokončena s výsledkem: %s", action, actionResult));
        });

        restApi.post("/accounts/create").handler(res -> {
            String actionResult;
            String body = res.body().asString();
            if(body == null) {
                res.response()
                        .setStatusCode(400)
                        .putHeader(HttpHeaders.CONTENT_TYPE, "text/plain; charset="+DEFAULT_CHARSET)
                        .end(BODY_MISSING);
                return;
            }
            DbService dbService = new DbService(vertx);
            actionResult = dbService.createAccount(body);

            res.response()
                    .putHeader(HttpHeaders.CONTENT_TYPE, "text/plain.; charset="+DEFAULT_CHARSET)
                    .end(actionResult);
        });

        restApi.post("/statements/create").handler(res -> {
            String actionResult;
            String body = res.body().asString();
            if(body == null) {
                res.response()
                        .setStatusCode(400)
                        .putHeader(HttpHeaders.CONTENT_TYPE, "text/plain; charset="+DEFAULT_CHARSET)
                        .end(BODY_MISSING);
                return;
            }
            DbService dbService = new DbService(vertx);
            actionResult = dbService.createStatement(body);

            res.response()
                    .putHeader(HttpHeaders.CONTENT_TYPE, "text/plain; charset="+DEFAULT_CHARSET)
                    .end(actionResult);
        });

        restApi.post("/transactions/type/create").handler(res -> {
            String actionResult;
            String body = res.body().asString();
            if(body == null) {
                res.response()
                        .setStatusCode(400)
                        .putHeader(HttpHeaders.CONTENT_TYPE, "text/plain; charset="+DEFAULT_CHARSET)
                        .end(BODY_MISSING);
                return;
            }
            DbService dbService = new DbService(vertx);
            actionResult = dbService.createTransactionType(body);

            res.response()
                    .putHeader(HttpHeaders.CONTENT_TYPE, "text/plain; charset="+DEFAULT_CHARSET)
                    .end(actionResult);
        });

        restApi.post("/transactions/create").handler(res -> {
            String actionResult;
            String body = res.body().asString();
            if(body == null) {
                res.response()
                        .setStatusCode(400)
                        .putHeader(HttpHeaders.CONTENT_TYPE, "text/plain; charset="+DEFAULT_CHARSET)
                        .end(BODY_MISSING);
                return;
            }
            DbService dbService = new DbService(vertx);
            actionResult = dbService.createTransaction(body);

            res.response()
                    .putHeader(HttpHeaders.CONTENT_TYPE, "text/plain; charset="+DEFAULT_CHARSET)
                    .end(actionResult);
        });

        return this;
    }
}
