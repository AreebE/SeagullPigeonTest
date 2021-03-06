package com.example.seagullpigeontest;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Delegate;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.ops.CastOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;


/** Classifies images with Tensorflow Lite. */
public class ImageClassifier {

    /** Tag for the {@link Log}. */
    private static final String TAG = "TfLiteCameraDemo";

    /** Name of the model file stored in Assets. */
    private static final String MODEL_PATH = "pigeonAndBirdSet.tflite";

    /** Name of the label file stored in Assets. */
    private static final String LABEL_PATH = "birdLabels.txt";

    /** Number of results to show in the UI. */
    private static final int RESULTS_TO_SHOW = 3;

    /** Dimensions of inputs. */
    private static final int DIM_BATCH_SIZE = 1;

    private static final int DIM_PIXEL_SIZE = 3;

    static final int DIM_IMG_SIZE_X = 224;
    static final int DIM_IMG_SIZE_Y = 224;

//    private static final int IMAGE_MEAN = 128;
//    private static final float IMAGE_STD = 128.0f;


    /* Preallocated buffers for storing image data in. */
    private int[] intValues = new int[1];

    private ImageProcessor imageProcessor;
    /** An instance of the driver class to run model inference with Tensorflow Lite. */
    private Interpreter tflite;

    /** Labels corresponding to the output of the vision model. */
    private List<String> labelList;

    /** A ByteBuffer to hold image data, to be feed into Tensorflow Lite as inputs. */
    private ByteBuffer imgData = null;

    /** An array to hold inference results, to be feed into Tensorflow Lite as outputs. */
    private byte[][] labelProbArray = null;
//    private float[][] labelProbArray = null;

    /** multi-stage low pass filter **/
    private byte[][] filterLabelProbArray = null;
//    private float[][] filterLabelProbArray = null;

    private static final int FILTER_STAGES = 3;
    private static final float FILTER_FACTOR = 0.4f;

    private PriorityQueue<Map.Entry<String, Byte>> sortedLabels =
            new PriorityQueue<>(
                    RESULTS_TO_SHOW,
                    new Comparator<Map.Entry<String, Byte>>() {
                        @Override
                        public int compare(Map.Entry<String, Byte> o1, Map.Entry<String, Byte> o2) {
                            return (o1.getValue()).compareTo(o2.getValue());
                        }
                    });
//
//    private PriorityQueue<Map.Entry<String, Float>> sortedLabels =
//            new PriorityQueue<>(
//                    RESULTS_TO_SHOW,
//                    new Comparator<Map.Entry<String, Float>>() {
//                        @Override
//                        public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
//                            return (o1.getValue()).compareTo(o2.getValue());
//                        }
//                    });

    /** Initializes an {@code ImageClassifier}. */
    ImageClassifier(Activity activity) throws IOException {
        tflite = new Interpreter(loadModelFile(activity));
        labelList = loadLabelList(activity);
        imageProcessor = new ImageProcessor.Builder()
                    .add(new ResizeOp(DIM_IMG_SIZE_Y, DIM_IMG_SIZE_X, ResizeOp.ResizeMethod.BILINEAR))
//                    .add(new CastOp(DataType.UINT8))

                .add(new NormalizeOp(0, 255))
                .add(new CastOp(DataType.FLOAT32))
                    .build();
        labelProbArray = new byte[1][labelList.size()];
        filterLabelProbArray = new byte[FILTER_STAGES][labelList.size()];
//        labelProbArray = new float[1][labelList.size()];
//        filterLabelProbArray = new float[FILTER_STAGES][labelList.size()];
        Log.d(TAG, "Created a Tensorflow Lite Image Classifier.");
    }

    /** Classifies a frame from the preview stream. */
    String classifyFrame(Bitmap bitmap) {
        if (tflite == null) {
            Log.e(TAG, "Image classifier has not been initialized; Skipped.");
            return "Uninitialized Classifier.";
        }
        convertBitmapToByteBuffer(bitmap);
        // Here's where the magic happens!!!
        for (int i = 0; i < labelProbArray.length; i++)
        {
            Log.d(TAG, Arrays.toString(labelProbArray[i]));
        }

        long startTime = SystemClock.uptimeMillis();
        tflite.run(imgData, labelProbArray);
        long endTime = SystemClock.uptimeMillis();
        Log.d(TAG, "Timecost to run model inference: " + Long.toString(endTime - startTime));
        for (int i = 0; i < labelProbArray.length; i++)
        {
            Log.d(TAG, Arrays.toString(labelProbArray[i]));
        }
        // smooth the results
        applyFilter();

        // print the results
        String textToShow = printTopKLabels();
        textToShow = Long.toString(endTime - startTime) + "ms" + textToShow;
        return textToShow;
    }

