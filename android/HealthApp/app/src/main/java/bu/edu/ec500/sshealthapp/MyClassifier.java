package bu.edu.ec500.sshealthapp;

import android.content.Context;
import android.content.res.AssetManager;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

public class MyClassifier {
    static {
        System.loadLibrary("tensorflow_inference");
    }

    private TensorFlowInferenceInterface inferenceInterface;
    private static final String MODEL_FILE = "file:///android_asset/optimized_frozen_lstm.pb";
    private static final String INPUT_NODE = "input";
    private static final String[] OUTPUT_NODES = {"output"};
    private static final String OUTPUT_NODE = "output";
    private static final int INPUT_SIZE = 3 * 128;
    private static final int OUTPUT_SIZE = 6;

    public MyClassifier(final Context ctx) {
        AssetManager assetManager = ctx.getAssets();
        inferenceInterface = new TensorFlowInferenceInterface(assetManager, MODEL_FILE);
    }

    public static final String TYPE = "lstm";

    private float[] prepareFeatures(SensorData[] sData, final int sampleCount) {
        float[] input = new float[INPUT_SIZE];
        for(int i = 0; i < sampleCount; ++i){
            input[3 * i] = sData[i].worldAcc[0];
            input[3 * i + 1] = sData[i].worldAcc[1];
            input[3 * i + 2] = sData[i].worldAcc[2];
        }
        return input;
    }

    public double[] recognize(SensorData[] sensorData, final int sampleCount) {
        float[] input = prepareFeatures(sensorData, sampleCount);
        float[] result = new float[OUTPUT_SIZE];

        inferenceInterface.feed(INPUT_NODE,input,INPUT_SIZE);
        inferenceInterface.run(OUTPUT_NODES);
        inferenceInterface.fetch(OUTPUT_NODE,result);
        double[] doubleResult = new double[OUTPUT_SIZE];
        for(int i = 0; i < OUTPUT_SIZE; ++i){
            doubleResult[i] = (double)result[i];
        }
        return doubleResult;
    }
}
