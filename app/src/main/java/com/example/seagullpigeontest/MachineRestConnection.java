package com.example.seagullpigeontest;

import android.graphics.Bitmap;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

public class MachineRestConnection {

    private static final String link = "https://automl.googleapis.com/v1/projects/machinelearningtest-355021/locations/us-central1/models/ICN7793374701560004608:predict";
    private static final String PAYLOAD = "Payload";
    private static final String IMAGE = "image";
    private static final String IMAGE_BYTES = "imageBytes";
    private static final String PARAMS = "params";
    private static final String SCORE_THRESH_KEY = "scoreThreshold";
    private static final String SCORE_THRESH = "0.5";
    private static final String MAX_BOUND_KEY = "maxBoundingBoxCount";
    private static final String MAX_BOUND = "100";

    public static String getResults(Bitmap bitmap)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String byteString = Base64.encodeToString(bytes.toByteArray(), Base64.DEFAULT);
        try {
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL(link).openConnection();
            httpsURLConnection.setRequestMethod("POST");

            JSONObject requestBody = new JSONObject();
            JSONObject image = new JSONObject();
            JSONObject params = new JSONObject();
            params.put(SCORE_THRESH_KEY, SCORE_THRESH);
            params.put(MAX_BOUND_KEY, MAX_BOUND);
            image.put(IMAGE_BYTES, byteString);
            requestBody.put(PARAMS, params);
            requestBody.put(PAYLOAD,new JSONObject().put(IMAGE, image));
            httpsURLConnection.getOutputStream().write(requestBody.toString().getBytes(StandardCharsets.UTF_8));

            InputStream result = httpsURLConnection.getInputStream();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            if (httpsURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                return null;
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            bytesRead = result.read(buffer);
            while (bytesRead > 0)
            {
                output.write(buffer, 0, bytesRead);
                bytesRead = result.read(buffer);
            }
            output.close();
            result.close();
            return new String(output.toByteArray());
        } catch (IOException| JSONException ioException) {
            ioException.printStackTrace();
        }
        return null;
    }
}
