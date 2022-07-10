package com.example.seagullpigeontest;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        try {
            ImageClassifier classifier = new ImageClassifier(this);
            ImageView imageView = (ImageView) findViewById(R.id.test_pigeon);
            Executors.newSingleThreadExecutor().execute(
                    new Runnable() {
                        @Override
                        public void run() {
                            String message = "";
                            try {
//                                message = MachineAPIConnection.predict(((BitmapDrawable) imageView.getDrawable()).getBitmap());
//                                message = classifier.classifyFrame(((BitmapDrawable) imageView.getDrawable()).getBitmap());
                                message = MachineRestConnection.getResults(((BitmapDrawable) imageView.getDrawable()).getBitmap());
                            } catch (Exception ioException) {
                                ioException.printStackTrace();
                            }
                            final String result = message;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
//                                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, result);
                                }
                            });
                        }
                    }
            );

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}