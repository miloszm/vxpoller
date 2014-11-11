package com.mimu;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.*;
import org.vertx.java.platform.Verticle;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by mm on 10/11/2014.
 */
public class PollerVerticle extends Verticle {

    public static final String VERSION = "0.0.1";
    public static final String REPEAT_PARAMETER = "r";
    AtomicLong receivedCounter = new AtomicLong();// we do not need atomic as this is single threaded...
    long pollMillis = 0L;

    final static int MAX_POLL = 10;

    public void start() {


        HttpServer server = vertx.createHttpServer();

        server.requestHandler(new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest req) {
                receivedCounter.set(0L);
                pollMillis = System.currentTimeMillis();

                final int maxPoll = HttpUtil.getIntParam(req, REPEAT_PARAMETER, MAX_POLL);

                String path = req.path();
                writeConfirmationResponse(req, req.response(), maxPoll, path);

                doPolling(maxPoll);

            }
        });

        server.listen(9191);

    }


    /**
     * writes confirmation response
     */
    private static void writeConfirmationResponse(HttpServerRequest req, HttpServerResponse resp, int maxPoll, String path) {
        resp.setChunked(true);
        resp.setStatusCode(200);
        resp.putHeader("Content-Type", "text/html");
        resp.write("<html><body>");
        resp.write("<h2>headers:</h2>");
        HttpUtil.sendHeaders(resp, req.headers());
        resp.write("<h2>path:</h2>");
        resp.write(path);
        resp.write("<h2>maxPoll:</h2>");
        resp.write("" + maxPoll);
        resp.write("<h2>version:</h2>");
        resp.write(VERSION);
        resp.write("</body></html>");
        resp.end();
    }


    /**
     * do polling
     *
     * polls host/port a given number of times
     *
     * @param numPoll number of times to poll
     */
    private void doPolling(final int numPoll){
        HttpClient client = vertx.createHttpClient().setHost("localhost").setPort(9111);//9199
        for (int i = 0; i < numPoll; i++) {
            doSinglePoll(client, numPoll);
        }
    }


    /**
     * do single poll
     */
    private void doSinglePoll(HttpClient client, final int maxPoll){


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
