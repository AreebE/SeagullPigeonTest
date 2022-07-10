package com.example.seagullpigeontest;

import android.graphics.Bitmap;
import android.util.Log;

//import com.google.api.client.util.Lists;
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.cloud.AuthCredentials;
//import com.google.cloud.automl.v1.AnnotationPayload;
//import com.google.cloud.automl.v1.BoundingPoly;
//import com.google.cloud.automl.v1.ExamplePayload;
//import com.google.cloud.automl.v1.Image;
//import com.google.cloud.automl.v1.ModelName;
//import com.google.cloud.automl.v1.NormalizedVertex;
//import com.google.cloud.automl.v1.PredictRequest;
//import com.google.cloud.automl.v1.PredictResponse;
//import com.google.cloud.automl.v1.PredictionServiceClient;
//import com.google.cloud.storage.Storage;
//import com.google.cloud.storage.StorageOptions;
//import com.google.protobuf.ByteString;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

//import io.grpc.Context;

public class MachineAPIConnection {

    private static final String TAG = "MachineAPIConnection";

    static String predict(Bitmap image) throws IOException {
        // TODO(developer): Replace these variables before running the sample.
        String projectId = "machinelearningtest-355021";
        String modelId = "ICN7793374701560004608";
        return predict(projectId, modelId, image);
    }

    static String predict(String projectId, String modelId, Bitmap image) throws IOException {
        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.
            // You can specify a credential file by providing a path to GoogleCredentials.
            // Otherwise credentials are read from the GOOGLE_APPLICATION_CREDENTIALS environment variable.
        FileInputStream jsonInput = new FileInputStream("src/main/assets/machinelearningtest-355021-9f9ff162001e.json");
//       Storage storage = StorageOptions.builder().authCredentials(AuthCredentials.createForJson(jsonInput)).build().service();

//
//        try  {
//            PredictionServiceClient client = PredictionServiceClient.create();            ModelName name = ModelName.of(projectId, "us-central1", modelId);
//            ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
//            image.compress(Bitmap.CompressFormat.PNG, 100, imageBytes);
//            Image i = Image.parseFrom(imageBytes.toByteArray());
//            ExamplePayload payload = ExamplePayload.newBuilder().setImage(i).build();
//            PredictRequest predictRequest =
//                    PredictRequest.newBuilder()
//                            .setName(name.toString())
//                            .setPayload(payload)
//                            .putParams(
//                                    "score_threshold", "0.5") // [0.0-1.0] Only produce results higher than this value
//                            .build();
//
//            PredictResponse response = client.predict(predictRequest);
//            String result = "";
//            for (AnnotationPayload annotationPayload : response.getPayloadList()) {
//                result = String.format("Predicted class name: %s\n", annotationPayload.getDisplayName())
//                     + String.format(
//                            "Predicted class score: %.2f\n",
//                            annotationPayload.getImageObjectDetection().getScore());
//                BoundingPoly boundingPoly = annotationPayload.getImageObjectDetection().getBoundingBox();
//                result += "\n" + "Normalized Vertices:";
//                for (NormalizedVertex vertex : boundingPoly.getNormalizedVerticesList()) {
//                    result += String.format("\tX: %.2f, Y: %.2f\n", vertex.getX(), vertex.getY());
//                }
//            }
//            return result;
//        } catch (Exception e)
//        {
//            Log.d(TAG, e.toString());
//        }
        return null;
    }
}