package bu.edu.ec500.sshealthapp;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.SymbolTable;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.SparseIntArray;

import java.util.List;

public class SensorService extends Service implements SensorEventListener {

    private SensorManager mSensorManager = null;

    private SensorData latestSampledData;

    private MyBinder mBinder = new MyBinder();

    private Handler activityRecognizeHandler = null;

    private boolean isRecognizing = false;

    private final int START_RECOGNIZING = 1;

    private final int STOP_RECOGNIZING = 2;

    private RecognizerAbstractClass mRecognizer;

    @Override
    public void onCreate() {
        super.onCreate();
        initHandler();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        latestSampledData = new SensorData();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class MyBinder extends Binder {
        void startRecognizing() {
            if (!isRecognizing) {
                registerListener();
                executeTask(START_RECOGNIZING);
                isRecognizing = true;
            }
        }

        void stopRecognizing() {
            if (isRecognizing) {
                executeTask(STOP_RECOGNIZING);
            }
        }

        void setRecognizer(RecognizerAbstractClass recog) {
            if (mRecognizer != null) {
                mRecognizer.dataFusionHandler.removeCallbacksAndMessages(null);
            }
            mRecognizer = recog;
        }

        void cleanUp() {
            latestSampledData = new SensorData();
            mRecognizer = null;
        }
    }

    private void initHandler() {
        HandlerThread activityRecognizeThread = new HandlerThread(
                "activityRecognizeThread");
        activityRecognizeThread.start();

        activityRecognizeHandler = new Handler(
                activityRecognizeThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) { handleRequest(msg); }
        };
    }

    private void handleRequest(Message msg) {
        switch (msg.what) {
            case START_RECOGNIZING:
                startActivityRecognition();
                break;
            case STOP_RECOGNIZING:
                stopActivityRecognition();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() { unregisterListener(); }

    private void executeTask(int taskType) {
        Message msg = Message.obtain(activityRecognizeHandler, taskType);
        msg.sendToTarget();
    }

    private void startActivityRecognition() {
        mRecognizer.dataFusionHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mRecognizer.dataSet[mRecognizer.dataSetIndex] == null) {
                    mRecognizer.dataSet[mRecognizer.dataSetIndex] = new SensorData();
                    mRecognizer.dataSet[mRecognizer.dataSetIndex].clone(latestSampledData);
                    mRecognizer.dataSet[mRecognizer.dataSetIndex].timestamp = System.currentTimeMillis();
                    mRecognizer.dataSet[mRecognizer.dataSetIndex].calculateWorldAcc();
                    mRecognizer.dataSetIndex++;
                    if (mRecognizer.dataSetIndex == mRecognizer.getSamplingPointCount() / 2) {
                        mRecognizer.dataSetIndex = 0;
                    }
                } else {
                    int halfIndex = mRecognizer.getSamplingPointCount() / 2;
                    if (mRecognizer.dataSet[halfIndex + mRecognizer.dataSetIndex] != null) {
                        mRecognizer.dataSet[mRecognizer.dataSetIndex].clone(
                                mRecognizer.dataSet[halfIndex + mRecognizer.dataSetIndex]);
                    }
                    mRecognizer.dataSet[halfIndex + mRecognizer.dataSetIndex] = new SensorData();
                    mRecognizer.dataSet[halfIndex + mRecognizer.dataSetIndex].clone(latestSampledData);
                    mRecognizer.dataSet[halfIndex + mRecognizer.dataSetIndex].timestamp = System.currentTimeMillis();
                    mRecognizer.dataSet[halfIndex + mRecognizer.dataSetIndex].calculateWorldAcc();
                    if (mRecognizer.dataSetIndex == halfIndex - 1) {
                        try {
                            mRecognizer.recognitionHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mRecognizer.onResult(mRecognizer.myClassifier.recognize(
                                            mRecognizer.dataSet,
                                            mRecognizer.getSamplingPointCount()));
                                }
                            });
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                    mRecognizer.dataSetIndex = (mRecognizer.dataSetIndex + 1) % (halfIndex);
                    if (!isRecognizing) {
                        mRecognizer.resetRecognizer();
                    }
                }
                mRecognizer.dataFusionHandler.postDelayed(this, mRecognizer.getSamplingInterval());
            }
        });
    }

    private void stopActivityRecognition() {
        mRecognizer.dataFusionHandler.removeCallbacksAndMessages(null);
        unregisterListener();
        isRecognizing = false;
    }

    private void registerListener() {
        SparseIntArray registeredSensors = mRecognizer.getAllSensor();
        for (int sensorIndex = 0; sensorIndex < registeredSensors.size(); ++sensorIndex) {
            checkAndRegisterSensor(registeredSensors.keyAt(sensorIndex));
        }
    }

    private void checkAndRegisterSensor(int sensorType) {
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        Sensor sensor;
        for (int index = 0; index < sensors.size(); ++index) {
            sensor = sensors.get(index);
            if (sensor.getType() == sensorType) {
                mSensorManager.registerListener(this, sensor,
                        SensorManager.SENSOR_DELAY_FASTEST);
            }
        }
    }

    private void unregisterListener() {mSensorManager.unregisterListener(this);}

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            updateSensorData(latestSampledData.accelerate, event);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            updateSensorData(latestSampledData.gyroscope, event);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            updateSensorData(latestSampledData.magnetic, event);
        } else if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            updateSensorData(latestSampledData.gameRotation, event);
        }
    }

    private void updateSensorData(float[] D, SensorEvent event) {
        if (D == null) {
            D = new float[3];
        }
        System.arraycopy(event.values, 0, D, 0, 3);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