    void applyFilter(){
        int num_labels =  labelList.size();

//         Low pass filter `labelProbArray` into the first stage of the filter.
        for(int j=0; j<num_labels; ++j){
            filterLabelProbArray[0][j] += FILTER_FACTOR*(labelProbArray[0][j] -
                    filterLabelProbArray[0][j]);
        }
        // Low pass filter each stage into the next.
        for (int i=1; i<FILTER_STAGES; ++i){
            for(int j=0; j<num_labels; ++j){
                filterLabelProbArray[i][j] += FILTER_FACTOR*(
                        filterLabelProbArray[i-1][j] -
                                filterLabelProbArray[i][j]);

            }
        }

        // Copy the last stage filter output back to `labelProbArray`.
        for(int j=0; j<num_labels; ++j){
            labelProbArray[0][j] = filterLabelProbArray[FILTER_STAGES-1][j];
        }
    }

    /** Closes tflite to release resources. */
    public void close() {
        tflite.close();
        tflite = null;
    }

    /** Reads label list from Assets. */
    private List<String> loadLabelList(Activity activity) throws IOException {
        List<String> labelList = new ArrayList<String>();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(activity.getAssets().open(LABEL_PATH)));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
//        Log.d(TAG, labelList.toString());
        return labelList;
    }

    /** Memory-map the model file in Assets. */
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_PATH);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    /** Writes Image data into a {@code ByteBuffer}. */
    private void convertBitmapToByteBuffer(Bitmap b) {
        b = Bitmap.createScaledBitmap(b, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, false);
        TensorImage ti = new TensorImage(DataType.UINT8);
//        TensorImage ti = new TensorImage(DataType.UINT8);

        ti.load(b);
        ti = imageProcessor.process(ti);

        Bitmap bitmap = ti.getBitmap();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        imgData =
                ByteBuffer.allocateDirect(
                          DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE * DIM_BATCH_SIZE);

        imgData.order(ByteOrder.nativeOrder());
//        if (imgData == null) {
//            return;
//        }
        imgData.rewind();
        imgData.limit(width * height * 3);
        Log.d(TAG, bitmap.toString() + ", " + bitmap.getHeight());
        intValues = new int[bitmap.getWidth() * bitmap.getHeight()];

        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        // Convert the image to floating point.
        int pixel = 0;
        Log.d(TAG, width * height + "");
        Log.d(TAG,imgData.limit() +" = limit");
        long startTime = SystemClock.uptimeMillis();
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                final int val = intValues[pixel++];
//                imgData.putFloat((((val >> 16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
//                imgData.putFloat((((val >> 8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
//                imgData.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
//                Log.d(TAG, "i = " + i + ", j = " + j);
                imgData.put((byte) ((val >> 16) & 0xFF));
                imgData.put((byte) ((val >> 8) & 0xFF));
                imgData.put((byte) ((val) & 0xFF));
//                imgData.put(((val >> 16) & 0xFF)255);
//                imgData.put(((val >> 8) & 0xFF) * 255);
//                imgData.put(((val) & 0xFF) * 255);
            }
        }
        long endTime = SystemClock.uptimeMillis();
        Log.d(TAG, "Timecost to put values into ByteBuffer: " + Long.toString(endTime - startTime));
    }

    /** Prints top-K labels, to be shown in UI as the results. */
    private String printTopKLabels() {
        for (int i = 0; i < labelList.size(); ++i) {
            sortedLabels.add(
                    new AbstractMap.SimpleEntry<>(labelList.get(i), labelProbArray[0][i]));
            if (sortedLabels.size() > RESULTS_TO_SHOW) {
                sortedLabels.poll();
            }
        }
        String textToShow = "";
        final int size = sortedLabels.size();
        for (int i = 0; i < size; ++i) {
            Map.Entry<String, Byte> label = sortedLabels.poll();
//            Map.Entry<String, Float> label = sortedLabels.poll();

            textToShow = String.format("\n%s: %d",label.getKey(),label.getValue()) + textToShow;
        }
        return textToShow;
    }
}
