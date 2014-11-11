package com.mimu;

import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by mm on 10/11/2014.
 */
public class HttpUtil {

    public static void sendHeaders(HttpServerResponse response, MultiMap headers){
        List<String> headerList = sliceMultiMap(headers);
        for (String headerStr: headerList) {
            response.write(headerStr);
        }
    }

    private static List<String> sliceMultiMap(MultiMap headers){
        List<String> list = new ArrayList<>();
        for (Map.Entry<String,String> e: headers.entries()){
            list.add("<p>" + e.getKey() + ": " + e.getValue() + "</p>\n");
        }
        return list;
    }

    /**
     * get int param if present or a default value
     */
    public static int getIntParam(HttpServerRequest req, String paramName, int defaultValue){

        int intParam;

        String paramText = req.params().get(paramName);
        if (paramText == null || paramText.isEmpty()){
            intParam = defaultValue;
        }
        else {
            try {
                intParam = Integer.parseInt(paramText);
            }
            catch(NumberFormatException e){
                intParam = defaultValue;
            }

        }

        return intParam;
    }



}
