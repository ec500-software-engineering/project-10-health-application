package bu.edu.ec500.sshealthapp;

import android.content.Context;

import java.io.InputStream;
import bu.edu.ec500.sshealthapp.xgboost.Predictor;
import bu.edu.ec500.sshealthapp.xgboost.util.FVec;

public class XGBoostClassifier {
    private Predictor predictor;
    private double[] features;

    public static final String TYPE = "xgboost";

    public XGBoostClassifier(Context ctx) {
        try {
            InputStream is = ctx.getAssets().open("rhar.model");
            predictor = new Predictor(is);
            is.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Extract and select features from the raw sensor data points.
     * These data points are collected with certain sampling frequency and windows.
     * @param sensorData Raw sensor data points.
     * @return Extracted features.
     */
    private double[] prepareFeatures(SensorData[] sensorData, final int sampleFreq, final int sampleCount) {

        double[] matrix = new double[SensorFeature.FEATURE_COUNT];
        Feature aFeature = new Feature();
        aFeature.extractFeatures(sensorData, sampleFreq, sampleCount);
        System.arraycopy(aFeature.getFeaturesAsArray(), 0, matrix, 0, SensorFeature.FEATURE_COUNT);
        return matrix;
    }

    /**
     * Recognize current human activity based on pre-defined rules.
     * @param sensorData Raw sensor data points.
     */
    public double[] recognize(SensorData[] sensorData, final int sampleFreq, final int sampleCount) {
        features = prepareFeatures(sensorData, sampleFreq, sampleCount);
        return predict();
    }

    public double[] getCurrentFeatures(){
        return features;
    }

    private double[] predict() {
        FVec vector = FVec.Transformer.fromArray(features, true);
        return predictor.predict(vector);
    }
}
