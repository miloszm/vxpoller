package com.mimu;

import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.*;
import org.vertx.java.platform.Verticle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by miloszmuszynski on 10/11/2014.
 */
public class PollerVerticle extends Verticle {

    public static final String VERSION = "0.0.1";
    AtomicLong receivedCounter = new AtomicLong();
    long pollMillis = 0L;

    final static int MAX_POLL = 10;

    public void start() {


        HttpServer server = vertx.createHttpServer();

        server.requestHandler(new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest req) {
                receivedCounter.set(0L);
                pollMillis = System.currentTimeMillis();

                final int maxPoll = HttpUtil.getIntParam(req, "r", MAX_POLL);

                String path = req.path();
                req.response().setChunked(true);
                req.response().setStatusCode(200);
                req.response().putHeader("Content-Type", "text/html");
                req.response().write("<html><body>");
                req.response().write("<h2>headers:</h2>");
                HttpUtil.sendHeaders(req.response(), req.headers());
                req.response().write("<h2>path:</h2>");
                req.response().write(path);
                req.response().write("<h2>maxPoll:</h2>");
                req.response().write("" + maxPoll);
                req.response().write("<h2>version:</h2>");
                req.response().write(VERSION);
                req.response().write("</body></html>");
                req.response().end();

                HttpClient client = vertx.createHttpClient().setHost("localhost").setPort(9111);//9199
                for (int i = 0; i < maxPoll; i++) {
                    doPolling(client, maxPoll);
                }
            }
        });

        server.listen(9191);

    }

    /**
     * doPolling
     */
    private void doPolling(HttpClient client, final int maxPoll){


        final HttpClientRequest request = client.post("/actionEndpoint?wsdl", new Handler<HttpClientResponse>() {
            @Override
            public void handle(HttpClientResponse resp) {
                resp.bodyHandler(new Handler<Buffer>(){
                    @Override
                    public void handle(Buffer buffer) {
                        if (receivedCounter.incrementAndGet() == maxPoll) {
                            long millis = System.currentTimeMillis() - pollMillis;
                            System.out.println("received " + maxPoll + " in " + millis + " millis  buffer = " + buffer);
                        }
                    }
                });
            }
        });

        request.setChunked(true);

        request.write("text");

        request.end();

    }

}
